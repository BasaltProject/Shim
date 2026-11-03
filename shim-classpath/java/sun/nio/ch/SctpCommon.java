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
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.Notification;
import com.sun.nio.sctp.NotificationHandler;
import com.sun.nio.sctp.PeerAddressChangeNotification;
import com.sun.nio.sctp.SctpSocketOption;
import com.sun.nio.sctp.SctpStandardSocketOptions;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import shim.NetNative;
import shim.SctpNative;

final class SctpCommon {
  private static final Set<SctpSocketOption<?>> SUPPORTED;

  static {
    Set<SctpSocketOption<?>> s = new HashSet<SctpSocketOption<?>>();
    s.add(SctpStandardSocketOptions.SCTP_DISABLE_FRAGMENTS);
    s.add(SctpStandardSocketOptions.SCTP_EXPLICIT_COMPLETE);
    s.add(SctpStandardSocketOptions.SCTP_FRAGMENT_INTERLEAVE);
    s.add(SctpStandardSocketOptions.SCTP_INIT_MAXSTREAMS);
    s.add(SctpStandardSocketOptions.SCTP_NODELAY);
    s.add(SctpStandardSocketOptions.SCTP_PRIMARY_ADDR);
    s.add(SctpStandardSocketOptions.SCTP_SET_PEER_PRIMARY_ADDR);
    s.add(SctpStandardSocketOptions.SO_SNDBUF);
    s.add(SctpStandardSocketOptions.SO_RCVBUF);
    s.add(SctpStandardSocketOptions.SO_LINGER);
    SUPPORTED = Collections.unmodifiableSet(s);
  }

  private SctpCommon() {
  }

  static Set<SctpSocketOption<?>> supported() {
    return SUPPORTED;
  }

  static SocketAddress address(byte[] raw, int port) {
    return new InetSocketAddress(SocketChannelImpl.addressOf(raw), port);
  }

  static Set<SocketAddress> addresses(long h, int assocId, boolean peer) throws IOException {
    byte[][] addrs = new byte[64][];
    int[] ports = new int[64];
    int n = SctpNative.addresses(h, assocId, peer, addrs, ports);
    Set<SocketAddress> out = new LinkedHashSet<SocketAddress>();
    for (int i = 0; i < n; i++) {
      out.add(address(addrs[i], ports[i]));
    }
    return Collections.unmodifiableSet(out);
  }

  static Association association(long h, int assocId) throws IOException {
    int[] st = new int[4];
    SctpNative.status(h, assocId, st);
    return new SctpAssociationImpl(st[0], st[1], st[2]);
  }

