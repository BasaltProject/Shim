/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.net.Socket;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;

final class X509KeyManagerImpl extends X509ExtendedKeyManager {
  private static final class Entry {
    final PrivateKey key;
    final X509Certificate[] chain;

    Entry(PrivateKey key, X509Certificate[] chain) {
      this.key = key;
      this.chain = chain;
    }
  }

  private final Map<String, Entry> entries = new LinkedHashMap<String, Entry>();

  X509KeyManagerImpl(KeyStore ks, char[] password) throws KeyStoreException, NoSuchAlgorithmException,
      UnrecoverableKeyException {
    if (ks == null) {
      return;
    }
    for (Enumeration<String> e = ks.aliases(); e.hasMoreElements();) {
      String alias = e.nextElement();
      if (!ks.isKeyEntry(alias)) {
        continue;
      }
      Key key = ks.getKey(alias, password);
      if (!(key instanceof PrivateKey)) {
        continue;
      }
      Certificate[] certs = ks.getCertificateChain(alias);
      if (certs == null || certs.length == 0 || !(certs[0] instanceof X509Certificate)) {
        continue;
      }
      X509Certificate[] chain = new X509Certificate[certs.length];
      for (int i = 0; i < certs.length; i++) {
        chain[i] = (X509Certificate) certs[i];
      }
      entries.put(alias, new Entry((PrivateKey) key, chain));
    }
  }

  private static String publicKeyType(X509Certificate cert) {
    String alg = cert.getPublicKey().getAlgorithm();
    return alg;
  }

  private static boolean typeMatches(String keyType, X509Certificate cert) {
    String alg = publicKeyType(cert);
    String sigAlg = cert.getSigAlgName().toUpperCase(Locale.ENGLISH);
    switch (keyType) {
    case "RSA":
      return alg.equals("RSA");
    case "EC":
      return alg.equals("EC");
    case "EC_EC":
      return alg.equals("EC") && sigAlg.contains("ECDSA");
    case "EC_RSA":
      return alg.equals("EC") && sigAlg.contains("RSA");
    case "RSA_RSA":
      return alg.equals("RSA") && sigAlg.contains("RSA");
    case "RSASSA-PSS":
      return alg.equals("RSASSA-PSS");
    default:
      return alg.equalsIgnoreCase(keyType);
    }
  }

  private boolean issuerMatches(X509Certificate[] chain, Principal[] issuers) {
    if (issuers == null || issuers.length == 0) {
      return true;
    }
    for (X509Certificate c : chain) {
      for (Principal p : issuers) {
        if (c.getIssuerX500Principal().equals(p)) {
          return true;
        }
      }
    }
    return false;
  }

  private String[] aliases(String keyType, Principal[] issuers) {
    List<String> out = new ArrayList<String>();
    for (Map.Entry<String, Entry> me : entries.entrySet()) {
      Entry e = me.getValue();
      if (keyType != null && !typeMatches(keyType, e.chain[0])) {
        continue;
      }
      if (!issuerMatches(e.chain, issuers)) {
        continue;
      }
      try {
        e.chain[0].checkValidity();
      } catch (CertificateException ex) {
        continue;
      }
      out.add("0." + me.getKey());
    }
    return out.isEmpty() ? null : out.toArray(new String[0]);
  }

  @Override
  public String[] getClientAliases(String keyType, Principal[] issuers) {
    return aliases(keyType, issuers);
  }

  @Override
  public String chooseClientAlias(String[] keyTypes, Principal[] issuers, Socket socket) {
    if (keyTypes == null) {
      return null;
    }
    for (String type : keyTypes) {
      String[] a = aliases(type, issuers);
      if (a != null) {
        return a[0];
      }
    }
    return null;
  }

  @Override
  public String[] getServerAliases(String keyType, Principal[] issuers) {
    return aliases(keyType, issuers);
  }

  @Override
  public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
    if (keyType == null) {
      return null;
    }
    String[] a = aliases(keyType, issuers);
    return a == null ? null : a[0];
  }

  @Override
  public String chooseEngineClientAlias(String[] keyTypes, Principal[] issuers, SSLEngine engine) {
    return chooseClientAlias(keyTypes, issuers, null);
  }

  @Override
  public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
    return chooseServerAlias(keyType, issuers, null);
  }

  private Entry find(String alias) {
    if (alias == null) {
      return null;
    }
    int dot = alias.indexOf('.');
    if (dot < 0) {
      return entries.get(alias);
    }
    return entries.get(alias.substring(dot + 1));
  }

  @Override
  public X509Certificate[] getCertificateChain(String alias) {
    Entry e = find(alias);
    return e == null ? null : e.chain.clone();
  }

  @Override
  public PrivateKey getPrivateKey(String alias) {
    Entry e = find(alias);
    return e == null ? null : e.key;
  }
}
