/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

public class StructureViolationException extends RuntimeException {
  private static final long serialVersionUID = -7705327650798235468L;

  public StructureViolationException() { }

  public StructureViolationException(String message) {
    super(message);
  }
}
