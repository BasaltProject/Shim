/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.zone;

import java.time.DateTimeException;

public class ZoneRulesException extends DateTimeException {
  private static final long serialVersionUID = -1632418723876261839L;

  public ZoneRulesException(String message) {
    super(message);
  }

  public ZoneRulesException(String message, Throwable cause) {
    super(message, cause);
  }
}
