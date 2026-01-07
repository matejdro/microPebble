package com.matejdro.micropebble.appstore.api.store.application

import com.matejdro.micropebble.appstore.api.serializer.Rfc1123InstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

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
   @Serializable(Rfc1123InstantSerializer::class)
   val publishedDate: Instant,
   @SerialName("release_notes")
   val releaseNotes: String,
   val version: String,
)
