/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.metadata;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

public class IIOMetadataNode implements Element, NodeList {
  private final String nodeName;
  private String nodeValue;
  private Object userObject;
  private IIOMetadataNode parent;
  private final List<IIOMetadataNode> children = new ArrayList<IIOMetadataNode>();
  private final List<AttrNode> attrs = new ArrayList<AttrNode>();

  public IIOMetadataNode() { this(null); }

  public IIOMetadataNode(String nodeName) { this.nodeName = nodeName; }

  private static DOMException unsupported() {
    return new DOMException(DOMException.NOT_SUPPORTED_ERR, "Method not supported");
  }

  private static final class AttrNode implements Attr {
    private final String name;
    private String value;
    private IIOMetadataNode owner;

    AttrNode(String name, String value) {
      this.name = name;
      this.value = value;
    }

    public String getName() { return name; }

    public String getNodeName() { return name; }

    public short getNodeType() { return ATTRIBUTE_NODE; }

    public boolean getSpecified() { return true; }

    public String getValue() { return value; }

    public String getNodeValue() { return value; }

    public void setValue(String v) { value = v; }

    public void setNodeValue(String v) { value = v; }

    public Element getOwnerElement() { return owner; }

    public void setOwnerElement(Element e) { owner = (IIOMetadataNode) e; }

    public boolean isId() { return false; }

    public Node getParentNode() { return null; }

    public NodeList getChildNodes() { return new IIOMetadataNode(); }

    public Node getFirstChild() { return null; }

    public Node getLastChild() { return null; }

    public Node getPreviousSibling() { return null; }

    public Node getNextSibling() { return null; }

    public NamedNodeMap getAttributes() { return null; }

    public Document getOwnerDocument() { return null; }

    public Node insertBefore(Node newChild, Node refChild) { throw unsupported(); }

    public Node replaceChild(Node newChild, Node oldChild) { throw unsupported(); }

    public Node removeChild(Node oldChild) { throw unsupported(); }

    public Node appendChild(Node newChild) { throw unsupported(); }

    public boolean hasChildNodes() { return false; }

    public Node cloneNode(boolean deep) { return new AttrNode(name, value); }

    public void normalize() { }

    public boolean isSupported(String feature, String version) { return false; }

    public String getNamespaceURI() { return null; }

    public String getPrefix() { return null; }

    public void setPrefix(String prefix) { }

    public String getLocalName() { return name; }

    public boolean hasAttributes() { return false; }

    public String getBaseURI() { return null; }

    public short compareDocumentPosition(Node other) { throw unsupported(); }

    public String getTextContent() { return value; }

    public void setTextContent(String t) { value = t; }

    public boolean isSameNode(Node other) { return this == other; }

    public String lookupPrefix(String namespaceURI) { return null; }

    public boolean isDefaultNamespace(String namespaceURI) { return false; }

    public String lookupNamespaceURI(String prefix) { return null; }

    public boolean isEqualNode(Node arg) { return this == arg; }

    public Object getFeature(String feature, String version) { return null; }

    public Object setUserData(String key, Object data, UserDataHandler handler) { throw unsupported(); }

    public Object getUserData(String key) { return null; }

    public TypeInfo getSchemaTypeInfo() { return null; }
  }

  private final class AttrMap implements NamedNodeMap {
    public Node getNamedItem(String name) { return getAttributeNode(name); }

    public Node item(int index) { return index >= 0 && index < attrs.size() ? attrs.get(index) : null; }

    public int getLength() { return attrs.size(); }

    public Node getNamedItemNS(String namespaceURI, String localName) { return getNamedItem(localName); }

    public Node setNamedItem(Node arg) { return setAttributeNode((Attr) arg); }

    public Node removeNamedItem(String name) {
      Attr a = getAttributeNode(name);
      if (a == null) {
        throw new DOMException(DOMException.NOT_FOUND_ERR, "No such attribute!");
      }
      removeAttribute(name);
      return a;
    }

    public Node setNamedItemNS(Node arg) { throw unsupported(); }

    public Node removeNamedItemNS(String namespaceURI, String localName) { throw unsupported(); }
  }

  public String getNodeName() { return nodeName; }

  public String getNodeValue() { return nodeValue; }

