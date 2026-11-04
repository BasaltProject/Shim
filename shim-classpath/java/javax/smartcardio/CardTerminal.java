/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.smartcardio;

public abstract class CardTerminal {
  protected CardTerminal() {
  }

  public abstract javax.smartcardio.Card connect(java.lang.String string) throws javax.smartcardio.CardException;
  public abstract java.lang.String getName();
  public abstract boolean isCardPresent() throws javax.smartcardio.CardException;
  public abstract boolean waitForCardAbsent(long l) throws javax.smartcardio.CardException;
  public abstract boolean waitForCardPresent(long l) throws javax.smartcardio.CardException;
}
