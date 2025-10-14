/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public abstract class X509ExtendedTrustManager implements X509TrustManager {
  public X509ExtendedTrustManager() {
  }

  public abstract void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
      throws CertificateException;

  public abstract void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
      throws CertificateException;

  public abstract void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
      throws CertificateException;

  public abstract void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
      throws CertificateException;
}
