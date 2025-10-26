/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.openmbean;

public class KeyAlreadyExistsException extends IllegalArgumentException {
  private static final long serialVersionUID = 1845183636745282866L;

  public KeyAlreadyExistsException() {
    super();
  }

  public KeyAlreadyExistsException(String msg) {
    super(msg);
  }
}
