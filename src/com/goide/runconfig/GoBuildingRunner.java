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

import com.goide.GoEnvironmentUtil;
import com.goide.dlv.DlvDebugProcess;
import com.goide.runconfig.application.GoApplicationRunningState;
import com.goide.runconfig.file.GoRunFileRunningState;
import com.goide.runconfig.testing.GoTestRunningState;
import com.goide.util.GoExecutor;
import com.goide.util.GoHistoryProcessListener;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.RunProfileStarter;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.AsyncGenericProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;

import java.io.File;
import java.io.IOException;

public class GoBuildingRunner extends AsyncGenericProgramRunner {
  private static final String ID = "GoBuildingRunner";

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
    FileDocumentManager.getInstance().saveAllDocuments();

    final AsyncPromise<RunProfileStarter> buildingPromise = new AsyncPromise<RunProfileStarter>();
    final GoHistoryProcessListener historyProcessListener = new GoHistoryProcessListener();

    String goToolCommand;
    File outputFile = null;
    String target = "";

    GoExecutor executor;
    if (state instanceof GoApplicationRunningState) {
      executor = ((GoApplicationRunningState)state).createCommonExecutor();
      outputFile = getOutputFile(environment, (GoApplicationRunningState)state);
      target = ((GoApplicationRunningState)state).getTarget();
      goToolCommand = "build";
    }
    else if (state instanceof GoRunFileRunningState) {
      executor = ((GoRunFileRunningState)state).createCommonExecutor();
      goToolCommand = "run";
    }
    else if (state instanceof GoTestRunningState) {
      executor = ((GoTestRunningState)state).createCommonExecutor();
      goToolCommand = "test";
    }
    else {
      throw new ExecutionException("Invalid running state");
    }

    executor
      .withParameters(goToolCommand)
      .withParameterString(((GoRunningState)state).getGoBuildParams());

    if (outputFile != null) executor.withParameters("-o", outputFile.getAbsolutePath());

    final File finalOutputFile = outputFile;
    executor
      .withParameters(((GoRunningState)state).isDebug() ? new String[]{"-gcflags", "-N -l"} : ArrayUtil.EMPTY_STRING_ARRAY)
      .withParameters(target)
      .showNotifications(true)
      .showOutputOnError()
      .disablePty()
      .withPresentableName("go " + goToolCommand)
      .withProcessListener(historyProcessListener);

    if (state instanceof GoApplicationRunningState) {
      executor.withProcessListener(new ProcessAdapter() {
        @Override
        public void processTerminated(ProcessEvent event) {
          super.processTerminated(event);
          if (event.getExitCode() == 0) {
            if (((GoRunningState)state).isDebug()) {
              buildingPromise
                .setResult(new GoDlvRunner(finalOutputFile.getAbsolutePath(), historyProcessListener, GoBuildingRunner.this));
            }
            else {
              buildingPromise.setResult(new MyRunStarter(finalOutputFile.getAbsolutePath(), historyProcessListener));
            }
          }
          else {
            buildingPromise.setResult(null);
            buildingPromise.setError(new ExecutionException(event.getText()));
          }
        }
      });
    }

    executor.executeWithProgress(false);
    return buildingPromise;
  }

  @NotNull
  private static File getOutputFile(@NotNull ExecutionEnvironment environment, @NotNull GoRunningState state)
    throws ExecutionException {
    final File outputFile;
    String outputDirectoryPath = state.getConfiguration().getOutputFilePath();
    RunnerAndConfigurationSettings settings = environment.getRunnerAndConfigurationSettings();
    String configurationName = settings != null ? settings.getName() : "application";
    if (StringUtil.isEmpty(outputDirectoryPath)) {
      try {
        outputFile = FileUtil.createTempFile(configurationName, "go", true);
      }
      catch (IOException e) {
        throw new ExecutionException("Cannot create temporary output file", e);
      }
    }
    else {
      File outputDirectory = new File(outputDirectoryPath);
      if (outputDirectory.isDirectory() || !outputDirectory.exists() && outputDirectory.mkdirs()) {
        outputFile = new File(outputDirectoryPath, GoEnvironmentUtil.getBinaryFileNameForPath(configurationName));
        try {
          if (!outputFile.exists() && !outputFile.createNewFile()) {
            throw new ExecutionException("Cannot create output file " + outputFile.getAbsolutePath());
          }
        }
        catch (IOException e) {
          throw new ExecutionException("Cannot create output file " + outputFile.getAbsolutePath());
        }
      }
      else {
        throw new ExecutionException("Cannot create output file in " + outputDirectory.getAbsolutePath());
      }
    }
    if (!prepareFile(outputFile)) {
      throw new ExecutionException("Cannot make temporary file executable " + outputFile.getAbsolutePath());
    }
    return outputFile;
  }

  private static boolean prepareFile(@NotNull File file) {
    try {
      FileUtil.writeToFile(file, new byte[]{0x7F, 'E', 'L', 'F'});
    }
    catch (IOException e) {
      return false;
    }
    return file.setExecutable(true);
  }

  private class MyRunStarter extends RunProfileStarter {
    private final String myOutputFilePath;
    private final GoHistoryProcessListener myHistoryProcessListener;


    private MyRunStarter(@NotNull String outputFilePath, @NotNull GoHistoryProcessListener historyProcessListener) {
      myOutputFilePath = outputFilePath;
      myHistoryProcessListener = historyProcessListener;
    }

    @Nullable
    @Override
    public RunContentDescriptor execute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env)
      throws ExecutionException {
      if (state instanceof GoApplicationRunningState) {
        FileDocumentManager.getInstance().saveAllDocuments();
        ((GoApplicationRunningState)state).setHistoryProcessHandler(myHistoryProcessListener);
        ((GoApplicationRunningState)state).setOutputFilePath(myOutputFilePath);
        ExecutionResult executionResult = state.execute(env.getExecutor(), GoBuildingRunner.this);
        return executionResult != null ? new RunContentBuilder(executionResult, env).showRunContent(env.getContentToReuse()) : null;
      }
      return null;
    }
  }
}
