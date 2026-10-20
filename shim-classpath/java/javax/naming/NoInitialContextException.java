/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class NoInitialContextException extends NamingException {
  private static final long serialVersionUID = 33336778862L;

  public NoInitialContextException(String explanation) {
    super(explanation);
  }

  public NoInitialContextException() {
    super();
  }
}
