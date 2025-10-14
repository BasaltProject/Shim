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
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocketFactory;

final class SSLSocketFactoryImpl extends SSLSocketFactory {
  private final SSLContextImpl context;

  SSLSocketFactoryImpl(SSLContextImpl context) {
    this.context = context;
  }

  @Override
  public Socket createSocket() throws IOException {
    return new SSLSocketImpl(context);
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    return new SSLSocketImpl(context, host, port);
  }

  @Override
  public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
    return new SSLSocketImpl(context, s, host, port, autoClose);
  }

  @Override
  public Socket createSocket(InetAddress address, int port) throws IOException {
    return new SSLSocketImpl(context, address, port);
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress clientAddress, int clientPort) throws IOException,
      UnknownHostException {
    return new SSLSocketImpl(context, host, port, clientAddress, clientPort);
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort)
      throws IOException {
    return new SSLSocketImpl(context, address, port, clientAddress, clientPort);
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
