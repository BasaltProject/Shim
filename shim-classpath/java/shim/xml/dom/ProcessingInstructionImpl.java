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
import org.w3c.dom.ProcessingInstruction;

public class ProcessingInstructionImpl extends CharacterDataImpl implements ProcessingInstruction {
  private static final long serialVersionUID = 7554435174099981510L;

  private final String target;
  String baseUri;

  ProcessingInstructionImpl(DocumentImpl ownerDocument, String target, String data) {
    super(ownerDocument, data);
    this.target = target;
  }

  public short getNodeType() {
    return Node.PROCESSING_INSTRUCTION_NODE;
  }

  public String getNodeName() {
    return target;
  }

  public String getTarget() {
    return target;
  }

  public String getData() {
    return data;
  }

  public void setData(String value) throws DOMException {
    super.setData(value);
  }

  public String getBaseURI() {
    return baseUri != null ? baseUri : baseUriOfAncestors();
  }

  public void setBaseURI(String uri) {
    baseUri = uri;
  }
}
