package com.matejdro.micropebble.webservices.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.AppstoreSourceService
import com.matejdro.micropebble.navigation.keys.WebservicesAuthScreenKey
import com.matejdro.micropebble.sharedresources.R.string.cancel
import com.matejdro.micropebble.sharedresources.R.string.ok
import com.matejdro.micropebble.ui.components.BasicExposedDropdownMenuBox
import com.matejdro.micropebble.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.micropebble.ui.debugging.FullScreenPreviews
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import com.matejdro.micropebble.webservices.api.ParsedWebservicesToken
import com.matejdro.micropebble.webservices.api.WebservicesToken
import com.matejdro.micropebble.webservices.ui.common.NotAuthorizedDisplay
import kotlinx.coroutines.delay
import si.inova.kotlinova.compose.components.itemsWithDivider
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.di.ContributesScreenBinding
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

@Stable
@InjectNavigationScreen
@ContributesScreenBinding
class WebservicesAuthScreen(
   private val viewModel: WebservicesAuthViewModel,
) : Screen<WebservicesAuthScreenKey>() {
   @Composable
   override fun Content(key: WebservicesAuthScreenKey) {
      val tokens = viewModel.authToken.collectAsStateWithLifecycle(emptyMap()).value
      val initialToken = viewModel.startAuthToken.collectAsStateWithLifecycleAndBlinkingPrevention().value ?: Outcome.Progress()

      LaunchedEffect(key.bootUuid) {
         // Used instead of onServiceRegistered because this needs to run whenever the key's UUID changes, which signals a
         // refresh.
         viewModel.loadFromBootUrl()
      }

      WebservicesAuthScreenContent(
         tokens,
         initialToken,
         viewModel.token.collectAsStateWithLifecycle().value,
         sources = viewModel.sources.collectAsStateWithLifecycle(null).value,
         authenticate = viewModel::authenticate,
         deauthenticate = viewModel::deauthenticate,
         makeToken = viewModel::makeToken,
      )
   }
}

