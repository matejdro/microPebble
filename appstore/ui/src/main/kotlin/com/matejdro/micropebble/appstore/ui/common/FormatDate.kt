package com.matejdro.micropebble.appstore.ui.common

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Instant
import kotlin.time.toJavaInstant

fun Instant.formatDate(): String =
   this.toJavaInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
