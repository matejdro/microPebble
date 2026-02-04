package com.matejdro.micropebble.appstore.api.store.application

import androidx.compose.runtime.Immutable
import com.matejdro.micropebble.appstore.api.serializer.BlankStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * Like [Application], but it's missing the fields that Algolia search doesn't return
 */
@Immutable
@Serializable
data class AlgoliaApplication(
   val author: String,
   val capabilities: List<String>? = null,
   val category: String,
   @SerialName("category_color")
   val categoryColor: String,
   @SerialName("category_id")
   val categoryId: String,
   val companions: String,
   val compatibility: Map<String, CompatibilityInfo>,
   val description: String,
   @SerialName("developer_id")
   val developerId: String,
   @SerialName("discourse_url")
   @Serializable(BlankStringSerializer::class)
   val discourseUrl: String? = null,
   val hearts: Int,
   @SerialName("icon_image")
   val iconImage: String,
   val id: String,
   @SerialName("list_image")
   val listImage: String,
   @SerialName("screenshot_hardware")
   val screenshotHardware: String? = null,
   @SerialName("screenshot_images")
   val screenshotImages: List<String>,
   @Serializable(BlankStringSerializer::class)
   val source: String? = null,
   val title: String,
   val type: ApplicationType,
   val uuid: Uuid,
   @Serializable(BlankStringSerializer::class)
   val website: String? = null,
) {
   fun toApplication() = Application(
      author = author,
      capabilities = capabilities.orEmpty(),
      category = category,
      categoryColor = categoryColor,
      categoryId = categoryId,
      changelog = emptyList(),
      companions = ApplicationCompanions(),
      compatibility = compatibility,
      createdAt = Instant.DISTANT_PAST,
      description = description,
      developerId = developerId,
      discourseUrl = discourseUrl,
      headerImages = emptyList(),
      hearts = hearts,
      iconImage = ApplicationIcon(small = iconImage.replace("48x48", "28x28") /* cursed */, medium = iconImage),
      id = id,
      latestRelease = ApplicationRelease(
         id = "TODO()",
         jsVersion = -1,
         pbwFile = "TODO()",
         publishedDate = Instant.DISTANT_PAST,
         releaseNotes = "TODO()",
         version = "TODO()",
      ),
      links = ApplicationLinks(
         add = "TODO()",
         addFlag = "TODO()",
         addHeart = "TODO()",
         remove = "TODO()",
         removeFlag = "TODO()",
         removeHeart = "TODO()",
         share = "TODO()",
      ),
      listImage = ApplicationImage(small = listImage, medium = listImage.replace("144x144", "80x80") /* cursed */),
      publishedDate = Instant.DISTANT_PAST,
      screenshotHardware = screenshotHardware ?: "basalt",
      screenshotImages = screenshotImages.map { ApplicationScreenshot(rectangle = it) },
      source = source,
      title = title,
      type = type,
      uuid = uuid,
      visible = true,
      website = website,
   )
}
