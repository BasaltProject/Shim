/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig.dom;

import java.security.Key;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.XMLValidateContext;
import org.w3c.dom.Node;

public class DOMValidateContext extends DOMCryptoContext implements XMLValidateContext {
  private Node node;

  public DOMValidateContext(KeySelector ks, Node node) {
    if (ks == null) {
      throw new NullPointerException("key selector is null");
    }
    init(node, ks);
  }

  public DOMValidateContext(Key validatingKey, Node node) {
    if (validatingKey == null) {
      throw new NullPointerException("validatingKey is null");
    }
    init(node, KeySelector.singletonKeySelector(validatingKey));
  }

  private void init(Node node, KeySelector ks) {
    if (node == null) {
      throw new NullPointerException("node is null");
    }
    this.node = node;
    super.setKeySelector(ks);
    super.setProperty("org.jcp.xml.dsig.secureValidation", Boolean.TRUE);
  }

  public void setNode(Node node) {
    if (node == null) {
      throw new NullPointerException();
    }
    this.node = node;
  }

  public Node getNode() {
    return node;
  }
}
