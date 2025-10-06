/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.x509;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.security.auth.x500.X500Principal;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class X509CertImpl extends X509Certificate {
  private static final long serialVersionUID = -3457612960190864406L;

  private final byte[] encoded;
  private byte[] tbs;
  private int version;
  private BigInteger serial;
  private String sigAlgOid;
  private byte[] sigAlgParams;
  private byte[] issuerDer;
  private byte[] subjectDer;
  private Date notBefore;
  private Date notAfter;
  private byte[] spki;
  private boolean[] issuerUid;
  private boolean[] subjectUid;
  private byte[] signature;
  private final Map<String, X509Ext> exts = new LinkedHashMap<String, X509Ext>();
  private transient PublicKey publicKey;
  private transient X500Name issuerName;
  private transient X500Name subjectName;

  public X509CertImpl(byte[] certData) throws CertificateException {
    this.encoded = certData.clone();
    try {
      parse();
    } catch (IOException e) {
      throw new CertificateException("Unable to initialize, " + e, e);
    } catch (RuntimeException e) {
      throw new CertificateException("Unable to initialize, " + e, e);
    }
  }

  public static X509CertImpl toImpl(X509Certificate cert) throws CertificateException {
    if (cert instanceof X509CertImpl) {
      return (X509CertImpl) cert;
    }
    return new X509CertImpl(cert.getEncoded());
  }

  private static long toEpoch(String s, boolean generalized) throws IOException {
    int p = 0;
    int year;
    if (generalized) {
      year = Integer.parseInt(s.substring(0, 4));
      p = 4;
    } else {
      int yy = Integer.parseInt(s.substring(0, 2));
      year = yy >= 50 ? 1900 + yy : 2000 + yy;
      p = 2;
    }
    int month = Integer.parseInt(s.substring(p, p + 2));
    int day = Integer.parseInt(s.substring(p + 2, p + 4));
    int hour = Integer.parseInt(s.substring(p + 4, p + 6));
    int minute = Integer.parseInt(s.substring(p + 6, p + 8));
    int second = 0;
    int q = p + 8;
    if (q < s.length() && Character.isDigit(s.charAt(q))) {
      second = Integer.parseInt(s.substring(q, q + 2));
      q += 2;
    }
    if (q >= s.length() || s.charAt(q) != 'Z') {
      throw new IOException("Invalid time encoding");
    }
    if (month < 1 || month > 12 || day < 1 || day > 31 || hour > 23 || minute > 59 || second > 60) {
      throw new IOException("Invalid time encoding");
    }
    long days = daysFromCivil(year, month, day);
    return ((days * 24 + hour) * 60 + minute) * 60000L + second * 1000L;
  }

  private static long daysFromCivil(int y, int m, int d) {
    y -= m <= 2 ? 1 : 0;
    long era = (y >= 0 ? y : y - 399) / 400;
    long yoe = y - era * 400;
    long doy = (153L * (m + (m > 2 ? -3 : 9)) + 2) / 5 + d - 1;
    long doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;
    return era * 146097 + doe - 719468;
  }

  static Date parseDate(DerValue v) throws IOException {
    return parseTime(v);
  }

  private static Date parseTime(DerValue v) throws IOException {
    if (v.getTag() == DerValue.tag_UtcTime) {
      return new Date(toEpoch(v.getAsString(), false));
    }
    if (v.getTag() == DerValue.tag_GeneralizedTime) {
      return new Date(toEpoch(v.getAsString(), true));
    }
    throw new IOException("Invalid encoding for time");
  }

  private void parse() throws IOException {
    DerInputStream top = new DerInputStream(encoded);
    DerValue cert = top.getDerValue();
    if (top.available() != 0) {
      throw new IOException("extra data at the end");
    }
    if (cert.getTag() != DerValue.tag_Sequence) {
      throw new IOException("signed fields invalid");
    }
    DerInputStream certIn = cert.data();
    DerValue tbsVal = certIn.getDerValue();
    DerValue sigAlg = certIn.getDerValue();
    DerValue sigBits = certIn.getDerValue();
    if (certIn.available() != 0) {
      throw new IOException("signed overrun, bytes = " + certIn.available());
    }
    if (tbsVal.getTag() != DerValue.tag_Sequence) {
      throw new IOException("signed fields invalid");
    }
    this.tbs = tbsVal.toByteArray();
    DerInputStream ai = sigAlg.data();
    sigAlgOid = ai.getOID().toString();
    sigAlgParams = ai.available() > 0 ? ai.getDerValue().toByteArray() : null;
    if (sigAlgParams != null && sigAlgParams.length == 2 && sigAlgParams[0] == 5) {
      sigAlgParams = null;
    }
    byte[] sigData = sigBits.getBitString();
    this.signature = sigData;
    DerInputStream in = tbsVal.data();
    DerValue next = in.getDerValue();
    version = 0;
    if (next.isContextSpecific((byte) 0)) {
      version = new DerValue(next.getDataBytes()).getInteger();
      if (version < 0 || version > 2) {
        throw new IOException("X.509 version, " + (version + 1) + ", is not supported");
      }
      next = in.getDerValue();
    }
    serial = next.getBigInteger();
    DerValue sigAlg2 = in.getDerValue();
    if (!sigAlg2.data().getOID().toString().equals(sigAlgOid)) {
      throw new IOException("Signature algorithm mismatch");
    }
    DerValue issuer = in.getDerValue();
    issuerDer = issuer.toByteArray();
    DerValue validity = in.getDerValue();
    DerInputStream vi = validity.data();
    notBefore = parseTime(vi.getDerValue());
    notAfter = parseTime(vi.getDerValue());
    DerValue subject = in.getDerValue();
    subjectDer = subject.toByteArray();
    spki = in.getDerValue().toByteArray();
    while (in.available() > 0) {
      DerValue opt = in.getDerValue();
      if (opt.isContextSpecific((byte) 1)) {
        issuerUid = trim(new DerValue(DerValue.tag_BitString, opt.getDataBytes()));
      } else if (opt.isContextSpecific((byte) 2)) {
        subjectUid = trim(new DerValue(DerValue.tag_BitString, opt.getDataBytes()));
      } else if (opt.isContextSpecific((byte) 3)) {
        DerValue seq = new DerValue(opt.getDataBytes());
        DerInputStream ei = seq.data();
        while (ei.available() > 0) {
          DerValue ext = ei.getDerValue();
          DerInputStream xi = ext.data();
          String oid = xi.getOID().toString();
          boolean critical = false;
          DerValue nextVal = xi.getDerValue();
          if (nextVal.getTag() == DerValue.tag_Boolean) {
            critical = nextVal.getBoolean();
            nextVal = xi.getDerValue();
          }
          byte[] val = nextVal.getOctetString();
          if (exts.containsKey(oid)) {
            throw new IOException("Duplicate extensions not allowed");
          }
          exts.put(oid, new X509Ext(oid, critical, val));
        }
      }
    }
  }

  private static boolean[] trim(DerValue bits) {
    byte[] data = bits.getDataBytes();
    int unused = data.length == 0 ? 0 : data[0] & 0xff;
    int total = (data.length - 1) * 8 - unused;
    boolean[] r = new boolean[Math.max(total, 0)];
    for (int i = 0; i < total; i++) {
      r[i] = (data[1 + i / 8] & (0x80 >> (i % 8))) != 0;
    }
    return r;
  }

  @Override
  public byte[] getEncoded() throws CertificateEncodingException {
    return encoded.clone();
  }

  @Override
  public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException,
      NoSuchProviderException, SignatureException {
    verify(key, "");
  }

  @Override
  public synchronized void verify(PublicKey key, String sigProvider) throws CertificateException,
      NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
    if (sigProvider == null) {
      sigProvider = "";
    }
    Signature sigVerf = sigProvider.isEmpty() ? Signature.getInstance(getSigAlgName())
        : Signature.getInstance(getSigAlgName(), sigProvider);
    doVerify(sigVerf, key);
  }

  @Override
  public synchronized void verify(PublicKey key, Provider sigProvider) throws CertificateException,
      NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature sigVerf = sigProvider == null ? Signature.getInstance(getSigAlgName())
        : Signature.getInstance(getSigAlgName(), sigProvider);
    doVerify(sigVerf, key);
  }

  private void doVerify(Signature sigVerf, PublicKey key) throws CertificateException, InvalidKeyException,
      SignatureException {
    if (sigAlgOid.equals("1.2.840.113549.1.1.10") && sigAlgParams != null) {
      try {
        sigVerf.setParameter(RSAUtilAccess.pssSpec(sigAlgParams));
      } catch (InvalidAlgorithmParameterException | IOException e) {
        throw new SignatureException("Invalid RSASSA-PSS parameters", e);
      }
    }
    sigVerf.initVerify(key);
    sigVerf.update(tbs, 0, tbs.length);
    if (!sigVerf.verify(signature)) {
      throw new SignatureException("Signature does not match.");
    }
  }

  public static void verify(X509Certificate cert, PublicKey key, Provider sigProvider) throws CertificateException,
      NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    toImpl(cert).verify(key, sigProvider);
  }

  @Override
  public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
    checkValidity(new Date());
  }

  @Override
  public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
    if (date.after(notAfter)) {
      throw new CertificateExpiredException("NotAfter: " + notAfter.toString());
    }
    if (date.before(notBefore)) {
      throw new CertificateNotYetValidException("NotBefore: " + notBefore.toString());
    }
  }

  @Override
  public int getVersion() {
    return version + 1;
  }

  @Override
  public BigInteger getSerialNumber() {
    return serial;
  }

  @Override
  public synchronized Principal getIssuerDN() {
    if (issuerName == null) {
      issuerName = X500Name.decode(issuerDer);
    }
    return issuerName;
  }

  @Override
  public X500Principal getIssuerX500Principal() {
    return new X500Principal(issuerDer);
  }

  @Override
  public synchronized Principal getSubjectDN() {
    if (subjectName == null) {
      subjectName = X500Name.decode(subjectDer);
    }
    return subjectName;
  }

  @Override
  public X500Principal getSubjectX500Principal() {
    return new X500Principal(subjectDer);
  }

  public static X500Principal getIssuerX500Principal(X509Certificate cert) {
    try {
      return toImpl(cert).getIssuerX500Principal();
    } catch (CertificateException e) {
      throw new RuntimeException("Could not parse issuer", e);
    }
  }

  public static X500Principal getSubjectX500Principal(X509Certificate cert) {
    try {
      return toImpl(cert).getSubjectX500Principal();
    } catch (CertificateException e) {
      throw new RuntimeException("Could not parse subject", e);
    }
  }

  @Override
  public Date getNotBefore() {
    return new Date(notBefore.getTime());
  }

  @Override
  public Date getNotAfter() {
    return new Date(notAfter.getTime());
  }

  @Override
  public byte[] getTBSCertificate() throws CertificateEncodingException {
    return tbs.clone();
  }

  @Override
  public byte[] getSignature() {
    return signature.clone();
  }

  @Override
  public String getSigAlgName() {
    return AlgorithmId.nameOf(sigAlgOid);
  }

  @Override
  public String getSigAlgOID() {
    return sigAlgOid;
  }

  @Override
  public byte[] getSigAlgParams() {
    return sigAlgParams == null ? null : sigAlgParams.clone();
  }

  @Override
  public boolean[] getIssuerUniqueID() {
    return issuerUid == null ? null : issuerUid.clone();
  }

  @Override
  public boolean[] getSubjectUniqueID() {
    return subjectUid == null ? null : subjectUid.clone();
  }

  @Override
  public synchronized PublicKey getPublicKey() {
    if (publicKey == null) {
      try {
        DerValue info = new DerValue(spki);
        String oid = info.data().getDerValue().data().getOID().toString();
        String alg = AlgorithmId.keyAlgorithm(oid);
        if (alg == null) {
          publicKey = new X509Key(oid, spki);
        } else {
          try {
            publicKey = java.security.KeyFactory.getInstance(alg).generatePublic(new X509EncodedKeySpec(spki));
          } catch (GeneralSecurityException e) {
            publicKey = new X509Key(oid, spki);
          }
        }
      } catch (IOException e) {
        throw new RuntimeException("Unable to decode public key", e);
      }
    }
    return publicKey;
  }

  private X509Ext ext(String oid) {
    return exts.get(oid);
  }

  @Override
  public boolean[] getKeyUsage() {
    X509Ext e = ext("2.5.29.15");
    if (e == null) {
      return null;
    }
    try {
      return X509Ext.bitsOf(new DerValue(e.value));
    } catch (IOException ex) {
      return null;
    }
  }

  @Override
  public int getBasicConstraints() {
    X509Ext e = ext("2.5.29.19");
    if (e == null) {
      return -1;
    }
    try {
      DerInputStream in = new DerValue(e.value).data();
      boolean ca = false;
      if (in.available() > 0 && in.peekByte() == DerValue.tag_Boolean) {
        ca = in.getDerValue().getBoolean();
      }
      if (!ca) {
        return -1;
      }
      if (in.available() > 0) {
        return in.getInteger();
      }
      return Integer.MAX_VALUE;
    } catch (IOException ex) {
      return -1;
    }
  }

  public static List<String> getExtendedKeyUsage(X509Certificate cert) throws CertificateParsingException {
    try {
      X509CertImpl impl = toImpl(cert);
      X509Ext e = impl.ext("2.5.29.37");
      if (e == null) {
        return null;
      }
      List<String> result = new ArrayList<String>();
      DerInputStream in = new DerValue(e.value).data();
      while (in.available() > 0) {
        result.add(in.getOID().toString());
      }
      return Collections.unmodifiableList(result);
    } catch (IOException | CertificateException ex) {
      throw new CertificateParsingException(ex);
    }
  }

  private static Collection<List<?>> generalNames(X509Ext e) throws IOException {
    if (e == null) {
      return null;
    }
    List<List<?>> result = new ArrayList<List<?>>();
    DerInputStream in = new DerValue(e.value).data();
    while (in.available() > 0) {
      DerValue gn = in.getDerValue();
      int tag = gn.getTag() & 0x1f;
      byte[] d = gn.getDataBytes();
      List<Object> item = new ArrayList<Object>(2);
      item.add(Integer.valueOf(tag));
      switch (tag) {
      case 1:
      case 2:
      case 6:
        item.add(new String(d, StandardCharsets.ISO_8859_1));
        break;
      case 4:
        item.add(X500Name.decode(d).getRFC2253Name(null));
        break;
      case 7:
        item.add(X509Ext.ipText(d));
        break;
      case 8:
        item.add(ObjectIdentifier.decode(d).toString());
        break;
      default:
        item.add(gn.toByteArray());
        break;
      }
      result.add(Collections.unmodifiableList(item));
    }
    return Collections.unmodifiableCollection(result);
  }

  public static Collection<List<?>> getSubjectAlternativeNames(X509Certificate cert)
      throws CertificateParsingException {
    try {
      return generalNames(toImpl(cert).ext("2.5.29.17"));
    } catch (IOException | CertificateException ex) {
      throw new CertificateParsingException(ex);
    }
  }

  public static Collection<List<?>> getIssuerAlternativeNames(X509Certificate cert)
      throws CertificateParsingException {
    try {
      return generalNames(toImpl(cert).ext("2.5.29.18"));
    } catch (IOException | CertificateException ex) {
      throw new CertificateParsingException(ex);
    }
  }

  @Override
  public boolean hasUnsupportedCriticalExtension() {
    for (X509Ext e : exts.values()) {
      if (e.critical && !X509Ext.isSupported(e.oid)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Set<String> getCriticalExtensionOIDs() {
    if (exts.isEmpty()) {
      return null;
    }
    Set<String> result = new HashSet<String>();
    for (X509Ext e : exts.values()) {
      if (e.critical) {
        result.add(e.oid);
      }
    }
    return Collections.unmodifiableSet(result);
  }

  @Override
  public Set<String> getNonCriticalExtensionOIDs() {
    if (exts.isEmpty()) {
      return null;
    }
    Set<String> result = new HashSet<String>();
    for (X509Ext e : exts.values()) {
      if (!e.critical) {
        result.add(e.oid);
      }
    }
    return Collections.unmodifiableSet(result);
  }

  @Override
  public byte[] getExtensionValue(String oid) {
    X509Ext e = exts.get(oid);
    if (e == null) {
      return null;
    }
    DerOutputStream out = new DerOutputStream();
    out.putOctetString(e.value);
    return out.toByteArray();
  }

  public X509Ext getExtension(String oid) {
    return exts.get(oid);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[\n");
    sb.append("[\n");
    sb.append("  Version: V").append(getVersion()).append('\n');
    sb.append("  Subject: ").append(getSubjectDN()).append('\n');
    sb.append("  Signature Algorithm: ").append(getSigAlgName()).append(", OID = ").append(sigAlgOid).append("\n\n");
    sb.append("  Key:  ").append(getPublicKey()).append('\n');
    sb.append("  Validity: [From: ").append(notBefore).append(",\n               To: ").append(notAfter)
        .append("]\n");
    sb.append("  Issuer: ").append(getIssuerDN()).append('\n');
    byte[] sb2 = serial.toByteArray();
    StringBuilder ser = new StringBuilder();
    for (int i = 0; i < sb2.length; i++) {
      if (i > 0) {
        ser.append(':');
      }
      ser.append(Character.forDigit((sb2[i] >> 4) & 0xf, 16)).append(Character.forDigit(sb2[i] & 0xf, 16));
    }
    sb.append("  SerialNumber: ").append(ser).append("\n");
    if (issuerUid != null) {
      sb.append("  Issuer Id:\n").append(bitsToHex(issuerUid)).append('\n');
    }
    if (subjectUid != null) {
      sb.append("  Subject Id:\n").append(bitsToHex(subjectUid)).append('\n');
    }
    if (!exts.isEmpty()) {
      sb.append("\nCertificate Extensions: ").append(exts.size()).append('\n');
      Map<String, X509Ext> sorted = new TreeMap<String, X509Ext>();
      for (X509Ext e : exts.values()) {
        sorted.put(X509Ext.nameOf(e.oid), e);
      }
      int i = 1;
      for (X509Ext e : sorted.values()) {
        sb.append('[').append(i++).append("]: ObjectId: ").append(e.oid).append(" Criticality=")
            .append(e.critical).append('\n');
        sb.append(X509Ext.describe(e)).append('\n');
      }
    }
    sb.append("]\n");
    sb.append("  Algorithm: [").append(getSigAlgName()).append("]\n");
    sb.append("  Signature:\n").append(X509Ext.hexDump(signature)).append('\n');
    sb.append(']');
    return sb.toString();
  }

  private static String bitsToHex(boolean[] bits) {
    byte[] b = new byte[(bits.length + 7) / 8];
    for (int i = 0; i < bits.length; i++) {
      if (bits[i]) {
        b[i / 8] |= (byte) (0x80 >> (i % 8));
      }
    }
    return X509Ext.hexDump(b);
  }
}
