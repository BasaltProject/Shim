/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyName;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.PGPData;
import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMKeyInfo implements KeyInfo {
  private final String id;
  private final List<XMLStructure> keyInfoTypes;

  @SuppressWarnings("rawtypes")
  public DOMKeyInfo(List content, String id) {
    if (content == null) {
      throw new NullPointerException("content cannot be null");
    }
    List<XMLStructure> typesCopy = new ArrayList<XMLStructure>(content);
    if (typesCopy.isEmpty()) {
      throw new IllegalArgumentException("content cannot be empty");
    }
    for (int i = 0, size = typesCopy.size(); i < size; i++) {
      if (!(typesCopy.get(i) instanceof XMLStructure)) {
        throw new ClassCastException("content[" + i + "] is not a valid KeyInfo type");
      }
    }
    this.keyInfoTypes = Collections.unmodifiableList(typesCopy);
    this.id = id;
  }

  public DOMKeyInfo(Element kiElem) throws MarshalException {
    this.id = DOMUtils.getAttributeValue(kiElem, "Id");
    if (id != null) {
      kiElem.setIdAttributeNS(null, "Id", true);
    }
    List<XMLStructure> content = new ArrayList<XMLStructure>();
    for (Element e : DOMUtils.children(kiElem)) {
      String n = e.getLocalName();
      boolean dsig = XMLSignature.XMLNS.equals(e.getNamespaceURI());
      if (dsig && "KeyName".equals(n)) {
        content.add(new DOMKeyName(e));
      } else if (dsig && "KeyValue".equals(n)) {
        content.add(new DOMKeyValue(e));
      } else if (dsig && "RetrievalMethod".equals(n)) {
        content.add(new DOMRetrievalMethod(e));
      } else if (dsig && "X509Data".equals(n)) {
        content.add(new DOMX509Data(e));
      } else if (dsig && "PGPData".equals(n)) {
        content.add(new DOMPGPData(e));
      } else {
        content.add(new DOMStructure(e));
      }
    }
    if (content.isEmpty()) {
      throw new MarshalException("KeyInfo must contain at least one type");
    }
    this.keyInfoTypes = Collections.unmodifiableList(content);
  }

  public String getId() {
    return id;
  }

  public List<XMLStructure> getContent() {
    return keyInfoTypes;
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  public void marshal(XMLStructure parent, XMLCryptoContext context) throws MarshalException {
    if (parent == null) {
      throw new NullPointerException("parent is null");
    }
    if (!(parent instanceof DOMStructure)) {
      throw new ClassCastException("parent must be of type DOMStructure");
    }
    Node pNode = ((DOMStructure) parent).getNode();
    String dsPrefix = DOMUtils.getSignaturePrefix(context);
    marshal(pNode, null, dsPrefix, (DOMCryptoContext) context);
  }

  public void marshal(Node parent, Node nextSibling, String dsPrefix, DOMCryptoContext context)
      throws MarshalException {
    Document ownerDoc = DOMUtils.getOwnerDocument(parent);
    Element kiElem = DOMUtils.createElement(ownerDoc, "KeyInfo", XMLSignature.XMLNS, dsPrefix);
    if (dsPrefix == null || dsPrefix.length() == 0) {
      kiElem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", XMLSignature.XMLNS);
    } else {
      kiElem.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + dsPrefix, XMLSignature.XMLNS);
    }
    DOMUtils.setAttributeID(kiElem, "Id", id);
    for (XMLStructure kiType : keyInfoTypes) {
      if (kiType instanceof DOMKeyName) {
        ((DOMKeyName) kiType).marshal(kiElem, dsPrefix, context);
      } else if (kiType instanceof DOMKeyValue) {
        ((DOMKeyValue) kiType).marshal(kiElem, dsPrefix, context);
      } else if (kiType instanceof DOMRetrievalMethod) {
        ((DOMRetrievalMethod) kiType).marshal(kiElem, dsPrefix, context);
      } else if (kiType instanceof DOMX509Data) {
        ((DOMX509Data) kiType).marshal(kiElem, dsPrefix, context);
      } else if (kiType instanceof DOMX509IssuerSerial) {
        ((DOMX509IssuerSerial) kiType).marshal(kiElem, dsPrefix, context);
      } else if (kiType instanceof DOMPGPData) {
        ((DOMPGPData) kiType).marshal(kiElem, dsPrefix, context);
      } else if (kiType instanceof DOMStructure) {
        kiElem.appendChild(ownerDoc.importNode(((DOMStructure) kiType).getNode(), true));
      } else {
        throw new MarshalException("unsupported KeyInfo type");
      }
    }
    parent.insertBefore(kiElem, nextSibling);
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof KeyInfo)) {
      return false;
    }
    KeyInfo oki = (KeyInfo) o;
    boolean idsEqual = id == null ? oki.getId() == null : id.equals(oki.getId());
    return keyInfoTypes.equals(oki.getContent()) && idsEqual;
  }

  public int hashCode() {
    int result = 17;
    if (id != null) {
      result = 31 * result + id.hashCode();
    }
    result = 31 * result + keyInfoTypes.hashCode();
    return result;
  }
}
