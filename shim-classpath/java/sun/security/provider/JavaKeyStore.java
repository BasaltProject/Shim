/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
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
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;

public class JavaKeyStore extends KeyStoreSpi {
  private static final int kMagic = 0xfeedfeed;
  private static final int kVersion1 = 1;
  private static final int kVersion2 = 2;
  private static final String kKeyProtectorOid = "1.3.6.1.4.1.42.2.17.1.1";

  private static class Entry {
    Date date;
  }

  private static final class KeyEntry extends Entry {
    byte[] protectedKey;
    Certificate[] chain;
  }

  private static final class CertEntry extends Entry {
    Certificate cert;
  }

  private final Map<String, Entry> entries = new LinkedHashMap<String, Entry>();
  private final boolean caseExact;

  public static final class JKS extends JavaKeyStore {
    public JKS() {
      super(false);
    }
  }

  public static final class DualFormatJKS extends sun.security.util.KeyStoreDelegator {
    public DualFormatJKS() {
      super(new JKS(), new sun.security.pkcs12.PKCS12KeyStore());
    }
  }

  public static final class CaseExactJKS extends JavaKeyStore {
    public CaseExactJKS() {
      super(true);
    }
  }

  JavaKeyStore(boolean caseExact) {
    this.caseExact = caseExact;
  }

  private String norm(String alias) {
    return caseExact ? alias : alias.toLowerCase(Locale.ENGLISH);
  }

  private static byte[] passwordBytes(char[] password) {
    byte[] b = new byte[password.length * 2];
    for (int i = 0, j = 0; i < password.length; i++) {
      b[j++] = (byte) (password[i] >> 8);
      b[j++] = (byte) password[i];
    }
    return b;
  }

  private static MessageDigest sha1() {
    try {
      return MessageDigest.getInstance("SHA");
    } catch (NoSuchAlgorithmException e) {
      throw new InternalError(e);
    }
  }

  private static byte[] protect(byte[] plain, char[] password) throws KeyStoreException {
    byte[] pw = passwordBytes(password);
    byte[] salt = new byte[20];
    new SecureRandom().nextBytes(salt);
    byte[] out = new byte[20 + plain.length + 20];
    System.arraycopy(salt, 0, out, 0, 20);
    MessageDigest md = sha1();
    byte[] digest = salt;
    int off = 0;
    while (off < plain.length) {
      md.update(pw);
      md.update(digest);
      digest = md.digest();
      for (int i = 0; i < digest.length && off < plain.length; i++, off++) {
        out[20 + off] = (byte) (plain[off] ^ digest[i]);
      }
    }
    md.update(pw);
    md.update(plain);
    System.arraycopy(md.digest(), 0, out, 20 + plain.length, 20);
    DerOutputStream algo = new DerOutputStream();
    try {
      algo.putOID(new ObjectIdentifier(kKeyProtectorOid));
      algo.putNull();
      DerOutputStream seq = new DerOutputStream();
      seq.write(DerValue.tag_Sequence, algo);
      seq.putOctetString(out);
      DerOutputStream info = new DerOutputStream();
      info.write(DerValue.tag_Sequence, seq);
      return info.toByteArray();
    } catch (IOException e) {
      throw new KeyStoreException(e.getMessage());
    }
  }

  private static byte[] recover(byte[] encoded, char[] password) throws UnrecoverableKeyException {
    byte[] data;
    try {
      DerInputStream in = new DerValue(encoded).data();
      String oid = in.getDerValue().data().getOID().toString();
      if (!oid.equals(kKeyProtectorOid)) {
        throw new UnrecoverableKeyException("Unsupported key protection algorithm");
      }
      data = in.getOctetString();
    } catch (IOException e) {
      throw new UnrecoverableKeyException("Cannot recover key");
    }
    if (data.length < 40) {
      throw new UnrecoverableKeyException("Cannot recover key");
    }
    byte[] pw = passwordBytes(password);
    int len = data.length - 40;
    byte[] plain = new byte[len];
    MessageDigest md = sha1();
    byte[] digest = Arrays.copyOfRange(data, 0, 20);
    int off = 0;
    while (off < len) {
      md.update(pw);
      md.update(digest);
      digest = md.digest();
      for (int i = 0; i < digest.length && off < len; i++, off++) {
        plain[off] = (byte) (data[20 + off] ^ digest[i]);
      }
    }
    md.update(pw);
    md.update(plain);
    byte[] check = md.digest();
    for (int i = 0; i < 20; i++) {
      if (check[i] != data[20 + len + i]) {
        throw new UnrecoverableKeyException("Cannot recover key");
      }
    }
    return plain;
  }

