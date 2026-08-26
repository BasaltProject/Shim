/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.smartcardio;

import java.util.List;

public abstract class CardTerminals {
  public enum State {
    ALL, CARD_PRESENT, CARD_ABSENT, CARD_INSERTION, CARD_REMOVAL;
  }

  protected CardTerminals() {
  }

  public List<CardTerminal> list() throws CardException {
    return list(State.ALL);
  }

  public abstract List<CardTerminal> list(State state) throws CardException;

  public CardTerminal getTerminal(String name) {
    if (name == null) {
      throw new NullPointerException();
    }
    try {
      for (CardTerminal terminal : list()) {
        if (terminal.getName().equals(name)) {
          return terminal;
        }
      }
      return null;
    } catch (CardException e) {
      return null;
    }
  }

  public void waitForChange() throws CardException {
    waitForChange(0);
  }

  public abstract boolean waitForChange(long timeout) throws CardException;
}
