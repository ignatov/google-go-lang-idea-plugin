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

import com.goide.GoConstants;
import com.goide.dlv.DlvDebugProcess;
import com.goide.dlv.DlvRemoteVmConnection;
import com.goide.runconfig.application.GoApplicationRunningState;
import com.goide.runconfig.file.GoRunFileRunningState;
import com.goide.runconfig.testing.GoTestRunningState;
import com.goide.util.GoHistoryProcessListener;
import com.goide.util.GoUtil;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.RunProfileStarter;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.internal.statistic.UsageTrigger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.net.NetUtils;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.debugger.connection.RemoteVmConnection;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class GoDlvRunner extends RunProfileStarter {
  private final String myOutputFilePath;
  private final GoHistoryProcessListener myHistoryProcessListener;
  private final ProgramRunner myProgramRunner;


  public GoDlvRunner(@NotNull String outputFilePath,
                     @NotNull GoHistoryProcessListener historyProcessListener,
                     ProgramRunner programRunner) {
    myOutputFilePath = outputFilePath;
    myHistoryProcessListener = historyProcessListener;
    myProgramRunner = programRunner;
  }

  public GoDlvRunner(ProgramRunner programRunner) {
    myProgramRunner = programRunner;
    myOutputFilePath = "";
    myHistoryProcessListener = null;
  }

  @Nullable
  @Override
  public RunContentDescriptor execute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env)
    throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();
    final int port = findFreePort();

    if (state instanceof GoApplicationRunningState) {
      ((GoApplicationRunningState)state).setHistoryProcessHandler(myHistoryProcessListener);
      ((GoApplicationRunningState)state).setOutputFilePath(myOutputFilePath);
      ((GoApplicationRunningState)state).setDebugPort(port);
    }
    else if (state instanceof GoTestRunningState) {
      ((GoTestRunningState)state).setDebugPort(port);
    }
    else if (state instanceof GoRunFileRunningState) {
      ((GoRunFileRunningState)state).setDebugPort(port);
    }
    else {
      return null;
    }

    // start debugger
    final ExecutionResult executionResult = state.execute(env.getExecutor(), myProgramRunner);
    if (executionResult == null) {
      throw new ExecutionException("Cannot run debugger");
    }

    UsageTrigger.trigger("go.dlv.debugger");

    return XDebuggerManager.getInstance(env.getProject()).startSession(env, new XDebugProcessStarter() {
      @NotNull
      @Override
      public XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException {
        RemoteVmConnection connection = new DlvRemoteVmConnection();
        DlvDebugProcess process = new DlvDebugProcess(session, connection, executionResult);
        connection.open(new InetSocketAddress(NetUtils.getLoopbackAddress(), port));
        return process;
      }
    }).getRunContentDescriptor();
  }

  @NotNull
  public static File getDlv() {
    String dlvPath = System.getProperty("dlv.path");
    if (StringUtil.isNotEmpty(dlvPath)) return new File(dlvPath);
    return new File(GoUtil.getPlugin().getPath(),
                    "lib/dlv/" + (SystemInfo.isMac ? "mac" : SystemInfo.isWindows ? "windows" : "linux") + "/"
                    + GoConstants.DELVE_EXECUTABLE_NAME + (SystemInfo.isWindows ? ".exe" : ""));
  }

  private static int findFreePort() {
    ServerSocket socket = null;
    try {
      //noinspection SocketOpenedButNotSafelyClosed
      socket = new ServerSocket(0);
      socket.setReuseAddress(true);
      return socket.getLocalPort();
    }
    catch (Exception ignore) {
    }
    finally {
      if (socket != null) {
        try {
          socket.close();
        }
        catch (Exception ignore) {
        }
      }
    }
    throw new IllegalStateException("Could not find a free TCP/IP port to start dlv");
  }
}