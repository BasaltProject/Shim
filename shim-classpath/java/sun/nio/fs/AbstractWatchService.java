/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

abstract class AbstractWatchService implements WatchService {
  private final LinkedBlockingDeque<WatchKey> pendingKeys = new LinkedBlockingDeque<WatchKey>();
  private final WatchKey closeKey = new AbstractWatchKey(null, null) {
    @Override
    public boolean isValid() {
      return true;
    }

    @Override
    public void cancel() {
    }
  };

  private volatile boolean closed;
  private final Object closeLock = new Object();

  protected AbstractWatchService() { }

  abstract WatchKey register(Path path, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifers)
      throws IOException;

  final void enqueueKey(WatchKey key) {
    pendingKeys.offer(key);
  }

  private void checkOpen() {
    if (closed) {
      throw new ClosedWatchServiceException();
    }
  }

  private void checkKey(WatchKey key) {
    if (key == closeKey) {
      pendingKeys.offer(key);
    }
    checkOpen();
  }

  @Override
  public final WatchKey poll() {
    checkOpen();
    WatchKey key = pendingKeys.poll();
    checkKey(key);
    return key;
  }

  @Override
  public final WatchKey poll(long timeout, TimeUnit unit) throws InterruptedException {
    checkOpen();
    WatchKey key = pendingKeys.poll(timeout, unit);
    checkKey(key);
    return key;
  }

  @Override
  public final WatchKey take() throws InterruptedException {
    checkOpen();
    WatchKey key = pendingKeys.take();
    checkKey(key);
    return key;
  }

  final boolean isOpen() {
    return !closed;
  }

  final Object closeLock() {
    return closeLock;
  }

  abstract void implClose() throws IOException;

  @Override
  public final void close() throws IOException {
    synchronized (closeLock) {
      if (closed) {
        return;
      }
      closed = true;
      implClose();
      pendingKeys.clear();
      pendingKeys.offer(closeKey);
    }
  }
}
