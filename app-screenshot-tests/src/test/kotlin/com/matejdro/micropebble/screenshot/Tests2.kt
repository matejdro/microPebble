package com.matejdro.micropebble.screenshot

import com.google.testing.junit.testparameterinjector.TestParameter
import org.junit.Test

class Tests2 : TestsBase() {
   @Test
   public override fun test(
      @TestParameter(valuesProvider = PreviewProvider::class)
      @SplitIndex(1)
      testKey: TestKey,
   ) {
      super.test(testKey)
   }
}
