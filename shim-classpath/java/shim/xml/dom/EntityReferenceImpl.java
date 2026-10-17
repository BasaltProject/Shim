/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;

public class EntityReferenceImpl extends ParentNode implements EntityReference {
  private static final long serialVersionUID = -7837873708906262756L;

  private final String name;
  String baseUri;

  EntityReferenceImpl(DocumentImpl ownerDocument, String name) {
    super(ownerDocument);
    this.name = name;
  }

  public short getNodeType() {
    return Node.ENTITY_REFERENCE_NODE;
  }

  public String getNodeName() {
    return name;
  }

  public Node cloneNode(boolean deep) {
    EntityReferenceImpl copy = (EntityReferenceImpl) super.cloneNode(deep);
    copy.setReadOnlyRecursive(false);
    return copy;
  }

  public String getBaseURI() {
    if (baseUri != null) {
      return baseUri;
    }
    org.w3c.dom.DocumentType doctype = ownerDocument.getDoctype();
    if (doctype != null) {
      org.w3c.dom.NamedNodeMap entities = doctype.getEntities();
      EntityImpl entity = entities == null ? null : (EntityImpl) entities.getNamedItem(name);
      if (entity != null) {
        return entity.getBaseURI();
      }
    }
    return baseUriOfAncestors();
  }

  public void setBaseURI(String uri) {
    baseUri = uri;
  }

  void setReadOnlyRecursive(boolean value) {
    readOnly = value;
    for (NodeImpl n = firstChildNode; n != null; n = n.nextSibling) {
      n.setReadOnlyRecursive(value);
    }
  }
}
