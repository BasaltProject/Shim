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
import java.util.Objects;
import java.util.concurrent.Executor;

public abstract class HttpServer {
  protected HttpServer() { }

  public static HttpServer create() throws IOException {
    return create(null, 0);
  }

  public static HttpServer create(InetSocketAddress addr, int backlog) throws IOException {
    HttpServerProvider provider = HttpServerProvider.provider();
    return provider.createHttpServer(addr, backlog);
  }

  public static HttpServer create(InetSocketAddress addr, int backlog, String path, HttpHandler handler,
      Filter... filters) throws IOException {
    HttpServer server = HttpServer.create(addr, backlog);
    HttpContext context = server.createContext(path, handler);
    for (Filter f : filters) {
      context.getFilters().add(f);
    }
    return server;
  }

  public abstract void bind(InetSocketAddress addr, int backlog) throws IOException;

  public abstract void start();

  public abstract void setExecutor(Executor executor);

  public abstract Executor getExecutor();

  public abstract void stop(int delay);

  public abstract HttpContext createContext(String path, HttpHandler handler);

  public abstract HttpContext createContext(String path);

  public abstract void removeContext(String path) throws IllegalArgumentException;

  public abstract void removeContext(HttpContext context);

  public abstract InetSocketAddress getAddress();
}
