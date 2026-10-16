/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Pipe;
import java.nio.channels.spi.SelectorProvider;
import shim.NetHandle;
import shim.NetNative;

final class PipeImpl extends Pipe {
  private final SourceChannelImpl source;
  private final SinkChannelImpl sink;

  PipeImpl(SelectorProvider provider) throws IOException {
    NetNative.init();
    long[] pair = new long[2];
    NetNative.socketPair(pair);
    source = new SourceChannelImpl(provider, pair[0]);
    sink = new SinkChannelImpl(provider, pair[1]);
  }

  @Override
  public SourceChannel source() {
    return source;
  }

  @Override
  public SinkChannel sink() {
    return sink;
  }

  private static final class SourceChannelImpl extends Pipe.SourceChannel implements SelectorImpl.HasHandle {
    private final NetHandle net = new NetHandle(false);

    SourceChannelImpl(SelectorProvider provider, long handle) {
      super(provider);
      net.adopt(handle);
    }

    @Override
    public long rawHandle() {
      return net.peek();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
      if (!isOpen()) {
        throw new ClosedChannelException();
      }
      long h = net.begin();
      try {
        return ChannelIo.read(h, dst, isBlocking());
      } finally {
        net.end();
      }
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
      if (!isOpen()) {
        throw new ClosedChannelException();
      }
      long h = net.begin();
      try {
        return ChannelIo.read(h, dsts, offset, length, isBlocking());
      } finally {
        net.end();
      }
    }

    @Override
    public long read(ByteBuffer[] dsts) throws IOException {
      return read(dsts, 0, dsts.length);
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {
      net.close();
    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {
      long h = net.begin();
      try {
        NetNative.setBlocking(h, block);
      } finally {
        net.end();
      }
    }
  }

  private static final class SinkChannelImpl extends Pipe.SinkChannel implements SelectorImpl.HasHandle {
    private final NetHandle net = new NetHandle(false);

    SinkChannelImpl(SelectorProvider provider, long handle) {
      super(provider);
      net.adopt(handle);
    }

    @Override
    public long rawHandle() {
      return net.peek();
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
      if (!isOpen()) {
        throw new ClosedChannelException();
      }
      long h = net.begin();
      try {
        return ChannelIo.write(h, src, isBlocking());
      } finally {
        net.end();
      }
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
      if (!isOpen()) {
        throw new ClosedChannelException();
      }
      long h = net.begin();
      try {
        return ChannelIo.write(h, srcs, offset, length, isBlocking());
      } finally {
        net.end();
      }
    }

    @Override
    public long write(ByteBuffer[] srcs) throws IOException {
      return write(srcs, 0, srcs.length);
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {
      net.close();
    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {
      long h = net.begin();
      try {
        NetNative.setBlocking(h, block);
      } finally {
        net.end();
      }
    }
  }
}
