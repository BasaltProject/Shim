/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Optional;

public abstract class HttpsURLConnection extends HttpURLConnection {
  private static HostnameVerifier defaultHostnameVerifier = new DefaultHostnameVerifier();
  private static SSLSocketFactory defaultSSLSocketFactory;

  protected HostnameVerifier hostnameVerifier = defaultHostnameVerifier;
  private SSLSocketFactory sslSocketFactory = getDefaultSSLSocketFactory();

  protected HttpsURLConnection(URL url) {
    super(url);
  }

  public abstract String getCipherSuite();

  public abstract Certificate[] getLocalCertificates();

  public abstract Certificate[] getServerCertificates() throws SSLPeerUnverifiedException;

  public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
    Certificate[] certs = getServerCertificates();
    return ((X509Certificate) certs[0]).getSubjectX500Principal();
  }

  public Principal getLocalPrincipal() {
    Certificate[] certs = getLocalCertificates();
    if (certs != null) {
      return ((X509Certificate) certs[0]).getSubjectX500Principal();
    }
    return null;
  }

  public static void setDefaultHostnameVerifier(HostnameVerifier v) {
    if (v == null) {
      throw new IllegalArgumentException("no default HostnameVerifier specified");
    }
    defaultHostnameVerifier = v;
  }

  public static HostnameVerifier getDefaultHostnameVerifier() {
    return defaultHostnameVerifier;
  }

  public void setHostnameVerifier(HostnameVerifier v) {
    if (v == null) {
      throw new IllegalArgumentException("no HostnameVerifier specified");
    }
    hostnameVerifier = v;
  }

  public HostnameVerifier getHostnameVerifier() {
    return hostnameVerifier;
  }

  public static void setDefaultSSLSocketFactory(SSLSocketFactory sf) {
    if (sf == null) {
      throw new IllegalArgumentException("no default SSLSocketFactory specified");
    }
    defaultSSLSocketFactory = sf;
  }

  public static SSLSocketFactory getDefaultSSLSocketFactory() {
    if (defaultSSLSocketFactory == null) {
      defaultSSLSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    }
    return defaultSSLSocketFactory;
  }

  public void setSSLSocketFactory(SSLSocketFactory sf) {
    if (sf == null) {
      throw new IllegalArgumentException("no SSLSocketFactory specified");
    }
    sslSocketFactory = sf;
  }

  public SSLSocketFactory getSSLSocketFactory() {
    return sslSocketFactory;
  }

  public Optional<SSLSession> getSSLSession() {
    return Optional.empty();
  }

  private static final class DefaultHostnameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {
      return false;
    }
  }
}
