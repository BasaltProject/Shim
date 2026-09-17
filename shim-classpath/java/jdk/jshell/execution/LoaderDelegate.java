/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell.execution;

public interface LoaderDelegate {
  void addToClasspath(java.lang.String string) throws jdk.jshell.spi.ExecutionControl.EngineTerminationException, jdk.jshell.spi.ExecutionControl.InternalException;
  void classesRedefined(jdk.jshell.spi.ExecutionControl.ClassBytecodes[] classBytecodess);
  java.lang.Class<?> findClass(java.lang.String string) throws java.lang.ClassNotFoundException;
  void load(jdk.jshell.spi.ExecutionControl.ClassBytecodes[] classBytecodess) throws jdk.jshell.spi.ExecutionControl.ClassInstallException, jdk.jshell.spi.ExecutionControl.NotImplementedException, jdk.jshell.spi.ExecutionControl.EngineTerminationException;
}
