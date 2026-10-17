/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.stream.events;

public interface XMLEvent extends javax.xml.stream.XMLStreamConstants {
  int getEventType();

  javax.xml.stream.Location getLocation();

  boolean isStartElement();

  boolean isAttribute();

  boolean isNamespace();

  boolean isEndElement();

  boolean isEntityReference();

  boolean isProcessingInstruction();

  boolean isCharacters();

  boolean isStartDocument();

  boolean isEndDocument();

  StartElement asStartElement();

  EndElement asEndElement();

  Characters asCharacters();

  javax.xml.namespace.QName getSchemaType();

  void writeAsEncodedUnicode(java.io.Writer p0) throws javax.xml.stream.XMLStreamException;
}
