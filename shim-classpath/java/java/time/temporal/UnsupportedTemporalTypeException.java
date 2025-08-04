/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.temporal;

import java.time.DateTimeException;

public class UnsupportedTemporalTypeException extends DateTimeException {
  public UnsupportedTemporalTypeException(String message) {
    super(message);
  }

  public UnsupportedTemporalTypeException(String message, Throwable cause) {
    super(message, cause);
  }
}
