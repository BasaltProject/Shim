/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class ContextNotEmptyException extends NamingException {
  private static final long serialVersionUID = 45696832176L;

  public ContextNotEmptyException(String explanation) {
    super(explanation);
  }

  public ContextNotEmptyException() {
    super();
  }
}
