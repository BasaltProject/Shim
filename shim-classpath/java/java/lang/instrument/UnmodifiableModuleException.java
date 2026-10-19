/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.instrument;

public class UnmodifiableModuleException extends RuntimeException {
  private static final long serialVersionUID = 6912511912351080644L;

  public UnmodifiableModuleException() {
    super();
  }

  public UnmodifiableModuleException(String msg) {
    super(msg);
  }
}
