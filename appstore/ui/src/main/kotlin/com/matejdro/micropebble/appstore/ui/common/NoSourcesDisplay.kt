package com.matejdro.micropebble.appstore.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.matejdro.micropebble.ui.R

@Composable
fun NoSourcesDisplay(
   modifier: Modifier = Modifier.padding(horizontal = 16.dp),
   content: @Composable ColumnScope.() -> Unit = {},
) {
   Column(
      modifier = modifier,
      verticalArrangement = Arrangement.spacedBy(8.dp)
   ) {
      Icon(
         painterResource(R.drawable.ic_no_sources),
         contentDescription = null,
         tint = MaterialTheme.colorScheme.error,
         modifier = Modifier.align(Alignment.CenterHorizontally)
      )
      Text(
         stringResource(R.string.no_sources_enabled_title),
         style = MaterialTheme.typography.titleLarge,
         textAlign = TextAlign.Center,
         modifier = Modifier.fillMaxWidth(),
      )
      Text(
         stringResource(R.string.no_sources_enabled_body),
         style = MaterialTheme.typography.bodyMedium,
         textAlign = TextAlign.Center,
         modifier = Modifier.fillMaxWidth(),
      )
      content()
   }
}
