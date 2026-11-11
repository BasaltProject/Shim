/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell.execution;

public class JdiDefaultExecutionControl extends jdk.jshell.execution.JdiExecutionControl {

  private JdiDefaultExecutionControl(jdk.jshell.spi.ExecutionEnv executionEnv, java.io.ObjectOutput objectOutput, java.io.ObjectInput objectInput, com.sun.jdi.VirtualMachine virtualMachine, java.lang.Process process, java.lang.String string, java.util.List<java.util.function.Consumer<java.lang.String>> list) {
    super(objectOutput, objectInput);
  }

  public void close() {
    throw new UnsupportedOperationException();
  }
  public java.lang.String invoke(java.lang.String string, java.lang.String string2) throws jdk.jshell.spi.ExecutionControl.RunException, jdk.jshell.spi.ExecutionControl.EngineTerminationException, jdk.jshell.spi.ExecutionControl.InternalException {
    throw new UnsupportedOperationException();
  }
  public void stop() throws jdk.jshell.spi.ExecutionControl.EngineTerminationException, jdk.jshell.spi.ExecutionControl.InternalException {
    throw new UnsupportedOperationException();
  }
  protected synchronized com.sun.jdi.VirtualMachine vm() throws jdk.jshell.spi.ExecutionControl.EngineTerminationException {
    throw new UnsupportedOperationException();
  }

  public interface JdiStarter {
    jdk.jshell.execution.JdiDefaultExecutionControl.JdiStarter.TargetDescription start(jdk.jshell.spi.ExecutionEnv executionEnv, java.util.Map<java.lang.String, java.lang.String> map, int i);

    public record TargetDescription(com.sun.jdi.VirtualMachine vm, java.lang.Process process) {
    }
  }
}
