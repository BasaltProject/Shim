/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.datatype;

public class DatatypeConfigurationException extends Exception {
  private static final long serialVersionUID = -1699373159027047238L;

  public DatatypeConfigurationException() {
    super();
  }

  public DatatypeConfigurationException(String message) {
    super(message);
  }

  public DatatypeConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }

  public DatatypeConfigurationException(Throwable cause) {
    super(cause);
  }
}
