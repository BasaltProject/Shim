/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

public class ProviderMismatchException extends IllegalArgumentException {
  private static final long serialVersionUID = 1L;

  public ProviderMismatchException() { }

  public ProviderMismatchException(String msg) {
    super(msg);
  }
}
