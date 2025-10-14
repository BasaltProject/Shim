/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.pkcs12;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.util.KeyStoreDelegator;
import sun.security.x509.AlgorithmId;

public class PKCS12KeyStore extends KeyStoreSpi {
  private static final String kDataOid = "1.2.840.113549.1.7.1";
  private static final String kEncryptedDataOid = "1.2.840.113549.1.7.6";
  private static final String kKeyBagOid = "1.2.840.113549.1.12.10.1.1";
  private static final String kShroudedKeyBagOid = "1.2.840.113549.1.12.10.1.2";
  private static final String kCertBagOid = "1.2.840.113549.1.12.10.1.3";
  private static final String kX509CertOid = "1.2.840.113549.1.9.22.1";
  private static final String kFriendlyNameOid = "1.2.840.113549.1.9.20";
  private static final String kLocalKeyIdOid = "1.2.840.113549.1.9.21";
  private static final String kTrustedKeyUsageOid = "2.16.840.1.113894.746875.1.1";
  private static final String kAnyEkuOid = "2.5.29.37.0";

  private static class Entry {
    Date date = new Date();
    String alias;
    byte[] keyId;
  }

  private static final class KeyEntry extends Entry {
    byte[] protectedKey;
    byte[] plainKey;
    Certificate[] chain;
  }

  private static final class CertEntry extends Entry {
    Certificate cert;
    boolean trusted;
  }

  private static final class Bag {
    String alias;
    byte[] keyId;
    byte[] value;
    Certificate cert;
    boolean trusted;
    boolean shrouded;
    boolean isKey;
  }

  private final Map<String, Entry> entries = new LinkedHashMap<String, Entry>();
  private int counter;

  public PKCS12KeyStore() {
  }

  public static final class DualFormatPKCS12 extends KeyStoreDelegator {
    public DualFormatPKCS12() {
      super(new PKCS12KeyStore(), new sun.security.provider.JavaKeyStore.JKS());
    }
  }

  private static String norm(String alias) {
    return alias.toLowerCase(Locale.ENGLISH);
  }

