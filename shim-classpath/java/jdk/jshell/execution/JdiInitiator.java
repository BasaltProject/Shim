/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell.execution;

public class JdiInitiator {

  public JdiInitiator(int i, java.util.List<java.lang.String> list, java.lang.String string, boolean flag, java.lang.String string2, int i2, java.util.Map<java.lang.String, java.lang.String> map) {
  }

  public java.lang.Process process() {
    throw new UnsupportedOperationException();
  }
  protected void runListenProcess(java.lang.String string, int i, java.util.List<java.lang.String> list, jdk.jshell.execution.JdiInitiator.ProcessStarted processStarted) {
    throw new UnsupportedOperationException();
  }
  public com.sun.jdi.VirtualMachine vm() {
    throw new UnsupportedOperationException();
  }

  protected interface ProcessStarted {
    void processStarted(java.lang.Process process) throws java.lang.Throwable;
  }
}
