package com.matejdro.micropebble.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.isPublic

class UseActionLoggerInViewModels(ruleSetConfig: Config) :
   Rule(ruleSetConfig, "Every public method in the ViewModel should start with the action logger call") {
   private var inViewModel: Boolean = false
   private var checkForNextCallFunction: KtNamedFunction? = null

   override fun visitClass(klass: KtClass) {
      inViewModel = !klass.isInterface() &&
         klass.name?.startsWith("Fake") != true &&
         (klass.name?.endsWith("ViewModel") == true || klass.name?.endsWith("ViewModelImpl") == true)
      super.visitClass(klass)
   }

   override fun visitNamedFunction(function: KtNamedFunction) {
      val shouldCheck = inViewModel && function.isPublic
      checkForNextCallFunction = if (shouldCheck) {
         function
      } else {
         null
      }
      super.visitNamedFunction(function)

      if (checkForNextCallFunction != null) {
         report(function)
      }
   }

   override fun visitCallExpression(expression: KtCallExpression) {
      super.visitCallExpression(expression)

      val checkForNextCallFunction = checkForNextCallFunction
      if (checkForNextCallFunction != null) {
         val calledFunction = expression.calleeExpression?.text
         if (calledFunction?.contains("launch") == true) {
            // Ignore coroutine launches
         } else if (calledFunction == "logAction") {
            this.checkForNextCallFunction = null
         } else {
            this.checkForNextCallFunction = null

            report(
               Finding(
                  Entity.atName(checkForNextCallFunction),
                  "Public ViewModel function ${checkForNextCallFunction.name}() does not start with a logAction() call"
               )
            )
         }
      }
   }

   private fun report(function: KtNamedFunction) {
      report(
         Finding(
            Entity.atName(function),
            "Function ${function.name} does not start with a logAction() call"
         )
      )
   }
}
