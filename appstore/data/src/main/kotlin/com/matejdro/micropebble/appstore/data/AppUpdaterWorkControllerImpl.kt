package com.matejdro.micropebble.appstore.data

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.matejdro.micropebble.appstore.api.updater.AppUpdaterWorkController
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@Inject
@ContributesBinding(AppScope::class)
class AppUpdaterWorkControllerImpl(
   private val workManager: WorkManager,
) : AppUpdaterWorkController {
   override fun scheduleBackgroundTasks() {
      workManager.enqueueUniquePeriodicWork(
         WORK_NAME_UPDATER,
         ExistingPeriodicWorkPolicy.KEEP,
         PeriodicWorkRequestBuilder<AppUpdaterWorker>(15.minutes.toJavaDuration()).addTag(WORK_NAME_UPDATER).build()
      )
   }
}

private const val WORK_NAME_UPDATER = "update_worker"
