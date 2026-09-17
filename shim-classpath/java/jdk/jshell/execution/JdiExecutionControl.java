/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell.execution;

public abstract class JdiExecutionControl extends jdk.jshell.execution.StreamingExecutionControl implements jdk.jshell.spi.ExecutionControl {

  protected JdiExecutionControl(java.io.ObjectOutput objectOutput, java.io.ObjectInput objectInput) {
    super(objectOutput, objectInput);
  }

  public void redefine(jdk.jshell.spi.ExecutionControl.ClassBytecodes[] classBytecodess) throws jdk.jshell.spi.ExecutionControl.ClassInstallException, jdk.jshell.spi.ExecutionControl.EngineTerminationException {
    throw new UnsupportedOperationException();
  }
  protected com.sun.jdi.ReferenceType referenceType(com.sun.jdi.VirtualMachine virtualMachine, java.lang.String string) {
    throw new UnsupportedOperationException();
  }
  protected abstract com.sun.jdi.VirtualMachine vm() throws jdk.jshell.spi.ExecutionControl.EngineTerminationException;
}
