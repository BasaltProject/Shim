/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import com.sun.nio.sctp.SctpSocketOption;

public final class SctpStdSocketOption<T> implements SctpSocketOption<T> {
  private final String name;
  private final Class<T> type;

  public SctpStdSocketOption(String name, Class<T> type) {
    this.name = name;
    this.type = type;
  }

  public String name() {
    return name;
  }

  public Class<T> type() {
    return type;
  }

  public String toString() {
    return name;
  }
}
