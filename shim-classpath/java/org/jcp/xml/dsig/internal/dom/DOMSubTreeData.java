/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.crypto.NodeSetData;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class DOMSubTreeData implements NodeSetData<Node> {
  private final boolean excludeComments;
  private final Node root;

  public DOMSubTreeData(Node root, boolean excludeComments) {
    this.root = root;
    this.excludeComments = excludeComments;
  }

  public Node getRoot() {
    return root;
  }

  public boolean excludesComments() {
    return excludeComments;
  }

  public Iterator<Node> iterator() {
    List<Node> nodes = new ArrayList<Node>();
    collect(root, nodes);
    return nodes.iterator();
  }

  private void collect(Node n, List<Node> out) {
    if (excludeComments && n.getNodeType() == Node.COMMENT_NODE) {
      return;
    }
    out.add(n);
    NamedNodeMap attrs = n.getAttributes();
    if (attrs != null) {
      for (int i = 0; i < attrs.getLength(); i++) {
        out.add(attrs.item(i));
      }
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
      collect(c, out);
    }
  }
}
