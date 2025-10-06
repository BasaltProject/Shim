/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.io.IOException;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import sun.security.util.ObjectIdentifier;

public class X509CertSelector implements CertSelector {
  private X509Certificate x509Cert;
  private BigInteger serialNumber;
  private X500Principal issuer;
  private X500Principal subject;
  private byte[] subjectKeyID;
  private byte[] authorityKeyID;
  private Date certificateValid;
  private Date privateKeyValid;
  private String subjectPublicKeyAlgID;
  private PublicKey subjectPublicKey;
  private boolean[] keyUsage;
  private Set<String> keyPurposeSet;
  private boolean matchAllSubjectAltNames = true;
  private Set<List<?>> subjectAlternativeNames;
  private byte[] nameConstraints;
  private int basicConstraints = -1;
  private Set<String> policy;
  private Set<List<?>> pathToNames;

  public X509CertSelector() {
  }

  public void setCertificate(X509Certificate cert) {
    x509Cert = cert;
  }

  public void setSerialNumber(BigInteger serial) {
    serialNumber = serial;
  }

  public void setIssuer(X500Principal issuer) {
    this.issuer = issuer;
  }

  public void setIssuer(String issuerDN) throws IOException {
    this.issuer = issuerDN == null ? null : new X500Principal(issuerDN);
  }

  public void setIssuer(byte[] issuerDN) throws IOException {
    this.issuer = issuerDN == null ? null : new X500Principal(issuerDN);
  }

  public void setSubject(X500Principal subject) {
    this.subject = subject;
  }

  public void setSubject(String subjectDN) throws IOException {
    this.subject = subjectDN == null ? null : new X500Principal(subjectDN);
  }

  public void setSubject(byte[] subjectDN) throws IOException {
    this.subject = subjectDN == null ? null : new X500Principal(subjectDN);
  }

  public void setSubjectKeyIdentifier(byte[] subjectKeyID) {
    this.subjectKeyID = subjectKeyID == null ? null : subjectKeyID.clone();
  }

  public void setAuthorityKeyIdentifier(byte[] authorityKeyID) {
    this.authorityKeyID = authorityKeyID == null ? null : authorityKeyID.clone();
  }

  public void setCertificateValid(Date certValid) {
    this.certificateValid = certValid == null ? null : (Date) certValid.clone();
  }

  public void setPrivateKeyValid(Date privateKeyValid) {
    this.privateKeyValid = privateKeyValid == null ? null : (Date) privateKeyValid.clone();
  }

  public void setSubjectPublicKeyAlgID(String oid) throws IOException {
    if (oid == null) {
      subjectPublicKeyAlgID = null;
    } else {
      subjectPublicKeyAlgID = new ObjectIdentifier(oid).toString();
    }
  }

  public void setSubjectPublicKey(PublicKey key) {
    subjectPublicKey = key;
  }

  public void setSubjectPublicKey(byte[] key) throws IOException {
    throw new IOException("subjectPublicKey from bytes not supported");
  }

  public void setKeyUsage(boolean[] keyUsage) {
    this.keyUsage = keyUsage == null ? null : keyUsage.clone();
  }

  public void setExtendedKeyUsage(Set<String> keyPurposeSet) throws IOException {
    if (keyPurposeSet == null || keyPurposeSet.isEmpty()) {
      this.keyPurposeSet = null;
    } else {
      this.keyPurposeSet = new HashSet<String>(keyPurposeSet);
      for (String s : this.keyPurposeSet) {
        new ObjectIdentifier(s);
      }
    }
  }

  public void setMatchAllSubjectAltNames(boolean matchAllNames) {
    this.matchAllSubjectAltNames = matchAllNames;
  }

  public void setSubjectAlternativeNames(Collection<List<?>> names) throws IOException {
    if (names == null) {
      subjectAlternativeNames = null;
    } else {
      subjectAlternativeNames = new HashSet<List<?>>(names);
    }
  }

  public void addSubjectAlternativeName(int type, String name) throws IOException {
    if (subjectAlternativeNames == null) {
      subjectAlternativeNames = new HashSet<List<?>>();
    }
    List<Object> l = new ArrayList<Object>(2);
    l.add(Integer.valueOf(type));
    l.add(name);
    subjectAlternativeNames.add(l);
  }

  public void addSubjectAlternativeName(int type, byte[] name) throws IOException {
    if (subjectAlternativeNames == null) {
      subjectAlternativeNames = new HashSet<List<?>>();
    }
    List<Object> l = new ArrayList<Object>(2);
    l.add(Integer.valueOf(type));
    l.add(name.clone());
    subjectAlternativeNames.add(l);
  }

