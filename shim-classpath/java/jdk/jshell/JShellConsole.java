/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

public interface JShellConsole {
  java.nio.charset.Charset charset();
  void flush();
  java.lang.String readLine(java.lang.String string) throws java.io.IOError;
  char[] readPassword(java.lang.String string) throws java.io.IOError;
  java.io.Reader reader();
  java.io.PrintWriter writer();
}
