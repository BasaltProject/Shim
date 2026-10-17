/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class JMRuntimeException extends RuntimeException {
  private static final long serialVersionUID = 6573344628407841861L;

  public JMRuntimeException() {
    super();
  }

  public JMRuntimeException(String message) {
    super(message);
  }
}
