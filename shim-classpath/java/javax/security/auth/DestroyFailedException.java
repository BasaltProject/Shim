/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth;

public class DestroyFailedException extends Exception {
  private static final long serialVersionUID = -7790152857282749162L;

  public DestroyFailedException() {
    super();
  }

  public DestroyFailedException(String msg) {
    super(msg);
  }
}
