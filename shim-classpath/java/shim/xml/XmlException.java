/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml;

public final class XmlException extends Exception {
  private static final long serialVersionUID = 1L;

  public final int line;
  public final int column;
  public final String systemId;
  public final String publicId;
  public final boolean recoverable;
  public final boolean internal;

  public XmlException(String message, int line, int column, String systemId, String publicId) {
    this(message, line, column, systemId, publicId, false);
  }

  public XmlException(String message, int line, int column, String systemId, String publicId, boolean recoverable) {
    this(message, line, column, systemId, publicId, recoverable, false);
  }

  public XmlException(String message, int line, int column, String systemId, String publicId, boolean recoverable,
                      boolean internal) {
    super(message);
    this.internal = internal;
    this.line = line;
    this.column = column;
    this.systemId = systemId;
    this.publicId = publicId;
    this.recoverable = recoverable;
  }
}
