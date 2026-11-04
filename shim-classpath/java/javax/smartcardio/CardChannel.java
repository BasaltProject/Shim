/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.smartcardio;

public abstract class CardChannel {
  protected CardChannel() {
  }

  public abstract void close() throws javax.smartcardio.CardException;
  public abstract javax.smartcardio.Card getCard();
  public abstract int getChannelNumber();
  public abstract int transmit(java.nio.ByteBuffer byteBuffer, java.nio.ByteBuffer byteBuffer2) throws javax.smartcardio.CardException;
  public abstract javax.smartcardio.ResponseAPDU transmit(javax.smartcardio.CommandAPDU commandAPDU) throws javax.smartcardio.CardException;
}
