package com.matejdro.micropebble.screenshot

import app.cash.paparazzi.HtmlReportWriter
import app.cash.paparazzi.Snapshot
import app.cash.paparazzi.SnapshotHandler
import app.cash.paparazzi.SnapshotVerifier
import app.cash.paparazzi.detectMaxPercentDifferenceDefault

class RenamingSnapshotHandler(private val original: SnapshotHandler) : SnapshotHandler by original {
   override fun newFrameHandler(
      snapshot: Snapshot,
      frameCount: Int,
      fps: Int,
   ): SnapshotHandler.FrameHandler {
      val renamedSnapshot = snapshot.copy(
         name = null,
         testName = snapshot.testName.copy(
            packageName = "",
            className = "",
            methodName = requireNotNull(snapshot.name) {
               "Every call to paparazzi.snapshot() must provide a name, $snapshot did not"
            }
         )
      )

      return original.newFrameHandler(renamedSnapshot, frameCount, fps)
   }
}

fun determinedHandlerWithRenaming(maxPercentDifference: Double = detectMaxPercentDifferenceDefault()): SnapshotHandler {
   val isVerifying: Boolean =
      System.getProperty("paparazzi.test.verify")?.toBoolean() == true

   val originalHandler =
      if (isVerifying) {
         SnapshotVerifier(maxPercentDifference)
      } else {
         HtmlReportWriter(maxPercentDifference = maxPercentDifference)
      }

   return RenamingSnapshotHandler(originalHandler)
}
