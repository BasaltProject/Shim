/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels.spi;

import java.io.IOException;
import java.net.ProtocolFamily;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public abstract class SelectorProvider {
  private static SelectorProvider provider;

  protected SelectorProvider() { }

  public static SelectorProvider provider() {
    synchronized (SelectorProvider.class) {
      if (provider == null) {
        provider = new sun.nio.ch.DefaultSelectorProvider();
      }
      return provider;
    }
  }

  public abstract DatagramChannel openDatagramChannel() throws IOException;

  public abstract DatagramChannel openDatagramChannel(ProtocolFamily family) throws IOException;

  public abstract Pipe openPipe() throws IOException;

  public abstract AbstractSelector openSelector() throws IOException;

  public abstract ServerSocketChannel openServerSocketChannel() throws IOException;

  public abstract SocketChannel openSocketChannel() throws IOException;

  public java.nio.channels.Channel inheritedChannel() throws IOException {
    return null;
  }
}
