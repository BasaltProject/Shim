/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.Objects;
import sun.security.jca.GetInstance;

public class SSLContext {
  private final Provider provider;
  private final SSLContextSpi contextSpi;
  private final String protocol;
  private static SSLContext defaultContext;

  protected SSLContext(SSLContextSpi contextSpi, Provider provider, String protocol) {
    this.contextSpi = contextSpi;
    this.provider = provider;
    this.protocol = protocol;
  }

  public static synchronized SSLContext getDefault() throws NoSuchAlgorithmException {
    if (defaultContext == null) {
      defaultContext = SSLContext.getInstance("Default");
    }
    return defaultContext;
  }

  public static synchronized void setDefault(SSLContext context) {
    if (context == null) {
      throw new NullPointerException();
    }
    defaultContext = context;
  }

  public static SSLContext getInstance(String protocol) throws NoSuchAlgorithmException {
    Objects.requireNonNull(protocol, "null protocol name");
    GetInstance.Instance instance = GetInstance.getInstance("SSLContext", SSLContextSpi.class, protocol);
    return new SSLContext((SSLContextSpi) instance.impl, instance.provider, protocol);
  }

  public static SSLContext getInstance(String protocol, String provider) throws NoSuchAlgorithmException,
      NoSuchProviderException {
    Objects.requireNonNull(protocol, "null protocol name");
    GetInstance.Instance instance = GetInstance.getInstance("SSLContext", SSLContextSpi.class, protocol, provider);
    return new SSLContext((SSLContextSpi) instance.impl, instance.provider, protocol);
  }

  public static SSLContext getInstance(String protocol, Provider provider) throws NoSuchAlgorithmException {
    Objects.requireNonNull(protocol, "null protocol name");
    GetInstance.Instance instance = GetInstance.getInstance("SSLContext", SSLContextSpi.class, protocol, provider);
    return new SSLContext((SSLContextSpi) instance.impl, instance.provider, protocol);
  }

  public final String getProtocol() {
    return this.protocol;
  }

  public final Provider getProvider() {
    return this.provider;
  }

  public final void init(KeyManager[] km, TrustManager[] tm, SecureRandom random) throws KeyManagementException {
    contextSpi.engineInit(km, tm, random);
  }

  public final SSLSocketFactory getSocketFactory() {
    return contextSpi.engineGetSocketFactory();
  }

  public final SSLServerSocketFactory getServerSocketFactory() {
    return contextSpi.engineGetServerSocketFactory();
  }

  public final SSLEngine createSSLEngine() {
    try {
      return contextSpi.engineCreateSSLEngine();
    } catch (AbstractMethodError e) {
      UnsupportedOperationException unsup = new UnsupportedOperationException(
          "Provider: " + getProvider() + " doesn't support this operation");
      unsup.initCause(e);
      throw unsup;
    }
  }

  public final SSLEngine createSSLEngine(String peerHost, int peerPort) {
    try {
      return contextSpi.engineCreateSSLEngine(peerHost, peerPort);
    } catch (AbstractMethodError e) {
      UnsupportedOperationException unsup = new UnsupportedOperationException(
          "Provider: " + getProvider() + " does not support this operation");
      unsup.initCause(e);
      throw unsup;
    }
  }

  public final SSLSessionContext getServerSessionContext() {
    return contextSpi.engineGetServerSessionContext();
  }

  public final SSLSessionContext getClientSessionContext() {
    return contextSpi.engineGetClientSessionContext();
  }

  public final SSLParameters getDefaultSSLParameters() {
    return contextSpi.engineGetDefaultSSLParameters();
  }

  public final SSLParameters getSupportedSSLParameters() {
    return contextSpi.engineGetSupportedSSLParameters();
  }
}
