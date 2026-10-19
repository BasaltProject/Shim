/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.net.UnixDomainSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.NoConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import shim.NetHandle;
import shim.NetNative;

final class SocketChannelImpl extends SocketChannel implements SelectorImpl.HasHandle {
  private final NetHandle net;
  private final boolean unix;
  private UnixDomainSocketAddress unixRemote;
  private UnixDomainSocketAddress unixLocal;
  private final Object stateLock = new Object();
  private boolean connected;
  private boolean pending;
  private boolean bound;
  private boolean inputShutdown;
  private boolean outputShutdown;
  private InetSocketAddress remote;
  private InetSocketAddress local;
  private Socket adaptor;

  SocketChannelImpl(SelectorProvider provider) throws IOException {
    this(provider, false);
  }

  SocketChannelImpl(SelectorProvider provider, boolean unix) throws IOException {
    super(provider);
    this.unix = unix;
    this.net = new NetHandle(false, unix);
    net.begin();
    net.end();
  }

  SocketChannelImpl(SelectorProvider provider, long handle, boolean unix) throws IOException {
    super(provider);
    this.unix = unix;
    this.net = new NetHandle(false, unix);
    net.adopt(handle);
    connected = true;
    bound = true;
    unixRemote = unixName(true);
    unixLocal = unixName(false);
  }

  private UnixDomainSocketAddress unixName(boolean peer) {
    try {
      long h = net.begin();
      try {
        byte[] raw = NetNative.sockNameUnix(h, peer);
        String path = raw == null ? "" : new String(raw, java.nio.charset.StandardCharsets.UTF_8);
        return UnixDomainSocketAddress.of(path);
      } finally {
        net.end();
      }
    } catch (IOException e) {
      return UnixDomainSocketAddress.of("");
    }
  }

  static byte[] unixPathBytes(SocketAddress sa) {
    if (sa == null) {
      throw new NullPointerException();
    }
    if (!(sa instanceof UnixDomainSocketAddress)) {
      throw new UnsupportedAddressTypeException();
    }
    return ((UnixDomainSocketAddress) sa).getPath().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
  }

  static String autobindPath() {
    String tmp = System.getProperty("java.io.tmpdir", "/tmp");
    return tmp + "/socket_" + Math.abs(new java.util.Random().nextInt());
  }

  SocketChannelImpl(SelectorProvider provider, long handle, byte[] peer, int peerPort) throws IOException {
    super(provider);
    this.unix = false;
    this.net = new NetHandle(false);
    net.adopt(handle);
    connected = true;
    bound = true;
    remote = new InetSocketAddress(addressOf(peer), peerPort);
    refreshLocal();
  }

