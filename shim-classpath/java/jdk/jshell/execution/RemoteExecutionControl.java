/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell.execution;

public class RemoteExecutionControl extends jdk.jshell.execution.DirectExecutionControl implements jdk.jshell.spi.ExecutionControl {

  public RemoteExecutionControl(jdk.jshell.execution.LoaderDelegate loaderDelegate) {
    super(loaderDelegate);
  }
  public RemoteExecutionControl() {
  }

  protected void clientCodeEnter() {
    throw new UnsupportedOperationException();
  }
  protected void clientCodeLeave() throws jdk.jshell.spi.ExecutionControl.InternalException {
    throw new UnsupportedOperationException();
  }
  protected java.lang.String invoke(java.lang.reflect.Method method) throws java.lang.Exception {
    throw new UnsupportedOperationException();
  }
  public static void main(java.lang.String[] strings) throws java.lang.Exception {
    throw new UnsupportedOperationException();
  }
  public void redefine(jdk.jshell.spi.ExecutionControl.ClassBytecodes[] classBytecodess) throws jdk.jshell.spi.ExecutionControl.ClassInstallException, jdk.jshell.spi.ExecutionControl.NotImplementedException, jdk.jshell.spi.ExecutionControl.EngineTerminationException {
    throw new UnsupportedOperationException();
  }
  public void stop() throws jdk.jshell.spi.ExecutionControl.EngineTerminationException, jdk.jshell.spi.ExecutionControl.InternalException {
    throw new UnsupportedOperationException();
  }
  protected java.lang.String throwConvertedInvocationException(java.lang.Throwable throwable) throws jdk.jshell.spi.ExecutionControl.RunException, jdk.jshell.spi.ExecutionControl.InternalException {
    throw new UnsupportedOperationException();
  }
  protected java.lang.String throwConvertedOtherException(java.lang.Throwable throwable) throws jdk.jshell.spi.ExecutionControl.RunException, jdk.jshell.spi.ExecutionControl.InternalException {
    throw new UnsupportedOperationException();
  }
  public java.lang.String varValue(java.lang.String string, java.lang.String string2) throws jdk.jshell.spi.ExecutionControl.RunException, jdk.jshell.spi.ExecutionControl.EngineTerminationException, jdk.jshell.spi.ExecutionControl.InternalException {
    throw new UnsupportedOperationException();
  }
}
