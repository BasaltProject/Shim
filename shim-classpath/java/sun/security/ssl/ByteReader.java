/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

final class ByteReader {
  private final byte[] buf;
  private int pos;
  private final int end;

  ByteReader(byte[] buf) {
    this(buf, 0, buf.length);
  }

  ByteReader(byte[] buf, int off, int len) {
    this.buf = buf;
    this.pos = off;
    this.end = off + len;
  }

  int remaining() {
    return end - pos;
  }

  boolean hasRemaining() {
    return pos < end;
  }

  private void need(int n) {
    if (n < 0 || end - pos < n) {
      throw new DecodeException();
    }
  }

  int u8() {
    need(1);
    return buf[pos++] & 0xff;
  }

  int u16() {
    need(2);
    int v = ((buf[pos] & 0xff) << 8) | (buf[pos + 1] & 0xff);
    pos += 2;
    return v;
  }

  int u24() {
    need(3);
    int v = ((buf[pos] & 0xff) << 16) | ((buf[pos + 1] & 0xff) << 8) | (buf[pos + 2] & 0xff);
    pos += 3;
    return v;
  }

  long u32() {
    need(4);
    long v = ((buf[pos] & 0xffL) << 24) | ((buf[pos + 1] & 0xffL) << 16) | ((buf[pos + 2] & 0xffL) << 8)
        | (buf[pos + 3] & 0xffL);
    pos += 4;
    return v;
  }

  byte[] bytes(int n) {
    need(n);
    byte[] out = new byte[n];
    System.arraycopy(buf, pos, out, 0, n);
    pos += n;
    return out;
  }

  byte[] vec8() {
    return bytes(u8());
  }

  byte[] vec16() {
    return bytes(u16());
  }

  byte[] vec24() {
    return bytes(u24());
  }

  ByteReader sub(int n) {
    need(n);
    ByteReader r = new ByteReader(buf, pos, n);
    pos += n;
    return r;
  }

  ByteReader sub24() {
    return sub(u24());
  }

  ByteReader sub8() {
    return sub(u8());
  }

  ByteReader sub16() {
    return sub(u16());
  }

  void skip(int n) {
    need(n);
    pos += n;
  }

  static final class DecodeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    DecodeException() {
      super("Not enough data", null, false, false);
    }
  }
}
