/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

public abstract class SSLContextImpl extends SSLContextSpi {
  private X509ExtendedKeyManager keyManager;
  private X509TrustManager trustManager;
  private SecureRandom secureRandom;
  private boolean initialized;
  private final SSLSessionContextImpl clientContext = new SSLSessionContextImpl();
  private final SSLSessionContextImpl serverContext = new SSLSessionContextImpl();

  SSLContextImpl() {
  }

  abstract String[] enabledProtocols(boolean client);

  abstract String[] supportedProtocols();

  @Override
  protected synchronized void engineInit(KeyManager[] km, TrustManager[] tm, SecureRandom sr)
      throws KeyManagementException {
    initialized = false;
    try {
      keyManager = chooseKeyManager(km);
      trustManager = chooseTrustManager(tm);
    } catch (KeyManagementException e) {
      throw e;
    } catch (Exception e) {
      throw new KeyManagementException("Initialization failed", e);
    }
    secureRandom = sr == null ? new SecureRandom() : sr;
    initialized = true;
  }

  private X509ExtendedKeyManager chooseKeyManager(KeyManager[] kms) throws Exception {
    if (kms == null) {
      KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(null, null);
      kms = kmf.getKeyManagers();
    }
    for (KeyManager km : kms) {
      if (km instanceof X509ExtendedKeyManager) {
        return (X509ExtendedKeyManager) km;
      }
      if (km instanceof X509KeyManager) {
        return new AbstractKeyManagerWrapper((X509KeyManager) km);
      }
    }
    return null;
  }

  private X509TrustManager chooseTrustManager(TrustManager[] tms) throws Exception {
    if (tms == null) {
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init((KeyStore) null);
      tms = tmf.getTrustManagers();
    }
    for (TrustManager tm : tms) {
      if (tm instanceof X509TrustManager) {
        return (X509TrustManager) tm;
      }
    }
    return null;
  }

  private void checkInitialized() {
    if (!initialized) {
      throw new IllegalStateException("SSLContext is not initialized");
    }
  }

  X509ExtendedKeyManager keyManager() {
    return keyManager;
  }

  SSLSessionContextImpl clientSessionContext() {
    return clientContext;
  }

  SSLSessionContextImpl serverSessionContext() {
    return serverContext;
  }

  void checkServerTrusted(X509Certificate[] chain, String authType, Object owner) throws CertificateException {
    X509TrustManager tm = trustManager;
    if (tm == null) {
      throw new CertificateException("No X509TrustManager implementation available");
    }
    if (tm instanceof X509ExtendedTrustManager) {
      X509ExtendedTrustManager etm = (X509ExtendedTrustManager) tm;
      if (owner instanceof Socket) {
        etm.checkServerTrusted(chain, authType, (Socket) owner);
        return;
      }
      if (owner instanceof SSLEngine) {
        etm.checkServerTrusted(chain, authType, (SSLEngine) owner);
        return;
      }
    }
    tm.checkServerTrusted(chain, authType);
  }

  void checkClientTrusted(X509Certificate[] chain, String authType, Object owner) throws CertificateException {
    X509TrustManager tm = trustManager;
    if (tm == null) {
      throw new CertificateException("No X509TrustManager implementation available");
    }
    if (tm instanceof X509ExtendedTrustManager) {
      X509ExtendedTrustManager etm = (X509ExtendedTrustManager) tm;
      if (owner instanceof Socket) {
        etm.checkClientTrusted(chain, authType, (Socket) owner);
        return;
      }
      if (owner instanceof SSLEngine) {
        etm.checkClientTrusted(chain, authType, (SSLEngine) owner);
        return;
      }
    }
    tm.checkClientTrusted(chain, authType);
  }

  X509Certificate[] acceptedIssuers() {
    return trustManager == null ? new X509Certificate[0] : trustManager.getAcceptedIssuers();
  }

