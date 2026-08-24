/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.modelmbean;

public class XMLParseException extends Exception {
  private static final long serialVersionUID = 3176664577895105181L;

  public XMLParseException() {
    super("XML Parse Exception.");
  }

  public XMLParseException(String s) {
    super("XML Parse Exception: " + s);
  }

  public XMLParseException(Exception e, String s) {
    super("XML Parse Exception: " + s + ":" + (e == null ? "" : e.toString()));
  }
}