  public void setNodeValue(String nodeValue) { this.nodeValue = nodeValue; }

  public short getNodeType() { return ELEMENT_NODE; }

  public Node getParentNode() { return parent; }

  public NodeList getChildNodes() { return this; }

  public Node getFirstChild() { return children.isEmpty() ? null : children.get(0); }

  public Node getLastChild() { return children.isEmpty() ? null : children.get(children.size() - 1); }

  public Node getPreviousSibling() {
    if (parent == null) {
      return null;
    }
    int at = parent.children.indexOf(this);
    return at > 0 ? parent.children.get(at - 1) : null;
  }

  public Node getNextSibling() {
    if (parent == null) {
      return null;
    }
    int at = parent.children.indexOf(this);
    return at >= 0 && at + 1 < parent.children.size() ? parent.children.get(at + 1) : null;
  }

  public NamedNodeMap getAttributes() { return new AttrMap(); }

  public Document getOwnerDocument() { return null; }

  private IIOMetadataNode check(Node n) {
    if (n == null) {
      throw new IllegalArgumentException("newChild == null!");
    }
    if (!(n instanceof IIOMetadataNode)) {
      throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, "newChild is not an IIOMetadataNode!");
    }
    return (IIOMetadataNode) n;
  }

  public Node insertBefore(Node newChild, Node refChild) {
    IIOMetadataNode newNode = check(newChild);
    IIOMetadataNode refNode = refChild == null ? null : check(refChild);
    if (newNode.parent != null) {
      newNode.parent.children.remove(newNode);
    }
    newNode.parent = this;
    if (refNode == null) {
      children.add(newNode);
    } else {
      int at = children.indexOf(refNode);
      if (at < 0) {
        throw new DOMException(DOMException.NOT_FOUND_ERR, "refChild is not a child");
      }
      children.add(at, newNode);
    }
    return newNode;
  }

  public Node replaceChild(Node newChild, Node oldChild) {
    IIOMetadataNode newNode = check(newChild);
    IIOMetadataNode oldNode = check(oldChild);
    int at = children.indexOf(oldNode);
    if (at < 0) {
      throw new DOMException(DOMException.NOT_FOUND_ERR, "oldChild is not a child");
    }
    if (newNode.parent != null) {
      newNode.parent.children.remove(newNode);
      at = children.indexOf(oldNode);
    }
    children.set(at, newNode);
    oldNode.parent = null;
    newNode.parent = this;
    return oldNode;
  }

  public Node removeChild(Node oldChild) {
    if (oldChild == null) {
      throw new IllegalArgumentException("oldChild == null!");
    }
    IIOMetadataNode old = check(oldChild);
    if (!children.remove(old)) {
      throw new DOMException(DOMException.NOT_FOUND_ERR, "oldChild is not a child");
    }
    old.parent = null;
    return old;
  }

  public Node appendChild(Node newChild) {
    if (newChild == null) {
      throw new IllegalArgumentException("newChild == null!");
    }
    return insertBefore(newChild, null);
  }

  public boolean hasChildNodes() { return !children.isEmpty(); }

  public Node cloneNode(boolean deep) {
    IIOMetadataNode copy = new IIOMetadataNode(nodeName);
    copy.nodeValue = nodeValue;
    copy.userObject = userObject;
    for (AttrNode a : attrs) {
      copy.setAttribute(a.name, a.value);
    }
    if (deep) {
      for (IIOMetadataNode c : children) {
        copy.appendChild(c.cloneNode(true));
      }
    }
    return copy;
  }

  public void normalize() { }

  public boolean isSupported(String feature, String version) { return false; }

  public String getNamespaceURI() throws DOMException { return null; }

  public String getPrefix() { return null; }

  public void setPrefix(String prefix) { }

  public String getLocalName() { return nodeName; }

  public boolean hasAttributes() { return !attrs.isEmpty(); }

  public String getBaseURI() throws DOMException { return null; }

  public short compareDocumentPosition(Node other) throws DOMException { throw unsupported(); }

  public String getTextContent() throws DOMException { throw unsupported(); }

  public void setTextContent(String textContent) throws DOMException { throw unsupported(); }

  public boolean isSameNode(Node node) throws DOMException { throw unsupported(); }

  public String lookupPrefix(String namespaceURI) throws DOMException { throw unsupported(); }

  public boolean isDefaultNamespace(String namespaceURI) throws DOMException { throw unsupported(); }

  public String lookupNamespaceURI(String prefix) throws DOMException { throw unsupported(); }

  public boolean isEqualNode(Node node) throws DOMException { throw unsupported(); }

  public Object getFeature(String feature, String version) throws DOMException { throw unsupported(); }

  public Object setUserData(String key, Object data, UserDataHandler handler) throws DOMException {
    throw unsupported();
  }

  public Object getUserData(String key) throws DOMException { throw unsupported(); }

  public TypeInfo getSchemaTypeInfo() throws DOMException { throw unsupported(); }

  public Object getUserObject() { return userObject; }

  public void setUserObject(Object userObject) { this.userObject = userObject; }

  public String getTagName() { return nodeName; }

  public String getAttribute(String name) {
    Attr a = getAttributeNode(name);
    return a == null ? "" : a.getValue();
  }

  public String getAttributeNS(String namespaceURI, String localName) { return getAttribute(localName); }

  public void setAttribute(String name, String value) {
    if (name == null) {
      throw new IllegalArgumentException("name == null");
    }
    removeAttribute(name, false);
    AttrNode a = new AttrNode(name, value);
    a.owner = this;
    attrs.add(a);
  }

  public void setAttributeNS(String namespaceURI, String qualifiedName, String value) {
    setAttribute(qualifiedName, value);
  }

  public void removeAttribute(String name) { removeAttribute(name, true); }

  private void removeAttribute(String name, boolean checkPresent) {
    for (int i = 0; i < attrs.size(); i++) {
      if (attrs.get(i).name.equals(name)) {
        attrs.remove(i).owner = null;
        return;
      }
    }
    if (checkPresent) {
      return;
    }
  }

  public void removeAttributeNS(String namespaceURI, String localName) { removeAttribute(localName); }

  public Attr getAttributeNode(String name) {
    for (AttrNode a : attrs) {
      if (a.name.equals(name)) {
        return a;
      }
    }
    return null;
  }

  public Attr getAttributeNodeNS(String namespaceURI, String localName) { return getAttributeNode(localName); }

  public Attr setAttributeNode(Attr newAttr) throws DOMException {
    Element owner = newAttr.getOwnerElement();
    if (owner != null) {
      if (owner == this) {
        return null;
      }
      throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, "Attribute is already in use");
    }
    Attr old = getAttributeNode(newAttr.getName());
    setAttribute(newAttr.getName(), newAttr.getValue());
    return old;
  }

  public Attr setAttributeNodeNS(Attr newAttr) { return setAttributeNode(newAttr); }

  public Attr removeAttributeNode(Attr oldAttr) {
    removeAttribute(oldAttr.getName());
    return oldAttr;
  }

  public boolean hasAttribute(String name) { return getAttributeNode(name) != null; }

  public boolean hasAttributeNS(String namespaceURI, String localName) { return hasAttribute(localName); }

  public NodeList getElementsByTagName(String name) {
    List<Node> found = new ArrayList<Node>();
    collect(this, name, found);
    return new ListNodeList(found);
  }

  public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
    return getElementsByTagName(localName);
  }

  private static void collect(IIOMetadataNode n, String name, List<Node> out) {
    if (name.equals("*") || name.equals(n.nodeName)) {
      out.add(n);
    }
    for (IIOMetadataNode c : n.children) {
      collect(c, name, out);
    }
  }

  private static final class ListNodeList implements NodeList {
    private final List<Node> nodes;

    ListNodeList(List<Node> nodes) { this.nodes = nodes; }

    public Node item(int index) { return index >= 0 && index < nodes.size() ? nodes.get(index) : null; }

    public int getLength() { return nodes.size(); }
  }

  public void setIdAttribute(String name, boolean isId) throws DOMException { throw unsupported(); }

  public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
    throw unsupported();
  }

  public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException { throw unsupported(); }

  public int getLength() { return children.size(); }

  public Node item(int index) { return index >= 0 && index < children.size() ? children.get(index) : null; }
}
