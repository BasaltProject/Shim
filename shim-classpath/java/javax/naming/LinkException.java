/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class LinkException extends NamingException {
  private static final long serialVersionUID = 82723171089L;

  public LinkException(String explanation) {
    super(explanation);
  }

  public LinkException() {
    super();
  }
}
