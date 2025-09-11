package com.matejdro.micropebble.ui.errors

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.matejdro.micropebble.ui.R
import si.inova.kotlinova.core.exceptions.NoNetworkException
import si.inova.kotlinova.core.outcome.CauseException

@Composable
fun CauseException.commonUserFriendlyMessage(
   hasExistingData: Boolean = false,
): String {
   return if (this is NoNetworkException) {
      stringResource(
         if (hasExistingData) {
            R.string.error_no_network_with_existing_data
         } else {
            R.string.error_no_network
         }
      )
   } else {
      stringResource(R.string.error_unknown)
   }
}
