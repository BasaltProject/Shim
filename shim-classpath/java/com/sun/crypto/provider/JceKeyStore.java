/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;

public final class JceKeyStore extends KeyStoreSpi {
  private static final int kMagic = 0xCECECECE;
  private static final int kVersion1 = 1;
  private static final int kVersion2 = 2;
  private static final int kIterations = 200000;
  private static final String kPbeAlgorithm = "PBEWithMD5AndTripleDES";
  private static final String kPbeOid = "1.3.6.1.4.1.42.2.19.1";

  private static class Entry {
    Date date = new Date();
  }

  private static final class PrivateKeyEntry extends Entry {
    byte[] protectedKey;
    Certificate[] chain;
  }

  private static final class CertEntry extends Entry {
    Certificate cert;
  }

  private static final class SecretKeyEntry extends Entry {
    SealedObject sealed;
  }

  private final Map<String, Entry> entries = new LinkedHashMap<String, Entry>();

  public JceKeyStore() {
  }

  private static String norm(String alias) {
    return alias.toLowerCase(Locale.ENGLISH);
  }

  private static Cipher pbe(int mode, char[] password, byte[] salt) throws GeneralSecurityException {
    SecretKey key = SecretKeyFactory.getInstance(kPbeAlgorithm).generateSecret(new PBEKeySpec(password));
    Cipher c = Cipher.getInstance(kPbeAlgorithm);
    c.init(mode, key, new PBEParameterSpec(salt, kIterations));
    return c;
  }

  private static byte[] protect(PrivateKey key, char[] password) throws KeyStoreException {
    try {
      byte[] salt = new byte[8];
      new SecureRandom().nextBytes(salt);
      byte[] enc = pbe(Cipher.ENCRYPT_MODE, password, salt).doFinal(key.getEncoded());
      DerOutputStream params = new DerOutputStream();
      params.putOctetString(salt);
      params.putInteger(java.math.BigInteger.valueOf(kIterations));
      DerOutputStream algId = new DerOutputStream();
      algId.putOID(ObjectIdentifier.of(kPbeOid));
      algId.write(DerValue.tag_Sequence, params);
      DerOutputStream body = new DerOutputStream();
      body.write(DerValue.tag_Sequence, algId);
      body.putOctetString(enc);
      DerOutputStream out = new DerOutputStream();
      out.write(DerValue.tag_Sequence, body);
      return out.toByteArray();
    } catch (GeneralSecurityException e) {
      throw new KeyStoreException("Encrypt Private Key failed: " + e.getMessage(), e);
    }
  }

  private static PrivateKey recover(byte[] encoded, char[] password) throws UnrecoverableKeyException {
    try {
      DerInputStream top = new DerValue(encoded).data();
      DerInputStream algId = top.getDerValue().data();
      String oid = algId.getDerValue().getOID().toString();
      if (!kPbeOid.equals(oid)) {
        throw new UnrecoverableKeyException("Unsupported encryption algorithm");
      }
      DerInputStream params = algId.getDerValue().data();
      byte[] salt = params.getDerValue().getOctetString();
      byte[] enc = top.getDerValue().getOctetString();
      byte[] plain = pbe(Cipher.DECRYPT_MODE, password, salt).doFinal(enc);
      DerInputStream pki = new DerValue(plain).data();
      pki.getDerValue();
      String keyOid = pki.getDerValue().data().getOID().toString();
      String alg = AlgorithmId.keyAlgorithm(keyOid);
      if (alg == null) {
        throw new UnrecoverableKeyException("Unsupported key algorithm " + keyOid);
      }
      return KeyFactory.getInstance(alg).generatePrivate(new PKCS8EncodedKeySpec(plain));
    } catch (GeneralSecurityException | IOException e) {
      UnrecoverableKeyException u = new UnrecoverableKeyException("Cannot recover key");
      u.initCause(e);
      throw u;
    }
  }

  private static SealedObject seal(SecretKey key, char[] password) throws KeyStoreException {
    try {
      byte[] salt = new byte[8];
      new SecureRandom().nextBytes(salt);
      return new SealedObjectForKeyProtector(key, pbe(Cipher.ENCRYPT_MODE, password, salt));
    } catch (GeneralSecurityException | IOException e) {
      throw new KeyStoreException("Encrypt Key failed: " + e.getMessage(), e);
    }
  }

  private static Key unseal(SealedObject so, char[] password) throws UnrecoverableKeyException {
    try {
      SecretKey pk = SecretKeyFactory.getInstance(kPbeAlgorithm).generateSecret(new PBEKeySpec(password));
      return (Key) so.getObject(pk);
    } catch (GeneralSecurityException | IOException | ClassNotFoundException e) {
      UnrecoverableKeyException u = new UnrecoverableKeyException("Cannot recover key");
      u.initCause(e);
      throw u;
    }
  }

