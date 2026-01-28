package com.matejdro.micropebble.navigation.keys.helper

import android.os.Parcel
import com.matejdro.micropebble.appstore.api.AppstoreSource
import kotlinx.parcelize.Parceler
import kotlinx.serialization.json.Json

class AppstoreSourceParceler : Parceler<AppstoreSource?> {
   override fun AppstoreSource?.write(parcel: Parcel, flags: Int) = parcel.writeString(Json.encodeToString(this))
   override fun create(parcel: Parcel): AppstoreSource? = Json.decodeFromString(parcel.readString()!!)
}
