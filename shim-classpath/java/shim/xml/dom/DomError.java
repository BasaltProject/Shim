/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import org.w3c.dom.DOMError;
import org.w3c.dom.DOMLocator;
import org.w3c.dom.Node;

final class DomError implements DOMError {
  private final short severity;
  private final String message;
  private final String type;
  private final Throwable related;
  private final DOMLocator location;

  DomError(short severity, String message, String type, Throwable related, final int line, final int column,
      final String uri) {
    this.severity = severity;
    this.message = message;
    this.type = type;
    this.related = related;
    this.location = new DOMLocator() {
      public int getLineNumber() {
        return line;
      }

      public int getColumnNumber() {
        return column;
      }

      public int getByteOffset() {
        return -1;
      }

      public int getUtf16Offset() {
        return -1;
      }

      public Node getRelatedNode() {
        return null;
      }

      public String getUri() {
        return uri;
      }
    };
  }

  public short getSeverity() {
    return severity;
  }

  public String getMessage() {
    return message;
  }

  public String getType() {
    return type;
  }

  public Object getRelatedException() {
    return related;
  }

  public Object getRelatedData() {
    return null;
  }

  public DOMLocator getLocation() {
    return location;
  }
}
