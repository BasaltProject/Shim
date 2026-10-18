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

public class DerValue {
  public static final byte tag_Boolean = 0x01;
  public static final byte tag_Integer = 0x02;
  public static final byte tag_BitString = 0x03;
  public static final byte tag_OctetString = 0x04;
  public static final byte tag_Null = 0x05;
  public static final byte tag_ObjectId = 0x06;
  public static final byte tag_Enumerated = 0x0A;
  public static final byte tag_UTF8String = 0x0C;
  public static final byte tag_PrintableString = 0x13;
  public static final byte tag_T61String = 0x14;
  public static final byte tag_IA5String = 0x16;
  public static final byte tag_UtcTime = 0x17;
  public static final byte tag_GeneralizedTime = 0x18;
  public static final byte tag_Sequence = 0x30;
  public static final byte tag_SequenceOf = 0x30;
  public static final byte tag_Set = 0x31;
  public static final byte tag_SetOf = 0x31;

  public byte tag;
  private final byte[] data;

  public DerValue(byte tag, byte[] data) {
    this.tag = tag;
    this.data = data.clone();
  }

  public DerValue(byte[] encoded) throws IOException {
    if (encoded.length == 0) {
      throw new IOException("Too short");
    }
    DerInputStream in = new DerInputStream(encoded);
    DerValue v = in.getDerValue();
    if (in.available() != 0) {
      throw new IOException("extra data at the end");
    }
    this.tag = v.tag;
    this.data = v.data;
  }

  DerValue(byte tag, byte[] data, boolean noCopy) {
    this.tag = tag;
    this.data = data;
  }

  public DerValue(String value) {
    this(tag_UTF8String, value.getBytes(java.nio.charset.StandardCharsets.UTF_8), true);
  }

  public static DerValue integer(BigInteger value) {
    return new DerValue(tag_Integer, value.toByteArray(), true);
  }

  public static DerValue octetString(byte[] bytes) {
    return new DerValue(tag_OctetString, bytes.clone(), true);
  }

  public static DerValue oid(ObjectIdentifier oid) {
    return new DerValue(tag_ObjectId, oid.encode(), true);
  }

  public static DerValue nullValue() {
    return new DerValue(tag_Null, new byte[0], true);
  }

  public static DerValue sequence(DerValue... values) {
    DerOutputStream out = new DerOutputStream();
    for (DerValue v : values) {
      out.putDerValue(v);
    }
    return new DerValue(tag_Sequence, out.toByteArray(), true);
  }

  public byte getTag() {
    return tag;
  }

  public boolean isConstructed() {
    return (tag & 0x20) != 0;
  }

  public boolean isContextSpecific(byte cntxtTag) {
    return (tag & 0xc0) == 0x80 && (tag & 0x1f) == cntxtTag;
  }

  public boolean isContextSpecific() {
    return (tag & 0xc0) == 0x80;
  }

  public int length() {
    return data.length;
  }

  public byte[] getDataBytes() {
    return data.clone();
  }

  byte[] rawData() {
    return data;
  }

  public DerInputStream data() {
    return new DerInputStream(data);
  }

  public byte[] toByteArray() throws IOException {
    DerOutputStream out = new DerOutputStream();
    out.putDerValue(this);
    return out.toByteArray();
  }

  public byte[] getOctetString() throws IOException {
    if (tag != tag_OctetString) {
      throw new IOException("DerValue.getOctetString, not an Octet String: " + tag);
    }
    return data.clone();
  }

  public BigInteger getBigInteger() throws IOException {
    if (tag != tag_Integer && tag != tag_Enumerated) {
      throw new IOException("DerValue.getBigInteger, not an int " + tag);
    }
    if (data.length == 0) {
      throw new IOException("Invalid encoding: zero length Int value");
    }
    return new BigInteger(data);
  }

  public BigInteger getPositiveBigInteger() throws IOException {
    return new BigInteger(1, getBigInteger().toByteArray());
  }

  public int getInteger() throws IOException {
    BigInteger v = getBigInteger();
    if (v.bitLength() > 31) {
      throw new IOException("Integer too big");
    }
    return v.intValue();
  }

  public ObjectIdentifier getOID() throws IOException {
    if (tag != tag_ObjectId) {
      throw new IOException("DerValue.getOID, not an OID " + tag);
    }
    return ObjectIdentifier.decode(data);
  }

  public byte[] getBitString() throws IOException {
    if (tag != tag_BitString) {
      throw new IOException("DerValue.getBitString, not a bit string " + tag);
    }
    if (data.length == 0) {
      throw new IOException("Invalid encoding: empty bit string");
    }
    return java.util.Arrays.copyOfRange(data, 1, data.length);
  }

  public int getUnusedBits() {
    return data.length == 0 ? 0 : data[0] & 0xff;
  }

  public boolean getBoolean() throws IOException {
    if (tag != tag_Boolean || data.length != 1) {
      throw new IOException("DerValue.getBoolean, not a boolean");
    }
    return data[0] != 0;
  }

  public String getAsString() throws IOException {
    switch (tag) {
    case tag_UTF8String:
      return new String(data, java.nio.charset.StandardCharsets.UTF_8);
    case tag_PrintableString:
    case tag_IA5String:
    case tag_T61String:
    case tag_UtcTime:
    case tag_GeneralizedTime:
      return new String(data, java.nio.charset.StandardCharsets.ISO_8859_1);
    default:
      return null;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DerValue)) {
      return false;
    }
    DerValue other = (DerValue) o;
    return tag == other.tag && java.util.Arrays.equals(data, other.data);
  }

  @Override
  public int hashCode() {
    return tag * 31 + java.util.Arrays.hashCode(data);
  }

  @Override
  public String toString() {
    return "DER[tag=0x" + Integer.toHexString(tag & 0xff) + ", length=" + data.length + "]";
  }
}
