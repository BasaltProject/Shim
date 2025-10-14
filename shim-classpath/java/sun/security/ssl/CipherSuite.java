/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class CipherSuite {
  static final int kAeadGcm = 0;
  static final int kAeadChacha = 1;

  final int id;
  final String name;
  final boolean tls13;
  final String keyExchange;
  final String hash;
  final String hmac;
  final int keyLength;
  final int aead;

  private CipherSuite(int id, String name, boolean tls13, String keyExchange, int bits, String hash, int aead) {
    this.id = id;
    this.name = name;
    this.tls13 = tls13;
    this.keyExchange = keyExchange;
    this.hash = hash;
    this.hmac = hash.equals("SHA-384") ? "HmacSHA384" : "HmacSHA256";
    this.keyLength = bits / 8;
    this.aead = aead;
  }

  private static final CipherSuite[] kSuites = {
    new CipherSuite(0x1302, "TLS_AES_256_GCM_SHA384", true, "TLS13", 256, "SHA-384", kAeadGcm),
    new CipherSuite(0x1301, "TLS_AES_128_GCM_SHA256", true, "TLS13", 128, "SHA-256", kAeadGcm),
    new CipherSuite(0xc02c, "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", false, "ECDHE_ECDSA", 256, "SHA-384", kAeadGcm),
    new CipherSuite(0xc02b, "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", false, "ECDHE_ECDSA", 128, "SHA-256", kAeadGcm),
    new CipherSuite(0xc030, "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", false, "ECDHE_RSA", 256, "SHA-384", kAeadGcm),
    new CipherSuite(0xc02f, "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", false, "ECDHE_RSA", 128, "SHA-256", kAeadGcm),
  };

  static List<CipherSuite> all() {
    return Collections.unmodifiableList(Arrays.asList(kSuites));
  }

  static CipherSuite byId(int id) {
    for (CipherSuite s : kSuites) {
      if (s.id == id) {
        return s;
      }
    }
    return null;
  }

  static CipherSuite byName(String name) {
    for (CipherSuite s : kSuites) {
      if (s.name.equals(name)) {
        return s;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return name;
  }
}
