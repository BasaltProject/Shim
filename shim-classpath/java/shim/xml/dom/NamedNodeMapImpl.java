/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class NamedNodeMapImpl implements NamedNodeMap, java.io.Serializable {
  private static final long serialVersionUID = -7039242451046758020L;

  final List<NodeImpl> nodes = new ArrayList<NodeImpl>();
  final NodeImpl ownerNode;
  boolean readOnlyMap;

  NamedNodeMapImpl(NodeImpl ownerNode) {
    this.ownerNode = ownerNode;
  }

  public int getLength() {
    return nodes.size();
  }

  public Node item(int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + nodes.size());
    }
    return index < nodes.size() ? nodes.get(index) : null;
  }

  int findName(String name) {
    int lo = 0;
    int hi = nodes.size() - 1;
    while (lo <= hi) {
      int mid = (lo + hi) >>> 1;
      int cmp = nodes.get(mid).getNodeName().compareTo(name);
      if (cmp == 0) {
        return mid;
      }
      if (cmp < 0) {
        lo = mid + 1;
      } else {
        hi = mid - 1;
      }
    }
    return -(lo + 1);
  }

  int findNs(String namespaceUri, String localName) {
    for (int i = 0; i < nodes.size(); i++) {
      NodeImpl n = nodes.get(i);
      String ns = n.getNamespaceURI();
      String local = n.getLocalName();
      if (local == null) {
        continue;
      }
      if (local.equals(localName) && (ns == null ? namespaceUri == null : ns.equals(namespaceUri))) {
        return i;
      }
    }
    return -1;
  }

  public Node getNamedItem(String name) {
    int i = findName(name);
    return i >= 0 ? nodes.get(i) : null;
  }

  public Node getNamedItemNS(String namespaceUri, String localName) {
    int i = findNs(namespaceUri, localName);
    return i >= 0 ? nodes.get(i) : null;
  }

  void checkWritable() {
    if (ownerNode.ownerDocument != null && ownerNode.ownerDocument.errorChecking && (readOnlyMap
        || ownerNode.readOnly)) {
      throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
  }

  void checkDocument(Node arg) {
    if (ownerNode.ownerDocument != null && ownerNode.ownerDocument.errorChecking
        && arg.getOwnerDocument() != ownerNode.ownerDocument) {
      throw DomMessages.error(DOMException.WRONG_DOCUMENT_ERR);
    }
  }

  public Node setNamedItem(Node arg) throws DOMException {
    checkWritable();
    checkDocument(arg);
    NodeImpl n = (NodeImpl) arg;
    int i = findName(n.getNodeName());
    if (i >= 0) {
      NodeImpl previous = nodes.get(i);
      nodes.set(i, n);
      return previous;
    }
    nodes.add(-(i + 1), n);
    return null;
  }

  public Node setNamedItemNS(Node arg) throws DOMException {
    checkWritable();
    checkDocument(arg);
    NodeImpl n = (NodeImpl) arg;
    int i = findNs(n.getNamespaceURI(), n.getLocalName());
    if (i >= 0) {
      NodeImpl previous = nodes.get(i);
      nodes.set(i, n);
      return previous;
    }
    int pos = findName(n.getNodeName());
    nodes.add(pos >= 0 ? pos : -(pos + 1), n);
    return null;
  }

  public Node removeNamedItem(String name) throws DOMException {
    checkWritable();
    int i = findName(name);
    if (i < 0) {
      throw DomMessages.error(DOMException.NOT_FOUND_ERR);
    }
    return nodes.remove(i);
  }

  public Node removeNamedItemNS(String namespaceUri, String name) throws DOMException {
    checkWritable();
    int i = findNs(namespaceUri, name);
    if (i < 0) {
      throw DomMessages.error(DOMException.NOT_FOUND_ERR);
    }
    return nodes.remove(i);
  }
}
