/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DocumentTypeImpl extends ParentNode implements DocumentType {
  private static final long serialVersionUID = 7751299192316526485L;

  final String name;
  String publicId;
  String systemId;
  String internalSubset;
  NamedNodeMapImpl entities;
  NamedNodeMapImpl notations;

  DocumentTypeImpl(DocumentImpl ownerDocument, String name) {
    super(ownerDocument);
    this.name = name;
    entities = new NamedNodeMapImpl(this);
    notations = new NamedNodeMapImpl(this);
    entities.readOnlyMap = true;
    notations.readOnlyMap = true;
    readOnly = true;
  }

  public void setNodeValue(String value) {
  }

  public short getNodeType() {
    return Node.DOCUMENT_TYPE_NODE;
  }

  public String getNodeName() {
    return name;
  }

  public String getName() {
    return name;
  }

  public NamedNodeMap getEntities() {
    return entities;
  }

  public NamedNodeMap getNotations() {
    return notations;
  }

  public String getPublicId() {
    return publicId;
  }

  public String getSystemId() {
    return systemId;
  }

  public String getInternalSubset() {
    return internalSubset;
  }

  public void setInternalSubset(String subset) {
    internalSubset = subset;
  }

  public String getTextContent() {
    return null;
  }

  public void setTextContent(String textContent) {
  }

  public Node cloneNode(boolean deep) {
    DocumentTypeImpl copy = (DocumentTypeImpl) super.cloneNode(false);
    copy.entities = new NamedNodeMapImpl(copy);
    copy.notations = new NamedNodeMapImpl(copy);
    copy.entities.readOnlyMap = true;
    copy.notations.readOnlyMap = true;
    for (NodeImpl e : entities.nodes) {
      NodeImpl c = (NodeImpl) e.cloneNode(true);
      c.ownerDocument = ownerDocument;
      copy.entities.nodes.add(c);
    }
    for (NodeImpl n : notations.nodes) {
      NodeImpl c = (NodeImpl) n.cloneNode(true);
      c.ownerDocument = ownerDocument;
      copy.notations.nodes.add(c);
    }
    return copy;
  }

  void setOwnerDocument(DocumentImpl doc) {
    ownerDocument = doc;
    for (NodeImpl e : entities.nodes) {
      e.ownerDocument = doc;
    }
    for (NodeImpl n : notations.nodes) {
      n.ownerDocument = doc;
    }
  }

  public boolean isEqualNode(Node arg) {
    if (!super.isEqualNode(arg)) {
      return false;
    }
    DocumentType other = (DocumentType) arg;
    return equalMaps(entities, (NamedNodeMapImpl) other.getEntities())
        && equalMaps(notations, (NamedNodeMapImpl) other.getNotations());
  }

  private static boolean equalMaps(NamedNodeMapImpl a, NamedNodeMapImpl b) {
    if (a.getLength() != b.getLength()) {
      return false;
    }
    for (int i = 0; i < a.getLength(); i++) {
      Node n1 = a.item(i);
      Node n2 = b.getNamedItem(n1.getNodeName());
      if (n2 == null || !n1.isEqualNode(n2)) {
        return false;
      }
    }
    return true;
  }
}