  @Override
  public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
    Entry e = entries.get(norm(alias));
    if (e instanceof PrivateKeyEntry) {
      if (password == null) {
        throw new UnrecoverableKeyException("Password must not be null");
      }
      return recover(((PrivateKeyEntry) e).protectedKey, password);
    }
    if (e instanceof SecretKeyEntry) {
      if (password == null) {
        throw new UnrecoverableKeyException("Password must not be null");
      }
      return unseal(((SecretKeyEntry) e).sealed, password);
    }
    return null;
  }

  @Override
  public Certificate[] engineGetCertificateChain(String alias) {
    Entry e = entries.get(norm(alias));
    if (e instanceof PrivateKeyEntry && ((PrivateKeyEntry) e).chain != null) {
      return ((PrivateKeyEntry) e).chain.clone();
    }
    return null;
  }

  @Override
  public Certificate engineGetCertificate(String alias) {
    Entry e = entries.get(norm(alias));
    if (e instanceof CertEntry) {
      return ((CertEntry) e).cert;
    }
    if (e instanceof PrivateKeyEntry) {
      Certificate[] c = ((PrivateKeyEntry) e).chain;
      return c == null || c.length == 0 ? null : c[0];
    }
    return null;
  }

  @Override
  public Date engineGetCreationDate(String alias) {
    Entry e = entries.get(norm(alias));
    return e == null ? null : new Date(e.date.getTime());
  }

  @Override
  public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain)
      throws KeyStoreException {
    if (password == null) {
      throw new KeyStoreException("password can't be null");
    }
    String name = norm(alias);
    if (key instanceof PrivateKey) {
      if (chain == null || chain.length == 0) {
        throw new KeyStoreException("Private key must be accompanied by certificate chain");
      }
      PrivateKeyEntry pe = new PrivateKeyEntry();
      pe.protectedKey = protect((PrivateKey) key, password);
      pe.chain = chain.clone();
      entries.put(name, pe);
    } else if (key instanceof SecretKey) {
      SecretKeyEntry se = new SecretKeyEntry();
      se.sealed = seal((SecretKey) key, password);
      entries.put(name, se);
    } else {
      throw new KeyStoreException("Key is not a PrivateKey or a SecretKey");
    }
  }

  @Override
  public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
    PrivateKeyEntry pe = new PrivateKeyEntry();
    pe.protectedKey = key.clone();
    pe.chain = chain == null ? null : chain.clone();
    entries.put(norm(alias), pe);
  }

  @Override
  public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
    Entry old = entries.get(norm(alias));
    if (old != null && !(old instanceof CertEntry)) {
      throw new KeyStoreException("Cannot overwrite own certificate");
    }
    CertEntry ce = new CertEntry();
    ce.cert = cert;
    entries.put(norm(alias), ce);
  }

  @Override
  public void engineDeleteEntry(String alias) throws KeyStoreException {
    entries.remove(norm(alias));
  }

  @Override
  public Enumeration<String> engineAliases() {
    return Collections.enumeration(new ArrayList<String>(entries.keySet()));
  }

  @Override
  public boolean engineContainsAlias(String alias) {
    return entries.containsKey(norm(alias));
  }

  @Override
  public int engineSize() {
    return entries.size();
  }

  @Override
  public boolean engineIsKeyEntry(String alias) {
    Entry e = entries.get(norm(alias));
    return e instanceof PrivateKeyEntry || e instanceof SecretKeyEntry;
  }

  @Override
  public boolean engineIsCertificateEntry(String alias) {
    return entries.get(norm(alias)) instanceof CertEntry;
  }

  @Override
  public String engineGetCertificateAlias(Certificate cert) {
    for (Map.Entry<String, Entry> me : entries.entrySet()) {
      Certificate c = null;
      if (me.getValue() instanceof CertEntry) {
        c = ((CertEntry) me.getValue()).cert;
      } else if (me.getValue() instanceof PrivateKeyEntry && ((PrivateKeyEntry) me.getValue()).chain != null
          && ((PrivateKeyEntry) me.getValue()).chain.length > 0) {
        c = ((PrivateKeyEntry) me.getValue()).chain[0];
      }
      if (c != null && c.equals(cert)) {
        return me.getKey();
      }
    }
    return null;
  }

  private static MessageDigest keyedDigest(char[] password) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA");
      byte[] b = new byte[password.length * 2];
      for (int i = 0, j = 0; i < password.length; i++) {
        b[j++] = (byte) (password[i] >> 8);
        b[j++] = (byte) password[i];
      }
      md.update(b);
      md.update("Mighty Aphrodite".getBytes(java.nio.charset.StandardCharsets.UTF_8));
      return md;
    } catch (NoSuchAlgorithmException e) {
      throw new InternalError(e);
    }
  }

  private static void writeCert(DataOutputStream dos, Certificate c) throws IOException, CertificateException {
    dos.writeUTF(c.getType());
    byte[] enc = c.getEncoded();
    dos.writeInt(enc.length);
    dos.write(enc);
  }

  @Override
  public void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException,
      CertificateException {
    if (password == null) {
      throw new IllegalArgumentException("password can't be null");
    }
    MessageDigest md = keyedDigest(password);
    DataOutputStream dos = new DataOutputStream(new DigestOutputStream(new BufferedOutputStream(stream), md));
    dos.writeInt(kMagic);
    dos.writeInt(kVersion2);
    dos.writeInt(entries.size());
    for (Map.Entry<String, Entry> me : entries.entrySet()) {
      Entry e = me.getValue();
      if (e instanceof PrivateKeyEntry) {
        PrivateKeyEntry pe = (PrivateKeyEntry) e;
        dos.writeInt(1);
        dos.writeUTF(me.getKey());
        dos.writeLong(pe.date.getTime());
        dos.writeInt(pe.protectedKey.length);
        dos.write(pe.protectedKey);
        int n = pe.chain == null ? 0 : pe.chain.length;
        dos.writeInt(n);
        for (int i = 0; i < n; i++) {
          writeCert(dos, pe.chain[i]);
        }
      } else if (e instanceof CertEntry) {
        dos.writeInt(2);
        dos.writeUTF(me.getKey());
        dos.writeLong(e.date.getTime());
        writeCert(dos, ((CertEntry) e).cert);
      } else {
        dos.writeInt(3);
        dos.writeUTF(me.getKey());
        dos.writeLong(e.date.getTime());
        ObjectOutputStream oos = new ObjectOutputStream(dos);
        oos.writeObject(((SecretKeyEntry) e).sealed);
        oos.flush();
      }
    }
    dos.flush();
    dos.write(md.digest());
    dos.flush();
  }

  private static Certificate readCert(DataInputStream dis, int version, Map<String, CertificateFactory> factories)
      throws IOException, CertificateException {
    String type = version == kVersion2 ? dis.readUTF() : "X.509";
    CertificateFactory cf = factories.get(type);
    if (cf == null) {
      cf = CertificateFactory.getInstance(type);
      factories.put(type, cf);
    }
    byte[] data = new byte[dis.readInt()];
    dis.readFully(data);
    return cf.generateCertificate(new ByteArrayInputStream(data));
  }

  @Override
  public void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException,
      CertificateException {
    entries.clear();
    if (stream == null) {
      return;
    }
    MessageDigest md = password == null ? null : keyedDigest(password);
    DataInputStream dis = new DataInputStream(md == null ? new BufferedInputStream(stream)
        : new DigestInputStream(new BufferedInputStream(stream), md));
    int magic = dis.readInt();
    int version = dis.readInt();
    if (magic != kMagic || (version != kVersion1 && version != kVersion2)) {
      throw new IOException("Invalid keystore format");
    }
    int count = dis.readInt();
    Map<String, CertificateFactory> factories = new HashMap<String, CertificateFactory>();
    for (int i = 0; i < count; i++) {
      int tag = dis.readInt();
      String alias = dis.readUTF();
      Date date = new Date(dis.readLong());
      if (tag == 1) {
        PrivateKeyEntry pe = new PrivateKeyEntry();
        pe.date = date;
        pe.protectedKey = new byte[dis.readInt()];
        dis.readFully(pe.protectedKey);
        int n = dis.readInt();
        if (n > 0) {
          List<Certificate> chain = new ArrayList<Certificate>(n);
          for (int j = 0; j < n; j++) {
            chain.add(readCert(dis, version, factories));
          }
          pe.chain = chain.toArray(new Certificate[0]);
        }
        entries.put(alias, pe);
      } else if (tag == 2) {
        CertEntry ce = new CertEntry();
        ce.date = date;
        ce.cert = readCert(dis, version, factories);
        entries.put(alias, ce);
      } else if (tag == 3) {
        SecretKeyEntry se = new SecretKeyEntry();
        se.date = date;
        try {
          se.sealed = new SealedObjectForKeyProtector((SealedObject) new ObjectInputStream(dis).readObject());
        } catch (ClassNotFoundException | ClassCastException e) {
          throw new IOException("Unrecognized sealed key entry", e);
        }
        entries.put(alias, se);
      } else {
        throw new IOException("Unrecognized keystore entry: " + tag);
      }
    }
    if (md != null) {
      byte[] computed = md.digest();
      byte[] actual = new byte[computed.length];
      dis.readFully(actual);
      if (!MessageDigest.isEqual(computed, actual)) {
        entries.clear();
        UnrecoverableKeyException cause = new UnrecoverableKeyException("Password verification failed");
        throw new IOException("Keystore was tampered with, or password was incorrect", cause);
      }
    }
  }

  @Override
  public boolean engineProbe(InputStream stream) throws IOException {
    try {
      return new DataInputStream(stream).readInt() == kMagic;
    } catch (java.io.EOFException e) {
      return false;
    }
  }
}
