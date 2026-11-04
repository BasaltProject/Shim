/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.keyinfo.KeyName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMKeyName implements KeyName {
  private final String name;

  public DOMKeyName(String name) {
    if (name == null) {
      throw new NullPointerException("name cannot be null");
    }
    this.name = name;
  }

  public DOMKeyName(Element knElem) {
    name = DOMUtils.textOf(knElem);
  }

  public String getName() {
    return name;
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
    Document ownerDoc = DOMUtils.getOwnerDocument(parent);
    parent.appendChild(DOMUtils.textElement(ownerDoc, "KeyName", dsPrefix, name));
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof KeyName)) {
      return false;
    }
    return name.equals(((KeyName) obj).getName());
  }

  public int hashCode() {
    return 17 * 31 + name.hashCode();
  }
}
