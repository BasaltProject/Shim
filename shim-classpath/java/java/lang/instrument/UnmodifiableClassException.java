/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.instrument;

public class UnmodifiableClassException extends Exception {
  private static final long serialVersionUID = 1716652643585309178L;

  public UnmodifiableClassException() {
    super();
  }

  public UnmodifiableClassException(String s) {
    super(s);
  }
}
