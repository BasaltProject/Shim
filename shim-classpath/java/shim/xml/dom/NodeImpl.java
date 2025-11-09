/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

public abstract class NodeImpl implements Node, NodeList, Cloneable, java.io.Serializable {
  private static final long serialVersionUID = -6316591992167219696L;

  DocumentImpl ownerDocument;
  NodeImpl parentNode;
  NodeImpl previousSibling;
  NodeImpl nextSibling;
  boolean readOnly;
  Map<String, Object[]> userData;

  NodeImpl(DocumentImpl ownerDocument) {
    this.ownerDocument = ownerDocument;
  }

  public abstract short getNodeType();

  public abstract String getNodeName();

  public String getNodeValue() throws DOMException {
    return null;
  }

  public void setNodeValue(String nodeValue) throws DOMException {
    if (ownerDocument != null && ownerDocument.errorChecking && readOnly) {
      throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
  }

  public Node getParentNode() {
    return parentNode;
  }

  public Node getFirstChild() {
    return null;
  }

  public Node getLastChild() {
    return null;
  }

  public Node getPreviousSibling() {
    return previousSibling;
  }

  public Node getNextSibling() {
    return nextSibling;
  }

  public NodeList getChildNodes() {
    return this;
  }

  public int getLength() {
    return 0;
  }

  public Node item(int index) {
    return null;
  }

  public NamedNodeMap getAttributes() {
    return null;
  }

  public boolean hasAttributes() {
    return false;
  }

  public Document getOwnerDocument() {
    return ownerDocument;
  }

  public boolean hasChildNodes() {
    return false;
  }

  public Node insertBefore(Node newChild, Node refChild) throws DOMException {
    throw DomMessages.error(DOMException.HIERARCHY_REQUEST_ERR);
  }

  public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
    throw DomMessages.error(DOMException.HIERARCHY_REQUEST_ERR);
  }

  public Node removeChild(Node oldChild) throws DOMException {
    throw DomMessages.error(DOMException.NOT_FOUND_ERR);
  }

  public Node appendChild(Node newChild) throws DOMException {
    return insertBefore(newChild, null);
  }

