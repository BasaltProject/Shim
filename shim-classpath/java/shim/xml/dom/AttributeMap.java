/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

public class AttributeMap extends NamedNodeMapImpl {
  private static final long serialVersionUID = 8872606282138665383L;

  AttributeMap(ElementImpl owner) {
    super(owner);
  }

  ElementImpl element() {
    return (ElementImpl) ownerNode;
  }

  public Node setNamedItem(Node arg) throws DOMException {
    checkWritable();
    checkDocument(arg);
    if (arg.getNodeType() != Node.ATTRIBUTE_NODE) {
      throw DomMessages.error(DOMException.HIERARCHY_REQUEST_ERR);
    }
    AttrImpl attr = (AttrImpl) arg;
    if (attr.ownerElement != null && attr.ownerElement != ownerNode) {
      throw DomMessages.error(DOMException.INUSE_ATTRIBUTE_ERR);
    }
    int i = findName(attr.getNodeName());
    if (i >= 0) {
      AttrImpl previous = (AttrImpl) nodes.get(i);
      if (previous == attr) {
        return attr;
      }
      nodes.set(i, attr);
      previous.ownerElement = null;
      attr.ownerElement = element();
      attr.ownerDocument = ownerNode.ownerDocument;
      ownerNode.changed();
      return previous;
    }
    nodes.add(-(i + 1), attr);
    attr.ownerElement = element();
    attr.ownerDocument = ownerNode.ownerDocument;
    ownerNode.changed();
    return null;
  }

  public Node setNamedItemNS(Node arg) throws DOMException {
    checkWritable();
    checkDocument(arg);
    if (arg.getNodeType() != Node.ATTRIBUTE_NODE) {
      throw DomMessages.error(DOMException.HIERARCHY_REQUEST_ERR);
    }
    AttrImpl attr = (AttrImpl) arg;
    if (attr.ownerElement != null && attr.ownerElement != ownerNode) {
      throw DomMessages.error(DOMException.INUSE_ATTRIBUTE_ERR);
    }
    int i = attr.getLocalName() == null ? findName(attr.getNodeName()) : findNs(attr.getNamespaceURI(), attr.getLocalName());
    if (i >= 0) {
      AttrImpl previous = (AttrImpl) nodes.get(i);
      if (previous == attr) {
        return attr;
      }
      nodes.remove(i);
      previous.ownerElement = null;
      int pos = findName(attr.getNodeName());
      nodes.add(pos >= 0 ? pos : -(pos + 1), attr);
      attr.ownerElement = element();
      attr.ownerDocument = ownerNode.ownerDocument;
      ownerNode.changed();
      return previous;
    }
    int pos = findName(attr.getNodeName());
    nodes.add(pos >= 0 ? pos : -(pos + 1), attr);
    attr.ownerElement = element();
    attr.ownerDocument = ownerNode.ownerDocument;
    ownerNode.changed();
    return null;
  }

  public Node removeNamedItem(String name) throws DOMException {
    checkWritable();
    int i = findName(name);
    if (i < 0) {
      throw DomMessages.error(DOMException.NOT_FOUND_ERR);
    }
    AttrImpl attr = (AttrImpl) nodes.remove(i);
    attr.ownerElement = null;
    ownerNode.changed();
    return attr;
  }

  public Node removeNamedItemNS(String namespaceUri, String name) throws DOMException {
    checkWritable();
    int i = findNs(namespaceUri, name);
    if (i < 0) {
      throw DomMessages.error(DOMException.NOT_FOUND_ERR);
    }
    AttrImpl attr = (AttrImpl) nodes.remove(i);
    attr.ownerElement = null;
    ownerNode.changed();
    return attr;
  }
}
