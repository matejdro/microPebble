package com.matejdro.micropebble.common.test.datastore

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Data store that stores its data in memory. Used for testing.
 */
class InMemoryDataStore<T>(defaultValue: T) : DataStore<T> {
   override val data = MutableStateFlow(defaultValue)

   override suspend fun updateData(transform: suspend (t: T) -> T): T {
      data.update {
         transform(it)
      }

      return data.value
   }
}
