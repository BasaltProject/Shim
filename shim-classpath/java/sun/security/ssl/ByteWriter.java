/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.util.Arrays;

final class ByteWriter {
  private byte[] buf = new byte[256];
  private int len;
  private final int[] marks = new int[8];
  private final int[] markWidth = new int[8];
  private int depth;

  private void ensure(int more) {
    if (len + more > buf.length) {
      buf = Arrays.copyOf(buf, Math.max(buf.length * 2, len + more));
    }
  }

  ByteWriter u8(int v) {
    ensure(1);
    buf[len++] = (byte) v;
    return this;
  }

  ByteWriter u16(int v) {
    ensure(2);
    buf[len++] = (byte) (v >> 8);
    buf[len++] = (byte) v;
    return this;
  }

  ByteWriter u24(int v) {
    ensure(3);
    buf[len++] = (byte) (v >> 16);
    buf[len++] = (byte) (v >> 8);
    buf[len++] = (byte) v;
    return this;
  }

  ByteWriter u32(long v) {
    ensure(4);
    buf[len++] = (byte) (v >> 24);
    buf[len++] = (byte) (v >> 16);
    buf[len++] = (byte) (v >> 8);
    buf[len++] = (byte) v;
    return this;
  }

  ByteWriter u64(long v) {
    u32(v >>> 32);
    return u32(v & 0xffffffffL);
  }

  ByteWriter bytes(byte[] b) {
    return bytes(b, 0, b.length);
  }

  ByteWriter bytes(byte[] b, int off, int n) {
    ensure(n);
    System.arraycopy(b, off, buf, len, n);
    len += n;
    return this;
  }

  ByteWriter vec8(byte[] b) {
    u8(b.length);
    return bytes(b);
  }

  ByteWriter vec16(byte[] b) {
    u16(b.length);
    return bytes(b);
  }

  ByteWriter vec24(byte[] b) {
    u24(b.length);
    return bytes(b);
  }

  ByteWriter begin(int width) {
    marks[depth] = len;
    markWidth[depth] = width;
    depth++;
    for (int i = 0; i < width; i++) {
      u8(0);
    }
    return this;
  }

  ByteWriter end() {
    depth--;
    int start = marks[depth];
    int width = markWidth[depth];
    int n = len - start - width;
    for (int i = 0; i < width; i++) {
      buf[start + i] = (byte) (n >> (8 * (width - 1 - i)));
    }
    return this;
  }

  void reset() {
    len = 0;
    depth = 0;
  }

  int size() {
    return len;
  }

  byte[] toByteArray() {
    return Arrays.copyOf(buf, len);
  }
}
