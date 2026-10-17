/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.module;

public class FindException extends RuntimeException {
  private static final long serialVersionUID = -5594688814111571025L;

  public FindException() {
  }

  public FindException(String msg) {
    super(msg);
  }

  public FindException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public FindException(Throwable cause) {
    super(cause);
  }
}
