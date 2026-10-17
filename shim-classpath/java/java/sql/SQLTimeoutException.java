/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public class SQLTimeoutException extends SQLTransientException {
  private static final long serialVersionUID = -4487171280562520262L;

  public SQLTimeoutException() {
    super();
  }

  public SQLTimeoutException(String reason) {
    super(reason);
  }

  public SQLTimeoutException(String reason, String SQLState) {
    super(reason, SQLState);
  }

  public SQLTimeoutException(String reason, String SQLState, int vendorCode) {
    super(reason, SQLState, vendorCode);
  }

  public SQLTimeoutException(Throwable cause) {
    super(cause);
  }

  public SQLTimeoutException(String reason, Throwable cause) {
    super(reason, cause);
  }

  public SQLTimeoutException(String reason, String SQLState, Throwable cause) {
    super(reason, SQLState, cause);
  }

  public SQLTimeoutException(String reason, String SQLState, int vendorCode, Throwable cause) {
    super(reason, SQLState, vendorCode, cause);
  }
}
