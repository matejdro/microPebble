package com.matejdro.micropebble.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dev.zacsweers.metro.Inject
import kotlin.reflect.KClass

@Inject
class WorkerFactoryImpl(
   private val factories: Map<KClass<out ListenableWorker>, (WorkerParameters) -> ListenableWorker>,
) : WorkerFactory() {
   override fun createWorker(
      appContext: Context,
      workerClassName: String,
      workerParameters: WorkerParameters,
   ) = factories[Class.forName(workerClassName).kotlin]?.invoke(workerParameters)
}
