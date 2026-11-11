/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.smartcardio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;

final class PCSCTerminals extends CardTerminals {
  private static long contextId;
  private static final Map<String, int[]> kStateMap = new HashMap<String, int[]>();
  private static final Map<String, TerminalImpl> kTerminals = new HashMap<String, TerminalImpl>();

  static synchronized void initContext() throws PCSCException {
    if (contextId == 0) {
      contextId = PCSC.SCardEstablishContext(0);
    }
  }

  static synchronized long context() {
    return contextId;
  }

  private static synchronized TerminalImpl terminal(String name) {
    TerminalImpl t = kTerminals.get(name);
    if (t == null) {
      t = new TerminalImpl(contextId, name);
      kTerminals.put(name, t);
    }
    return t;
  }

  public synchronized List<CardTerminal> list(State state) throws CardException {
    if (state == null) {
      throw new NullPointerException();
    }
    try {
      String[] names;
      try {
        names = PCSC.SCardListReaders(contextId);
      } catch (PCSCException e) {
        if (e.code == PCSC.SCARD_E_NO_READERS_AVAILABLE) {
          kStateMap.clear();
          return new ArrayList<CardTerminal>();
        }
        throw e;
      }
      List<CardTerminal> out = new ArrayList<CardTerminal>(names.length);
      int[] events = null;
      if (state == State.CARD_INSERTION || state == State.CARD_REMOVAL) {
        int[] known = new int[names.length];
        for (int i = 0; i < names.length; i++) {
          int[] s = kStateMap.get(names[i]);
          known[i] = s == null ? PCSC.SCARD_STATE_UNAWARE : s[0];
        }
        events = PCSC.SCardGetStatusChange(contextId, 0, known, names);
      }
      for (int i = 0; i < names.length; i++) {
        TerminalImpl t = terminal(names[i]);
        switch (state) {
          case ALL:
            out.add(t);
            break;
          case CARD_PRESENT:
            if (t.isCardPresent()) {
              out.add(t);
            }
            break;
          case CARD_ABSENT:
            if (!t.isCardPresent()) {
              out.add(t);
            }
            break;
          case CARD_INSERTION:
            if ((events[i] & PCSC.SCARD_STATE_CHANGED) != 0 && (events[i] & PCSC.SCARD_STATE_PRESENT) != 0) {
              out.add(t);
            }
            break;
          default:
            if ((events[i] & PCSC.SCARD_STATE_CHANGED) != 0 && (events[i] & PCSC.SCARD_STATE_EMPTY) != 0) {
              out.add(t);
            }
            break;
        }
      }
      return out;
    } catch (PCSCException e) {
      throw new CardException("list() failed", e);
    }
  }

  public boolean waitForChange(long timeout) throws CardException {
    if (timeout < 0) {
      throw new IllegalArgumentException("Timeout must not be negative: " + timeout);
    }
    long wait = timeout == 0 ? PCSC.INFINITE : timeout;
    try {
      String[] names;
      int[] known;
      synchronized (this) {
        names = PCSC.SCardListReaders(contextId);
        known = new int[names.length];
        for (int i = 0; i < names.length; i++) {
          int[] s = kStateMap.get(names[i]);
          known[i] = s == null ? PCSC.SCARD_STATE_UNAWARE : s[0];
        }
      }
      int[] now = PCSC.SCardGetStatusChange(contextId, wait, known, names);
      synchronized (this) {
        for (int i = 0; i < names.length; i++) {
          kStateMap.put(names[i], new int[] {now[i]});
        }
      }
      return true;
    } catch (PCSCException e) {
      if (e.code == PCSC.SCARD_E_TIMEOUT) {
        return false;
      }
      throw new CardException("waitForChange() failed", e);
    }
  }
}
