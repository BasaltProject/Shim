/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.smartcardio;

public abstract class Card {
  protected Card() {
  }

  public abstract void beginExclusive() throws javax.smartcardio.CardException;
  public abstract void disconnect(boolean flag) throws javax.smartcardio.CardException;
  public abstract void endExclusive() throws javax.smartcardio.CardException;
  public abstract javax.smartcardio.ATR getATR();
  public abstract javax.smartcardio.CardChannel getBasicChannel();
  public abstract java.lang.String getProtocol();
  public abstract javax.smartcardio.CardChannel openLogicalChannel() throws javax.smartcardio.CardException;
  public abstract byte[] transmitControlCommand(int i, byte[] bs) throws javax.smartcardio.CardException;
}
