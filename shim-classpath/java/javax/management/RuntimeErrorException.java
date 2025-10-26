/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class RuntimeErrorException extends JMRuntimeException {
  private static final long serialVersionUID = 704338937753949796L;

  private Error error;

  public RuntimeErrorException(Error e) {
    super();
    this.error = e;
  }

  public RuntimeErrorException(Error e, String message) {
    super(message);
    this.error = e;
  }

  public Error getTargetError() {
    return error;
  }

  @Override
  public Throwable getCause() {
    return error;
  }
}
