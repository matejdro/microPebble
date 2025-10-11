package com.matejdro.micropebble.apps.ui.list

import android.net.Uri
import com.matejdro.micropebble.navigation.keys.HomeScreenKey
import com.matejdro.micropebble.navigation.keys.WatchappListKey
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import si.inova.kotlinova.navigation.deeplink.DeepLinkHandler
import si.inova.kotlinova.navigation.di.OuterNavigationScope
import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import si.inova.kotlinova.navigation.instructions.ReplaceBackstack

@ContributesIntoSet(OuterNavigationScope::class)
@Inject
class PbwInstallDeepLinkHandler : DeepLinkHandler {
   override fun handleDeepLink(
      uri: Uri,
      startup: Boolean,
   ): NavigationInstruction? {
      return if (uri.scheme == "content" && uri.path?.endsWith(".pbw") == true) {
         ReplaceBackstack(
            HomeScreenKey(WatchappListKey(uri)),
         )
      } else {
         null
      }
   }
}
