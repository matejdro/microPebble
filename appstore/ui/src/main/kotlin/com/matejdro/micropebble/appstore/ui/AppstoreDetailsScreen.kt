package com.matejdro.micropebble.appstore.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.matejdro.micropebble.appstore.api.store.application.Application
import com.matejdro.micropebble.navigation.keys.AppstoreDetailsScreenKey
import dev.zacsweers.metro.Inject
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@Inject
@InjectNavigationScreen
class AppstoreDetailsScreen : Screen<AppstoreDetailsScreenKey>() {
   val bannerContentType = "bannerContentType"
   val titleContentType = "titleContentType"
   val imageCarouselContentType = "imageCarouselContentType"
   val descriptionContentType = "descriptionContentType"
   val actionsContentType = "actionsContentType"

   @OptIn(ExperimentalMaterial3Api::class)
   @Composable
   override fun Content(key: AppstoreDetailsScreenKey) {
      val app = key.app
      val childModifier = Modifier.padding(horizontal = 8.dp)
      val actions = remember { getActionsFor(app) }
      LazyColumn(
         verticalArrangement = Arrangement.spacedBy(8.dp),
         modifier = Modifier.safeDrawingPadding(),
         contentPadding = PaddingValues(bottom = WindowInsets.safeContent.asPaddingValues().calculateBottomPadding())
      ) {
         item(contentType = bannerContentType) {
            app.headerImages.firstOrNull()?.let {
               AsyncImage(
                  model = it.medium,
                  contentDescription = "Banner for ${app.title}", // No horizontal padding because the banner extends to the edges
                  modifier = Modifier.fillMaxWidth(),
                  contentScale = ContentScale.FillWidth,
               )
            }
         }
         stickyHeader(contentType = titleContentType) {
            Column(
               childModifier
                  .fillMaxWidth()
                  .background(MaterialTheme.colorScheme.background)
            ) {
               Text(app.title, childModifier.paddingFromBaseline(bottom = 16.dp), style = MaterialTheme.typography.headlineLarge)
               Text(app.author, childModifier.paddingFromBaseline(bottom = 16.dp), style = MaterialTheme.typography.titleLarge)
            }
         }
         item(contentType = imageCarouselContentType) {
            HorizontalMultiBrowseCarousel(
               rememberCarouselState(0) { app.screenshotImages.size },
               preferredItemWidth = 144.dp,
               modifier = childModifier,
               itemSpacing = 8.dp,
            ) {
               AsyncImage(
                  model = app.screenshotImages[it].medium,
                  contentDescription = "Screenshot image $it for ${app.title}",
                  modifier = Modifier
                     .fillMaxWidth(),
                  contentScale = ContentScale.FillWidth,
               )
            }
         }
         item(contentType = descriptionContentType) {
            Card(modifier = childModifier) {
               Text(app.description, Modifier.padding(8.dp))
            }
         }

         items(actions, contentType = { actionsContentType }) {
            val uriHandler = LocalUriHandler.current
            Card(modifier = childModifier, onClick = {
               when (it) {
                  is AppLink -> uriHandler.openUri(it.linkTarget)
                  is AppButton -> it.onClick()
               }
            }) {
               Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
               ) {
                  Text(stringResource(it.label), Modifier.padding(8.dp), style = MaterialTheme.typography.titleLarge)
                  if (it is AppLink) {
                     Icon(
                        painter = painterResource(R.drawable.baseline_open_in_new_24),
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp)
                     )
                  }
               }
            }
         }
      }
   }

   private fun getActionsFor(app: Application): List<AppAction> = buildList {
      add(AppButton(R.string.appVersionInfo) {})
      app.website?.let { add(AppLink(R.string.appWebsiteLink, it)) }
      app.source?.let { add(AppLink(R.string.appSourceCode, it)) }
      add(AppButton(R.string.appFromDeveloper) {})
   }

   private sealed class AppAction(val label: Int)
   private class AppButton(label: Int, val onClick: () -> Unit) : AppAction(label)
   private class AppLink(label: Int, val linkTarget: String) : AppAction(label)
}
