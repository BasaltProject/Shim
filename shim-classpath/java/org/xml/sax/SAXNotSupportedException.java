/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.xml.sax;

public class SAXNotSupportedException extends SAXException {
  static final long serialVersionUID = -1422818934641823846L;

  public SAXNotSupportedException() {
    super();
  }

  public SAXNotSupportedException(String message) {
    super(message);
  }
}
