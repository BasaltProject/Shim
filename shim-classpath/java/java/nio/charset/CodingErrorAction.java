/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.charset;

public class CodingErrorAction {
  private final String name;

  private CodingErrorAction(String name) {
    this.name = name;
  }

  public static final CodingErrorAction IGNORE = new CodingErrorAction("IGNORE");
  public static final CodingErrorAction REPLACE = new CodingErrorAction("REPLACE");
  public static final CodingErrorAction REPORT = new CodingErrorAction("REPORT");

  @Override
  public String toString() {
    return name;
  }
}
