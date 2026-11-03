/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;
import com.sun.nio.sctp.SctpSocketOption;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.Set;
import shim.NetHandle;
import shim.NetNative;
import shim.SctpNative;

final class SctpServerChannelImpl extends SctpServerChannel implements SelectorImpl.HasHandle {
  private final NetHandle net = new NetHandle(false);
  private final Object stateLock = new Object();
  private boolean bound;

  SctpServerChannelImpl(SelectorProvider provider) throws IOException {
    super(provider);
    net.adopt(SctpNative.socket(false));
  }

  @Override
  public long rawHandle() {
    return net.peek();
  }

  private void ensureOpen() throws ClosedChannelException {
    if (!isOpen()) {
      throw new ClosedChannelException();
    }
  }

  @Override
  public SctpChannel accept() throws IOException {
    synchronized (stateLock) {
      ensureOpen();
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
      return new SctpChannelImpl(provider(), client, 0);
    } finally {
      net.end();
    }
  }

  @Override
  public SctpServerChannel bind(SocketAddress local, int backlog) throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      if (bound) {
        throw new AlreadyBoundException();
      }
      InetSocketAddress isa = local == null ? new InetSocketAddress(0) : SocketChannelImpl.checkAddress(local);
      long h = net.begin();
      try {
        NetNative.setOption(h, NetNative.OPT_REUSEADDR, 1);
        NetNative.bind(h, isa.getAddress().getAddress(), isa.getPort());
        NetNative.listen(h, backlog < 1 ? 50 : backlog);
      } finally {
        net.end();
      }
      bound = true;
    }
    return this;
  }

  private int port(long h) {
    int[] port = new int[1];
    NetNative.sockName(h, false, port);
    return port[0];
  }

  @Override
  public SctpServerChannel bindAddress(InetAddress address) throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      if (!bound) {
        throw new NotYetBoundException();
      }
      long h = net.begin();
      try {
        SctpCommon.bindAddress(h, address, port(h), true);
      } finally {
        net.end();
      }
    }
    return this;
  }

  @Override
  public SctpServerChannel unbindAddress(InetAddress address) throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      if (!bound) {
        throw new NotYetBoundException();
      }
      long h = net.begin();
      try {
        SctpCommon.bindAddress(h, address, port(h), false);
      } catch (IOException e) {
        throw new com.sun.nio.sctp.IllegalUnbindException(e.getMessage());
      } finally {
        net.end();
      }
    }
    return this;
  }

  @Override
  public Set<SocketAddress> getAllLocalAddresses() throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      if (!bound) {
        return Collections.emptySet();
      }
      long h = net.begin();
      try {
        return SctpCommon.addresses(h, 0, false);
      } finally {
        net.end();
      }
    }
  }

  @Override
  public <T> T getOption(SctpSocketOption<T> name) throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      long h = net.begin();
      try {
        return SctpCommon.getOption(h, name, 0);
      } finally {
        net.end();
      }
    }
  }

  @Override
  public <T> SctpServerChannel setOption(SctpSocketOption<T> name, T value) throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      long h = net.begin();
      try {
        SctpCommon.setOption(h, name, value, 0);
      } finally {
        net.end();
      }
    }
    return this;
  }

  @Override
  public Set<SctpSocketOption<?>> supportedOptions() {
    return SctpCommon.supported();
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
}
