package io.github.beppe2880.opencodemobile.feature.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import io.github.beppe2880.opencodemobile.core.domain.PromptAttachment

/**
 * The system photo and file pickers. Picked files become `data:` URI attachments, which the
 * server accepts inline; large photos are downscaled and other files are capped in size.
 */
@Stable
internal interface AttachmentPicker {
    fun pickPhotos()

    fun pickFiles()
}

/** [onRead] gets one entry per picked file, null for a file that could not be read or is too large. */
@Composable
internal expect fun rememberAttachmentPicker(onRead: (List<PromptAttachment?>) -> Unit): AttachmentPicker

internal object AttachmentLimits {
    const val MAX_BYTES = 10 * 1024 * 1024
    const val MAX_IMAGE_SIDE = 2048
    const val KEEP_IMAGE_BYTES = 1536 * 1024
    const val JPEG_QUALITY = 85
    const val MAX_PICKED = 5
}
