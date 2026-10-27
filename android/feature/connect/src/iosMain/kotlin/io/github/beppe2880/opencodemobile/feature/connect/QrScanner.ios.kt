package io.github.beppe2880.opencodemobile.feature.connect

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSURL
import platform.QuartzCore.CATransaction
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIView
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue

@Composable
internal actual fun rememberCameraAccess(): CameraAccess {
    val hasCamera = remember { AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo) != null }
    var status by remember { mutableStateOf(AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) }
    // The user can grant the permission from the system settings at any time.
    LifecycleResumeEffect(Unit) {
        status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        onPauseOrDispose { }
    }
    return object : CameraAccess {
        override val hasCamera = hasCamera
        override val granted = status == AVAuthorizationStatusAuthorized
        override val denied = status == AVAuthorizationStatusDenied || status == AVAuthorizationStatusRestricted

        override fun request() {
            if (status != AVAuthorizationStatusNotDetermined) return
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { _ ->
                dispatch_async(dispatch_get_main_queue()) {
                    status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
                }
            }
        }

        override fun openSettings() {
            val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
            UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun CameraPreview(onResult: (String) -> Unit) {
    val currentOnResult by rememberUpdatedState(onResult)
    val scanner = remember { QrCaptureSession { currentOnResult(it) } }
    DisposableEffect(scanner) {
        scanner.start()
        onDispose { scanner.stop() }
    }
    UIKitView(
        factory = { PreviewView(scanner.previewLayer) },
        modifier = Modifier.fillMaxSize(),
    )
}

/** Back camera session with a QR metadata output; reports the first code once, on the main thread. */
@OptIn(ExperimentalForeignApi::class)
private class QrCaptureSession(private val onCode: (String) -> Unit) {
    private val session = AVCaptureSession()
    private var delivered = false
    private val delegate = object : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
        override fun captureOutput(output: AVCaptureOutput, didOutputMetadataObjects: List<*>, fromConnection: AVCaptureConnection) {
            if (delivered) return
            val text = didOutputMetadataObjects
                .filterIsInstance<AVMetadataMachineReadableCodeObject>()
                .firstOrNull { it.type == AVMetadataObjectTypeQRCode }
                ?.stringValue
                ?: return
            delivered = true
            onCode(text)
        }
    }

    val previewLayer = AVCaptureVideoPreviewLayer(session = session).apply {
        videoGravity = AVLayerVideoGravityResizeAspectFill
    }

    init {
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        val input = device?.let { AVCaptureDeviceInput.deviceInputWithDevice(it, error = null) }
        if (input != null && session.canAddInput(input)) {
            session.addInput(input)
            val output = AVCaptureMetadataOutput()
            if (session.canAddOutput(output)) {
                session.addOutput(output)
                output.setMetadataObjectsDelegate(delegate, queue = dispatch_get_main_queue())
                if (output.availableMetadataObjectTypes.contains(AVMetadataObjectTypeQRCode)) {
                    output.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
                }
            }
        }
    }

    // startRunning and stopRunning block, so they run off the main thread.
    fun start() {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) { session.startRunning() }
    }

    fun stop() {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) { session.stopRunning() }
    }
}

/** Hosts the preview layer and keeps it sized to the view. */
@OptIn(ExperimentalForeignApi::class)
private class PreviewView(private val previewLayer: AVCaptureVideoPreviewLayer) : UIView(frame = CGRectZero.readValue()) {
    init {
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        previewLayer.frame = bounds
        CATransaction.commit()
    }
}
