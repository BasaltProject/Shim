/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.smartcardio;

import java.util.Locale;
import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

final class CardImpl extends Card {
  final TerminalImpl terminal;
  final long cardId;
  final int protocol;
  private final ATR atr;
  private final ChannelImpl basic;
  private volatile boolean valid = true;
  private volatile Thread exclusiveThread;

  CardImpl(TerminalImpl terminal, String protocolString) throws PCSCException {
    this.terminal = terminal;
    int share = PCSC.SCARD_SHARE_SHARED;
    int wanted;
    if (protocolString.equals("*")) {
      wanted = 3;
    } else if (protocolString.equalsIgnoreCase("T=0")) {
      wanted = PCSC.SCARD_PROTOCOL_T0;
    } else if (protocolString.equalsIgnoreCase("T=1")) {
      wanted = PCSC.SCARD_PROTOCOL_T1;
    } else if (protocolString.equalsIgnoreCase("direct")) {
      wanted = 0;
      share = PCSC.SCARD_SHARE_DIRECT;
    } else {
      throw new IllegalArgumentException("Unsupported protocol " + protocolString);
    }
    long[] r = PCSC.SCardConnect(terminal.contextId, terminal.name, share, wanted);
    cardId = r[0];
    protocol = (int) r[1];
    byte[] bytes = new byte[0];
    try {
      bytes = PCSC.SCardStatus(cardId);
    } catch (PCSCException e) {
      // ATR stays empty on direct connections
    }
    atr = new ATR(bytes);
    basic = new ChannelImpl(this, 0);
  }

  boolean isValid() {
    return valid;
  }

  void checkState() {
    if (!valid) {
      throw new IllegalStateException("Card has been disconnected");
    }
  }

  void checkExclusive() throws CardException {
    Thread t = exclusiveThread;
    if (t != null && t != Thread.currentThread()) {
      throw new CardException("Exclusive access established by another Thread");
    }
  }

  public ATR getATR() {
    return atr;
  }

  public String getProtocol() {
    switch (protocol) {
      case PCSC.SCARD_PROTOCOL_T0:
        return "T=0";
      case PCSC.SCARD_PROTOCOL_T1:
        return "T=1";
      default:
        return "Unknown protocol " + protocol;
    }
  }

  public CardChannel getBasicChannel() {
    checkState();
    return basic;
  }

  public CardChannel openLogicalChannel() throws CardException {
    checkState();
    checkExclusive();
    try {
      byte[] response = basic.rawTransmit(new byte[] {0, 0x70, 0, 0, 1});
      if (response.length != 3 || response[1] != (byte) 0x90 || response[2] != 0) {
        throw new CardException("openLogicalChannel() failed, card response: " + toHex(response));
      }
      return new ChannelImpl(this, response[0]);
    } catch (PCSCException e) {
      handleError(e);
      throw new CardException("openLogicalChannel() failed", e);
    }
  }

  static String toHex(byte[] b) {
    StringBuilder sb = new StringBuilder();
    for (byte x : b) {
      sb.append(Character.forDigit((x >> 4) & 15, 16)).append(Character.forDigit(x & 15, 16));
    }
    return sb.toString().toUpperCase(Locale.ROOT);
  }

  void handleError(PCSCException e) {
    if (e.code == PCSC.SCARD_W_REMOVED_CARD) {
      valid = false;
    }
  }

  public synchronized void beginExclusive() throws CardException {
    checkState();
    checkExclusive();
    try {
      PCSC.SCardBeginTransaction(cardId);
    } catch (PCSCException e) {
      handleError(e);
      throw new CardException("beginExclusive() failed", e);
    }
    exclusiveThread = Thread.currentThread();
  }

  public synchronized void endExclusive() throws CardException {
    checkState();
    if (exclusiveThread != Thread.currentThread()) {
      throw new IllegalStateException("Exclusive access not assigned to current Thread");
    }
    try {
      PCSC.SCardEndTransaction(cardId, PCSC.SCARD_LEAVE_CARD);
    } catch (PCSCException e) {
      handleError(e);
      throw new CardException("endExclusive() failed", e);
    } finally {
      exclusiveThread = null;
    }
  }

  public byte[] transmitControlCommand(int controlCode, byte[] command) throws CardException {
    checkState();
    checkExclusive();
    if (command == null) {
      throw new NullPointerException();
    }
    try {
      return PCSC.SCardControl(cardId, controlCode, command);
    } catch (PCSCException e) {
      handleError(e);
      throw new CardException("transmitControlCommand() failed", e);
    }
  }

  public synchronized void disconnect(boolean reset) throws CardException {
    if (!valid) {
      return;
    }
    valid = false;
    try {
      PCSC.SCardDisconnect(cardId, reset ? PCSC.SCARD_RESET_CARD : PCSC.SCARD_LEAVE_CARD);
    } catch (PCSCException e) {
      throw new CardException("disconnect() failed", e);
    }
  }

  public String toString() {
    return "PC/SC card in " + terminal.name + ", protocol " + getProtocol() + ", state " + (valid ? "OK" : "DISCONNECTED");
  }
}
