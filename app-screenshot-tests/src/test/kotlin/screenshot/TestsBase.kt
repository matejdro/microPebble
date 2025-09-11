// Package is intentionally short to reduce image file name
package screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.DeviceConfig.Companion.PIXEL_5
import app.cash.paparazzi.Paparazzi
import com.airbnb.android.showkase.models.ShowkaseBrowserComponent
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@Suppress("JUnitMalformedDeclaration")
@RunWith(TestParameterInjector::class)
abstract class TestsBase {
   @get:Rule
   val paparazzi = Paparazzi(
      deviceConfig = DeviceConfig.PIXEL_5,
      theme = "android:Theme.Material.Light.NoActionBar",
      maxPercentDifference = 0.0,
      showSystemUi = false,
      renderingMode = SessionParams.RenderingMode.SHRINK
   )

   object PreviewProvider : TestParameterValuesProvider() {
      override fun provideValues(context: Context): List<*> {
//         TODO uncomment this when you have at least one preview marked with @ShowkaseComposable
//         val splitIndex = context.getOtherAnnotation(SplitIndex::class.java).index
//         val whitelistedPackages = Splits.paparazziSplits.elementAt(splitIndex)
//
//         val components = Showkase.getMetadata().componentList
//            .filter { showkaseBrowserComponent ->
//               val isInSplit = if (whitelistedPackages.isNotEmpty()) {
//                  whitelistedPackages.any { showkaseBrowserComponent.componentKey.startsWith(it) }
//               } else {
//                  val blacklistedPackages = Splits.paparazziSplits.flatten()
//                  blacklistedPackages.all { !showkaseBrowserComponent.componentKey.startsWith(it) }
//               }
//
//               isInSplit && showkaseBrowserComponent.group != "Default Group"
//            }
//            .map { TestKey(it) }
         val components = emptyList<TestKey>()

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

   @Before
   fun setUp() {
      // Note: if you have lottie in your project, uncomment this
      // Workaround for the https://github.com/cashapp/paparazzi/issues/630
      // LottieTask.EXECUTOR = Executor(Runnable::run)
   }

   protected open fun test(

      testKey: TestKey,
   ) {
      val composable = @Composable {
         CompositionLocalProvider(LocalInspectionMode provides true) {
            testKey.showkaseBrowserComponent.component()
         }
      }

      paparazzi.snapshot {
         composable()
      }
      paparazzi.unsafeUpdateConfig(
         DeviceConfig.PIXEL_5.copy(
            nightMode = NightMode.NIGHT
         )
      )
      paparazzi.snapshot("night") {
         composable()
      }
      paparazzi.unsafeUpdateConfig(
         PIXEL_5.copy(
            ydpi = 600,
            xdpi = 300,
            screenWidth = 300 * 440 / 160,
            screenHeight = 600 * 440 / 160,
            nightMode = NightMode.NOTNIGHT
         )
      )
      paparazzi.snapshot("small") {
         composable()
      }
      paparazzi.unsafeUpdateConfig(
         PIXEL_5.copy(
            fontScale = 1.5f
         )
      )
      paparazzi.snapshot("largefont") {
         composable()
      }
   }

   annotation class SplitIndex(val index: Int)
}
