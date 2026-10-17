/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.ElementTraversal;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

public class ElementImpl extends ParentNode implements Element, ElementTraversal, TypeInfo {
  private static final long serialVersionUID = 3717253516652722278L;

  String name;
  String namespaceUri;
  String localName;
  AttributeMap attributes;

  ElementImpl(DocumentImpl ownerDocument, String name) {
    super(ownerDocument);
    this.name = name;
  }

  ElementImpl(DocumentImpl ownerDocument, String namespaceUri, String qualifiedName, String localName) {
    super(ownerDocument);
    this.name = qualifiedName;
    this.namespaceUri = namespaceUri;
    this.localName = localName;
  }

  public short getNodeType() {
    return Node.ELEMENT_NODE;
  }

  public String getNodeName() {
    return name;
  }

  public String getTagName() {
    return name;
  }

  public String getNamespaceURI() {
    return namespaceUri;
  }

  public String getLocalName() {
    return localName;
  }

  public String getPrefix() {
    if (localName == null) {
      return null;
    }
    int colon = name.indexOf(':');
    return colon < 0 ? null : name.substring(0, colon);
  }

  public void setPrefix(String prefix) throws DOMException {
    if (ownerDocument.errorChecking) {
      if (readOnly) {
        throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
      }
      if (prefix != null && prefix.length() != 0) {
        if (!DomNames.isName(prefix)) {
          throw DomMessages.error(DOMException.INVALID_CHARACTER_ERR);
        }
        if (namespaceUri == null || prefix.indexOf(':') >= 0) {
          throw DomMessages.error(DOMException.NAMESPACE_ERR);
        } else if (prefix.equals("xml")) {
          if (!namespaceUri.equals(DomNames.kXmlUri)) {
            throw DomMessages.error(DOMException.NAMESPACE_ERR);
          }
        }
      }
    }
    if (localName == null) {
      return;
    }
    name = prefix != null && prefix.length() != 0 ? prefix + ":" + localName : localName;
    changed();
  }

  public NamedNodeMap getAttributes() {
    if (attributes == null) {
      attributes = new AttributeMap(this);
    }
    return attributes;
  }

  public boolean hasAttributes() {
    return attributes != null && attributes.getLength() != 0;
  }

  public Node cloneNode(boolean deep) {
    ElementImpl copy = (ElementImpl) super.cloneNode(deep);
    copy.attributes = null;
    if (attributes != null) {
      AttributeMap map = new AttributeMap(copy);
      for (NodeImpl a : attributes.nodes) {
        AttrImpl clone = (AttrImpl) a.cloneNode(true);
        clone.specified = ((AttrImpl) a).specified;
        clone.idAttribute = ((AttrImpl) a).idAttribute;
        clone.dtdType = ((AttrImpl) a).dtdType;
        clone.ownerElement = copy;
        clone.ownerDocument = ownerDocument;
        map.nodes.add(clone);
      }
      copy.attributes = map;
    }
    return copy;
  }

  public String getAttribute(String name) {
    if (attributes == null) {
      return "";
    }
    Attr attr = (Attr) attributes.getNamedItem(name);
    return attr == null ? "" : attr.getValue();
  }

  public Attr getAttributeNode(String name) {
    if (attributes == null) {
      return null;
    }
    return (Attr) attributes.getNamedItem(name);
  }

