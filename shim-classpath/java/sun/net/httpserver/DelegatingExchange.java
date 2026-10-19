/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package sun.net.httpserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.Request;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public final class DelegatingExchange extends HttpExchange {
  private final HttpExchange exchange;
  private final Request request;

  public DelegatingExchange(HttpExchange exchange, Request request) {
    this.exchange = exchange;
    this.request = request;
  }

  @Override
  public Headers getRequestHeaders() {
    return request.getRequestHeaders();
  }

  @Override
  public Headers getResponseHeaders() {
    return exchange.getResponseHeaders();
  }

  @Override
  public URI getRequestURI() {
    return request.getRequestURI();
  }

  @Override
  public String getRequestMethod() {
    return request.getRequestMethod();
  }

  @Override
  public HttpContext getHttpContext() {
    return exchange.getHttpContext();
  }

  @Override
  public void close() {
    exchange.close();
  }

  @Override
  public InputStream getRequestBody() {
    return exchange.getRequestBody();
  }

  @Override
  public OutputStream getResponseBody() {
    return exchange.getResponseBody();
  }

  @Override
  public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
    exchange.sendResponseHeaders(rCode, responseLength);
  }

  @Override
  public InetSocketAddress getRemoteAddress() {
    return exchange.getRemoteAddress();
  }

  @Override
  public int getResponseCode() {
    return exchange.getResponseCode();
  }

  @Override
  public InetSocketAddress getLocalAddress() {
    return exchange.getLocalAddress();
  }

  @Override
  public String getProtocol() {
    return exchange.getProtocol();
  }

  @Override
  public Object getAttribute(String name) {
    return exchange.getAttribute(name);
  }

  @Override
  public void setAttribute(String name, Object value) {
    exchange.setAttribute(name, value);
  }

  @Override
  public void setStreams(InputStream i, OutputStream o) {
    exchange.setStreams(i, o);
  }

  @Override
  public HttpPrincipal getPrincipal() {
    return exchange.getPrincipal();
  }
}
