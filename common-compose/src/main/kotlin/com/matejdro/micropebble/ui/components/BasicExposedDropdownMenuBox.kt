package com.matejdro.micropebble.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Suppress("ModifierNotUsedAtRoot") // ExposedDropdownMenuBox is weird
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicExposedDropdownMenuBox(
   textFieldValue: String,
   modifier: Modifier = Modifier,
   textFieldLeadingIcon: @Composable (() -> Unit)? = null,
   textFieldLabel: @Composable (() -> Unit)? = null,
   dropdownMenuContent: @Composable (ColumnScope.() -> Unit),
) {
   var expanded by remember { mutableStateOf(false) }
   ExposedDropdownMenuBox(expanded, onExpandedChange = { expanded = it }) {
      TextField(
         value = textFieldValue,
         onValueChange = {},
         modifier = modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
         readOnly = true,
         singleLine = true,
         leadingIcon = textFieldLeadingIcon,
         label = textFieldLabel,
      )
      ExposedDropdownMenu(
         expanded = expanded,
         onDismissRequest = { expanded = false }
      ) {
         dropdownMenuContent()
      }
   }
}
