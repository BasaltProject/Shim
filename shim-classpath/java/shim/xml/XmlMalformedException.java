/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml;

import java.io.IOException;

public final class XmlMalformedException extends IOException {
  private static final long serialVersionUID = 1L;

  public XmlMalformedException(String message) {
    super(message);
  }
}
