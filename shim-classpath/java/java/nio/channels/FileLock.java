/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels;

import java.io.IOException;
import java.util.Objects;

public abstract class FileLock implements AutoCloseable {
  private final Channel channel;
  private final long position;
  private final long size;
  private final boolean shared;

  protected FileLock(FileChannel channel, long position, long size, boolean shared) {
    if (position < 0 || size < 0 || position + size < 0) {
      throw new IllegalArgumentException("Negative position or size");
    }
    this.channel = Objects.requireNonNull(channel, "channel");
    this.position = position;
    this.size = size;
    this.shared = shared;
  }

  protected FileLock(Channel channel, long position, long size, boolean shared) {
    if (position < 0 || size < 0 || position + size < 0) {
      throw new IllegalArgumentException("Negative position or size");
    }
    this.channel = Objects.requireNonNull(channel, "channel");
    this.position = position;
    this.size = size;
    this.shared = shared;
  }

  public final Channel acquiredBy() {
    return channel;
  }

  public final FileChannel channel() {
    return (channel instanceof FileChannel) ? (FileChannel) channel : null;
  }

  public final long position() {
    return position;
  }

  public final long size() {
    return size;
  }

  public final boolean isShared() {
    return shared;
  }

  public final boolean overlaps(long position, long size) {
    if (position + size <= this.position) return false;
    if (this.position + this.size <= position) return false;
    return true;
  }

  public abstract boolean isValid();

  public abstract void release() throws IOException;

  @Override
  public final void close() throws IOException {
    release();
  }

  @Override
  public String toString() {
    return getClass().getName() + "[position=" + position + ", size=" + size + ", shared=" + shared + ", valid=" + isValid() + "]";
  }
}
