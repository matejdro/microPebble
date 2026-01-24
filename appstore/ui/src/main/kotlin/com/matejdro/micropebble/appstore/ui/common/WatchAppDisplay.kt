package com.matejdro.micropebble.appstore.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.store.application.Application
import com.matejdro.micropebble.appstore.api.store.application.ApplicationScreenshot
import com.matejdro.micropebble.appstore.ui.R
import com.matejdro.micropebble.navigation.keys.AppstoreDetailsScreenKey
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator

@Composable
fun WatchAppDisplay(
   app: Application,
   navigator: Navigator?,
   modifier: Modifier = Modifier,
   appstoreSource: AppstoreSource? = null,
   onlyPartialData: Boolean = false,
) {
   Card(
      onClick = { navigator?.navigateTo(AppstoreDetailsScreenKey(app, onlyPartialData, appstoreSource)) },
      modifier = modifier.fillMaxSize(),
   ) {
      Column(
         Modifier
            .fillMaxSize()
            .padding(8.dp),
         horizontalAlignment = Alignment.CenterHorizontally,
      ) {
         val cardShape = CardDefaults.shape as RoundedCornerShape

         // This should not be so hard
         operator fun CornerSize.minus(b: CornerSize) = let { baseSize ->
            object : CornerSize {
               override fun toPx(shapeSize: Size, density: Density) =
                  (baseSize.toPx(shapeSize, density) - b.toPx(shapeSize, density)).coerceAtLeast(0f)
            }
         }

         val padCornerSize = CornerSize(8.dp)
         app.screenshotImages.firstOrNull()?.let {
            val ratio = ApplicationScreenshot.Hardware.fromHardwarePlatform(app.screenshotHardware)?.aspectRatio
               ?: it.imageHardware.aspectRatio
            AsyncImage(
               model = it.image,
               contentDescription = "App image for ${app.title}",
               modifier = Modifier
                  .fillMaxWidth()
                  .padding(bottom = 8.dp)
                  .aspectRatio(ratio)
                  .clip(
                     RoundedCornerShape(
                        cardShape.topStart - padCornerSize,
                        cardShape.topEnd - padCornerSize,
                        cardShape.bottomEnd - padCornerSize,
                        cardShape.bottomStart - padCornerSize
                     )
                  ),
               contentScale = ContentScale.FillBounds,
            )
         }
         Text(
            app.title,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium
         )
         Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
         ) {
            Icon(painterResource(R.drawable.ic_like), contentDescription = null)
            Text(app.hearts.toString())
            if (app.source != null) {
               VerticalDivider()
               Icon(painterResource(R.drawable.ic_source_code), contentDescription = null)
            }
         }
      }
   }
}
