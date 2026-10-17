/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class JMException extends Exception {
  private static final long serialVersionUID = 350520924977331825L;

  public JMException() {
    super();
  }

  public JMException(String message) {
    super(message);
  }
}
