/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import org.w3c.dom.Entity;
import org.w3c.dom.Node;

public class EntityImpl extends ParentNode implements Entity {
  private static final long serialVersionUID = -3575760943444303423L;

  private final String name;
  String publicId;
  String systemId;
  String notationName;
  String version;
  String encoding;
  String inputEncoding;
  String baseUri;

  EntityImpl(DocumentImpl ownerDocument, String name) {
    super(ownerDocument);
    this.name = name;
  }

  public short getNodeType() {
    return Node.ENTITY_NODE;
  }

  public String getNodeName() {
    return name;
  }

  public void setNodeValue(String value) {
    if (ownerDocument.errorChecking && readOnly) {
      throw DomMessages.error(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
  }

  public void setPrefix(String prefix) {
    if (ownerDocument.errorChecking && readOnly) {
      throw DomMessages.error(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
  }

  public String getPublicId() {
    return publicId;
  }

  public String getSystemId() {
    return systemId;
  }

  public String getXmlVersion() {
    return version;
  }

  public String getXmlEncoding() {
    return encoding;
  }

  public String getInputEncoding() {
    return inputEncoding;
  }

  public String getNotationName() {
    return notationName;
  }

  public void setPublicId(String id) {
    publicId = id;
  }

  public void setSystemId(String id) {
    systemId = id;
  }

  public void setNotationName(String name) {
    notationName = name;
  }

  public String getBaseURI() {
    return baseUri != null ? baseUri : ownerDocument.getBaseURI();
  }

  public void setBaseURI(String uri) {
    baseUri = uri;
  }
}
