/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.security.KeyManagementException;
import java.security.SecureRandom;

public abstract class SSLContextSpi {
  public SSLContextSpi() {
  }

  protected abstract void engineInit(KeyManager[] km, TrustManager[] tm, SecureRandom sr)
      throws KeyManagementException;

  protected abstract SSLSocketFactory engineGetSocketFactory();

  protected abstract SSLServerSocketFactory engineGetServerSocketFactory();

  protected abstract SSLEngine engineCreateSSLEngine();

  protected abstract SSLEngine engineCreateSSLEngine(String host, int port);

  protected abstract SSLSessionContext engineGetServerSessionContext();

  protected abstract SSLSessionContext engineGetClientSessionContext();

  private SSLSocket getDefaultSocket() {
    try {
      SSLSocketFactory factory = engineGetSocketFactory();
      return (SSLSocket) factory.createSocket();
    } catch (java.io.IOException e) {
      throw new UnsupportedOperationException("Could not obtain parameters", e);
    }
  }

  protected SSLParameters engineGetDefaultSSLParameters() {
    SSLSocket socket = getDefaultSocket();
    return socket.getSSLParameters();
  }

  protected SSLParameters engineGetSupportedSSLParameters() {
    SSLSocket socket = getDefaultSocket();
    SSLParameters params = new SSLParameters();
    params.setCipherSuites(socket.getSupportedCipherSuites());
    params.setProtocols(socket.getSupportedProtocols());
    return params;
  }
}
