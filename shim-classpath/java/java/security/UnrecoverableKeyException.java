/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public class UnrecoverableKeyException extends UnrecoverableEntryException {
  private static final long serialVersionUID = 7275063078190151277L;

  public UnrecoverableKeyException() {
    super();
  }

  public UnrecoverableKeyException(String msg) {
    super(msg);
  }
}
