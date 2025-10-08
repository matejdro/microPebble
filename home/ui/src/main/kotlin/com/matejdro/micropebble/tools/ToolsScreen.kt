package com.matejdro.micropebble.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.micropebble.home.ui.R
import com.matejdro.micropebble.navigation.keys.DeveloperConnectionScreenKey
import com.matejdro.micropebble.navigation.keys.OnboardingKey
import com.matejdro.micropebble.ui.debugging.FullScreenPreviews
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import si.inova.kotlinova.navigation.di.ContributesScreenBinding
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
@ContributesScreenBinding
class ToolsScreen(
   private val navigator: Navigator,
) : Screen<ToolsScreenKey>() {
   @Composable
   override fun Content(key: ToolsScreenKey) {
      Surface {
         ToolsScreenContent(
            { navigator.navigateTo(OnboardingKey) },
            { navigator.navigateTo(DeveloperConnectionScreenKey) },
         )
      }
   }
}

@Composable
private fun ToolsScreenContent(
   openPermissions: () -> Unit,
   openDevConnection: () -> Unit,
) {
   LazyVerticalGrid(
      GridCells.Adaptive(175.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
      modifier = Modifier
         .padding(horizontal = 16.dp)
         .fillMaxSize()
   ) {
      item {
         ToolButton(openDevConnection, R.drawable.developer_connection, R.string.developer_connection)
      }

      item {
         ToolButton(openPermissions, R.drawable.permissions, R.string.permissions)
      }

      item {
         Box(
            Modifier
               .fillMaxSize()
               .background(Color.Red)
         )
      }
   }
}

@Composable
private fun ToolButton(onClick: () -> Unit, icon: Int, text: Int) {
   Button(onClick = onClick, Modifier.sizeIn(minHeight = 60.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
         Icon(
            painterResource(icon),
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
         )

         Text(stringResource(text))
      }
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "test")
internal fun ToolsScreenPreview() {
   PreviewTheme {
      ToolsScreenContent({}, {})
   }
}
