/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.io.IOException;
import java.nio.channels.spi.SelectorProvider;

public final class SctpProvider {
  private SctpProvider() {
  }

  public static Object open(int kind) throws IOException {
    SelectorProvider sp = SelectorProvider.provider();
    switch (kind) {
      case 0:
        return new SctpChannelImpl(sp);
      case 1:
        return new SctpServerChannelImpl(sp);
      default:
        return new SctpMultiChannelImpl(sp);
    }
  }
}
