/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.stream;

import javax.xml.stream.XMLStreamConstants;

final class StaxNames {
  private StaxNames() {
  }

  static String eventName(int type) {
    switch (type) {
      case XMLStreamConstants.START_ELEMENT:
        return "START_ELEMENT";
      case XMLStreamConstants.END_ELEMENT:
        return "END_ELEMENT";
      case XMLStreamConstants.PROCESSING_INSTRUCTION:
        return "PROCESSING_INSTRUCTION";
      case XMLStreamConstants.CHARACTERS:
        return "CHARACTERS";
      case XMLStreamConstants.COMMENT:
        return "COMMENT";
      case XMLStreamConstants.SPACE:
        return "SPACE";
      case XMLStreamConstants.START_DOCUMENT:
        return "START_DOCUMENT";
      case XMLStreamConstants.END_DOCUMENT:
        return "END_DOCUMENT";
      case XMLStreamConstants.ENTITY_REFERENCE:
        return "ENTITY_REFERENCE";
      case XMLStreamConstants.ATTRIBUTE:
        return "ATTRIBUTE";
      case XMLStreamConstants.DTD:
        return "DTD";
      case XMLStreamConstants.CDATA:
        return "CDATA";
      case XMLStreamConstants.NAMESPACE:
        return "NAMESPACE";
      case XMLStreamConstants.NOTATION_DECLARATION:
        return "NOTATION_DECLARATION";
      case XMLStreamConstants.ENTITY_DECLARATION:
        return "ENTITY_DECLARATION";
      default:
        return "UNKNOWN_EVENT_TYPE, " + type;
    }
  }
}