  public Node cloneNode(boolean deep) {
    NodeImpl copy;
    try {
      copy = (NodeImpl) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
    copy.parentNode = null;
    copy.previousSibling = null;
    copy.nextSibling = null;
    copy.readOnly = false;
    copy.userData = null;
    ownerDocument.callUserDataHandlers(this, copy, UserDataHandler.NODE_CLONED);
    return copy;
  }

  public void normalize() {
  }

  public boolean isSupported(String feature, String version) {
    return ownerDocument.getImplementation().hasFeature(feature, version);
  }

  public String getNamespaceURI() {
    return null;
  }

  public String getPrefix() {
    return null;
  }

  public void setPrefix(String prefix) throws DOMException {
    throw DomMessages.error(DOMException.NAMESPACE_ERR);
  }

  public String getLocalName() {
    return null;
  }

  public String getBaseURI() {
    return null;
  }

  String baseUriOfAncestors() {
    for (NodeImpl n = parentNode; n != null; n = n.parentNode) {
      String b = n.getBaseURI();
      if (b != null) {
        return b;
      }
    }
    return null;
  }

  public short compareDocumentPosition(Node other) throws DOMException {
    if (other == this) {
      return 0;
    }
    if (!(other instanceof NodeImpl)) {
      throw DomMessages.error(DOMException.NOT_SUPPORTED_ERR);
    }
    NodeImpl reference = this;
    NodeImpl node = (NodeImpl) other;
    Document thisDoc = reference.getNodeType() == DOCUMENT_NODE ? (Document) reference : reference.getOwnerDocument();
    Document otherDoc = node.getNodeType() == DOCUMENT_NODE ? (Document) node : node.getOwnerDocument();
    if (thisDoc != otherDoc && thisDoc != null && otherDoc != null) {
      int c1 = System.identityHashCode(thisDoc);
      int c2 = System.identityHashCode(otherDoc);
      return c1 > c2 ? (short) (DOCUMENT_POSITION_DISCONNECTED | DOCUMENT_POSITION_FOLLOWING
          | DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC)
          : (short) (DOCUMENT_POSITION_DISCONNECTED | DOCUMENT_POSITION_PRECEDING
          | DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC);
    }
    List<NodeImpl> thisAncestors = ancestors(reference);
    List<NodeImpl> otherAncestors = ancestors(node);
    NodeImpl thisTop = thisAncestors.get(thisAncestors.size() - 1);
    NodeImpl otherTop = otherAncestors.get(otherAncestors.size() - 1);
    if (thisTop != otherTop) {
      int c1 = System.identityHashCode(thisTop);
      int c2 = System.identityHashCode(otherTop);
      return c1 > c2 ? (short) (DOCUMENT_POSITION_DISCONNECTED | DOCUMENT_POSITION_FOLLOWING
          | DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC)
          : (short) (DOCUMENT_POSITION_DISCONNECTED | DOCUMENT_POSITION_PRECEDING
          | DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC);
    }
    if (thisAncestors.contains(node)) {
      return (short) (DOCUMENT_POSITION_CONTAINS | DOCUMENT_POSITION_PRECEDING);
    }
    if (otherAncestors.contains(reference)) {
      return (short) (DOCUMENT_POSITION_CONTAINED_BY | DOCUMENT_POSITION_FOLLOWING);
    }
    int i = thisAncestors.size() - 1;
    int j = otherAncestors.size() - 1;
    while (i >= 0 && j >= 0 && thisAncestors.get(i) == otherAncestors.get(j)) {
      i--;
      j--;
    }
    NodeImpl a = thisAncestors.get(i);
    NodeImpl b = otherAncestors.get(j);
    NodeImpl parent = a.parentNode;
    if (a.getNodeType() == ATTRIBUTE_NODE || b.getNodeType() == ATTRIBUTE_NODE) {
      return (short) (a.getNodeType() == ATTRIBUTE_NODE ? DOCUMENT_POSITION_FOLLOWING : DOCUMENT_POSITION_PRECEDING);
    }
    for (NodeImpl c = (NodeImpl) parent.getFirstChild(); c != null; c = c.nextSibling) {
      if (c == a) {
        return DOCUMENT_POSITION_FOLLOWING;
      }
      if (c == b) {
        return DOCUMENT_POSITION_PRECEDING;
      }
    }
    return DOCUMENT_POSITION_DISCONNECTED;
  }

  private static List<NodeImpl> ancestors(NodeImpl start) {
    List<NodeImpl> list = new ArrayList<NodeImpl>();
    NodeImpl n = start;
    while (n != null) {
      list.add(n);
      if (n.getNodeType() == ATTRIBUTE_NODE) {
        n = (NodeImpl) ((org.w3c.dom.Attr) n).getOwnerElement();
      } else {
        n = n.parentNode;
      }
    }
    return list;
  }

  public String getTextContent() throws DOMException {
    return getNodeValue();
  }

  public void setTextContent(String textContent) throws DOMException {
    setNodeValue(textContent);
  }

  public boolean isSameNode(Node other) {
    return this == other;
  }

  public String lookupPrefix(String namespaceURI) {
    if (namespaceURI == null) {
      return null;
    }
    short type = getNodeType();
    switch (type) {
    case ELEMENT_NODE: {
      String namespace = getNamespaceURI();
      return lookupNamespacePrefix(namespaceURI, (ElementImpl) this);
    }
    case DOCUMENT_NODE: {
      org.w3c.dom.Element docElement = ((Document) this).getDocumentElement();
      if (docElement != null) {
        return docElement.lookupPrefix(namespaceURI);
      }
      return null;
    }
    case ENTITY_NODE:
    case NOTATION_NODE:
    case DOCUMENT_FRAGMENT_NODE:
    case DOCUMENT_TYPE_NODE:
      return null;
    case ATTRIBUTE_NODE: {
      org.w3c.dom.Element owner = ((org.w3c.dom.Attr) this).getOwnerElement();
      if (owner != null) {
        return owner.lookupPrefix(namespaceURI);
      }
      return null;
    }
    default: {
      NodeImpl ancestor = parentNodeForLookup();
      if (ancestor != null) {
        return ancestor.lookupPrefix(namespaceURI);
      }
      return null;
    }
    }
  }

  private NodeImpl parentNodeForLookup() {
    Node n = getParentNode();
    while (n != null && n.getNodeType() != ELEMENT_NODE) {
      n = n.getParentNode();
    }
    return (NodeImpl) n;
  }

  String lookupNamespacePrefix(String namespaceURI, ElementImpl el) {
    String namespace = getNamespaceURI();
    String prefix = getPrefix();
    if (namespace != null && namespace.equals(namespaceURI)) {
      if (prefix != null) {
        String foundNamespace = el.lookupNamespaceURI(prefix);
        if (foundNamespace != null && foundNamespace.equals(namespaceURI)) {
          return prefix;
        }
      }
    }
    if (hasAttributes()) {
      NamedNodeMap map = getAttributes();
      int length = map.getLength();
      for (int i = 0; i < length; i++) {
        Node attr = map.item(i);
        String attrPrefix = attr.getPrefix();
        String value = attr.getNodeValue();
        namespace = attr.getNamespaceURI();
        if (namespace != null && namespace.equals("http://www.w3.org/2000/xmlns/")) {
          if (attr.getNodeName().startsWith("xmlns:") && value != null && value.equals(namespaceURI)) {
            String localname = attr.getLocalName();
            String foundNamespace = el.lookupNamespaceURI(localname);
            if (foundNamespace != null && foundNamespace.equals(namespaceURI)) {
              return localname;
            }
          }
        }
      }
    }
    NodeImpl ancestor = parentNodeForLookup();
    if (ancestor != null && ancestor.getNodeType() == ELEMENT_NODE) {
      return ancestor.lookupNamespacePrefix(namespaceURI, el);
    }
    return null;
  }

  public boolean isDefaultNamespace(String namespaceURI) {
    short type = getNodeType();
    switch (type) {
    case ELEMENT_NODE: {
      String namespace = getNamespaceURI();
      String prefix = getPrefix();
      if (prefix == null || prefix.isEmpty()) {
        if (namespaceURI == null) {
          return namespace == namespaceURI;
        }
        return namespaceURI.equals(namespace);
      }
      if (hasAttributes()) {
        ElementImpl elem = (ElementImpl) this;
        NodeImpl attr = (NodeImpl) elem.getAttributeNodeNS("http://www.w3.org/2000/xmlns/", "xmlns");
        if (attr != null) {
          String value = attr.getNodeValue();
          if (namespaceURI == null) {
            return namespace == value;
          }
          return namespaceURI.equals(value);
        }
      }
      NodeImpl ancestor = parentNodeForLookup();
      if (ancestor != null) {
        return ancestor.isDefaultNamespace(namespaceURI);
      }
      return false;
    }
    case DOCUMENT_NODE: {
      org.w3c.dom.Element docElement = ((Document) this).getDocumentElement();
      if (docElement != null) {
        return docElement.isDefaultNamespace(namespaceURI);
      }
      return false;
    }
    case ENTITY_NODE:
    case NOTATION_NODE:
    case DOCUMENT_FRAGMENT_NODE:
    case DOCUMENT_TYPE_NODE:
      return false;
    case ATTRIBUTE_NODE: {
      org.w3c.dom.Element owner = ((org.w3c.dom.Attr) this).getOwnerElement();
      if (owner != null) {
        return owner.isDefaultNamespace(namespaceURI);
      }
      return false;
    }
    default: {
      NodeImpl ancestor = parentNodeForLookup();
      if (ancestor != null) {
        return ancestor.isDefaultNamespace(namespaceURI);
      }
      return false;
    }
    }
  }

  public String lookupNamespaceURI(String specifiedPrefix) {
    short type = getNodeType();
    switch (type) {
    case ELEMENT_NODE: {
      String namespace = getNamespaceURI();
      String prefix = getPrefix();
      if (namespace != null) {
        if (specifiedPrefix == null && prefix == specifiedPrefix) {
          return namespace;
        } else if (prefix != null && prefix.equals(specifiedPrefix)) {
          return namespace;
        }
      }
      if (hasAttributes()) {
        NamedNodeMap map = getAttributes();
        int length = map.getLength();
        for (int i = 0; i < length; i++) {
          Node attr = map.item(i);
          String attrPrefix = attr.getPrefix();
          String value = attr.getNodeValue();
          namespace = attr.getNamespaceURI();
          if (namespace != null && namespace.equals("http://www.w3.org/2000/xmlns/")) {
            if (specifiedPrefix == null && attr.getNodeName().equals("xmlns")) {
              return value.length() > 0 ? value : null;
            } else if (attrPrefix != null && attrPrefix.equals("xmlns")
                && attr.getLocalName().equals(specifiedPrefix)) {
              return value.length() > 0 ? value : null;
            }
          }
        }
      }
      NodeImpl ancestor = parentNodeForLookup();
      if (ancestor != null) {
        return ancestor.lookupNamespaceURI(specifiedPrefix);
      }
      return null;
    }
    case DOCUMENT_NODE: {
      org.w3c.dom.Element docElement = ((Document) this).getDocumentElement();
      if (docElement != null) {
        return docElement.lookupNamespaceURI(specifiedPrefix);
      }
      return null;
    }
    case ENTITY_NODE:
    case NOTATION_NODE:
    case DOCUMENT_FRAGMENT_NODE:
    case DOCUMENT_TYPE_NODE:
      return null;
    case ATTRIBUTE_NODE: {
      org.w3c.dom.Element owner = ((org.w3c.dom.Attr) this).getOwnerElement();
      if (owner != null) {
        return owner.lookupNamespaceURI(specifiedPrefix);
      }
      return null;
    }
    default: {
      NodeImpl ancestor = parentNodeForLookup();
      if (ancestor != null) {
        return ancestor.lookupNamespaceURI(specifiedPrefix);
      }
      return null;
    }
    }
  }

  public boolean isEqualNode(Node arg) {
    if (arg == this) {
      return true;
    }
    if (arg.getNodeType() != getNodeType()) {
      return false;
    }
    if (!eq(getNodeName(), arg.getNodeName()) || !eq(getLocalName(), arg.getLocalName())
        || !eq(getNamespaceURI(), arg.getNamespaceURI()) || !eq(getPrefix(), arg.getPrefix())
        || !eq(getNodeValue(), arg.getNodeValue())) {
      return false;
    }
    if (hasAttributes() != arg.hasAttributes()) {
      return false;
    }
    if (hasAttributes()) {
      NamedNodeMap map1 = getAttributes();
      NamedNodeMap map2 = arg.getAttributes();
      int len = map1.getLength();
      if (len != map2.getLength()) {
        return false;
      }
      for (int i = 0; i < len; i++) {
        Node n1 = map1.item(i);
        Node n2;
        if (n1.getLocalName() == null) {
          n2 = map2.getNamedItem(n1.getNodeName());
        } else {
          n2 = map2.getNamedItemNS(n1.getNamespaceURI(), n1.getLocalName());
        }
        if (n2 == null || !n1.isEqualNode(n2)) {
          return false;
        }
      }
    }
    if (hasChildNodes() != arg.hasChildNodes()) {
      return false;
    }
    if (hasChildNodes()) {
      Node child1 = getFirstChild();
      Node child2 = arg.getFirstChild();
      while (child1 != null && child2 != null) {
        if (!child1.isEqualNode(child2)) {
          return false;
        }
        child1 = child1.getNextSibling();
        child2 = child2.getNextSibling();
      }
      if (child1 != child2) {
        return false;
      }
    }
    if (getNodeType() == DOCUMENT_TYPE_NODE) {
      org.w3c.dom.DocumentType a = (org.w3c.dom.DocumentType) this;
      org.w3c.dom.DocumentType b = (org.w3c.dom.DocumentType) arg;
      if (!eq(a.getPublicId(), b.getPublicId()) || !eq(a.getSystemId(), b.getSystemId())
          || !eq(a.getInternalSubset(), b.getInternalSubset())) {
        return false;
      }
    }
    return true;
  }

  private static boolean eq(String a, String b) {
    return a == null ? b == null : a.equals(b);
  }

  public Object getFeature(String feature, String version) {
    return isSupported(feature, version) ? this : null;
  }

  public Object setUserData(String key, Object data, UserDataHandler handler) {
    if (userData == null) {
      userData = new HashMap<String, Object[]>();
    }
    Object[] old;
    if (data == null) {
      old = userData.remove(key);
    } else {
      old = userData.put(key, new Object[] {data, handler});
    }
    return old == null ? null : old[0];
  }

  public Object getUserData(String key) {
    if (userData == null) {
      return null;
    }
    Object[] entry = userData.get(key);
    return entry == null ? null : entry[0];
  }

  DocumentImpl ownerDocument_() {
    return ownerDocument;
  }

  void appendTextContent(StringBuilder buf) {
    String t = getTextContent();
    if (t != null) {
      buf.append(t);
    }
  }

  NodeImpl lastChildNodeOrNull() {
    return null;
  }

  NodeImpl firstChildNode_() {
    return (NodeImpl) getFirstChild();
  }

  void setUserDataNoHandler(String key, Object data, UserDataHandler handler) {
    if (userData == null) {
      userData = new HashMap<String, Object[]>();
    }
    userData.put(key, new Object[] {data, handler});
  }

  boolean isReadOnly() {
    return readOnly;
  }

  void setReadOnlyRecursive(boolean value) {
    readOnly = value;
  }

  void checkWritable() {
    if (ownerDocument != null && ownerDocument.errorChecking && readOnly) {
      throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
  }

  void changed() {
    if (ownerDocument != null) {
      ownerDocument.changed();
    }
  }

  public String toString() {
    return "[" + getNodeName() + ": " + getNodeValue() + "]";
  }
}
