package com.matejdro.micropebble.bluetooth.firmware

import android.net.Uri
import com.matejdro.micropebble.navigation.keys.FirmwareUpdateScreenKey
import com.matejdro.micropebble.navigation.keys.HomeScreenKey
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import si.inova.kotlinova.navigation.deeplink.DeepLinkHandler
import si.inova.kotlinova.navigation.di.OuterNavigationScope
import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import si.inova.kotlinova.navigation.instructions.ReplaceBackstackOrOpenScreen

@ContributesIntoSet(OuterNavigationScope::class)
@Inject
class FirmwareUpdateDeepLinkHandler : DeepLinkHandler {
   override fun handleDeepLink(
      uri: Uri,
      startup: Boolean,
   ): NavigationInstruction? {
      return if (uri.scheme == "content" && uri.path?.endsWith(".pbz") == true) {
         ReplaceBackstackOrOpenScreen(
            startup,
            HomeScreenKey(),
            FirmwareUpdateScreenKey(pbzUri = uri)
         )
      } else {
         null
      }
   }
}
