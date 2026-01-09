package com.matejdro.micropebble.navigation.keys

import com.matejdro.micropebble.appstore.api.store.application.Application
import com.matejdro.micropebble.navigation.keys.base.BaseScreenKey
import com.matejdro.micropebble.navigation.keys.helper.ApplicationParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

@Parcelize
@TypeParceler<Application, ApplicationParceler>
data class AppstoreDetailsScreenKey(
   val app: Application,
) : BaseScreenKey()
