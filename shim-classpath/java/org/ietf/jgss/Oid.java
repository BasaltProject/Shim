/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.ietf.jgss;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Oid {
  private final String oid;
  private final byte[] derEncoding;

  public Oid(String strOid) throws GSSException {
    try {
      byte[] der = encode(strOid);
      this.oid = strOid;
      this.derEncoding = der;
    } catch (RuntimeException e) {
      throw new GSSException(GSSException.FAILURE, "Improperly formatted Object Identifier String - " + strOid);
    }
  }

  public Oid(InputStream derOid) throws GSSException {
    try {
      int tag = derOid.read();
      int len = derOid.read();
      if (tag != 0x06 || len < 0) {
        throw new IOException("bad tag");
      }
      if (len > 127) {
        int n = len & 0x7f;
        len = 0;
        for (int i = 0; i < n; i++) {
          len = (len << 8) | derOid.read();
        }
      }
      byte[] body = new byte[len];
      int off = 0;
      while (off < len) {
        int r = derOid.read(body, off, len - off);
        if (r < 0) {
          throw new IOException("short");
        }
        off += r;
      }
      this.oid = decode(body);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      out.write(0x06);
      writeLen(out, len);
      out.write(body, 0, len);
      this.derEncoding = out.toByteArray();
    } catch (IOException e) {
      throw new GSSException(GSSException.FAILURE, "Improperly formatted ASN.1 DER encoding for Oid");
    }
  }

  public Oid(byte[] data) throws GSSException {
    this(new ByteArrayInputStream(data));
  }

  static Oid getInstance(String strOid) {
    try {
      return new Oid(strOid);
    } catch (GSSException e) {
      return null;
    }
  }

  private static void writeLen(ByteArrayOutputStream out, int len) {
    if (len < 128) {
      out.write(len);
    } else if (len < 256) {
      out.write(0x81);
      out.write(len);
    } else {
      out.write(0x82);
      out.write(len >> 8);
      out.write(len);
    }
  }

  private static byte[] encode(String s) {
    String[] parts = s.split("\\.", -1);
    if (parts.length < 2) {
      throw new IllegalArgumentException(s);
    }
    long[] arcs = new long[parts.length];
    for (int i = 0; i < parts.length; i++) {
      if (parts[i].length() == 0) {
        throw new IllegalArgumentException(s);
      }
      arcs[i] = Long.parseLong(parts[i]);
      if (arcs[i] < 0) {
        throw new IllegalArgumentException(s);
      }
    }
    if (arcs[0] > 2 || (arcs[0] < 2 && arcs[1] >= 40)) {
      throw new IllegalArgumentException(s);
    }
    ByteArrayOutputStream body = new ByteArrayOutputStream();
    putArc(body, arcs[0] * 40 + arcs[1]);
    for (int i = 2; i < arcs.length; i++) {
      putArc(body, arcs[i]);
    }
    byte[] b = body.toByteArray();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(0x06);
    writeLen(out, b.length);
    out.write(b, 0, b.length);
    return out.toByteArray();
  }

  private static void putArc(ByteArrayOutputStream out, long v) {
    int n = 1;
    for (long t = v >> 7; t > 0; t >>= 7) {
      n++;
    }
    for (int i = n - 1; i >= 0; i--) {
      int b = (int) ((v >> (7 * i)) & 0x7f);
      out.write(i > 0 ? (b | 0x80) : b);
    }
  }

  private static String decode(byte[] body) {
    StringBuilder sb = new StringBuilder();
    long v = 0;
    boolean first = true;
    for (int i = 0; i < body.length; i++) {
      v = (v << 7) | (body[i] & 0x7f);
      if ((body[i] & 0x80) == 0) {
        if (first) {
          long a = v < 80 ? v / 40 : 2;
          long b = v < 80 ? v % 40 : v - 80;
          sb.append(a).append('.').append(b);
          first = false;
        } else {
          sb.append('.').append(v);
        }
        v = 0;
      }
    }
    return sb.toString();
  }

  public String toString() {
    return oid;
  }

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other instanceof Oid) {
      return Arrays.equals(this.derEncoding, ((Oid) other).derEncoding);
    }
    return false;
  }

  public byte[] getDER() {
    return derEncoding.clone();
  }

  public boolean containedIn(Oid[] oids) {
    for (int i = 0; i < oids.length; i++) {
      if (oids[i].equals(this)) {
        return true;
      }
    }
    return false;
  }

  public int hashCode() {
    int headerLen = (derEncoding[1] & 0x80) == 0 ? 2 : 2 + (derEncoding[1] & 0x7f);
    return Arrays.hashCode(Arrays.copyOfRange(derEncoding, headerLen, derEncoding.length));
  }
}
