/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.x509;

public final class AlgorithmId {
  private static final String[][] kNames = {
    {"1.2.840.113549.1.1.1", "RSA"},
    {"1.2.840.113549.1.1.4", "MD5withRSA"},
    {"1.2.840.113549.1.1.2", "MD2withRSA"},
    {"1.2.840.113549.1.1.5", "SHA1withRSA"},
    {"1.3.14.3.2.29", "SHA1withRSA"},
    {"1.2.840.113549.1.1.14", "SHA224withRSA"},
    {"1.2.840.113549.1.1.11", "SHA256withRSA"},
    {"1.2.840.113549.1.1.12", "SHA384withRSA"},
    {"1.2.840.113549.1.1.13", "SHA512withRSA"},
    {"1.2.840.113549.1.1.15", "SHA512/224withRSA"},
    {"1.2.840.113549.1.1.16", "SHA512/256withRSA"},
    {"2.16.840.1.101.3.4.3.13", "SHA3-224withRSA"},
    {"2.16.840.1.101.3.4.3.14", "SHA3-256withRSA"},
    {"2.16.840.1.101.3.4.3.15", "SHA3-384withRSA"},
    {"2.16.840.1.101.3.4.3.16", "SHA3-512withRSA"},
    {"1.2.840.113549.1.1.10", "RSASSA-PSS"},
    {"1.2.840.10045.4.1", "SHA1withECDSA"},
    {"1.2.840.10045.4.3.1", "SHA224withECDSA"},
    {"1.2.840.10045.4.3.2", "SHA256withECDSA"},
    {"1.2.840.10045.4.3.3", "SHA384withECDSA"},
    {"1.2.840.10045.4.3.4", "SHA512withECDSA"},
    {"2.16.840.1.101.3.4.3.9", "SHA3-224withECDSA"},
    {"2.16.840.1.101.3.4.3.10", "SHA3-256withECDSA"},
    {"2.16.840.1.101.3.4.3.11", "SHA3-384withECDSA"},
    {"2.16.840.1.101.3.4.3.12", "SHA3-512withECDSA"},
    {"1.2.840.10040.4.3", "SHA1withDSA"},
    {"2.16.840.1.101.3.4.3.1", "SHA224withDSA"},
    {"2.16.840.1.101.3.4.3.2", "SHA256withDSA"},
    {"1.3.101.112", "Ed25519"},
    {"1.3.101.113", "Ed448"},
    {"1.2.840.10045.2.1", "EC"},
    {"1.2.840.10040.4.1", "DSA"},
    {"1.3.101.110", "X25519"},
    {"1.3.101.111", "X448"},
  };

  private AlgorithmId() {
  }

  public static String nameOf(String oid) {
    for (String[] e : kNames) {
      if (e[0].equals(oid)) {
        return e[1];
      }
    }
    return oid;
  }

  public static String keyAlgorithm(String oid) {
    switch (oid) {
    case "1.2.840.113549.1.1.1":
      return "RSA";
    case "1.2.840.113549.1.1.10":
      return "RSASSA-PSS";
    case "1.2.840.10045.2.1":
      return "EC";
    case "1.2.840.10040.4.1":
      return "DSA";
    case "1.3.101.112":
      return "Ed25519";
    case "1.3.101.113":
      return "Ed448";
    case "1.3.101.110":
      return "X25519";
    case "1.3.101.111":
      return "X448";
    default:
      return null;
    }
  }
}
