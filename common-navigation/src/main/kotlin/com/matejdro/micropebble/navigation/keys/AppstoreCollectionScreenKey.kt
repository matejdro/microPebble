package com.matejdro.micropebble.navigation.keys

import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.navigation.keys.base.BaseScreenKey
import com.matejdro.micropebble.navigation.keys.helper.AppstoreSourceParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

@Parcelize
@TypeParceler<AppstoreSource?, AppstoreSourceParceler>()
data class AppstoreCollectionScreenKey(
   val title: String,
   val endpoint: String,
   val platformFilter: String?,
   val appstoreSource: AppstoreSource? = null,
) : BaseScreenKey()
