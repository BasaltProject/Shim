/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.zip;

public class ZipError extends InternalError {
  private static final long serialVersionUID = 853973422266861979L;

  public ZipError(String s) {
    super(s);
  }
}
