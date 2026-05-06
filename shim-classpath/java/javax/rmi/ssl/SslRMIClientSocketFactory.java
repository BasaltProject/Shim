/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.rmi.ssl;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SslRMIClientSocketFactory implements RMIClientSocketFactory, Serializable {
  private static final long serialVersionUID = -8310631444933958385L;

  private static SSLSocketFactory defaultSocketFactory = null;

  public SslRMIClientSocketFactory() {
  }

  public Socket createSocket(String host, int port) throws IOException {
    final SSLSocketFactory sslSocketFactory = getDefaultClientSocketFactory();
    final SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(host, port);
    final String enabledCipherSuites = System.getProperty("javax.rmi.ssl.client.enabledCipherSuites");
    if (enabledCipherSuites != null) {
      sslSocket.setEnabledCipherSuites(enabledCipherSuites.split(","));
    }
    final String enabledProtocols = System.getProperty("javax.rmi.ssl.client.enabledProtocols");
    if (enabledProtocols != null) {
      sslSocket.setEnabledProtocols(enabledProtocols.split(","));
    }
    sslSocket.getSession().getPeerCertificates();
    return sslSocket;
  }

  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    return this.getClass().equals(obj.getClass());
  }

  public int hashCode() {
    return getClass().hashCode();
  }

  private static synchronized SSLSocketFactory getDefaultClientSocketFactory() {
    if (defaultSocketFactory == null) {
      defaultSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    }
    return defaultSocketFactory;
  }
}
