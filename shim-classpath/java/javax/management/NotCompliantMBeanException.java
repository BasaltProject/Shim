/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class NotCompliantMBeanException extends OperationsException {
  private static final long serialVersionUID = 5175579583207963577L;

  public NotCompliantMBeanException() {
    super();
  }

  public NotCompliantMBeanException(String message) {
    super(message);
  }
}
