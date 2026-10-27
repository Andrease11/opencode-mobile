package io.github.beppe2880.opencodemobile.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import io.github.beppe2880.opencodemobile.core.domain.PromptAttachment
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.UniformTypeIdentifiers.UTTypeItem
import platform.UniformTypeIdentifiers.conformsToType
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlin.coroutines.resume
import kotlin.io.encoding.Base64
import kotlin.math.max

@Composable
internal actual fun rememberAttachmentPicker(onRead: (List<PromptAttachment?>) -> Unit): AttachmentPicker {
    val scope = rememberCoroutineScope()
    val currentOnRead by rememberUpdatedState(onRead)
    return remember(scope) { IosAttachmentPicker(scope) { currentOnRead(it) } }
}

/** PHPicker for photos and the document picker for files, presented over the top view controller. */
private class IosAttachmentPicker(
    private val scope: CoroutineScope,
    private val onRead: (List<PromptAttachment?>) -> Unit,
) : AttachmentPicker {

    // UIKit keeps delegates weakly: these references keep them alive while a picker is shown.
    private val photoDelegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
        override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
            picker.dismissViewControllerAnimated(true, completion = null)
            val results = didFinishPicking.filterIsInstance<PHPickerResult>()
            if (results.isEmpty()) return
            scope.launch {
                val read = results.map { result -> async { readPhoto(result) } }.awaitAll()
                onRead(read)
            }
        }
    }

    private val documentDelegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
        override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
            val urls = didPickDocumentsAtURLs.filterIsInstance<NSURL>()
            if (urls.isEmpty()) return
            scope.launch {
                onRead(withContext(Dispatchers.IO) { urls.map { readFile(it) } })
            }
        }
    }

    override fun pickPhotos() {
        val configuration = PHPickerConfiguration().apply {
            filter = PHPickerFilter.imagesFilter
            selectionLimit = AttachmentLimits.MAX_PICKED.toLong()
        }
        val picker = PHPickerViewController(configuration = configuration)
        picker.delegate = photoDelegate
        topViewController()?.presentViewController(picker, animated = true, completion = null)
    }

    override fun pickFiles() {
        val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeItem), asCopy = true)
        picker.allowsMultipleSelection = true
        picker.delegate = documentDelegate
        topViewController()?.presentViewController(picker, animated = true, completion = null)
    }
}

private suspend fun readPhoto(result: PHPickerResult): PromptAttachment? {
    val provider = result.itemProvider
    val type = provider.registeredTypeIdentifiers.filterIsInstance<String>()
        .firstOrNull { UTType.typeWithIdentifier(it)?.conformsToType(UTTypeImage) == true }
        ?: return null
    val data = suspendCancellableCoroutine<NSData?> { continuation ->
        provider.loadDataRepresentationForTypeIdentifier(type) { data, _ -> continuation.resume(data) }
    } ?: return null
    val mime = UTType.typeWithIdentifier(type)?.preferredMIMEType ?: "image/jpeg"
    val extension = UTType.typeWithIdentifier(type)?.preferredFilenameExtension ?: "jpg"
    val name = (provider.suggestedName ?: "photo") + "." + extension
    return withContext(Dispatchers.Default) { AttachmentEncoder.encode(data, mime, name) }
}

@OptIn(ExperimentalForeignApi::class)
private fun readFile(url: NSURL): PromptAttachment? {
    val scoped = url.startAccessingSecurityScopedResource()
    try {
        val data = NSData.dataWithContentsOfURL(url) ?: return null
        val extension = url.pathExtension.orEmpty()
        val mime = UTType.typeWithFilenameExtension(extension)?.preferredMIMEType ?: "application/octet-stream"
        return AttachmentEncoder.encode(data, mime, url.lastPathComponent ?: "file")
    } finally {
        if (scoped) url.stopAccessingSecurityScopedResource()
    }
}

/**
 * Turns file data into a `data:` URI attachment, with the same limits as on Android. Large
 * photos, and formats that models rarely accept (HEIC), become JPEGs at most
 * [AttachmentLimits.MAX_IMAGE_SIDE] pixels on the longer side.
 */
@OptIn(ExperimentalForeignApi::class)
private object AttachmentEncoder {
    private val PASSTHROUGH_IMAGES = setOf("image/jpeg", "image/png", "image/gif", "image/webp")

    fun encode(data: NSData, mime: String, name: String): PromptAttachment? {
        if (data.length.toLong() > AttachmentLimits.MAX_BYTES) return null
        val image = if (mime.startsWith("image/") && mime != "image/gif") UIImage.imageWithData(data) else null
        val (bytes, type, fileName) = if (image != null && (mime !in PASSTHROUGH_IMAGES || needsDownscale(data, image))) {
            val jpeg = downscale(image) ?: return null
            Triple(jpeg.toByteArray(), "image/jpeg", name.substringBeforeLast('.') + ".jpg")
        } else {
            Triple(data.toByteArray(), mime, name)
        }
        return PromptAttachment(uri = "data:$type;base64," + Base64.Default.encode(bytes), name = fileName, mime = type)
    }

    private fun needsDownscale(data: NSData, image: UIImage): Boolean {
        if (data.length.toLong() > AttachmentLimits.KEEP_IMAGE_BYTES) return true
        val side = image.size.useContents { max(width, height) } * image.scale
        return side > AttachmentLimits.MAX_IMAGE_SIDE
    }

    /** Redraws [image] (applying its EXIF orientation) with the longer side capped. */
    private fun downscale(image: UIImage): NSData? {
        val (width, height) = image.size.useContents { width to height }
        val side = max(width, height) * image.scale
        val scale = if (side > AttachmentLimits.MAX_IMAGE_SIDE) AttachmentLimits.MAX_IMAGE_SIDE / side else 1.0
        val targetWidth = width * image.scale * scale
        val targetHeight = height * image.scale * scale
        val format = UIGraphicsImageRendererFormat.defaultFormat().apply { this.scale = 1.0 }
        val renderer = UIGraphicsImageRenderer(size = CGSizeMake(targetWidth, targetHeight), format = format)
        val output = renderer.imageWithActions { _ -> image.drawInRect(CGRectMake(0.0, 0.0, targetWidth, targetHeight)) }
        return UIImageJPEGRepresentation(output, AttachmentLimits.JPEG_QUALITY / 100.0)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    return ByteArray(size).apply { usePinned { memcpy(it.addressOf(0), bytes, length) } }
}

/** The view controller currently on top of the key window, to present pickers from. */
internal fun topViewController(): UIViewController? {
    val window = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .flatMap { scene -> scene.windows.filterIsInstance<UIWindow>() }
        .firstOrNull { it.isKeyWindow() }
    var controller = window?.rootViewController
    while (controller?.presentedViewController != null) controller = controller.presentedViewController
    return controller
}
