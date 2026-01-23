package com.matejdro.micropebble.appstore.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.micropebble.appstore.api.AppInstallState
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.store.application.Application
import com.matejdro.micropebble.appstore.ui.common.APP_IMAGE_ASPECT_RATIO
import com.matejdro.micropebble.appstore.ui.common.BANNER_RATIO
import com.matejdro.micropebble.appstore.ui.common.formatDate
import com.matejdro.micropebble.common.util.joinUrls
import com.matejdro.micropebble.navigation.keys.AppstoreCollectionScreenKey
import com.matejdro.micropebble.navigation.keys.AppstoreDetailsScreenKey
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import si.inova.kotlinova.compose.components.itemsWithDivider
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import java.net.URI

@Inject
@InjectNavigationScreen
class AppstoreDetailsScreen(
   private val viewModel: AppstoreDetailsViewModel,
   private val navigator: Navigator,
) : Screen<AppstoreDetailsScreenKey>() {
   @OptIn(ExperimentalMaterial3Api::class)
   @Composable
   override fun Content(key: AppstoreDetailsScreenKey) {
      val installState = viewModel.appState.collectAsStateWithLifecycleAndBlinkingPrevention().value ?: Outcome.Progress()
      val dataState = viewModel.appDataState.collectAsStateWithLifecycleAndBlinkingPrevention().value
      val snackbarHostState = remember { SnackbarHostState() }
      LaunchedEffect(Unit) {
         viewModel.appState.filterIsInstance<Outcome.Error<*>>().collect {
            snackbarHostState.showSnackbar(it.exception.message ?: "Unknown error")
         }
      }
      ProgressErrorSuccessScaffold(dataState) {
         AppstoreDetailsContent(
            app = it,
            snackbarHostState,
            appInstallState = installState,
            key.appstoreSource,
            navigator
         ) {
            viewModel.install()
         }
      }
   }
}

@Composable
private fun floatingActionButtonElevation(): ButtonElevation {
   return ButtonDefaults.buttonElevation(
      defaultElevation = 6.0.dp,
      pressedElevation = 6.0.dp,
      focusedElevation = 6.0.dp,
      hoveredElevation = 8.0.dp,
      disabledElevation = 4.0.dp,
   )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AppstoreDetailsContent(
   app: Application,
   errorsSnackbarState: SnackbarHostState,
   appInstallState: Outcome<AppInstallState>,
   appstoreSource: AppstoreSource? = null,
   navigator: Navigator? = null,
   installApp: () -> Unit,
) {
   val sheetState = rememberModalBottomSheetState()
   var showVersionSheet by remember { mutableStateOf(false) }
   val colors = ButtonDefaults.buttonColors()
      .run { copy(disabledContainerColor = disabledContainerColor.compositeOver(MaterialTheme.colorScheme.background)) }

   Scaffold(floatingActionButton = {
      ElevatedButton(
         onClick = installApp,
         enabled = appInstallState.data == AppInstallState.CAN_INSTALL,
         elevation = floatingActionButtonElevation(),
         colors = colors,
         modifier = Modifier.defaultMinSize(minHeight = 56.dp).animateContentSize(),
         shape = FloatingActionButtonDefaults.extendedFabShape,
      ) {
         if (appInstallState is Outcome.Progress) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
         } else {
            Icon(painterResource(R.drawable.ic_download), contentDescription = "Install")
            val text = when (appInstallState.data) {
               AppInstallState.CAN_INSTALL -> stringResource(R.string.install)
               AppInstallState.INSTALLED -> stringResource(R.string.installed)
               null -> ""
            }
            Text(text, modifier = Modifier.padding(start = 8.dp))
         }
      }
   }, snackbarHost = {
      SnackbarHost(hostState = errorsSnackbarState)
   }) { _ ->
      val childModifier = Modifier.padding(horizontal = 0.dp)
      val actions = remember {
         listOf(
            AppButton(R.string.appVersionInfo) {
               showVersionSheet = true
            }
         ) + getActionsFor(app, navigator, appstoreSource)
      }

      LazyColumn(
         verticalArrangement = Arrangement.spacedBy(8.dp),
         modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .safeDrawingPadding()
            .clip(CardDefaults.shape),
         contentPadding = PaddingValues(bottom = 88.dp),
      ) {
         item(contentType = 0) {
            Banner(app)
         }

         item(contentType = 1) {
            TitleCard(app, childModifier)
         }

         item(contentType = 2) {
            AppScreenshotCarousel(app, childModifier)
         }

         item(contentType = 3) {
            Card(modifier = childModifier.fillMaxWidth()) {
               Text(app.description, Modifier.padding(8.dp))
            }
         }

         item(contentType = 4) {
            InfoCard(app, childModifier)
         }

         item(contentType = 5) {
            LinksCard(actions, childModifier)
         }
      }

      if (showVersionSheet) {
         ModalBottomSheet(onDismissRequest = { showVersionSheet = false }, sheetState = sheetState) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(8.dp)) {
               itemsWithDivider(app.changelog) {
                  Text(it.version, Modifier.padding(8.dp), style = MaterialTheme.typography.titleLarge)
                  Text(it.publishedDate.formatDate(), Modifier.padding(8.dp))
                  Text(it.releaseNotes, Modifier.padding(8.dp))
               }
            }
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
         modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .aspectRatio(BANNER_RATIO),
         contentScale = ContentScale.FillWidth,
      )
   }
   content()
}

