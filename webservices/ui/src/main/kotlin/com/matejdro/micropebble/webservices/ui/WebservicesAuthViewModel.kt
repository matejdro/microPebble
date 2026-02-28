package com.matejdro.micropebble.webservices.ui

import androidx.compose.runtime.Stable
import com.matejdro.micropebble.appstore.api.AppstoreSourceService
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.WebservicesAuthScreenKey
import com.matejdro.micropebble.webservices.api.ParsedWebservicesToken
import com.matejdro.micropebble.webservices.api.WebservicesClient
import com.matejdro.micropebble.webservices.api.WebservicesToken
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

class InvalidTokenException : CauseException(message = "Token is invalid", isProgrammersFault = false)

@Stable
@Inject
@ContributesScopedService
class WebservicesAuthViewModel(
   private val client: WebservicesClient,
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   sourceService: AppstoreSourceService,
) : SingleScreenViewModel<WebservicesAuthScreenKey>(resources.scope) {
   val authToken = client.tokens
   val sources = sourceService.sources

   private val _token = MutableStateFlow<Outcome<WebservicesToken>>(Outcome.Error(InvalidTokenException()))
   val token: StateFlow<Outcome<WebservicesToken>> = _token
   private val _startAuthToken = MutableStateFlow<Outcome<ParsedWebservicesToken?>>(Outcome.Progress())
   val startAuthToken: StateFlow<Outcome<ParsedWebservicesToken?>> = _startAuthToken

   fun authenticate(token: WebservicesToken) {
      actionLogger.logAction { "WebservicesAuthViewModel.authenticate($token)" }
      resources.scope.launch {
         client.authenticate(token)
      }
   }

   fun deauthenticate(token: WebservicesToken) {
      actionLogger.logAction { "WebservicesAuthViewModel.deauthenticate($token)" }
      resources.scope.launch {
         client.deauthenticate(token)
      }
   }

   fun makeToken(token: WebservicesToken) = resources.launchResourceControlTask(_token) {
      actionLogger.logAction { "WebservicesAuthViewModel.canAuthenticate($token)" }
      if (client.checkToken(token)) {
         emit(Outcome.Success(token))
      } else {
         emit(Outcome.Error(InvalidTokenException()))
      }
   }

   fun loadFromBootUrl() {
      actionLogger.logAction { "WebservicesAuthViewModel.loadFromBootUrl()" }
      val intentBootUri = key.bootUrl
      if (intentBootUri == null) {
         _startAuthToken.value = Outcome.Success(null)
         return
      }
      resources.launchResourceControlTask(_startAuthToken) {
         emit(Outcome.Success(client.parseTokenUri(intentBootUri)))
      }
   }
}
