/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public class SQLNonTransientConnectionException extends SQLNonTransientException {
  private static final long serialVersionUID = -5852318857474782892L;

  public SQLNonTransientConnectionException() {
    super();
  }

  public SQLNonTransientConnectionException(String reason) {
    super(reason);
  }

  public SQLNonTransientConnectionException(String reason, String SQLState) {
    super(reason, SQLState);
  }

  public SQLNonTransientConnectionException(String reason, String SQLState, int vendorCode) {
    super(reason, SQLState, vendorCode);
  }

  public SQLNonTransientConnectionException(Throwable cause) {
    super(cause);
  }

  public SQLNonTransientConnectionException(String reason, Throwable cause) {
    super(reason, cause);
  }

  public SQLNonTransientConnectionException(String reason, String SQLState, Throwable cause) {
    super(reason, SQLState, cause);
  }

  public SQLNonTransientConnectionException(String reason, String SQLState, int vendorCode, Throwable cause) {
    super(reason, SQLState, vendorCode, cause);
  }
}
