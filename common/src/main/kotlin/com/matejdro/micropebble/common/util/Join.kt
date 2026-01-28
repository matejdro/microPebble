package com.matejdro.micropebble.common.util

import java.io.File.separator

/**
 * Join `this` to [ends] while placing [separator] between each section, as long as one doesn't already exist.
 */
fun String.joinWithOneSeparator(vararg ends: String, separator: Char) = sequenceOf(this, *ends).reduce { accumulator, string ->
   accumulator.trimEnd(separator) + separator.toString() + string.trimStart(separator)
}

/**
 * Join `this` to [ends] while placing separator `'/'` between each section, as long as one doesn't already exist.
 */
fun String.joinUrls(vararg ends: String) = joinWithOneSeparator(*ends, separator = '/')
