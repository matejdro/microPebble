package com.matejdro.micropebble.common.notifications

import android.content.Intent

interface MainActivityProvider {
   fun getMainActivityIntent(): Intent
}
