/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.security.Principal;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Optional;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

public abstract class SecureCacheResponse extends CacheResponse {
  public SecureCacheResponse() {
  }

  public abstract String getCipherSuite();

  public abstract List<Certificate> getLocalCertificateChain();

  public abstract List<Certificate> getServerCertificateChain() throws SSLPeerUnverifiedException;

  public abstract Principal getPeerPrincipal() throws SSLPeerUnverifiedException;

  public abstract Principal getLocalPrincipal();

  public Optional<SSLSession> getSSLSession() {
    return Optional.empty();
  }
}
