/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class AsynchronousFileChannelImpl extends AsynchronousFileChannel {
  private final FileChannel channel;
  private final ExecutorService executor;
  private final boolean readable;
  private final boolean writable;

  public AsynchronousFileChannelImpl(Path file, Set<? extends OpenOption> options, ExecutorService executor,
      FileAttribute<?>... attrs) throws IOException {
    if (options.contains(StandardOpenOption.APPEND)) {
      throw new UnsupportedOperationException("APPEND not allowed");
    }
    this.channel = FileChannel.open(file, options, attrs);
    this.writable = options.contains(StandardOpenOption.WRITE);
    this.readable = options.contains(StandardOpenOption.READ) || !writable;
    this.executor = executor != null ? executor
        : Executors.newCachedThreadPool(new java.util.concurrent.ThreadFactory() {
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
      }
    });
  }

  @Override
  public boolean isOpen() {
    return channel.isOpen();
  }

  @Override
  public void close() throws IOException {
    channel.close();
  }

  @Override
  public long size() throws IOException {
    return channel.size();
  }

  @Override
  public AsynchronousFileChannel truncate(long size) throws IOException {
    if (size < 0) {
      throw new IllegalArgumentException("Negative size");
    }
    if (!writable) {
      throw new NonWritableChannelException();
    }
    channel.truncate(size);
    return this;
  }

  @Override
  public void force(boolean metaData) throws IOException {
    channel.force(metaData);
  }

  private interface Op<V> {
    V run() throws Exception;
  }

  private <V, A> void submit(Op<V> op, A attachment, CompletionHandler<V, ? super A> handler) {
    executor.execute(new Runnable() {
      public void run() {
        V result;
        try {
          if (!channel.isOpen()) {
            throw new ClosedChannelException();
          }
          result = op.run();
        } catch (Throwable t) {
          Throwable cause = t;
          if (!channel.isOpen() && !(t instanceof ClosedChannelException)) {
            cause = new AsynchronousCloseException();
          }
          handler.failed(cause, attachment);
          return;
        }
        handler.completed(result, attachment);
      }
    });
  }

  private <V> Future<V> future(Op<V> op) {
    CompletableFuture<V> future = new CompletableFuture<V>();
    submit(op, null, new CompletionHandler<V, Object>() {
      public void completed(V result, Object a) {
        future.complete(result);
      }

      public void failed(Throwable exc, Object a) {
        future.completeExceptionally(exc);
      }
    });
    return future;
  }

  private void checkLock(long position, long size) {
    if (position < 0) {
      throw new IllegalArgumentException("Negative position");
    }
    if (size < 0) {
      throw new IllegalArgumentException("Negative size");
    }
    if (position + size < 0) {
      throw new IllegalArgumentException("Negative position + size");
    }
  }

  @Override
  public <A> void lock(long position, long size, boolean shared, A attachment,
      CompletionHandler<FileLock, ? super A> handler) {
    if (handler == null) {
      throw new NullPointerException("'handler' is null");
    }
    checkLock(position, size);
    if (shared && !readable) {
      throw new NonReadableChannelException();
    }
    if (!shared && !writable) {
      throw new NonWritableChannelException();
    }
    submit(() -> channel.lock(position, size, shared), attachment, handler);
  }

  @Override
  public Future<FileLock> lock(long position, long size, boolean shared) {
    checkLock(position, size);
    if (shared && !readable) {
      throw new NonReadableChannelException();
    }
    if (!shared && !writable) {
      throw new NonWritableChannelException();
    }
    return future(() -> channel.lock(position, size, shared));
  }

  @Override
  public FileLock tryLock(long position, long size, boolean shared) throws IOException {
    checkLock(position, size);
    if (shared && !readable) {
      throw new NonReadableChannelException();
    }
    if (!shared && !writable) {
      throw new NonWritableChannelException();
    }
    return channel.tryLock(position, size, shared);
  }

  @Override
  public <A> void read(ByteBuffer dst, long position, A attachment, CompletionHandler<Integer, ? super A> handler) {
    if (handler == null) {
      throw new NullPointerException("'handler' is null");
    }
    if (position < 0) {
      throw new IllegalArgumentException("Negative position");
    }
    if (!readable) {
      throw new NonReadableChannelException();
    }
    if (dst.isReadOnly()) {
      throw new IllegalArgumentException("Read-only buffer");
    }
    submit(() -> Integer.valueOf(channel.read(dst, position)), attachment, handler);
  }

  @Override
  public Future<Integer> read(ByteBuffer dst, long position) {
    if (position < 0) {
      throw new IllegalArgumentException("Negative position");
    }
    if (!readable) {
      throw new NonReadableChannelException();
    }
    if (dst.isReadOnly()) {
      throw new IllegalArgumentException("Read-only buffer");
    }
    return future(() -> Integer.valueOf(channel.read(dst, position)));
  }

  @Override
  public <A> void write(ByteBuffer src, long position, A attachment, CompletionHandler<Integer, ? super A> handler) {
    if (handler == null) {
      throw new NullPointerException("'handler' is null");
    }
    if (position < 0) {
      throw new IllegalArgumentException("Negative position");
    }
    if (!writable) {
      throw new NonWritableChannelException();
    }
    submit(() -> Integer.valueOf(channel.write(src, position)), attachment, handler);
  }

  @Override
  public Future<Integer> write(ByteBuffer src, long position) {
    if (position < 0) {
      throw new IllegalArgumentException("Negative position");
    }
    if (!writable) {
      throw new NonWritableChannelException();
    }
    return future(() -> Integer.valueOf(channel.write(src, position)));
  }
}
