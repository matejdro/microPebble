// Package is intentionally short to reduce image file name
package com.matejdro.micropebble.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import app.cash.paparazzi.DeviceConfig.Companion.PIXEL_5
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.TestName
import com.airbnb.android.showkase.models.Showkase
import com.airbnb.android.showkase.models.ShowkaseBrowserComponent
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import com.matejdro.micropebble.showkase.getMetadata
import org.junit.runner.RunWith

@Suppress("JUnitMalformedDeclaration")
@RunWith(TestParameterInjector::class)
abstract class TestsBase {
   object PreviewProvider : TestParameterValuesProvider() {
      override fun provideValues(context: Context): List<*> {
         val splitIndex = context.getOtherAnnotation(SplitIndex::class.java).index
         val totalSplits = System.getProperty("maxParallelForks")?.toInt() ?: error("Missing maxParallelForks property")

         val allComponents = Showkase.getMetadata().componentList
         val perSplit = allComponents.size / totalSplits

         val start = splitIndex * perSplit
         val end = if (splitIndex == totalSplits - 1) {
            allComponents.size
         } else {
            start + perSplit
         }

         val components = allComponents
            .subList(start, end)
            .map { TestKey(it) }

         for (i in components.indices) {
            for (j in components.indices) {
               if (i != j && components[i].key == components[j].key) {
                  throw AssertionError("Duplicate @Preview: '${components[i].key}'")
               }
            }
         }

         return components
      }
   }

   data class TestKey(val showkaseBrowserComponent: ShowkaseBrowserComponent) {
      val key = with(showkaseBrowserComponent) {
         componentName + (styleName?.let { "-$it" }.orEmpty())
      }

      override fun toString(): String = key
   }

   protected open fun test(

      testKey: TestKey,
   ) {
      val paparazzi = Paparazzi(
         deviceConfig = PIXEL_5,
         theme = "android:Theme.Material.Light.NoActionBar",
         showSystemUi = false,
         renderingMode = SessionParams.RenderingMode.SHRINK,
      )

      try {
         paparazzi.setup(
            testName = TestName(
               packageName = "",
               className = "",
               methodName = testKey.toString().substringBefore("(")
            )
         )
         val composable = @Composable {
            CompositionLocalProvider(LocalInspectionMode provides true) {
               testKey.showkaseBrowserComponent.component()
            }
         }

         fun snapshot(suffix: String? = null) {
            paparazzi.snapshot(name = suffix) {
               composable()
            }
         }

         snapshot()

         paparazzi.unsafeUpdateConfig(
            PIXEL_5.copy(
               nightMode = NightMode.NIGHT
            )
         )
         snapshot("night")

         paparazzi.unsafeUpdateConfig(
            PIXEL_5.copy(
               ydpi = 600,
               xdpi = 300,
               screenWidth = 300 * 440 / 160,
               screenHeight = 600 * 440 / 160,
               nightMode = NightMode.NOTNIGHT
            )
         )
         snapshot("small")

         paparazzi.unsafeUpdateConfig(
            PIXEL_5.copy(
               fontScale = 1.5f
            )
         )
         snapshot("largefont")
      } finally {
         paparazzi.teardown()
      }
   }

   annotation class SplitIndex(val index: Int)
}
