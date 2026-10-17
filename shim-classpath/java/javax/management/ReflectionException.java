/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class ReflectionException extends JMException {
  private static final long serialVersionUID = 9170809325636915553L;

  private Exception exception;

  public ReflectionException(Exception e) {
    super();
    this.exception = e;
  }

  public ReflectionException(Exception e, String message) {
    super(message);
    this.exception = e;
  }

  public Exception getTargetException() {
    return exception;
  }

  @Override
  public Throwable getCause() {
    return exception;
  }
}
