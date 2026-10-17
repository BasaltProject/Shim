/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

final class SSLServerSocketFactoryImpl extends SSLServerSocketFactory {
  private final SSLContextImpl context;

  SSLServerSocketFactoryImpl(SSLContextImpl context) {
    this.context = context;
  }

  @Override
  public ServerSocket createServerSocket() throws IOException {
    return new SSLServerSocketImpl(context);
  }

  @Override
  public ServerSocket createServerSocket(int port) throws IOException {
    return new SSLServerSocketImpl(context, port, 50, null);
  }

  @Override
  public ServerSocket createServerSocket(int port, int backlog) throws IOException {
    return new SSLServerSocketImpl(context, port, backlog, null);
  }

  @Override
  public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress) throws IOException {
    return new SSLServerSocketImpl(context, port, backlog, ifAddress);
  }

  @Override
  public String[] getDefaultCipherSuites() {
    return TlsParams.supportedCipherSuites();
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return TlsParams.supportedCipherSuites();
  }
}
