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
import io.rebble.libpebblecommon.locker.AppType
import io.rebble.libpebblecommon.locker.LockerWrapper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.files.Path
import okio.buffer
import okio.sink
import okio.source
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import java.io.File
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

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
   private val _actionStatus = MutableStateFlow<Outcome<Unit>>(Outcome.Success(Unit))
   val actionStatus: StateFlow<Outcome<Unit>> = _actionStatus

   override fun onServiceRegistered() {
      actionLogger.logAction { "WatchappListViewModel.onServiceRegistered()" }

      resources.launchResourceControlTask(_uiState) {
         emitAll(
            combine(
               lockerApi.getLocker(AppType.Watchface, null, Int.MAX_VALUE),
               lockerApi.getLocker(AppType.Watchapp, null, Int.MAX_VALUE),
            ) { watchfaces, watchapps ->
               Outcome.Success(
                  WatchappListState(
                     watchfaces.filterIsInstance<LockerWrapper.NormalApp>(),
                     watchapps.filterIsInstance<LockerWrapper.NormalApp>()
                  )
               )
            }
         )
      }
   }

   fun startInstall(contentUri: Uri) = resources.launchResourceControlTask(_actionStatus) {
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

         tmpFile.sink().use { fileSink ->
            stream.source().buffer().use { it.readAll(fileSink) }
         }

         try {
            runLibPebbleActionWithErrorConversion<UserFacingError.FailedToSideloadApp> {
               lockerApi.sideloadApp(Path(tmpFile.absolutePath))
            }
         } finally {
            tmpFile.delete()
            Unit // Extra Unit to not trip up detekt
         }

         emit(Outcome.Success(Unit))
      }
   }

   fun deleteApp(uuid: Uuid) = resources.launchResourceControlTask(_actionStatus) {
      actionLogger.logAction { "WatchappListViewModel.deleteApp(uuid = $uuid)" }

      withDefault {
         runLibPebbleActionWithErrorConversion<UserFacingError.FailedToRemovePbwFromLocker> {
            lockerApi.removeApp(uuid)
         }

         emit(Outcome.Success(Unit))
      }
   }

   private suspend inline fun <reified E : UserFacingError> runLibPebbleActionWithErrorConversion(
      crossinline action: suspend () -> Boolean,
   ) {
      coroutineScope {
         val errorCollection = errorHandler.userFacingErrors.filterIsInstance<E>()
            .buffer(Channel.BUFFERED)
            .produceIn(this)

         val success = action()

         if (!success) {
            val error = withTimeoutOrNull(1.seconds) {
               errorCollection.consume { receive() }
            }
               ?.let { LibPebbleError(it.message) }

            if (error != null) {
               throw error
            }
         }

         errorCollection.cancel()
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
