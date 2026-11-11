/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.smartcardio;

import java.nio.ByteBuffer;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

final class ChannelImpl extends CardChannel {
  private final CardImpl card;
  private final int channel;
  private volatile boolean closed;

  ChannelImpl(CardImpl card, int channel) {
    this.card = card;
    this.channel = channel;
  }

  byte[] rawTransmit(byte[] command) throws PCSCException {
    return PCSC.SCardTransmit(card.cardId, card.protocol, command, 0, command.length);
  }

  void checkClosed() {
    card.checkState();
    if (closed) {
      throw new IllegalStateException("Logical channel has been closed");
    }
  }

  public Card getCard() {
    return card;
  }

  public int getChannelNumber() {
    checkClosed();
    return channel;
  }

  public ResponseAPDU transmit(CommandAPDU command) throws CardException {
    checkClosed();
    card.checkExclusive();
    byte[] bytes = command.getBytes();
    return new ResponseAPDU(doTransmit(bytes));
  }

  public int transmit(ByteBuffer command, ByteBuffer response) throws CardException {
    checkClosed();
    card.checkExclusive();
    if (command == null || response == null) {
      throw new NullPointerException();
    }
    if (response.isReadOnly()) {
      throw new java.nio.ReadOnlyBufferException();
    }
    if (command == response) {
      throw new IllegalArgumentException("command and response must not be the same object");
    }
    if (response.remaining() < 258) {
      throw new IllegalArgumentException("Insufficient space in response buffer");
    }
    byte[] bytes = new byte[command.remaining()];
    command.get(bytes);
    byte[] r = doTransmit(bytes);
    response.put(r);
    return r.length;
  }

  private byte[] doTransmit(byte[] command) throws CardException {
    if (command.length >= 2 && command[1] == 0x70 && (command[0] & 0x80) == 0 && (command[0] & 0x40) == 0) {
      throw new IllegalArgumentException("Manage channel command not allowed, use openLogicalChannel()");
    }
    byte[] sent = command.clone();
    setChannel(sent);
    try {
      byte[] r = rawTransmit(sent);
      if (r.length == 2) {
        int sw1 = r[0] & 0xff;
        if (sw1 == 0x6c && sent.length >= 5) {
          sent[sent.length - 1] = r[1];
          r = rawTransmit(sent);
        }
      }
      if (r.length >= 2 && (r[r.length - 2] & 0xff) == 0x61) {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        while (r.length >= 2 && (r[r.length - 2] & 0xff) == 0x61) {
          out.write(r, 0, r.length - 2);
          byte[] get = {(byte) channelClass(), (byte) 0xc0, 0, 0, r[r.length - 1]};
          r = rawTransmit(get);
        }
        out.write(r, 0, r.length);
        r = out.toByteArray();
      }
      return r;
    } catch (PCSCException e) {
      card.handleError(e);
      throw new CardException("transmit() failed", e);
    }
  }

  private int channelClass() {
    if (channel == 0) {
      return 0;
    }
    return channel < 4 ? channel : 0x40 | (channel - 4);
  }

  private void setChannel(byte[] command) {
    if (channel == 0 || command.length == 0) {
      return;
    }
    int cla = command[0] & 0xff;
    if (cla >= 0x80 && cla < 0xb0 && cla != 0x80 || cla == 0xff) {
      return;
    }
    if (channel < 4) {
      command[0] = (byte) ((cla & 0xbc) | channel);
    } else {
      command[0] = (byte) ((cla & 0xb0) | 0x40 | (channel - 4));
    }
  }

  public void close() throws CardException {
    if (getChannelNumber() == 0) {
      throw new IllegalStateException("Cannot close basic logical channel");
    }
    if (closed) {
      return;
    }
    card.checkExclusive();
    try {
      byte[] r = rawTransmit(new byte[] {(byte) channelClass(), 0x70, (byte) 0x80, (byte) channel});
      if (r.length != 2 || r[0] != (byte) 0x90 || r[1] != 0) {
        throw new CardException("close() failed, card response: " + CardImpl.toHex(r));
      }
    } catch (PCSCException e) {
      card.handleError(e);
      throw new CardException("Could not close channel", e);
    }
    closed = true;
  }

  public String toString() {
    return "PC/SC channel " + channel;
  }
}
