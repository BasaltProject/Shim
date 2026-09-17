/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell.execution;

public class JdiExecutionControlProvider implements jdk.jshell.spi.ExecutionControlProvider {
  public static final java.lang.String PARAM_REMOTE_AGENT = "remoteAgent";
  public static final java.lang.String PARAM_TIMEOUT = "timeout";
  public static final java.lang.String PARAM_HOST_NAME = "hostname";
  public static final java.lang.String PARAM_LAUNCH = "launch";

  public JdiExecutionControlProvider() {
  }
  public JdiExecutionControlProvider(jdk.jshell.execution.JdiDefaultExecutionControl.JdiStarter jdiStarter) {
  }

  public java.util.Map<java.lang.String, java.lang.String> defaultParameters() {
    throw new UnsupportedOperationException();
  }
  public jdk.jshell.spi.ExecutionControl generate(jdk.jshell.spi.ExecutionEnv executionEnv, java.util.Map<java.lang.String, java.lang.String> map) throws java.io.IOException {
    throw new UnsupportedOperationException();
  }
  public java.lang.String name() {
    throw new UnsupportedOperationException();
  }
}
