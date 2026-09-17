/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell.spi;

public interface ExecutionEnv {
  void closeDown();
  default java.util.Optional<jdk.jshell.JShellConsole> console() {
    throw new UnsupportedOperationException();
  }
  java.util.List<java.lang.String> extraRemoteVMOptions();
  java.io.PrintStream userErr();
  java.io.InputStream userIn();
  java.io.PrintStream userOut();
}
