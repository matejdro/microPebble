package com.matejdro.micropebble.webservices.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.matejdro.micropebble.webservices.api.WebservicesToken
import com.matejdro.micropebble.webservices.data.serializers.AuthenticationTokenSerializer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dispatch.core.IOCoroutineScope
import kotlin.uuid.Uuid

@ContributesTo(AppScope::class)
interface AuthenticationDataProviders {
   @Provides
   @SingleIn(AppScope::class)
   fun provideAuthTokenStore(context: Context, ioCoroutineScope: IOCoroutineScope): DataStore<Map<Uuid, WebservicesToken>> {
      return DataStoreFactory.create(scope = ioCoroutineScope, serializer = AuthenticationTokenSerializer) {
         context.dataStoreFile("webservicesAuth.json")
      }
   }
}
