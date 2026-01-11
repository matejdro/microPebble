package com.matejdro.micropebble.appstore.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.micropebble.appstore.api.store.application.Application
import com.matejdro.micropebble.navigation.keys.AppstoreDetailsScreenKey
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import dev.zacsweers.metro.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import java.net.URI
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Instant
import kotlin.time.toJavaInstant

@Inject
@InjectNavigationScreen
class AppstoreDetailsScreen : Screen<AppstoreDetailsScreenKey>() {
   @OptIn(ExperimentalMaterial3Api::class)
   @Composable
   override fun Content(key: AppstoreDetailsScreenKey) {
      AppstoreDetailsContent(key.app)
   }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AppstoreDetailsContent(app: Application) {
   Scaffold(floatingActionButton = {
      ExtendedFloatingActionButton(
         onClick = {},
         icon = { Icon(painterResource(R.drawable.outline_download_24), contentDescription = "Install") },
         text = { Text(stringResource(R.string.install)) }
      )
   }) { paddingValues ->
      val childModifier = Modifier.padding(horizontal = 8.dp)
      val actions = remember { getActionsFor(app) }
      Box(
         Modifier
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState()),
      ) {
         Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
               .fillMaxSize()
               .padding(bottom = 88.dp)
         ) {
            Banner(app)

            TitleCard(app, childModifier)

            AppScreenshotCarousel(app, childModifier)

            Card(modifier = childModifier.fillMaxWidth()) {
               Text(app.description, Modifier.padding(8.dp))
            }

            InfoCard(app, childModifier)

            LinksCard(actions, childModifier)
         }
      }
   }
}

@Composable
private fun Banner(app: Application, content: @Composable () -> Unit = {}) {
   app.headerImages.firstOrNull()?.let {
      AsyncImage(
         model = it.medium,
         contentDescription = "Banner for ${app.title}",
         modifier = Modifier.fillMaxWidth(),
         contentScale = ContentScale.FillWidth,
      )
   }
   content()
}

@Composable
private fun TitleCard(app: Application, modifier: Modifier = Modifier) {
   Card(modifier = modifier.fillMaxWidth()) {
      Column(
         modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
      ) {
         Text(app.title, style = MaterialTheme.typography.headlineLarge)
         Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
         ) {
            Text(app.author, style = MaterialTheme.typography.titleLarge)
            Row(
               horizontalArrangement = Arrangement.spacedBy(8.dp),
               modifier = Modifier.fillMaxHeight(),
            ) {
               Icon(painterResource(R.drawable.outline_favorite_24), contentDescription = null)
               Text(app.hearts.toString())
            }
         }
      }
   }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AppScreenshotCarousel(app: Application, modifier: Modifier = Modifier) {
   HorizontalMultiBrowseCarousel(
      rememberCarouselState(0) { app.screenshotImages.size },
      preferredItemWidth = 144.dp,
      modifier = modifier,
      itemSpacing = 8.dp,
   ) {
      AsyncImage(
         model = app.screenshotImages[it].medium,
         contentDescription = "Screenshot image $it for ${app.title}",
         modifier = Modifier.fillMaxWidth(),
         contentScale = ContentScale.FillWidth,
      )
   }
}

@Composable
private fun InfoCard(app: Application, modifier: Modifier = Modifier) {
   Card(modifier = modifier.fillMaxWidth()) {
      Column(
         modifier = Modifier.fillMaxWidth()
      ) {
         Text("Version ${app.latestRelease.version}", Modifier.padding(8.dp))
         HorizontalDivider()
         Text(
            "Published ${app.latestRelease.publishedDate.formatDate()}", Modifier.padding(8.dp)
         )
         if (app.changelog.size > 1) {
            HorizontalDivider()
            Text("Created ${app.createdAt.formatDate()}", Modifier.padding(8.dp))
         }
      }
   }
}

@Composable
private fun LinksCard(actions: List<AppAction>, modifier: Modifier = Modifier) {
   Card(modifier = modifier.fillMaxWidth()) {
      for ((index, action) in actions.withIndex()) {
         val uriHandler = LocalUriHandler.current
         if (index != 0) {
            HorizontalDivider()
         }
         TextButton(shape = CardDefaults.shape, modifier = Modifier.fillMaxWidth(), onClick = {
            when (action) {
               is AppLink -> uriHandler.openUri(action.linkTarget)
               is AppButton -> action.onClick()
            }
         }) {
            Row(
               modifier = Modifier.fillMaxWidth(),
               horizontalArrangement = Arrangement.SpaceBetween,
               verticalAlignment = Alignment.CenterVertically
            ) {
               Text(stringResource(action.label), Modifier.padding(8.dp))
               if (action is AppLink) {
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

private fun Instant.formatDate(): String =
   this.toJavaInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun AppstoreDetailsContentPreview() {
   PreviewTheme {
      val rawString = URI("https://appstore-api.rebble.io/api/v1/apps/id/67c751c6d2acb30009a3c812").toURL().readText()
      val string = Json.encodeToString(Json.parseToJsonElement(rawString).jsonObject["data"]?.jsonArray[0])
      AppstoreDetailsContent(Json.decodeFromString(string))
   }
}
