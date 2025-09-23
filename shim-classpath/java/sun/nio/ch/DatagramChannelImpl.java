/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ProtocolFamily;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import shim.NetHandle;
import shim.NetNative;

final class DatagramChannelImpl extends DatagramChannel implements SelectorImpl.HasHandle {
  private final NetHandle net = new NetHandle(true);
  private final ProtocolFamily family;
  private final Object stateLock = new Object();
  private boolean bound;
  private boolean connected;
  private InetSocketAddress local;
  private InetSocketAddress remote;
  private final List<MembershipKeyImpl> memberships = new ArrayList<MembershipKeyImpl>();
  private DatagramSocket adaptor;

  DatagramChannelImpl(SelectorProvider provider, ProtocolFamily family) throws IOException {
    super(provider);
    this.family = family;
    net.begin();
    net.end();
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
          local = new InetSocketAddress(SocketChannelImpl.addressOf(raw), port[0]);
        }
      } finally {
        net.end();
      }
    } catch (IOException ignore) { }
  }

  @Override
  public DatagramChannel bind(SocketAddress addr) throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      if (bound) {
        throw new AlreadyBoundException();
      }
      InetSocketAddress isa = addr == null ? new InetSocketAddress(0) : SocketChannelImpl.checkAddress(addr);
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

  private void implicitBind() throws IOException {
    if (!bound) {
      bind(null);
    }
  }

  @Override
  public <T> DatagramChannel setOption(SocketOption<T> name, T value) throws IOException {
    if (name == null) {
      throw new NullPointerException();
    }
    ensureOpen();
    ChannelOptions.set(net, name, value, ChannelOptions.kDatagram);
    return this;
  }

  @Override
  public <T> T getOption(SocketOption<T> name) throws IOException {
    if (name == null) {
      throw new NullPointerException();
    }
    ensureOpen();
    return ChannelOptions.get(net, name, ChannelOptions.kDatagram);
  }

  @Override
  public Set<SocketOption<?>> supportedOptions() {
    return ChannelOptions.supported(ChannelOptions.kDatagram);
  }

  @Override
  public DatagramSocket socket() {
    synchronized (stateLock) {
      if (adaptor == null) {
        adaptor = new DatagramAdaptor(this);
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
  public DatagramChannel connect(SocketAddress sa) throws IOException {
    InetSocketAddress isa = SocketChannelImpl.checkAddress(sa);
    synchronized (stateLock) {
      ensureOpen();
      if (connected) {
        throw new java.nio.channels.AlreadyConnectedException();
      }
      implicitBind();
      long h = net.begin();
      try {
        NetNative.connect(h, isa.getAddress().getAddress(), isa.getPort(), 0);
      } finally {
        net.end();
      }
      remote = isa;
      connected = true;
    }
    return this;
  }

  @Override
  public DatagramChannel disconnect() throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      if (connected) {
        long h = net.begin();
        try {
          NetNative.disconnect(h);
        } finally {
          net.end();
        }
        connected = false;
        remote = null;
      }
    }
    return this;
  }

  @Override
  public SocketAddress getRemoteAddress() throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      return remote;
    }
  }

  @Override
  public SocketAddress getLocalAddress() throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      return bound ? local : null;
    }
  }

  @Override
  public SocketAddress receive(ByteBuffer dst) throws IOException {
    if (dst == null) {
      throw new NullPointerException();
    }
    if (dst.isReadOnly()) {
      throw new IllegalArgumentException("Read-only buffer");
    }
    synchronized (stateLock) {
      ensureOpen();
      implicitBind();
    }
    long h = net.begin();
    try {
      int cap = dst.remaining();
      byte[] tmp = new byte[Math.max(cap, 1)];
      byte[][] src = new byte[1][];
      int[] srcPort = new int[1];
      int n = isBlocking() ? NetNative.recvFrom(h, tmp, 0, cap, 0, src, srcPort, false)
          : NetNative.recvFromNb(h, tmp, 0, cap, src, srcPort);
      if (n < 0) {
        if (!isOpen()) {
          throw new java.nio.channels.AsynchronousCloseException();
        }
        return null;
      }
      dst.put(tmp, 0, Math.min(n, cap));
      return new InetSocketAddress(SocketChannelImpl.addressOf(src[0]), srcPort[0]);
    } finally {
      net.end();
    }
  }

  @Override
  public int send(ByteBuffer src, SocketAddress target) throws IOException {
    if (src == null) {
      throw new NullPointerException();
    }
    InetSocketAddress isa = SocketChannelImpl.checkAddress(target);
    synchronized (stateLock) {
      ensureOpen();
      if (connected && !isa.equals(remote)) {
        throw new java.lang.IllegalArgumentException("Connected address not equal to target address");
      }
      implicitBind();
    }
    long h = net.begin();
    try {
      int len = src.remaining();
      byte[] tmp = new byte[len];
      int position = src.position();
      src.get(tmp);
      int n = isBlocking() ? NetNative.sendTo(h, tmp, 0, len, isa.getAddress().getAddress(), isa.getPort())
          : NetNative.sendToNb(h, tmp, 0, len, isa.getAddress().getAddress(), isa.getPort());
      if (n < 0) {
        src.position(position);
        return 0;
      }
      return n;
    } finally {
      net.end();
    }
  }

  @Override
  public int read(ByteBuffer dst) throws IOException {
    if (dst == null) {
      throw new NullPointerException();
    }
    synchronized (stateLock) {
      ensureOpen();
      if (!connected) {
        throw new NotYetConnectedException();
      }
    }
    long h = net.begin();
    try {
      int cap = dst.remaining();
      byte[] tmp = new byte[Math.max(cap, 1)];
      int n = isBlocking() ? NetNative.recvFrom(h, tmp, 0, cap, 0, null, null, false)
          : NetNative.recvFromNb(h, tmp, 0, cap, null, null);
      if (n < 0) {
        return 0;
      }
      dst.put(tmp, 0, Math.min(n, cap));
      return Math.min(n, cap);
    } finally {
      net.end();
    }
  }

  @Override
  public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
    long total = 0;
    for (int i = offset; i < offset + length; i++) {
      if (dsts[i].hasRemaining()) {
        total += read(dsts[i]);
        break;
      }
    }
    return total;
  }

  @Override
  public int write(ByteBuffer src) throws IOException {
    if (src == null) {
      throw new NullPointerException();
    }
    synchronized (stateLock) {
      ensureOpen();
      if (!connected) {
        throw new NotYetConnectedException();
      }
    }
    long h = net.begin();
    try {
      int len = src.remaining();
      byte[] tmp = new byte[len];
      int position = src.position();
      src.get(tmp);
      int n = isBlocking() ? NetNative.sendTo(h, tmp, 0, len, null, 0) : NetNative.sendToNb(h, tmp, 0, len, null, 0);
      if (n < 0) {
        src.position(position);
        return 0;
      }
      return n;
    } finally {
      net.end();
    }
  }

  @Override
  public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
    long total = 0;
    for (int i = offset; i < offset + length; i++) {
      if (srcs[i].hasRemaining()) {
        total += write(srcs[i]);
      }
    }
    return total;
  }

  @Override
  public MembershipKey join(InetAddress group, NetworkInterface interf) throws IOException {
    if (group == null || interf == null) {
      throw new NullPointerException();
    }
    if (!group.isMulticastAddress()) {
      throw new IllegalArgumentException("Group not a multicast address");
    }
    synchronized (stateLock) {
      ensureOpen();
      long h = net.begin();
      try {
        NetNative.membership(h, group.getAddress(), interf.getIndex(), true);
      } finally {
        net.end();
      }
      MembershipKeyImpl key = new MembershipKeyImpl(this, group, interf);
      memberships.add(key);
      return key;
    }
  }

  @Override
  public MembershipKey join(InetAddress group, NetworkInterface interf, InetAddress source) throws IOException {
    if (source == null) {
      throw new NullPointerException("source address is null");
    }
    throw new UnsupportedOperationException();
  }

  void drop(MembershipKeyImpl key) {
    synchronized (stateLock) {
      memberships.remove(key);
      if (isOpen()) {
        try {
          long h = net.begin();
          try {
            NetNative.membership(h, key.group().getAddress(), key.networkInterface().getIndex(), false);
          } finally {
            net.end();
          }
        } catch (IOException ignore) { }
      }
    }
  }

  @Override
  protected void implCloseSelectableChannel() throws IOException {
    synchronized (stateLock) {
      for (MembershipKeyImpl k : memberships) {
        k.invalidate();
      }
      memberships.clear();
    }
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
        if (local != null) {
          sb.append("local=").append(local);
        }
        if (remote != null) {
          sb.append(" remote=").append(remote);
        }
      }
    }
    sb.append(']');
    return sb.toString();
  }
}
