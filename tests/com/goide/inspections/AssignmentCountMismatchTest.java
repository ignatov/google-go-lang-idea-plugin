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

import com.goide.GoCodeInsightFixtureTestCase;
import com.intellij.testFramework.LightProjectDescriptor;

public class AssignmentCountMismatchTest extends GoCodeInsightFixtureTestCase {
  public void setUp() throws Exception {
    super.setUp();
    setUpProjectSdk();
    myFixture.enableInspections(
      GoAssignmentCountMismatchInspection.class
    );
  }

  private void doTest() {
    myFixture.testHighlighting(true, false, false, getTestName(true) + ".go");
  }

  @Override
  protected String getBasePath() {
    return "inspections/assignment-count-mismatch";
  }

  @Override
  protected boolean isWriteActionRequired() {
    return false;
  }

  public void testGh1219()                      { doTest(); }
  public void testGh1842()                      { doTest(); }
  public void testGh1906()                      { doTest(); }
  public void testResolve()                     { doTest(); }
  public void testUnresolved()                  { doTest(); }
  public void testResolveMyTypeList()           { doTest(); }
  public void testResolveTypeList()             { doTest(); }
  public void testResolveType()                 { doTest(); }
  public void testResolveMap()                  { doTest(); }
  public void testResolveFuncTypeParam()        { doTest(); }
  public void testResolveInterface()            { doTest(); }
  public void testResolveChannelType()          { doTest(); }
  public void testResolveFieldDefinition()      { doTest(); }
  public void testResolveChannelReceiver()      { doTest(); }
  public void testResolveTypeResult()           { doTest(); }
  public void testResolveFunctionType()         { doTest(); }
  public void testResolveArrayOrSliceType()     { doTest(); }
  public void testResolveOverwriteBuiltin()     { doTest(); }
  public void testResolveReceiver()             { doTest(); }
  public void testResolveGoCallExprCallGoExpr() { doTest(); }
  public void testResolveVarValue()             { doTest(); }
  public void testResolveParType()              { doTest(); }
  public void testResolveGoMapType()            { doTest(); }
  public void testResolveTypeSwitchGuard()      { doTest(); }
  public void testResolveTypeAssertion()        { doTest(); }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return createMockProjectDescriptor();
  }
}
