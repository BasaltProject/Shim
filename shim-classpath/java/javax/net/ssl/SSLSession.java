/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.security.Principal;
import java.security.cert.Certificate;

public interface SSLSession {
  byte[] getId();

  SSLSessionContext getSessionContext();

  long getCreationTime();

  long getLastAccessedTime();

  void invalidate();

  boolean isValid();

  void putValue(String name, Object value);

  Object getValue(String name);

  void removeValue(String name);

  String[] getValueNames();

  Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException;

  Certificate[] getLocalCertificates();

  default javax.security.cert.X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
    throw new UnsupportedOperationException("This method is deprecated and marked for removal. "
        + "Use the getPeerCertificates() method instead.");
  }

  Principal getPeerPrincipal() throws SSLPeerUnverifiedException;

  Principal getLocalPrincipal();

  String getCipherSuite();

  String getProtocol();

  String getPeerHost();

  int getPeerPort();

  int getPacketBufferSize();

  int getApplicationBufferSize();
}
