package com.matejdro.micropebble.bluetooth.errors

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.matejdro.micropebble.bluetooth.ui.R
import com.matejdro.micropebble.ui.errors.commonUserFriendlyMessage
import si.inova.kotlinova.core.outcome.CauseException

@Composable
fun CauseException.bluetoothUserFriendlyErrorMessage(
   hasExistingData: Boolean = false,
): String {
   return if (this is InvalidPbzFileException) {
      stringResource(R.string.this_is_not_a_valid_pbz_file)
   } else {
      commonUserFriendlyMessage(hasExistingData)
   }
}
