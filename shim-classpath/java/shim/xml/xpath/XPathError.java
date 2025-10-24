/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.xpath;

final class XPathError extends RuntimeException {
  private static final long serialVersionUID = 1L;
  static final String kTransformer = "javax.xml.transform.TransformerException";
  static final String kXPath = "com.sun.org.apache.xpath.internal.XPathException";

  private final String className;

  XPathError(String className, String message) {
    super(message);
    this.className = className;
  }

  XPathError(String message) {
    this(kTransformer, message);
  }

  @Override
  public String toString() {
    return className + ": " + getMessage();
  }
}
