package io.github.beppe2880.opencodemobile.feature.chat

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface

internal actual fun decodeImage(bytes: ByteArray, maxSide: Int): ImageBitmap? {
    val image = runCatching { Image.makeFromEncoded(bytes) }.getOrNull() ?: return null
    var sample = 1
    while (maxOf(image.width, image.height) / (sample * 2) >= maxSide) sample *= 2
    if (sample == 1) return image.toComposeImageBitmap()
    val width = image.width / sample
    val height = image.height / sample
    val surface = Surface.makeRasterN32Premul(width, height)
    surface.canvas.drawImageRect(image, Rect.makeWH(width.toFloat(), height.toFloat()))
    return surface.makeImageSnapshot().toComposeImageBitmap()
}

internal actual fun edgeToEdgeDialogProperties() = DialogProperties(usePlatformDefaultWidth = false, usePlatformInsets = false)
