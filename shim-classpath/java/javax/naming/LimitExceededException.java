/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class LimitExceededException extends NamingException {
  private static final long serialVersionUID = 50533929182L;

  public LimitExceededException(String explanation) {
    super(explanation);
  }

  public LimitExceededException() {
    super();
  }
}
