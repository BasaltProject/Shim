/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang;

public final class WrongThreadException extends RuntimeException {
  private static final long serialVersionUID = 4676498871006316905L;

  public WrongThreadException() {
    super();
  }

  public WrongThreadException(String message) {
    super(message);
  }

  public WrongThreadException(String message, Throwable cause) {
    super(message, cause);
  }

  public WrongThreadException(Throwable cause) {
    super(cause);
  }
}
