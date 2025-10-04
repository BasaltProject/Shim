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

package java.net;

import java.io.IOException;

public abstract class URLStreamHandler {
  protected abstract URLConnection openConnection(URL u) throws IOException;

  protected URLConnection openConnection(URL u, Proxy p) throws IOException {
    throw new UnsupportedOperationException("Method not implemented.");
  }

  protected void parseURL(URL u, String spec, int start, int limit) {
    String protocol = u.getProtocol();
    String authority = u.getAuthority();
    String userInfo = u.getUserInfo();
    String host = u.getHost();
    int port = u.getPort();
    String path = u.getPath();
    String query = u.getQuery();
    String ref = u.getRef();
    boolean isRelPath = false;
    boolean queryOnly = false;
    if (start < limit) {
      int queryStart = spec.indexOf('?');
      queryOnly = queryStart == start;
      if ((queryStart != -1) && (queryStart < limit)) {
        query = spec.substring(queryStart + 1, limit);
        if (limit > queryStart) {
          limit = queryStart;
        }
        spec = spec.substring(0, queryStart);
      }
    }
    int i = 0;
    boolean isUNCName = (start <= limit - 4) && (spec.charAt(start) == '/') && (spec.charAt(start + 1) == '/')
        && (spec.charAt(start + 2) == '/') && (spec.charAt(start + 3) == '/');
    if (!isUNCName && (start <= limit - 2) && (spec.charAt(start) == '/') && (spec.charAt(start + 1) == '/')) {
      start += 2;
      i = spec.indexOf('/', start);
      if (i < 0 || i > limit) {
        i = spec.indexOf('?', start);
        if (i < 0 || i > limit) {
          i = limit;
        }
      }
      host = authority = spec.substring(start, i);
      int ind = authority.indexOf('@');
      if (ind != -1) {
        if (ind != authority.lastIndexOf('@')) {
          userInfo = null;
          host = null;
        } else {
          userInfo = authority.substring(0, ind);
          host = authority.substring(ind + 1);
        }
      } else {
        userInfo = null;
      }
      if (host != null) {
        if (host.length() > 0 && (host.charAt(0) == '[')) {
          if ((ind = host.indexOf(']')) > 2) {
            String nhost = host;
            host = nhost.substring(0, ind + 1);
            if (!isIPv6LiteralAddress(host.substring(1, ind))) {
              throw new IllegalArgumentException("Invalid host: " + host);
            }
            port = -1;
            if (nhost.length() > ind + 1) {
              if (nhost.charAt(ind + 1) == ':') {
                ++ind;
                if (nhost.length() > (ind + 1)) {
                  port = Integer.parseInt(nhost, ind + 1, nhost.length(), 10);
                }
              } else {
                throw new IllegalArgumentException("Invalid authority field: " + authority);
              }
            }
          } else {
            throw new IllegalArgumentException("Invalid authority field: " + authority);
          }
        } else {
          ind = host.indexOf(':');
          port = -1;
          if (ind >= 0) {
            if (host.length() > (ind + 1)) {
              port = Integer.parseInt(host, ind + 1, host.length(), 10);
            }
            host = host.substring(0, ind);
          }
        }
      } else {
        host = "";
      }
      for (int k = 0; k < host.length(); k++) {
        char hc = host.charAt(k);
        if (hc <= ' ' || hc == 127 || "\"<>\\^`{|}".indexOf(hc) >= 0) {
          throw new IllegalArgumentException("Illegal character found in host: '" + hc + "'");
        }
      }
      if (port < -1) {
        throw new IllegalArgumentException("Invalid port number :" + port);
      }
      start = i;
      if (authority != null && !authority.isEmpty()) {
        path = "";
      }
    }
    if (host == null) {
      host = "";
    }
    if (start < limit) {
      if (spec.charAt(start) == '/') {
        path = spec.substring(start, limit);
      } else if (path != null && !path.isEmpty()) {
        isRelPath = true;
        int ind = path.lastIndexOf('/');
        String separator = "";
        if (ind == -1 && authority != null) {
          separator = "/";
        }
        path = path.substring(0, ind + 1) + separator + spec.substring(start, limit);
      } else {
        path = spec.substring(start, limit);
        path = (authority != null) ? "/" + path : path;
      }
    } else if (queryOnly && path != null) {
      int ind = path.lastIndexOf('/');
      if (ind < 0) {
        ind = 0;
      }
      path = path.substring(0, ind) + "/";
    }
    if (path == null) {
      path = "";
    }
    if (isRelPath) {
      while ((i = path.indexOf("/./")) >= 0) {
        path = path.substring(0, i) + path.substring(i + 2);
      }
      i = 0;
      while ((i = path.indexOf("/../", i)) >= 0) {
        if (i > 0 && (limit = path.lastIndexOf('/', i - 1)) >= 0 && (path.indexOf("/../", limit) != 0)) {
          path = path.substring(0, limit) + path.substring(i + 3);
          i = 0;
        } else {
          i = i + 3;
        }
      }
      while (path.endsWith("/..")) {
        i = path.indexOf("/..");
        if ((limit = path.lastIndexOf('/', i - 1)) >= 0) {
          path = path.substring(0, limit + 1);
        } else {
          break;
        }
      }
      if (path.startsWith("./") && path.length() > 2) {
        path = path.substring(2);
      }
      if (path.endsWith("/.")) {
        path = path.substring(0, path.length() - 1);
      }
    }
    setURL(u, protocol, host, port, authority, userInfo, path, query, ref);
  }

