/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.transform;

public class TransformerFactoryConfigurationError extends Error {
  private static final long serialVersionUID = -6527718720676281516L;

  private Exception exception;

  public TransformerFactoryConfigurationError() {
    super();
    this.exception = null;
  }

  public TransformerFactoryConfigurationError(String msg) {
    super(msg);
    this.exception = null;
  }

  public TransformerFactoryConfigurationError(Exception e) {
    super(e.toString());
    this.exception = e;
  }

  public TransformerFactoryConfigurationError(Exception e, String msg) {
    super(msg);
    this.exception = e;
  }

  public String getMessage() {
    String message = super.getMessage();
    if (message == null && exception != null) {
      return exception.getMessage();
    }
    return message;
  }

  public Exception getException() {
    return exception;
  }

  public Throwable getCause() {
    return exception;
  }
}
