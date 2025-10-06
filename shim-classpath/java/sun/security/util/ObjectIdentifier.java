/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.util;

import java.io.IOException;
import java.io.Serializable;

public final class ObjectIdentifier implements Serializable {
  private static final long serialVersionUID = 8697030238860181294L;

  private final int[] components;
  private final String text;

  public ObjectIdentifier(String oid) throws IOException {
    if (oid == null) {
      throw new IOException("null OID");
    }
    String[] parts = oid.split("\\.");
    if (parts.length < 2) {
      throw new IOException("ObjectIdentifier() -- Invalid format: must be at least two components: " + oid);
    }
    int[] values = new int[parts.length];
    for (int i = 0; i < parts.length; i++) {
      try {
        values[i] = Integer.parseInt(parts[i]);
      } catch (NumberFormatException e) {
        throw new IOException("ObjectIdentifier() -- Invalid format: " + oid, e);
      }
      if (values[i] < 0) {
        throw new IOException("ObjectIdentifier() -- Invalid format: " + oid);
      }
    }
    if (values[0] > 2 || (values[0] < 2 && values[1] > 39)) {
      throw new IOException("ObjectIdentifier() -- Invalid first components: " + oid);
    }
    this.components = values;
    this.text = oid;
  }

  public static ObjectIdentifier of(String oid) {
    try {
      return new ObjectIdentifier(oid);
    } catch (IOException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

  public static ObjectIdentifier decode(byte[] encoded) throws IOException {
    if (encoded.length == 0) {
      throw new IOException("ObjectIdentifier() -- empty encoding");
    }
    StringBuilder sb = new StringBuilder();
    long v = 0;
    boolean first = true;
    for (int i = 0; i < encoded.length; i++) {
      v = (v << 7) | (encoded[i] & 0x7f);
      if (v < 0) {
        throw new IOException("ObjectIdentifier() -- component overflow");
      }
      if ((encoded[i] & 0x80) == 0) {
        if (first) {
          long a = v < 40 ? 0 : v < 80 ? 1 : 2;
          sb.append(a).append('.').append(v - 40 * a);
          first = false;
        } else {
          sb.append('.').append(v);
        }
        v = 0;
      } else if (i == encoded.length - 1) {
        throw new IOException("ObjectIdentifier() -- unterminated component");
      }
    }
    return new ObjectIdentifier(sb.toString());
  }

  public byte[] encode() {
    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
    putComponent(out, (long) components[0] * 40 + components[1]);
    for (int i = 2; i < components.length; i++) {
      putComponent(out, components[i]);
    }
    return out.toByteArray();
  }

  private static void putComponent(java.io.ByteArrayOutputStream out, long value) {
    int n = 1;
    for (long t = value >>> 7; t != 0; t >>>= 7) {
      n++;
    }
    for (int i = n - 1; i >= 0; i--) {
      int b = (int) ((value >>> (7 * i)) & 0x7f);
      out.write(i == 0 ? b : b | 0x80);
    }
  }

  public int[] toIntArray() {
    return components.clone();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ObjectIdentifier && java.util.Arrays.equals(components, ((ObjectIdentifier) obj).components);
  }

  @Override
  public int hashCode() {
    return java.util.Arrays.hashCode(components);
  }

  @Override
  public String toString() {
    return text;
  }
}
