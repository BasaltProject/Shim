/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

class FileLockImpl extends FileLock {
  private volatile boolean invalid;

  FileLockImpl(FileChannel channel, long position, long size, boolean shared) {
    super(channel, position, size, shared);
  }

  @Override
  public boolean isValid() {
    return !invalid && channel().isOpen();
  }

  void invalidate() {
    invalid = true;
  }

  @Override
  public synchronized void release() throws IOException {
    FileChannelImpl ch = (FileChannelImpl) channel();
    if (!ch.isOpen()) {
      throw new ClosedChannelException();
    }
    if (isValid()) {
      ch.release(this);
      invalid = true;
    }
  }
}
