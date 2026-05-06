/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.rmi.ssl;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Arrays;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class SslRMIServerSocketFactory implements RMIServerSocketFactory {
  private final String[] enabledCipherSuites;
  private final String[] enabledProtocols;
  private final boolean needClientAuth;
  private final SSLContext context;

  public SslRMIServerSocketFactory() {
    this(null, null, false);
  }

  public SslRMIServerSocketFactory(String[] enabledCipherSuites, String[] enabledProtocols, boolean needClientAuth)
      throws IllegalArgumentException {
    this(null, enabledCipherSuites, enabledProtocols, needClientAuth);
  }

  public SslRMIServerSocketFactory(SSLContext context, String[] enabledCipherSuites, String[] enabledProtocols,
      boolean needClientAuth) throws IllegalArgumentException {
    this.enabledCipherSuites = enabledCipherSuites == null ? null : enabledCipherSuites.clone();
    this.enabledProtocols = enabledProtocols == null ? null : enabledProtocols.clone();
    this.needClientAuth = needClientAuth;
    this.context = context;
    if (this.enabledCipherSuites != null || this.enabledProtocols != null) {
      SSLSocketFactoryProbe.check(context, this.enabledCipherSuites, this.enabledProtocols);
    }
  }

  public final String[] getEnabledCipherSuites() {
    return enabledCipherSuites == null ? null : enabledCipherSuites.clone();
  }

  public final String[] getEnabledProtocols() {
    return enabledProtocols == null ? null : enabledProtocols.clone();
  }

  public final boolean getNeedClientAuth() {
    return needClientAuth;
  }

  public ServerSocket createServerSocket(int port) throws IOException {
    final SSLServerSocketFactory factory = context == null ? (SSLServerSocketFactory) SSLServerSocketFactory.getDefault()
        : context.getServerSocketFactory();
    SSLServerSocket socket = (SSLServerSocket) factory.createServerSocket(port);
    if (enabledCipherSuites != null) {
      socket.setEnabledCipherSuites(enabledCipherSuites);
    }
    if (enabledProtocols != null) {
      socket.setEnabledProtocols(enabledProtocols);
    }
    socket.setNeedClientAuth(needClientAuth);
    return socket;
  }

  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof SslRMIServerSocketFactory)) {
      return false;
    }
    SslRMIServerSocketFactory that = (SslRMIServerSocketFactory) obj;
    return getClass().equals(that.getClass()) && checkParameters(that);
  }

  private boolean checkParameters(SslRMIServerSocketFactory that) {
    if (context == null ? that.context != null : !context.equals(that.context)) {
      return false;
    }
    if (needClientAuth != that.needClientAuth) {
      return false;
    }
    if ((enabledCipherSuites == null && that.enabledCipherSuites != null)
        || (enabledCipherSuites != null && that.enabledCipherSuites == null)) {
      return false;
    }
    if (enabledCipherSuites != null && !Arrays.equals(enabledCipherSuites, that.enabledCipherSuites)) {
      return false;
    }
    if ((enabledProtocols == null && that.enabledProtocols != null)
        || (enabledProtocols != null && that.enabledProtocols == null)) {
      return false;
    }
    return enabledProtocols == null || Arrays.equals(enabledProtocols, that.enabledProtocols);
  }

  public int hashCode() {
    return getClass().hashCode() + (context == null ? 0 : context.hashCode()) + (needClientAuth ? 1 : 0)
        + (enabledCipherSuites == null ? 0 : Arrays.asList(enabledCipherSuites).hashCode())
        + (enabledProtocols == null ? 0 : Arrays.asList(enabledProtocols).hashCode());
  }

  private static final class SSLSocketFactoryProbe {
    static void check(SSLContext context, String[] suites, String[] protocols) {
      try {
        SSLServerSocketFactory f = context == null ? (SSLServerSocketFactory) SSLServerSocketFactory.getDefault()
            : context.getServerSocketFactory();
        SSLServerSocket s = (SSLServerSocket) f.createServerSocket();
        try {
          if (suites != null) {
            s.setEnabledCipherSuites(suites);
          }
          if (protocols != null) {
            s.setEnabledProtocols(protocols);
          }
        } finally {
          s.close();
        }
      } catch (IOException e) {
        throw new IllegalArgumentException("Unable to check if the cipher suites and protocols to enable are "
            + "supported");
      }
    }
  }
}
