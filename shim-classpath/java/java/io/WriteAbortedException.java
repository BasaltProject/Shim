/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.io;

public class WriteAbortedException extends ObjectStreamException {
  public Exception detail;

  public WriteAbortedException(String s, Exception ex) {
    super(s);
    initCause(null);
    detail = ex;
  }

  public String getMessage() {
    if (detail == null) {
      return super.getMessage();
    } else {
      return super.getMessage() + "; " + detail.toString();
    }
  }

  public Throwable getCause() {
    return detail;
  }
}