  @Override
  public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
    Entry e = entries.get(norm(alias));
    if (!(e instanceof KeyEntry)) {
      return null;
    }
    KeyEntry ke = (KeyEntry) e;
    byte[] plain = ke.plainKey;
    if (plain == null) {
      if (password == null) {
        throw new UnrecoverableKeyException("Password must not be null");
      }
      try {
        DerInputStream in = new DerValue(ke.protectedKey).data();
        byte[] algId = in.getDerValue().toByteArray();
        byte[] data = in.getOctetString();
        plain = PKCS12Crypto.decrypt(algId, password, data);
      } catch (NoSuchAlgorithmException nsae) {
        throw nsae;
      } catch (GeneralSecurityException | IOException ex) {
        UnrecoverableKeyException uke = new UnrecoverableKeyException("Get Key failed: " + ex.getMessage());
        uke.initCause(ex);
        throw uke;
      }
    }
    try {
      DerInputStream pki = new DerValue(plain).data();
      pki.getDerValue();
      String oid = pki.getDerValue().data().getOID().toString();
      String alg = AlgorithmId.keyAlgorithm(oid);
      if (alg == null) {
        throw new UnrecoverableKeyException("Get Key failed: unsupported key algorithm " + oid);
      }
      return KeyFactory.getInstance(alg).generatePrivate(new PKCS8EncodedKeySpec(plain));
    } catch (IOException | InvalidKeySpecException ex) {
      throw new UnrecoverableKeyException("Get Key failed: " + ex.getMessage());
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
      throw new KeyStoreException("Key is not a PrivateKey");
    }
    if (chain == null || chain.length == 0) {
      throw new KeyStoreException("Private key must be accompanied by certificate chain");
    }
    KeyEntry ke = new KeyEntry();
    ke.alias = alias;
    ke.chain = chain.clone();
    ke.keyId = ("Time " + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8);
    if (password == null) {
      ke.plainKey = key.getEncoded();
      entries.put(norm(alias), ke);
      return;
    }
    try {
      byte[][] enc = PKCS12Crypto.encryptPbes2(password, key.getEncoded(), new SecureRandom());
      DerOutputStream out = new DerOutputStream();
      out.write(enc[0]);
      out.putOctetString(enc[1]);
      DerOutputStream info = new DerOutputStream();
      info.write(DerValue.tag_Sequence, out);
      ke.protectedKey = info.toByteArray();
    } catch (GeneralSecurityException e) {
      throw new KeyStoreException("Key protection algorithm not found: " + e, e);
    }
    entries.put(norm(alias), ke);
  }

  @Override
  public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
    KeyEntry ke = new KeyEntry();
    ke.alias = alias;
    ke.chain = chain == null ? null : chain.clone();
    ke.keyId = ("Time " + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8);
    try {
      DerValue v = new DerValue(key);
      v.data().getDerValue();
    } catch (IOException e) {
      throw new KeyStoreException("Private key is not stored as PKCS#8 EncryptedPrivateKeyInfo: " + e, e);
    }
    ke.protectedKey = key.clone();
    entries.put(norm(alias), ke);
  }

  @Override
  public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
    Entry old = entries.get(norm(alias));
    if (old instanceof KeyEntry) {
      throw new KeyStoreException("Cannot overwrite own certificate");
    }
    CertEntry ce = new CertEntry();
    ce.alias = alias;
    ce.cert = cert;
    ce.trusted = true;
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
      Entry e = me.getValue();
      Certificate c = e instanceof CertEntry ? ((CertEntry) e).cert : (((KeyEntry) e).chain != null
          && ((KeyEntry) e).chain.length > 0 ? ((KeyEntry) e).chain[0] : null);
      if (c != null && c.equals(cert)) {
        return me.getKey();
      }
    }
    return null;
  }

  @Override
  public boolean engineProbe(InputStream stream) throws IOException {
    return stream.read() == 0x30;
  }

  private static byte[] bmp(String s) {
    return s.getBytes(StandardCharsets.UTF_16BE);
  }

  private static byte[] tlv(int tag, byte[] content) {
    DerOutputStream o = new DerOutputStream();
    o.write((byte) tag, content);
    return o.toByteArray();
  }

  private static byte[] oidDer(String oid) throws IOException {
    DerOutputStream o = new DerOutputStream();
    o.putOID(new ObjectIdentifier(oid));
    return o.toByteArray();
  }

  private static byte[] cat(byte[]... parts) {
    int n = 0;
    for (byte[] p : parts) {
      n += p.length;
    }
    byte[] out = new byte[n];
    int o = 0;
    for (byte[] p : parts) {
      System.arraycopy(p, 0, out, o, p.length);
      o += p.length;
    }
    return out;
  }

  private static byte[] attribute(String oid, byte[] valueDer) throws IOException {
    return tlv(DerValue.tag_Sequence, cat(oidDer(oid), tlv(DerValue.tag_Set, valueDer)));
  }

  private static byte[] attributes(byte[]... attrs) {
    return tlv(DerValue.tag_Set, cat(attrs));
  }

  private static byte[] bag(String bagOid, byte[] valueDer, byte[] attrs) throws IOException {
    return tlv(DerValue.tag_Sequence, cat(oidDer(bagOid), tlv(0xa0, valueDer), attrs));
  }

  @Override
  public void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException,
      CertificateException {
    SecureRandom random = new SecureRandom();
    ByteArrayOutputStream keyBags = new ByteArrayOutputStream();
    ByteArrayOutputStream certBags = new ByteArrayOutputStream();
    for (Map.Entry<String, Entry> me : entries.entrySet()) {
      Entry e = me.getValue();
      byte[] name = attribute(kFriendlyNameOid, tlv(0x1e, bmp(e.alias)));
      if (e instanceof KeyEntry) {
        KeyEntry ke = (KeyEntry) e;
        byte[] id = attribute(kLocalKeyIdOid, tlv(4, ke.keyId));
        if (ke.protectedKey != null) {
          keyBags.write(bag(kShroudedKeyBagOid, ke.protectedKey, attributes(name, id)));
        } else {
          keyBags.write(bag(kKeyBagOid, ke.plainKey, attributes(name, id)));
        }
        Certificate[] chain = ke.chain == null ? new Certificate[0] : ke.chain;
        for (int i = 0; i < chain.length; i++) {
          certBags.write(bag(kCertBagOid, certBody(chain[i]), i == 0 ? attributes(name, id) : new byte[0]));
        }
      } else {
        CertEntry ce = (CertEntry) e;
        byte[] trust = attribute(kTrustedKeyUsageOid, oidDer(kAnyEkuOid));
        certBags.write(bag(kCertBagOid, certBody(ce.cert), attributes(name, trust)));
      }
    }
    ByteArrayOutputStream authSafes = new ByteArrayOutputStream();
    byte[] keySafe = tlv(DerValue.tag_Sequence, keyBags.toByteArray());
    authSafes.write(tlv(DerValue.tag_Sequence, cat(oidDer(kDataOid), tlv(0xa0, tlv(4, keySafe)))));
    if (certBags.size() > 0) {
      byte[] certSafe = tlv(DerValue.tag_Sequence, certBags.toByteArray());
      if (password == null) {
        authSafes.write(tlv(DerValue.tag_Sequence, cat(oidDer(kDataOid), tlv(0xa0, tlv(4, certSafe)))));
      } else {
        try {
          byte[][] enc = PKCS12Crypto.encryptPbes2(password, certSafe, random);
          byte[] encContent = tlv(DerValue.tag_Sequence, cat(oidDer(kDataOid), enc[0], tlv(0x80, enc[1])));
          byte[] encData = tlv(DerValue.tag_Sequence, cat(new byte[] {2, 1, 0}, encContent));
          authSafes.write(tlv(DerValue.tag_Sequence, cat(oidDer(kEncryptedDataOid), tlv(0xa0, encData))));
        } catch (GeneralSecurityException ex) {
          throw new IOException("Error encrypting certificates: " + ex.getMessage(), ex);
        }
      }
    }
    byte[] authSafeSeq = tlv(DerValue.tag_Sequence, authSafes.toByteArray());
    byte[] authSafe = tlv(DerValue.tag_Sequence, cat(oidDer(kDataOid), tlv(0xa0, tlv(4, authSafeSeq))));
    byte[] macData = new byte[0];
    if (password != null) {
      byte[] salt = new byte[PKCS12Crypto.kSaltLength];
      random.nextBytes(salt);
      byte[] digest;
      try {
        digest = PKCS12Crypto.mac("SHA-256", PKCS12Crypto.bmpPassword(password), salt,
            PKCS12Crypto.kDefaultIterations, authSafeSeq);
      } catch (GeneralSecurityException ex) {
        throw new IOException("Error computing MAC: " + ex.getMessage(), ex);
      }
      byte[] algId = tlv(DerValue.tag_Sequence, cat(oidDer(PKCS12Crypto.macOid("SHA-256")), new byte[] {5, 0}));
      byte[] digestInfo = tlv(DerValue.tag_Sequence, cat(algId, tlv(4, digest)));
      DerOutputStream iter = new DerOutputStream();
      iter.putInteger(PKCS12Crypto.kDefaultIterations);
      macData = tlv(DerValue.tag_Sequence, cat(digestInfo, tlv(4, salt), iter.toByteArray()));
    }
    stream.write(tlv(DerValue.tag_Sequence, cat(new byte[] {2, 1, 3}, authSafe, macData)));
    stream.flush();
  }

  private static byte[] certBody(Certificate c) throws CertificateException, IOException {
    return tlv(DerValue.tag_Sequence, cat(oidDer(kX509CertOid), tlv(0xa0, tlv(4, c.getEncoded()))));
  }

  @Override
  public void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException,
      CertificateException {
    entries.clear();
    counter = 0;
    if (stream == null) {
      return;
    }
    byte[] all = readAll(stream);
    if (all.length == 0) {
      throw new IOException("Tag number over 30 is not supported");
    }
    if (all[0] != DerValue.tag_Sequence) {
      throw new IOException("toDerInputStream rejects tag type " + (all[0] & 0xff));
    }
    DerValue pfx;
    try {
      pfx = new DerValue(all);
    } catch (IOException e) {
      throw new EOFException();
    }
    DerInputStream pin = pfx.data();
    int version = pin.getInteger();
    if (version != 3) {
      throw new IOException("PKCS12 keystore not in version 3 format");
    }
    DerInputStream ci = pin.getDerValue().data();
    String contentType = ci.getOID().toString();
    if (!contentType.equals(kDataOid)) {
      throw new IOException("public key signed PKCS12 not supported");
    }
    byte[] authSafeSeq = new DerValue(ci.getDerValue().getDataBytes()).getOctetString();
    DerValue macData = pin.available() > 0 ? pin.getDerValue() : null;
    DerInputStream safes = new DerValue(authSafeSeq).data();
    List<Bag> bags = new ArrayList<Bag>();
    while (safes.available() > 0) {
      DerInputStream sci = safes.getDerValue().data();
      String type = sci.getOID().toString();
      byte[] content = sci.getDerValue().getDataBytes();
      byte[] safeContents;
      if (type.equals(kDataOid)) {
        safeContents = new DerValue(content).getOctetString();
      } else if (type.equals(kEncryptedDataOid)) {
        if (password == null) {
          continue;
        }
        DerInputStream ed = new DerValue(content).data();
        ed.getInteger();
        DerInputStream eci = ed.getDerValue().data();
        eci.getOID();
        byte[] algId = eci.getDerValue().toByteArray();
        DerValue encVal = eci.getDerValue();
        byte[] encBytes = encVal.getTag() == DerValue.tag_OctetString ? encVal.getOctetString()
            : encVal.getDataBytes();
        try {
          safeContents = PKCS12Crypto.decrypt(algId, password, encBytes);
        } catch (GeneralSecurityException e) {
          UnrecoverableKeyException uke = new UnrecoverableKeyException(
              "failed to decrypt safe contents entry: " + e);
          throw new IOException("keystore password was incorrect", uke);
        }
      } else {
        throw new IOException("public key signed PKCS12 not supported");
      }
      parseSafeContents(safeContents, bags);
    }
    if (macData != null && password != null) {
      verifyMac(macData, authSafeSeq, password);
    }
    assemble(bags);
  }

  private static byte[] readAll(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buf = new byte[8192];
    int n;
    while ((n = in.read(buf)) > 0) {
      out.write(buf, 0, n);
    }
    return out.toByteArray();
  }

  private void verifyMac(DerValue macData, byte[] authSafeSeq, char[] password) throws IOException {
    DerInputStream mi = macData.data();
    DerInputStream di = mi.getDerValue().data();
    String oid = di.getDerValue().data().getOID().toString();
    byte[] expected = di.getOctetString();
    byte[] salt = mi.getOctetString();
    int iterations = mi.available() > 0 ? mi.getInteger() : 1;
    String hash = PKCS12Crypto.macHash(oid);
    if (hash == null) {
      throw new IOException("unsupported MAC algorithm " + oid);
    }
    try {
      byte[] actual = PKCS12Crypto.mac(hash, PKCS12Crypto.bmpPassword(password), salt, iterations, authSafeSeq);
      if (!MessageDigest.isEqual(expected, actual) && password.length == 0) {
        actual = PKCS12Crypto.mac(hash, new byte[0], salt, iterations, authSafeSeq);
      }
      if (!MessageDigest.isEqual(expected, actual)) {
        UnrecoverableKeyException uke = new UnrecoverableKeyException("Failed PKCS12 integrity checking");
        throw new IOException("Integrity check failed: " + uke, uke);
      }
    } catch (GeneralSecurityException e) {
      throw new IOException("Integrity check failed: " + e, e);
    }
  }

  private static void parseSafeContents(byte[] data, List<Bag> out) throws IOException, CertificateException {
    DerInputStream in = new DerValue(data).data();
    while (in.available() > 0) {
      DerInputStream bi = in.getDerValue().data();
      String bagOid = bi.getOID().toString();
      DerValue value = bi.getDerValue();
      Bag bag = new Bag();
      if (bi.available() > 0) {
        DerInputStream ai = bi.getDerValue().data();
        while (ai.available() > 0) {
          DerInputStream one = ai.getDerValue().data();
          String aoid = one.getOID().toString();
          DerInputStream vals = one.getDerValue().data();
          if (vals.available() > 0) {
            DerValue v = vals.getDerValue();
            if (aoid.equals(kFriendlyNameOid)) {
              bag.alias = new String(v.getDataBytes(), StandardCharsets.UTF_16BE);
            } else if (aoid.equals(kLocalKeyIdOid)) {
              bag.keyId = v.getDataBytes();
            } else if (aoid.equals(kTrustedKeyUsageOid)) {
              bag.trusted = true;
            }
          }
        }
      }
      byte[] inner = value.getDataBytes();
      if (bagOid.equals(kShroudedKeyBagOid)) {
        bag.isKey = true;
        bag.shrouded = true;
        bag.value = inner;
      } else if (bagOid.equals(kKeyBagOid)) {
        bag.isKey = true;
        bag.value = inner;
      } else if (bagOid.equals(kCertBagOid)) {
        DerInputStream cb = new DerValue(inner).data();
        String certType = cb.getOID().toString();
        if (!certType.equals(kX509CertOid)) {
          continue;
        }
        byte[] der = new DerValue(cb.getDerValue().getDataBytes()).getOctetString();
        bag.cert = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(der));
      } else {
        continue;
      }
      out.add(bag);
    }
  }

  private static boolean sameId(byte[] a, byte[] b) {
    return a != null && b != null && Arrays.equals(a, b);
  }

  private void assemble(List<Bag> bags) {
    List<Bag> certs = new ArrayList<Bag>();
    List<Bag> keys = new ArrayList<Bag>();
    for (Bag b : bags) {
      if (b.isKey) {
        keys.add(b);
      } else {
        certs.add(b);
      }
    }
    Set<Bag> used = new HashSet<Bag>();
    for (Bag kb : keys) {
      KeyEntry ke = new KeyEntry();
      if (kb.shrouded) {
        ke.protectedKey = kb.value;
      } else {
        ke.plainKey = kb.value;
      }
      ke.keyId = kb.keyId;
      Bag first = null;
      for (Bag cb : certs) {
        if (sameId(cb.keyId, kb.keyId)) {
          first = cb;
          break;
        }
      }
      if (first == null && keys.size() == 1 && kb.keyId == null) {
        for (Bag cb : certs) {
          if (cb.keyId == null) {
            first = cb;
            break;
          }
        }
      }
      List<Certificate> chain = new ArrayList<Certificate>();
      if (first != null) {
        used.add(first);
        chain.add(first.cert);
        Certificate last = first.cert;
        boolean progress = true;
        while (progress && last instanceof X509Certificate) {
          progress = false;
          X509Certificate x = (X509Certificate) last;
          if (x.getSubjectX500Principal().equals(x.getIssuerX500Principal())) {
            break;
          }
          for (Bag cb : certs) {
            if (cb.cert instanceof X509Certificate && !chain.contains(cb.cert)
                && ((X509Certificate) cb.cert).getSubjectX500Principal().equals(x.getIssuerX500Principal())) {
              chain.add(cb.cert);
              used.add(cb);
              last = cb.cert;
              progress = true;
              break;
            }
          }
        }
      }
      ke.chain = chain.isEmpty() ? null : chain.toArray(new Certificate[0]);
      String alias = kb.alias != null ? kb.alias : (first != null && first.alias != null ? first.alias : null);
      ke.alias = alias != null ? alias : String.valueOf(++counter);
      entries.put(norm(ke.alias), ke);
    }
    for (Bag cb : certs) {
      if (!cb.trusted) {
        continue;
      }
      CertEntry ce = new CertEntry();
      ce.cert = cb.cert;
      ce.trusted = true;
      ce.alias = cb.alias != null ? cb.alias : String.valueOf(++counter);
      entries.put(norm(ce.alias), ce);
    }
  }
}
