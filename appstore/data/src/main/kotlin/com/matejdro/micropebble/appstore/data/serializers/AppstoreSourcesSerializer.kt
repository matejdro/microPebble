package com.matejdro.micropebble.appstore.data.serializers

import android.util.Log
import androidx.datastore.core.Serializer
import com.matejdro.micropebble.appstore.api.AppstoreSource
import com.matejdro.micropebble.appstore.api.AppstoreSourceService
import dispatch.core.withIO
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

internal object AppstoreSourcesSerializer : Serializer<List<AppstoreSource>> {
   private val json = Json { ignoreUnknownKeys = true }
   override val defaultValue = AppstoreSourceService.defaultSources

   override suspend fun readFrom(input: InputStream) =
      try {
         json.decodeFromString<List<AppstoreSource>>(input.readBytes().decodeToString())
      } catch (e: SerializationException) {
         Log.e("AppstoreSourcesSerializer.readFrom", "Failed to load appstore sources", e)
         defaultValue
      }

   override suspend fun writeTo(t: List<AppstoreSource>, output: OutputStream) = withIO {
      output.write(json.encodeToString(t).encodeToByteArray())
   }
}
