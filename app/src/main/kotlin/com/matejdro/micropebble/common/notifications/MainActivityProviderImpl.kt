package com.matejdro.micropebble.common.notifications

import android.content.Context
import android.content.Intent
import com.matejdro.micropebble.MainActivity
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class MainActivityProviderImpl(
   private val context: Context,
) : MainActivityProvider {
   override fun getMainActivityIntent(): Intent {
      return Intent(context, MainActivity::class.java)
   }
}
