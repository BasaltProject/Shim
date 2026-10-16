/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.reflect;

public class UndeclaredThrowableException extends RuntimeException {
  private static final long serialVersionUID = 330127114055056639L;

  private final Throwable undeclaredThrowable;

  public UndeclaredThrowableException(Throwable undeclaredThrowable) {
    super((Throwable) null);
    this.undeclaredThrowable = undeclaredThrowable;
  }

  public UndeclaredThrowableException(Throwable undeclaredThrowable, String s) {
    super(s, null);
    this.undeclaredThrowable = undeclaredThrowable;
  }

  public Throwable getUndeclaredThrowable() {
    return undeclaredThrowable;
  }

  @Override
  public Throwable getCause() {
    return undeclaredThrowable;
  }
}
