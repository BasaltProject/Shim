/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

public class AEADBadTagException extends BadPaddingException {
  private static final long serialVersionUID = -488059093241685509L;

  public AEADBadTagException() {
    super();
  }

  public AEADBadTagException(String msg) {
    super(msg);
  }
}
