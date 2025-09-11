package com.matejdro.micropebble.ui.debugging

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import coil3.ColorImage
import coil3.ImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.compose.asPainter
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.pxOrElse
import com.matejdro.micropebble.ui.theme.MicroPebbleTheme
import si.inova.kotlinova.compose.time.ComposeAndroidDateTimeFormatter
import si.inova.kotlinova.compose.time.LocalDateFormatter
import si.inova.kotlinova.core.time.AndroidDateTimeFormatter
import si.inova.kotlinova.core.time.FakeAndroidDateTimeFormatter

@OptIn(DelicateCoilApi::class, ExperimentalCoilApi::class)
@Composable
@Suppress("ModifierMissing") // This is intentional
fun PreviewTheme(
   formatter: AndroidDateTimeFormatter = FakeAndroidDateTimeFormatter(),
   fill: Boolean = true,
   content: @Composable () -> Unit,
) {
   CompositionLocalProvider(
      LocalDateFormatter provides ComposeAndroidDateTimeFormatter(formatter),
      LocalAsyncImagePreviewHandler provides ColorCyclingAsyncImagePreviewHandler()
   ) {
      // Disable Material You on previews (and screenshot tests) to improve reproducibility
      MicroPebbleTheme(dynamicColor = false) {
         Surface(modifier = if (fill) Modifier.fillMaxSize() else Modifier, content = content)
      }
   }
}

@OptIn(ExperimentalCoilApi::class)
private class ColorCyclingAsyncImagePreviewHandler(
   /**
    * List of [ColorInt] colors that are displayed as part of the prevew
    */
   private val colors: List<Int> = listOf(
      Color.RED,
      Color.GREEN,
      Color.BLUE,
      Color.GRAY,
      Color.CYAN,
      Color.YELLOW,
      Color.MAGENTA
   ),
) : AsyncImagePreviewHandler {
   @ColorInt
   private var currentColor = 0

   override suspend fun handle(imageLoader: ImageLoader, request: ImageRequest): AsyncImagePainter.State {
      val nextColor = colors[currentColor++ % colors.size]
      val size = request.sizeResolver.size()

      val image = ColorImage(nextColor, size.width.pxOrElse { 0 }, size.height.pxOrElse { 0 })

      return AsyncImagePainter.State.Success(image.asPainter(request.context), SuccessResult(image, request))
   }
}
