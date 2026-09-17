/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

public abstract class Diag {
  public static final long NOPOS = -1L;

  Diag() {
  }

  public abstract java.lang.String getCode();
  public abstract long getEndPosition();
  public abstract java.lang.String getMessage(java.util.Locale locale);
  public abstract long getPosition();
  public abstract long getStartPosition();
  public abstract boolean isError();
}
