/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.net.www.protocol.https;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import sun.net.www.http.HttpClient;

public class DelegateHttpsURLConnection extends sun.net.www.protocol.http.HttpURLConnection {
  private final HttpsURLConnectionImpl owner;

  DelegateHttpsURLConnection(URL url, Proxy p, sun.net.www.protocol.http.Handler handler,
      HttpsURLConnectionImpl owner) throws IOException {
    super(url, p, handler);
    this.owner = owner;
  }

  @Override
  protected HttpClient getNewHttpClient(URL url, Proxy p, int connectTimeout, boolean useCache) throws IOException {
    return HttpsClient.New(owner.getSSLSocketFactory(), url, owner.getHostnameVerifier(), p, useCache,
        connectTimeout, this);
  }

  SSLSession sslSession() {
    HttpClient c = http;
    if (c == null) {
      throw new IllegalStateException("connection not yet open");
    }
    return c instanceof HttpsClient ? ((HttpsClient) c).getSession() : null;
  }

  java.security.cert.Certificate[] localCertificates() {
    SSLSession s = sslSession();
    if (s == null) {
      throw new IllegalStateException("connection not yet open");
    }
    return s.getLocalCertificates();
  }

  String cipherSuite() {
    SSLSession s = sslSession();
    if (s == null) {
      throw new IllegalStateException("connection not yet open");
    }
    return s.getCipherSuite();
  }

  java.security.cert.Certificate[] serverCertificates() throws SSLPeerUnverifiedException {
    SSLSession s = sslSession();
    if (s == null) {
      throw new IllegalStateException("connection not yet open");
    }
    return s.getPeerCertificates();
  }
}
