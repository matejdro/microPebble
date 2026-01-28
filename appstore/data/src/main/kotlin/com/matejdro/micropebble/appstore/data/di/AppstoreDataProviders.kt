package com.matejdro.micropebble.appstore.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.matejdro.micropebble.appstore.api.AppInstallSource
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.data.serializers.AppInstallSourcesSerializer
import com.matejdro.micropebble.appstore.data.serializers.AppstoreSourcesSerializer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dispatch.core.IOCoroutineScope
import kotlin.uuid.Uuid

@ContributesTo(AppScope::class)
interface AppstoreDataProviders {
   @Provides
   @SingleIn(AppScope::class)
   fun provideAppInstallStore(context: Context, ioCoroutineScope: IOCoroutineScope): DataStore<Map<Uuid, AppInstallSource>> {
      return DataStoreFactory.create(scope = ioCoroutineScope, serializer = AppInstallSourcesSerializer) {
         context.dataStoreFile("appInstallSources.json")
      }
   }

   @Provides
   @SingleIn(AppScope::class)
   fun provideAppstoreSourcesStore(context: Context, ioCoroutineScope: IOCoroutineScope): DataStore<List<AppstoreSource>> {
      return DataStoreFactory.create(scope = ioCoroutineScope, serializer = AppstoreSourcesSerializer) {
         context.dataStoreFile("appstoreSources.json")
      }
   }
}
