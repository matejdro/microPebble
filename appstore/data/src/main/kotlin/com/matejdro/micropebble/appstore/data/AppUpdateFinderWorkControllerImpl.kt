package com.matejdro.micropebble.appstore.data

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.matejdro.micropebble.appstore.api.updater.AppUpdateFinderWorkController
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

@Inject
@ContributesBinding(AppScope::class)
class AppUpdateFinderWorkControllerImpl(
   private val workManager: WorkManager,
) : AppUpdateFinderWorkController {
   override fun scheduleBackgroundTasks() {
      workManager.enqueueUniquePeriodicWork(
         WORK_NAME_UPDATER,
         ExistingPeriodicWorkPolicy.KEEP,
         PeriodicWorkRequestBuilder<AppUpdateFinderWorker>(12.hours.toJavaDuration()).addTag(WORK_NAME_UPDATER).build()
      )
   }
}

private const val WORK_NAME_UPDATER = "update_worker"
