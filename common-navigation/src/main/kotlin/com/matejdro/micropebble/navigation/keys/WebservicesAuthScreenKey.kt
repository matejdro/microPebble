package com.matejdro.micropebble.navigation.keys

import com.matejdro.micropebble.navigation.keys.base.BaseScreenKey
import kotlinx.parcelize.Parcelize
import kotlin.uuid.Uuid

@Parcelize
data class WebservicesAuthScreenKey(
   /**
    * Received from the boot URL, looks something like https://boot.rebble.io/api/stage2/?access_token=ACCESS_TOKEN&t=SOME_TIMESTAMP
    */
   val bootUrl: String? = null,
   /**
    * Change to a new UUID to force loading.
    */
   val bootUuid: Uuid = Uuid.NIL,
) : BaseScreenKey()
