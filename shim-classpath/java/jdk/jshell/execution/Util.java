/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell.execution;

public class Util {

  private Util() {
  }

  public static void detectJdiExitEvent(com.sun.jdi.VirtualMachine virtualMachine, java.util.function.Consumer<java.lang.String> consumer) {
    throw new UnsupportedOperationException();
  }
  public static void forwardExecutionControl(jdk.jshell.spi.ExecutionControl executionControl, java.io.ObjectInput objectInput, java.io.ObjectOutput objectOutput) {
    throw new UnsupportedOperationException();
  }
  public static void forwardExecutionControlAndIO(jdk.jshell.spi.ExecutionControl executionControl, java.io.InputStream inputStream, java.io.OutputStream outputStream, java.util.Map<java.lang.String, java.util.function.Consumer<java.io.OutputStream>> map, java.util.Map<java.lang.String, java.util.function.Consumer<java.io.InputStream>> map2) throws java.io.IOException {
    throw new UnsupportedOperationException();
  }
  public static jdk.jshell.spi.ExecutionControl remoteInputOutput(java.io.InputStream inputStream, java.io.OutputStream outputStream, java.util.Map<java.lang.String, java.io.OutputStream> map, java.util.Map<java.lang.String, java.io.InputStream> map2, java.util.function.BiFunction<java.io.ObjectInput, java.io.ObjectOutput, jdk.jshell.spi.ExecutionControl> biFunction) throws java.io.IOException {
    throw new UnsupportedOperationException();
  }
}