@Composable
private fun TitleCard(app: Application, modifier: Modifier = Modifier) {
   ElevatedCard(modifier = modifier.fillMaxWidth()) {
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
               Icon(painterResource(R.drawable.ic_like), contentDescription = null)
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
      modifier = modifier.clip(CardDefaults.shape),
      itemSpacing = 8.dp,
   ) {
      AsyncImage(
         model = app.screenshotImages[it].medium,
         contentDescription = "Screenshot image $it for ${app.title}",
         modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(APP_IMAGE_ASPECT_RATIO),
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
            "Last updated ${app.latestRelease.publishedDate.formatDate()}", Modifier.padding(8.dp)
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
                     painter = painterResource(R.drawable.ic_open),
                     contentDescription = null,
                     modifier = Modifier.padding(8.dp)
                  )
               }
            }
         }
      }
   }
}

private fun getActionsFor(
   app: Application,
   navigator: Navigator? = null,
   appstoreSource: AppstoreSource? = null,
): List<AppAction> = buildList {
   app.website?.let { add(AppLink(R.string.appWebsiteLink, it)) }
   app.source?.let { add(AppLink(R.string.appSourceCode, it)) }
   if (navigator != null && appstoreSource != null) {
      add(
         AppButton(R.string.appFromDeveloper) {
            navigator.navigateTo(
               AppstoreCollectionScreenKey(
                  "Apps from ${app.author}", appstoreSource.url.joinUrls("v1/apps/dev/${app.developerId}"), appstoreSource
               )
            )
         }
      )
   }
}

private sealed class AppAction(val label: Int)
private class AppButton(label: Int, val onClick: () -> Unit) : AppAction(label)
private class AppLink(label: Int, val linkTarget: String) : AppAction(label)

@Preview
@Composable
@ShowkaseComposable(group = "Test")
internal fun AppstoreDetailsContentPreview() {
   PreviewTheme {
      val rawString = URI("https://appstore-api.rebble.io/api/v1/apps/id/67c751c6d2acb30009a3c812").toURL().readText()
      val string = Json.encodeToString(Json.parseToJsonElement(rawString).jsonObject["data"]?.jsonArray[0])
      AppstoreDetailsContent(
         Json.decodeFromString(string), SnackbarHostState(), Outcome.Progress(), appstoreSource = null, navigator = null
      ) {}
   }
}
