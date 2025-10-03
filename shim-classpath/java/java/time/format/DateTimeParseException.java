/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time.format;

import java.time.DateTimeException;

public class DateTimeParseException extends DateTimeException {
  private static final long serialVersionUID = 4304633501674722597L;

  private final String parsedData;
  private final int errorIndex;

  public DateTimeParseException(String message, CharSequence parsedData, int errorIndex) {
    super(message);
    this.parsedData = (parsedData != null) ? parsedData.toString() : null;
    this.errorIndex = errorIndex;
  }

  public DateTimeParseException(String message, CharSequence parsedData, int errorIndex, Throwable cause) {
    super(message, cause);
    this.parsedData = (parsedData != null) ? parsedData.toString() : null;
    this.errorIndex = errorIndex;
  }

  public String getParsedString() {
    return parsedData;
  }

  public int getErrorIndex() {
    return errorIndex;
  }
}
