/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell.spi;

public interface ExecutionControlProvider {
  default java.util.Map<java.lang.String, java.lang.String> defaultParameters() {
    throw new UnsupportedOperationException();
  }
  jdk.jshell.spi.ExecutionControl generate(jdk.jshell.spi.ExecutionEnv executionEnv, java.util.Map<java.lang.String, java.lang.String> map) throws java.lang.Throwable;
  java.lang.String name();
}
