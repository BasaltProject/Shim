/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.DomainLoadStoreParameter;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

abstract class DomainKeyStore extends KeyStoreSpi {
  private final Map<String, KeyStore> stores = new LinkedHashMap<String, KeyStore>();
  private final Map<String, PolicyParser.KeyStoreEntry> configs = new LinkedHashMap<String, PolicyParser.KeyStoreEntry>();
  private final Map<String, String> domainProperties = new LinkedHashMap<String, String>();
  private String domainName;

  public static final class DKS extends DomainKeyStore {
  }

  private KeyStore find(String alias) {
    int i = alias.indexOf(' ');
    if (i < 0) {
      for (KeyStore ks : stores.values()) {
        try {
          if (ks.containsAlias(alias)) {
            return ks;
          }
        } catch (KeyStoreException e) {
          continue;
        }
      }
      return null;
    }
    return stores.get(alias.substring(0, i));
  }

  private static String local(String alias) {
    int i = alias.indexOf(' ');
    return i < 0 ? alias : alias.substring(i + 1);
  }

  @Override
  public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
    KeyStore ks = find(alias);
    if (ks == null) {
      return null;
    }
    try {
      return ks.getKey(local(alias), password);
    } catch (KeyStoreException e) {
      throw new UnrecoverableKeyException("Get Key failed: " + e.getMessage());
    }
  }

  @Override
  public Certificate[] engineGetCertificateChain(String alias) {
    KeyStore ks = find(alias);
    try {
      return ks == null ? null : ks.getCertificateChain(local(alias));
    } catch (KeyStoreException e) {
      return null;
    }
  }

  @Override
  public Certificate engineGetCertificate(String alias) {
    KeyStore ks = find(alias);
    try {
      return ks == null ? null : ks.getCertificate(local(alias));
    } catch (KeyStoreException e) {
      return null;
    }
  }

  @Override
  public Date engineGetCreationDate(String alias) {
    KeyStore ks = find(alias);
    try {
      return ks == null ? null : ks.getCreationDate(local(alias));
    } catch (KeyStoreException e) {
      return null;
    }
  }

  @Override
  public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain)
      throws KeyStoreException {
    KeyStore ks = find(alias);
    if (ks == null) {
      throw new KeyStoreException("Error setting key entry for '" + alias + "'");
    }
    try {
      ks.setKeyEntry(local(alias), key, password, chain);
    } catch (KeyStoreException e) {
      throw new KeyStoreException("Error setting key entry for '" + alias + "'", e);
    }
  }

  @Override
  public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
    KeyStore ks = find(alias);
    if (ks == null) {
      throw new KeyStoreException("Error setting protected key entry for '" + alias + "'");
    }
    try {
      ks.setKeyEntry(local(alias), key, chain);
    } catch (KeyStoreException e) {
      throw new KeyStoreException("Error setting protected key entry for '" + alias + "'", e);
    }
  }

  @Override
  public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
    KeyStore ks = find(alias);
    if (ks == null) {
      throw new KeyStoreException("Error setting certificate entry for '" + alias + "'");
    }
    try {
      ks.setCertificateEntry(local(alias), cert);
    } catch (KeyStoreException e) {
      throw new KeyStoreException("Error setting certificate entry for '" + alias + "'", e);
    }
  }

  @Override
  public void engineDeleteEntry(String alias) throws KeyStoreException {
    KeyStore ks = find(alias);
    if (ks == null) {
      throw new KeyStoreException("Error deleting entry for '" + alias + "'");
    }
    try {
      ks.deleteEntry(local(alias));
    } catch (KeyStoreException e) {
      throw new KeyStoreException("Error deleting entry for '" + alias + "'", e);
    }
  }

  @Override
  public Enumeration<String> engineAliases() {
    List<String> out = new ArrayList<String>();
    for (Map.Entry<String, KeyStore> e : stores.entrySet()) {
      try {
        for (String a : Collections.list(e.getValue().aliases())) {
          out.add(e.getKey() + " " + a);
        }
      } catch (KeyStoreException ex) {
        continue;
      }
    }
    return Collections.enumeration(out);
  }

  @Override
  public boolean engineContainsAlias(String alias) {
    KeyStore ks = find(alias);
    try {
      return ks != null && ks.containsAlias(local(alias));
    } catch (KeyStoreException e) {
      return false;
    }
  }

  @Override
  public int engineSize() {
    int n = 0;
    for (KeyStore ks : stores.values()) {
      try {
        n += ks.size();
      } catch (KeyStoreException e) {
        continue;
      }
    }
    return n;
  }

  @Override
  public boolean engineIsKeyEntry(String alias) {
    KeyStore ks = find(alias);
    try {
      return ks != null && ks.isKeyEntry(local(alias));
    } catch (KeyStoreException e) {
      return false;
    }
  }

  @Override
  public boolean engineIsCertificateEntry(String alias) {
    KeyStore ks = find(alias);
    try {
      return ks != null && ks.isCertificateEntry(local(alias));
    } catch (KeyStoreException e) {
      return false;
    }
  }

  @Override
  public String engineGetCertificateAlias(Certificate cert) {
    for (Map.Entry<String, KeyStore> e : stores.entrySet()) {
      try {
        String a = e.getValue().getCertificateAlias(cert);
        if (a != null) {
          return e.getKey() + " " + a;
        }
      } catch (KeyStoreException ex) {
        continue;
      }
    }
    return null;
  }

  @Override
  public void engineStore(OutputStream stream, char[] password) {
    throw new UnsupportedOperationException("This keystore must be stored using a DomainLoadStoreParameter");
  }

  @Override
  public void engineLoad(InputStream stream, char[] password) {
    if (stream == null) {
      return;
    }
    throw new UnsupportedOperationException("This keystore must be loaded using a DomainLoadStoreParameter");
  }

  @Override
  public void engineStore(KeyStore.LoadStoreParameter param) throws IOException, NoSuchAlgorithmException,
      CertificateException {
    if (!(param instanceof DomainLoadStoreParameter)) {
      throw new UnsupportedOperationException("This keystore must be stored using a DomainLoadStoreParameter");
    }
    Map<String, KeyStore.ProtectionParameter> protections = ((DomainLoadStoreParameter) param).getProtectionParams();
    for (Map.Entry<String, KeyStore> e : stores.entrySet()) {
      PolicyParser.KeyStoreEntry cfg = configs.get(e.getKey());
      String uri = property(cfg, "keystoreURI");
      if (uri == null) {
        continue;
      }
      char[] pw = passwordFor(protections.get(domainName + ":" + e.getKey()), property(cfg, "keystorePasswordEnv"));
      if (pw == null) {
        continue;
      }
      try (OutputStream out = new FileOutputStream(new File(URI.create(uri)))) {
        e.getValue().store(out, pw);
      } catch (KeyStoreException ex) {
        throw new IOException(ex);
      }
    }
  }

  @Override
  public void engineLoad(KeyStore.LoadStoreParameter param) throws IOException, NoSuchAlgorithmException,
      CertificateException {
    if (!(param instanceof DomainLoadStoreParameter)) {
      throw new UnsupportedOperationException("This keystore must be loaded using a DomainLoadStoreParameter");
    }
    URI config = ((DomainLoadStoreParameter) param).getConfiguration();
    String wanted = config.getFragment();
    Map<String, PolicyParser.DomainEntry> domains;
    try (InputStream in = open(config)) {
      domains = PolicyParser.parseDomains(new InputStreamReader(in, StandardCharsets.UTF_8));
    } catch (PolicyParser.ParsingException e) {
      throw new IOException(e);
    }
    if (domains.isEmpty()) {
      throw new NullPointerException("Cannot invoke \"java.util.Map.values()\" because \"this.domainEntries\" is null");
    }
    PolicyParser.DomainEntry domain = wanted == null ? firstOf(domains) : domains.get(wanted);
    if (domain == null || domain.keystores.isEmpty()) {
      throw new IOException("Error locating domain configuration data for: " + config);
    }
    Map<String, KeyStore> loaded = new LinkedHashMap<String, KeyStore>();
    Map<String, PolicyParser.KeyStoreEntry> loadedConfigs = new LinkedHashMap<String, PolicyParser.KeyStoreEntry>();
    for (PolicyParser.KeyStoreEntry entry : domain.keystores.values()) {
      PolicyParser.KeyStoreEntry merged = new PolicyParser.KeyStoreEntry(entry.name);
      merged.properties.putAll(domain.properties);
      merged.properties.putAll(entry.properties);
      loaded.put(entry.name, openStore(merged));
      loadedConfigs.put(entry.name, merged);
    }
    stores.clear();
    stores.putAll(loaded);
    configs.clear();
    configs.putAll(loadedConfigs);
    domainProperties.clear();
    domainProperties.putAll(domain.properties);
    domainName = domain.name;
  }

  private static PolicyParser.DomainEntry firstOf(Map<String, PolicyParser.DomainEntry> domains) {
    return domains.isEmpty() ? null : domains.values().iterator().next();
  }

  private static InputStream open(URI uri) throws IOException {
    if ("file".equalsIgnoreCase(uri.getScheme())) {
      return new FileInputStream(new File(URI.create(uri.getScheme() + ":" + uri.getRawSchemeSpecificPart())));
    }
    return uri.toURL().openStream();
  }

  private static String property(PolicyParser.KeyStoreEntry cfg, String name) {
    return cfg.properties.get(name);
  }

  private static char[] passwordFor(KeyStore.ProtectionParameter protection, String env) {
    if (protection instanceof KeyStore.PasswordProtection) {
      return ((KeyStore.PasswordProtection) protection).getPassword();
    }
    String v = env == null ? null : System.getenv(env);
    return v == null ? null : v.toCharArray();
  }

  private static KeyStore openStore(PolicyParser.KeyStoreEntry cfg) throws IOException {
    String type = property(cfg, "keystoreType");
    String provider = property(cfg, "keystoreProviderName");
    String uri = property(cfg, "keystoreURI");
    String env = property(cfg, "keystorePasswordEnv");
    KeyStore ks;
    try {
      ks = type == null ? KeyStore.getInstance(KeyStore.getDefaultType()) : provider == null ? KeyStore.getInstance(type)
          : KeyStore.getInstance(type, provider);
    } catch (NoSuchProviderException e) {
      throw new IOException("Error locating JCE provider: " + provider);
    } catch (KeyStoreException e) {
      throw new IOException(new KeyStoreException("KeyStore instantiation failed"));
    }
    char[] pw = null;
    if (env != null) {
      String v = System.getenv(env);
      pw = v == null ? null : v.toCharArray();
    }
    try {
      if (uri == null) {
        ks.load(null, pw);
      } else {
        File file = new File(URI.create(uri));
        if (!file.isFile()) {
          throw new IllegalArgumentException("File does not exist or it does not refer to a normal file: " + file);
        }
        try (InputStream in = new FileInputStream(file)) {
          ks.load(in, pw);
        }
      }
    } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
      throw new IOException(new KeyStoreException("KeyStore instantiation failed"));
    }
    return ks;
  }
}
