/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class NotContextException extends NamingException {
  private static final long serialVersionUID = 30485910304L;

  public NotContextException(String explanation) {
    super(explanation);
  }

  public NotContextException() {
    super();
  }
}
