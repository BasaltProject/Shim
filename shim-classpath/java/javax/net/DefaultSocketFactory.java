/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

class DefaultSocketFactory extends SocketFactory {
  DefaultSocketFactory() {
  }

  @Override
  public Socket createSocket() {
    return new Socket();
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    return new Socket(host, port);
  }

  @Override
  public Socket createSocket(InetAddress address, int port) throws IOException {
    return new Socket(address, port);
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress clientAddress, int clientPort) throws IOException,
      UnknownHostException {
    return new Socket(host, port, clientAddress, clientPort);
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort)
      throws IOException {
    return new Socket(address, port, clientAddress, clientPort);
  }
}
