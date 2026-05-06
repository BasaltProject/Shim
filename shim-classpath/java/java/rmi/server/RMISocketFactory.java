/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class RMISocketFactory implements RMIClientSocketFactory, RMIServerSocketFactory {
  private static RMISocketFactory factory = null;
  private static RMISocketFactory defaultSocketFactory;
  private static RMIFailureHandler handler = null;

  public RMISocketFactory() {
    super();
  }

  public abstract Socket createSocket(String host, int port) throws IOException;

  public abstract ServerSocket createServerSocket(int port) throws IOException;

  public synchronized static void setSocketFactory(RMISocketFactory fac) throws IOException {
    if (factory != null) {
      throw new SocketException("factory already defined");
    }
    factory = fac;
  }

  public synchronized static RMISocketFactory getSocketFactory() {
    return factory != null ? factory : getDefaultSocketFactory();
  }

  public synchronized static RMISocketFactory getDefaultSocketFactory() {
    if (defaultSocketFactory == null) {
      defaultSocketFactory = new RMISocketFactory() {
        public Socket createSocket(String host, int port) throws IOException {
          return new Socket(host, port);
        }

        public ServerSocket createServerSocket(int port) throws IOException {
          return new ServerSocket(port);
        }
      };
    }
    return defaultSocketFactory;
  }

  public synchronized static void setFailureHandler(RMIFailureHandler fh) {
    handler = fh;
  }

  public synchronized static RMIFailureHandler getFailureHandler() {
    return handler;
  }

  private static final class SocketException extends IOException {
    private static final long serialVersionUID = 1L;

    SocketException(String m) {
      super(m);
    }
  }
}
