/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import org.w3c.dom.Node;
import org.w3c.dom.Notation;

public class NotationImpl extends NodeImpl implements Notation {
  private static final long serialVersionUID = -764632195890658402L;

  private final String name;
  String publicId;
  String systemId;
  String baseUri;

  NotationImpl(DocumentImpl ownerDocument, String name) {
    super(ownerDocument);
    this.name = name;
  }

  public short getNodeType() {
    return Node.NOTATION_NODE;
  }

  public String getNodeName() {
    return name;
  }

  public String getPublicId() {
    return publicId;
  }

  public String getSystemId() {
    return systemId;
  }

  public void setPublicId(String id) {
    publicId = id;
  }

  public void setSystemId(String id) {
    systemId = id;
  }

  public String getBaseURI() {
    return baseUri != null ? baseUri : ownerDocument.getBaseURI();
  }

  public void setBaseURI(String uri) {
    baseUri = uri;
  }
}
