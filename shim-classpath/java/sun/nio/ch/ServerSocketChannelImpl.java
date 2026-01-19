/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;
import shim.NetHandle;
import shim.NetNative;

final class ServerSocketChannelImpl extends ServerSocketChannel implements SelectorImpl.HasHandle {
  private final NetHandle net;
  private final boolean unix;
  private java.net.UnixDomainSocketAddress unixLocal;
  private final Object stateLock = new Object();
  private boolean bound;
  private InetSocketAddress local;
  private ServerSocket adaptor;

  ServerSocketChannelImpl(SelectorProvider provider) throws IOException {
    this(provider, false);
  }

  ServerSocketChannelImpl(SelectorProvider provider, boolean unix) throws IOException {
    super(provider);
    this.unix = unix;
    this.net = new NetHandle(false, unix);
    net.begin();
    net.end();
  }

  @Override
  public long rawHandle() {
    return net.peek();
  }

  @Override
  public ServerSocketChannel bind(SocketAddress addr, int backlog) throws IOException {
    synchronized (stateLock) {
      if (!isOpen()) {
        throw new ClosedChannelException();
      }
      if (bound) {
        throw new AlreadyBoundException();
      }
      if (unix) {
        byte[] path = addr == null ? SocketChannelImpl.autobindPath().getBytes(java.nio.charset.StandardCharsets.UTF_8)
            : SocketChannelImpl.unixPathBytes(addr);
        long uh = net.begin();
        try {
          NetNative.bindUnix(uh, path);
          NetNative.listen(uh, backlog < 1 ? 50 : backlog);
          byte[] raw = NetNative.sockNameUnix(uh, false);
          unixLocal = java.net.UnixDomainSocketAddress.of(new String(raw, java.nio.charset.StandardCharsets.UTF_8));
        } finally {
          net.end();
        }
        bound = true;
        return this;
      }
      InetSocketAddress isa = addr == null ? new InetSocketAddress(0) : SocketChannelImpl.checkAddress(addr);
      long h = net.begin();
      try {
        NetNative.setOption(h, NetNative.OPT_REUSEADDR, 1);
        NetNative.bind(h, isa.getAddress().getAddress(), isa.getPort());
        NetNative.listen(h, backlog < 1 ? 50 : backlog);
        int[] port = new int[1];
        byte[] raw = NetNative.sockName(h, false, port);
        local = new InetSocketAddress(SocketChannelImpl.addressOf(raw), port[0]);
      } finally {
        net.end();
      }
      bound = true;
    }
    return this;
  }

  @Override
  public <T> ServerSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (!isOpen()) {
      throw new ClosedChannelException();
    }
    ChannelOptions.set(net, name, value, unix ? ChannelOptions.kUnixServer : ChannelOptions.kServer);
    return this;
  }

  @Override
  public <T> T getOption(SocketOption<T> name) throws IOException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (!isOpen()) {
      throw new ClosedChannelException();
    }
    return ChannelOptions.get(net, name, unix ? ChannelOptions.kUnixServer : ChannelOptions.kServer);
  }

  @Override
  public Set<SocketOption<?>> supportedOptions() {
    return ChannelOptions.supported(unix ? ChannelOptions.kUnixServer : ChannelOptions.kServer);
  }

  @Override
  public ServerSocket socket() {
    if (unix) {
      throw new UnsupportedOperationException("Not supported");
    }
    synchronized (stateLock) {
      if (adaptor == null) {
        try {
          adaptor = new ServerSocketAdaptor(this);
        } catch (IOException e) {
          throw new IllegalStateException(e);
        }
      }
      return adaptor;
    }
  }

  @Override
  public SocketChannel accept() throws IOException {
    synchronized (stateLock) {
      if (!isOpen()) {
        throw new ClosedChannelException();
      }
      if (!bound) {
        throw new NotYetBoundException();
      }
    }
    long h = net.begin();
    try {
      byte[][] peer = new byte[1][];
      int[] peerPort = new int[1];
      long client;
      if (isBlocking()) {
        client = NetNative.accept(h, 0, peer, peerPort);
        if (!isOpen()) {
          NetNative.close(client);
          throw new java.nio.channels.AsynchronousCloseException();
        }
      } else {
        client = NetNative.acceptNb(h, peer, peerPort);
        if (client == -1) {
          return null;
        }
      }
      NetNative.setBlocking(client, true);
      if (unix) {
        return new SocketChannelImpl(provider(), client, true);
      }
      return new SocketChannelImpl(provider(), client, peer[0], peerPort[0]);
    } finally {
      net.end();
    }
  }

  SocketAddress getLocalAddressUnchecked() {
    synchronized (stateLock) {
      return bound ? (unix ? unixLocal : local) : null;
    }
  }

  @Override
  public SocketAddress getLocalAddress() throws IOException {
    synchronized (stateLock) {
      if (!isOpen()) {
        throw new ClosedChannelException();
      }
      return bound ? (unix ? unixLocal : local) : null;
    }
  }

  @Override
  protected void implCloseSelectableChannel() throws IOException {
    net.close();
  }

  @Override
  protected void implConfigureBlocking(boolean block) throws IOException {
    long h = net.begin();
    try {
      NetNative.setBlocking(h, block);
    } finally {
      net.end();
    }
  }

  NetHandle net() {
    return net;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getName());
    sb.append('[');
    if (!isOpen()) {
      sb.append("closed");
    } else {
      synchronized (stateLock) {
        Object shown = unix ? unixLocal : local;
        if (shown == null) {
          sb.append("unbound");
        } else {
          sb.append(shown);
        }
      }
    }
    sb.append(']');
    return sb.toString();
  }
}
