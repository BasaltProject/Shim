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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import javax.security.auth.x500.X500Principal;

public class X509CRLSelector implements CRLSelector {
  private HashSet<X500Principal> issuerNames;
  private BigInteger minCRL;
  private BigInteger maxCRL;
  private long dateAndTime = -1;
  private X509Certificate certChecking;

  public X509CRLSelector() {
  }

  public void setIssuers(Collection<X500Principal> issuers) {
    if ((issuers == null) || issuers.isEmpty()) {
      issuerNames = null;
    } else {
      issuerNames = new HashSet<X500Principal>(issuers);
    }
  }

  public void setIssuerNames(Collection<?> names) throws IOException {
    if (names == null || names.isEmpty()) {
      issuerNames = null;
      return;
    }
    HashSet<X500Principal> set = new HashSet<X500Principal>();
    for (Object o : names) {
      if (o instanceof String) {
        set.add(new X500Principal((String) o));
      } else if (o instanceof byte[]) {
        set.add(new X500Principal((byte[]) o));
      } else {
        throw new IOException("name not byte array or String");
      }
    }
    issuerNames = set;
  }

  public void addIssuer(X500Principal issuer) {
    if (issuerNames == null) {
      issuerNames = new HashSet<X500Principal>();
    }
    issuerNames.add(issuer);
  }

  public void addIssuerName(String name) throws IOException {
    addIssuer(new X500Principal(name));
  }

  public void addIssuerName(byte[] name) throws IOException {
    addIssuer(new X500Principal(name));
  }

  public void setMinCRLNumber(BigInteger minCRL) {
    this.minCRL = minCRL;
  }

  public void setMaxCRLNumber(BigInteger maxCRL) {
    this.maxCRL = maxCRL;
  }

  public void setDateAndTime(Date dateAndTime) {
    this.dateAndTime = dateAndTime == null ? -1 : dateAndTime.getTime();
  }

  public void setCertificateChecking(X509Certificate cert) {
    certChecking = cert;
  }

  public Collection<X500Principal> getIssuers() {
    return issuerNames == null ? null : Collections.unmodifiableCollection(issuerNames);
  }

  public Collection<Object> getIssuerNames() {
    if (issuerNames == null) {
      return null;
    }
    List<Object> r = new ArrayList<Object>();
    for (X500Principal p : issuerNames) {
      r.add(p.getEncoded());
    }
    return Collections.unmodifiableCollection(r);
  }

  public BigInteger getMinCRL() {
    return minCRL;
  }

  public BigInteger getMaxCRL() {
    return maxCRL;
  }

  public Date getDateAndTime() {
    return dateAndTime == -1 ? null : new Date(dateAndTime);
  }

  public X509Certificate getCertificateChecking() {
    return certChecking;
  }

  @Override
  public String toString() {
    return "X509CRLSelector: [\n]";
  }

  @Override
  public boolean match(CRL crl) {
    if (!(crl instanceof X509CRL)) {
      return false;
    }
    X509CRL xcrl = (X509CRL) crl;
    if (issuerNames != null && !issuerNames.contains(xcrl.getIssuerX500Principal())) {
      return false;
    }
    if (dateAndTime != -1) {
      Date thisUpdate = xcrl.getThisUpdate();
      Date nextUpdate = xcrl.getNextUpdate();
      if (nextUpdate == null || dateAndTime < thisUpdate.getTime() || dateAndTime >= nextUpdate.getTime()) {
        return false;
      }
    }
    if (certChecking != null && !certChecking.getIssuerX500Principal().equals(xcrl.getIssuerX500Principal())) {
      return false;
    }
    return true;
  }

  @Override
  public Object clone() {
    try {
      X509CRLSelector copy = (X509CRLSelector) super.clone();
      if (issuerNames != null) {
        copy.issuerNames = new HashSet<X500Principal>(issuerNames);
      }
      return copy;
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e.toString(), e);
    }
  }
}
