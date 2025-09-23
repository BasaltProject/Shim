/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.io.IOException;
import java.nio.ByteBuffer;
import shim.NetNative;

final class ChannelIo {
  private static final int kChunk = 1 << 16;

  private ChannelIo() { }

  static int read(long handle, ByteBuffer dst, boolean blocking) throws IOException {
    int remaining = dst.remaining();
    if (remaining == 0) {
      return 0;
    }
    int len = Math.min(remaining, kChunk);
    byte[] tmp = new byte[len];
    int n = blocking ? NetNative.read(handle, tmp, 0, len, 0) : NetNative.readNb(handle, tmp, 0, len);
    if (n > 0) {
      dst.put(tmp, 0, n);
    }
    return n;
  }

  static long read(long handle, ByteBuffer[] dsts, int offset, int length, boolean blocking) throws IOException {
    if (offset < 0 || length < 0 || offset > dsts.length - length) {
      throw new IndexOutOfBoundsException();
    }
    long total = 0;
    for (int i = offset; i < offset + length; i++) {
      if (!dsts[i].hasRemaining()) {
        continue;
      }
      int n = read(handle, dsts[i], blocking);
      if (n < 0) {
        return total == 0 ? -1 : total;
      }
      total += n;
      if (dsts[i].hasRemaining()) {
        break;
      }
    }
    return total;
  }

  static int write(long handle, ByteBuffer src, boolean blocking) throws IOException {
    int total = 0;
    while (src.hasRemaining()) {
      int len = Math.min(src.remaining(), kChunk);
      byte[] tmp = new byte[len];
      int position = src.position();
      src.get(tmp);
      int n;
      if (blocking) {
        NetNative.write(handle, tmp, 0, len);
        n = len;
      } else {
        n = NetNative.writeNb(handle, tmp, 0, len);
      }
      src.position(position + n);
      total += n;
      if (n < len) {
        break;
      }
    }
    return total;
  }

  static long write(long handle, ByteBuffer[] srcs, int offset, int length, boolean blocking) throws IOException {
    if (offset < 0 || length < 0 || offset > srcs.length - length) {
      throw new IndexOutOfBoundsException();
    }
    long total = 0;
    for (int i = offset; i < offset + length; i++) {
      if (!srcs[i].hasRemaining()) {
        continue;
      }
      total += write(handle, srcs[i], blocking);
      if (srcs[i].hasRemaining()) {
        break;
      }
    }
    return total;
  }
}
