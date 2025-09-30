package com.matejdro.micropebble.apps.ui.list

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.Stable
import com.matejdro.micropebble.apps.ui.errors.InvalidPbwFileException
import com.matejdro.micropebble.apps.ui.errors.LibPebbleError
import com.matejdro.micropebble.common.logging.ActionLogger
import com.matejdro.micropebble.navigation.keys.WatchappListKey
import dev.zacsweers.metro.Inject
import dispatch.core.withDefault
import io.rebble.libpebblecommon.connection.Errors
import io.rebble.libpebblecommon.connection.LockerApi
import io.rebble.libpebblecommon.connection.UserFacingError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.files.Path
import okio.buffer
import okio.sink
import okio.source
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import java.io.File
import kotlin.time.Duration.Companion.seconds

@Stable
@Inject
@ContributesScopedService
class WatchappListViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val lockerApi: LockerApi,
   private val context: Context,
   private val errorHandler: Errors,
) : SingleScreenViewModel<WatchappListKey>(resources.scope) {
   private val _uiState = MutableStateFlow<Outcome<WatchappListState>>(Outcome.Progress())
   val uiState: StateFlow<Outcome<WatchappListState>> = _uiState
   private val _installingStatus = MutableStateFlow<Outcome<Unit>>(Outcome.Success(Unit))
   val installingStatus: StateFlow<Outcome<Unit>> = _installingStatus

   override fun onServiceRegistered() {
      actionLogger.logAction { "WatchappListViewModel.onServiceRegistered()" }

      resources.launchResourceControlTask(_uiState) {
         emitAll(
            lockerApi.getAllLockerBasicInfo().map { Outcome.Success(WatchappListState(it)) }
         )
      }
   }

   fun startInstall(contentUri: Uri) = resources.launchResourceControlTask(_installingStatus) {
      actionLogger.logAction { "WatchappListViewModel.startInstall($contentUri)" }

      withDefault {
         if (getFileName(contentUri)?.endsWith(".pbw") != true) {
            throw InvalidPbwFileException()
         }

         val tmpFile = File.createTempFile("pbw", null)
         val stream =
            requireNotNull(context.contentResolver.openInputStream(contentUri)) {
               "Files provider should not return null streams"
            }

         stream.source().buffer().use { it.readAll(tmpFile.sink()) }

         val errorCollection = errorHandler.userFacingErrors.filterIsInstance<UserFacingError.FailedToSideloadApp>()
            .buffer(Channel.BUFFERED)
            .produceIn(this)

         val success = try {
            lockerApi.sideloadApp(Path(tmpFile.absolutePath))
         } finally {
            tmpFile.delete()
            Unit // Extra Unit to not trip up detekt
         }

         val result = if (!success) {
            val error = withTimeoutOrNull(1.seconds) {
               errorCollection.consume { receive() }
            }
               ?.let { LibPebbleError(it.message) }
               ?: UnknownCauseException()

            Outcome.Error(error)
         } else {
            errorCollection.cancel()
            Outcome.Success(Unit)
         }

         emit(result)
      }
   }

   private suspend fun getFileName(uri: Uri): String? = withDefault {
      val projection = arrayOf<String?>(MediaStore.MediaColumns.DISPLAY_NAME)
      context.contentResolver.query(uri, projection, null, null, null).use {
         if (it?.moveToFirst() != true) return@use null
         it.getString(0)
      }
   }
}
