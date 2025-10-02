/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang;

public class UnknownError extends VirtualMachineError {
  private static final long serialVersionUID = 2524784860676771849L;

  public UnknownError() {
    super();
  }

  public UnknownError(String s) {
    super(s);
  }
}
