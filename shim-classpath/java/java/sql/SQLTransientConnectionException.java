/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public class SQLTransientConnectionException extends SQLTransientException {
  private static final long serialVersionUID = -2520155553543391200L;

  public SQLTransientConnectionException() {
    super();
  }

  public SQLTransientConnectionException(String reason) {
    super(reason);
  }

  public SQLTransientConnectionException(String reason, String SQLState) {
    super(reason, SQLState);
  }

  public SQLTransientConnectionException(String reason, String SQLState, int vendorCode) {
    super(reason, SQLState, vendorCode);
  }

  public SQLTransientConnectionException(Throwable cause) {
    super(cause);
  }

  public SQLTransientConnectionException(String reason, Throwable cause) {
    super(reason, cause);
  }

  public SQLTransientConnectionException(String reason, String SQLState, Throwable cause) {
    super(reason, SQLState, cause);
  }

  public SQLTransientConnectionException(String reason, String SQLState, int vendorCode, Throwable cause) {
    super(reason, SQLState, vendorCode, cause);
  }
}
