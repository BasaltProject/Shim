/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

final class TrustStoreManager {
  private static final String[] kSystemBundles = {
    "/etc/ssl/certs/ca-certificates.crt",
    "/etc/pki/tls/certs/ca-bundle.crt",
    "/etc/ssl/ca-bundle.pem",
    "/etc/ssl/cert.pem",
    "/etc/pki/ca-trust/extracted/pem/tls-ca-bundle.pem",
    "/usr/local/share/certs/ca-root-nss.crt",
    "/etc/openssl/certs/ca-certificates.crt",
  };

  private static Set<X509Certificate> cached;
  private static String cachedKey;

  private TrustStoreManager() {
  }

  static synchronized Set<X509Certificate> getTrustedCerts() throws Exception {
    String store = System.getProperty("javax.net.ssl.trustStore");
    String type = System.getProperty("javax.net.ssl.trustStoreType");
    String password = System.getProperty("javax.net.ssl.trustStorePassword");
    String key = store + "|" + type + "|" + password;
    if (cached != null && key.equals(cachedKey)) {
      return cached;
    }
    Set<X509Certificate> result = load(store, type, password);
    cached = result;
    cachedKey = key;
    return result;
  }

  private static Set<X509Certificate> load(String store, String type, String password) throws Exception {
    if (store != null) {
      if (store.equals("NONE") || store.isEmpty()) {
        return new LinkedHashSet<X509Certificate>();
      }
      File file = new File(store);
      if (!file.isFile()) {
        return new LinkedHashSet<X509Certificate>();
      }
      return fromKeyStoreFile(file, type == null || type.isEmpty() ? KeyStore.getDefaultType() : type, password);
    }
    String home = System.getProperty("java.home");
    if (home != null) {
      for (String name : new String[] {"jssecacerts", "cacerts"}) {
        File file = new File(home + File.separator + "lib" + File.separator + "security" + File.separator + name);
        if (file.isFile()) {
          return fromKeyStoreFile(file, KeyStore.getDefaultType(), "changeit");
        }
      }
    }
    for (String bundle : kSystemBundles) {
      File file = new File(bundle);
      if (file.isFile()) {
        return fromPem(file);
      }
    }
    return new LinkedHashSet<X509Certificate>();
  }

  private static Set<X509Certificate> fromPem(File file) throws Exception {
    Set<X509Certificate> certs = new LinkedHashSet<X509Certificate>();
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
      for (java.security.cert.Certificate c : cf.generateCertificates(in)) {
        if (c instanceof X509Certificate) {
          certs.add((X509Certificate) c);
        }
      }
    }
    return certs;
  }

  private static Set<X509Certificate> fromKeyStoreFile(File file, String type, String password) throws Exception {
    KeyStore ks = KeyStore.getInstance(type);
    try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
      ks.load(in, password == null ? null : password.toCharArray());
    }
    return trustedCerts(ks);
  }

  static Set<X509Certificate> trustedCerts(KeyStore ks) throws KeyStoreException {
    Set<X509Certificate> certs = new LinkedHashSet<X509Certificate>();
    for (Enumeration<String> e = ks.aliases(); e.hasMoreElements();) {
      String alias = e.nextElement();
      if (ks.isCertificateEntry(alias)) {
        java.security.cert.Certificate c = ks.getCertificate(alias);
        if (c instanceof X509Certificate) {
          certs.add((X509Certificate) c);
        }
      } else if (ks.isKeyEntry(alias)) {
        java.security.cert.Certificate[] chain = ks.getCertificateChain(alias);
        if (chain != null && chain.length > 0 && chain[0] instanceof X509Certificate) {
          certs.add((X509Certificate) chain[0]);
        }
      }
    }
    return certs;
  }
}
