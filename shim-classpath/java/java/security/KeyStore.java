/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;
import javax.security.auth.callback.CallbackHandler;
import sun.security.jca.GetInstance;

public class KeyStore {
  private static final String KEYSTORE_TYPE = "keystore.type";

  private final KeyStoreSpi keyStoreSpi;
  private final Provider provider;
  private final String type;
  private boolean initialized = false;

  public interface LoadStoreParameter {
    ProtectionParameter getProtectionParameter();
  }

  public interface ProtectionParameter {
  }

  static final class SimpleLoadStoreParameter implements LoadStoreParameter {
    private final ProtectionParameter protection;

    SimpleLoadStoreParameter(ProtectionParameter protection) {
      this.protection = protection;
    }

    @Override
    public ProtectionParameter getProtectionParameter() {
      return protection;
    }
  }

  public static class PasswordProtection implements ProtectionParameter, Destroyable {
    private final char[] password;
    private final String protectionAlgorithm;
    private final AlgorithmParameterSpec protectionParameters;
    private volatile boolean destroyed = false;

    public PasswordProtection(char[] password) {
      this.password = (password == null) ? null : password.clone();
      this.protectionAlgorithm = null;
      this.protectionParameters = null;
    }

    public PasswordProtection(char[] password, String protectionAlgorithm,
        AlgorithmParameterSpec protectionParameters) {
      if (protectionAlgorithm == null) {
        throw new NullPointerException("invalid null input");
      }
      this.password = (password == null) ? null : password.clone();
      this.protectionAlgorithm = protectionAlgorithm;
      this.protectionParameters = protectionParameters;
    }

    public String getProtectionAlgorithm() {
      return protectionAlgorithm;
    }

    public AlgorithmParameterSpec getProtectionParameters() {
      return protectionParameters;
    }

    public synchronized char[] getPassword() {
      if (destroyed) {
        throw new IllegalStateException("password has been cleared");
      }
      return password;
    }

    @Override
    public synchronized void destroy() throws DestroyFailedException {
      destroyed = true;
      if (password != null) {
        java.util.Arrays.fill(password, ' ');
      }
    }

    @Override
    public synchronized boolean isDestroyed() {
      return destroyed;
    }
  }

  public static class CallbackHandlerProtection implements ProtectionParameter {
    private final CallbackHandler handler;

    public CallbackHandlerProtection(CallbackHandler handler) {
      if (handler == null) {
        throw new NullPointerException("handler must not be null");
      }
      this.handler = handler;
    }

    public CallbackHandler getCallbackHandler() {
      return handler;
    }
  }

  public interface Entry {
    interface Attribute {
      String getName();

      String getValue();
    }

    default Set<Attribute> getAttributes() {
      return Collections.<Attribute>emptySet();
    }
  }

  public static final class PrivateKeyEntry implements Entry {
    private final PrivateKey privKey;
    private final Certificate[] chain;
    private final Set<Attribute> attributes;

    public PrivateKeyEntry(PrivateKey privateKey, Certificate[] chain) {
      this(privateKey, chain, Collections.<Attribute>emptySet());
    }

    public PrivateKeyEntry(PrivateKey privateKey, Certificate[] chain, Set<Attribute> attributes) {
      if (privateKey == null || chain == null || attributes == null) {
        throw new NullPointerException("invalid null input");
      }
      if (chain.length == 0) {
        throw new IllegalArgumentException("invalid zero-length input chain");
      }
      Certificate[] clonedChain = chain.clone();
      String certType = clonedChain[0].getType();
      for (int i = 1; i < clonedChain.length; i++) {
        if (!certType.equals(clonedChain[i].getType())) {
          throw new IllegalArgumentException("chain does not contain certificates of the same type");
        }
      }
      if (!privateKey.getAlgorithm().equals(clonedChain[0].getPublicKey().getAlgorithm())) {
        throw new IllegalArgumentException("private key algorithm does not match algorithm of public key in end "
            + "entity certificate (at index 0)");
      }
      this.privKey = privateKey;
      this.chain = clonedChain;
      this.attributes = Collections.unmodifiableSet(new HashSet<Attribute>(attributes));
    }

    public PrivateKey getPrivateKey() {
      return privKey;
    }

    public Certificate[] getCertificateChain() {
      return chain.clone();
    }

    public Certificate getCertificate() {
      return chain[0];
    }

    @Override
    public Set<Attribute> getAttributes() {
      return attributes;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Private key entry and certificate chain with " + chain.length + " elements:\r\n");
      for (Certificate cert : chain) {
        sb.append(cert);
        sb.append("\r\n");
      }
      return sb.toString();
    }
  }

