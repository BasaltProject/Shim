/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

final class Buf {
  byte[] data = new byte[256];
  int len;

  private void ensure(int n) {
    if (len + n > data.length) {
      data = Arrays.copyOf(data, Math.max(data.length * 2, len + n));
    }
  }

  void u8(int b) {
    ensure(1);
    data[len++] = (byte) b;
  }

  void bytes(byte[] b, int off, int n) {
    ensure(n);
    System.arraycopy(b, off, data, len, n);
    len += n;
  }

  void varlong(long v) {
    for (int i = 0; i < 8; i++) {
      if ((v & ~0x7fL) == 0) {
        u8((int) v);
        return;
      }
      u8((int) ((v & 0x7f) | 0x80));
      v >>>= 7;
    }
    u8((int) (v & 0xff));
  }

  void i32(int v) {
    u8(v >>> 24);
    u8(v >>> 16);
    u8(v >>> 8);
    u8(v);
  }

  void i64(long v) {
    i32((int) (v >>> 32));
    i32((int) v);
  }

  void str(String s) {
    if (s == null) {
      u8(0);
    } else if (s.isEmpty()) {
      u8(1);
    } else {
      byte[] b = s.getBytes(StandardCharsets.UTF_8);
      u8(3);
      varlong(b.length);
      bytes(b, 0, b.length);
    }
  }

  void putI64(int at, long v) {
    for (int i = 0; i < 8; i++) {
      data[at + i] = (byte) (v >>> (56 - 8 * i));
    }
  }

  void putI32(int at, int v) {
    for (int i = 0; i < 4; i++) {
      data[at + i] = (byte) (v >>> (24 - 8 * i));
    }
  }

  void writeEvent(Buf body) {
    int size = body.len + 4;
    u8((size & 0x7f) | 0x80);
    u8(((size >>> 7) & 0x7f) | 0x80);
    u8(((size >>> 14) & 0x7f) | 0x80);
    u8((size >>> 21) & 0x7f);
    bytes(body.data, 0, body.len);
  }
}
