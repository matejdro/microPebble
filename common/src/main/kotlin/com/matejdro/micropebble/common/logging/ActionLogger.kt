package com.matejdro.micropebble.common.logging

/**
 * Interface that logs all user's actions and sends them to an online logger (for example Firebase Crashlytics)
 * to aid with crash diagnosing
 */
fun interface ActionLogger {
   fun logAction(text: () -> String)
}
