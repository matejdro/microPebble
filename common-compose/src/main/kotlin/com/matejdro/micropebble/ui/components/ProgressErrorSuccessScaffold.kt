package com.matejdro.micropebble.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import com.matejdro.micropebble.ui.errors.commonUserFriendlyMessage
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.Outcome

/**
 * A scaffold that will display a basic progress bar, an error message or passed composable as success.
 *
 * This can be used to add error and loading handling to simple screens.
 */
@Composable
@Suppress("ModifierNaming") // It's intentionally called a different way
@SuppressLint("ModifierParameter")
fun <T> ProgressErrorSuccessScaffold(
   outcomeProvider: () -> Outcome<T>?,
   /**
    * Modifier that is applied to the error and progress composables (but not to the called [content]).
    */
   errorProgressModifier: Modifier = Modifier,
   errorText: @Composable (CauseException) -> String = { it.commonUserFriendlyMessage() },
   content: @Composable (T) -> Unit,
) {
   when (val outcome = outcomeProvider()) {
      is Outcome.Error -> {
         val data = outcome.data
         if (data != null) {
            ErrorAlertDialog(outcome)
            content(data)
         } else {
            Text(
               text = errorText(outcome.exception),
               errorProgressModifier
                  .fillMaxSize()
                  .wrapContentSize(),
               color = MaterialTheme.colorScheme.error
            )
         }
      }

      is Outcome.Progress -> {
         CircularProgressIndicator(
            errorProgressModifier
               .fillMaxSize()
               .wrapContentSize()
         )
      }

      is Outcome.Success -> {
         content(outcome.data)
      }

      null -> {}
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Components", name = "ProgressErrorSuccessScaffold", styleName = "Error")
internal fun ProgressErrorSuccessScaffoldErrorPreview() {
   PreviewTheme(fill = false) {
      ProgressErrorSuccessScaffold({ Outcome.Error<Unit>(UnknownCauseException()) }) {
      }
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Components", name = "ProgressErrorSuccessScaffold", styleName = "DialogError")
internal fun ProgressErrorSuccessScaffoldDialogErrorPreview() {
   PreviewTheme(fill = false) {
      ProgressErrorSuccessScaffold({ Outcome.Error<Unit>(UnknownCauseException(), Unit) }) {
         Text("Content!", Modifier.fillMaxSize())
      }
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Components", name = "ProgressErrorSuccessScaffold", styleName = "Progress", tags = ["animated"])
internal fun ProgressErrorSuccessScaffoldProgressPreview() {
   PreviewTheme(fill = false) {
      ProgressErrorSuccessScaffold({ Outcome.Progress<Unit>() }) {
      }
   }
}
