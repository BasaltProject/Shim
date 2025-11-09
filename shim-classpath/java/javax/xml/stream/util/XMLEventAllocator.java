/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.stream.util;

public interface XMLEventAllocator {
  XMLEventAllocator newInstance();

  javax.xml.stream.events.XMLEvent allocate(javax.xml.stream.XMLStreamReader p0)
      throws javax.xml.stream.XMLStreamException;

  void allocate(javax.xml.stream.XMLStreamReader p0, XMLEventConsumer p1) throws javax.xml.stream.XMLStreamException;
}