  public void setNameConstraints(byte[] bytes) throws IOException {
    nameConstraints = bytes == null ? null : bytes.clone();
  }

  public void setBasicConstraints(int minMaxPathLen) {
    if (minMaxPathLen < -2) {
      throw new IllegalArgumentException("basic constraints less than -2");
    }
    basicConstraints = minMaxPathLen;
  }

  public void setPolicy(Set<String> certPolicySet) throws IOException {
    policy = certPolicySet == null ? null : new HashSet<String>(certPolicySet);
  }

  public void setPathToNames(Collection<List<?>> names) throws IOException {
    pathToNames = names == null ? null : new HashSet<List<?>>(names);
  }

  public void addPathToName(int type, String name) throws IOException {
    if (pathToNames == null) {
      pathToNames = new HashSet<List<?>>();
    }
    List<Object> l = new ArrayList<Object>(2);
    l.add(Integer.valueOf(type));
    l.add(name);
    pathToNames.add(l);
  }

  public void addPathToName(int type, byte[] name) throws IOException {
    if (pathToNames == null) {
      pathToNames = new HashSet<List<?>>();
    }
    List<Object> l = new ArrayList<Object>(2);
    l.add(Integer.valueOf(type));
    l.add(name.clone());
    pathToNames.add(l);
  }

  public X509Certificate getCertificate() {
    return x509Cert;
  }

  public BigInteger getSerialNumber() {
    return serialNumber;
  }

  public X500Principal getIssuer() {
    return issuer;
  }

  public String getIssuerAsString() {
    return issuer == null ? null : issuer.getName();
  }

  public byte[] getIssuerAsBytes() throws IOException {
    return issuer == null ? null : issuer.getEncoded();
  }

  public X500Principal getSubject() {
    return subject;
  }

  public String getSubjectAsString() {
    return subject == null ? null : subject.getName();
  }

  public byte[] getSubjectAsBytes() throws IOException {
    return subject == null ? null : subject.getEncoded();
  }

  public byte[] getSubjectKeyIdentifier() {
    return subjectKeyID == null ? null : subjectKeyID.clone();
  }

  public byte[] getAuthorityKeyIdentifier() {
    return authorityKeyID == null ? null : authorityKeyID.clone();
  }

  public Date getCertificateValid() {
    return certificateValid == null ? null : (Date) certificateValid.clone();
  }

  public Date getPrivateKeyValid() {
    return privateKeyValid == null ? null : (Date) privateKeyValid.clone();
  }

  public String getSubjectPublicKeyAlgID() {
    return subjectPublicKeyAlgID;
  }

  public PublicKey getSubjectPublicKey() {
    return subjectPublicKey;
  }

  public boolean[] getKeyUsage() {
    return keyUsage == null ? null : keyUsage.clone();
  }

  public Set<String> getExtendedKeyUsage() {
    return keyPurposeSet == null ? null : Collections.unmodifiableSet(keyPurposeSet);
  }

  public boolean getMatchAllSubjectAltNames() {
    return matchAllSubjectAltNames;
  }

  public Collection<List<?>> getSubjectAlternativeNames() {
    return subjectAlternativeNames == null ? null : Collections.unmodifiableCollection(subjectAlternativeNames);
  }

  public byte[] getNameConstraints() {
    return nameConstraints == null ? null : nameConstraints.clone();
  }

  public int getBasicConstraints() {
    return basicConstraints;
  }

  public Set<String> getPolicy() {
    return policy == null ? null : Collections.unmodifiableSet(policy);
  }

  public Collection<List<?>> getPathToNames() {
    return pathToNames == null ? null : Collections.unmodifiableCollection(pathToNames);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("X509CertSelector: [\n");
    if (x509Cert != null) {
      sb.append("  Certificate: " + x509Cert.toString() + "\n");
    }
    if (serialNumber != null) {
      sb.append("  Serial Number: " + serialNumber.toString() + "\n");
    }
    if (issuer != null) {
      sb.append("  Issuer: " + getIssuerAsString() + "\n");
    }
    if (subject != null) {
      sb.append("  Subject: " + getSubjectAsString() + "\n");
    }
    sb.append("  matchAllSubjectAltNames flag: " + String.valueOf(matchAllSubjectAltNames) + "\n");
    sb.append("]");
    return sb.toString();
  }

