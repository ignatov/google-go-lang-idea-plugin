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

package com.goide.runconfig;

import com.goide.dlv.DlvDebugProcess;
import com.goide.runconfig.application.GoApplicationRunningState;
import com.goide.runconfig.file.GoRunFileRunningState;
import com.goide.runconfig.testing.GoTestRunningState;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunProfileStarter;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.AsyncGenericProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;

public class GoRunner extends AsyncGenericProgramRunner {
  private static final String ID = "GoRunner";

  @NotNull
  @Override
  public String getRunnerId() {
    return ID;
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return (DefaultRunExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof GoRunConfigurationBase)
           || DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && !DlvDebugProcess.IS_DLV_DISABLED;
  }

  @NotNull
  @Override
  protected Promise<RunProfileStarter> prepare(@NotNull ExecutionEnvironment environment, @NotNull final RunProfileState state)
    throws ExecutionException {
    if (state instanceof GoTestRunningState) {
      return new GoTestRunningRunner().prepare(environment, state);
    }
    else if (state instanceof GoRunFileRunningState) {
      return new GoRunFileRunner().prepare(environment, state);
    }
    else if (state instanceof GoApplicationRunningState) {
      return new GoBuildingRunner().prepare(environment, state);
    }
    return new AsyncPromise<RunProfileStarter>();
  }

  @Override
  protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env) throws ExecutionException {
    if (state instanceof GoTestRunningState) {
      if (((GoTestRunningState)state).isDebug()) {
        return new GoDlvRunner(this).execute(state, env);
      }
    }
    else if (state instanceof GoRunFileRunningState) {
      if (((GoRunFileRunningState)state).isDebug()) {
        return new GoDlvRunner(this).execute(state, env);
      }
    }
    return super.doExecute(state, env);
  }
}