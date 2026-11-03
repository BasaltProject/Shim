/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import com.sun.nio.sctp.Association;
import com.sun.nio.sctp.InvalidStreamException;
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.Notification;
import com.sun.nio.sctp.NotificationHandler;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpSocketOption;
import com.sun.nio.sctp.SctpStandardSocketOptions;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.NoConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.Set;
import shim.NetHandle;
import shim.NetNative;
import shim.SctpNative;

final class SctpChannelImpl extends SctpChannel implements SelectorImpl.HasHandle {
  private final NetHandle net = new NetHandle(false);
  private final Object stateLock = new Object();
  private boolean bound;
  private boolean connected;
  private boolean pending;
  private boolean shutdown;
  private int assocId;
  private Association association;
  private final Object receiveLock = new Object();
  private final Object sendLock = new Object();

  SctpChannelImpl(SelectorProvider provider) throws IOException {
    super(provider);
    net.adopt(SctpNative.socket(false));
  }

  SctpChannelImpl(SelectorProvider provider, long handle, int assocId) {
    super(provider);
    net.adopt(handle);
    this.connected = true;
    this.bound = true;
    this.assocId = assocId;
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

  private void ensureConnected() throws ClosedChannelException {
    ensureOpen();
    if (!connected) {
      throw new NotYetConnectedException();
    }
  }

  @Override
  public Association association() throws IOException {
    synchronized (stateLock) {
      ensureConnected();
      if (association == null) {
        long h = net.begin();
        try {
          association = SctpCommon.association(h, assocId);
        } finally {
          net.end();
        }
      }
      return association;
    }
  }

  @Override
  public SctpChannel bind(SocketAddress local) throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      if (pending) {
        throw new ConnectionPendingException();
      }
      if (bound) {
        throw new AlreadyBoundException();
      }
      InetSocketAddress isa = local == null ? new InetSocketAddress(0) : SocketChannelImpl.checkAddress(local);
      long h = net.begin();
      try {
        NetNative.bind(h, isa.getAddress().getAddress(), isa.getPort());
      } finally {
        net.end();
      }
      bound = true;
    }
    return this;
  }

  @Override
  public SctpChannel bindAddress(java.net.InetAddress address) throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      if (!bound) {
        throw new java.nio.channels.NotYetBoundException();
      }
      long h = net.begin();
      try {
        SctpCommon.bindAddress(h, address, localPort(h), true);
      } finally {
        net.end();
      }
    }
    return this;
  }

  @Override
  public SctpChannel unbindAddress(java.net.InetAddress address) throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      if (!bound) {
        throw new java.nio.channels.NotYetBoundException();
      }
      long h = net.begin();
      try {
        SctpCommon.bindAddress(h, address, localPort(h), false);
      } catch (IOException e) {
        throw new com.sun.nio.sctp.IllegalUnbindException(e.getMessage());
      } finally {
        net.end();
      }
    }
    return this;
  }

  private static int localPort(long h) {
    int[] port = new int[1];
    NetNative.sockName(h, false, port);
    return port[0];
  }

  @Override
  public boolean connect(SocketAddress remote, int maxOutStreams, int maxInStreams) throws IOException {
    if (maxOutStreams < 0 || maxOutStreams > 65536 || maxInStreams < 0 || maxInStreams > 65536) {
      throw new IllegalArgumentException("Invalid max streams");
    }
    synchronized (stateLock) {
      ensureOpen();
      long h = net.begin();
      try {
        SctpNative.setOpt(h, 2, maxOutStreams, maxInStreams);
      } finally {
        net.end();
      }
    }
    return connect(remote);
  }

  @Override
  public boolean connect(SocketAddress remote) throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      if (connected) {
        throw new AlreadyConnectedException();
      }
      if (pending) {
        throw new ConnectionPendingException();
      }
      InetSocketAddress isa = SocketChannelImpl.checkAddress(remote);
      long h = net.begin();
      try {
        boolean done;
        if (isBlocking()) {
          NetNative.connect(h, isa.getAddress().getAddress(), isa.getPort(), 0);
          done = true;
        } else {
          done = NetNative.connectNb(h, isa.getAddress().getAddress(), isa.getPort());
        }
        bound = true;
        if (done) {
          connected = true;
        } else {
          pending = true;
        }
        return done;
      } catch (IOException e) {
        close();
        throw e;
      } finally {
        net.end();
      }
    }
  }

  @Override
  public boolean isConnectionPending() {
    synchronized (stateLock) {
      return pending;
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
        }
        return done;
      } catch (IOException e) {
        close();
        throw e;
      } finally {
        net.end();
      }
    }
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
        return SctpCommon.addresses(h, assocId, false);
      } finally {
        net.end();
      }
    }
  }

  @Override
  public Set<SocketAddress> getRemoteAddresses() throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      if (!connected) {
        return Collections.emptySet();
      }
      long h = net.begin();
      try {
        return SctpCommon.addresses(h, assocId, true);
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
        return SctpCommon.getOption(h, name, assocId);
      } finally {
        net.end();
      }
    }
  }

  @Override
  public <T> SctpChannel setOption(SctpSocketOption<T> name, T value) throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      long h = net.begin();
      try {
        SctpCommon.setOption(h, name, value, assocId);
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
  public <T> MessageInfo receive(ByteBuffer dst, T attachment, NotificationHandler<T> handler) throws IOException {
    if (dst == null) {
      throw new NullPointerException("dst");
    }
    if (dst.isReadOnly()) {
      throw new IllegalArgumentException("Read-only buffer");
    }
    synchronized (stateLock) {
      ensureConnected();
    }
    synchronized (receiveLock) {
      for (;;) {
        ensureOpen();
        int len = dst.remaining();
        byte[] tmp = new byte[Math.max(len, 1)];
        long[] info = new long[12];
        byte[][] addr = new byte[1][];
        int n;
        long h = net.begin();
        try {
          n = SctpNative.recv(h, tmp, 0, len, !isBlocking(), info, addr);
        } finally {
          net.end();
        }
        if (!isOpen()) {
          throw new java.nio.channels.AsynchronousCloseException();
        }
        if (n == -2) {
          return null;
        }
        if (n == -1) {
          return SctpCommon.endOfStream(association());
        }
        if (SctpCommon.isNotification(info)) {
          Notification note = SctpCommon.notification(info, addr, tmp, (int) info[8], association());
          if (SctpCommon.dispatch(note, handler, attachment) == HandlerResult.RETURN) {
            return null;
          }
          continue;
        }
        dst.put(tmp, 0, n);
        SctpMessageInfoImpl mi = new SctpMessageInfoImpl(association(), null, 0);
        SctpCommon.fill(mi, association(), info, addr, n);
        return mi;
      }
    }
  }

  @Override
  public int send(ByteBuffer src, MessageInfo messageInfo) throws IOException {
    if (src == null) {
      throw new NullPointerException("src");
    }
    if (messageInfo == null) {
      throw new NullPointerException("messageInfo");
    }
    synchronized (stateLock) {
      ensureConnected();
    }
    Association a = association();
    if (messageInfo.streamNumber() < 0 || messageInfo.streamNumber() >= a.maxOutboundStreams()) {
      throw new InvalidStreamException();
    }
    synchronized (sendLock) {
      int len = src.remaining();
      byte[] tmp = new byte[len];
      src.duplicate().get(tmp);
      int flags = messageInfo.isUnordered() ? 1 : 0;
      int n;
      long h = net.begin();
      try {
        n = SctpNative.send(h, tmp, 0, len, assocId, messageInfo.streamNumber(), messageInfo.payloadProtocolID(),
            flags, messageInfo.timeToLive(), null, 0, !isBlocking());
      } finally {
        net.end();
      }
      if (n < 0) {
        return 0;
      }
      src.position(src.position() + n);
      return n;
    }
  }

  @Override
  public SctpChannel shutdown() throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      if (!connected) {
        throw new NotYetConnectedException();
      }
      if (!shutdown) {
        long h = net.begin();
        try {
          NetNative.shutdown(h, 1);
        } finally {
          net.end();
        }
        shutdown = true;
      }
    }
    return this;
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