  private static boolean isIPv6LiteralAddress(String s) {
    int scope = s.indexOf('%');
    if (scope >= 0) {
      if (scope == s.length() - 1) {
        return false;
      }
      s = s.substring(0, scope);
    }
    int groups = 0;
    boolean compressed = false;
    int i = 0;
    int n = s.length();
    if (n == 0) {
      return false;
    }
    if (s.startsWith("::")) {
      compressed = true;
      i = 2;
      if (i == n) {
        return true;
      }
    } else if (s.charAt(0) == ':') {
      return false;
    }
    while (i < n) {
      int j = i;
      while (j < n && Character.digit(s.charAt(j), 16) >= 0 && s.charAt(j) < 128) {
        j++;
      }
      if (j < n && s.charAt(j) == '.') {
        if (!isIPv4Literal(s.substring(i))) {
          return false;
        }
        groups += 2;
        i = n;
        break;
      }
      if (j == i || j - i > 4) {
        return false;
      }
      groups++;
      i = j;
      if (i == n) {
        break;
      }
      if (s.charAt(i) != ':') {
        return false;
      }
      i++;
      if (i < n && s.charAt(i) == ':') {
        if (compressed) {
          return false;
        }
        compressed = true;
        i++;
        if (i == n) {
          break;
        }
      } else if (i == n) {
        return false;
      }
    }
    return compressed ? groups < 8 : groups == 8;
  }

  private static boolean isIPv4Literal(String s) {
    String[] parts = s.split("\\.", -1);
    if (parts.length != 4) {
      return false;
    }
    for (String p : parts) {
      if (p.isEmpty() || p.length() > 3) {
        return false;
      }
      for (int k = 0; k < p.length(); k++) {
        if (p.charAt(k) < '0' || p.charAt(k) > '9') {
          return false;
        }
      }
      if (Integer.parseInt(p) > 255) {
        return false;
      }
    }
    return true;
  }

  protected int getDefaultPort() {
    return -1;
  }

  protected boolean equals(URL u1, URL u2) {
    String ref1 = u1.getRef();
    String ref2 = u2.getRef();
    return (ref1 == ref2 || (ref1 != null && ref1.equals(ref2))) && sameFile(u1, u2);
  }

