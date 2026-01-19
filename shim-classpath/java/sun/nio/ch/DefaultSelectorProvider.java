/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.io.IOException;
import java.net.ProtocolFamily;
import java.net.StandardProtocolFamily;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

public class DefaultSelectorProvider extends SelectorProvider {
  public DefaultSelectorProvider() { }

  @Override
  public DatagramChannel openDatagramChannel() throws IOException {
    return new DatagramChannelImpl(this, null);
  }

  @Override
  public DatagramChannel openDatagramChannel(ProtocolFamily family) throws IOException {
    if (family == null) {
      throw new NullPointerException();
    }
    if (family != StandardProtocolFamily.INET && family != StandardProtocolFamily.INET6) {
      throw new UnsupportedOperationException("Protocol family not supported");
    }
    return new DatagramChannelImpl(this, family);
  }

  @Override
  public Pipe openPipe() throws IOException {
    return new PipeImpl(this);
  }

  @Override
  public AbstractSelector openSelector() throws IOException {
    return new SelectorImpl(this);
  }

  @Override
  public ServerSocketChannel openServerSocketChannel() throws IOException {
    return new ServerSocketChannelImpl(this);
  }

  @Override
  public SocketChannel openSocketChannel() throws IOException {
    return new SocketChannelImpl(this);
  }

  @Override
  public SocketChannel openSocketChannel(ProtocolFamily family) throws IOException {
    java.util.Objects.requireNonNull(family);
    if (family == StandardProtocolFamily.UNIX) {
      return new SocketChannelImpl(this, true);
    }
    if (family != StandardProtocolFamily.INET && family != StandardProtocolFamily.INET6) {
      throw new UnsupportedOperationException("Protocol family not supported");
    }
    return new SocketChannelImpl(this);
  }

  @Override
  public ServerSocketChannel openServerSocketChannel(ProtocolFamily family) throws IOException {
    java.util.Objects.requireNonNull(family);
    if (family == StandardProtocolFamily.UNIX) {
      return new ServerSocketChannelImpl(this, true);
    }
    if (family != StandardProtocolFamily.INET && family != StandardProtocolFamily.INET6) {
      throw new UnsupportedOperationException("Protocol family not supported");
    }
    return new ServerSocketChannelImpl(this);
  }
}
