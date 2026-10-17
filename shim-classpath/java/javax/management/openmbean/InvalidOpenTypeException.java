/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

public class InvalidOpenTypeException extends IllegalArgumentException {
  private static final long serialVersionUID = -2837312755412327534L;

  public InvalidOpenTypeException() {
    super();
  }

  public InvalidOpenTypeException(String msg) {
    super(msg);
  }
}
