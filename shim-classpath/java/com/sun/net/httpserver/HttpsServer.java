/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.net.httpserver;

import com.sun.net.httpserver.spi.HttpServerProvider;
import java.io.IOException;
import java.net.InetSocketAddress;

public abstract class HttpsServer extends HttpServer {
  protected HttpsServer() { }

  public static HttpsServer create() throws IOException {
    return create(null, 0);
  }

  public static HttpsServer create(InetSocketAddress addr, int backlog) throws IOException {
    HttpServerProvider provider = HttpServerProvider.provider();
    return provider.createHttpsServer(addr, backlog);
  }

  public static HttpsServer create(InetSocketAddress addr, int backlog, String path, HttpHandler handler,
      Filter... filters) throws IOException {
    HttpsServer server = HttpsServer.create(addr, backlog);
    HttpContext context = server.createContext(path, handler);
    for (Filter f : filters) {
      context.getFilters().add(f);
    }
    return server;
  }

  public abstract void setHttpsConfigurator(HttpsConfigurator config);

  public abstract HttpsConfigurator getHttpsConfigurator();
}
