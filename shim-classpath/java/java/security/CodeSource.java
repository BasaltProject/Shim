/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.security;

import java.net.URL;
import java.security.cert.Certificate;

public class CodeSource implements java.io.Serializable {
  private static final long serialVersionUID = 4977541819976013951L;

  private final URL location;
  private final Certificate[] certs;
  private transient CodeSigner[] signers;

  public CodeSource(URL url, Certificate[] certs) {
    this.location = url;
    this.certs = certs == null ? null : certs.clone();
  }

  public CodeSource(URL url, CodeSigner[] signers) {
    this.location = url;
    this.signers = signers == null ? null : signers.clone();
    this.certs = null;
  }

  @Override
  public int hashCode() {
    return location != null ? location.hashCode() : 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof CodeSource)) {
      return false;
    }
    CodeSource cs = (CodeSource) obj;
    if (location == null) {
      if (cs.location != null) {
        return false;
      }
    } else if (cs.location == null || !location.toString().equals(cs.location.toString())) {
      return false;
    }
    return java.util.Arrays.equals(certs, cs.certs);
  }

  public final URL getLocation() {
    return location;
  }

  public final Certificate[] getCertificates() {
    return certs == null ? null : certs.clone();
  }

  public final CodeSigner[] getCodeSigners() {
    return signers == null ? null : signers.clone();
  }

  public boolean implies(CodeSource codesource) {
    if (codesource == null) {
      return false;
    }
    return matchCerts(codesource) && matchLocation(codesource);
  }

  private boolean matchCerts(CodeSource that) {
    if (certs == null || certs.length == 0) {
      return true;
    }
    if (that.certs == null) {
      return false;
    }
    for (Certificate c : certs) {
      boolean found = false;
      for (Certificate o : that.certs) {
        if (c.equals(o)) {
          found = true;
          break;
        }
      }
      if (!found) {
        return false;
      }
    }
    return true;
  }

  private boolean matchLocation(CodeSource that) {
    if (location == null) {
      return true;
    }
    if (that.location == null) {
      return false;
    }
    if (location.equals(that.location)) {
      return true;
    }
    if (!location.getProtocol().equalsIgnoreCase(that.location.getProtocol())) {
      return false;
    }
    if (location.getPort() != -1 && location.getPort() != that.location.getPort()) {
      return false;
    }
    String host = location.getHost();
    if (host != null && !host.isEmpty() && !host.equalsIgnoreCase(that.location.getHost())) {
      return false;
    }
    String path = location.getFile();
    String thatPath = that.location.getFile();
    if (path.endsWith("/-")) {
      String prefix = path.substring(0, path.length() - 1);
      return thatPath.startsWith(prefix);
    }
    if (path.endsWith("/*")) {
      String prefix = path.substring(0, path.length() - 1);
      if (!thatPath.startsWith(prefix)) {
        return false;
      }
      return thatPath.indexOf('/', prefix.length()) < 0 || thatPath.indexOf('/',
          prefix.length()) == thatPath.length() - 1;
    }
    if (!thatPath.equals(path) && !thatPath.equals(path + "/")) {
      return false;
    }
    String ref = location.getRef();
    return ref == null || ref.equals(that.location.getRef());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("(").append(location);
    if (certs != null && certs.length > 0) {
      for (Certificate c : certs) {
        sb.append(' ').append(c);
      }
    } else if (signers != null && signers.length > 0) {
      for (CodeSigner s : signers) {
        sb.append(' ').append(s);
      }
    } else {
      sb.append(" <no signer certificates>");
    }
    return sb.append(')').toString();
  }
}