  public static final class SecretKeyEntry implements Entry {
    private final SecretKey sKey;
    private final Set<Attribute> attributes;

    public SecretKeyEntry(SecretKey secretKey) {
      if (secretKey == null) {
        throw new NullPointerException("invalid null input");
      }
      this.sKey = secretKey;
      this.attributes = Collections.<Attribute>emptySet();
    }

    public SecretKeyEntry(SecretKey secretKey, Set<Attribute> attributes) {
      if (secretKey == null || attributes == null) {
        throw new NullPointerException("invalid null input");
      }
      this.sKey = secretKey;
      this.attributes = Collections.unmodifiableSet(new HashSet<Attribute>(attributes));
    }

    public SecretKey getSecretKey() {
      return sKey;
    }

    @Override
    public Set<Attribute> getAttributes() {
      return attributes;
    }

    @Override
    public String toString() {
      return "Secret key entry with algorithm " + sKey.getAlgorithm();
    }
  }

  public static final class TrustedCertificateEntry implements Entry {
    private final Certificate cert;
    private final Set<Attribute> attributes;

    public TrustedCertificateEntry(Certificate trustedCert) {
      if (trustedCert == null) {
        throw new NullPointerException("invalid null input");
      }
      this.cert = trustedCert;
      this.attributes = Collections.<Attribute>emptySet();
    }

    public TrustedCertificateEntry(Certificate trustedCert, Set<Attribute> attributes) {
      if (trustedCert == null || attributes == null) {
        throw new NullPointerException("invalid null input");
      }
      this.cert = trustedCert;
      this.attributes = Collections.unmodifiableSet(new HashSet<Attribute>(attributes));
    }

    public Certificate getTrustedCertificate() {
      return cert;
    }

    @Override
    public Set<Attribute> getAttributes() {
      return attributes;
    }

    @Override
    public String toString() {
      return "Trusted certificate entry:\r\n" + cert.toString();
    }
  }

  public abstract static class Builder {
    protected Builder() {
    }

    public abstract KeyStore getKeyStore() throws KeyStoreException;

    public abstract ProtectionParameter getProtectionParameter(String alias) throws KeyStoreException;

    public static Builder newInstance(final KeyStore keyStore, final ProtectionParameter protectionParameter) {
      if ((keyStore == null) || (protectionParameter == null)) {
        throw new NullPointerException();
      }
      if (!keyStore.initialized) {
        throw new IllegalArgumentException("KeyStore not initialized");
      }
      return new Builder() {
        private volatile boolean getCalled;

        @Override
        public KeyStore getKeyStore() {
          getCalled = true;
          return keyStore;
        }

        @Override
        public ProtectionParameter getProtectionParameter(String alias) {
          if (alias == null) {
            throw new NullPointerException();
          }
          if (!getCalled) {
            throw new IllegalStateException("getKeyStore() must be called first");
          }
          return protectionParameter;
        }
      };
    }

    public static Builder newInstance(final String type, final Provider provider, final File file,
        final ProtectionParameter protection) {
      if ((type == null) || (file == null) || (protection == null)) {
        throw new NullPointerException();
      }
      if ((protection instanceof PasswordProtection == false)
          && (protection instanceof CallbackHandlerProtection == false)) {
        throw new IllegalArgumentException("Protection must be PasswordProtection or CallbackHandlerProtection");
      }
      if (file.isFile() == false) {
        throw new IllegalArgumentException("File does not exist or it does not refer to a normal file: " + file);
      }
      return new Builder() {
        private KeyStore keyStore;
        private Throwable oldException;

        @Override
        public synchronized KeyStore getKeyStore() throws KeyStoreException {
          if (oldException != null) {
            throw new KeyStoreException("Previous KeyStore instantiation failed", oldException);
          }
          if (keyStore == null) {
            try {
              KeyStore ks;
              if (provider == null) {
                ks = KeyStore.getInstance(type);
              } else {
                ks = KeyStore.getInstance(type, provider);
              }
              InputStream in = null;
              char[] password = null;
              try {
                in = new FileInputStream(file);
                if (protection instanceof PasswordProtection) {
                  password = ((PasswordProtection) protection).getPassword();
                  ks.load(in, password);
                } else {
                  ks.load(new SimpleLoadStoreParameter(protection));
                }
              } finally {
                if (in != null) {
                  in.close();
                }
              }
              keyStore = ks;
            } catch (Exception e) {
              oldException = e;
              throw new KeyStoreException("KeyStore instantiation failed", e);
            }
          }
          return keyStore;
        }

        @Override
        public ProtectionParameter getProtectionParameter(String alias) {
          if (alias == null) {
            throw new NullPointerException();
          }
          return protection;
        }
      };
    }

