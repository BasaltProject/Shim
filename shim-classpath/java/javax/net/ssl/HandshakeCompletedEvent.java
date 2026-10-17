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
import java.util.EventObject;

public class HandshakeCompletedEvent extends EventObject {
  private static final long serialVersionUID = 7914963744257769778L;

  private final transient SSLSession session;

  public HandshakeCompletedEvent(SSLSocket sock, SSLSession s) {
    super(sock);
    session = s;
  }

  public SSLSession getSession() {
    return session;
  }

  public String getCipherSuite() {
    return session.getCipherSuite();
  }

  public Certificate[] getLocalCertificates() {
    return session.getLocalCertificates();
  }

  public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
    return session.getPeerCertificates();
  }

  public javax.security.cert.X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
    return session.getPeerCertificateChain();
  }

  public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
    return session.getPeerPrincipal();
  }

  public Principal getLocalPrincipal() {
    return session.getLocalPrincipal();
  }

  public SSLSocket getSocket() {
    return (SSLSocket) getSource();
  }
}
