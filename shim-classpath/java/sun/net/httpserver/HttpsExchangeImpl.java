/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package sun.net.httpserver;

import com.sun.net.httpserver.HttpsExchange;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpPrincipal;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import javax.net.ssl.SSLSession;

class HttpsExchangeImpl extends HttpsExchange {
  private final ExchangeImpl impl;

  HttpsExchangeImpl(ExchangeImpl impl) {
    this.impl = impl;
  }

  @Override
  public Headers getRequestHeaders() {
    return impl.getRequestHeaders();
  }

  @Override
  public Headers getResponseHeaders() {
    return impl.getResponseHeaders();
  }

  @Override
  public URI getRequestURI() {
    return impl.getRequestURI();
  }

  @Override
  public String getRequestMethod() {
    return impl.getRequestMethod();
  }

  @Override
  public HttpContext getHttpContext() {
    return impl.getHttpContext();
  }

  @Override
  public void close() {
    impl.close();
  }

  @Override
  public InputStream getRequestBody() {
    return impl.getRequestBody();
  }

  @Override
  public OutputStream getResponseBody() {
    return impl.getResponseBody();
  }

  @Override
  public void sendResponseHeaders(int rCode, long contentLen) throws IOException {
    impl.sendResponseHeaders(rCode, contentLen);
  }

  @Override
  public InetSocketAddress getRemoteAddress() {
    return impl.getRemoteAddress();
  }

  @Override
  public int getResponseCode() {
    return impl.getResponseCode();
  }

  @Override
  public InetSocketAddress getLocalAddress() {
    return impl.getLocalAddress();
  }

  @Override
  public String getProtocol() {
    return impl.getProtocol();
  }

  @Override
  public SSLSession getSSLSession() {
    return impl.sslSession();
  }

  @Override
  public Object getAttribute(String name) {
    return impl.getAttribute(name);
  }

  @Override
  public void setAttribute(String name, Object value) {
    impl.setAttribute(name, value);
  }

  @Override
  public void setStreams(InputStream i, OutputStream o) {
    impl.setStreams(i, o);
  }

  @Override
  public HttpPrincipal getPrincipal() {
    return impl.getPrincipal();
  }

  ExchangeImpl getExchangeImpl() {
    return impl;
  }
}
