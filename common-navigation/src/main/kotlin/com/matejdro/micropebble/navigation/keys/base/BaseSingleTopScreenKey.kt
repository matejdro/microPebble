package com.matejdro.micropebble.navigation.keys.base

import androidx.activity.BackEventCompat
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.TransformOrigin
import si.inova.kotlinova.navigation.screenkeys.SingleTopKey
import si.inova.kotlinova.navigation.simplestack.StateChangeResult

abstract class BaseSingleTopScreenKey : SingleTopKey() {
   @Suppress("MagicNumber") // Magic numbers are the whole point of this function
   override fun backAnimation(scope: AnimatedContentTransitionScope<StateChangeResult>): ContentTransform {
      // Animation spec that attempts to mimic Google's back preview guidelines as close as possible
      // https://developer.android.com/design/ui/mobile/guides/patterns/predictive-back#back-preview

      // See https://issuetracker.google.com/issues/347047848 for feature request to be able
      // to implement the guidelines fully

      val scaleTransformOrigin = when (scope.targetState.backSwipeEdge) {
         BackEventCompat.EDGE_LEFT -> TransformOrigin(1f, 0.5f)
         BackEventCompat.EDGE_RIGHT -> TransformOrigin(0f, 0.5f)
         else -> TransformOrigin.Center
      }

      return (fadeIn() + scaleIn(initialScale = 1.1f, transformOrigin = scaleTransformOrigin)) togetherWith
         (fadeOut() + scaleOut(targetScale = 0.9f, transformOrigin = scaleTransformOrigin))
   }
}
