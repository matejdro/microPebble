package com.matejdro.micropebble.appstore.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.matejdro.micropebble.appstore.api.updater.AppUpdateFinder
import com.matejdro.micropebble.appstore.data.di.WorkerKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.IntoMap
import dev.zacsweers.metro.binding

@AssistedInject
@IntoMap
class AppUpdateFinderWorker(
   @Assisted
   context: Context,
   @Assisted
   params: WorkerParameters,
   private val appUpdateFinder: AppUpdateFinder,
) : CoroutineWorker(context, params) {
   override suspend fun doWork() =
      if (appUpdateFinder.findAndNotifyUpdates()) {
         Result.success()
      } else {
         Result.failure()
      }

   @AssistedFactory
   @IntoMap
   @ContributesIntoMap(AppScope::class, binding<(Context, WorkerParameters) -> ListenableWorker>())
   @WorkerKey(AppUpdateFinderWorker::class)
   interface Factory : (Context, WorkerParameters) -> ListenableWorker {
      fun create(context: Context, params: WorkerParameters): AppUpdateFinderWorker

      override fun invoke(p1: Context, p2: WorkerParameters) = create(p1, p2)
   }
}
