package com.matejdro.micropebble.navigation.keys

import com.matejdro.micropebble.navigation.keys.base.BaseScreenKey
import com.matejdro.micropebble.navigation.keys.base.TabContainerKey
import kotlinx.parcelize.Parcelize

@Parcelize
data object HomeScreenKey : BaseScreenKey(), TabContainerKey
