/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.xml.sax;

public class SAXNotRecognizedException extends SAXException {
  static final long serialVersionUID = 5440506620509557213L;

  public SAXNotRecognizedException() {
    super();
  }

  public SAXNotRecognizedException(String message) {
    super(message);
  }
}