  public void setAttribute(String name, String value) {
    if (ownerDocument.errorChecking && readOnly) {
      throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    Attr newAttr = getAttributeNode(name);
    if (newAttr == null) {
      newAttr = ownerDocument.createAttribute(name);
      getAttributes();
      attributes.setNamedItem(newAttr);
    }
    newAttr.setValue(value);
  }

  public void removeAttribute(String name) {
    if (ownerDocument.errorChecking && readOnly) {
      throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    if (attributes == null) {
      return;
    }
    if (attributes.getNamedItem(name) != null) {
      attributes.removeNamedItem(name);
    }
  }

  public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
    if (ownerDocument.errorChecking && readOnly) {
      throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    if (attributes == null || attributes.nodes.indexOf(oldAttr) < 0) {
      throw DomMessages.error(DOMException.NOT_FOUND_ERR);
    }
    return (Attr) attributes.removeNamedItem(oldAttr.getName());
  }

  public Attr setAttributeNode(Attr newAttr) throws DOMException {
    if (ownerDocument.errorChecking) {
      if (readOnly) {
        throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
      }
      if (newAttr.getOwnerDocument() != ownerDocument) {
        throw DomMessages.error(DOMException.WRONG_DOCUMENT_ERR);
      }
    }
    getAttributes();
    return (Attr) attributes.setNamedItem(newAttr);
  }

  public String getAttributeNS(String namespaceURI, String localName) {
    if (attributes == null) {
      return "";
    }
    Attr attr = (Attr) attributes.getNamedItemNS(namespaceURI, localName);
    return attr == null ? "" : attr.getValue();
  }

  public void setAttributeNS(String namespaceURI, String qualifiedName, String value) {
    if (ownerDocument.errorChecking && readOnly) {
      throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    String[] parts = DomNames.split(qualifiedName);
    String ns = namespaceURI;
    AttrImpl newAttr = (AttrImpl) getAttributeNodeNS(ns, parts[1]);
    if (newAttr == null) {
      newAttr = (AttrImpl) ownerDocument.createAttributeNS(namespaceURI, qualifiedName);
      getAttributes();
      attributes.setNamedItemNS(newAttr);
    } else {
      if (ownerDocument.errorChecking) {
        DomNames.checkAttribute(ns, qualifiedName);
      }
      int oldPos = attributes.nodes.indexOf(newAttr);
      attributes.nodes.remove(oldPos);
      newAttr.name = qualifiedName;
      int pos = attributes.findName(qualifiedName);
      attributes.nodes.add(pos >= 0 ? pos : -(pos + 1), newAttr);
    }
    newAttr.setValue(value);
  }

  public void removeAttributeNS(String namespaceURI, String localName) {
    if (ownerDocument.errorChecking && readOnly) {
      throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    if (attributes == null) {
      return;
    }
    if (attributes.getNamedItemNS(namespaceURI, localName) != null) {
      attributes.removeNamedItemNS(namespaceURI, localName);
    }
  }

  public Attr getAttributeNodeNS(String namespaceURI, String localName) {
    if (attributes == null) {
      return null;
    }
    return (Attr) attributes.getNamedItemNS(namespaceURI, localName);
  }

  public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
    if (ownerDocument.errorChecking) {
      if (readOnly) {
        throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
      }
      if (newAttr.getOwnerDocument() != ownerDocument) {
        throw DomMessages.error(DOMException.WRONG_DOCUMENT_ERR);
      }
    }
    getAttributes();
    return (Attr) attributes.setNamedItemNS(newAttr);
  }

  public boolean hasAttribute(String name) {
    return getAttributeNode(name) != null;
  }

  public boolean hasAttributeNS(String namespaceURI, String localName) {
    return getAttributeNodeNS(namespaceURI, localName) != null;
  }

  public NodeList getElementsByTagName(String tagname) {
    return new DeepNodeList(this, tagname);
  }

  public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
    return new DeepNodeList(this, namespaceURI, localName);
  }

  public void setIdAttribute(String name, boolean makeId) {
    if (ownerDocument.errorChecking && readOnly) {
      throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    AttrImpl attr = (AttrImpl) getAttributeNode(name);
    if (attr == null) {
      throw DomMessages.error(DOMException.NOT_FOUND_ERR);
    }
    attr.idAttribute = makeId;
    changed();
  }

  public void setIdAttributeNS(String namespaceURI, String localName, boolean makeId) {
    if (ownerDocument.errorChecking && readOnly) {
      throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    AttrImpl attr = (AttrImpl) getAttributeNodeNS(namespaceURI, localName);
    if (attr == null) {
      throw DomMessages.error(DOMException.NOT_FOUND_ERR);
    }
    attr.idAttribute = makeId;
    changed();
  }

  public void setIdAttributeNode(Attr at, boolean makeId) {
    if (ownerDocument.errorChecking && readOnly) {
      throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    if (attributes == null || attributes.nodes.indexOf(at) < 0) {
      throw DomMessages.error(DOMException.NOT_FOUND_ERR);
    }
    ((AttrImpl) at).idAttribute = makeId;
    changed();
  }

  public TypeInfo getSchemaTypeInfo() {
    return this;
  }

  public String getTypeName() {
    return null;
  }

  public String getTypeNamespace() {
    return null;
  }

  public boolean isDerivedFrom(String typeNamespaceArg, String typeNameArg, int derivationMethod) {
    return false;
  }

  public String getBaseURI() {
    if (attributes != null) {
      Attr base = getAttributeNodeNS("http://www.w3.org/XML/1998/namespace", "base");
      if (base == null) {
        base = getAttributeNode("xml:base");
      }
      if (base != null) {
        String uri = base.getNodeValue();
        if (uri != null && uri.length() != 0) {
          try {
            java.net.URI baseUri = new java.net.URI(uri);
            if (baseUri.isAbsolute()) {
              return uri;
            }
            String parentBase = parentNode != null ? parentNode.getBaseURI() : ownerDocument.getDocumentURI();
            if (parentBase != null) {
              return new java.net.URI(parentBase).resolve(baseUri).toString();
            }
            return null;
          } catch (java.net.URISyntaxException e) {
            return null;
          }
        }
      }
    }
    String parentBase = parentNode != null ? parentNode.getBaseURI() : null;
    return parentBase != null ? parentBase : ownerDocument.getBaseURI();
  }

  public Element getFirstElementChild() {
    for (NodeImpl n = firstChildNode; n != null; n = n.nextSibling) {
      if (n.getNodeType() == ELEMENT_NODE) {
        return (Element) n;
      }
    }
    return null;
  }

  public Element getLastElementChild() {
    for (NodeImpl n = lastChildNode; n != null; n = n.previousSibling) {
      if (n.getNodeType() == ELEMENT_NODE) {
        return (Element) n;
      }
    }
    return null;
  }

  public Element getNextElementSibling() {
    for (NodeImpl n = nextSibling; n != null; n = n.nextSibling) {
      if (n.getNodeType() == ELEMENT_NODE) {
        return (Element) n;
      }
    }
    return null;
  }

  public Element getPreviousElementSibling() {
    for (NodeImpl n = previousSibling; n != null; n = n.previousSibling) {
      if (n.getNodeType() == ELEMENT_NODE) {
        return (Element) n;
      }
    }
    return null;
  }

  public int getChildElementCount() {
    int count = 0;
    for (NodeImpl n = firstChildNode; n != null; n = n.nextSibling) {
      if (n.getNodeType() == ELEMENT_NODE) {
        count++;
      }
    }
    return count;
  }

  public void normalize() {
    super.normalize();
  }

  void setReadOnlyRecursive(boolean value) {
    readOnly = value;
  }
}
