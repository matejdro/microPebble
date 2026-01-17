package com.matejdro.micropebble.appstore.api.store.application

import com.matejdro.micropebble.appstore.api.serializer.BlankStringSerializer
import com.matejdro.micropebble.appstore.api.serializer.HeaderImagesSerializer
import com.matejdro.micropebble.appstore.api.serializer.PebbleAPIInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Application(
   val author: String,
   val capabilities: List<String>,
   val category: String,
   @SerialName("category_color")
   val categoryColor: String,
   @SerialName("category_id")
   val categoryId: String,
   val changelog: List<ApplicationUpdate>,
   val companions: ApplicationCompanions,
   val compatibility: Map<String, CompatibilityInfo>,
   @SerialName("created_at")
   @Serializable(PebbleAPIInstantSerializer::class)
   val createdAt: Instant,
   val description: String,
   @SerialName("developer_id")
   val developerId: String,
   @SerialName("discourse_url")
   @Serializable(BlankStringSerializer::class)
   val discourseUrl: String? = null,
   @SerialName("header_images")
   @Serializable(HeaderImagesSerializer::class)
   val headerImages: List<HeaderImage>,
   val hearts: Int,
   @SerialName("icon_image")
   val iconImage: ApplicationIcon,
   val id: String,
   @SerialName("latest_release")
   val latestRelease: ApplicationRelease,
   val links: ApplicationLinks,
   @SerialName("list_image")
   val listImage: ApplicationImage,
   /**
    * Unused, possibly.
    */
   @SerialName("published_date")
   @Serializable(PebbleAPIInstantSerializer::class)
   val publishedDate: Instant?,
   @SerialName("screenshot_hardware")
   val screenshotHardware: String,
   @SerialName("screenshot_images")
   val screenshotImages: List<ApplicationScreenshot>,
   @Serializable(BlankStringSerializer::class)
   val source: String? = null,
   val title: String,
   val type: ApplicationType,
   val uuid: Uuid,
   val visible: Boolean,
   @Serializable(BlankStringSerializer::class)
   val website: String? = null,
)
