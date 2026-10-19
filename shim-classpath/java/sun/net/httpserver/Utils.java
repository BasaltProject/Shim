/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package sun.net.httpserver;

public final class Utils {
  private Utils() { }

  public static boolean isTokenChar(char c) {
    if (c <= ' ' || c >= 127) {
      return false;
    }
    return "()<>@,;:\\\"/[]?={}".indexOf(c) < 0;
  }
}
