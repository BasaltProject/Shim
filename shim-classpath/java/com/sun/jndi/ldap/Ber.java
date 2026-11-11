/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.ldap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Minimal BER encoder and decoder for the LDAP message subset. */
final class Ber {
  private Ber() {
  }

  static byte[] tlv(int tag, byte[]... parts) {
    int n = 0;
    for (byte[] p : parts) {
      n += p.length;
    }
    ByteArrayOutputStream o = new ByteArrayOutputStream(n + 6);
    o.write(tag);
    if (n < 128) {
      o.write(n);
    } else if (n < 256) {
      o.write(0x81);
      o.write(n);
    } else if (n < 65536) {
      o.write(0x82);
      o.write(n >> 8);
      o.write(n);
    } else {
      o.write(0x83);
      o.write(n >> 16);
      o.write(n >> 8);
      o.write(n);
    }
    for (byte[] p : parts) {
      o.write(p, 0, p.length);
    }
    return o.toByteArray();
  }

  static byte[] integer(int tag, int v) {
    byte[] raw = {(byte) (v >> 24), (byte) (v >> 16), (byte) (v >> 8), (byte) v};
    int s = 0;
    while (s < 3 && ((raw[s] == 0 && (raw[s + 1] & 0x80) == 0) || (raw[s] == -1 && (raw[s + 1] & 0x80) != 0))) {
      s++;
    }
    return tlv(tag, java.util.Arrays.copyOfRange(raw, s, 4));
  }

  static byte[] bool(boolean b) {
    return tlv(0x01, new byte[] {(byte) (b ? 0xff : 0)});
  }

  static byte[] octets(int tag, byte[] b) {
    return tlv(tag, b);
  }

  static byte[] str(int tag, String s) {
    return tlv(tag, s.getBytes(StandardCharsets.UTF_8));
  }

  static final class Reader {
    private final byte[] b;
    private int pos;
    private final int end;
    int lastTag;

    Reader(byte[] b, int off, int end) {
      this.b = b;
      this.pos = off;
      this.end = end;
    }

    Reader(byte[] b) {
      this(b, 0, b.length);
    }

    boolean more() {
      return pos < end;
    }

    int peek() throws IOException {
      if (pos >= end) {
        throw new IOException("BER: unexpected end");
      }
      return b[pos] & 0xff;
    }

    private int[] header() throws IOException {
      lastTag = peek();
      int p = pos + 1;
      if (p >= end) {
        throw new IOException("BER: truncated");
      }
      int l = b[p++] & 0xff;
      if (l >= 0x80) {
        int n = l & 0x7f;
        if (n == 0 || n > 4 || p + n > end) {
          throw new IOException("BER: bad length");
        }
        l = 0;
        for (int i = 0; i < n; i++) {
          l = (l << 8) | (b[p++] & 0xff);
        }
      }
      if (l < 0 || p + l > end) {
        throw new IOException("BER: truncated");
      }
      return new int[] {p, l};
    }

    Reader seq() throws IOException {
      int[] h = header();
      pos = h[0] + h[1];
      return new Reader(b, h[0], h[0] + h[1]);
    }

    byte[] raw() throws IOException {
      int[] h = header();
      pos = h[0] + h[1];
      return java.util.Arrays.copyOfRange(b, h[0], h[0] + h[1]);
    }

    String str() throws IOException {
      return new String(raw(), StandardCharsets.UTF_8);
    }

    int intv() throws IOException {
      byte[] r = raw();
      int v = r.length > 0 && r[0] < 0 ? -1 : 0;
      for (byte x : r) {
        v = (v << 8) | (x & 0xff);
      }
      return v;
    }
  }
}
