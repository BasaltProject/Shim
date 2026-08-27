/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.math.BigInteger;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMX509IssuerSerial implements X509IssuerSerial {
  private final String issuerName;
  private final BigInteger serialNumber;

  public DOMX509IssuerSerial(String issuerName, BigInteger serialNumber) {
    if (issuerName == null) {
      throw new NullPointerException("issuerName cannot be null");
    }
    if (serialNumber == null) {
      throw new NullPointerException("serialNumber cannot be null");
    }
    new javax.security.auth.x500.X500Principal(issuerName);
    this.issuerName = issuerName;
    this.serialNumber = serialNumber;
  }

  public DOMX509IssuerSerial(Element isElem) throws MarshalException {
    Element iNElem = DOMUtils.getFirstChildElement(isElem);
    Element sNElem = DOMUtils.getNextSiblingElement(iNElem);
    issuerName = DOMUtils.textOf(iNElem);
    serialNumber = new BigInteger(DOMUtils.textOf(sNElem).trim());
  }

  public String getIssuerName() {
    return issuerName;
  }

  public BigInteger getSerialNumber() {
    return serialNumber;
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
    Document ownerDoc = DOMUtils.getOwnerDocument(parent);
    Element isElem = DOMUtils.createElement(ownerDoc, "X509IssuerSerial", XMLSignature.XMLNS, dsPrefix);
    isElem.appendChild(DOMUtils.textElement(ownerDoc, "X509IssuerName", dsPrefix, issuerName));
    isElem.appendChild(DOMUtils.textElement(ownerDoc, "X509SerialNumber", dsPrefix, serialNumber.toString()));
    parent.appendChild(isElem);
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof X509IssuerSerial)) {
      return false;
    }
    X509IssuerSerial ois = (X509IssuerSerial) obj;
    return issuerName.equals(ois.getIssuerName()) && serialNumber.equals(ois.getSerialNumber());
  }

  public int hashCode() {
    return 31 * issuerName.hashCode() + serialNumber.hashCode();
  }
}
