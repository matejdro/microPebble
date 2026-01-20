package com.matejdro.micropebble.common.util

private val regex = Regex("(\\d+)\\.(\\d+)(-.*)?")

data class VersionInfo(
   val majorVersion: Int,
   val minorVersion: Int,
   val extra: String? = null,
)

operator fun VersionInfo.compareTo(other: VersionInfo) = when {
   majorVersion == other.majorVersion && minorVersion == other.minorVersion -> 0
   majorVersion > other.majorVersion -> 1
   majorVersion < other.majorVersion -> -1
   minorVersion > other.minorVersion -> 1
   else -> -1
}

fun VersionInfo.toVersionString() = buildString {
   append(majorVersion)
   append(".")
   append(minorVersion)
   if (extra != null) {
      append("-")
      append(extra)
   }
}

fun parseVersionString(version: String) = regex.matchEntire(version)?.groupValues?.let { groups ->
   VersionInfo(
      majorVersion = groups[1].toInt(),
      minorVersion = groups[2].toInt(),
      extra = groups[3].ifBlank { null }
   )
}
