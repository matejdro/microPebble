package com.matejdro.micropebble.navigation.keys

import com.matejdro.micropebble.navigation.keys.base.BaseScreenKey
import com.matejdro.micropebble.navigation.keys.base.TabContainerKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeScreenKey : BaseScreenKey(), TabContainerKey
