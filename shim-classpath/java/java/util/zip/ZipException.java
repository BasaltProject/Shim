/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.zip;

import java.io.IOException;

public class ZipException extends IOException {
  private static final long serialVersionUID = 8000196834066748623L;

  public ZipException() {
    super();
  }

  public ZipException(String s) {
    super(s);
  }
}
