/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote;

public class JMXProviderException extends java.io.IOException {
  private static final long serialVersionUID = -3166703627550447198L;


  public JMXProviderException(java.lang.String string, java.lang.Throwable throwable) {
    super(string, throwable);
  }
  public JMXProviderException(java.lang.String string) {
    super(string);
  }
  public JMXProviderException() {
  }

  public java.lang.Throwable getCause() {
    throw new UnsupportedOperationException();
  }
}
