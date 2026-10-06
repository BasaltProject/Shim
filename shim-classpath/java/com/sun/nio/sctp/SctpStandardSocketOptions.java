/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.nio.sctp;

public final class SctpStandardSocketOptions {
  public static final com.sun.nio.sctp.SctpSocketOption<java.lang.Boolean> SCTP_DISABLE_FRAGMENTS = new sun.nio.ch.SctpStdSocketOption<java.lang.Boolean>("sctp_disable_fragments", java.lang.Boolean.class);
  public static final com.sun.nio.sctp.SctpSocketOption<java.lang.Boolean> SCTP_EXPLICIT_COMPLETE = new sun.nio.ch.SctpStdSocketOption<java.lang.Boolean>("sctp_explicit_complete", java.lang.Boolean.class);
  public static final com.sun.nio.sctp.SctpSocketOption<java.lang.Integer> SCTP_FRAGMENT_INTERLEAVE = new sun.nio.ch.SctpStdSocketOption<java.lang.Integer>("sctp_fragment_interleave", java.lang.Integer.class);
  public static final com.sun.nio.sctp.SctpSocketOption<com.sun.nio.sctp.SctpStandardSocketOptions.InitMaxStreams> SCTP_INIT_MAXSTREAMS = new sun.nio.ch.SctpStdSocketOption<SctpStandardSocketOptions.InitMaxStreams>("sctp_init_maxstreams", SctpStandardSocketOptions.InitMaxStreams.class);
  public static final com.sun.nio.sctp.SctpSocketOption<java.lang.Boolean> SCTP_NODELAY = new sun.nio.ch.SctpStdSocketOption<java.lang.Boolean>("sctp_nodelay", java.lang.Boolean.class);
  public static final com.sun.nio.sctp.SctpSocketOption<java.net.SocketAddress> SCTP_PRIMARY_ADDR = new sun.nio.ch.SctpStdSocketOption<java.net.SocketAddress>("sctp_primary_addr", java.net.SocketAddress.class);
  public static final com.sun.nio.sctp.SctpSocketOption<java.net.SocketAddress> SCTP_SET_PEER_PRIMARY_ADDR = new sun.nio.ch.SctpStdSocketOption<java.net.SocketAddress>("sctp_set_peer_primary_addr", java.net.SocketAddress.class);
  public static final com.sun.nio.sctp.SctpSocketOption<java.lang.Integer> SO_SNDBUF = new sun.nio.ch.SctpStdSocketOption<java.lang.Integer>("so_sndbuf", java.lang.Integer.class);
  public static final com.sun.nio.sctp.SctpSocketOption<java.lang.Integer> SO_RCVBUF = new sun.nio.ch.SctpStdSocketOption<java.lang.Integer>("so_rcvbuf", java.lang.Integer.class);
  public static final com.sun.nio.sctp.SctpSocketOption<java.lang.Integer> SO_LINGER = new sun.nio.ch.SctpStdSocketOption<java.lang.Integer>("so_linger", java.lang.Integer.class);

  private SctpStandardSocketOptions() {
  }


  public static class InitMaxStreams {

    private final int maxIn;
    private final int maxOut;

    private InitMaxStreams(int maxIn, int maxOut) {
      this.maxIn = maxIn;
      this.maxOut = maxOut;
    }

    public static com.sun.nio.sctp.SctpStandardSocketOptions.InitMaxStreams create(int maxInStreams, int maxOutStreams) {
      if (maxInStreams < 0 || maxInStreams > 65536) {
        throw new IllegalArgumentException("Invalid maxInStreams value");
      }
      if (maxOutStreams < 0 || maxOutStreams > 65536) {
        throw new IllegalArgumentException("Invalid maxOutStreams value");
      }
      return new InitMaxStreams(maxInStreams, maxOutStreams);
    }
    public boolean equals(java.lang.Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof InitMaxStreams)) {
        return false;
      }
      InitMaxStreams other = (InitMaxStreams) o;
      return maxIn == other.maxIn && maxOut == other.maxOut;
    }
    public int hashCode() {
      return 31 * (31 * 7 + maxIn) + maxOut;
    }
    public int maxInStreams() {
      return maxIn;
    }
    public int maxOutStreams() {
      return maxOut;
    }
    public java.lang.String toString() {
      return super.toString() + "[maxIn:" + maxIn + ", maxOut:" + maxOut + "]";
    }
  }
}
