/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

public class InvalidKeyException extends IllegalArgumentException {
  private static final long serialVersionUID = 4224269443946322062L;

  public InvalidKeyException() {
    super();
  }

  public InvalidKeyException(String msg) {
    super(msg);
  }
}
