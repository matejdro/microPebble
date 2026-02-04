package com.matejdro.micropebble.appstore.ui.sources

import android.webkit.URLUtil
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.micropebble.appstore.api.AlgoliaData
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.ui.R
import com.matejdro.micropebble.navigation.keys.AppstoreSourcesScreenKey
import com.matejdro.micropebble.ui.debugging.FullScreenPreviews
import com.matejdro.micropebble.ui.debugging.PreviewTheme
import com.matejdro.micropebble.appstore.ui.common.NoSourcesDisplay
import com.matejdro.micropebble.ui.lists.ReorderableListContainer
import si.inova.kotlinova.compose.components.itemsWithDivider
import si.inova.kotlinova.navigation.di.ContributesScreenBinding
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import kotlin.uuid.Uuid
import com.matejdro.micropebble.sharedresources.R as sharedR

@Stable
@InjectNavigationScreen
@ContributesScreenBinding
class AppstoreSourcesScreen(
   private val viewModel: AppstoreSourcesViewModel,
) : Screen<AppstoreSourcesScreenKey>() {
   @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
   @Composable
   override fun Content(key: AppstoreSourcesScreenKey) {
      val listState = rememberLazyListState()
      val listItems by viewModel.sources.collectAsState(emptyList())
      val isDefaultSources = viewModel.isDefaultSources.collectAsState(initial = true).value

      var editedSource: AppstoreSource? by remember { mutableStateOf(null) }
      var isNewSource by remember { mutableStateOf(false) }

      var isConfirmResetShown by remember { mutableStateOf(false) }

      AppstoreSourcesContent(
         onCreateSource = {
            isNewSource = true
            editedSource = null
         },
         onReset = {
            isConfirmResetShown = true
         },
         isDefaultSources = isDefaultSources,
         listItems = listItems,
         listState = listState,
         setOrder = { source, newIndex -> viewModel.reorderSource(source, newIndex) },
         sourceEnabledChange = { source, enabled ->
            viewModel.replaceSource(
               source,
               source.copy(enabled = enabled)
            )
         },
         onEditSource = { editedSource = it },
         onRemoveSource = { viewModel.removeSource(it) }
      )

      if (editedSource != null || isNewSource) {
         AppstoreSourceConfigurator(editedSource) { newSource ->
            if (newSource != null) {
               val editedSource2 = editedSource
               if (editedSource2 == null) {
                  viewModel.addSource(newSource)
               } else {
                  viewModel.replaceSource(editedSource2, newSource)
               }
            }
            isNewSource = false
            editedSource = null
         }
      }
      if (isConfirmResetShown) {
         AlertDialog(
            onDismissRequest = { isConfirmResetShown = false },
            icon = {
               Icon(painterResource(R.drawable.ic_restore), contentDescription = null)
            },
            text = {
               Text(stringResource(R.string.confirm_restore))
            },
            confirmButton = {
               TextButton(
                  onClick = {
                     viewModel.restoreSources()
                     isConfirmResetShown = false
                  }
               ) {
                  Text(stringResource(sharedR.string.ok))
               }
            },
            dismissButton = {
               TextButton(onClick = { isConfirmResetShown = false }) {
                  Text(stringResource(sharedR.string.cancel))
               }
            }
         )
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppstoreSourcesContent(
   onCreateSource: () -> Unit,
   onReset: () -> Unit,
   isDefaultSources: Boolean,
   listItems: List<AppstoreSource>,
   listState: LazyListState,
   setOrder: (AppstoreSource, Int) -> Unit,
   sourceEnabledChange: (AppstoreSource, Boolean) -> Unit,
   onEditSource: (AppstoreSource) -> Unit,
   onRemoveSource: (AppstoreSource) -> Unit,
) {
   Scaffold(
      floatingActionButton = {
         FloatingActionButton(
            onClick = onCreateSource,
            content = {
               Icon(painterResource(R.drawable.ic_add), contentDescription = null)
            },
         )
      },
      topBar = {
         TopAppBar(
            title = {
               Text(stringResource(R.string.appstore_sources))
            },
            actions = {
               IconButton(onClick = onReset, enabled = !isDefaultSources) {
                  Icon(painterResource(R.drawable.ic_restore), contentDescription = null)
               }
            }
         )
      }
   ) { contentPadding ->
      ReorderableListContainer(listItems, listState) { items ->
         LazyColumn(
            state = listState,
            modifier = Modifier
               .fillMaxSize()
               .padding(contentPadding),
         ) {
            itemsWithDivider(
               items,
               key = { it.id },
               modifier = { Modifier.animateItem() }
            ) { source ->
               ReorderableListItem(
                  key = source.id.toString(),
                  data = source,
                  setOrder = { setOrder(source, it) },
                  modifier = Modifier
                     .fillMaxWidth()
               ) { modifier ->
                  SourceListItem(
                     source,
                     sourceEnabledChange = { sourceEnabledChange(source, it) },
                     onEditSource = { onEditSource(source) },
                     onRemoveSource = { onRemoveSource(source) },
                     modifier
                  )
               }
            }
            if (!listItems.any { it.enabled }) {
               item {
                  NoSourcesDisplay()
               }
            }
         }
      }
   }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun SourceListItem(
   source: AppstoreSource,
   sourceEnabledChange: (Boolean) -> Unit,
   onEditSource: () -> Unit,
   onRemoveSource: () -> Unit,
   modifier: Modifier = Modifier,
) {
   Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = modifier
         .background(MaterialTheme.colorScheme.surface)
         .padding(horizontal = 16.dp, vertical = 8.dp)
         .sizeIn(minHeight = 48.dp)
   ) {
      OutlinedToggleButton(
         checked = source.enabled,
         onCheckedChange = sourceEnabledChange,
         contentPadding = PaddingValues(8.dp),
      ) {
         Icon(
            painterResource(R.drawable.ic_appstore_source),
            contentDescription = null,
         )
      }

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

      OutlinedButton(onClick = onEditSource, contentPadding = PaddingValues(8.dp)) {
         Icon(
            painterResource(R.drawable.ic_edit),
            contentDescription = null,
         )
      }
      OutlinedButton(onClick = onRemoveSource, contentPadding = PaddingValues(8.dp)) {
         Icon(
            painterResource(R.drawable.ic_delete),
            contentDescription = null,
         )
      }
   }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AppstoreSourceConfigurator(
   source: AppstoreSource? = null,
   onSubmitted: (AppstoreSource?) -> Unit,
) {
   val id = source?.id ?: remember { Uuid.random() }
   var name by remember { mutableStateOf(source?.name ?: "New source") }
   var url by remember { mutableStateOf(source?.url.orEmpty()) }
   var algoliaEnabled by remember { mutableStateOf(source?.algoliaData != null) }
   var algoliaAppId by remember { mutableStateOf(source?.algoliaData?.appId.orEmpty()) }
   var algoliaApiKey by remember { mutableStateOf(source?.algoliaData?.apiKey.orEmpty()) }
   var algoliaIndexName by remember { mutableStateOf(source?.algoliaData?.indexName.orEmpty()) }
   val newSource by remember {
      derivedStateOf {
         AppstoreSource(
            id = id,
            url = url,
            name = name,
            algoliaData = if (algoliaEnabled) {
               AlgoliaData(
                  appId = algoliaAppId,
                  apiKey = algoliaApiKey,
                  indexName = algoliaIndexName,
               )
            } else {
               null
            }
         )
      }
   }
   val trySubmit = {
      if (newSource.isValid()) {
         onSubmitted(newSource)
      } else if (source == null) {
         onSubmitted(null)
      }
   }
   AlertDialog(
      onDismissRequest = { trySubmit() },
      title = {
         Text(stringResource(R.string.edit_source, name))
      },
      confirmButton = {
         TextButton(
            onClick = { trySubmit() },
            enabled = newSource.isValid(),
         ) {
            Text(stringResource(sharedR.string.ok))
         }
      },
      dismissButton = {
         TextButton(onClick = { onSubmitted(null) }) {
            Text(stringResource(sharedR.string.cancel))
         }
      },
      text = {
         Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
               name,
               { name = it },
               label = { Text(stringResource(R.string.name)) },
               modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
               url,
               { url = it },
               label = { Text(stringResource(R.string.url)) },
               modifier = Modifier.fillMaxWidth()
            )

            Row(
               horizontalArrangement = Arrangement.SpaceBetween,
               verticalAlignment = Alignment.CenterVertically,
               modifier = Modifier.fillMaxWidth()
            ) {
               Text("Search API", style = MaterialTheme.typography.titleMedium)
               Switch(algoliaEnabled, { algoliaEnabled = it })
            }

            if (algoliaEnabled) {
               OutlinedTextField(
                  algoliaAppId,
                  { algoliaAppId = it },
                  label = { Text(stringResource(R.string.app_id)) },
                  modifier = Modifier.fillMaxWidth()
               )

               OutlinedTextField(
                  algoliaApiKey,
                  { algoliaApiKey = it },
                  label = { Text(stringResource(R.string.api_key)) },
                  modifier = Modifier.fillMaxWidth()
               )

               OutlinedTextField(
                  algoliaIndexName,
                  { algoliaIndexName = it },
                  label = { Text(stringResource(R.string.index_name)) },
                  modifier = Modifier.fillMaxWidth()
               )
            }
            Text(
               stringResource(R.string.id_placeholder, id),
               style = MaterialTheme.typography.labelSmall,
               color = MaterialTheme.colorScheme.secondary
            )
         }
      }
   )
}

private fun AppstoreSource.isValid() = name.isNotEmpty() && URLUtil.isNetworkUrl(url) && algoliaData.isValid()

private fun AlgoliaData?.isValid() = this == null || appId.isNotBlank() && apiKey.isNotBlank() && indexName.isNotBlank()

private val baseSources = listOf(
   AppstoreSource(
      id = Uuid.parse("00000000-89ab-cdef-0123-456789abcdef"),
      url = "",
      name = "Appstore source 1"
   ),
   AppstoreSource(
      id = Uuid.parse("10000000-89ab-cdef-0123-456789abcdef"),
      url = "",
      name = "Appstore source 2",
      enabled = false,
   ),
   AppstoreSource(
      id = Uuid.parse("20000000-89ab-cdef-0123-456789abcdef"),
      url = "",
      name = "Appstore source 3"
   ),
   AppstoreSource(
      id = Uuid.parse("30000000-89ab-cdef-0123-456789abcdef"),
      url = "",
      name = "Appstore source 4444444444444"
   ),
)

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
private fun AppstoreSourcesPreview() {
   PreviewTheme {
      val sources = baseSources
      AppstoreSourcesContent(
         onCreateSource = {},
         onReset = {},
         isDefaultSources = sources == baseSources,
         listItems = sources,
         listState = rememberLazyListState(),
         setOrder = { _, _ -> },
         sourceEnabledChange = { _, _ -> },
         onEditSource = {},
         onRemoveSource = {},
      )
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
private fun AllDisabledAppstoreSourcesPreview() {
   PreviewTheme {
      val sources = baseSources.map { it.copy(enabled = false) }
      AppstoreSourcesContent(
         onCreateSource = {},
         onReset = {},
         isDefaultSources = sources == baseSources,
         listItems = sources,
         listState = rememberLazyListState(),
         setOrder = { _, _ -> },
         sourceEnabledChange = { _, _ -> },
         onEditSource = {},
         onRemoveSource = {},
      )
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "Test")
private fun NoSourcesSourcesPreview() {
   PreviewTheme {
      val sources = emptyList<AppstoreSource>()
      AppstoreSourcesContent(
         onCreateSource = {},
         onReset = {},
         isDefaultSources = sources == baseSources,
         listItems = sources,
         listState = rememberLazyListState(),
         setOrder = { _, _ -> },
         sourceEnabledChange = { _, _ -> },
         onEditSource = {},
         onRemoveSource = {},
      )
   }
}
