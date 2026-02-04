package com.matejdro.micropebble.appstore.api

import androidx.compose.runtime.Immutable
import com.matejdro.micropebble.common.util.VersionInfo
import si.inova.kotlinova.core.outcome.CauseException

@Immutable
sealed class AppStatus {

   /**
    * An error occurred when attempting to fetch updates for the app.
    */
   data object Error : AppStatus()

   /**
    * The app is already up to date.
    */
   data object UpToDate : AppStatus()

   /**
    * The app is updatable.
    */
   data class Updatable(val fromVersion: VersionInfo, val toVersion: VersionInfo) : AppStatus()

   /**
    * The app wasn't found on the remote server.
    */
   data object AppNotFound : AppStatus()

   /**
    * No source is listed for the app, thus, it cannot be installed.
    */
   data object MissingSource : AppStatus()

   /**
    * The app isn't updatable, because it's a system app or similar.
    */
   data object NotUpdatable : AppStatus()

   /**
    * The update failed to complete.
    */
   data class UpdateFailed(val error: CauseException) : AppStatus()

   /**
    * The app was just updated.
    */
   data object JustUpdated : AppStatus()

   /**
    * The update is happening as we speak.
    */
   data object Updating : AppStatus()

   data object CheckingForUpdates : AppStatus()
}