  @Override
  public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
    Entry e = entries.get(norm(alias));
    if (!(e instanceof KeyEntry)) {
      return null;
    }
    if (password == null) {
      throw new UnrecoverableKeyException("Password must not be null");
    }
    byte[] plain = recover(((KeyEntry) e).protectedKey, password);
    try {
      DerValue info = new DerValue(plain);
      DerInputStream pki = info.data();
      pki.getDerValue();
      String oid = pki.getDerValue().data().getOID().toString();
      String alg = AlgorithmId.keyAlgorithm(oid);
      if (alg == null) {
        throw new UnrecoverableKeyException("Unsupported key algorithm " + oid);
      }
      return KeyFactory.getInstance(alg).generatePrivate(new PKCS8EncodedKeySpec(plain));
    } catch (IOException | InvalidKeySpecException e2) {
      throw new UnrecoverableKeyException("Cannot recover key");
    }
  }

  @Override
  public Certificate[] engineGetCertificateChain(String alias) {
    Entry e = entries.get(norm(alias));
    if (e instanceof KeyEntry && ((KeyEntry) e).chain != null) {
      return ((KeyEntry) e).chain.clone();
    }
    return null;
  }

  @Override
  public Certificate engineGetCertificate(String alias) {
    Entry e = entries.get(norm(alias));
    if (e instanceof CertEntry) {
      return ((CertEntry) e).cert;
    }
    if (e instanceof KeyEntry) {
      Certificate[] c = ((KeyEntry) e).chain;
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
    if (!(key instanceof PrivateKey)) {
      throw new KeyStoreException("Cannot store non-PrivateKeys");
    }
    KeyEntry ke = new KeyEntry();
    ke.date = new Date();
    ke.protectedKey = protect(key.getEncoded(), password);
    if (chain != null) {
      if (chain.length > 1 && !checkChain(chain)) {
        throw new KeyStoreException("Certificate chain is not valid");
      }
      ke.chain = chain.clone();
    }
    entries.put(norm(alias), ke);
  }

  private static boolean checkChain(Certificate[] chain) {
    return true;
  }

  @Override
  public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
    KeyEntry ke = new KeyEntry();
    ke.date = new Date();
    try {
      new DerValue(key);
    } catch (IOException e) {
      throw new KeyStoreException("Private key is not stored as PKCS#8 EncryptedPrivateKeyInfo: " + e);
    }
    ke.protectedKey = key.clone();
    if (chain != null) {
      ke.chain = chain.clone();
    }
    entries.put(norm(alias), ke);
  }

  @Override
  public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
    Entry old = entries.get(norm(alias));
    if (old instanceof KeyEntry) {
      throw new KeyStoreException("Cannot overwrite own certificate");
    }
    CertEntry ce = new CertEntry();
    ce.cert = cert;
    ce.date = new Date();
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
    return entries.get(norm(alias)) instanceof KeyEntry;
  }

  @Override
  public boolean engineIsCertificateEntry(String alias) {
    return entries.get(norm(alias)) instanceof CertEntry;
  }

  @Override
  public String engineGetCertificateAlias(Certificate cert) {
    for (Map.Entry<String, Entry> me : entries.entrySet()) {
      Certificate c = me.getValue() instanceof CertEntry ? ((CertEntry) me.getValue()).cert
          : (((KeyEntry) me.getValue()).chain != null && ((KeyEntry) me.getValue()).chain.length > 0
              ? ((KeyEntry) me.getValue()).chain[0] : null);
      if (c != null && c.equals(cert)) {
        return me.getKey();
      }
    }
    return null;
  }

  @Override
  public void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException,
      CertificateException {
    if (password == null) {
      throw new IllegalArgumentException("password can't be null");
    }
    MessageDigest md = keyedDigest(password);
    DataOutputStream dos = new DataOutputStream(new DigestOutputStream(new java.io.BufferedOutputStream(stream), md));
    dos.writeInt(kMagic);
    dos.writeInt(kVersion2);
    dos.writeInt(entries.size());
    for (Map.Entry<String, Entry> me : entries.entrySet()) {
      Entry e = me.getValue();
      if (e instanceof KeyEntry) {
        KeyEntry ke = (KeyEntry) e;
        dos.writeInt(1);
        dos.writeUTF(me.getKey());
        dos.writeLong(ke.date.getTime());
        dos.writeInt(ke.protectedKey.length);
        dos.write(ke.protectedKey);
        int n = ke.chain == null ? 0 : ke.chain.length;
        dos.writeInt(n);
        for (int i = 0; i < n; i++) {
          writeCert(dos, ke.chain[i]);
        }
      } else {
        CertEntry ce = (CertEntry) e;
        dos.writeInt(2);
        dos.writeUTF(me.getKey());
        dos.writeLong(ce.date.getTime());
        writeCert(dos, ce.cert);
      }
    }
    dos.flush();
    byte[] digest = md.digest();
    dos.write(digest);
    dos.flush();
  }

  private static void writeCert(DataOutputStream dos, Certificate c) throws IOException, CertificateException {
    dos.writeUTF(c.getType());
    byte[] enc = c.getEncoded();
    dos.writeInt(enc.length);
    dos.write(enc);
  }

  private static MessageDigest keyedDigest(char[] password) {
    MessageDigest md = sha1();
    md.update(passwordBytes(password));
    md.update("Mighty Aphrodite".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    return md;
  }

  @Override
  public void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException,
      CertificateException {
    entries.clear();
    if (stream == null) {
      return;
    }
    MessageDigest md = password == null ? null : keyedDigest(password);
    DataInputStream dis = new DataInputStream(md == null ? new java.io.BufferedInputStream(stream)
        : new DigestInputStream(new java.io.BufferedInputStream(stream), md));
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
        KeyEntry ke = new KeyEntry();
        ke.date = date;
        ke.protectedKey = new byte[dis.readInt()];
        dis.readFully(ke.protectedKey);
        int n = dis.readInt();
        if (n > 0) {
          List<Certificate> chain = new ArrayList<Certificate>(n);
          for (int j = 0; j < n; j++) {
            chain.add(readCert(dis, version, factories));
          }
          ke.chain = chain.toArray(new Certificate[0]);
        }
        entries.put(alias, ke);
      } else if (tag == 2) {
        CertEntry ce = new CertEntry();
        ce.date = date;
        ce.cert = readCert(dis, version, factories);
        entries.put(alias, ce);
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
  public boolean engineProbe(InputStream stream) throws IOException {
    try {
      return new DataInputStream(stream).readInt() == kMagic;
    } catch (java.io.EOFException e) {
      return false;
    }
  }
}