    public static Builder newInstance(File file, ProtectionParameter protection) {
      return newInstance(KeyStore.getDefaultType(), null, file, protection);
    }

    public static Builder newInstance(final String type, final Provider provider,
        final ProtectionParameter protection) {
      if ((type == null) || (protection == null)) {
        throw new NullPointerException();
      }
      return new Builder() {
        private KeyStore keyStore;

        @Override
        public synchronized KeyStore getKeyStore() throws KeyStoreException {
          if (keyStore == null) {
            try {
              KeyStore ks = (provider == null) ? KeyStore.getInstance(type) : KeyStore.getInstance(type, provider);
              ks.load(new SimpleLoadStoreParameter(protection));
              keyStore = ks;
            } catch (Exception e) {
              throw new KeyStoreException("KeyStore instantiation failed", e);
            }
          }
          return keyStore;
        }

        @Override
        public ProtectionParameter getProtectionParameter(String alias) {
          if (alias == null) {
            throw new NullPointerException();
          }
          return protection;
        }
      };
    }
  }

  protected KeyStore(KeyStoreSpi keyStoreSpi, Provider provider, String type) {
    this.keyStoreSpi = keyStoreSpi;
    this.provider = provider;
    this.type = type;
  }

  public static KeyStore getInstance(String type) throws KeyStoreException {
    Objects.requireNonNull(type, "null type name");
    try {
      GetInstance.Instance instance = GetInstance.getInstance("KeyStore", KeyStoreSpi.class, type);
      return new KeyStore((KeyStoreSpi) instance.impl, instance.provider, type);
    } catch (NoSuchAlgorithmException nsae) {
      throw new KeyStoreException(type + " not found", nsae);
    }
  }

  public static KeyStore getInstance(String type, String provider) throws KeyStoreException,
      NoSuchProviderException {
    Objects.requireNonNull(type, "null type name");
    if (provider == null || provider.isEmpty()) {
      throw new IllegalArgumentException("missing provider");
    }
    try {
      GetInstance.Instance instance = GetInstance.getInstance("KeyStore", KeyStoreSpi.class, type, provider);
      return new KeyStore((KeyStoreSpi) instance.impl, instance.provider, type);
    } catch (NoSuchAlgorithmException nsae) {
      throw new KeyStoreException(type + " not found", nsae);
    }
  }

  public static KeyStore getInstance(String type, Provider provider) throws KeyStoreException {
    Objects.requireNonNull(type, "null type name");
    if (provider == null) {
      throw new IllegalArgumentException("missing provider");
    }
    try {
      GetInstance.Instance instance = GetInstance.getInstance("KeyStore", KeyStoreSpi.class, type, provider);
      return new KeyStore((KeyStoreSpi) instance.impl, instance.provider, type);
    } catch (NoSuchAlgorithmException nsae) {
      throw new KeyStoreException(type + " not found", nsae);
    }
  }

  public static final String getDefaultType() {
    String kstype = Security.getProperty(KEYSTORE_TYPE);
    if (kstype == null) {
      kstype = "jks";
    }
    return kstype;
  }

  public final Provider getProvider() {
    return this.provider;
  }

  public final String getType() {
    return this.type;
  }

  private void checkInit() throws KeyStoreException {
    if (!initialized) {
      throw new KeyStoreException("Uninitialized keystore");
    }
  }

  public final Key getKey(String alias, char[] password) throws KeyStoreException, NoSuchAlgorithmException,
      UnrecoverableKeyException {
    checkInit();
    return keyStoreSpi.engineGetKey(alias, password);
  }

  public final Certificate[] getCertificateChain(String alias) throws KeyStoreException {
    checkInit();
    return keyStoreSpi.engineGetCertificateChain(alias);
  }

  public final Certificate getCertificate(String alias) throws KeyStoreException {
    checkInit();
    return keyStoreSpi.engineGetCertificate(alias);
  }

  public final Date getCreationDate(String alias) throws KeyStoreException {
    checkInit();
    return keyStoreSpi.engineGetCreationDate(alias);
  }

  public final void setKeyEntry(String alias, Key key, char[] password, Certificate[] chain)
      throws KeyStoreException {
    checkInit();
    if ((key instanceof PrivateKey) && (chain == null || chain.length == 0)) {
      throw new IllegalArgumentException("Private key must be accompanied by certificate chain");
    }
    keyStoreSpi.engineSetKeyEntry(alias, key, password, chain);
  }

