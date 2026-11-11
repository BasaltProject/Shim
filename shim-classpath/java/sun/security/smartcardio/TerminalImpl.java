/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.smartcardio;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardNotPresentException;
import javax.smartcardio.CardTerminal;

final class TerminalImpl extends CardTerminal {
  final long contextId;
  final String name;
  private CardImpl card;

  TerminalImpl(long contextId, String name) {
    this.contextId = contextId;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public synchronized Card connect(String protocol) throws CardException {
    if (card != null) {
      if (card.isValid()) {
        String cardProto = card.getProtocol();
        if (protocol.equals("*") || protocol.equalsIgnoreCase(cardProto)) {
          return card;
        }
        throw new CardException("Cannot connect using " + protocol + ", connection already established using "
            + cardProto);
      }
      card = null;
    }
    try {
      card = new CardImpl(this, protocol);
      return card;
    } catch (PCSCException e) {
      if (e.code == PCSC.SCARD_W_REMOVED_CARD || e.code == PCSC.SCARD_E_NO_SMARTCARD) {
        throw new CardNotPresentException("No card present", e);
      }
      throw new CardException("connect() failed", e);
    }
  }

  public boolean isCardPresent() throws CardException {
    try {
      int[] status = PCSC.SCardGetStatusChange(contextId, 0, new int[] {PCSC.SCARD_STATE_UNAWARE},
          new String[] {name});
      return (status[0] & PCSC.SCARD_STATE_PRESENT) != 0;
    } catch (PCSCException e) {
      throw new CardException("isCardPresent() failed", e);
    }
  }

  private boolean waitFor(boolean wantPresent, long timeout) throws CardException {
    if (timeout < 0) {
      throw new IllegalArgumentException("Timeout must not be negative: " + timeout);
    }
    long wait = timeout == 0 ? PCSC.INFINITE : timeout;
    long deadline = timeout == 0 ? Long.MAX_VALUE : System.currentTimeMillis() + timeout;
    try {
      int[] status = PCSC.SCardGetStatusChange(contextId, 0, new int[] {PCSC.SCARD_STATE_UNAWARE},
          new String[] {name});
      while (((status[0] & PCSC.SCARD_STATE_PRESENT) != 0) != wantPresent) {
        long left = timeout == 0 ? PCSC.INFINITE : deadline - System.currentTimeMillis();
        if (left <= 0) {
          return false;
        }
        status = PCSC.SCardGetStatusChange(contextId, left, new int[] {status[0]}, new String[] {name});
      }
      return true;
    } catch (PCSCException e) {
      if (e.code == PCSC.SCARD_E_TIMEOUT) {
        return false;
      }
      throw new CardException("waitForCard" + (wantPresent ? "Present" : "Absent") + "() failed", e);
    }
  }

  public boolean waitForCardPresent(long timeout) throws CardException {
    return waitFor(true, timeout);
  }

  public boolean waitForCardAbsent(long timeout) throws CardException {
    return waitFor(false, timeout);
  }

  public String toString() {
    return "PC/SC terminal " + name;
  }
}
