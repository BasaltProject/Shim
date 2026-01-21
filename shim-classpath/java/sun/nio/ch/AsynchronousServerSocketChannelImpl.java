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
import java.nio.channels.AcceptPendingException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

final class AsynchronousServerSocketChannelImpl extends AsynchronousServerSocketChannel {
  private final AsyncGroupImpl group;
  private final ServerSocketChannelImpl channel;
  private final AtomicBoolean accepting = new AtomicBoolean();

  AsynchronousServerSocketChannelImpl(AsynchronousChannelProvider provider, AsyncGroupImpl group) throws IOException {
    super(provider);
    this.group = group;
    this.channel = new ServerSocketChannelImpl(java.nio.channels.spi.SelectorProvider.provider());
  }

  @Override
  public AsynchronousServerSocketChannel bind(SocketAddress local, int backlog) throws IOException {
    channel.bind(local, backlog);
    return this;
  }

  @Override
  public <T> AsynchronousServerSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
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
    return shim.SocketOptionSupport.supported(shim.SocketOptionSupport.kAsyncServer);
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

  @Override
  public <A> void accept(A attachment, CompletionHandler<AsynchronousSocketChannel, ? super A> handler) {
    if (handler == null) {
      throw new NullPointerException("'handler' is null");
    }
    if (channel.isOpen() && channel.getLocalAddressUnchecked() == null) {
      throw new NotYetBoundException();
    }
    if (!accepting.compareAndSet(false, true)) {
      throw new AcceptPendingException();
    }
    group.execute(new Runnable() {
      public void run() {
        AsynchronousSocketChannel result;
        try {
          if (!channel.isOpen()) {
            throw new ClosedChannelException();
          }
          java.nio.channels.SocketChannel accepted = channel.accept();
          result = new AsynchronousSocketChannelImpl(provider(), group, (SocketChannelImpl) accepted);
        } catch (Throwable t) {
          accepting.set(false);
          Throwable cause = t;
          if (!channel.isOpen() && !(t instanceof ClosedChannelException)) {
            cause = new AsynchronousCloseException();
          }
          handler.failed(cause, attachment);
          return;
        }
        accepting.set(false);
        handler.completed(result, attachment);
      }
    });
  }

  @Override
  public Future<AsynchronousSocketChannel> accept() {
    CompletableFuture<AsynchronousSocketChannel> future = new CompletableFuture<AsynchronousSocketChannel>();
    accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
      public void completed(AsynchronousSocketChannel result, Object a) {
        future.complete(result);
      }

      public void failed(Throwable exc, Object a) {
        future.completeExceptionally(exc);
      }
    });
    return future;
  }
}
