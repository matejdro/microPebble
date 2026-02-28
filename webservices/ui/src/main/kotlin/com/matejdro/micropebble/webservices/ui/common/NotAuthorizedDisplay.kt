package com.matejdro.micropebble.webservices.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.matejdro.micropebble.webservices.ui.R

@Composable
fun NotAuthorizedDisplay(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit = {}) {
   Column(
      modifier = modifier,
      verticalArrangement = Arrangement.spacedBy(8.dp)
   ) {
      Icon(
         painterResource(R.drawable.ic_auth_off),
         contentDescription = null,
         tint = MaterialTheme.colorScheme.error,
         modifier = Modifier.align(Alignment.CenterHorizontally)
      )
      Text(
         stringResource(R.string.webservices_not_authorized),
         style = MaterialTheme.typography.titleLarge,
         textAlign = TextAlign.Center,
         modifier = Modifier.fillMaxWidth(),
      )
      content()
   }
}
