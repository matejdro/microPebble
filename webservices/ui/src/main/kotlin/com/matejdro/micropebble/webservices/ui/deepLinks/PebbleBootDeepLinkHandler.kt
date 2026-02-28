package com.matejdro.micropebble.webservices.ui.deepLinks

import android.net.Uri
import com.matejdro.micropebble.navigation.keys.WebservicesAuthScreenKey
import com.zhuinden.simplestack.StateChange
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import kotlinx.parcelize.Parcelize
import si.inova.kotlinova.navigation.deeplink.DeepLinkHandler
import si.inova.kotlinova.navigation.deeplink.matchDeepLink
import si.inova.kotlinova.navigation.di.NavigationContext
import si.inova.kotlinova.navigation.di.OuterNavigationScope
import si.inova.kotlinova.navigation.instructions.NavigationInstruction
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import java.net.URLDecoder
import java.nio.charset.Charset
import kotlin.uuid.Uuid

/**
 * Need a special instruction instead of OpenScreenOrMoveToTop as I don't want to compare by value, since it changes based on the
 * token passed.
 */
@Parcelize
private data class PebbleBootNavigationInstruction(val screen: ScreenKey) : NavigationInstruction() {
   override fun performNavigation(backstack: List<ScreenKey>, context: NavigationContext) =
      NavigationResult(backstack.filter { it !is WebservicesAuthScreenKey } + screen, StateChange.FORWARD)
}

@ContributesIntoSet(OuterNavigationScope::class)
@Inject
class PebbleBootDeepLinkHandler : DeepLinkHandler {
   override fun handleDeepLink(uri: Uri, startup: Boolean) =
      uri.matchDeepLink("pebble://custom-boot-config-url/{args}") { params ->
         val key = WebservicesAuthScreenKey(
            // Fall back to the undecoded URI if decoding fails.
            runCatching { URLDecoder.decode(params["args"], Charset.defaultCharset().name())!! }.getOrElse { params["args"] },
            Uuid.random()
         )
         return PebbleBootNavigationInstruction(key)
      }
}
