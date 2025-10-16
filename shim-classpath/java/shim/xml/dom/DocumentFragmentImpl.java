/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

public class DocumentFragmentImpl extends ParentNode implements DocumentFragment {
  private static final long serialVersionUID = -7596449967279236746L;

  DocumentFragmentImpl(DocumentImpl ownerDocument) {
    super(ownerDocument);
  }

  public short getNodeType() {
    return Node.DOCUMENT_FRAGMENT_NODE;
  }

  public String getNodeName() {
    return "#document-fragment";
  }

  public void normalize() {
    if (readOnly) {
      return;
    }
    super.normalize();
  }
}
