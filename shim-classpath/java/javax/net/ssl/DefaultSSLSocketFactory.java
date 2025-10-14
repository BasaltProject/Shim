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
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

final class DefaultSSLSocketFactory extends SSLSocketFactory {
  private final Exception reason;

  DefaultSSLSocketFactory(Exception reason) {
    this.reason = reason;
  }

  private Socket throwException() throws SocketException {
    throw (SocketException) new SocketException(reason.toString()).initCause(reason);
  }

  @Override
  public Socket createSocket() throws IOException {
    return throwException();
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    return throwException();
  }

  @Override
  public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
    return throwException();
  }

  @Override
  public Socket createSocket(InetAddress address, int port) throws IOException {
    return throwException();
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress clientAddress, int clientPort) throws IOException,
      UnknownHostException {
    return throwException();
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort)
      throws IOException {
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
