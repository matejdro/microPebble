package com.matejdro.micropebble.appstore.api.store.application

import androidx.compose.runtime.Immutable
import com.matejdro.micropebble.appstore.api.serializer.PebbleAPIInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Immutable
@Serializable
data class ApplicationRelease(
   val id: String,
   @SerialName("js_md5")
   val jsMd5: String? = null,
   @SerialName("js_version")
   val jsVersion: Int,
   @SerialName("pbw_file")
   val pbwFile: String,
   @SerialName("published_date")
   @Serializable(PebbleAPIInstantSerializer::class)
   val publishedDate: Instant,
   @SerialName("release_notes")
   val releaseNotes: String,
   val version: String,
)
