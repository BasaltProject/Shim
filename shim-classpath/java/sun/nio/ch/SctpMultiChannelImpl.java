/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import com.sun.nio.sctp.Association;
import com.sun.nio.sctp.AssociationChangeNotification;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.InvalidStreamException;
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.Notification;
import com.sun.nio.sctp.NotificationHandler;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpMultiChannel;
import com.sun.nio.sctp.SctpSocketOption;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import shim.NetHandle;
import shim.NetNative;
import shim.SctpNative;

final class SctpMultiChannelImpl extends SctpMultiChannel implements SelectorImpl.HasHandle {
  private final NetHandle net = new NetHandle(false);
  private final Object stateLock = new Object();
  private final Map<Integer, Association> associations = new HashMap<Integer, Association>();
  private boolean bound;

  SctpMultiChannelImpl(SelectorProvider provider) throws IOException {
    super(provider);
    net.adopt(SctpNative.socket(true));
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

  private Association lookup(long h, int id) throws IOException {
    synchronized (associations) {
      Association a = associations.get(id);
      if (a == null) {
        a = SctpCommon.association(h, id);
        associations.put(id, a);
      }
      return a;
    }
  }

  @Override
  public Set<Association> associations() throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      if (!bound) {
        throw new NotYetBoundException();
      }
    }
    synchronized (associations) {
      return Collections.unmodifiableSet(new HashSet<Association>(associations.values()));
    }
  }

  @Override
  public SctpMultiChannel bind(SocketAddress local, int backlog) throws IOException {
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
  public SctpMultiChannel bindAddress(InetAddress address) throws IOException {
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
  public SctpMultiChannel unbindAddress(InetAddress address) throws IOException {
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
  public SctpChannel branch(Association association) throws IOException {
    java.util.Objects.requireNonNull(association);
    synchronized (stateLock) {
      ensureOpen();
      long h = net.begin();
      try {
        long fd = SctpNative.peeloff(h, association.associationID());
        synchronized (associations) {
          associations.remove(association.associationID());
        }
        return new SctpChannelImpl(provider(), fd, association.associationID());
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
        return SctpCommon.addresses(h, 0, false);
      } finally {
        net.end();
      }
    }
  }

  @Override
  public Set<SocketAddress> getRemoteAddresses(Association association) throws IOException {
    java.util.Objects.requireNonNull(association);
    synchronized (stateLock) {
      ensureOpen();
      long h = net.begin();
      try {
        return SctpCommon.addresses(h, association.associationID(), true);
      } finally {
        net.end();
      }
    }
  }

  @Override
  public <T> T getOption(SctpSocketOption<T> name, Association association) throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      long h = net.begin();
      try {
        return SctpCommon.getOption(h, name, association == null ? 0 : association.associationID());
      } finally {
        net.end();
      }
    }
  }

  @Override
  public <T> SctpMultiChannel setOption(SctpSocketOption<T> name, T value, Association association)
      throws IOException {
    synchronized (stateLock) {
      ensureOpen();
      long h = net.begin();
      try {
        SctpCommon.setOption(h, name, value, association == null ? 0 : association.associationID());
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
        if (!isOpen()) {
          throw new java.nio.channels.AsynchronousCloseException();
        }
        if (n == -2) {
          return null;
        }
        if (n == -1) {
          continue;
        }
        Association assoc = lookup(h, (int) info[0]);
        if (SctpCommon.isNotification(info)) {
          if (info[5] == 1 && info[6] != 0 && info[6] != 2) {
            synchronized (associations) {
              associations.remove((int) info[0]);
            }
          } else if (info[5] == 1) {
            synchronized (associations) {
              associations.put((int) info[0], new SctpAssociationImpl((int) info[0], (int) info[8], (int) info[7]));
            }
            assoc = lookup(h, (int) info[0]);
          }
          Notification note = SctpCommon.notification(info, addr, tmp, (int) info[8], assoc);
          if (SctpCommon.dispatch(note, handler, attachment) == HandlerResult.RETURN) {
            return null;
          }
          continue;
        }
        dst.put(tmp, 0, n);
        SctpMessageInfoImpl mi = new SctpMessageInfoImpl(assoc, null, 0);
        SctpCommon.fill(mi, assoc, info, addr, n);
        return mi;
      } finally {
        net.end();
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
      ensureOpen();
    }
    Association a = messageInfo.association();
    SocketAddress target = messageInfo.address();
    if (a == null && target == null) {
      throw new IllegalArgumentException("Either the association or the address must be specified");
    }
    if (a != null && messageInfo.streamNumber() >= a.maxOutboundStreams()) {
      throw new InvalidStreamException();
    }
    byte[] raw = null;
    int port = 0;
    if (a == null || target != null) {
      if (target != null) {
        InetSocketAddress isa = SocketChannelImpl.checkAddress(target);
        raw = isa.getAddress().getAddress();
        port = isa.getPort();
      }
    }
    int len = src.remaining();
    byte[] tmp = new byte[len];
    src.duplicate().get(tmp);
    int n;
    long h = net.begin();
    try {
      n = SctpNative.send(h, tmp, 0, len, a == null ? 0 : a.associationID(), messageInfo.streamNumber(),
          messageInfo.payloadProtocolID(), messageInfo.isUnordered() ? 1 : 0, messageInfo.timeToLive(), raw, port,
          !isBlocking());
    } finally {
      net.end();
    }
    if (n < 0) {
      return 0;
    }
    src.position(src.position() + n);
    return n;
  }

  @Override
  public SctpMultiChannel shutdown(Association association) throws IOException {
    java.util.Objects.requireNonNull(association);
    synchronized (stateLock) {
      ensureOpen();
      long h = net.begin();
      try {
        SctpNative.send(h, new byte[0], 0, 0, association.associationID(), 0, 0, 0x200, 0, null, 0, false);
      } finally {
        net.end();
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
