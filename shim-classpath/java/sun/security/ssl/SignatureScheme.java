/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Arrays;
import java.util.List;

final class SignatureScheme {
  static final int kEcdsa = 0;
  static final int kRsaPss = 1;
  static final int kRsaPkcs1 = 2;

  final int id;
  final String name;
  final int kind;
  final String jcaName;
  final String digest;
  final int saltLength;
  final String curve;

  private SignatureScheme(int id, String name, int kind, String jcaName, String digest, int saltLength,
      String curve) {
    this.id = id;
    this.name = name;
    this.kind = kind;
    this.jcaName = jcaName;
    this.digest = digest;
    this.saltLength = saltLength;
    this.curve = curve;
  }

  private static final SignatureScheme[] kAll = {
    new SignatureScheme(0x0403, "ecdsa_secp256r1_sha256", kEcdsa, "SHA256withECDSA", "SHA-256", 0, "secp256r1"),
    new SignatureScheme(0x0503, "ecdsa_secp384r1_sha384", kEcdsa, "SHA384withECDSA", "SHA-384", 0, "secp384r1"),
    new SignatureScheme(0x0603, "ecdsa_secp521r1_sha512", kEcdsa, "SHA512withECDSA", "SHA-512", 0, "secp521r1"),
    new SignatureScheme(0x0804, "rsa_pss_rsae_sha256", kRsaPss, "RSASSA-PSS", "SHA-256", 32, null),
    new SignatureScheme(0x0805, "rsa_pss_rsae_sha384", kRsaPss, "RSASSA-PSS", "SHA-384", 48, null),
    new SignatureScheme(0x0806, "rsa_pss_rsae_sha512", kRsaPss, "RSASSA-PSS", "SHA-512", 64, null),
    new SignatureScheme(0x0401, "rsa_pkcs1_sha256", kRsaPkcs1, "SHA256withRSA", "SHA-256", 0, null),
    new SignatureScheme(0x0501, "rsa_pkcs1_sha384", kRsaPkcs1, "SHA384withRSA", "SHA-384", 0, null),
    new SignatureScheme(0x0601, "rsa_pkcs1_sha512", kRsaPkcs1, "SHA512withRSA", "SHA-512", 0, null),
  };

  static List<SignatureScheme> all() {
    return Arrays.asList(kAll);
  }

  static SignatureScheme byId(int id) {
    for (SignatureScheme s : kAll) {
      if (s.id == id) {
        return s;
      }
    }
    return null;
  }

  static SignatureScheme byName(String name) {
    for (SignatureScheme s : kAll) {
      if (s.name.equals(name)) {
        return s;
      }
    }
    return null;
  }

  boolean usableWith(String keyAlgorithm, PublicKeyInfo key, boolean tls13) {
    if (kind == kEcdsa) {
      if (!keyAlgorithm.equals("EC")) {
        return false;
      }
      return !tls13 || curve.equals(key.curveName);
    }
    if (kind == kRsaPkcs1) {
      return keyAlgorithm.equals("RSA");
    }
    return keyAlgorithm.equals("RSA") || keyAlgorithm.equals("RSASSA-PSS");
  }

  Signature newSignature() throws GeneralSecurityException {
    Signature sig = Signature.getInstance(jcaName);
    if (kind == kRsaPss) {
      MGF1ParameterSpec mgf = new MGF1ParameterSpec(digest);
      sig.setParameter(new PSSParameterSpec(digest, "MGF1", mgf, saltLength, 1));
    }
    return sig;
  }

  @Override
  public String toString() {
    return name;
  }

  static final class PublicKeyInfo {
    final String curveName;

    PublicKeyInfo(String curveName) {
      this.curveName = curveName;
    }

    static PublicKeyInfo of(java.security.PublicKey key) {
      if (key instanceof java.security.interfaces.ECPublicKey) {
        int bits = ((java.security.interfaces.ECPublicKey) key).getParams().getOrder().bitLength();
        return new PublicKeyInfo(bits == 256 ? "secp256r1" : bits == 384 ? "secp384r1" : bits == 521 ? "secp521r1"
            : "unknown");
      }
      return new PublicKeyInfo(null);
    }
  }
}
