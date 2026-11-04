/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dom;

import javax.xml.crypto.XMLStructure;
import org.w3c.dom.Node;

public class DOMStructure implements XMLStructure {
  private final Node node;

  public DOMStructure(Node node) {
    if (node == null) {
      throw new NullPointerException("node cannot be null");
    }
    this.node = node;
  }

  public Node getNode() {
    return node;
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    } else {
      return false;
    }
  }
}
