/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

public abstract class AsynchronousChannelProvider {
  private static AsynchronousChannelProvider provider;

  protected AsynchronousChannelProvider() { }

  public static AsynchronousChannelProvider provider() {
    synchronized (AsynchronousChannelProvider.class) {
      if (provider == null) {
        provider = new sun.nio.ch.DefaultAsynchronousChannelProvider();
      }
      return provider;
    }
  }

  public abstract AsynchronousChannelGroup openAsynchronousChannelGroup(int nThreads, ThreadFactory threadFactory)
      throws IOException;

  public abstract AsynchronousChannelGroup openAsynchronousChannelGroup(ExecutorService executor, int initialSize)
      throws IOException;

  public abstract AsynchronousServerSocketChannel openAsynchronousServerSocketChannel(
      AsynchronousChannelGroup group) throws IOException;

  public abstract AsynchronousSocketChannel openAsynchronousSocketChannel(AsynchronousChannelGroup group)
      throws IOException;
}