  private static byte[] keyId(byte[] extValue) {
    try {
      sun.security.util.DerValue v = new sun.security.util.DerValue(extValue);
      return v.getOctetString();
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public boolean match(Certificate cert) {
    if (!(cert instanceof X509Certificate)) {
      return false;
    }
    X509Certificate xcert = (X509Certificate) cert;
    if (x509Cert != null && !x509Cert.equals(xcert)) {
      return false;
    }
    if (serialNumber != null && !serialNumber.equals(xcert.getSerialNumber())) {
      return false;
    }
    if (issuer != null && !issuer.equals(xcert.getIssuerX500Principal())) {
      return false;
    }
    if (subject != null && !subject.equals(xcert.getSubjectX500Principal())) {
      return false;
    }
    if (subjectKeyID != null) {
      byte[] ext = xcert.getExtensionValue("2.5.29.14");
      if (ext == null) {
        return false;
      }
      byte[] inner = keyId(ext);
      byte[] id = inner == null ? null : keyId(inner);
      if (id == null || !Arrays.equals(subjectKeyID, id)) {
        return false;
      }
    }
    if (authorityKeyID != null) {
      byte[] ext = xcert.getExtensionValue("2.5.29.35");
      if (ext == null) {
        return false;
      }
      byte[] inner = keyId(ext);
      if (inner == null || !Arrays.equals(authorityKeyID, inner)) {
        return false;
      }
    }
    if (certificateValid != null) {
      try {
        xcert.checkValidity(certificateValid);
      } catch (CertificateException e) {
        return false;
      }
    }
    if (subjectPublicKey != null && !subjectPublicKey.equals(xcert.getPublicKey())) {
      return false;
    }
    if (subjectPublicKeyAlgID != null) {
      try {
        sun.security.util.DerValue spki = new sun.security.util.DerValue(xcert.getPublicKey().getEncoded());
        String oid = spki.data().getDerValue().data().getOID().toString();
        if (!subjectPublicKeyAlgID.equals(oid)) {
          return false;
        }
      } catch (IOException e) {
        return false;
      }
    }
    if (keyUsage != null) {
      boolean[] ku = xcert.getKeyUsage();
      if (ku != null) {
        for (int i = 0; i < keyUsage.length; i++) {
          if (keyUsage[i] && (i >= ku.length || !ku[i])) {
            return false;
          }
        }
      }
    }
    if (keyPurposeSet != null) {
      try {
        List<String> eku = xcert.getExtendedKeyUsage();
        if (eku != null && !eku.contains("2.5.29.37.0") && !eku.containsAll(keyPurposeSet)) {
          return false;
        }
      } catch (CertificateParsingException e) {
        return false;
      }
    }
    if (subjectAlternativeNames != null) {
      try {
        Collection<List<?>> sans = xcert.getSubjectAlternativeNames();
        if (sans == null) {
          return false;
        }
        boolean any = false;
        boolean all = true;
        for (List<?> want : subjectAlternativeNames) {
          boolean found = false;
          for (List<?> have : sans) {
            if (have.get(0).equals(want.get(0)) && sameName(have.get(1), want.get(1))) {
              found = true;
              break;
            }
          }
          any |= found;
          all &= found;
        }
        if (matchAllSubjectAltNames ? !all : !any) {
          return false;
        }
      } catch (CertificateParsingException e) {
        return false;
      }
    }
    if (basicConstraints != -1) {
      int bc = xcert.getBasicConstraints();
      if (basicConstraints == -2) {
        if (bc != -1) {
          return false;
        }
      } else if (bc < basicConstraints) {
        return false;
      }
    }
    return true;
  }

  private static boolean sameName(Object a, Object b) {
    if (a instanceof String && b instanceof String) {
      return ((String) a).equalsIgnoreCase((String) b);
    }
    if (a instanceof byte[] && b instanceof byte[]) {
      return Arrays.equals((byte[]) a, (byte[]) b);
    }
    return a.equals(b);
  }

  @Override
  public Object clone() {
    try {
      X509CertSelector copy = (X509CertSelector) super.clone();
      if (subjectAlternativeNames != null) {
        copy.subjectAlternativeNames = new HashSet<List<?>>(subjectAlternativeNames);
      }
      if (pathToNames != null) {
        copy.pathToNames = new HashSet<List<?>>(pathToNames);
      }
      if (keyPurposeSet != null) {
        copy.keyPurposeSet = new HashSet<String>(keyPurposeSet);
      }
      return copy;
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e.toString(), e);
    }
  }
}
