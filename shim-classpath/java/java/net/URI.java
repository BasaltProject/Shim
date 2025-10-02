/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.Serializable;
import java.util.Objects;

public final class URI implements Comparable<URI>, Serializable {
  private static final long serialVersionUID = -6052424284110960213L;

  private final String string;
  private final String scheme;
  private final String schemeSpecificPart;
  private final String authority;
  private final String path;
  private final String query;
  private final String fragment;

  public URI(String str) throws URISyntaxException {
    Objects.requireNonNull(str, "str");
    this.string = str;
    int colon = str.indexOf(':');
    int slash = str.indexOf('/');
    int qm = str.indexOf('?');
    int hash = str.indexOf('#');

    if (colon > 0 && (slash < 0 || colon < slash)) {
      this.scheme = str.substring(0, colon);
      String rest = str.substring(colon + 1);
      int rhash = rest.indexOf('#');
      if (rhash >= 0) {
        this.fragment = rest.substring(rhash + 1);
        rest = rest.substring(0, rhash);
      } else {
        this.fragment = null;
      }
      this.schemeSpecificPart = rest;
      int rqm = rest.indexOf('?');
      if (rqm >= 0) {
        this.query = rest.substring(rqm + 1);
        rest = rest.substring(0, rqm);
      } else {
        this.query = null;
      }
      if (rest.startsWith("//")) {
        int nextSlash = rest.indexOf('/', 2);
        String auth;
        if (nextSlash >= 0) {
          auth = rest.substring(2, nextSlash);
          this.path = rest.substring(nextSlash);
        } else {
          auth = rest.substring(2);
          this.path = "";
        }
        this.authority = auth.isEmpty() ? null : auth;
      } else {
        this.authority = null;
        this.path = rest;
      }
    } else {
      this.scheme = null;
      String rest = str;
      int rhash = rest.indexOf('#');
      if (rhash >= 0) {
        this.fragment = rest.substring(rhash + 1);
        rest = rest.substring(0, rhash);
      } else {
        this.fragment = null;
      }
      this.schemeSpecificPart = rest;
      int rqm = rest.indexOf('?');
      if (rqm >= 0) {
        this.query = rest.substring(rqm + 1);
        this.path = rest.substring(0, rqm);
      } else {
        this.query = null;
        this.path = rest;
      }
      this.authority = null;
    }
  }

  public static URI create(String str) {
    try {
      return new URI(str);
    } catch (URISyntaxException x) {
      throw new IllegalArgumentException(x.getMessage(), x);
    }
  }

  public String getScheme() {
    return scheme;
  }

  public String getSchemeSpecificPart() {
    return schemeSpecificPart;
  }

  public String getAuthority() {
    return authority;
  }

  public String getUserInfo() {
    if (authority == null) return null;
    int at = authority.indexOf('@');
    return at >= 0 ? authority.substring(0, at) : null;
  }

  public String getHost() {
    if (authority == null) return null;
    int at = authority.indexOf('@');
    String hostPort = at >= 0 ? authority.substring(at + 1) : authority;
    int colon = hostPort.lastIndexOf(':');
    String h = colon >= 0 ? hostPort.substring(0, colon) : hostPort;
    return h.isEmpty() ? null : h;
  }

  public int getPort() {
    if (authority == null) return -1;
    int at = authority.indexOf('@');
    String hostPort = at >= 0 ? authority.substring(at + 1) : authority;
    int colon = hostPort.lastIndexOf(':');
    if (colon >= 0) {
      try {
        return Integer.parseInt(hostPort.substring(colon + 1));
      } catch (NumberFormatException ignored) {}
    }
    return -1;
  }

  public String getPath() {
    return path;
  }

  public String getQuery() {
    return query;
  }

  public String getFragment() {
    return fragment;
  }

  public boolean isAbsolute() {
    return scheme != null;
  }

  public boolean isOpaque() {
    return path == null;
  }

  public URL toURL() throws MalformedURLException {
    return new URL(string);
  }

  @Override
  public int compareTo(URI that) {
    return this.string.compareTo(that.string);
  }

  @Override
  public boolean equals(Object ob) {
    if (ob == this) return true;
    if (ob instanceof URI) {
      return this.string.equals(((URI) ob).string);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return string.hashCode();
  }

  @Override
  public String toString() {
    return string;
  }
}
