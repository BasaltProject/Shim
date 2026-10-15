/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public interface WatchService extends Closeable {
  void close() throws IOException;

  WatchKey poll();

  WatchKey poll(long timeout, TimeUnit unit) throws InterruptedException;

  WatchKey take() throws InterruptedException;
}
