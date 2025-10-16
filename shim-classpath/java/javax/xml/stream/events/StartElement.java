/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.stream.events;

public interface StartElement extends XMLEvent {
  javax.xml.namespace.QName getName();

  java.util.Iterator<Attribute> getAttributes();

  java.util.Iterator<Namespace> getNamespaces();

  Attribute getAttributeByName(javax.xml.namespace.QName p0);

  javax.xml.namespace.NamespaceContext getNamespaceContext();

  String getNamespaceURI(String p0);
}
