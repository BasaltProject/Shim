/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.misc;

import java.util.HashMap;

public final class Signal {
  private static final String[] kNames = {
    null, "HUP", "INT", "QUIT", "ILL", "TRAP", "ABRT", "BUS", "FPE", "KILL", "USR1", "SEGV", "USR2", "PIPE",
    "ALRM", "TERM", "STKFLT", "CHLD", "CONT", "STOP", "TSTP", "TTIN", "TTOU", "URG", "XCPU", "XFSZ", "VTALRM",
    "PROF", "WINCH", "IO", "PWR", "SYS"
  };
  private static final HashMap<Integer, SignalHandler> handlers = new HashMap<>();

  private final int number;
  private final String name;

  public Signal(String name) {
    int n = -1;
    for (int i = 1; i < kNames.length; ++i) {
      if (kNames[i].equals(name)) {
        n = i;
        break;
      }
    }
    if (n < 0) {
      throw new IllegalArgumentException("Unknown signal: " + name);
    }
    this.number = n;
    this.name = name;
  }

  public boolean equals(Object object) {
    return object instanceof Signal && ((Signal) object).number == number;
  }

  public String getName() {
    return name;
  }

  public int getNumber() {
    return number;
  }

  // Handlers are recorded and run by raise(); no OS signal is hooked so the host keeps its own.
  public static synchronized SignalHandler handle(Signal signal, SignalHandler handler) {
    SignalHandler old = handlers.put(signal.number, handler);
    return old == null ? SignalHandler.SIG_DFL : old;
  }

  public int hashCode() {
    return number;
  }

  public static void raise(Signal signal) {
    SignalHandler h;
    synchronized (Signal.class) {
      h = handlers.get(signal.number);
    }
    if (h != null && h != SignalHandler.SIG_DFL && h != SignalHandler.SIG_IGN) {
      h.handle(signal);
    }
  }

  public String toString() {
    return "SIG" + name;
  }
}
