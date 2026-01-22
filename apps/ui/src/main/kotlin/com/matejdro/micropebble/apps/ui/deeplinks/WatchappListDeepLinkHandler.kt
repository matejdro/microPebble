package com.matejdro.micropebble.apps.ui.deeplinks

import android.net.Uri
import com.matejdro.micropebble.navigation.keys.HomeScreenKey
import com.matejdro.micropebble.navigation.keys.WatchappListKey
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import si.inova.kotlinova.navigation.deeplink.DeepLinkHandler
import si.inova.kotlinova.navigation.deeplink.matchDeepLink
import si.inova.kotlinova.navigation.di.OuterNavigationScope
import si.inova.kotlinova.navigation.instructions.ReplaceBackstack

@ContributesIntoSet(OuterNavigationScope::class)
@Inject
class WatchappListDeepLinkHandler : DeepLinkHandler {
   override fun handleDeepLink(uri: Uri, startup: Boolean) = uri.matchDeepLink("micropebble://watchapps") {
      return ReplaceBackstack(HomeScreenKey(WatchappListKey()))
   }
}