  protected int hashCode(URL u) {
    int h = 0;
    String protocol = u.getProtocol();
    if (protocol != null) {
      h += protocol.hashCode();
    }
    InetAddress addr = getHostAddress(u);
    if (addr != null) {
      h += addr.hashCode();
    } else {
      String host = u.getHost();
      if (host != null) {
        h += host.toLowerCase(java.util.Locale.ROOT).hashCode();
      }
    }
    String file = u.getFile();
    if (file != null) {
      h += file.hashCode();
    }
    if (u.getPort() == -1) {
      h += getDefaultPort();
    } else {
      h += u.getPort();
    }
    String ref = u.getRef();
    if (ref != null) {
      h += ref.hashCode();
    }
    return h;
  }

  protected boolean sameFile(URL u1, URL u2) {
    if (!(u1.getProtocol() == u2.getProtocol()
        || (u1.getProtocol() != null && u1.getProtocol().equalsIgnoreCase(u2.getProtocol())))) {
      return false;
    }
    if (!(u1.getFile() == u2.getFile() || (u1.getFile() != null && u1.getFile().equals(u2.getFile())))) {
      return false;
    }
    int port1 = (u1.getPort() != -1) ? u1.getPort() : u1.handler.getDefaultPort();
    int port2 = (u2.getPort() != -1) ? u2.getPort() : u2.handler.getDefaultPort();
    if (port1 != port2) {
      return false;
    }
    return hostsEqual(u1, u2);
  }

  protected synchronized InetAddress getHostAddress(URL u) {
    return u.getHostAddress();
  }

  protected boolean hostsEqual(URL u1, URL u2) {
    InetAddress a1 = getHostAddress(u1);
    InetAddress a2 = getHostAddress(u2);
    if (a1 != null && a2 != null) {
      return a1.equals(a2);
    } else if (u1.getHost() != null && u2.getHost() != null) {
      return u1.getHost().equalsIgnoreCase(u2.getHost());
    }
    return u1.getHost() == null && u2.getHost() == null;
  }

  protected String toExternalForm(URL u) {
    int len = u.getProtocol().length() + 1;
    if (u.getAuthority() != null && !u.getAuthority().isEmpty()) {
      len += 2 + u.getAuthority().length();
    }
    if (u.getPath() != null) {
      len += u.getPath().length();
    }
    if (u.getQuery() != null) {
      len += 1 + u.getQuery().length();
    }
    if (u.getRef() != null) {
      len += 1 + u.getRef().length();
    }
    StringBuilder sb = new StringBuilder(len);
    sb.append(u.getProtocol());
    sb.append(':');
    if (u.getAuthority() != null && !u.getAuthority().isEmpty()) {
      sb.append("//");
      sb.append(u.getAuthority());
    }
    String s = u.getPath();
    if (s != null) {
      sb.append(s);
    }
    s = u.getQuery();
    if (s != null) {
      sb.append('?');
      sb.append(s);
    }
    s = u.getRef();
    if (s != null) {
      sb.append('#');
      sb.append(s);
    }
    return sb.toString();
  }

  protected void setURL(URL u, String protocol, String host, int port, String authority, String userInfo,
      String path, String query, String ref) {
    u.set(protocol, host, port, authority, userInfo, path, query, ref);
  }

  @Deprecated
  protected void setURL(URL u, String protocol, String host, int port, String file, String ref) {
    String authority = null;
    String userInfo = null;
    if (host != null && !host.isEmpty()) {
      authority = (port == -1) ? host : host + ":" + port;
      int at = host.lastIndexOf('@');
      if (at != -1) {
        userInfo = host.substring(0, at);
        host = host.substring(at + 1);
      }
    }
    String path = null;
    String query = null;
    if (file != null) {
      int q = file.lastIndexOf('?');
      if (q != -1) {
        query = file.substring(q + 1);
        path = file.substring(0, q);
      } else {
        path = file;
      }
    }
    setURL(u, protocol, host, port, authority, userInfo, path, query, ref);
  }
}
