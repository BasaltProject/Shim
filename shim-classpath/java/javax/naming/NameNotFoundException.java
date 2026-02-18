/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class NameNotFoundException extends NamingException {
  private static final long serialVersionUID = 88792009477L;

  public NameNotFoundException(String explanation) {
    super(explanation);
  }

  public NameNotFoundException() {
    super();
  }
}
