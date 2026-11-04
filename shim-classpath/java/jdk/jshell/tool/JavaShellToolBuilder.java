/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell.tool;

public interface JavaShellToolBuilder {
  static jdk.jshell.tool.JavaShellToolBuilder builder() {
    throw new UnsupportedOperationException();
  }
  jdk.jshell.tool.JavaShellToolBuilder env(java.util.Map<java.lang.String, java.lang.String> map);
  jdk.jshell.tool.JavaShellToolBuilder err(java.io.PrintStream printStream, java.io.PrintStream printStream2);
  jdk.jshell.tool.JavaShellToolBuilder err(java.io.PrintStream printStream);
  jdk.jshell.tool.JavaShellToolBuilder in(java.io.InputStream inputStream, java.io.InputStream inputStream2);
  default jdk.jshell.tool.JavaShellToolBuilder interactiveTerminal(boolean flag) {
    throw new UnsupportedOperationException();
  }
  jdk.jshell.tool.JavaShellToolBuilder locale(java.util.Locale locale);
  jdk.jshell.tool.JavaShellToolBuilder out(java.io.PrintStream printStream, java.io.PrintStream printStream2, java.io.PrintStream printStream3);
  jdk.jshell.tool.JavaShellToolBuilder out(java.io.PrintStream printStream);
  jdk.jshell.tool.JavaShellToolBuilder persistence(java.util.prefs.Preferences preferences);
  jdk.jshell.tool.JavaShellToolBuilder persistence(java.util.Map<java.lang.String, java.lang.String> map);
  jdk.jshell.tool.JavaShellToolBuilder promptCapture(boolean flag);
  void run(java.lang.String... strings) throws java.lang.Exception;
  default int start(java.lang.String... strings) throws java.lang.Exception {
    throw new UnsupportedOperationException();
  }
  default jdk.jshell.tool.JavaShellToolBuilder windowSize(int i, int i2) {
    throw new UnsupportedOperationException();
  }
}
