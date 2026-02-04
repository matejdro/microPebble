package com.matejdro.micropebble.appstore.ui.keys

import android.os.Parcel
import com.matejdro.micropebble.appstore.api.store.application.Application
import kotlinx.parcelize.Parceler
import kotlinx.serialization.json.Json

/**
 * [Application] doesn't implement [android.os.Parcelable], opting for [Serializable] since it gets deserialized from the API
 * response. [si.inova.kotlinova.navigation.screenkeys.ScreenKey]s require themselves to be [android.os.Parcelable], but the
 * [com.matejdro.micropebble.appstore.api] module shouldn't have Android dependencies, so this external [Parceler] implementation
 * was created that just serializes the [Application] to JSON, then deserializes it from JSON.
 */
object ApplicationParceler : Parceler<Application> {
   override fun Application.write(parcel: Parcel, flags: Int) = parcel.writeString(Json.encodeToString(this))
   override fun create(parcel: Parcel): Application = Json.decodeFromString(parcel.readString()!!)
}