  public final void setKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
    checkInit();
    keyStoreSpi.engineSetKeyEntry(alias, key, chain);
  }

  public final void setCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
    checkInit();
    keyStoreSpi.engineSetCertificateEntry(alias, cert);
  }

  public final void deleteEntry(String alias) throws KeyStoreException {
    checkInit();
    keyStoreSpi.engineDeleteEntry(alias);
  }

  public final Enumeration<String> aliases() throws KeyStoreException {
    checkInit();
    return keyStoreSpi.engineAliases();
  }

  public final boolean containsAlias(String alias) throws KeyStoreException {
    checkInit();
    return keyStoreSpi.engineContainsAlias(alias);
  }

  public final int size() throws KeyStoreException {
    checkInit();
    return keyStoreSpi.engineSize();
  }

  public final boolean isKeyEntry(String alias) throws KeyStoreException {
    checkInit();
    return keyStoreSpi.engineIsKeyEntry(alias);
  }

  public final boolean isCertificateEntry(String alias) throws KeyStoreException {
    checkInit();
    return keyStoreSpi.engineIsCertificateEntry(alias);
  }

  public final String getCertificateAlias(Certificate cert) throws KeyStoreException {
    checkInit();
    return keyStoreSpi.engineGetCertificateAlias(cert);
  }

  public final void store(OutputStream stream, char[] password) throws KeyStoreException, IOException,
      NoSuchAlgorithmException, CertificateException {
    checkInit();
    keyStoreSpi.engineStore(stream, password);
  }

  public final void store(LoadStoreParameter param) throws KeyStoreException, IOException,
      NoSuchAlgorithmException, CertificateException {
    checkInit();
    keyStoreSpi.engineStore(param);
  }

  public final void load(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException,
      CertificateException {
    keyStoreSpi.engineLoad(stream, password);
    initialized = true;
  }

  public final void load(LoadStoreParameter param) throws IOException, NoSuchAlgorithmException,
      CertificateException {
    keyStoreSpi.engineLoad(param);
    initialized = true;
  }

  public final Entry getEntry(String alias, ProtectionParameter protParam) throws NoSuchAlgorithmException,
      UnrecoverableEntryException, KeyStoreException {
    if (alias == null) {
      throw new NullPointerException("invalid null input");
    }
    checkInit();
    return keyStoreSpi.engineGetEntry(alias, protParam);
  }

  public final void setEntry(String alias, Entry entry, ProtectionParameter protParam) throws KeyStoreException {
    if (alias == null || entry == null) {
      throw new NullPointerException("invalid null input");
    }
    checkInit();
    keyStoreSpi.engineSetEntry(alias, entry, protParam);
  }

  public final boolean entryInstanceOf(String alias, Class<? extends Entry> entryClass) throws KeyStoreException {
    if (alias == null || entryClass == null) {
      throw new NullPointerException("invalid null input");
    }
    checkInit();
    return keyStoreSpi.engineEntryInstanceOf(alias, entryClass);
  }

  public final Set<Entry.Attribute> getAttributes(String alias) throws KeyStoreException {
    Objects.requireNonNull(alias, "invalid null input");
    checkInit();
    return keyStoreSpi.engineGetAttributes(alias);
  }

  public static final KeyStore getInstance(File file, char[] password) throws KeyStoreException, IOException,
      NoSuchAlgorithmException, CertificateException {
    return getInstance(file, password, null, true);
  }

  public static final KeyStore getInstance(File file, LoadStoreParameter param) throws KeyStoreException,
      IOException, NoSuchAlgorithmException, CertificateException {
    return getInstance(file, null, param, false);
  }

  private static KeyStore getInstance(File file, char[] password, LoadStoreParameter param, boolean hasPassword)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
    if (file == null) {
      throw new NullPointerException("file is null");
    }
    if (hasPassword && password == null) {
      throw new NullPointerException("password is null");
    }
    KeyStore keystore = null;
    try (DataInputStream dataStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
      for (String type : new String[] {"PKCS12", "JKS", "JCEKS"}) {
        try {
          dataStream.mark(1 << 20);
          KeyStore ks = KeyStore.getInstance(type);
          if (hasPassword) {
            ks.load(dataStream, password);
          } else {
            ks.load(param);
          }
          keystore = ks;
          break;
        } catch (Exception e) {
          dataStream.reset();
        }
      }
    }
    if (keystore == null) {
      throw new KeyStoreException("Unrecognized keystore format. Please load it with a specified type");
    }
    return keystore;
  }
}
