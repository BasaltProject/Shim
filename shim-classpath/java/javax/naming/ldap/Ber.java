/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

final class Ber {
  static final int INTEGER = 0x02;
  static final int BOOLEAN = 0x01;
  static final int OCTET_STRING = 0x04;
  static final int ENUMERATED = 0x0a;
  static final int SEQUENCE = 0x30;

  private final byte[] buf;
  private int pos;
  private final int end;

  Ber(byte[] buf) {
    this(buf, 0, buf.length);
  }

  Ber(byte[] buf, int off, int len) {
    this.buf = buf;
    this.pos = off;
    this.end = off + len;
  }

  static void writeLength(ByteArrayOutputStream out, int len) {
    if (len < 128) {
      out.write(len);
    } else if (len < 256) {
      out.write(0x81);
      out.write(len);
    } else if (len < 65536) {
      out.write(0x82);
      out.write(len >> 8);
      out.write(len);
    } else {
      out.write(0x83);
      out.write(len >> 16);
      out.write(len >> 8);
      out.write(len);
    }
  }

  static byte[] tlv(int tag, byte[] content) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(tag);
    writeLength(out, content.length);
    out.write(content, 0, content.length);
    return out.toByteArray();
  }

  static byte[] integer(int tag, int value) {
    byte[] raw = new byte[] {(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
    int start = 0;
    while (start < 3 && ((raw[start] == 0 && (raw[start + 1] & 0x80) == 0)
        || (raw[start] == (byte) 0xff && (raw[start + 1] & 0x80) != 0))) {
      start++;
    }
    byte[] c = new byte[4 - start];
    System.arraycopy(raw, start, c, 0, c.length);
    return tlv(tag, c);
  }

  static byte[] concat(byte[]... parts) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    for (byte[] p : parts) {
      out.write(p, 0, p.length);
    }
    return out.toByteArray();
  }

  int peekTag() throws IOException {
    if (pos >= end) {
      throw new IOException("BER: unexpected end");
    }
    return buf[pos] & 0xff;
  }

  boolean hasMore() {
    return pos < end;
  }

  private int readLength() throws IOException {
    if (pos >= end) {
      throw new IOException("BER: unexpected end");
    }
    int b = buf[pos++] & 0xff;
    if (b < 128) {
      return b;
    }
    int n = b & 0x7f;
    if (n == 0 || n > 4 || pos + n > end) {
      throw new IOException("BER: bad length");
    }
    int len = 0;
    for (int i = 0; i < n; i++) {
      len = (len << 8) | (buf[pos++] & 0xff);
    }
    return len;
  }

  Ber enter(int tag) throws IOException {
    int t = peekTag();
    if (t != tag) {
      throw new IOException("BER: expected tag " + tag + " but found " + t);
    }
    pos++;
    int len = readLength();
    if (pos + len > end) {
      throw new IOException("BER: truncated");
    }
    Ber inner = new Ber(buf, pos, len);
    pos += len;
    return inner;
  }

  int readInt(int tag) throws IOException {
    Ber in = enter(tag);
    int v = 0;
    boolean first = true;
    while (in.pos < in.end) {
      int b = in.buf[in.pos++];
      if (first && b < 0) {
        v = -1;
      }
      first = false;
      v = (v << 8) | (b & 0xff);
    }
    return v;
  }

  byte[] readBytes(int tag) throws IOException {
    Ber in = enter(tag);
    byte[] out = new byte[in.end - in.pos];
    System.arraycopy(in.buf, in.pos, out, 0, out.length);
    return out;
  }

  boolean readBoolean(int tag) throws IOException {
    byte[] b = readBytes(tag);
    return b.length > 0 && b[0] != 0;
  }
}
