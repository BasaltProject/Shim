/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.transform;

public class TransformerConfigurationException extends TransformerException {
  private static final long serialVersionUID = 1285547467942875745L;

  public TransformerConfigurationException() {
    super("Configuration Error");
  }

  public TransformerConfigurationException(String msg) {
    super(msg);
  }

  public TransformerConfigurationException(Throwable e) {
    super(e);
  }

  public TransformerConfigurationException(String msg, Throwable e) {
    super(msg, e);
  }

  public TransformerConfigurationException(String message, SourceLocator locator) {
    super(message, locator);
  }

  public TransformerConfigurationException(String message, SourceLocator locator, Throwable e) {
    super(message, locator, e);
  }
}
