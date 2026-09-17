/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell.execution;

public class LocalExecutionControl extends jdk.jshell.execution.DirectExecutionControl {

  public LocalExecutionControl(jdk.jshell.execution.LoaderDelegate loaderDelegate) {
    super(loaderDelegate);
  }
  public LocalExecutionControl(java.lang.ClassLoader classLoader) {
  }
  public LocalExecutionControl() {
  }

  protected void clientCodeEnter() {
    throw new UnsupportedOperationException();
  }
  protected void clientCodeLeave() {
    throw new UnsupportedOperationException();
  }
  protected java.lang.String invoke(java.lang.reflect.Method method) throws java.lang.Exception {
    throw new UnsupportedOperationException();
  }
  public void load(jdk.jshell.spi.ExecutionControl.ClassBytecodes[] classBytecodess) throws jdk.jshell.spi.ExecutionControl.ClassInstallException, jdk.jshell.spi.ExecutionControl.NotImplementedException, jdk.jshell.spi.ExecutionControl.EngineTerminationException {
    throw new UnsupportedOperationException();
  }
  public void stop() throws jdk.jshell.spi.ExecutionControl.EngineTerminationException, jdk.jshell.spi.ExecutionControl.InternalException {
    throw new UnsupportedOperationException();
  }
}
