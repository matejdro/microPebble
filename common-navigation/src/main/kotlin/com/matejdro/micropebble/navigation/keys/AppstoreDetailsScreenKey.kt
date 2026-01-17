package com.matejdro.micropebble.navigation.keys

import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.store.application.Application
import com.matejdro.micropebble.navigation.keys.base.BaseScreenKey
import com.matejdro.micropebble.navigation.keys.helper.ApplicationParceler
import com.matejdro.micropebble.navigation.keys.helper.AppstoreSourceParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

@Parcelize
@TypeParceler<Application, ApplicationParceler>
@TypeParceler<AppstoreSource?, AppstoreSourceParceler>
data class AppstoreDetailsScreenKey(
   val app: Application,
   val onlyPartialData: Boolean,
   val appstoreSource: AppstoreSource? = null,
) : BaseScreenKey()
