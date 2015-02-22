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

package com.goide.highlighting;

import com.goide.psi.*;
import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector;
import com.intellij.psi.*;

public class GoReadWriteAccessDetector extends ReadWriteAccessDetector {

  @Override
  public boolean isReadWriteAccessible(PsiElement element) {
    if (element instanceof GoVarDefinition ||
        element instanceof GoConstDefinition) {
      return true;
    }

    return false;
  }

  @Override
  public boolean isDeclarationWriteAccess(PsiElement element) {
    if (element instanceof GoVarDefinition) {
      return true;
    }
    return false;
  }

  @Override
  public Access getReferenceAccess(PsiElement referencedElement, PsiReference reference) {
    if (referencedElement instanceof GoVarDefinition) {
      return Access.Write;
    }
    return Access.Read;
  }

  @Override
  public Access getExpressionAccess(PsiElement expression) {
    return Access.Write;
  }
}
