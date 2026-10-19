/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package sun.net.httpserver;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

class HttpContextImpl extends HttpContext {
  private final String path;
  private final String protocol;
  private volatile HttpHandler handler;
  private final Map<String, Object> attributes = new HashMap<String, Object>();
  private final ServerImpl server;
  private final List<Filter> filters = new CopyOnWriteArrayList<Filter>();
  private volatile Authenticator authenticator;

  HttpContextImpl(String protocol, String path, HttpHandler cb, ServerImpl server) {
    if (path == null || protocol == null || path.length() < 1 || path.charAt(0) != '/') {
      throw new IllegalArgumentException("Illegal value for path or protocol");
    }
    this.protocol = protocol.toLowerCase(java.util.Locale.ROOT);
    this.path = path;
    this.handler = cb;
    this.server = server;
  }

  @Override
  public HttpHandler getHandler() {
    return handler;
  }

  @Override
  public void setHandler(HttpHandler h) {
    if (h == null) {
      throw new NullPointerException("Null handler parameter");
    }
    if (handler != null) {
      throw new IllegalArgumentException("handler already set");
    }
    handler = h;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public HttpServer getServer() {
    return server.getWrapper();
  }

  ServerImpl getServerImpl() {
    return server;
  }

  String getProtocol() {
    return protocol;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public List<Filter> getFilters() {
    return filters;
  }

  @Override
  public Authenticator setAuthenticator(Authenticator auth) {
    Authenticator old = authenticator;
    authenticator = auth;
    return old;
  }

  @Override
  public Authenticator getAuthenticator() {
    return authenticator;
  }
}
