/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.net.www.protocol.http;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import sun.net.www.http.HeaderParser;

public abstract class AuthenticationInfo implements Cloneable {
  static final char kServerAuthentication = 's';
  static final char kProxyAuthentication = 'p';

  private static final Map<String, List<AuthenticationInfo>> kCache = new HashMap<String, List<AuthenticationInfo>>();

  final char type;
  final String authScheme;
  final String protocol;
  final String host;
  final int port;
  final String realm;
  String path;

  AuthenticationInfo(char type, String authScheme, URL url, String realm) {
    this.type = type;
    this.authScheme = authScheme;
    this.protocol = url.getProtocol().toLowerCase();
    this.host = url.getHost().toLowerCase();
    this.port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
    this.realm = realm;
    String urlPath = url.getPath();
    this.path = urlPath == null || urlPath.isEmpty() ? "/" : reducePath(urlPath);
  }

  AuthenticationInfo(char type, String authScheme, String host, int port, String realm) {
    this.type = type;
    this.authScheme = authScheme;
    this.protocol = "";
    this.host = host.toLowerCase();
    this.port = port;
    this.realm = realm;
    this.path = null;
  }

  static String reducePath(String urlPath) {
    int sepIndex = urlPath.lastIndexOf('/');
    int targetSuffixIndex = urlPath.lastIndexOf('.');
    if (sepIndex != -1) {
      if (sepIndex < targetSuffixIndex) {
        return urlPath.substring(0, sepIndex + 1);
      }
      return urlPath;
    }
    return urlPath;
  }

  public abstract boolean supportsPreemptiveAuthorization();

  public abstract String getHeaderValue(URL url, String method);

  public abstract boolean isAuthorizationStale(String header);

  public abstract boolean setHeaders(HttpURLConnection conn, HeaderParser p, String raw);

  public String getHeaderName() {
    return type == kServerAuthentication ? "Authorization" : "Proxy-Authorization";
  }

  String cacheKey(boolean includeRealm) {
    String base = type + ":" + protocol + ":" + host + ":" + port;
    return includeRealm ? base + ":" + realm + ":" + authScheme : base;
  }

  public void addToCache() {
    put(cacheKey(true), this);
    if (supportsPreemptiveAuthorization()) {
      put(cacheKey(false), this);
    }
  }

  public void removeFromCache() {
    remove(cacheKey(true), this);
    if (supportsPreemptiveAuthorization()) {
      remove(cacheKey(false), this);
    }
  }

  @Override
  public AuthenticationInfo clone() {
    try {
      return (AuthenticationInfo) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  private static synchronized void put(String key, AuthenticationInfo info) {
    List<AuthenticationInfo> list = kCache.get(key);
    if (list == null) {
      list = new ArrayList<AuthenticationInfo>();
      kCache.put(key, list);
    }
    for (Iterator<AuthenticationInfo> it = list.iterator(); it.hasNext();) {
      AuthenticationInfo old = it.next();
      if (Objects.equals(old.path, info.path) && old.realm.equals(info.realm) && old.authScheme.equals(info.authScheme)) {
        it.remove();
      }
    }
    list.add(0, info);
  }

  private static synchronized void remove(String key, AuthenticationInfo info) {
    List<AuthenticationInfo> list = kCache.get(key);
    if (list != null) {
      list.remove(info);
      if (list.isEmpty()) {
        kCache.remove(key);
      }
    }
  }

  private static synchronized AuthenticationInfo lookup(String key, String path) {
    List<AuthenticationInfo> list = kCache.get(key);
    if (list == null) {
      return null;
    }
    for (AuthenticationInfo info : list) {
      if (path == null || info.path == null || path.startsWith(info.path)) {
        return info;
      }
    }
    return null;
  }

  static AuthenticationInfo getServerAuth(URL url) {
    int port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
    String key = kServerAuthentication + ":" + url.getProtocol().toLowerCase() + ":" + url.getHost().toLowerCase()
        + ":" + port;
    String path = url.getPath();
    return lookup(key, path == null || path.isEmpty() ? "/" : path);
  }

  static AuthenticationInfo getServerAuth(String key) {
    return lookup(key, null);
  }

  static String serverAuthKey(URL url, String realm, String scheme) {
    int port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
    return kServerAuthentication + ":" + url.getProtocol().toLowerCase() + ":" + url.getHost().toLowerCase() + ":"
        + port + ":" + realm + ":" + scheme;
  }

  static AuthenticationInfo getProxyAuth(String host, int port) {
    return lookup(kProxyAuthentication + "::" + host.toLowerCase() + ":" + port, null);
  }

  static String proxyAuthKey(String host, int port, String realm, String scheme) {
    return kProxyAuthentication + "::" + host.toLowerCase() + ":" + port + ":" + realm + ":" + scheme;
  }
}
