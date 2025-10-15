/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public class SQLSyntaxErrorException extends SQLNonTransientException {
  private static final long serialVersionUID = -1843832610477496053L;

  public SQLSyntaxErrorException() {
    super();
  }

  public SQLSyntaxErrorException(String reason) {
    super(reason);
  }

  public SQLSyntaxErrorException(String reason, String SQLState) {
    super(reason, SQLState);
  }

  public SQLSyntaxErrorException(String reason, String SQLState, int vendorCode) {
    super(reason, SQLState, vendorCode);
  }

  public SQLSyntaxErrorException(Throwable cause) {
    super(cause);
  }

  public SQLSyntaxErrorException(String reason, Throwable cause) {
    super(reason, cause);
  }

  public SQLSyntaxErrorException(String reason, String SQLState, Throwable cause) {
    super(reason, SQLState, cause);
  }

  public SQLSyntaxErrorException(String reason, String SQLState, int vendorCode, Throwable cause) {
    super(reason, SQLState, vendorCode, cause);
  }
}
