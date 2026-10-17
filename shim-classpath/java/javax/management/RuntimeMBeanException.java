/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class RuntimeMBeanException extends JMRuntimeException {
  private static final long serialVersionUID = 5274912751982730171L;

  private RuntimeException runtimeException;

  public RuntimeMBeanException(RuntimeException e) {
    super();
    this.runtimeException = e;
  }

  public RuntimeMBeanException(RuntimeException e, String message) {
    super(message);
    this.runtimeException = e;
  }

  public RuntimeException getTargetException() {
    return runtimeException;
  }

  @Override
  public Throwable getCause() {
    return runtimeException;
  }
}
