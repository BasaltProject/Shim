/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.reflect;

public class InaccessibleObjectException extends RuntimeException {
  private static final long serialVersionUID = 4158786093378140901L;

  public InaccessibleObjectException() {
    super();
  }

  public InaccessibleObjectException(String msg) {
    super(msg);
  }
}
