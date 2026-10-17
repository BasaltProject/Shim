/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.parsers;

public class ParserConfigurationException extends Exception {
  private static final long serialVersionUID = -3688849216575373917L;

  public ParserConfigurationException() {
    super();
  }

  public ParserConfigurationException(String msg) {
    super(msg);
  }
}
