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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

final class DeepNodeList implements NodeList {
  private final NodeImpl root;
  private final String tagName;
  private final String namespaceUri;
  private final String localName;
  private final boolean ns;
  private List<Node> cache;
  private int stamp = -1;

  DeepNodeList(NodeImpl root, String tagName) {
    this.root = root;
    this.tagName = tagName;
    this.namespaceUri = null;
    this.localName = null;
    this.ns = false;
  }

  DeepNodeList(NodeImpl root, String namespaceUri, String localName) {
    this.root = root;
    this.tagName = null;
    this.namespaceUri = namespaceUri != null && namespaceUri.length() == 0 ? null : namespaceUri;
    this.localName = localName;
    this.ns = true;
  }

  private void refresh() {
    if (cache != null && root.ownerDocument != null && stamp == root.ownerDocument.modCount) {
      return;
    }
    cache = new ArrayList<Node>();
    collect(root);
    stamp = root.ownerDocument == null ? 0 : root.ownerDocument.modCount;
  }

  private void collect(NodeImpl parent) {
    for (NodeImpl n = (NodeImpl) parent.getFirstChild(); n != null; n = n.nextSibling) {
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        if (matches(n)) {
          cache.add(n);
        }
        collect(n);
      } else if (n.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
        collect(n);
      }
    }
  }

  private boolean matches(NodeImpl n) {
    if (!ns) {
      return tagName.equals("*") || n.getNodeName().equals(tagName);
    }
    boolean nsMatch = "*".equals(namespaceUri) || (namespaceUri == null ? n.getNamespaceURI() == null
        : namespaceUri.equals(n.getNamespaceURI()));
    if (!nsMatch) {
      return false;
    }
    if ("*".equals(localName)) {
      return true;
    }
    String local = n.getLocalName();
    return local != null && local.equals(localName);
  }

  public int getLength() {
    refresh();
    return cache.size();
  }

  public Node item(int index) {
    refresh();
    return index >= 0 && index < cache.size() ? cache.get(index) : null;
  }
}
