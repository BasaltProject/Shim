/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels;

import java.io.IOException;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;

public abstract class Pipe {
  public abstract static class SourceChannel extends AbstractSelectableChannel implements ReadableByteChannel,
      ScatteringByteChannel {
    protected SourceChannel(SelectorProvider provider) {
      super(provider);
    }

    public final int validOps() {
      return SelectionKey.OP_READ;
    }
  }

  public abstract static class SinkChannel extends AbstractSelectableChannel implements WritableByteChannel,
      GatheringByteChannel {
    protected SinkChannel(SelectorProvider provider) {
      super(provider);
    }

    public final int validOps() {
      return SelectionKey.OP_WRITE;
    }
  }

  protected Pipe() { }

  public abstract SourceChannel source();

  public abstract SinkChannel sink();

  public static Pipe open() throws IOException {
    return SelectorProvider.provider().openPipe();
  }
}
