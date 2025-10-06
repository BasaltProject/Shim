/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.util;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

public class DerOutputStream {
  private final ByteArrayOutputStream out = new ByteArrayOutputStream();

  public DerOutputStream() {
  }

  public void write(byte tag, byte[] data) {
    out.write(tag);
    writeLength(data.length);
    out.write(data, 0, data.length);
  }

  public void write(byte tag, DerOutputStream inner) {
    write(tag, inner.toByteArray());
  }

  private void writeLength(int length) {
    if (length < 128) {
      out.write(length);
    } else if (length < 256) {
      out.write(0x81);
      out.write(length);
    } else if (length < 65536) {
      out.write(0x82);
      out.write(length >> 8);
      out.write(length);
    } else if (length < (1 << 24)) {
      out.write(0x83);
      out.write(length >> 16);
      out.write(length >> 8);
      out.write(length);
    } else {
      out.write(0x84);
      out.write(length >> 24);
      out.write(length >> 16);
      out.write(length >> 8);
      out.write(length);
    }
  }

  public void putDerValue(DerValue v) {
    write(v.tag, v.rawData());
  }

  public void putInteger(BigInteger i) {
    write(DerValue.tag_Integer, i.toByteArray());
  }

  public void putInteger(int i) {
    putInteger(BigInteger.valueOf(i));
  }

  public void putOctetString(byte[] bytes) {
    write(DerValue.tag_OctetString, bytes);
  }

  public void putOID(ObjectIdentifier oid) {
    write(DerValue.tag_ObjectId, oid.encode());
  }

  public void putNull() {
    write(DerValue.tag_Null, new byte[0]);
  }

  public void putBitString(byte[] bits) {
    byte[] data = new byte[bits.length + 1];
    System.arraycopy(bits, 0, data, 1, bits.length);
    write(DerValue.tag_BitString, data);
  }

  public void putSequence(DerValue... values) {
    DerOutputStream inner = new DerOutputStream();
    for (DerValue v : values) {
      inner.putDerValue(v);
    }
    write(DerValue.tag_Sequence, inner);
  }

  public void write(byte[] raw) {
    out.write(raw, 0, raw.length);
  }

  public int size() {
    return out.size();
  }

  public byte[] toByteArray() {
    return out.toByteArray();
  }
}
