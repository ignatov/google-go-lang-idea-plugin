/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Florin Patan
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
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.goide.psi.impl.GoPsiImplUtil.*;

public class GoAssignmentCountMismatchInspection extends GoInspectionBase {

  @NotNull
  @Override
  protected GoVisitor buildGoVisitor(@NotNull final ProblemsHolder holder, @NotNull LocalInspectionToolSession session) {
    return new GoVisitor() {
      @Override
      public void visitVarSpec(@NotNull GoVarSpec o) {
        super.visitVarSpec(o);
        if (o instanceof GoRecvStatement) return;
        check(o, holder);
      }

      @Override
      public void visitRecvStatement(@NotNull GoRecvStatement o) {
        super.visitRecvStatement(o);
        check(o, holder);
      }

      @Override
      public void visitAssignmentStatement(@NotNull GoAssignmentStatement o) {
        super.visitAssignmentStatement(o);
        check(o, holder);
      }

      @Override
      public void visitShortVarDeclaration(@NotNull GoShortVarDeclaration o) {
        super.visitShortVarDeclaration(o);
        check(o, holder);
      }

      @Override
      public void visitAssignOp(@NotNull GoAssignOp o) {
        super.visitAssignOp(o);
        check(o, holder);
      }
    };
  }

  protected void check(@NotNull GoRecvStatement recvStatement, @NotNull ProblemsHolder holder) {
    boolean isValid = true;
    if ((recvStatement.getVarDefinitionList().size() == 0 &&
         recvStatement.getExpressionList().size() > 3) ||
        (recvStatement.getVarDefinitionList().size() > 2 &&
         recvStatement.getExpressionList().size() == 1)) {
      isValid = false;
    }

    if (isValid) return;

    channelReceiveError(recvStatement, holder);
  }

  protected void check(@NotNull GoVarSpec varSpec, @NotNull ProblemsHolder holder) {
    if (varSpec.getAssign() == null) return;

    int leftSideNumber = varSpec.getVarDefinitionList().size();
    List<GoExpression> rightSide = varSpec.getExpressionList();

    checkAssignment(varSpec, leftSideNumber, rightSide, holder);
  }

  protected void check(@NotNull GoAssignmentStatement goAssignmentStatement, @NotNull ProblemsHolder holder) {
    int leftSideNumber = goAssignmentStatement.getLeftHandExprList().getExpressionList().size();
    List<GoExpression> rightSide = goAssignmentStatement.getExpressionList();
    checkAssignment(goAssignmentStatement, leftSideNumber, rightSide, holder);
  }

  protected void check(@NotNull GoShortVarDeclaration goShortVarDeclaration, @NotNull ProblemsHolder holder) {
    int leftSideNumber = goShortVarDeclaration.getVarDefinitionList().size();
    List<GoExpression> rightSide = goShortVarDeclaration.getExpressionList();
    checkAssignment(goShortVarDeclaration, leftSideNumber, rightSide, holder);
  }

  private static void check(@NotNull GoAssignOp o, @NotNull ProblemsHolder holder) {
    if (o.getAssign() != null) return;

    if (((GoAssignmentStatement)o.getParent()).getLeftHandExprList().getExpressionList().size() > 1) {
      holder.registerProblem(o, "Syntax error: unexpected op=, expecting := or = or comma", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
    }
  }

  private static void checkAssignment(@NotNull PsiElement element,
                                      int leftSideNumber,
                                      @NotNull List<GoExpression> rightSide,
                                      @NotNull ProblemsHolder holder) {
    if (hasUnresolvedReferences(rightSide) ||
        hasUnresolvedCalls(rightSide) ||
        hasCGOCall(rightSide)) {
      return;
    }

    int rightSideNumber = countRightSide(rightSide);

    if (isTypeAssertion(rightSide) ||
        isMapRead(rightSide) ||
        isChannelReceiver(rightSide)) {
      if (leftSideNumber == 1 ||
          leftSideNumber == 2) {
        return;
      }
      else if (isChannelReceiver(rightSide)) {
        channelReceiveError(element, holder);
        return;
      }
    }

    if (leftSideNumber != rightSideNumber) {
      holder.registerProblem(element, getErrorText(leftSideNumber, rightSideNumber), ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
    }

    checkFunctionsCalls(leftSideNumber, rightSide, holder);
  }

  private static void checkFunctionsCalls(int leftSideNumber, @NotNull List<GoExpression> expressionList, @NotNull ProblemsHolder holder) {
    if (expressionList.size() == 1) {
      GoExpression callExpr = expressionList.get(0);
      if (!(callExpr instanceof GoCallExpr) ||
          ((GoCallExpr)callExpr).getExpression() instanceof GoCallExpr) {
        return;
      }

      int funcArity = getArity((GoCallExpr)callExpr);
      if (leftSideNumber != funcArity) {
        checkFuncCall(callExpr, funcArity, holder);
      }

      return;
    }

    int returnCount;
    for (GoExpression expression : expressionList) {
      if (expression instanceof GoCallExpr) {
        returnCount = getArity((GoCallExpr)expression);
        if (returnCount != 1) {
          checkFuncCall(expression, returnCount, holder);
        }
      }
    }
  }

  private static void checkFuncCall(@NotNull GoExpression expression, int returnCount, @NotNull ProblemsHolder holder) {
    String text;
    if (returnCount == 0) {
      text = expression.getText() + " doesn't return a value";
    }
    else {
      String funcText = expression.getText();
      if (expression instanceof GoCallExpr) {
        if (((GoCallExpr)expression).getExpression() instanceof GoFunctionLit) {
          GoSignature signature = ((GoFunctionLit)((GoCallExpr)expression).getExpression()).getSignature();
          if (signature != null) {
            funcText = "func" + signature.getText();
          }
        }
      }
      text = "Multiple-value " + funcText + " in single-value context";
    }
    holder.registerProblem(expression, text, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
  }

  private static int countRightSide(@NotNull List<GoExpression> rightSide) {
    if (rightSide.size() == 1) {
      return getArity(rightSide.get(0));
    }

    if (hasAtLeastOneCall(rightSide)) {
      return rightSide.size();
    }

    return getArity(rightSide);
  }

  private static void channelReceiveError(@NotNull PsiElement recvStatement, @NotNull ProblemsHolder holder) {
    String text = "Assignment count mismatch: channel receiving should be: <-channel OR value = <-channel OR value, isClosed = <-channel";
    holder.registerProblem(recvStatement, text, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
  }

  @NotNull
  private static String getErrorText(int leftSideNumber, int rightSideNumber) {
    return "Assignment count mismatch: " + rightSideNumber + " element(s) assigned to " + leftSideNumber + " element(s)";
  }
}
