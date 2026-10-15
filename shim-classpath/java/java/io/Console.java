/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.io;

public final class Console implements Flushable {
  private Console() {}

  public PrintWriter writer() {
    return new PrintWriter(System.out, true);
  }

  public Reader reader() {
    return new InputStreamReader(System.in);
  }

  public Console format(String fmt, Object... args) {
    writer().format(fmt, args);
    return this;
  }

  public Console printf(String format, Object... args) {
    return format(format, args);
  }

  public String readLine(String fmt, Object... args) {
    return null;
  }

  public String readLine() {
    return readLine("");
  }

  public char[] readPassword(String fmt, Object... args) {
    return null;
  }

  public char[] readPassword() {
    return readPassword("");
  }

  @Override
  public void flush() {
    System.out.flush();
  }
}
