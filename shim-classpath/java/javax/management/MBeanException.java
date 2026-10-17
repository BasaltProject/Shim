/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class MBeanException extends JMException {
  private static final long serialVersionUID = 4066342430588744142L;

  private Exception exception;

  public MBeanException(Exception e) {
    super();
    this.exception = e;
  }

  public MBeanException(Exception e, String message) {
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
