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
import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dom.DOMURIReference;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMRetrievalMethod implements RetrievalMethod, DOMURIReference {
  private final List<Transform> transforms;
  private final String uri;
  private final String type;
  private Node here;

  @SuppressWarnings("rawtypes")
  public DOMRetrievalMethod(String uri, String type, List transforms) {
    if (uri == null) {
      throw new NullPointerException("uri cannot be null");
    }
    if (transforms == null || transforms.isEmpty()) {
      this.transforms = Collections.emptyList();
    } else {
      this.transforms = Collections.unmodifiableList(new ArrayList<Transform>(transforms));
      for (int i = 0, size = this.transforms.size(); i < size; i++) {
        if (!(this.transforms.get(i) instanceof Transform)) {
          throw new ClassCastException("transforms[" + i + "] is not a valid type");
        }
      }
    }
    this.uri = uri;
    this.type = type;
  }

  public DOMRetrievalMethod(Element rmElem) throws MarshalException {
    uri = DOMUtils.getAttributeValue(rmElem, "URI");
    type = DOMUtils.getAttributeValue(rmElem, "Type");
    List<Transform> list = new ArrayList<Transform>();
    Element tsElem = DOMUtils.getFirstChildElement(rmElem);
    if (tsElem != null) {
      for (Element t : DOMUtils.children(tsElem)) {
        list.add(new DOMTransform(t));
      }
    }
    transforms = Collections.unmodifiableList(list);
    Node a = rmElem.getAttributeNodeNS(null, "URI");
    here = a != null ? a : rmElem;
  }

  public String getURI() {
    return uri;
  }

  public String getType() {
    return type;
  }

  public List<Transform> getTransforms() {
    return transforms;
  }

  public Node getHere() {
    return here;
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  public Data dereference(XMLCryptoContext context) throws URIReferenceException {
    if (context == null) {
      throw new NullPointerException("context cannot be null");
    }
    URIDereferencer deref = context.getURIDereferencer();
    if (deref == null) {
      deref = DOMURIDereferencer.INSTANCE;
    }
    Data data = deref.dereference(this, context);
    try {
      for (Transform t : transforms) {
        data = t.transform(data, context);
      }
    } catch (TransformException e) {
      throw new URIReferenceException(e);
    }
    return data;
  }

  public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
    Document ownerDoc = DOMUtils.getOwnerDocument(parent);
    Element rmElem = DOMUtils.createElement(ownerDoc, "RetrievalMethod", XMLSignature.XMLNS, dsPrefix);
    DOMUtils.setAttribute(rmElem, "URI", uri);
    DOMUtils.setAttribute(rmElem, "Type", type);
    if (!transforms.isEmpty()) {
      Element tsElem = DOMUtils.createElement(ownerDoc, "Transforms", XMLSignature.XMLNS, dsPrefix);
      rmElem.appendChild(tsElem);
      for (Transform t : transforms) {
        ((DOMTransform) t).marshal(tsElem, dsPrefix, context);
      }
    }
    parent.appendChild(rmElem);
    Node a = rmElem.getAttributeNodeNS(null, "URI");
    here = a != null ? a : rmElem;
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RetrievalMethod)) {
      return false;
    }
    RetrievalMethod orm = (RetrievalMethod) obj;
    boolean typesEqual = type == null ? orm.getType() == null : type.equals(orm.getType());
    return uri.equals(orm.getURI()) && transforms.equals(orm.getTransforms()) && typesEqual;
  }

  public int hashCode() {
    return 31 * uri.hashCode() + transforms.hashCode() + (type == null ? 0 : type.hashCode());
  }
}
