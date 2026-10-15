/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.logging;

public class ConsoleHandler extends StreamHandler {
  public ConsoleHandler() {
    setFormatter(new SimpleFormatter());
    setLevel(Level.INFO);
    setOutputStream(System.err);
  }

  public void publish(LogRecord record) {
    super.publish(record);
    flush();
  }

  public void close() {
    flush();
  }
}
