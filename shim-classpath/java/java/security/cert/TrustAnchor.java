/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.security.PublicKey;
import javax.security.auth.x500.X500Principal;

public class TrustAnchor {
  private final PublicKey pubKey;
  private final String caName;
  private final X500Principal caPrincipal;
  private final X509Certificate trustedCert;
  private byte[] ncBytes;

  public TrustAnchor(X509Certificate trustedCert, byte[] nameConstraints) {
    if (trustedCert == null) {
      throw new NullPointerException("the trustedCert parameter must be non-null");
    }
    this.trustedCert = trustedCert;
    this.pubKey = null;
    this.caName = null;
    this.caPrincipal = null;
    setNameConstraints(nameConstraints);
  }

  public TrustAnchor(X500Principal caPrincipal, PublicKey pubKey, byte[] nameConstraints) {
    if ((caPrincipal == null) || (pubKey == null)) {
      throw new NullPointerException();
    }
    this.trustedCert = null;
    this.caPrincipal = caPrincipal;
    this.caName = caPrincipal.getName();
    this.pubKey = pubKey;
    setNameConstraints(nameConstraints);
  }

  public TrustAnchor(String caName, PublicKey pubKey, byte[] nameConstraints) {
    if (pubKey == null) {
      throw new NullPointerException("the pubKey parameter must be non-null");
    }
    if (caName == null) {
      throw new NullPointerException("the caName parameter must be non-null");
    }
    if (caName.length() == 0) {
      throw new IllegalArgumentException("the caName parameter must be a non-empty String");
    }
    this.caPrincipal = new X500Principal(caName);
    this.pubKey = pubKey;
    this.caName = caName;
    this.trustedCert = null;
    setNameConstraints(nameConstraints);
  }

  public final X509Certificate getTrustedCert() {
    return this.trustedCert;
  }

  public final X500Principal getCA() {
    return this.caPrincipal != null ? this.caPrincipal : this.trustedCert.getSubjectX500Principal();
  }

  public final String getCAName() {
    return this.caName != null ? this.caName : this.trustedCert.getSubjectX500Principal().getName();
  }

  public final PublicKey getCAPublicKey() {
    return this.pubKey != null ? this.pubKey : this.trustedCert.getPublicKey();
  }

  private void setNameConstraints(byte[] bytes) {
    if (bytes == null) {
      ncBytes = null;
    } else {
      ncBytes = bytes.clone();
    }
  }

  public final byte[] getNameConstraints() {
    return ncBytes == null ? null : ncBytes.clone();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[\n");
    if (pubKey != null) {
      sb.append("  Trusted CA Public Key: " + pubKey.toString() + "\n");
      sb.append("  Trusted CA Issuer Name: " + getCAName() + "\n");
    } else {
      sb.append("  Trusted CA cert: " + trustedCert.toString() + "\n");
    }
    if (ncBytes != null) {
      sb.append("  Name Constraints: present\n");
    }
    return sb.toString();
  }
}
