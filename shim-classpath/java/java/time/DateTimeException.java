/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time;

public class DateTimeException extends RuntimeException {
  public DateTimeException(String message) {
    super(message);
  }

  public DateTimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
