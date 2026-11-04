/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell.execution;

public class StreamingExecutionControl implements jdk.jshell.spi.ExecutionControl {

  public StreamingExecutionControl(java.io.ObjectOutput objectOutput, java.io.ObjectInput objectInput) {
  }

  public void addToClasspath(java.lang.String string) throws jdk.jshell.spi.ExecutionControl.EngineTerminationException, jdk.jshell.spi.ExecutionControl.InternalException {
    throw new UnsupportedOperationException();
  }
  public void close() {
    throw new UnsupportedOperationException();
  }
  public java.lang.Object extensionCommand(java.lang.String string, java.lang.Object object) throws jdk.jshell.spi.ExecutionControl.RunException, jdk.jshell.spi.ExecutionControl.EngineTerminationException, jdk.jshell.spi.ExecutionControl.InternalException {
    throw new UnsupportedOperationException();
  }
  public java.lang.String invoke(java.lang.String string, java.lang.String string2) throws jdk.jshell.spi.ExecutionControl.RunException, jdk.jshell.spi.ExecutionControl.EngineTerminationException, jdk.jshell.spi.ExecutionControl.InternalException {
    throw new UnsupportedOperationException();
  }
  public void load(jdk.jshell.spi.ExecutionControl.ClassBytecodes[] classBytecodess) throws jdk.jshell.spi.ExecutionControl.ClassInstallException, jdk.jshell.spi.ExecutionControl.NotImplementedException, jdk.jshell.spi.ExecutionControl.EngineTerminationException {
    throw new UnsupportedOperationException();
  }
  public void redefine(jdk.jshell.spi.ExecutionControl.ClassBytecodes[] classBytecodess) throws jdk.jshell.spi.ExecutionControl.ClassInstallException, jdk.jshell.spi.ExecutionControl.NotImplementedException, jdk.jshell.spi.ExecutionControl.EngineTerminationException {
    throw new UnsupportedOperationException();
  }
  public void stop() throws jdk.jshell.spi.ExecutionControl.EngineTerminationException, jdk.jshell.spi.ExecutionControl.InternalException {
    throw new UnsupportedOperationException();
  }
  public java.lang.String varValue(java.lang.String string, java.lang.String string2) throws jdk.jshell.spi.ExecutionControl.RunException, jdk.jshell.spi.ExecutionControl.EngineTerminationException, jdk.jshell.spi.ExecutionControl.InternalException {
    throw new UnsupportedOperationException();
  }
}
