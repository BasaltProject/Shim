/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package sun.net.httpserver;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import java.io.IOException;
import java.net.InetSocketAddress;

public class DefaultHttpServerProvider extends HttpServerProvider {
  @Override
  public HttpServer createHttpServer(InetSocketAddress addr, int backlog) throws IOException {
    return new HttpServerImpl(addr, backlog);
  }

  @Override
  public HttpsServer createHttpsServer(InetSocketAddress addr, int backlog) throws IOException {
    return new HttpsServerImpl(addr, backlog);
  }
}
