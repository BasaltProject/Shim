/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell.execution;

public class LocalExecutionControlProvider implements jdk.jshell.spi.ExecutionControlProvider {
  public LocalExecutionControlProvider() {
  }

  public jdk.jshell.spi.ExecutionControl createExecutionControl(jdk.jshell.spi.ExecutionEnv executionEnv, java.util.Map<java.lang.String, java.lang.String> map) {
    throw new UnsupportedOperationException();
  }
  public java.util.Map<java.lang.String, java.lang.String> defaultParameters() {
    throw new UnsupportedOperationException();
  }
  public jdk.jshell.spi.ExecutionControl generate(jdk.jshell.spi.ExecutionEnv executionEnv, java.util.Map<java.lang.String, java.lang.String> map) {
    throw new UnsupportedOperationException();
  }
  public java.lang.String name() {
    throw new UnsupportedOperationException();
  }
}
