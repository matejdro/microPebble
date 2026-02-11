package com.matejdro.micropebble.appstore.ui.common

import com.matejdro.micropebble.appstore.api.store.application.Application
import com.matejdro.micropebble.appstore.api.store.application.ApplicationCompanions
import com.matejdro.micropebble.appstore.api.store.application.ApplicationIcon
import com.matejdro.micropebble.appstore.api.store.application.ApplicationImage
import com.matejdro.micropebble.appstore.api.store.application.ApplicationLinks
import com.matejdro.micropebble.appstore.api.store.application.ApplicationRelease
import com.matejdro.micropebble.appstore.api.store.application.ApplicationScreenshot
import com.matejdro.micropebble.appstore.api.store.application.ApplicationType
import com.matejdro.micropebble.appstore.api.store.application.ApplicationUpdate
import com.matejdro.micropebble.appstore.api.store.application.CompatibilityInfo
import com.matejdro.micropebble.appstore.api.store.application.HeaderImage
import kotlin.random.Random
import kotlin.time.Instant
import kotlin.uuid.Uuid

private const val MIN_LIKES = 5
private const val MAX_LIKES = 50

@Suppress("LongMethod") // I cannot make this shorter.
fun makeFakeApp(
   name: String = "My Really Cool Watchface",
   author: String = "Author",
   likes: Int? = null,
   hasSource: Boolean? = null,
): Application {
   val random = Random(name.map { it.code }.sum())
   val realHasSource = hasSource ?: random.nextBoolean()
   val realLikes = likes ?: random.nextInt(MIN_LIKES, MAX_LIKES)
   return Application(
      author = author,
      capabilities = listOf("configurable"),
      category = "Faces",
      categoryColor = "ffffff",
      categoryId = "528d3ef2dc7b5f580700000a",
      changelog = listOf(
         ApplicationUpdate(
            Instant.parse("2026-02-06T22:24:09.064996519Z"),
            releaseNotes = "Initial release",
            version = "1.0.0",
         )
      ),
      companions = ApplicationCompanions(),
      compatibility = mapOf(
         "android" to CompatibilityInfo(true),
         "aplite" to CompatibilityInfo(true),
         "basalt" to CompatibilityInfo(true),
         "emery" to CompatibilityInfo(true)
      ),
      createdAt = Instant.parse("2026-02-06T22:24:09.064996519Z"),
      description = "A really long description",
      developerId = "",
      discourseUrl = "discourse URL",
      headerImages = listOf(HeaderImage("", "")),
      hearts = realLikes,
      iconImage = ApplicationIcon("", ""),
      id = Uuid.random().toString(),
      latestRelease = ApplicationRelease(
         id = "",
         jsVersion = -1,
         pbwFile = "",
         publishedDate = Instant.parse("2026-02-06T22:24:09.064996519Z"),
         releaseNotes = "AAAAAAAA",
         version = "1.0.0",
      ),
      links = ApplicationLinks(
         add = "",
         addFlag = "",
         addHeart = "",
         remove = "",
         removeFlag = "",
         removeHeart = "",
         share = ""
      ),
      listImage = ApplicationImage("", ""),
      publishedDate = Instant.parse("2026-02-06T22:24:09.064996519Z"),
      screenshotHardware = "basalt",
      screenshotImages = listOf(ApplicationScreenshot(""), ApplicationScreenshot(""), ApplicationScreenshot("")),
      source = "source link".takeIf { realHasSource },
      title = name,
      type = ApplicationType.Watchface,
      uuid = Uuid.random(),
      visible = true,
      website = "https://github.com/MateJDroR/MateJDroR",
   )
}
