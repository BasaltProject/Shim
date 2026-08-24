/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class TimeLimitExceededException extends javax.naming.LimitExceededException {
  private static final long serialVersionUID = -3597009011385034696L;


  public TimeLimitExceededException() {
  }
  public TimeLimitExceededException(java.lang.String string) {
    super(string);
  }

}
