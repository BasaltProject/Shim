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
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;

final class SSLServerSocketImpl extends SSLServerSocket {
  private final SSLContextImpl context;
  private final SslConfig cfg;

  SSLServerSocketImpl(SSLContextImpl context) throws IOException {
    super();
    this.context = context;
    this.cfg = context.newConfig(false);
  }

  SSLServerSocketImpl(SSLContextImpl context, int port, int backlog, InetAddress address) throws IOException {
    super();
    this.context = context;
    this.cfg = context.newConfig(false);
    bind(new InetSocketAddress(address, port), backlog);
  }

  @Override
  public Socket accept() throws IOException {
    Socket s = super.accept();
    SSLSocketImpl ssl = new SSLSocketImpl(context, s, false);
    synchronized (this) {
      ssl.applyConfig(cfg);
    }
    return ssl;
  }

  @Override
  public synchronized String[] getEnabledCipherSuites() {
    return cfg.cipherSuites.clone();
  }

  @Override
  public synchronized void setEnabledCipherSuites(String[] suites) {
    TlsParams.checkSuites(suites);
    cfg.cipherSuites = suites.clone();
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return TlsParams.supportedCipherSuites();
  }

  @Override
  public String[] getSupportedProtocols() {
    return TlsParams.supportedProtocols();
  }

  @Override
  public synchronized String[] getEnabledProtocols() {
    return cfg.protocols.clone();
  }

  @Override
  public synchronized void setEnabledProtocols(String[] protocols) {
    TlsParams.checkProtocols(protocols);
    cfg.protocols = protocols.clone();
  }

  @Override
  public synchronized void setNeedClientAuth(boolean need) {
    cfg.needClientAuth = need;
    if (need) {
      cfg.wantClientAuth = false;
    }
  }

  @Override
  public synchronized boolean getNeedClientAuth() {
    return cfg.needClientAuth;
  }

  @Override
  public synchronized void setWantClientAuth(boolean want) {
    cfg.wantClientAuth = want;
    if (want) {
      cfg.needClientAuth = false;
    }
  }

  @Override
  public synchronized boolean getWantClientAuth() {
    return cfg.wantClientAuth;
  }

  @Override
  public synchronized void setUseClientMode(boolean mode) {
    if (cfg.clientMode != mode) {
      cfg.clientMode = mode;
      cfg.protocols = context.enabledProtocols(mode);
    }
  }

  @Override
  public synchronized boolean getUseClientMode() {
    return cfg.clientMode;
  }

  @Override
  public synchronized void setEnableSessionCreation(boolean flag) {
    cfg.enableSessionCreation = flag;
  }

  @Override
  public synchronized boolean getEnableSessionCreation() {
    return cfg.enableSessionCreation;
  }

  @Override
  public synchronized SSLParameters getSSLParameters() {
    SSLParameters params = super.getSSLParameters();
    params.setEndpointIdentificationAlgorithm(cfg.endpointIdentification);
    params.setServerNames(cfg.serverNames);
    params.setSNIMatchers(cfg.sniMatchers);
    params.setUseCipherSuitesOrder(cfg.useCipherSuitesOrder);
    params.setApplicationProtocols(cfg.applicationProtocols);
    params.setSignatureSchemes(cfg.signatureSchemes);
    params.setNamedGroups(cfg.namedGroups);
    return params;
  }

  @Override
  public synchronized void setSSLParameters(SSLParameters params) {
    super.setSSLParameters(params);
    cfg.endpointIdentification = params.getEndpointIdentificationAlgorithm();
    if (params.getServerNames() != null) {
      cfg.serverNames = params.getServerNames();
    }
    if (params.getSNIMatchers() != null) {
      cfg.sniMatchers = params.getSNIMatchers();
    }
    cfg.useCipherSuitesOrder = params.getUseCipherSuitesOrder();
    cfg.applicationProtocols = params.getApplicationProtocols();
    if (params.getSignatureSchemes() != null) {
      cfg.signatureSchemes = params.getSignatureSchemes();
    }
    if (params.getNamedGroups() != null) {
      cfg.namedGroups = params.getNamedGroups();
    }
  }

  @Override
  public String toString() {
    return "[SSL: " + super.toString() + "]";
  }
}
