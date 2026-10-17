/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.xpath;

public class XPathExpressionException extends XPathException {
  private static final long serialVersionUID = -1837080260374986980L;

  public XPathExpressionException(String message) {
    super(message);
  }

  public XPathExpressionException(Throwable cause) {
    super(cause);
  }
}
