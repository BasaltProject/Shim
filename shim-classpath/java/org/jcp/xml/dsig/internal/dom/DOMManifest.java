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
import javax.xml.crypto.dsig.Manifest;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLSignature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMManifest implements Manifest {
  private final List<Reference> references;
  private final String id;

  @SuppressWarnings("rawtypes")
  public DOMManifest(List references, String id) {
    if (references == null) {
      throw new NullPointerException("references cannot be null");
    }
    this.references = Collections.unmodifiableList(new ArrayList<Reference>(references));
    if (this.references.isEmpty()) {
      throw new IllegalArgumentException("list of references must contain at least one entry");
    }
    for (int i = 0, size = this.references.size(); i < size; i++) {
      if (!(this.references.get(i) instanceof Reference)) {
        throw new ClassCastException("references[" + i + "] is not a valid type");
      }
    }
    this.id = id;
  }

  public DOMManifest(Element manElem) throws MarshalException {
    this.id = DOMUtils.getAttributeValue(manElem, "Id");
    if (id != null) {
      manElem.setIdAttributeNS(null, "Id", true);
    }
    List<Reference> refs = new ArrayList<Reference>();
    for (Element e : DOMUtils.children(manElem)) {
      refs.add(new DOMReference(e, manElem.getOwnerDocument()));
    }
    if (refs.isEmpty()) {
      throw new MarshalException("A Manifest must contain at least one Reference");
    }
    this.references = Collections.unmodifiableList(refs);
  }

  public String getId() {
    return id;
  }

  public List<Reference> getReferences() {
    return references;
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
    Document ownerDoc = DOMUtils.getOwnerDocument(parent);
    Element manElem = DOMUtils.createElement(ownerDoc, "Manifest", XMLSignature.XMLNS, dsPrefix);
    DOMUtils.setAttributeID(manElem, "Id", id);
    for (Reference ref : references) {
      ((DOMReference) ref).marshal(manElem, dsPrefix, context);
    }
    parent.appendChild(manElem);
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Manifest)) {
      return false;
    }
    Manifest oman = (Manifest) o;
    boolean idsEqual = id == null ? oman.getId() == null : id.equals(oman.getId());
    return idsEqual && references.equals(oman.getReferences());
  }

  public int hashCode() {
    int result = 17;
    if (id != null) {
      result = 31 * result + id.hashCode();
    }
    result = 31 * result + references.hashCode();
    return result;
  }
}
