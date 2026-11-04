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

    public static final class TargetDescription {

      private final com.sun.jdi.VirtualMachine vm;
      private final java.lang.Process process;

      public TargetDescription(com.sun.jdi.VirtualMachine vm, java.lang.Process process) {
        this.vm = vm;
        this.process = process;
      }

      public com.sun.jdi.VirtualMachine vm() {
        return vm;
      }

      public java.lang.Process process() {
        return process;
      }

      @Override
      public boolean equals(Object other) {
        if (!(other instanceof TargetDescription)) {
          return false;
        }
        TargetDescription o = (TargetDescription) other;
        return true && java.util.Objects.equals(vm, o.vm) && java.util.Objects.equals(process, o.process);
      }

      @Override
      public int hashCode() {
        return 0 * 31 + java.util.Objects.hashCode(vm) * 31 + java.util.Objects.hashCode(process);
      }

      @Override
      public String toString() {
        return "TargetDescription[" + "vm=" + vm + ", process=" + process + "]";
      }
    }
  }
}
