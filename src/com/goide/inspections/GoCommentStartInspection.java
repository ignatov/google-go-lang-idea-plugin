/*
 * Copyright 2013-2016 Sergey Ignatov, Alexander Zolotov, Florin Patan
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

import com.goide.GoConstants;
import com.goide.GoDocumentationProvider;
import com.goide.psi.*;
import com.goide.runconfig.testing.GoTestFinder;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * golint inspection from:
 * https://github.com/golang/lint/blob/32a87160691b3c96046c0c678fe57c5bef761456/lint.go#L744
 */
public class GoCommentStartInspection extends GoInspectionBase {
  @NotNull
  @Override
  protected GoVisitor buildGoVisitor(@NotNull final ProblemsHolder holder, @NotNull LocalInspectionToolSession session) {
    return new GoVisitor() {

      @Override
      public void visitPackageClause(@NotNull GoPackageClause o) {
        super.visitPackageClause(o);
        String packageName = o.getName();
        if (GoConstants.MAIN.equals(packageName)) return;

        List<PsiComment> comments = GoDocumentationProvider.getCommentsForElement(o);
        String commentText = GoDocumentationProvider.getCommentText(comments, false);
        if (!comments.isEmpty() && !commentText.isEmpty() && !commentText.startsWith("Package " + packageName)) {
          registerProblem(comments, "Package comment should be of the form 'Package " + packageName + " ...'", holder);
        }
      }

      @Override
      public void visitCompositeElement(@NotNull GoCompositeElement o) {
        super.visitCompositeElement(o);
        if (!shouldInspect(o)) return;

        boolean isBlockComment = false;
        int elementsInBlock = 0;
        PsiElement specElement = o;
        if (o instanceof GoVarDefinition || o instanceof GoConstDefinition) {
          specElement = o.getParent();
        }

        List<PsiComment> comments = GoDocumentationProvider.getCommentsInner(specElement);
        if (comments.size() == 0) {
          comments = GoDocumentationProvider.getCommentsForElement(o);
          isBlockComment = true;
          elementsInBlock = getElementsInBlockCount(specElement);
        }
        String commentText = GoDocumentationProvider.getCommentText(comments, false);
        String elementName = ((GoNamedElement)o).getName();
        if (elementName == null) return;

        if (comments.isEmpty() || commentText.isEmpty()) {
          PsiElement identifier = ((GoNamedElement)o).getIdentifier();
          if (identifier != null) {
            holder.registerProblem(identifier, String.format("'%s' should have a comment or not be exported", elementName), ProblemHighlightType.WEAK_WARNING);
          }
        }
        else if (!isCorrectComment(commentText, elementName, isBlockComment, elementsInBlock)) {
          registerProblem(comments, "Comment should start with '" + elementName + "'", holder);
        }
        // +1 stands for Element_Name<space>
        else if (commentText.length() <= elementName.length() + 1) {
          registerProblem(comments, "Comment should be meaningful or it should be removed", holder);
        }
      }
    };
  }

  private static int getElementsInBlockCount(PsiElement o) {
    GoTopLevelDeclaration parent = PsiTreeUtil.getParentOfType(o, GoVarDeclaration.class, GoConstDeclaration.class, GoTypeDeclaration.class);
    if (parent == null) return 0;
    
    if (parent instanceof GoVarDeclaration) {
      return ((GoVarDeclaration)parent).getVarSpecList().size();
    }
    else if (parent instanceof GoConstDeclaration) {
      return ((GoConstDeclaration) parent).getConstSpecList().size();
    }
    else if (parent instanceof GoTypeDeclaration) {
      return ((GoTypeDeclaration) parent).getTypeSpecList().size();
    }
    return 0;
  }

  private static boolean shouldInspect(@NotNull GoCompositeElement o) {
    return !GoTestFinder.isTestFile(o.getContainingFile()) &&
           (o instanceof GoNamedElement) &&
           ((GoNamedElement)o).isPublic() &&
           PsiTreeUtil.getParentOfType(o, GoFile.class, GoBlock.class) instanceof GoFile;
  }

  private static void registerProblem(@NotNull List<PsiComment> comments, @NotNull String description, @NotNull ProblemsHolder holder) {
    for (PsiComment comment : comments) {
      holder.registerProblem(comment, description, ProblemHighlightType.WEAK_WARNING);
    }
  }

  private static boolean isCorrectComment(@NotNull String commentText,
                                          @NotNull String elementName,
                                          boolean isBlockComment,
                                          int elementsInBlock) {
    return (isBlockComment && elementsInBlock > 1) ||
           (commentText.startsWith(elementName)
            || commentText.startsWith("A " + elementName)
            || commentText.startsWith("An " + elementName)
            || commentText.startsWith("The " + elementName));
  }
}
