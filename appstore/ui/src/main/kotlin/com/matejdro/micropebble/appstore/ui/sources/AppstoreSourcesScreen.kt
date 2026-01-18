package com.matejdro.micropebble.appstore.ui.sources

import android.webkit.URLUtil
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.matejdro.micropebble.appstore.api.AlgoliaData
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.ui.R
import com.matejdro.micropebble.navigation.keys.AppstoreSourcesScreenKey
import com.matejdro.micropebble.ui.lists.ReorderableListContainer
import si.inova.kotlinova.compose.components.itemsWithDivider
import si.inova.kotlinova.navigation.di.ContributesScreenBinding
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import com.matejdro.micropebble.sharedresources.R as sharedR

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

      var editedSource: AppstoreSource? by remember { mutableStateOf(null) }
      var isNewSource by remember { mutableStateOf(false) }

      var isConfirmResetShown by remember { mutableStateOf(false) }

      Scaffold(
         floatingActionButton = {
            FloatingActionButton(
               onClick = {
                  isNewSource = true
                  editedSource = null
               },
               content = {
                  Icon(painterResource(R.drawable.outline_add_24), contentDescription = null)
               },
            )
         },
         topBar = {
            TopAppBar(
               title = {
                  Text(stringResource(R.string.appstore_sources))
               },
               actions = {
                  IconButton(onClick = { isConfirmResetShown = true }) {
                     Icon(painterResource(R.drawable.ic_restore), contentDescription = null)
                  }
               }
            )
         }
      ) { contentPadding ->
         ReorderableListContainer(listItems, listState) { items ->
            LazyColumn(
               state = listState,
               modifier = Modifier.fillMaxSize(),
               contentPadding = contentPadding
            ) {
               itemsWithDivider(
                  items,
                  modifier = { Modifier.animateItem() }
               ) { source ->
                  ReorderableListItem(
                     key = source,
                     data = source,
                     setOrder = {
                        viewModel.reorderSource(source, it)
                     },
                     modifier = Modifier
                        .fillMaxWidth()
                  ) { modifier ->
                     Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = modifier
                           .background(MaterialTheme.colorScheme.surface)
                           .padding(8.dp)
                           .sizeIn(minHeight = 48.dp)
                     ) {
                        Column(
                           modifier = Modifier.weight(1f),
                           verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                           Text(source.name, style = MaterialTheme.typography.bodyMedium)
                        }

                        OutlinedButton(onClick = { editedSource = source }, contentPadding = PaddingValues(8.dp)) {
                           Icon(
                              painterResource(R.drawable.ic_edit),
                              contentDescription = null,
                           )
                        }
                        OutlinedButton(onClick = { viewModel.removeSource(source) }, contentPadding = PaddingValues(8.dp)) {
                           Icon(
                              painterResource(R.drawable.ic_delete),
                              contentDescription = null,
                           )
                        }
                     }
                  }
               }
            }
         }
      }
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
            title = {
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

// I think the number of if statements is triggering the cyclomatic complexity detector, even though there's really not much
// I can do about that
@Suppress("CyclomaticComplexMethod")
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AppstoreSourceConfigurator(
   source: AppstoreSource? = null,
   onSubmitted: (AppstoreSource?) -> Unit,
) {
   var name by remember { mutableStateOf(source?.name ?: "New source") }
   var url by remember { mutableStateOf(source?.url ?: "") }
   var algoliaEnabled by remember { mutableStateOf(source?.algoliaData != null) }
   var algoliaAppId by remember { mutableStateOf(source?.algoliaData?.appId ?: "") }
   var algoliaApiKey by remember { mutableStateOf(source?.algoliaData?.apiKey ?: "") }
   var algoliaIndexName by remember { mutableStateOf(source?.algoliaData?.indexName ?: "") }
   val canBeSubmitted by remember {
      derivedStateOf {
         name.isNotEmpty()
            && URLUtil.isNetworkUrl(url)
            && (!algoliaEnabled || algoliaAppId.isNotEmpty() && algoliaApiKey.isNotEmpty() && algoliaIndexName.isNotEmpty())
      }
   }
   val trySubmit = {
      if (source == null) {
         onSubmitted(null)
      }
      if (canBeSubmitted) {
         onSubmitted(
            AppstoreSource(
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
         )
      }
   }
   Dialog(onDismissRequest = { trySubmit() }, properties = DialogProperties(windowTitle = name)) {
      Card {
         Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
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
            Box(Modifier.fillMaxWidth()) {
               Button(trySubmit, enabled = canBeSubmitted, modifier = Modifier.align(Alignment.Center)) {
                  Text(stringResource(R.string.save))
               }
            }
         }
      }
   }
}
