package com.matejdro.micropebble

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.matejdro.micropebble.ui.theme.MicroPebbleTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import si.inova.kotlinova.compose.result.LocalResultPassingStore
import si.inova.kotlinova.compose.result.ResultPassingStore
import si.inova.kotlinova.compose.time.ComposeAndroidDateTimeFormatter
import si.inova.kotlinova.compose.time.LocalDateFormatter
import si.inova.kotlinova.core.time.AndroidDateTimeFormatter
import si.inova.kotlinova.navigation.deeplink.HandleNewIntentDeepLinks
import si.inova.kotlinova.navigation.deeplink.MainDeepLinkHandler
import si.inova.kotlinova.navigation.di.NavigationContext
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.simplestack.RootNavigationContainer

@Stable
class MainActivity : ComponentActivity() {
   private lateinit var navigationInjectionFactory: NavigationInjection.Factory
   private lateinit var mainDeepLinkHandler: MainDeepLinkHandler
   private lateinit var navigationContext: NavigationContext
   private lateinit var dateFormatter: AndroidDateTimeFormatter
   private lateinit var mainViewModelFactory: MainViewModel.Factory

   private val viewModel by viewModels<MainViewModel>() { ViewModelFactory() }
   private var initComplete = false

   override fun onCreate(savedInstanceState: Bundle?) {
      val appGraph = (requireNotNull(application) as MicroPebbleApplication).applicationGraph

      navigationInjectionFactory = appGraph.getNavigationInjectionFactory()
      mainDeepLinkHandler = appGraph.getMainDeepLinkHandler()
      navigationContext = appGraph.getNavigationContext()
      dateFormatter = appGraph.getDateFormatter()
      mainViewModelFactory = appGraph.getMainViewModelFactory()

      super.onCreate(savedInstanceState)
      enableEdgeToEdge()

      val splashScreen = installSplashScreen()
      splashScreen.setKeepOnScreenCondition { !initComplete }

      beginInitialisation(savedInstanceState == null)
   }

   private fun beginInitialisation(startup: Boolean) {
      lifecycleScope.launch {
         val initialHistory: ImmutableList<ScreenKey> = persistentListOf(viewModel.startingScreen.filterNotNull().first())

         val deepLinkTarget = if (startup) {
            intent?.data?.let { mainDeepLinkHandler.handleDeepLink(it, startup = true) }
         } else {
            null
         }

         val overridenInitialHistoryFromDeepLink = if (deepLinkTarget != null) {
            deepLinkTarget.performNavigation(initialHistory, navigationContext).newBackstack.toPersistentList()
         } else {
            initialHistory
         }

         setContent {
            NavigationRoot(overridenInitialHistoryFromDeepLink)
         }

         initComplete = true
      }
   }

   @Composable
   private fun NavigationRoot(initialHistory: ImmutableList<ScreenKey>) {
      val mainState = viewModel.mainState.collectAsStateWithLifecycle().value
      if (mainState != null) {
         RequestStartupPermissions(mainState)
      }

      MicroPebbleTheme {
         // A surface container using the 'background' color from the theme
         Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
         ) {
            val resultPassingStore = rememberSaveable { ResultPassingStore() }
            CompositionLocalProvider(
               LocalDateFormatter provides ComposeAndroidDateTimeFormatter(dateFormatter),
               LocalResultPassingStore provides resultPassingStore
            ) {
               val backstack = navigationInjectionFactory.RootNavigationContainer(
                  initialHistory = { initialHistory },
                  screenWrapper = { _, screen ->
                     Surface {
                        screen()
                     }
                  }
               )

               mainDeepLinkHandler.HandleNewIntentDeepLinks(this@MainActivity, backstack)
            }
         }
      }
   }

   @Composable
   private fun RequestStartupPermissions(mainState: MainState) {
      val permissions = rememberMultiplePermissionsState(
         listOfNotNull(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) android.Manifest.permission.POST_NOTIFICATIONS else null,
         )
      ) {}

      LaunchedEffect(Unit) {
         if (!permissions.allPermissionsGranted) {
            permissions.launchMultiplePermissionRequest()
         } else if (!mainState.notificationListenerEnabled) {
            viewModel.requestNotificationPermissions()
         }
      }
   }

   private inner class ViewModelFactory : ViewModelProvider.Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
         @Suppress("UNCHECKED_CAST")
         return mainViewModelFactory.create() as T
      }
   }
}
