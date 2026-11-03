/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.misc;

public interface SignalHandler {
  sun.misc.SignalHandler SIG_DFL = null;
  sun.misc.SignalHandler SIG_IGN = null;

  void handle(sun.misc.Signal signal);
}