@Composable
private fun WebservicesAuthScreenContent(
   tokens: Map<Uuid, WebservicesToken>,
   initialToken: Outcome<ParsedWebservicesToken?>,
   potentialToken: Outcome<WebservicesToken>,
   sources: List<AppstoreSource>?,
   authenticate: (WebservicesToken) -> Unit,
   deauthenticate: (WebservicesToken) -> Unit,
   makeToken: (WebservicesToken) -> Unit,
) {
   var setupDialogData by remember { mutableStateOf(if (initialToken is Outcome.Success) initialToken.data else null) }
   var setupDialogShown by remember { mutableStateOf(if (initialToken is Outcome.Success) initialToken.data != null else false) }
   ProgressErrorSuccessScaffold(initialToken) { initialTokenData ->
      LaunchedEffect(initialTokenData) {
         if (initialTokenData != null) {
            setupDialogShown = true
            setupDialogData = initialTokenData
         }
      }

      if (tokens.isEmpty()) {
         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            NotAuthorizedDisplay {
               Column(
                  Modifier
                     .width(IntrinsicSize.Max)
                     .align(Alignment.CenterHorizontally),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
               ) {
                  val uriHandler = LocalUriHandler.current
                  Button(onClick = { uriHandler.openUri("https://boot.rebble.io") }, Modifier.fillMaxWidth()) {
                     Text(stringResource(R.string.authorize_rebble))
                  }
                  Button(onClick = { setupDialogShown = true }, Modifier.fillMaxWidth()) {
                     Text(stringResource(R.string.manual_setup))
                  }
               }
            }
         }
      } else if (sources != null) {
         LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier.fillMaxSize(),
            contentPadding = WindowInsets.safeDrawing.asPaddingValues()
         ) {
            itemsWithDivider(sources, key = { it.id }) { source ->
               val token = tokens[source.id]
               Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier
                     .background(MaterialTheme.colorScheme.surface)
                     .padding(horizontal = 16.dp, vertical = 8.dp)
                     .sizeIn(minHeight = 48.dp)
               ) {
                  Column(
                     modifier = Modifier.weight(1f),
                  ) {
                     val appyStrikethrough: TextStyle.() -> TextStyle = {
                        if (source.enabled) {
                           this
                        } else {
                           copy(textDecoration = TextDecoration.LineThrough)
                        }
                     }
                     Text(
                        source.name,
                        style = MaterialTheme.typography.bodyMedium.appyStrikethrough(),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                     )
                     Text(
                        source.id.toString(),
                        style = MaterialTheme.typography.labelSmall.appyStrikethrough(),
                        color = MaterialTheme.colorScheme.secondary,
                        overflow = TextOverflow.MiddleEllipsis,
                        maxLines = 1,
                     )
                  }
                  if (token == null) {
                     OutlinedButton(onClick = {
                        setupDialogShown = true
                        setupDialogData = ParsedWebservicesToken(sourceId = source.id)
                     }, contentPadding = PaddingValues(8.dp)) {
                        Icon(
                           painterResource(R.drawable.ic_auth_off),
                           contentDescription = null,
                        )
                     }
                  } else {
                     Button(
                        onClick = {
                           deauthenticate(token)
                        },
                        contentPadding = PaddingValues(8.dp),
                        colors = ButtonDefaults.buttonColors(
                           containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                     ) {
                        Icon(
                           painterResource(R.drawable.ic_deauth),
                           contentDescription = null,
                        )
                     }
                  }
               }
            }
            val unrecognizedTokens = tokens.filter { (tokenSourceId) -> sources.none { source -> source.id == tokenSourceId } }
            if (unrecognizedTokens.isNotEmpty()) {
               item { HorizontalDivider() }
               itemsWithDivider(unrecognizedTokens.toList()) { (_, token) ->
                  Text("Unrecognized token ${token.sourceId} for boot URL ${token.bootUrl}")
               }
            }
         }
      }

      if (setupDialogShown && sources != null) {
         ManualSetupDialog(
            tokens,
            setupDialogData,
            sources,
            potentialToken,
            onDismissed = { setupDialogShown = false },
            onSubmitted = {
               setupDialogShown = false
               authenticate(it)
            },
            {
               makeToken(it)
            },
         )
      }
   }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ManualSetupDialog(
   tokens: Map<Uuid, WebservicesToken>,
   initialValues: ParsedWebservicesToken?,
   sources: List<AppstoreSource>,
   token: Outcome<WebservicesToken>,
   onDismissed: () -> Unit,
   onSubmitted: (WebservicesToken) -> Unit,
   makeToken: (WebservicesToken) -> Unit,
   modifier: Modifier = Modifier,
) {
   var appstoreSource by remember { mutableStateOf(initialValues?.sourceId?.let { source -> sources.find { it.id == source } }) }
   var bootUrl by remember { mutableStateOf(initialValues?.bootUrl.orEmpty()) }
   var apiToken by remember { mutableStateOf(initialValues?.token.orEmpty()) }

   LaunchedEffect(appstoreSource?.id, bootUrl, apiToken) {
      // Debounce a bit.
      delay(100.milliseconds)
      appstoreSource?.id?.let { makeToken(WebservicesToken(it, bootUrl, apiToken)) }
   }

   AlertDialog(
      onDismissRequest = onDismissed,
      title = {
         Text(stringResource(R.string.manual_setup))
      },
      confirmButton = {
         if (token is Outcome.Progress) {
            CircularProgressIndicator()
         } else {
            val isSuccess = token is Outcome.Success
            TextButton(
               onClick = {
                  if (isSuccess) {
                     onSubmitted(token.data)
                  }
               },
               enabled = isSuccess,
            ) {
               Text(stringResource(ok))
            }
         }
      },
      dismissButton = {
         TextButton(onClick = onDismissed) {
            Text(stringResource(cancel))
         }
      },
      text = {
         Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BasicExposedDropdownMenuBox(
               textFieldValue = appstoreSource?.name.orEmpty(),
               modifier = Modifier.fillMaxWidth(),
               textFieldLabel = { Text(stringResource(R.string.appstore_source)) },
            ) {
               for (source in sources.filter { it.enabled }) {
                  DropdownMenuItem(
                     text = { Text(source.name) },
                     onClick = {
                        appstoreSource = source
                     },
                     contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                     enabled = source.id !in tokens, // Disable menu entries that already have associated tokens.
                  )
               }
            }
            OutlinedTextField(
               value = bootUrl,
               onValueChange = { bootUrl = it },
               label = { Text(stringResource(R.string.api_boot_url)) },
               modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
               value = apiToken,
               onValueChange = { apiToken = it },
               label = { Text(stringResource(R.string.api_token)) },
               modifier = Modifier.fillMaxWidth()
            )
         }
      },
      modifier = modifier,
   )
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun WebservicesAuthScreenContentNoTokensPreview() {
   PreviewTheme {
      WebservicesAuthScreenContent(
         emptyMap(),
         Outcome.Success(null),
         Outcome.Error(InvalidTokenException()),
         AppstoreSourceService.defaultSources,
         authenticate = {},
         deauthenticate = {},
         makeToken = {},
      )
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun WebservicesAuthScreenContentWithTokensPreview() {
   PreviewTheme {
      val sourceId = AppstoreSourceService.defaultSources.first().id
      WebservicesAuthScreenContent(
         mapOf(
            sourceId to WebservicesToken(
               sourceId = sourceId,
               bootUrl = "whatever",
               token = "my token",
            )
         ),
         Outcome.Success(null),
         Outcome.Error(InvalidTokenException()),
         AppstoreSourceService.defaultSources,
         authenticate = {},
         deauthenticate = {},
         makeToken = {},
      )
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
internal fun WebservicesAuthScreenContentWithInitialTokenPreview() {
   PreviewTheme {
      val sourceId = AppstoreSourceService.defaultSources.first().id
      WebservicesAuthScreenContent(
         emptyMap(),
         Outcome.Success(ParsedWebservicesToken(sourceId, "whatever", "my token")),
         Outcome.Error(InvalidTokenException()),
         AppstoreSourceService.defaultSources,
         authenticate = {},
         deauthenticate = {},
         makeToken = {},
      )
   }
}
