/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.util;

import java.io.IOException;
import java.math.BigInteger;

public class DerInputStream {
  private final byte[] buf;
  private int pos;
  private final int end;

  public DerInputStream(byte[] data) {
    this(data, 0, data.length);
  }

  public DerInputStream(byte[] data, int offset, int length) {
    this.buf = data;
    this.pos = offset;
    this.end = offset + length;
  }

  public int available() {
    return end - pos;
  }

  public int peekByte() throws IOException {
    if (pos >= end) {
      throw new IOException("Short read of DER length");
    }
    return buf[pos] & 0xff;
  }

  public DerValue getDerValue() throws IOException {
    if (pos >= end) {
      throw new IOException("Short read of DER value");
    }
    byte tag = buf[pos++];
    if ((tag & 0x1f) == 0x1f) {
      throw new IOException("Indefinite or high tag numbers are not supported");
    }
    if (pos >= end) {
      throw new IOException("Short read of DER length");
    }
    int lenByte = buf[pos++] & 0xff;
    int length;
    if (lenByte < 0x80) {
      length = lenByte;
    } else if (lenByte == 0x80) {
      throw new IOException("Indefinite length encoding not supported");
    } else {
      int n = lenByte & 0x7f;
      if (n > 4) {
        throw new IOException("DerInputStream.getLength(): lengthTag=" + n + ", too big.");
      }
      if (pos + n > end) {
        throw new IOException("Short read of DER length");
      }
      long l = 0;
      for (int i = 0; i < n; i++) {
        l = (l << 8) | (buf[pos++] & 0xff);
      }
      if (l > Integer.MAX_VALUE || l < 0) {
        throw new IOException("DerInputStream.getLength(): length too big");
      }
      if (n == 1 && l < 0x80 || (n > 1 && (l >> (8 * (n - 1))) == 0)) {
        throw new IOException("Redundant length bytes found");
      }
      length = (int) l;
    }
    if (length > end - pos) {
      throw new java.io.EOFException("not enough content");
    }
    byte[] data = new byte[length];
    System.arraycopy(buf, pos, data, 0, length);
    pos += length;
    return new DerValue(tag, data, true);
  }

  public DerValue[] getSequence(int startLen) throws IOException {
    DerValue seq = getDerValue();
    if (seq.tag != DerValue.tag_Sequence) {
      throw new IOException("Sequence tag error");
    }
    return readAll(seq.data());
  }

  public DerValue[] getSet(int startLen) throws IOException {
    DerValue set = getDerValue();
    if (set.tag != DerValue.tag_Set) {
      throw new IOException("Set tag error");
    }
    return readAll(set.data());
  }

  private static DerValue[] readAll(DerInputStream in) throws IOException {
    java.util.List<DerValue> list = new java.util.ArrayList<DerValue>();
    while (in.available() > 0) {
      list.add(in.getDerValue());
    }
    return list.toArray(new DerValue[0]);
  }

  public BigInteger getBigInteger() throws IOException {
    return getDerValue().getBigInteger();
  }

  public int getInteger() throws IOException {
    return getDerValue().getInteger();
  }

  public byte[] getOctetString() throws IOException {
    return getDerValue().getOctetString();
  }

  public ObjectIdentifier getOID() throws IOException {
    return getDerValue().getOID();
  }
}
