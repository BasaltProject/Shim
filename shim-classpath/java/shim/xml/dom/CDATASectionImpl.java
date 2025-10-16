/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Node;

public class CDATASectionImpl extends TextImpl implements CDATASection {
  private static final long serialVersionUID = 2372071297878177780L;

  CDATASectionImpl(DocumentImpl ownerDocument, String data) {
    super(ownerDocument, data);
  }

  public short getNodeType() {
    return Node.CDATA_SECTION_NODE;
  }

  public String getNodeName() {
    return "#cdata-section";
  }
}
