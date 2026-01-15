package com.matejdro.micropebble.navigation.keys

import com.matejdro.micropebble.navigation.keys.base.BaseScreenKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppstoreCollectionScreenKey(val title: String, val endpoint: String) : BaseScreenKey()
