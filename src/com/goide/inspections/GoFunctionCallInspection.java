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
import com.goide.psi.impl.GoPsiImplUtil;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GoFunctionCallInspection extends GoInspectionBase {
  @NotNull
  @Override
  protected GoVisitor buildGoVisitor(@NotNull final ProblemsHolder holder, @NotNull LocalInspectionToolSession session) {
    return new GoVisitor() {
      @Override
      public void visitCallExpr(@NotNull GoCallExpr o) {
        super.visitCallExpr(o);
        PsiElement resolve = GoPsiImplUtil.resolveCallRaw(o);
        GoExpression expression = o.getExpression();
        if (resolve == null || !(expression instanceof GoReferenceExpression)) return;

        List<GoExpression> list = o.getArgumentList().getExpressionList();
        int actualSize = list.size();
        if (resolve instanceof GoTypeSpec && actualSize != 1) {
          String message = String.format("%sto conversion to %s: %s.", actualSize == 0 ? "Missing argument " : "Too many arguments ",
                                         expression.getText(), o.getText());
          holder.registerProblem(o, message);
          return;
        }

        GoFunctionType functionType = GoPsiImplUtil.findFunctionType(o.getExpression().getGoType(null));
        if (functionType == null) return;

        GoSignature signature = functionType.getSignature();
        if (signature == null) return;
        int expectedSize = 0;
        GoParameters parameters = signature.getParameters();
        for (GoParameterDeclaration declaration : parameters.getParameterDeclarationList()) {
          if (declaration.isVariadic() && actualSize >= expectedSize) return;
          int size = declaration.getParamDefinitionList().size();
          expectedSize += size == 0 ? 1 : size;
        }

        if (actualSize == 1) {
          GoExpression first = ContainerUtil.getFirstItem(list);
          GoSignatureOwner firstResolve = GoPsiImplUtil.resolveCall(first);
          if (firstResolve != null) {
            actualSize = GoInspectionUtil.getFunctionResultCount(firstResolve);
          }
        }

        boolean isMethodExpr = false;
        if (resolve instanceof GoVarDefinition) {
          GoExpression value = ((GoVarDefinition)resolve).getValue();
          if (value instanceof GoSelectorExpr) {
            List<GoExpression> expressionList = ((GoSelectorExpr)value).getExpressionList();
            if (expressionList.size() == 2) {
              GoExpression subExpression1 = expressionList.get(0);
              GoExpression subExpression2 = expressionList.get(1);
              if (subExpression1 instanceof GoParenthesesExpr &&
                  subExpression2 instanceof GoReferenceExpression &&
                  ((GoReferenceExpression)subExpression2).getReference().resolve() instanceof GoMethodDeclaration) {
                value = ((GoParenthesesExpr)subExpression1).getExpression();
              }
            }
          }
          if (value instanceof GoReferenceExpression) {
            GoReferenceExpression qualifier = (GoReferenceExpression)value;
            PsiElement resolved = qualifier.getReference().resolve();
            isMethodExpr = resolved instanceof GoMethodDeclaration ||
                           resolved instanceof GoTypeSpec;
          }
        }
        else {
          GoReferenceExpression qualifier = ((GoReferenceExpression)expression).getQualifier();
          isMethodExpr = qualifier != null && qualifier.getReference().resolve() instanceof GoTypeSpec;
        }

        if (isMethodExpr) actualSize -= 1;

        if (actualSize == expectedSize) return;

        String tail = " arguments in call to " + expression.getText();
        holder.registerProblem(o.getArgumentList(), (actualSize > expectedSize ? "too many" : "not enough") + tail);
      }
    };
  }
}
