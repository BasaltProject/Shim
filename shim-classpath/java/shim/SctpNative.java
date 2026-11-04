/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.io.IOException;

public final class SctpNative {
  private SctpNative() {
  }

  public static native boolean supported();

  public static native long socket(boolean oneToMany) throws IOException;

  public static native void bindx(long handle, byte[][] addrs, int port, boolean add) throws IOException;

  public static native int send(long handle, byte[] buffer, int off, int len, int assocId, int stream, int ppid,
      int flags, long ttl, byte[] addr, int port, boolean nonblock) throws IOException;

  public static native int recv(long handle, byte[] buffer, int off, int len, boolean nonblock, long[] info,
      byte[][] addr) throws IOException;

  public static native int addresses(long handle, int assocId, boolean peer, byte[][] addrs, int[] ports)
      throws IOException;

  public static native void status(long handle, int assocId, int[] out) throws IOException;

  public static native long peeloff(long handle, int assocId) throws IOException;

  public static native void setOpt(long handle, int id, int a, int b) throws IOException;

  public static native void getOpt(long handle, int id, int[] out) throws IOException;

  public static native void setPrimary(long handle, int assocId, byte[] addr, int port, boolean peerPrimary)
      throws IOException;

  public static native byte[] getPrimary(long handle, int assocId, int[] port) throws IOException;
}
