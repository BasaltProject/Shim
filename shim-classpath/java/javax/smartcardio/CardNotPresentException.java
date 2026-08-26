/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.smartcardio;

public class CardNotPresentException extends javax.smartcardio.CardException {
  private static final long serialVersionUID = 1346879911706545215L;


  public CardNotPresentException(java.lang.String string) {
    super(string);
  }
  public CardNotPresentException(java.lang.Throwable throwable) {
    super(throwable);
  }
  public CardNotPresentException(java.lang.String string, java.lang.Throwable throwable) {
    super(string, throwable);
  }

}
