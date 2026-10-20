/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class PartialResultException extends NamingException {
  private static final long serialVersionUID = 37029027322L;

  public PartialResultException(String explanation) {
    super(explanation);
  }

  public PartialResultException() {
    super();
  }
}
