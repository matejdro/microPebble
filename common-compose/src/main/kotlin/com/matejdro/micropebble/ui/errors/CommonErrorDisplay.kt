package com.matejdro.micropebble.ui.errors

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.matejdro.micropebble.common.exceptions.LibPebbleError
import com.matejdro.micropebble.common.exceptions.WatchDisconnectedException
import com.matejdro.micropebble.ui.R
import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.outcome.CauseException

@Composable
fun CauseException.commonUserFriendlyMessage(
   hasExistingData: Boolean = false,
): String {
   return when (this) {
      is NoNetworkException -> {
         stringResource(
            if (hasExistingData) {
               R.string.error_no_network_with_existing_data
            } else {
               R.string.error_no_network
            }
         )
      }

      is WatchDisconnectedException -> {
         stringResource(R.string.error_watch_not_connected)
      }

      is LibPebbleError -> message ?: stringResource(R.string.error_unknown)

      else -> {
         stringResource(R.string.error_unknown)
      }
   }
}
