/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.Serializable;
import java.security.Permission;

public final class URLPermission extends Permission implements Serializable {
  private static final long serialVersionUID = -2702463814894478682L;

  private final String scheme;
  private final String authority;
  private final String path;
  private final String methods;
  private final String headers;

  public URLPermission(String url, String actions) {
    super(url);
    int colon = actions.indexOf(':');
    if (colon < 0) {
      colon = actions.length();
    }
    methods = actions.substring(0, colon).toUpperCase();
    headers = colon < actions.length() ? actions.substring(colon + 1) : "";
    int schemeEnd = url.indexOf(':');
    if (schemeEnd < 0) {
      throw new IllegalArgumentException("invalid URL string: " + url);
    }
    scheme = url.substring(0, schemeEnd).toLowerCase();
    String rest = url.substring(schemeEnd + 1);
    if (rest.startsWith("//")) {
      rest = rest.substring(2);
    }
    int slash = rest.indexOf('/');
    authority = slash < 0 ? rest : rest.substring(0, slash);
    path = slash < 0 ? "" : rest.substring(slash);
  }

  public URLPermission(String url) {
    this(url, "*:*");
  }

  @Override
  public String getActions() {
    return methods + ":" + headers;
  }

  @Override
  public boolean implies(Permission p) {
    if (!(p instanceof URLPermission)) {
      return false;
    }
    URLPermission that = (URLPermission) p;
    if (!scheme.equals(that.scheme) || !authority.equalsIgnoreCase(that.authority)) {
      return false;
    }
    boolean pathOk = path.equals(that.path) || (path.endsWith("/*") && that.path.startsWith(path.substring(0,
        path.length() - 1)))
        || (path.endsWith("/-") && that.path.startsWith(path.substring(0, path.length() - 1)));
    if (!pathOk) {
      return false;
    }
    return methods.equals("*") || methods.equals(that.methods);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof URLPermission)) {
      return false;
    }
    URLPermission that = (URLPermission) obj;
    return scheme.equals(that.scheme) && authority.equalsIgnoreCase(that.authority) && path.equals(that.path)
        && methods.equals(that.methods) && headers.equals(that.headers);
  }

  @Override
  public int hashCode() {
    return scheme.hashCode() ^ authority.toLowerCase().hashCode() ^ path.hashCode();
  }
}