  @SuppressWarnings("unchecked")
  static <T> void setOption(long h, SctpSocketOption<T> name, T value, int assocId) throws IOException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (!SUPPORTED.contains(name)) {
      throw new UnsupportedOperationException("'" + name + "' not supported");
    }
    if (name == SctpStandardSocketOptions.SCTP_NODELAY) {
      SctpNative.setOpt(h, 1, flag(value), 0);
    } else if (name == SctpStandardSocketOptions.SCTP_DISABLE_FRAGMENTS) {
      SctpNative.setOpt(h, 3, flag(value), 0);
    } else if (name == SctpStandardSocketOptions.SCTP_FRAGMENT_INTERLEAVE) {
      SctpNative.setOpt(h, 4, number(value), 0);
    } else if (name == SctpStandardSocketOptions.SCTP_EXPLICIT_COMPLETE) {
      SctpNative.setOpt(h, 5, flag(value), 0);
    } else if (name == SctpStandardSocketOptions.SCTP_INIT_MAXSTREAMS) {
      SctpStandardSocketOptions.InitMaxStreams ims = (SctpStandardSocketOptions.InitMaxStreams) value;
      if (ims == null) {
        throw new IllegalArgumentException("Invalid value 'null'");
      }
      SctpNative.setOpt(h, 2, ims.maxOutStreams(), ims.maxInStreams());
    } else if (name == SctpStandardSocketOptions.SO_SNDBUF) {
      NetNative.setOption(h, NetNative.OPT_SNDBUF, number(value));
    } else if (name == SctpStandardSocketOptions.SO_RCVBUF) {
      NetNative.setOption(h, NetNative.OPT_RCVBUF, number(value));
    } else if (name == SctpStandardSocketOptions.SO_LINGER) {
      NetNative.setOption(h, NetNative.OPT_LINGER, number(value));
    } else {
      InetSocketAddress isa = SocketChannelImpl.checkAddress((SocketAddress) value);
      SctpNative.setPrimary(h, assocId, isa.getAddress().getAddress(), isa.getPort(),
          name == SctpStandardSocketOptions.SCTP_SET_PEER_PRIMARY_ADDR);
    }
  }

  private static int flag(Object v) {
    if (v == null) {
      throw new IllegalArgumentException("Invalid value 'null'");
    }
    return ((Boolean) v) ? 1 : 0;
  }

  private static int number(Object v) {
    if (v == null) {
      throw new IllegalArgumentException("Invalid value 'null'");
    }
    return (Integer) v;
  }

  @SuppressWarnings("unchecked")
  static <T> T getOption(long h, SctpSocketOption<T> name, int assocId) throws IOException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (!SUPPORTED.contains(name)) {
      throw new UnsupportedOperationException("'" + name + "' not supported");
    }
    int[] out = new int[2];
    if (name == SctpStandardSocketOptions.SCTP_NODELAY) {
      SctpNative.getOpt(h, 1, out);
      return (T) Boolean.valueOf(out[0] != 0);
    } else if (name == SctpStandardSocketOptions.SCTP_DISABLE_FRAGMENTS) {
      SctpNative.getOpt(h, 3, out);
      return (T) Boolean.valueOf(out[0] != 0);
    } else if (name == SctpStandardSocketOptions.SCTP_FRAGMENT_INTERLEAVE) {
      SctpNative.getOpt(h, 4, out);
      return (T) Integer.valueOf(out[0]);
    } else if (name == SctpStandardSocketOptions.SCTP_EXPLICIT_COMPLETE) {
      SctpNative.getOpt(h, 5, out);
      return (T) Boolean.valueOf(out[0] != 0);
    } else if (name == SctpStandardSocketOptions.SCTP_INIT_MAXSTREAMS) {
      SctpNative.getOpt(h, 2, out);
      return (T) SctpStandardSocketOptions.InitMaxStreams.create(out[1], out[0]);
    } else if (name == SctpStandardSocketOptions.SO_SNDBUF) {
      return (T) Integer.valueOf(NetNative.getOption(h, NetNative.OPT_SNDBUF));
    } else if (name == SctpStandardSocketOptions.SO_RCVBUF) {
      return (T) Integer.valueOf(NetNative.getOption(h, NetNative.OPT_RCVBUF));
    } else if (name == SctpStandardSocketOptions.SO_LINGER) {
      return (T) Integer.valueOf(NetNative.getOption(h, NetNative.OPT_LINGER));
    } else if (name == SctpStandardSocketOptions.SCTP_PRIMARY_ADDR) {
      int[] port = new int[1];
      byte[] raw = SctpNative.getPrimary(h, assocId, port);
      return (T) address(raw, port[0]);
    }
    throw new IllegalArgumentException("'" + name + "' can only be set");
  }

  static void bindAddress(long h, InetAddress addr, int port, boolean add) throws IOException {
    SctpNative.bindx(h, new byte[][] {addr.getAddress()}, port, add);
  }

  static AssociationChangeNotification.AssocChangeEvent assocEvent(int state) {
    switch (state) {
      case 0:
        return AssociationChangeNotification.AssocChangeEvent.COMM_UP;
      case 1:
        return AssociationChangeNotification.AssocChangeEvent.COMM_LOST;
      case 2:
        return AssociationChangeNotification.AssocChangeEvent.RESTART;
      case 3:
        return AssociationChangeNotification.AssocChangeEvent.SHUTDOWN;
      default:
        return AssociationChangeNotification.AssocChangeEvent.CANT_START;
    }
  }

  static PeerAddressChangeNotification.AddressChangeEvent addrEvent(int state) {
    PeerAddressChangeNotification.AddressChangeEvent[] v = PeerAddressChangeNotification.AddressChangeEvent.values();
    return v[Math.max(0, Math.min(state, v.length - 1))];
  }

  static Notification notification(long[] info, byte[][] addr, byte[] data, int dataLen, Association assoc) {
    int type = (int) info[5];
    if (type == 1) {
      return new SctpNotifications.AssocChange(assoc, assocEvent((int) info[6]));
    } else if (type == 2) {
      SocketAddress a = addr[0] == null ? null : address(addr[0], (int) info[4]);
      return new SctpNotifications.PeerAddress(assoc, a, addrEvent((int) info[6]));
    } else if (type == 3) {
      ByteBuffer b = ByteBuffer.wrap(data, 0, Math.max(0, dataLen));
      return new SctpNotifications.SendFailed(assoc, null, b, (int) info[6], (int) info[7]);
    } else if (type == 4) {
      return new SctpNotifications.Shutdown(assoc);
    }
    return null;
  }

  static <T> HandlerResult dispatch(Notification n, NotificationHandler<T> handler, T att) {
    if (n == null || handler == null) {
      return HandlerResult.CONTINUE;
    }
    HandlerResult r = handler.handleNotification(n, att);
    return r == null ? HandlerResult.CONTINUE : r;
  }

  static boolean isNotification(long[] info) {
    return (info[3] & 4) != 0;
  }

  static void fill(SctpMessageInfoImpl mi, Association assoc, long[] info, byte[][] addr, int bytes) {
    SocketAddress a = addr[0] == null ? null : address(addr[0], (int) info[4]);
    mi.received(assoc, a, bytes, (int) info[1], (info[3] & 1) != 0, (info[3] & 2) != 0, (int) info[2]);
  }

  static MessageInfo endOfStream(Association assoc) {
    SctpMessageInfoImpl mi = new SctpMessageInfoImpl(assoc, null, 0);
    mi.received(assoc, null, -1, 0, true, false, 0);
    return mi;
  }
}
