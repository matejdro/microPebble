package com.matejdro.micropebble.appstore.ui.keys

import androidx.compose.runtime.Immutable
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.navigation.keys.base.BaseScreenKey
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

@Immutable
@Parcelize
@TypeParceler<AppstoreSource?, AppstoreSourceParceler>()
data class AppstoreCollectionScreenKey(
   val title: String,
   val endpoint: String,
   val platformFilter: String?,
   val appstoreSource: AppstoreSource? = null,
) : BaseScreenKey()
