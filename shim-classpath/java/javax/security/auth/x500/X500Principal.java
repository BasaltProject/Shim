/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.x500;

import java.io.IOException;
import java.io.InputStream;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import sun.security.x509.X500Name;

public final class X500Principal implements Principal, Serializable {
  private static final long serialVersionUID = -500463348111345721L;

  public static final String RFC1779 = "RFC1779";
  public static final String RFC2253 = "RFC2253";
  public static final String CANONICAL = "CANONICAL";

  private transient X500Name thisX500Name;

  public X500Principal(String name) {
    this(name, Collections.<String, String>emptyMap());
  }

  public X500Principal(String name, Map<String, String> keywordMap) {
    if (name == null) {
      throw new NullPointerException("provided null name");
    }
    if (keywordMap == null) {
      throw new NullPointerException("provided null keyword map");
    }
    thisX500Name = X500Name.parse(name, keywordMap);
  }

  public X500Principal(byte[] name) {
    thisX500Name = X500Name.decode(name);
  }

  public X500Principal(InputStream is) {
    if (is == null) {
      throw new NullPointerException("provided null input stream");
    }
    try {
      is.mark(is.available());
      byte[] hdr = new byte[2];
      int n = is.read(hdr);
      if (n < 2) {
        throw new IllegalArgumentException("improperly specified input stream");
      }
      int len = hdr[1] & 0xff;
      int extra = 0;
      byte[] lenBytes = new byte[0];
      if (len >= 0x80) {
        extra = len & 0x7f;
        lenBytes = new byte[extra];
        if (is.read(lenBytes) < extra) {
          throw new IllegalArgumentException("improperly specified input stream");
        }
        len = 0;
        for (byte b : lenBytes) {
          len = (len << 8) | (b & 0xff);
        }
      }
      byte[] body = new byte[len];
      int off = 0;
      while (off < len) {
        int r = is.read(body, off, len - off);
        if (r < 0) {
          throw new IllegalArgumentException("improperly specified input stream");
        }
        off += r;
      }
      byte[] all = new byte[2 + extra + len];
      System.arraycopy(hdr, 0, all, 0, 2);
      System.arraycopy(lenBytes, 0, all, 2, extra);
      System.arraycopy(body, 0, all, 2 + extra, len);
      thisX500Name = X500Name.decode(all);
    } catch (IOException e) {
      throw new IllegalArgumentException("improperly specified input stream", e);
    }
  }

  @Override
  public String getName() {
    return getName(RFC2253);
  }

  public String getName(String format) {
    if (format != null) {
      if (format.equalsIgnoreCase(RFC1779)) {
        return thisX500Name.getRFC1779Name(null);
      } else if (format.equalsIgnoreCase(RFC2253)) {
        return thisX500Name.getRFC2253Name(null);
      } else if (format.equalsIgnoreCase(CANONICAL)) {
        return thisX500Name.getCanonicalName();
      }
    }
    throw new IllegalArgumentException("invalid format specified");
  }

  public String getName(String format, Map<String, String> oidMap) {
    if (oidMap == null) {
      throw new NullPointerException("provided null OID map");
    }
    if (format != null) {
      if (format.equalsIgnoreCase(RFC1779)) {
        return thisX500Name.getRFC1779Name(oidMap);
      } else if (format.equalsIgnoreCase(RFC2253)) {
        return thisX500Name.getRFC2253Name(oidMap);
      }
    }
    throw new IllegalArgumentException("invalid format specified");
  }

  public byte[] getEncoded() {
    return thisX500Name.getEncoded();
  }

  @Override
  public String toString() {
    return thisX500Name.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof X500Principal == false) {
      return false;
    }
    X500Principal other = (X500Principal) o;
    return this.thisX500Name.equals(other.thisX500Name);
  }

  @Override
  public int hashCode() {
    return thisX500Name.hashCode();
  }

  private void writeObject(ObjectOutputStream s) throws IOException {
    s.writeObject(thisX500Name.getEncoded());
  }

  private void readObject(ObjectInputStream s) throws IOException, NotActiveException, ClassNotFoundException {
    thisX500Name = X500Name.decode((byte[]) s.readObject());
  }
}
