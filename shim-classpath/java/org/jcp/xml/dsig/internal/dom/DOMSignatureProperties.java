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
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.SignatureProperties;
import javax.xml.crypto.dsig.SignatureProperty;
import javax.xml.crypto.dsig.XMLSignature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMSignatureProperties implements SignatureProperties {
  private final String id;
  private final List<SignatureProperty> properties;

  @SuppressWarnings("rawtypes")
  public DOMSignatureProperties(List properties, String id) {
    if (properties == null) {
      throw new NullPointerException("properties cannot be null");
    } else if (properties.isEmpty()) {
      throw new IllegalArgumentException("properties cannot be empty");
    } else {
      this.properties = Collections.unmodifiableList(new ArrayList<SignatureProperty>(properties));
      for (int i = 0, size = this.properties.size(); i < size; i++) {
        if (!(this.properties.get(i) instanceof SignatureProperty)) {
          throw new ClassCastException("properties[" + i + "] is not a valid type");
        }
      }
    }
    this.id = id;
  }

  public DOMSignatureProperties(Element propsElem) throws MarshalException {
    this.id = DOMUtils.getAttributeValue(propsElem, "Id");
    if (id != null) {
      propsElem.setIdAttributeNS(null, "Id", true);
    }
    List<SignatureProperty> newProperties = new ArrayList<SignatureProperty>();
    for (Element e : DOMUtils.children(propsElem)) {
      newProperties.add(new DOMSignatureProperty(e));
    }
    if (newProperties.isEmpty()) {
      throw new MarshalException("properties cannot be empty");
    }
    this.properties = Collections.unmodifiableList(newProperties);
  }

  public List<SignatureProperty> getProperties() {
    return properties;
  }

  public String getId() {
    return id;
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
    Document ownerDoc = DOMUtils.getOwnerDocument(parent);
    Element propsElem = DOMUtils.createElement(ownerDoc, "SignatureProperties", XMLSignature.XMLNS, dsPrefix);
    DOMUtils.setAttributeID(propsElem, "Id", id);
    for (SignatureProperty property : properties) {
      ((DOMSignatureProperty) property).marshal(propsElem, dsPrefix, context);
    }
    parent.appendChild(propsElem);
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SignatureProperties)) {
      return false;
    }
    SignatureProperties osp = (SignatureProperties) o;
    boolean idsEqual = id == null ? osp.getId() == null : id.equals(osp.getId());
    return properties.equals(osp.getProperties()) && idsEqual;
  }

  public int hashCode() {
    int result = 17;
    if (id != null) {
      result = 31 * result + id.hashCode();
    }
    result = 31 * result + properties.hashCode();
    return result;
  }
}
