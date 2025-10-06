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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class X509CRLImpl extends X509CRL {
  private final byte[] encoded;
  private byte[] tbs;
  private int version;
  private String sigAlgOid;
  private byte[] sigAlgParams;
  private byte[] issuerDer;
  private Date thisUpdate;
  private Date nextUpdate;
  private byte[] signature;
  private final Map<BigInteger, X509CRLEntry> revoked = new LinkedHashMap<BigInteger, X509CRLEntry>();
  private final Map<String, X509Ext> exts = new LinkedHashMap<String, X509Ext>();

  public X509CRLImpl(byte[] crlData) throws CRLException {
    this.encoded = crlData.clone();
    try {
      parse();
    } catch (IOException e) {
      throw new CRLException("Parsing error: " + e.getMessage(), e);
    }
  }

  private static Date time(DerValue v) throws IOException {
    return X509CertImpl.parseDate(v);
  }

  private void parse() throws IOException {
    DerInputStream top = new DerInputStream(encoded);
    DerValue crl = top.getDerValue();
    if (top.available() != 0) {
      throw new IOException("Extra data at end of the CRL");
    }
    DerInputStream in = crl.data();
    DerValue tbsVal = in.getDerValue();
    DerValue sigAlg = in.getDerValue();
    signature = in.getDerValue().getBitString();
    tbs = tbsVal.toByteArray();
    DerInputStream ai = sigAlg.data();
    sigAlgOid = ai.getOID().toString();
    sigAlgParams = ai.available() > 0 ? ai.getDerValue().toByteArray() : null;
    DerInputStream t = tbsVal.data();
    DerValue next = t.getDerValue();
    version = 0;
    if (next.getTag() == DerValue.tag_Integer) {
      version = next.getInteger();
      next = t.getDerValue();
    }
    issuerDer = t.getDerValue().toByteArray();
    thisUpdate = time(t.getDerValue());
    if (t.available() > 0 && (t.peekByte() == DerValue.tag_UtcTime || t.peekByte() == DerValue.tag_GeneralizedTime)) {
      nextUpdate = time(t.getDerValue());
    }
    while (t.available() > 0) {
      DerValue item = t.getDerValue();
      if (item.getTag() == DerValue.tag_Sequence) {
        DerInputStream ri = item.data();
        while (ri.available() > 0) {
          DerValue entry = ri.getDerValue();
          DerInputStream ei = entry.data();
          BigInteger serial = ei.getBigInteger();
          Date when = time(ei.getDerValue());
          revoked.put(serial, new Entry(serial, when, entry.toByteArray()));
        }
      } else if (item.isContextSpecific((byte) 0)) {
        DerInputStream ei = new DerValue(item.getDataBytes()).data();
        while (ei.available() > 0) {
          DerValue ext = ei.getDerValue();
          DerInputStream xi = ext.data();
          String oid = xi.getOID().toString();
          boolean critical = false;
          DerValue v = xi.getDerValue();
          if (v.getTag() == DerValue.tag_Boolean) {
            critical = v.getBoolean();
            v = xi.getDerValue();
          }
          exts.put(oid, new X509Ext(oid, critical, v.getOctetString()));
        }
      }
    }
  }

  private static final class Entry extends X509CRLEntry {
    private final BigInteger serial;
    private final Date date;
    private final byte[] enc;

    Entry(BigInteger serial, Date date, byte[] enc) {
      this.serial = serial;
      this.date = date;
      this.enc = enc;
    }

    @Override
    public byte[] getEncoded() {
      return enc.clone();
    }

    @Override
    public BigInteger getSerialNumber() {
      return serial;
    }

    @Override
    public Date getRevocationDate() {
      return new Date(date.getTime());
    }

    @Override
    public boolean hasExtensions() {
      return false;
    }

    @Override
    public boolean hasUnsupportedCriticalExtension() {
      return false;
    }

    @Override
    public Set<String> getCriticalExtensionOIDs() {
      return null;
    }

    @Override
    public Set<String> getNonCriticalExtensionOIDs() {
      return null;
    }

    @Override
    public byte[] getExtensionValue(String oid) {
      return null;
    }

    @Override
    public String toString() {
      return "[" + serial.toString(16) + "]";
    }
  }

  @Override
  public byte[] getEncoded() {
    return encoded.clone();
  }

  @Override
  public void verify(PublicKey key) throws CRLException, NoSuchAlgorithmException, InvalidKeyException,
      NoSuchProviderException, SignatureException {
    verify(key, "");
  }

  @Override
  public void verify(PublicKey key, String sigProvider) throws CRLException, NoSuchAlgorithmException,
      InvalidKeyException, NoSuchProviderException, SignatureException {
    Signature sigVerf = (sigProvider == null || sigProvider.isEmpty()) ? Signature.getInstance(getSigAlgName())
        : Signature.getInstance(getSigAlgName(), sigProvider);
    sigVerf.initVerify(key);
    sigVerf.update(tbs, 0, tbs.length);
    if (!sigVerf.verify(signature)) {
      throw new SignatureException("Signature does not match.");
    }
  }

  @Override
  public int getVersion() {
    return version + 1;
  }

  @Override
  public Principal getIssuerDN() {
    return X500Name.decode(issuerDer);
  }

  @Override
  public X500Principal getIssuerX500Principal() {
    return new X500Principal(issuerDer);
  }

  @Override
  public Date getThisUpdate() {
    return new Date(thisUpdate.getTime());
  }

  @Override
  public Date getNextUpdate() {
    return nextUpdate == null ? null : new Date(nextUpdate.getTime());
  }

  @Override
  public X509CRLEntry getRevokedCertificate(BigInteger serialNumber) {
    return revoked.get(serialNumber);
  }

  @Override
  public Set<? extends X509CRLEntry> getRevokedCertificates() {
    if (revoked.isEmpty()) {
      return null;
    }
    return Collections.unmodifiableSet(new LinkedHashSet<X509CRLEntry>(revoked.values()));
  }

  @Override
  public byte[] getTBSCertList() {
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
  public boolean isRevoked(Certificate cert) {
    if (!(cert instanceof X509Certificate)) {
      return false;
    }
    X509Certificate xcert = (X509Certificate) cert;
    X509CRLEntry e = revoked.get(xcert.getSerialNumber());
    return e != null && getIssuerX500Principal().equals(xcert.getIssuerX500Principal());
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
    Set<String> r = new HashSet<String>();
    for (X509Ext e : exts.values()) {
      if (e.critical) {
        r.add(e.oid);
      }
    }
    return r;
  }

  @Override
  public Set<String> getNonCriticalExtensionOIDs() {
    if (exts.isEmpty()) {
      return null;
    }
    Set<String> r = new HashSet<String>();
    for (X509Ext e : exts.values()) {
      if (!e.critical) {
        r.add(e.oid);
      }
    }
    return r;
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

  @Override
  public String toString() {
    return "X.509 CRL v" + getVersion() + "\nIssuer: " + getIssuerDN() + "\nThis Update: " + thisUpdate
        + (nextUpdate != null ? "\nNext Update: " + nextUpdate : "") + "\nRevoked: " + revoked.size() + "\n";
  }
}
