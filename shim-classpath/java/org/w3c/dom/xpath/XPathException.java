/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.xpath;

public class XPathException extends java.lang.RuntimeException {
  private static final long serialVersionUID = 3471034171575979943L;

  public short code;
  public static final short INVALID_EXPRESSION_ERR = (short) 1;
  public static final short TYPE_ERR = (short) 2;

  public XPathException(short s, java.lang.String string) {
  }

}
