package com.matejdro.micropebble.appstore.data.serializers

import android.util.Log
import androidx.datastore.core.Serializer
import com.matejdro.micropebble.appstore.api.AppInstallSource
import dispatch.core.withIO
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import kotlin.uuid.Uuid

internal object AppInstallSourcesSerializer : Serializer<Map<Uuid, AppInstallSource>> {
   private val json = Json { ignoreUnknownKeys = true }
   override val defaultValue = emptyMap<Uuid, AppInstallSource>()

   override suspend fun readFrom(input: InputStream) =
      try {
         json.decodeFromString<Map<Uuid, AppInstallSource>>(input.readBytes().decodeToString())
      } catch (e: SerializationException) {
         Log.e("AppInstallSourcesSerializer.readFrom", "Failed to load appstore sources", e)
         defaultValue
      }

   override suspend fun writeTo(t: Map<Uuid, AppInstallSource>, output: OutputStream) = withIO {
      output.write(json.encodeToString(t).encodeToByteArray())
   }
}
