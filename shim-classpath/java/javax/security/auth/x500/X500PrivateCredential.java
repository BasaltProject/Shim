/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.x500;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.security.auth.Destroyable;

public final class X500PrivateCredential implements Destroyable {
  private X509Certificate cert;
  private PrivateKey key;
  private String alias;

  public X500PrivateCredential(X509Certificate cert, PrivateKey key) {
    if (cert == null || key == null) {
      throw new IllegalArgumentException();
    }
    this.cert = cert;
    this.key = key;
    this.alias = null;
  }

  public X500PrivateCredential(X509Certificate cert, PrivateKey key, String alias) {
    if (cert == null || key == null || alias == null) {
      throw new IllegalArgumentException();
    }
    this.cert = cert;
    this.key = key;
    this.alias = alias;
  }

  public X509Certificate getCertificate() {
    return cert;
  }

  public PrivateKey getPrivateKey() {
    return key;
  }

  public String getAlias() {
    return alias;
  }

  @Override
  public void destroy() {
    cert = null;
    key = null;
    alias = null;
  }

  @Override
  public boolean isDestroyed() {
    return cert == null && key == null && alias == null;
  }
}
