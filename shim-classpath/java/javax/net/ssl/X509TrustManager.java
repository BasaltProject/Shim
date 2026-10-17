/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public interface X509TrustManager extends TrustManager {
  void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException;

  void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException;

  X509Certificate[] getAcceptedIssuers();
}
