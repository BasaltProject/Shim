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
  static final int kCbc = 2;

  final int id;
  final String name;
  final boolean tls13;
  final String keyExchange;
  final String hash;
  final String hmac;
  final int keyLength;
  final int aead;
  final String macName;
  final int macLength;
  final boolean scsv;

  private CipherSuite(int id, String name, boolean tls13, String keyExchange, int bits, String hash, int aead,
      String macName, int macLength, boolean scsv) {
    this.id = id;
    this.name = name;
    this.tls13 = tls13;
    this.keyExchange = keyExchange;
    this.hash = hash;
    this.hmac = hash.equals("SHA-384") ? "HmacSHA384" : "HmacSHA256";
    this.keyLength = bits / 8;
    this.aead = aead;
    this.macName = macName;
    this.macLength = macLength;
    this.scsv = scsv;
  }

  private static CipherSuite aead(int id, String name, boolean tls13, String kx, int bits, String hash, int kind) {
    return new CipherSuite(id, name, tls13, kx, bits, hash, kind, null, 0, false);
  }

  private static CipherSuite cbc(int id, String name, String kx, int bits, String mac) {
    boolean sha1 = mac.equals("SHA1");
    String prf = mac.equals("SHA384") ? "SHA-384" : "SHA-256";
    return new CipherSuite(id, name, false, kx, bits, prf, kCbc, "Hmac" + mac, sha1 ? 20 : mac.equals("SHA256") ? 32
        : 48, false);
  }

  private static final CipherSuite[] kSuites = {
    aead(0x1302, "TLS_AES_256_GCM_SHA384", true, "TLS13", 256, "SHA-384", kAeadGcm),
    aead(0x1301, "TLS_AES_128_GCM_SHA256", true, "TLS13", 128, "SHA-256", kAeadGcm),
    aead(0x1303, "TLS_CHACHA20_POLY1305_SHA256", true, "TLS13", 256, "SHA-256", kAeadChacha),
    aead(0xc02c, "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", false, "ECDHE_ECDSA", 256, "SHA-384", kAeadGcm),
    aead(0xc02b, "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", false, "ECDHE_ECDSA", 128, "SHA-256", kAeadGcm),
    aead(0xcca9, "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256", false, "ECDHE_ECDSA", 256, "SHA-256", kAeadChacha),
    aead(0xc030, "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", false, "ECDHE_RSA", 256, "SHA-384", kAeadGcm),
    aead(0xcca8, "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256", false, "ECDHE_RSA", 256, "SHA-256", kAeadChacha),
    aead(0xc02f, "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", false, "ECDHE_RSA", 128, "SHA-256", kAeadGcm),
    aead(0x009f, "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384", false, "DHE_RSA", 256, "SHA-384", kAeadGcm),
    aead(0xccaa, "TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256", false, "DHE_RSA", 256, "SHA-256", kAeadChacha),
    aead(0x00a3, "TLS_DHE_DSS_WITH_AES_256_GCM_SHA384", false, "DHE_DSS", 256, "SHA-384", kAeadGcm),
    aead(0x009e, "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256", false, "DHE_RSA", 128, "SHA-256", kAeadGcm),
    aead(0x00a2, "TLS_DHE_DSS_WITH_AES_128_GCM_SHA256", false, "DHE_DSS", 128, "SHA-256", kAeadGcm),
    cbc(0xc024, "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", "ECDHE_ECDSA", 256, "SHA384"),
    cbc(0xc028, "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", "ECDHE_RSA", 256, "SHA384"),
    cbc(0xc023, "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", "ECDHE_ECDSA", 128, "SHA256"),
    cbc(0xc027, "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", "ECDHE_RSA", 128, "SHA256"),
    cbc(0x006b, "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256", "DHE_RSA", 256, "SHA256"),
    cbc(0x006a, "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256", "DHE_DSS", 256, "SHA256"),
    cbc(0x0067, "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256", "DHE_RSA", 128, "SHA256"),
    cbc(0x0040, "TLS_DHE_DSS_WITH_AES_128_CBC_SHA256", "DHE_DSS", 128, "SHA256"),
    cbc(0xc00a, "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA", "ECDHE_ECDSA", 256, "SHA1"),
    cbc(0xc014, "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", "ECDHE_RSA", 256, "SHA1"),
    cbc(0xc009, "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA", "ECDHE_ECDSA", 128, "SHA1"),
    cbc(0xc013, "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "ECDHE_RSA", 128, "SHA1"),
    cbc(0x0039, "TLS_DHE_RSA_WITH_AES_256_CBC_SHA", "DHE_RSA", 256, "SHA1"),
    cbc(0x0038, "TLS_DHE_DSS_WITH_AES_256_CBC_SHA", "DHE_DSS", 256, "SHA1"),
    cbc(0x0033, "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "DHE_RSA", 128, "SHA1"),
    cbc(0x0032, "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "DHE_DSS", 128, "SHA1"),
    new CipherSuite(0x00ff, "TLS_EMPTY_RENEGOTIATION_INFO_SCSV", false, "SCSV", 0, "SHA-256", kAeadGcm, null, 0, true),
  };

  static List<CipherSuite> all() {
    return Collections.unmodifiableList(Arrays.asList(kSuites));
  }

  boolean supports(int version) {
    if (version == TlsConnection.kTls13) {
      return tls13;
    }
    return !tls13 && (version >= TlsConnection.kTls12 || scsv || aead == kCbc && macLength == 20);
  }

  boolean dhe() {
    return keyExchange.startsWith("DHE_");
  }

  boolean ecdhe() {
    return keyExchange.startsWith("ECDHE_");
  }

  int certKind() {
    return keyExchange.endsWith("_ECDSA") ? SignatureScheme.kEcdsa : keyExchange.endsWith("_DSS")
        ? SignatureScheme.kDsa : SignatureScheme.kRsaPss;
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
