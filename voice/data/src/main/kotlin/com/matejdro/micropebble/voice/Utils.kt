package com.matejdro.micropebble.voice

import io.rebble.libpebblecommon.voice.TranscriptionResult
import io.rebble.libpebblecommon.voice.TranscriptionWord

internal fun staticSuccess(string: String): TranscriptionResult.Success = TranscriptionResult.Success(
   string
      .split(" ")
      .map { TranscriptionWord(it, 1f) }
)
