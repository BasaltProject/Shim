/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.smartcardio;

public class CardException extends java.lang.Exception {
  private static final long serialVersionUID = 7787607144922050628L;


  public CardException(java.lang.String string) {
    super(string);
  }
  public CardException(java.lang.Throwable throwable) {
    super(throwable);
  }
  public CardException(java.lang.String string, java.lang.Throwable throwable) {
    super(string, throwable);
  }

}
