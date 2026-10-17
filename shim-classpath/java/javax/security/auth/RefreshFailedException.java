/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth;

public class RefreshFailedException extends Exception {
  private static final long serialVersionUID = 5058444488565265840L;

  public RefreshFailedException() {
    super();
  }

  public RefreshFailedException(String msg) {
    super(msg);
  }
}
