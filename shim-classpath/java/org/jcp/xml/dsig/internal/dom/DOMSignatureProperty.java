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
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.SignatureProperty;
import javax.xml.crypto.dsig.XMLSignature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMSignatureProperty implements SignatureProperty {
  private final String id;
  private final String target;
  private final List<XMLStructure> content;

  @SuppressWarnings("rawtypes")
  public DOMSignatureProperty(List content, String target, String id) {
    if (target == null) {
      throw new NullPointerException("target cannot be null");
    } else if (content == null) {
      throw new NullPointerException("content cannot be null");
    } else if (content.isEmpty()) {
      throw new IllegalArgumentException("content cannot be empty");
    } else {
      this.content = Collections.unmodifiableList(new ArrayList<XMLStructure>(content));
      for (int i = 0, size = this.content.size(); i < size; i++) {
        if (!(this.content.get(i) instanceof XMLStructure)) {
          throw new ClassCastException("content[" + i + "] is not a valid type");
        }
      }
    }
    this.target = target;
    this.id = id;
  }

  public DOMSignatureProperty(Element propElem) throws MarshalException {
    this.target = DOMUtils.getAttributeValue(propElem, "Target");
    if (target == null) {
      throw new MarshalException("target cannot be null");
    }
    this.id = DOMUtils.getAttributeValue(propElem, "Id");
    if (id != null) {
      propElem.setIdAttributeNS(null, "Id", true);
    }
    List<XMLStructure> newContent = new ArrayList<XMLStructure>();
    for (Node n = propElem.getFirstChild(); n != null; n = n.getNextSibling()) {
      newContent.add(new DOMStructure(n));
    }
    if (newContent.isEmpty()) {
      throw new MarshalException("content cannot be empty");
    }
    this.content = Collections.unmodifiableList(newContent);
  }

  public List<XMLStructure> getContent() {
    return content;
  }

  public String getId() {
    return id;
  }

  public String getTarget() {
    return target;
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
    Document ownerDoc = DOMUtils.getOwnerDocument(parent);
    Element propElem = DOMUtils.createElement(ownerDoc, "SignatureProperty", XMLSignature.XMLNS, dsPrefix);
    DOMUtils.setAttributeID(propElem, "Id", id);
    DOMUtils.setAttribute(propElem, "Target", target);
    for (XMLStructure property : content) {
      propElem.appendChild(ownerDoc.importNode(((DOMStructure) property).getNode(), true));
    }
    parent.appendChild(propElem);
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SignatureProperty)) {
      return false;
    }
    SignatureProperty osp = (SignatureProperty) o;
    boolean idsEqual = id == null ? osp.getId() == null : id.equals(osp.getId());
    return target.equals(osp.getTarget()) && idsEqual && content.size() == osp.getContent().size();
  }

  public int hashCode() {
    int result = 17;
    if (id != null) {
      result = 31 * result + id.hashCode();
    }
    result = 31 * result + target.hashCode();
    result = 31 * result + content.hashCode();
    return result;
  }
}
