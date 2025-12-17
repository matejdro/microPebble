package com.matejdro.micropebble.apps.ui.webviewconfig

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.kevinnzou.web.AccompanistWebViewClient
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberWebViewState
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.navigation.instructions.goBack
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class AppConfigScreen(
   private val viewModel: AppConfigScreenViewModel,
   private val navigator: Navigator,
) : Screen<AppConfigScreenKey>() {
   @Composable
   override fun Content(key: AppConfigScreenKey) {
      val url = viewModel.configUrl.collectAsStateWithLifecycleAndBlinkingPrevention().value

      ProgressErrorSuccessScaffold(
         url,
         Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
      ) { state ->
         when (state) {
            is AppConfigScreenState.WebView -> {
               WebView(
                  state = rememberWebViewState(state.url),
                  client = remember { CustomWebViewClient(viewModel::save) },
                  modifier = Modifier
                     .fillMaxSize()
                     .windowInsetsPadding(WindowInsets.safeDrawing),
                  onCreated = {
                     @SuppressLint("SetJavaScriptEnabled") // Needed for some apps
                     it.settings.javaScriptEnabled = true
                     it.settings.domStorageEnabled = true
                  }
               )
            }

            AppConfigScreenState.Close -> {
               SideEffect {
                  navigator.goBack()
               }
            }
         }
      }
   }
}

private class CustomWebViewClient(
   private val submit: (String) -> Unit,
) : AccompanistWebViewClient() {

   override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
      val url = request.url
      return if (url.toString().startsWith("pebblejs://close")) {
         submit(url.fragment.orEmpty())
         false
      } else {
         false
      }
   }
}
