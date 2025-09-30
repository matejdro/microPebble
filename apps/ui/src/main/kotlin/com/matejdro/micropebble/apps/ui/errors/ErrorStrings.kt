package com.matejdro.micropebble.apps.ui.errors

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.matejdro.micropebble.apps.ui.R
import com.matejdro.micropebble.ui.errors.commonUserFriendlyMessage
import si.inova.kotlinova.core.outcome.CauseException

@Composable
fun CauseException.installUserFriendlyErrorMessage(
   hasExistingData: Boolean = false,
): String {
   return when (this) {
      is InvalidPbwFileException -> stringResource(R.string.this_is_not_a_valid_pbw_file)
      is LibPebbleError -> message
      else -> commonUserFriendlyMessage(hasExistingData)
   }
}
