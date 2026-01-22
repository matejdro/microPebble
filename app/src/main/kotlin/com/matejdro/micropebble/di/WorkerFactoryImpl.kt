package com.matejdro.micropebble.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.reflect.KClass

@Inject
@ContributesBinding(AppScope::class)
class WorkerFactoryImpl(
   private val factories: Map<KClass<out ListenableWorker>, (Context, WorkerParameters) -> ListenableWorker>,
) : WorkerFactory() {
   override fun createWorker(
      appContext: Context,
      workerClassName: String,
      workerParameters: WorkerParameters,
   ) = factories[Class.forName(workerClassName).kotlin]?.invoke(appContext, workerParameters)
}
