/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.InterruptedByTimeoutException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.ReadPendingException;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritePendingException;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import shim.NetNative;

final class AsynchronousSocketChannelImpl extends AsynchronousSocketChannel {
  private final AsyncGroupImpl group;
  private final SocketChannelImpl channel;
  private final AtomicBoolean reading = new AtomicBoolean();
  private final AtomicBoolean writing = new AtomicBoolean();
  private final AtomicBoolean connecting = new AtomicBoolean();

  AsynchronousSocketChannelImpl(AsynchronousChannelProvider provider, AsyncGroupImpl group, SocketChannelImpl existing)
      throws IOException {
    super(provider);
    this.group = group;
    this.channel = existing != null ? existing : new SocketChannelImpl(
        java.nio.channels.spi.SelectorProvider.provider());
  }

  @Override
  public AsynchronousSocketChannel bind(SocketAddress local) throws IOException {
    try {
      channel.bind(local);
    } catch (java.nio.channels.AlreadyBoundException e) {
      throw new java.nio.channels.AlreadyBoundException();
    }
    return this;
  }

  @Override
  public <T> AsynchronousSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (!supportedOptions().contains(name)) {
      throw new UnsupportedOperationException("'" + name + "' not supported");
    }
    channel.setOption(name, value);
    return this;
  }

  @Override
  public <T> T getOption(SocketOption<T> name) throws IOException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (!supportedOptions().contains(name)) {
      throw new UnsupportedOperationException("'" + name + "' not supported");
    }
    return channel.getOption(name);
  }

  @Override
  public Set<SocketOption<?>> supportedOptions() {
    return shim.SocketOptionSupport.supported(shim.SocketOptionSupport.kAsyncClient);
  }

  @Override
  public AsynchronousSocketChannel shutdownInput() throws IOException {
    channel.shutdownInput();
    return this;
  }

  @Override
  public AsynchronousSocketChannel shutdownOutput() throws IOException {
    channel.shutdownOutput();
    return this;
  }

  @Override
  public SocketAddress getRemoteAddress() throws IOException {
    return channel.getRemoteAddress();
  }

  @Override
  public SocketAddress getLocalAddress() throws IOException {
    return channel.getLocalAddress();
  }

  @Override
  public boolean isOpen() {
    return channel.isOpen();
  }

  @Override
  public void close() throws IOException {
    channel.close();
  }

  private <V, A> void fail(CompletionHandler<V, ? super A> handler, A attachment, Throwable exc) {
    Throwable cause = exc;
    if (!channel.isOpen() && exc instanceof java.net.SocketException && exc.getMessage() != null
        && exc.getMessage().toLowerCase().contains("closed")) {
      cause = new AsynchronousCloseException();
    }
    handler.failed(cause, attachment);
  }

  @Override
  public <A> void connect(SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler) {
    if (handler == null) {
      throw new NullPointerException("'handler' is null");
    }
    if (channel.isConnected()) {
      throw new AlreadyConnectedException();
    }
    if (!connecting.compareAndSet(false, true)) {
      throw new ConnectionPendingException();
    }
    group.execute(new Runnable() {
      public void run() {
        try {
          channel.connect(remote);
        } catch (Throwable t) {
          connecting.set(false);
          fail(handler, attachment, t);
          return;
        }
        connecting.set(false);
        handler.completed(null, attachment);
      }
    });
  }

  @Override
  public Future<Void> connect(SocketAddress remote) {
    CompletableFuture<Void> future = new CompletableFuture<Void>();
    connect(remote, null, new CompletionHandler<Void, Object>() {
      public void completed(Void result, Object a) {
        future.complete(null);
      }

      public void failed(Throwable exc, Object a) {
        future.completeExceptionally(exc);
      }
    });
    return future;
  }

  @Override
  public <A> void read(ByteBuffer dst, long timeout, TimeUnit unit, A attachment,
      CompletionHandler<Integer, ? super A> handler) {
    if (handler == null) {
      throw new NullPointerException("'handler' is null");
    }
    if (dst.isReadOnly()) {
      throw new IllegalArgumentException("Read-only buffer");
    }
    if (!channel.isConnected() && channel.isOpen()) {
      throw new NotYetConnectedException();
    }
    if (!reading.compareAndSet(false, true)) {
      throw new ReadPendingException();
    }
    int timeoutMs = timeout <= 0 ? 0 : (int) Math.max(1, Math.min(Integer.MAX_VALUE, unit.toMillis(timeout)));
    group.execute(new Runnable() {
      public void run() {
        Integer result;
        try {
          if (!channel.isOpen()) {
            throw new ClosedChannelException();
          }
          result = Integer.valueOf(channel.readTimed(dst, timeoutMs));
        } catch (SocketTimeoutException t) {
          reading.set(false);
          handler.failed(new InterruptedByTimeoutException(), attachment);
          return;
        } catch (Throwable t) {
          reading.set(false);
          fail(handler, attachment, t);
          return;
        }
        reading.set(false);
        handler.completed(result, attachment);
      }
    });
  }

  @Override
  public Future<Integer> read(ByteBuffer dst) {
    CompletableFuture<Integer> future = new CompletableFuture<Integer>();
    read(dst, null, new CompletionHandler<Integer, Object>() {
      public void completed(Integer result, Object a) {
        future.complete(result);
      }

      public void failed(Throwable exc, Object a) {
        future.completeExceptionally(exc);
      }
    });
    return future;
  }

  @Override
  public <A> void read(ByteBuffer[] dsts, int offset, int length, long timeout, TimeUnit unit, A attachment,
      CompletionHandler<Long, ? super A> handler) {
    if (handler == null) {
      throw new NullPointerException("'handler' is null");
    }
    if (offset < 0 || length < 0 || offset > dsts.length - length) {
      throw new IndexOutOfBoundsException();
    }
    if (!channel.isConnected() && channel.isOpen()) {
      throw new NotYetConnectedException();
    }
    if (!reading.compareAndSet(false, true)) {
      throw new ReadPendingException();
    }
    int timeoutMs = timeout <= 0 ? 0 : (int) Math.max(1, Math.min(Integer.MAX_VALUE, unit.toMillis(timeout)));
    group.execute(new Runnable() {
      public void run() {
        long total = 0;
        try {
          if (!channel.isOpen()) {
            throw new ClosedChannelException();
          }
          for (int i = offset; i < offset + length; i++) {
            if (!dsts[i].hasRemaining()) {
              continue;
            }
            int n = channel.readTimed(dsts[i], total == 0 ? timeoutMs : -1);
            if (n < 0) {
              total = total == 0 ? -1 : total;
              break;
            }
            total += n;
            break;
          }
        } catch (SocketTimeoutException t) {
          reading.set(false);
          handler.failed(new InterruptedByTimeoutException(), attachment);
          return;
        } catch (Throwable t) {
          reading.set(false);
          fail(handler, attachment, t);
          return;
        }
        reading.set(false);
        handler.completed(Long.valueOf(total), attachment);
      }
    });
  }

  @Override
  public <A> void write(ByteBuffer src, long timeout, TimeUnit unit, A attachment,
      CompletionHandler<Integer, ? super A> handler) {
    if (handler == null) {
      throw new NullPointerException("'handler' is null");
    }
    if (!channel.isConnected() && channel.isOpen()) {
      throw new NotYetConnectedException();
    }
    if (!writing.compareAndSet(false, true)) {
      throw new WritePendingException();
    }
    group.execute(new Runnable() {
      public void run() {
        Integer result;
        try {
          if (!channel.isOpen()) {
            throw new ClosedChannelException();
          }
          result = Integer.valueOf(channel.write(src));
        } catch (Throwable t) {
          writing.set(false);
          fail(handler, attachment, t);
          return;
        }
        writing.set(false);
        handler.completed(result, attachment);
      }
    });
  }

  @Override
  public Future<Integer> write(ByteBuffer src) {
    CompletableFuture<Integer> future = new CompletableFuture<Integer>();
    write(src, null, new CompletionHandler<Integer, Object>() {
      public void completed(Integer result, Object a) {
        future.complete(result);
      }

      public void failed(Throwable exc, Object a) {
        future.completeExceptionally(exc);
      }
    });
    return future;
  }

  @Override
  public <A> void write(ByteBuffer[] srcs, int offset, int length, long timeout, TimeUnit unit, A attachment,
      CompletionHandler<Long, ? super A> handler) {
    if (handler == null) {
      throw new NullPointerException("'handler' is null");
    }
    if (offset < 0 || length < 0 || offset > srcs.length - length) {
      throw new IndexOutOfBoundsException();
    }
    if (!channel.isConnected() && channel.isOpen()) {
      throw new NotYetConnectedException();
    }
    if (!writing.compareAndSet(false, true)) {
      throw new WritePendingException();
    }
    group.execute(new Runnable() {
      public void run() {
        Long result;
        try {
          if (!channel.isOpen()) {
            throw new ClosedChannelException();
          }
          result = Long.valueOf(channel.write(srcs, offset, length));
        } catch (Throwable t) {
          writing.set(false);
          fail(handler, attachment, t);
          return;
        }
        writing.set(false);
        handler.completed(result, attachment);
      }
    });
  }

  SocketChannel channel() {
    return channel;
  }
}
