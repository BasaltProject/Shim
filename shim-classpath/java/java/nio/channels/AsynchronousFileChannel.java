/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public abstract class AsynchronousFileChannel implements AsynchronousChannel {
  protected AsynchronousFileChannel() { }

  public static AsynchronousFileChannel open(Path file, Set<? extends OpenOption> options, ExecutorService executor,
      FileAttribute<?>... attrs) throws IOException {
    return new sun.nio.ch.AsynchronousFileChannelImpl(file, options, executor, attrs);
  }

  public static AsynchronousFileChannel open(Path file, OpenOption... options) throws IOException {
    Set<OpenOption> set = new HashSet<OpenOption>();
    Collections.addAll(set, options);
    return open(file, set, null);
  }

  public abstract long size() throws IOException;

  public abstract AsynchronousFileChannel truncate(long size) throws IOException;

  public abstract void force(boolean metaData) throws IOException;

  public abstract <A> void lock(long position, long size, boolean shared, A attachment,
      CompletionHandler<FileLock, ? super A> handler);

  public final <A> void lock(A attachment, CompletionHandler<FileLock, ? super A> handler) {
    lock(0L, Long.MAX_VALUE, false, attachment, handler);
  }

  public abstract Future<FileLock> lock(long position, long size, boolean shared);

  public final Future<FileLock> lock() {
    return lock(0L, Long.MAX_VALUE, false);
  }

  public abstract FileLock tryLock(long position, long size, boolean shared) throws IOException;

  public final FileLock tryLock() throws IOException {
    return tryLock(0L, Long.MAX_VALUE, false);
  }

  public abstract <A> void read(ByteBuffer dst, long position, A attachment,
      CompletionHandler<Integer, ? super A> handler);

  public abstract Future<Integer> read(ByteBuffer dst, long position);

  public abstract <A> void write(ByteBuffer src, long position, A attachment,
      CompletionHandler<Integer, ? super A> handler);

  public abstract Future<Integer> write(ByteBuffer src, long position);
}
