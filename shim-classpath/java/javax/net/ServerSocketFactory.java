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
import java.net.ServerSocket;
import java.net.SocketException;

public abstract class ServerSocketFactory {
  private static ServerSocketFactory theFactory;

  protected ServerSocketFactory() {
  }

  public static ServerSocketFactory getDefault() {
    synchronized (ServerSocketFactory.class) {
      if (theFactory == null) {
        theFactory = new DefaultServerSocketFactory();
      }
    }
    return theFactory;
  }

  public ServerSocket createServerSocket() throws IOException {
    throw new SocketException("Unbound server sockets not implemented");
  }

  public abstract ServerSocket createServerSocket(int port) throws IOException;

  public abstract ServerSocket createServerSocket(int port, int backlog) throws IOException;

  public abstract ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress) throws IOException;
}
