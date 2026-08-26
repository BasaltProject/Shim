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
import org.w3c.dom.Node;

public final class DOMNodeSetData implements NodeSetData<Node> {
  private final List<Node> nodes;

  public DOMNodeSetData(List<Node> nodes) {
    this.nodes = new ArrayList<Node>(nodes);
  }

  public Iterator<Node> iterator() {
    return nodes.iterator();
  }
}
