package com.matejdro.micropebble.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
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
fun <T> ProgressErrorSuccessScaffold(
   outcome: Outcome<T>?,
   modifier: Modifier = Modifier,
   errorText: @Composable (CauseException) -> String = { it.commonUserFriendlyMessage() },
   data: @Composable (T) -> Unit,
) {
   when (outcome) {
      is Outcome.Error -> {
         Text(
            text = errorText(outcome.exception),
            modifier
               .fillMaxWidth()
               .wrapContentWidth(),
            color = MaterialTheme.colorScheme.error
         )
      }

      is Outcome.Progress -> {
         CircularProgressIndicator(
            modifier
               .fillMaxWidth()
               .wrapContentWidth()
         )
      }

      is Outcome.Success -> {
         data(outcome.data)
      }

      null -> {}
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Components", name = "ProgressErrorSuccessScaffold", styleName = "Error")
internal fun ProgressErrorSuccessScaffoldErrorPreview() {
   PreviewTheme(fill = false) {
      ProgressErrorSuccessScaffold(Outcome.Error<Unit>(UnknownCauseException())) {
      }
   }
}

@Preview
@Composable
@ShowkaseComposable(group = "Components", name = "ProgressErrorSuccessScaffold", styleName = "Progress")
internal fun ProgressErrorSuccessScaffoldProgressPreview() {
   PreviewTheme(fill = false) {
      ProgressErrorSuccessScaffold(Outcome.Progress<Unit>()) {
      }
   }
}
