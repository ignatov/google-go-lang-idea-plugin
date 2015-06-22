/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Mihai Toader, Florin Patan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goide.inspections;

import com.goide.psi.*;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class GoPrintfInspection extends GoInspectionBase {
  private static List<String> myPrintfFunctionsList = Arrays.asList(
    "Errorf",
    "Printf",
    "Scanf",
    "Sprintf",
    "Sscanf",
    "Fatalf",
    "Panicf"
  );
  private static List<String> myFPrintfFunctionsList = Arrays.asList(
    "Fprint",
    "Fprintf",
    "Fscanf"
  );

  @NotNull
  @Override
  protected GoVisitor buildGoVisitor(@NotNull final ProblemsHolder holder, @NotNull LocalInspectionToolSession session) {
    return new GoVisitor() {
      @Override
      public void visitCallExpr(@NotNull GoCallExpr o) {
        super.visitCallExpr(o);
        if (o.getExpression() instanceof GoReferenceExpression) {
          check(o, holder);
        }
      }
    };
  }

  protected void check(@NotNull GoCallExpr callExpr, @NotNull ProblemsHolder holder) {
    GoReferenceExpression expression = (GoReferenceExpression)callExpr.getExpression();

    PsiReference reference = expression.getReference();
    PsiElement resolve = reference.resolve();
    if (resolve == null) return;

    if (!(resolve instanceof GoFunctionDeclaration)) return;

    String functionName = ((GoFunctionDeclaration)resolve).getName();

    if (functionName == null) return;

    if (myPrintfFunctionsList.contains(functionName)) {
      checkParameters(holder, callExpr, (GoFunctionDeclaration)resolve, 1);
    }
    else if (myFPrintfFunctionsList.contains(functionName)) {
      checkParameters(holder, callExpr, (GoFunctionDeclaration)resolve, 2);
    }
  }

  protected static void checkParameters(@NotNull ProblemsHolder holder,
                                        @NotNull GoCallExpr callExpr,
                                        @NotNull GoFunctionDeclaration function,
                                        int startPosition) {
    if (function.getSignature() == null) return;

    int argumentCount = function.getSignature().getParameters().getParameterDeclarationList().size() - startPosition;
    if (argumentCount < 0) {
      holder.registerProblem(callExpr, "Too few arguments in function call", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
      return;
    }
    GoExpression format = callExpr.getArgumentList().getExpressionList().get(startPosition);
    if (format instanceof GoStringLiteral) {
      if (((GoStringLiteral)format).getRawString() != null) {
        checkFormat(((GoStringLiteral)format).getRawString().getText());
      } else if (((GoStringLiteral)format).getString() != null) {
        checkFormat(((GoStringLiteral)format).getString().getText());
      }
    }
    //holder.registerProblem(parameter, errorText(name), ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
  }

  private static void checkFormat(String fmt, int argumentCount) {
    int placeholdersCount = 0;
    for (int i = 0; i < fmt.length(); i++) {
      char c = fmt.charAt(i);
      if (c != '%') {
        continue;
      }

      if (i == fmt.length() - 1) {
        continue;
      }

      while (++i < fmt.length()) {
        char verb = fmt.charAt(i);
        if (Character.isDigit(verb) || verb == '+' || verb == '-' || verb == ' ' || verb == '#' ||
            verb == '.') {
          // It's not a verb, it's a flag, ignore it.
          continue;
        }

        if (verb == '*') {
          placeholdersCount++;
          continue;
        }

        ctx.endOffset = i;

        if (ctx.isScanning && INVALID_VERBS_IN_SCANNING.indexOf(verb) != -1) {
          placeholdersCount++;
        } else if (verb == '%') {
          // A literal percent sign, consumes no value
        } else if (BOOL_VERBS.indexOf(verb) != -1) {
          placeholdersCount++;
        } else if (GENERAL_VERBS.indexOf(verb) != -1) {
          placeholdersCount++;
        } else if (INT_VERBS.indexOf(verb) != -1) {
          placeholdersCount++;
        } else if (FLOAT_VERBS.indexOf(verb) != -1) {
          placeholdersCount++;
        } else if (STR_VERBS.indexOf(verb) != -1) {
          placeholdersCount++;
        } else if (PTR_VERBS.indexOf(verb) != -1) {
          placeholdersCount++;
        } else {
          //ctx.unknownFlag();
        }
        break;
      }
    }
  }

  private static String errorText(@NotNull String name) {
    return "Duplicate argument " + "'" + name + "'";
  }
}
