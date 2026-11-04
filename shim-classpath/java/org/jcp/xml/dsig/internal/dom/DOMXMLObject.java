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
import javax.xml.crypto.dsig.Manifest;
import javax.xml.crypto.dsig.SignatureProperties;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMXMLObject implements XMLObject {
  private final String id;
  private final String mimeType;
  private final String encoding;
  private final List<XMLStructure> content;
  private Element objectElem;

  @SuppressWarnings("rawtypes")
  public DOMXMLObject(List content, String id, String mimeType, String encoding) {
    if (content == null || content.isEmpty()) {
      this.content = Collections.emptyList();
    } else {
      List<XMLStructure> copy = new ArrayList<XMLStructure>(content);
      for (int i = 0, size = copy.size(); i < size; i++) {
        if (!(copy.get(i) instanceof XMLStructure)) {
          throw new ClassCastException("content[" + i + "] is not a valid type");
        }
      }
      this.content = Collections.unmodifiableList(copy);
    }
    this.id = id;
    this.mimeType = mimeType;
    this.encoding = encoding;
  }

  public DOMXMLObject(Element objElem) throws MarshalException {
    this.encoding = DOMUtils.getAttributeValue(objElem, "Encoding");
    this.id = DOMUtils.getAttributeValue(objElem, "Id");
    if (id != null) {
      objElem.setIdAttributeNS(null, "Id", true);
    }
    this.mimeType = DOMUtils.getAttributeValue(objElem, "MimeType");
    this.objectElem = objElem;
    List<XMLStructure> newContent = new ArrayList<XMLStructure>();
    for (Node n = objElem.getFirstChild(); n != null; n = n.getNextSibling()) {
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        Element child = (Element) n;
        if (XMLSignature.XMLNS.equals(child.getNamespaceURI())) {
          String tag = child.getLocalName();
          if ("Manifest".equals(tag)) {
            newContent.add(new DOMManifest(child));
            continue;
          } else if ("SignatureProperties".equals(tag)) {
            newContent.add(new DOMSignatureProperties(child));
            continue;
          } else if ("Signature".equals(tag)) {
            newContent.add(new DOMXMLSignature(child));
            continue;
          }
        }
      }
      newContent.add(new DOMStructure(n));
    }
    this.content = Collections.unmodifiableList(newContent);
  }

  public List<XMLStructure> getContent() {
    return content;
  }

  public String getId() {
    return id;
  }

  public String getMimeType() {
    return mimeType;
  }

  public String getEncoding() {
    return encoding;
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
    Document ownerDoc = DOMUtils.getOwnerDocument(parent);
    Element objElem = DOMUtils.createElement(ownerDoc, "Object", XMLSignature.XMLNS, dsPrefix);
    DOMUtils.setAttributeID(objElem, "Id", id);
    DOMUtils.setAttribute(objElem, "MimeType", mimeType);
    DOMUtils.setAttribute(objElem, "Encoding", encoding);
    for (XMLStructure object : content) {
      if (object instanceof DOMStructure) {
        objElem.appendChild(ownerDoc.importNode(((DOMStructure) object).getNode(), true));
      } else if (object instanceof DOMManifest) {
        ((DOMManifest) object).marshal(objElem, dsPrefix, context);
      } else if (object instanceof DOMSignatureProperties) {
        ((DOMSignatureProperties) object).marshal(objElem, dsPrefix, context);
      } else if (object instanceof DOMXMLSignature) {
        ((DOMXMLSignature) object).marshal(objElem, null, dsPrefix, context);
      } else {
        throw new MarshalException("unsupported Object content");
      }
    }
    parent.appendChild(objElem);
    objectElem = objElem;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof XMLObject)) {
      return false;
    }
    XMLObject oxo = (XMLObject) o;
    boolean idsEqual = id == null ? oxo.getId() == null : id.equals(oxo.getId());
    boolean encodingsEqual = encoding == null ? oxo.getEncoding() == null : encoding.equals(oxo.getEncoding());
    boolean mimeTypesEqual = mimeType == null ? oxo.getMimeType() == null : mimeType.equals(oxo.getMimeType());
    return idsEqual && encodingsEqual && mimeTypesEqual && equalsContent(oxo.getContent());
  }

  private boolean equalsContent(List<XMLStructure> otherContent) {
    if (content.size() != otherContent.size()) {
      return false;
    }
    for (int i = 0, osize = otherContent.size(); i < osize; i++) {
      XMLStructure oxs = otherContent.get(i);
      XMLStructure xs = content.get(i);
      if (oxs instanceof DOMStructure) {
        if (!(xs instanceof DOMStructure)) {
          return false;
        }
        if (!((DOMStructure) xs).getNode().isEqualNode(((DOMStructure) oxs).getNode())) {
          return false;
        }
      } else if (!xs.equals(oxs)) {
        return false;
      }
    }
    return true;
  }

  public int hashCode() {
    int result = 17;
    if (id != null) {
      result = 31 * result + id.hashCode();
    }
    if (encoding != null) {
      result = 31 * result + encoding.hashCode();
    }
    if (mimeType != null) {
      result = 31 * result + mimeType.hashCode();
    }
    result = 31 * result + content.hashCode();
    return result;
  }
}
