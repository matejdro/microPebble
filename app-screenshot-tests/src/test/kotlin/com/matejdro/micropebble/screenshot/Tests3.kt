package com.matejdro.micropebble.screenshot

import com.google.testing.junit.testparameterinjector.TestParameter
import org.junit.Test

class Tests3 : TestsBase() {
   @Test
   public override fun test(
      @TestParameter(valuesProvider = PreviewProvider::class)
      @SplitIndex(2)
      testKey: TestKey,
   ) {
      super.test(testKey)
   }
}
