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
import java.net.SocketException;
import java.net.UnknownHostException;

public abstract class SocketFactory {
  private static SocketFactory theFactory;

  protected SocketFactory() {
  }

  public static SocketFactory getDefault() {
    synchronized (SocketFactory.class) {
      if (theFactory == null) {
        theFactory = new DefaultSocketFactory();
      }
    }
    return theFactory;
  }

  public Socket createSocket() throws IOException {
    UnsupportedOperationException uop = new UnsupportedOperationException();
    SocketException se = new SocketException("Unconnected sockets not implemented");
    se.initCause(uop);
    throw se;
  }

  public abstract Socket createSocket(String host, int port) throws IOException, UnknownHostException;

  public abstract Socket createSocket(String host, int port, InetAddress localHost, int localPort)
      throws IOException, UnknownHostException;

  public abstract Socket createSocket(InetAddress host, int port) throws IOException;

  public abstract Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
      throws IOException;
}
