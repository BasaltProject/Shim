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
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

public class AttrImpl extends ParentNode implements Attr, TypeInfo {
  private static final long serialVersionUID = 7277707688218972102L;

  String name;
  String namespaceUri;
  String localName;
  ElementImpl ownerElement;
  boolean specified = true;
  boolean idAttribute;
  String dtdType;

  AttrImpl(DocumentImpl ownerDocument, String name) {
    super(ownerDocument);
    this.name = name;
  }

  AttrImpl(DocumentImpl ownerDocument, String namespaceUri, String qualifiedName, String localName) {
    super(ownerDocument);
    this.name = qualifiedName;
    this.namespaceUri = namespaceUri;
    this.localName = localName;
  }

  public short getNodeType() {
    return Node.ATTRIBUTE_NODE;
  }

  public String getNodeName() {
    return name;
  }

  public String getName() {
    return name;
  }

  public Node getParentNode() {
    return null;
  }

  public Node getPreviousSibling() {
    return null;
  }

  public Node getNextSibling() {
    return null;
  }

  public Element getOwnerElement() {
    return ownerElement;
  }

  public boolean getSpecified() {
    return specified;
  }

  public String getNodeValue() {
    return getValue();
  }

  public void setNodeValue(String value) throws DOMException {
    setValue(value);
  }

  public String getValue() {
    if (firstChildNode == null) {
      return "";
    }
    if (firstChildNode == lastChildNode) {
      return firstChildNode.getNodeType() == ENTITY_REFERENCE_NODE ? firstChildNode.getTextContent()
          : firstChildNode.getNodeValue();
    }
    StringBuilder sb = new StringBuilder();
    for (NodeImpl n = firstChildNode; n != null; n = n.nextSibling) {
      if (n.getNodeType() == ENTITY_REFERENCE_NODE) {
        sb.append(n.getTextContent());
      } else {
        sb.append(n.getNodeValue());
      }
    }
    return sb.toString();
  }

  public void setValue(String newValue) {
    if (ownerDocument.errorChecking && readOnly) {
      throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    NodeImpl kid;
    while ((kid = firstChildNode) != null) {
      internalRemoveChild(kid, false);
    }
    specified = true;
    if (newValue != null && newValue.length() > 0) {
      internalInsertBefore(ownerDocument.createTextNode(newValue), null, false);
    }
    changed();
  }

  void setValueQuiet(String newValue) {
    firstChildNode = null;
    lastChildNode = null;
    if (newValue != null && newValue.length() > 0) {
      TextImpl t = (TextImpl) ownerDocument.createTextNode(newValue);
      t.parentNode = this;
      firstChildNode = t;
      lastChildNode = t;
    }
  }

  public boolean isId() {
    return idAttribute;
  }

  public void setIdAttribute(boolean id) {
    idAttribute = id;
  }

  public String getTextContent() throws DOMException {
    return getNodeValue();
  }

  public void setTextContent(String textContent) throws DOMException {
    setNodeValue(textContent);
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
        } else if (prefix.equals("xmlns")) {
          if (!namespaceUri.equals(DomNames.kXmlnsUri)) {
            throw DomMessages.error(DOMException.NAMESPACE_ERR);
          }
        } else if (prefix.equals("xml")) {
          if (!namespaceUri.equals(DomNames.kXmlUri)) {
            throw DomMessages.error(DOMException.NAMESPACE_ERR);
          }
        } else if (name.equals("xmlns")) {
          throw DomMessages.error(DOMException.NAMESPACE_ERR);
        }
      }
    }
    if (localName == null) {
      return;
    }
    if (prefix != null && prefix.length() != 0) {
      name = prefix + ":" + localName;
    } else {
      name = localName;
    }
    ownerElement = ownerElement;
    changed();
  }

  public Node cloneNode(boolean deep) {
    AttrImpl copy = (AttrImpl) super.cloneNode(true);
    copy.ownerElement = null;
    copy.specified = true;
    return copy;
  }

  public String getBaseURI() {
    return null;
  }

  public TypeInfo getSchemaTypeInfo() {
    return this;
  }

  public String getTypeName() {
    return dtdType == null ? null : dtdType;
  }

  public String getTypeNamespace() {
    return dtdType == null ? null : "http://www.w3.org/TR/REC-xml";
  }

  public boolean isDerivedFrom(String typeNamespaceArg, String typeNameArg, int derivationMethod) {
    return false;
  }

  public String toString() {
    return getName() + "=\"" + getValue() + "\"";
  }

  void setReadOnlyRecursive(boolean value) {
    readOnly = value;
    for (NodeImpl n = firstChildNode; n != null; n = n.nextSibling) {
      n.setReadOnlyRecursive(value);
    }
  }
}
