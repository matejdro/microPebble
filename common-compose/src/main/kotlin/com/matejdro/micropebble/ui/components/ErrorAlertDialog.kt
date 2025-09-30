package com.matejdro.micropebble.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.matejdro.micropebble.ui.R
import com.matejdro.micropebble.ui.errors.commonUserFriendlyMessage
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.Outcome

/**
 * Composable that will display an alert dialog whenever passed Outcome is an error. It will not display a dialog again until
 * error changes.
 */
@Composable
fun ErrorAlertDialog(
   outcome: Outcome<*>?,
   modifier: Modifier = Modifier,
   errorText: @Composable (CauseException) -> String = { it.commonUserFriendlyMessage() },
) {
   var displayedDialogException by remember { mutableStateOf<CauseException?>(null) }
   var lastExceptionHash by rememberSaveable { mutableStateOf<Int?>(null) }

   LaunchedEffect(outcome) {
      if (outcome is Outcome.Error) {
         val exceptionHash = outcome.exception.hashCode()
         if (lastExceptionHash != exceptionHash) {
            displayedDialogException = outcome.exception
            lastExceptionHash = exceptionHash
         }
      } else {
         displayedDialogException = null
      }
   }

   val displayedDialogExceptionLocal = displayedDialogException
   if (displayedDialogExceptionLocal != null) {
      AlertDialog(
         modifier = modifier,
         onDismissRequest = { displayedDialogException = null },
         confirmButton = { TextButton(onClick = { displayedDialogException = null }) { Text(stringResource(R.string.ok)) } },
         text = { Text(errorText(displayedDialogExceptionLocal)) },
         title = { Text(stringResource(R.string.error)) }
      )
   }
}
