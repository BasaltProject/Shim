/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public class UnrecoverableEntryException extends GeneralSecurityException {
  private static final long serialVersionUID = -4527142945246286535L;

  public UnrecoverableEntryException() {
    super();
  }

  public UnrecoverableEntryException(String msg) {
    super(msg);
  }
}
