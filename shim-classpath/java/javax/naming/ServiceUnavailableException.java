/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class ServiceUnavailableException extends NamingException {
  private static final long serialVersionUID = 10001489486L;

  public ServiceUnavailableException(String explanation) {
    super(explanation);
  }

  public ServiceUnavailableException() {
    super();
  }
}
