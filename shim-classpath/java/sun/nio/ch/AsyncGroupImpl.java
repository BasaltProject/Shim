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
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

final class AsyncGroupImpl extends AsynchronousChannelGroup {
  private static AsyncGroupImpl defaultGroup;

  private final ExecutorService executor;

  AsyncGroupImpl(AsynchronousChannelProvider provider, ExecutorService executor) {
    super(provider);
    this.executor = executor;
  }

  static synchronized AsyncGroupImpl defaultGroup(AsynchronousChannelProvider provider) {
    if (defaultGroup == null) {
      ExecutorService pool = Executors.newCachedThreadPool(new ThreadFactory() {
        public Thread newThread(Runnable r) {
          Thread t = new Thread(r);
          t.setDaemon(true);
          return t;
        }
      });
      defaultGroup = new AsyncGroupImpl(provider, pool);
    }
    return defaultGroup;
  }

  void execute(Runnable task) {
    executor.execute(task);
  }

  @Override
  public boolean isShutdown() {
    return executor.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return executor.isTerminated();
  }

  @Override
  public void shutdown() {
    executor.shutdown();
  }

  @Override
  public void shutdownNow() throws IOException {
    executor.shutdownNow();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return executor.awaitTermination(timeout, unit);
  }
}
