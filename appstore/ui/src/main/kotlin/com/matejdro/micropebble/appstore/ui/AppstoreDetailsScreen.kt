package com.matejdro.micropebble.appstore.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.micropebble.appstore.api.AppInstallState
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.store.application.Application
import com.matejdro.micropebble.appstore.api.store.application.ApplicationCompanions
import com.matejdro.micropebble.appstore.api.store.application.ApplicationIcon
import com.matejdro.micropebble.appstore.api.store.application.ApplicationImage
import com.matejdro.micropebble.appstore.api.store.application.ApplicationLinks
import com.matejdro.micropebble.appstore.api.store.application.ApplicationRelease
import com.matejdro.micropebble.appstore.api.store.application.ApplicationScreenshot
import com.matejdro.micropebble.appstore.api.store.application.ApplicationType
import com.matejdro.micropebble.appstore.api.store.application.ApplicationUpdate
import com.matejdro.micropebble.appstore.api.store.application.CompatibilityInfo
import com.matejdro.micropebble.appstore.api.store.application.HeaderImage
import com.matejdro.micropebble.appstore.api.store.application.getImage
import com.matejdro.micropebble.appstore.ui.common.BANNER_RATIO
import com.matejdro.micropebble.appstore.ui.common.getIcon
import com.matejdro.micropebble.appstore.ui.common.getWatchesForCodename
import com.matejdro.micropebble.appstore.ui.keys.AppstoreCollectionScreenKey
import com.matejdro.micropebble.appstore.ui.keys.AppstoreDetailsScreenKey
import com.matejdro.micropebble.common.util.joinUrls
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.micropebble.ui.debugging.FullScreenPreviews
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import dev.zacsweers.metro.Inject
import io.rebble.libpebblecommon.metadata.WatchType
import kotlinx.coroutines.flow.filterIsInstance
import si.inova.kotlinova.compose.components.itemsWithDivider
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.compose.time.ComposeAndroidDateTimeFormatter
import si.inova.kotlinova.compose.time.LocalDateFormatter
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.time.FakeAndroidDateTimeFormatter
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import java.time.ZoneId
import java.time.format.FormatStyle
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.uuid.Uuid
import com.matejdro.micropebble.sharedresources.R as sharedR

@Composable
private fun Instant.formatDate(): String {
   return toJavaInstant().atZone(ZoneId.systemDefault())
      .format(LocalDateFormatter.current.ofLocalizedDateTime(FormatStyle.SHORT))
}

