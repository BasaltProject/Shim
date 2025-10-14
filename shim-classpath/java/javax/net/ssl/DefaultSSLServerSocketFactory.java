/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;

final class DefaultSSLServerSocketFactory extends SSLServerSocketFactory {
  private final Exception reason;

  DefaultSSLServerSocketFactory(Exception reason) {
    this.reason = reason;
  }

  private ServerSocket throwException() throws SocketException {
    throw (SocketException) new SocketException(reason.toString()).initCause(reason);
  }

  @Override
  public ServerSocket createServerSocket() throws IOException {
    return throwException();
  }

  @Override
  public ServerSocket createServerSocket(int port) throws IOException {
    return throwException();
  }

  @Override
  public ServerSocket createServerSocket(int port, int backlog) throws IOException {
    return throwException();
  }

  @Override
  public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress) throws IOException {
    return throwException();
  }

  @Override
  public String[] getDefaultCipherSuites() {
    return new String[0];
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return new String[0];
  }
}
