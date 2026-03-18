package com.matejdro.micropebble.webservices.api

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.Outcome
import kotlin.uuid.Uuid

@Stable
@Serializable
data class ParsedWebservicesToken(
   val sourceId: Uuid? = null,
   val bootUrl: String? = null,
   val token: String? = null,
)

class NotAuthenticated : CauseException("User is not authenticated", isProgrammersFault = false)

interface WebservicesClient {
   val tokens: Flow<Map<Uuid, WebservicesToken>>
   suspend fun checkToken(token: WebservicesToken): Boolean
   suspend fun authenticate(token: WebservicesToken): Boolean
   suspend fun deauthenticate(token: WebservicesToken)
   suspend fun parseTokenUri(uri: String): ParsedWebservicesToken
   suspend fun fetchLocker(sourceId: Uuid): Outcome<WebserviceLocker>
}
