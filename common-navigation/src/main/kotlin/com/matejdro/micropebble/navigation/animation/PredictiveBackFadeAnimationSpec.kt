package com.matejdro.micropebble.navigation.animation

import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.FloatAnimationSpec
import androidx.compose.animation.core.FloatTweenSpec
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorizedFiniteAnimationSpec
import androidx.compose.animation.core.VectorizedFloatAnimationSpec

internal class PredictiveBackFadeAnimationSpec(
   private val durationMillis: Int = AnimationConstants.DefaultDurationMillis,
   private val easing: Easing = FastOutSlowInEasing,
) : FiniteAnimationSpec<Float> {
   override fun <V : AnimationVector> vectorize(converter: TwoWayConverter<Float, V>): VectorizedFiniteAnimationSpec<V> {
      return VectorizedFloatAnimationSpec(PredictiveBackFadeFloatAnimationSpec(durationMillis, easing))
   }
}

private class PredictiveBackFadeFloatAnimationSpec(
   public val duration: Int = AnimationConstants.DefaultDurationMillis,
   private val easing: Easing = FastOutSlowInEasing,
) : FloatAnimationSpec {
   private val tweenSpec = FloatTweenSpec(duration, easing = easing)
   override fun getDurationNanos(
      initialValue: Float,
      targetValue: Float,
      initialVelocity: Float,
   ): Long {
      return tweenSpec.getDurationNanos(initialValue, targetValue, initialVelocity)
   }

   @Suppress("MagicNumber") // Commented
   override fun getValueFromNanos(
      playTimeNanos: Long,
      initialValue: Float,
      targetValue: Float,
      initialVelocity: Float,
   ): Float {
      // Animation spec that attempts to mimic Google's back preview guidelines as close as possible
      // https://developer.android.com/design/ui/mobile/guides/patterns/predictive-back#back-preview

      val position = tweenSpec.getValueFromNanos(
         playTimeNanos = playTimeNanos,
         initialValue = 0f,
         targetValue = 1f,
         initialVelocity = initialVelocity
      )
      return if (targetValue < 0.5f) {
         // We are in "Fade Out" animation. Complete the fade at 35%
         (1f - (position * 2.85f)).coerceAtLeast(0f)
      } else {
         // We are in "Fade In" animation. Start fading at 35%
         if (position < 0.35f) {
            0f
         } else {
            (position - 0.35f) * 1 / (1 - 0.35f)
         }
      }
   }

   override fun getVelocityFromNanos(
      playTimeNanos: Long,
      initialValue: Float,
      targetValue: Float,
      initialVelocity: Float,
   ): Float {
      return tweenSpec.getVelocityFromNanos(playTimeNanos, initialValue, targetValue, initialVelocity)
   }
}
