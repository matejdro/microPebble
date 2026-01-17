package com.matejdro.micropebble.appstore.api.store.application

import com.matejdro.micropebble.appstore.api.serializer.PebbleAPIInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ApplicationUpdate(
   @SerialName("published_date")
   @Serializable(PebbleAPIInstantSerializer::class)
   val publishedDate: Instant,
   @SerialName("release_notes")
   val releaseNotes: String,
   val version: String,
)
