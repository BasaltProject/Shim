/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DefaultAsynchronousChannelProvider extends AsynchronousChannelProvider {
  public DefaultAsynchronousChannelProvider() { }

  @Override
  public AsynchronousChannelGroup openAsynchronousChannelGroup(int nThreads, ThreadFactory threadFactory)
      throws IOException {
    if (nThreads <= 0) {
      throw new IllegalArgumentException("'nThreads' must be > 0");
    }
    if (threadFactory == null) {
      throw new NullPointerException("'threadFactory' is null");
    }
    return new AsyncGroupImpl(this, Executors.newFixedThreadPool(nThreads, threadFactory));
  }

  @Override
  public AsynchronousChannelGroup openAsynchronousChannelGroup(ExecutorService executor, int initialSize)
      throws IOException {
    if (executor == null) {
      throw new NullPointerException("'executor' is null");
    }
    return new AsyncGroupImpl(this, executor);
  }

  static AsyncGroupImpl groupFor(AsynchronousChannelGroup group, DefaultAsynchronousChannelProvider provider) {
    if (group == null) {
      return AsyncGroupImpl.defaultGroup(provider);
    }
    if (!(group instanceof AsyncGroupImpl)) {
      throw new java.nio.channels.IllegalChannelGroupException();
    }
    return (AsyncGroupImpl) group;
  }

  @Override
  public AsynchronousServerSocketChannel openAsynchronousServerSocketChannel(AsynchronousChannelGroup group)
      throws IOException {
    return new AsynchronousServerSocketChannelImpl(this, groupFor(group, this));
  }

  @Override
  public AsynchronousSocketChannel openAsynchronousSocketChannel(AsynchronousChannelGroup group) throws IOException {
    return new AsynchronousSocketChannelImpl(this, groupFor(group, this), null);
  }
}
