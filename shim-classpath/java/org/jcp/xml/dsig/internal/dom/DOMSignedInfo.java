/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.XMLSignature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMSignedInfo implements SignedInfo {
  private final List<Reference> references;
  private final CanonicalizationMethod canonicalizationMethod;
  private final SignatureMethod signatureMethod;
  private final String id;
  private Element siElem;
  private byte[] canonData;

  @SuppressWarnings("rawtypes")
  public DOMSignedInfo(CanonicalizationMethod cm, SignatureMethod sm, List references, String id) {
    if (cm == null || sm == null || references == null) {
      throw new NullPointerException();
    }
    this.canonicalizationMethod = cm;
    this.signatureMethod = sm;
    this.references = new ArrayList<Reference>(references);
    if (this.references.isEmpty()) {
      throw new IllegalArgumentException("list of references must contain at least one entry");
    }
    for (int i = 0, size = this.references.size(); i < size; i++) {
      Object obj = this.references.get(i);
      if (!(obj instanceof Reference)) {
        throw new ClassCastException("list of references contains an illegal type");
      }
    }
    this.id = id;
  }

  public DOMSignedInfo(Element siElem, Document ownerDoc) throws MarshalException {
    this.siElem = siElem;
    this.id = DOMUtils.getAttributeValue(siElem, "Id");
    if (id != null) {
      siElem.setIdAttributeNS(null, "Id", true);
    }
    Element cmElem = DOMUtils.getFirstChildElement(siElem);
    if (cmElem == null || !"CanonicalizationMethod".equals(cmElem.getLocalName())) {
      throw new MarshalException("Invalid element name: " + (cmElem == null ? "null" : cmElem.getLocalName())
          + ", expected CanonicalizationMethod");
    }
    this.canonicalizationMethod = new DOMCanonicalizationMethod(cmElem);
    Element smElem = DOMUtils.getNextSiblingElement(cmElem);
    if (smElem == null || !"SignatureMethod".equals(smElem.getLocalName())) {
      throw new MarshalException("Invalid element name: " + (smElem == null ? "null" : smElem.getLocalName())
          + ", expected SignatureMethod");
    }
    this.signatureMethod = new DOMSignatureMethod(smElem);
    List<Reference> refs = new ArrayList<Reference>(5);
    Element refElem = DOMUtils.getNextSiblingElement(smElem);
    while (refElem != null) {
      refs.add(new DOMReference(refElem, ownerDoc));
      refElem = DOMUtils.getNextSiblingElement(refElem);
    }
    if (refs.isEmpty()) {
      throw new MarshalException("SignedInfo must contain at least one Reference");
    }
    if (DOMUtils.secureValidation() && refs.size() > 30) {
      throw new MarshalException("A maximum of 30 references per Manifest are allowed when secure validation is "
          + "enabled");
    }
    this.references = Collections.unmodifiableList(refs);
  }

  public CanonicalizationMethod getCanonicalizationMethod() {
    return canonicalizationMethod;
  }

  public SignatureMethod getSignatureMethod() {
    return signatureMethod;
  }

  public String getId() {
    return id;
  }

  public List<Reference> getReferences() {
    return Collections.unmodifiableList(references);
  }

  public InputStream getCanonicalizedData() {
    return canonData == null ? null : new ByteArrayInputStream(canonData);
  }

  InputStream canonicalized(DOMCryptoContext context) throws IOException, javax.xml.crypto.dsig.XMLSignatureException {
    Data data = new DOMSubTreeData(siElem, true);
    try {
      Data out = ((DOMTransform) canonicalizationMethod).transform(data, context);
      canonData = DOMTransformAccess.read(((OctetStreamData) out).getOctetStream());
    } catch (TransformException e) {
      throw new javax.xml.crypto.dsig.XMLSignatureException(e);
    }
    return new ByteArrayInputStream(canonData);
  }

  public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
    Document ownerDoc = DOMUtils.getOwnerDocument(parent);
    siElem = DOMUtils.createElement(ownerDoc, "SignedInfo", XMLSignature.XMLNS, dsPrefix);
    ((DOMTransform) canonicalizationMethod).marshal(siElem, dsPrefix, context);
    ((DOMSignatureMethod) signatureMethod).marshal(siElem, dsPrefix, context);
    for (Reference ref : references) {
      ((DOMReference) ref).marshal(siElem, dsPrefix, context);
    }
    DOMUtils.setAttributeID(siElem, "Id", id);
    parent.appendChild(siElem);
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SignedInfo)) {
      return false;
    }
    SignedInfo osi = (SignedInfo) o;
    boolean idEqual = id == null ? osi.getId() == null : id.equals(osi.getId());
    return canonicalizationMethod.equals(osi.getCanonicalizationMethod())
        && signatureMethod.equals(osi.getSignatureMethod()) && references.equals(osi.getReferences()) && idEqual;
  }

  public int hashCode() {
    int result = 17;
    if (id != null) {
      result = 31 * result + id.hashCode();
    }
    result = 31 * result + canonicalizationMethod.hashCode();
    result = 31 * result + signatureMethod.hashCode();
    result = 31 * result + references.hashCode();
    return result;
  }
}
