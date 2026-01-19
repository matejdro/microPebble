package com.matejdro.micropebble.apps.ui.list

import androidx.compose.runtime.Stable

@Stable
enum class AppStatus {
   /**
    * An error occurred when attempting to fetch updates for the app.
    */
   Error,

   /**
    * The app is already up to date.
    */
   UpToDate,

   /**
    * The app is updatable.
    */
   Updatable,

   /**
    * The app wasn't found on the remote server.
    */
   AppNotFound,

   /**
    * No source is listed for the app, thus, it cannot be installed.
    */
   MissingSource,

   /**
    * The app isn't updatable, because it's a system app or similar.
    */
   NotUpdatable,
}
