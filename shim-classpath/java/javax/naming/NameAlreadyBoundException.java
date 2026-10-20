/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class NameAlreadyBoundException extends NamingException {
  private static final long serialVersionUID = 36099240017L;

  public NameAlreadyBoundException(String explanation) {
    super(explanation);
  }

  public NameAlreadyBoundException() {
    super();
  }
}
