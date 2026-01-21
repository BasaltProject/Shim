/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.net.httpserver.spi;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ServiceLoader;

public abstract class HttpServerProvider {
  private static final Object kLock = new Object();
  private static HttpServerProvider provider;

  public abstract HttpServer createHttpServer(InetSocketAddress addr, int backlog) throws IOException;

  public abstract HttpsServer createHttpsServer(InetSocketAddress addr, int backlog) throws IOException;

  protected HttpServerProvider() { }

  public static HttpServerProvider provider() {
    synchronized (kLock) {
      if (provider != null) {
        return provider;
      }
      String name = System.getProperty("com.sun.net.httpserver.HttpServerProvider");
      if (name != null) {
        try {
          provider = (HttpServerProvider) Class.forName(name, true, ClassLoader.getSystemClassLoader())
              .getDeclaredConstructor().newInstance();
          return provider;
        } catch (ReflectiveOperationException e) {
          throw new java.util.ServiceConfigurationError(null, e);
        }
      }
      for (HttpServerProvider found : ServiceLoader.load(HttpServerProvider.class,
          ClassLoader.getSystemClassLoader())) {
        provider = found;
        return provider;
      }
      provider = new sun.net.httpserver.DefaultHttpServerProvider();
      return provider;
    }
  }
}
