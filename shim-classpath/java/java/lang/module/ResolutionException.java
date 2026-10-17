/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.module;

public class ResolutionException extends RuntimeException {
  private static final long serialVersionUID = 1112122150310415792L;

  public ResolutionException() {
  }

  public ResolutionException(String msg) {
    super(msg);
  }

  public ResolutionException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public ResolutionException(Throwable cause) {
    super(cause);
  }
}