@Stable
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

      var isWarningPopupShown by remember { mutableStateOf(false) }

      ProgressErrorSuccessScaffold(dataState) { app ->
         AppstoreDetailsContent(
            app = app,
            snackbarHostState,
            appInstallState = installState,
            uninstallApp = viewModel::uninstall,
            installApp = {
               if (viewModel.appState.value.data == AppInstallState.INCOMPATIBLE) {
                  isWarningPopupShown = true
               } else {
                  viewModel.install()
               }
            },
            viewModel.platform,
            key.appstoreSource,
            navigator,
         )
         if (isWarningPopupShown) {
            AlertDialog(
               { isWarningPopupShown = false },
               title = {
                  Text(stringResource(R.string.install_incompatible_title))
               },
               text = {
                  Text(stringResource(R.string.install_incompatible))
               },
               confirmButton = {
                  TextButton(
                     onClick = {
                        viewModel.install()
                        isWarningPopupShown = false
                     }
                  ) {
                     Text(stringResource(R.string.install))
                  }
               },
               dismissButton = {
                  TextButton(onClick = { isWarningPopupShown = false }) {
                     Text(stringResource(sharedR.string.cancel))
                  }
               }
            )
         }
      }
   }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AppstoreDetailsContent(
   app: Application,
   errorsSnackbarState: SnackbarHostState,
   appInstallState: Outcome<AppInstallState>,
   uninstallApp: () -> Unit,
   installApp: () -> Unit,
   platform: WatchType?,
   appstoreSource: AppstoreSource? = null,
   navigator: Navigator? = null,
) {
   val doNothing = {}

   var showVersionSheet by remember { mutableStateOf(false) }
   var showCompatibilitySheet by remember { mutableStateOf(false) }

   @Suppress("UnusedMaterial3ScaffoldPaddingParameter") // It doesn't seem to need it, other things consume the padding
   Scaffold(floatingActionButton = {
      ExtendedFloatingActionButton(
         onClick = when {
            appInstallState !is Outcome.Success -> doNothing
            appInstallState.data == AppInstallState.CAN_INSTALL -> installApp
            appInstallState.data == AppInstallState.INCOMPATIBLE -> installApp
            appInstallState.data == AppInstallState.INSTALLED -> uninstallApp
            else -> doNothing
         },
         modifier = Modifier
            .defaultMinSize(minHeight = 56.dp)
            .animateContentSize(),
      ) {
         if (appInstallState is Outcome.Progress) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
         } else {
            val icon = when (appInstallState.data) {
               AppInstallState.CAN_INSTALL -> painterResource(R.drawable.ic_download)
               AppInstallState.INSTALLED -> painterResource(R.drawable.ic_delete)
               AppInstallState.INCOMPATIBLE -> painterResource(R.drawable.ic_warning)
               null -> painterResource(R.drawable.ic_download)
            }
            Icon(icon, contentDescription = "Install")
            val text = when (appInstallState.data) {
               AppInstallState.CAN_INSTALL -> stringResource(R.string.install)
               AppInstallState.INSTALLED -> stringResource(R.string.uninstall)
               AppInstallState.INCOMPATIBLE -> stringResource(R.string.not_installable)
               null -> ""
            }
            Text(text, modifier = Modifier.padding(start = 8.dp))
         }
      }
   }, snackbarHost = {
      SnackbarHost(hostState = errorsSnackbarState)
   }) { _ ->
      val childModifier = Modifier
      val collectionTitle = stringResource(R.string.appstore_collection_title, app.author)
      val actions = remember {
         listOf(
            AppButton(R.string.app_version_info) {
               showVersionSheet = true
            }
         ) + getActionsFor(app, collectionTitle, navigator, platform, appstoreSource)
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
         item(contentType = "banner") {
            Banner(app)
         }

         item(contentType = "title") {
            TitleCard(app, childModifier)
         }

         item(contentType = "carousel") {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
               AppScreenshotCarousel(app, childModifier)
            }
         }

         item(contentType = "") {
            Card(onClick = { showCompatibilitySheet = true }, modifier = childModifier.fillMaxWidth()) {
               Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                     .padding(8.dp)
                     .align(Alignment.CenterHorizontally)
               ) {
                  for ((appPlatform, appCompatibility) in app.compatibility - "android" - "ios") {
                     if (appCompatibility.supported) {
                        appPlatform.getIcon()
                           ?.let { Icon(painterResource(it), contentDescription = null, modifier = Modifier.size(36.dp)) }
                     }
                  }
               }
            }
         }

         item(contentType = "description") {
            Card(modifier = childModifier.fillMaxWidth()) {
               Text(app.description, Modifier.padding(8.dp))
            }
         }

         item(contentType = "info") {
            InfoCard(app, childModifier)
         }

         item(contentType = "links") {
            LinksCard(actions, childModifier)
         }
      }

      if (showVersionSheet) {
         ModalBottomSheet(onDismissRequest = { showVersionSheet = false }, sheetState = rememberModalBottomSheetState()) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(8.dp)) {
               itemsWithDivider(app.changelog) {
                  Text(it.version, Modifier.padding(8.dp), style = MaterialTheme.typography.titleLarge)
                  Text(it.publishedDate.formatDate(), Modifier.padding(8.dp))
                  Text(it.releaseNotes, Modifier.padding(8.dp))
               }
            }
         }
      }

      if (showCompatibilitySheet) {
         ModalBottomSheet(
            onDismissRequest = { showCompatibilitySheet = false },
            sheetState = rememberModalBottomSheetState(),
         ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(8.dp)) {
               item {
                  Text(stringResource(R.string.compatibility_info_header), style = MaterialTheme.typography.titleLarge)
               }
               for ((appPlatform, appCompatibility) in app.compatibility - "android" - "ios") {
                  if (appCompatibility.supported) {
                     val type = WatchType.fromCodename(appPlatform)
                     type?.let {
                        item {
                           Row(
                              horizontalArrangement = Arrangement.spacedBy(8.dp),
                              verticalAlignment = Alignment.CenterVertically
                           ) {
                              Icon(painterResource(it.getIcon()), contentDescription = null)
                              val watches = getWatchesForCodename(type.codename).joinToString(", ")
                              Text(stringResource(R.string.compatibility_info, type.codename, watches))
                           }
                        }
                     }
                  }
               }
               item {
                  Box(Modifier.height(56.dp))
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
      val (imageUrl, hardware) = app.screenshotImages[it].getImage()
      val ratio = ApplicationScreenshot.Hardware.fromHardwarePlatform(app.screenshotHardware)?.aspectRatio
         ?: hardware.aspectRatio
      AsyncImage(
         model = imageUrl,
         contentDescription = "Screenshot image $it for ${app.title}",
         modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ratio).run {
               if (hardware == ApplicationScreenshot.Hardware.CIRCLE) {
                  maskClip(CircleShape)
               } else {
                  maskClip(CardDefaults.shape)
               }
            },
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
                     painter = painterResource(R.drawable.ic_open_externally),
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
   title: String,
   navigator: Navigator? = null,
   platformFilter: WatchType? = null,
   appstoreSource: AppstoreSource? = null,
): List<AppAction> = buildList {
   app.website?.let { add(AppLink(R.string.app_website_link, it)) }
   app.source?.let { add(AppLink(R.string.app_source_code, it)) }
   app.discourseUrl?.let { add(AppLink(R.string.app_discourse_link, it)) }
   if (navigator != null && appstoreSource != null) {
      add(
         AppButton(R.string.apps_from_developer) {
            navigator.navigateTo(
               AppstoreCollectionScreenKey(
                  title = title,
                  endpoint = appstoreSource.url.joinUrls("v1/apps/dev/${app.developerId}"),
                  platformFilter = platformFilter?.codename,
                  appstoreSource = appstoreSource
               )
            )
         }
      )
   }
}

private sealed class AppAction(val label: Int)
private class AppButton(label: Int, val onClick: () -> Unit) : AppAction(label)
private class AppLink(label: Int, val linkTarget: String) : AppAction(label)

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun AppstoreDetailsContentPreview() {
   PreviewTheme {
      val exampleApp = Application(
         author = "Author",
         capabilities = listOf("configurable"),
         category = "Faces",
         categoryColor = "ffffff",
         categoryId = "528d3ef2dc7b5f580700000a",
         changelog = listOf(
            ApplicationUpdate(
               Instant.parse("2026-02-06T22:24:09.064996519Z"),
               releaseNotes = "Initial release",
               version = "1.0.0",
            )
         ),
         companions = ApplicationCompanions(),
         compatibility = mapOf(
            "android" to CompatibilityInfo(true),
            "aplite" to CompatibilityInfo(true),
            "basalt" to CompatibilityInfo(true),
            "emery" to CompatibilityInfo(true)
         ),
         createdAt = Instant.parse("2026-02-06T22:24:09.064996519Z"),
         description = "A really long description",
         developerId = "",
         discourseUrl = "discourse URL",
         headerImages = listOf(HeaderImage("", "")),
         hearts = 15,
         iconImage = ApplicationIcon("", ""),
         id = "id",
         latestRelease = ApplicationRelease(
            id = "",
            jsVersion = -1,
            pbwFile = "",
            publishedDate = Instant.parse("2026-02-06T22:24:09.064996519Z"),
            releaseNotes = "AAAAAAAA",
            version = "1.0.0",
         ),
         links = ApplicationLinks(
            add = "",
            addFlag = "",
            addHeart = "",
            remove = "",
            removeFlag = "",
            removeHeart = "",
            share = ""
         ),
         listImage = ApplicationImage("", ""),
         publishedDate = Instant.parse("2026-02-06T22:24:09.064996519Z"),
         screenshotHardware = "basalt",
         screenshotImages = listOf(ApplicationScreenshot(""), ApplicationScreenshot(""), ApplicationScreenshot("")),
         source = "source link",
         title = "My Really Cool Watchface",
         type = ApplicationType.Watchface,
         uuid = Uuid.random(),
         visible = true,
         website = "https://github.com/MateJDroR/MateJDroR",
      )

      val dateTimeFormatter = FakeAndroidDateTimeFormatter()

      CompositionLocalProvider(LocalDateFormatter provides ComposeAndroidDateTimeFormatter(dateTimeFormatter)) {
         AppstoreDetailsContent(exampleApp, SnackbarHostState(), Outcome.Progress(), {}, {}, platform = null)
      }
   }
}