  SslConfig newConfig(boolean client) {
    SslConfig cfg = new SslConfig();
    cfg.clientMode = client;
    cfg.protocols = enabledProtocols(client);
    cfg.cipherSuites = TlsParams.supportedCipherSuites();
    return cfg;
  }

  @Override
  protected SSLSocketFactory engineGetSocketFactory() {
    checkInitialized();
    return new SSLSocketFactoryImpl(this);
  }

  @Override
  protected SSLServerSocketFactory engineGetServerSocketFactory() {
    checkInitialized();
    return new SSLServerSocketFactoryImpl(this);
  }

  @Override
  protected SSLEngine engineCreateSSLEngine() {
    checkInitialized();
    return new SSLEngineImpl(this, null, -1);
  }

  @Override
  protected SSLEngine engineCreateSSLEngine(String host, int port) {
    checkInitialized();
    return new SSLEngineImpl(this, host, port);
  }

  @Override
  protected SSLSessionContext engineGetServerSessionContext() {
    return serverContext;
  }

  @Override
  protected SSLSessionContext engineGetClientSessionContext() {
    return clientContext;
  }

  private static final String[] kTls13Up = {"TLSv1.3", "TLSv1.2"};

  public static final class TLSContext extends SSLContextImpl {
    public TLSContext() {
    }

    @Override
    String[] enabledProtocols(boolean client) {
      return kTls13Up.clone();
    }

    @Override
    String[] supportedProtocols() {
      return TlsParams.supportedProtocols();
    }
  }

  public static final class TLS13Context extends SSLContextImpl {
    public TLS13Context() {
    }

    @Override
    String[] enabledProtocols(boolean client) {
      return kTls13Up.clone();
    }

    @Override
    String[] supportedProtocols() {
      return TlsParams.supportedProtocols();
    }
  }

  public static final class TLS12Context extends SSLContextImpl {
    public TLS12Context() {
    }

    @Override
    String[] enabledProtocols(boolean client) {
      return new String[] {"TLSv1.2"};
    }

    @Override
    String[] supportedProtocols() {
      return new String[] {"TLSv1.2"};
    }
  }

  public static final class TLS11Context extends SSLContextImpl {
    public TLS11Context() {
    }

    @Override
    String[] enabledProtocols(boolean client) {
      return new String[0];
    }

    @Override
    String[] supportedProtocols() {
      return new String[0];
    }
  }

  public static final class TLS10Context extends SSLContextImpl {
    public TLS10Context() {
    }

    @Override
    String[] enabledProtocols(boolean client) {
      return new String[0];
    }

    @Override
    String[] supportedProtocols() {
      return new String[0];
    }
  }

  public static final class DefaultSSLContext extends SSLContextImpl {
    public DefaultSSLContext() throws Exception {
      engineInit(null, null, null);
    }

    @Override
    String[] enabledProtocols(boolean client) {
      return kTls13Up.clone();
    }

    @Override
    String[] supportedProtocols() {
      return TlsParams.supportedProtocols();
    }
  }

  private static final class AbstractKeyManagerWrapper extends X509ExtendedKeyManager {
    private final X509KeyManager km;

    AbstractKeyManagerWrapper(X509KeyManager km) {
      this.km = km;
    }

    @Override
    public String[] getClientAliases(String keyType, java.security.Principal[] issuers) {
      return km.getClientAliases(keyType, issuers);
    }

    @Override
    public String chooseClientAlias(String[] keyType, java.security.Principal[] issuers, Socket socket) {
      return km.chooseClientAlias(keyType, issuers, socket);
    }

    @Override
    public String[] getServerAliases(String keyType, java.security.Principal[] issuers) {
      return km.getServerAliases(keyType, issuers);
    }

    @Override
    public String chooseServerAlias(String keyType, java.security.Principal[] issuers, Socket socket) {
      return km.chooseServerAlias(keyType, issuers, socket);
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
      return km.getCertificateChain(alias);
    }

    @Override
    public java.security.PrivateKey getPrivateKey(String alias) {
      return km.getPrivateKey(alias);
    }
  }
}
