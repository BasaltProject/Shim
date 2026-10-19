/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package sun.net.httpserver;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

public class HttpsServerImpl extends HttpsServer {
  private final ServerImpl server;

  HttpsServerImpl() throws IOException {
    this(new InetSocketAddress(0), 0);
  }

  HttpsServerImpl(InetSocketAddress addr, int backlog) throws IOException {
    server = new ServerImpl(this, "https", addr, backlog);
  }

  @Override
  public void setHttpsConfigurator(HttpsConfigurator config) {
    server.setHttpsConfigurator(config);
  }

  @Override
  public HttpsConfigurator getHttpsConfigurator() {
    return server.getHttpsConfigurator();
  }

  @Override
  public void bind(InetSocketAddress addr, int backlog) throws IOException {
    server.bind(addr, backlog);
  }

  @Override
  public void start() {
    server.start();
  }

  @Override
  public void setExecutor(Executor executor) {
    server.setExecutor(executor);
  }

  @Override
  public Executor getExecutor() {
    return server.getExecutor();
  }

  @Override
  public void stop(int delay) {
    server.stop(delay);
  }

  @Override
  public HttpContext createContext(String path, HttpHandler handler) {
    return server.createContext(path, handler);
  }

  @Override
  public HttpContext createContext(String path) {
    return server.createContext(path);
  }

  @Override
  public void removeContext(String path) throws IllegalArgumentException {
    server.removeContext(path);
  }

  @Override
  public void removeContext(HttpContext context) throws IllegalArgumentException {
    server.removeContext(context);
  }

  @Override
  public InetSocketAddress getAddress() {
    return server.getAddress();
  }
}
