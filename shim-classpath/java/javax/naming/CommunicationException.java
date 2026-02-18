/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class CommunicationException extends NamingException {
  private static final long serialVersionUID = 58302848294L;

  public CommunicationException(String explanation) {
    super(explanation);
  }

  public CommunicationException() {
    super();
  }
}