  static InetAddress addressOf(byte[] raw) {
    try {
      boolean zero = true;
      for (byte b : raw) {
        zero &= b == 0;
      }
      return zero ? InetAddress.getByAddress(new byte[raw.length]) : InetAddress.getByAddress(raw);
    } catch (UnknownHostException e) {
      throw new IllegalStateException(e);
    }
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

  private void refreshLocal() {
    try {
      long h = net.begin();
      try {
        int[] port = new int[1];
        byte[] raw = NetNative.sockName(h, false, port);
        if (raw != null) {
          local = new InetSocketAddress(addressOf(raw), port[0]);
        }
      } finally {
        net.end();
      }
    } catch (IOException ignore) { }
  }

  @Override
  public SocketChannel bind(SocketAddress addr) throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      if (connected || pending) {
        throw new AlreadyConnectedException();
      }
      if (bound) {
        throw new AlreadyBoundException();
      }
      if (unix) {
        byte[] path = addr == null ? autobindPath().getBytes(java.nio.charset.StandardCharsets.UTF_8)
            : unixPathBytes(addr);
        long h = net.begin();
        try {
          NetNative.bindUnix(h, path);
        } finally {
          net.end();
        }
        bound = true;
        unixLocal = unixName(false);
        return this;
      }
      InetSocketAddress isa = addr == null ? new InetSocketAddress(0) : checkAddress(addr);
      long h = net.begin();
      try {
        NetNative.bind(h, isa.getAddress().getAddress(), isa.getPort());
      } finally {
        net.end();
      }
      bound = true;
      refreshLocal();
    }
    return this;
  }

  static InetSocketAddress checkAddress(SocketAddress sa) {
    if (sa == null) {
      throw new NullPointerException();
    }
    if (!(sa instanceof InetSocketAddress)) {
      throw new UnsupportedAddressTypeException();
    }
    InetSocketAddress isa = (InetSocketAddress) sa;
    if (isa.isUnresolved()) {
      throw new UnresolvedAddressException();
    }
    return isa;
  }

  @Override
  public <T> SocketChannel setOption(SocketOption<T> name, T value) throws IOException {
    if (name == null) {
      throw new NullPointerException();
    }
    ensureOpen();
    ChannelOptions.set(net, name, value, unix ? ChannelOptions.kUnixStream : ChannelOptions.kStream);
    return this;
  }

  @Override
  public <T> T getOption(SocketOption<T> name) throws IOException {
    if (name == null) {
      throw new NullPointerException();
    }
    ensureOpen();
    return ChannelOptions.get(net, name, unix ? ChannelOptions.kUnixStream : ChannelOptions.kStream);
  }

  @Override
  public Set<SocketOption<?>> supportedOptions() {
    return ChannelOptions.supported(unix ? ChannelOptions.kUnixStream : ChannelOptions.kStream);
  }

  @Override
  public SocketChannel shutdownInput() throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      if (!connected) {
        throw new NotYetConnectedException();
      }
      if (!inputShutdown) {
        long h = net.begin();
        try {
          NetNative.shutdown(h, 0);
        } finally {
          net.end();
        }
        inputShutdown = true;
      }
    }
    return this;
  }

  @Override
  public SocketChannel shutdownOutput() throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      if (!connected) {
        throw new NotYetConnectedException();
      }
      if (!outputShutdown) {
        long h = net.begin();
        try {
          NetNative.shutdown(h, 1);
        } finally {
          net.end();
        }
        outputShutdown = true;
      }
    }
    return this;
  }

  @Override
  public Socket socket() {
    if (unix) {
      throw new UnsupportedOperationException("Not supported");
    }
    synchronized (stateLock) {
      if (adaptor == null) {
        adaptor = new SocketAdaptor(this);
      }
      return adaptor;
    }
  }

  @Override
  public boolean isConnected() {
    synchronized (stateLock) {
      return connected;
    }
  }

  @Override
  public boolean isConnectionPending() {
    synchronized (stateLock) {
      return pending;
    }
  }

  @Override
  public boolean connect(SocketAddress sa) throws IOException {
    if (unix) {
      return connectUnix(sa);
    }
    InetSocketAddress isa = checkAddress(sa);
    synchronized (stateLock) {
      ensureOpen();
      if (connected) {
        throw new AlreadyConnectedException();
      }
      if (pending) {
        throw new ConnectionPendingException();
      }
      InetAddress target = isa.getAddress();
      if (target.isAnyLocalAddress()) {
        target = target instanceof Inet4Address ? InetAddress.getLoopbackAddress()
            : InetAddress.getByAddress(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1});
      }
      long h = net.begin();
      try {
        boolean done;
        if (isBlocking()) {
          NetNative.connect(h, target.getAddress(), isa.getPort(), 0);
          done = true;
        } else {
          done = NetNative.connectNb(h, target.getAddress(), isa.getPort());
        }
        remote = new InetSocketAddress(target, isa.getPort());
        bound = true;
        if (done) {
          connected = true;
        } else {
          pending = true;
        }
        refreshLocal();
        return done;
      } catch (IOException e) {
        try {
          close();
        } catch (IOException ignore) { }
        throw e;
      } finally {
        net.end();
      }
    }
  }

  private boolean connectUnix(SocketAddress sa) throws IOException {
    byte[] path = unixPathBytes(sa);
    synchronized (stateLock) {
      ensureOpen();
      if (connected) {
        throw new AlreadyConnectedException();
      }
      if (pending) {
        throw new ConnectionPendingException();
      }
      long h = net.begin();
      try {
        boolean done;
        if (isBlocking()) {
          NetNative.connectUnix(h, path);
          done = true;
        } else {
          done = NetNative.connectNbUnix(h, path);
        }
        unixRemote = (UnixDomainSocketAddress) sa;
        bound = true;
        if (done) {
          connected = true;
        } else {
          pending = true;
        }
        unixLocal = unixName(false);
        return done;
      } catch (IOException e) {
        try {
          close();
        } catch (IOException ignore) { }
        throw e;
      } finally {
        net.end();
      }
    }
  }

  @Override
  public boolean finishConnect() throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      if (connected) {
        return true;
      }
      if (!pending) {
        throw new NoConnectionPendingException();
      }
      long h = net.begin();
      try {
        boolean done = NetNative.finishConnect(h, isBlocking() ? -1 : 0);
        if (done) {
          connected = true;
          pending = false;
          if (unix) {
            unixLocal = unixName(false);
          } else {
            refreshLocal();
          }
        }
        return done;
      } catch (IOException e) {
        try {
          close();
        } catch (IOException ignore) { }
        throw e;
      } finally {
        net.end();
      }
    }
  }

  @Override
  public SocketAddress getRemoteAddress() throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      return connected ? (unix ? unixRemote : remote) : null;
    }
  }

  @Override
  public SocketAddress getLocalAddress() throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      return bound ? (unix ? unixLocal : local) : null;
    }
  }

  private void checkReadable() throws IOException {
    ensureOpen();
    if (!connected) {
      throw new NotYetConnectedException();
    }
  }

  @Override
  public int read(ByteBuffer dst) throws IOException {
    if (dst == null) {
      throw new NullPointerException();
    }
    synchronized (stateLock) {
      checkReadable();
      if (inputShutdown) {
        return -1;
      }
    }
    long h = net.begin();
    try {
      int n = ChannelIo.read(h, dst, isBlocking());
      if (n < 0 && !isOpen()) {
        throw new java.nio.channels.AsynchronousCloseException();
      }
      return n;
    } finally {
      net.end();
    }
  }

  int readTimed(ByteBuffer dst, int timeoutMs) throws IOException {
    synchronized (stateLock) {
      checkReadable();
      if (inputShutdown) {
        return -1;
      }
    }
    long h = net.begin();
    try {
      int remaining = dst.remaining();
      if (remaining == 0) {
        return 0;
      }
      byte[] tmp = new byte[Math.min(remaining, 1 << 16)];
      int n = NetNative.read(h, tmp, 0, tmp.length, Math.max(timeoutMs, 0));
      if (n < 0 && !isOpen()) {
        throw new java.nio.channels.AsynchronousCloseException();
      }
      if (n > 0) {
        dst.put(tmp, 0, n);
      }
      return n;
    } finally {
      net.end();
    }
  }

  @Override
  public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
    synchronized (stateLock) {
      checkReadable();
      if (inputShutdown) {
        return -1;
      }
    }
    long h = net.begin();
    try {
      return ChannelIo.read(h, dsts, offset, length, isBlocking());
    } finally {
      net.end();
    }
  }

  @Override
  public int write(ByteBuffer src) throws IOException {
    if (src == null) {
      throw new NullPointerException();
    }
    synchronized (stateLock) {
      checkReadable();
      if (outputShutdown) {
        throw new java.nio.channels.ClosedChannelException();
      }
    }
    long h = net.begin();
    try {
      return ChannelIo.write(h, src, isBlocking());
    } finally {
      net.end();
    }
  }

  @Override
  public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
    synchronized (stateLock) {
      checkReadable();
      if (outputShutdown) {
        throw new java.nio.channels.ClosedChannelException();
      }
    }
    long h = net.begin();
    try {
      return ChannelIo.write(h, srcs, offset, length, isBlocking());
    } finally {
      net.end();
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
    StringBuilder sb = new StringBuilder(getClass().getSuperclass().getName());
    sb.append('[');
    if (!isOpen()) {
      sb.append("closed");
    } else {
      synchronized (stateLock) {
        if (connected) {
          sb.append("connected");
          if (inputShutdown) {
            sb.append(" ishut");
          }
          if (outputShutdown) {
            sb.append(" oshut");
          }
        } else if (pending) {
          sb.append("connection-pending");
        } else {
          sb.append("unconnected");
        }
        Object shownLocal = unix ? unixLocal : local;
        Object shownRemote = unix ? unixRemote : remote;
        if (shownLocal != null) {
          sb.append(" local=").append(shownLocal);
        }
        if (shownRemote != null && connected) {
          sb.append(" remote=").append(shownRemote);
        }
      }
    }
    sb.append(']');
    return sb.toString();
  }
}
