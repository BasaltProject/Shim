/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public class SQLWarning extends SQLException {
  private static final long serialVersionUID = 3917336774604784856L;

  public SQLWarning(String reason, String SQLState, int vendorCode) {
    super(reason, SQLState, vendorCode);
    log("SQLWarning: reason(" + reason + ") SQLState(" + SQLState + ") vendor code(" + vendorCode + ")");
  }

  public SQLWarning(String reason, String SQLState) {
    super(reason, SQLState);
    log("SQLWarning: reason(" + reason + ") SQLState(" + SQLState + ")");
  }

  public SQLWarning(String reason) {
    super(reason);
    log("SQLWarning: reason(" + reason + ")");
  }

  public SQLWarning() {
    super();
    log("SQLWarning: ");
  }

  public SQLWarning(Throwable cause) {
    super(cause);
    log("SQLWarning");
  }

  public SQLWarning(String reason, Throwable cause) {
    super(reason, cause);
    log("SQLWarning : reason(" + reason + ")");
  }

  public SQLWarning(String reason, String SQLState, Throwable cause) {
    super(reason, SQLState, cause);
    log("SQLWarning: reason(" + reason + ") SQLState(" + SQLState + ")");
  }

  public SQLWarning(String reason, String SQLState, int vendorCode, Throwable cause) {
    super(reason, SQLState, vendorCode, cause);
    log("SQLWarning: reason(" + reason + ") SQLState(" + SQLState + ") vendor code(" + vendorCode + ")");
  }

  private static void log(String message) {
    if (DriverManager.getLogWriter() != null) {
      DriverManager.println(message);
    }
  }

  public SQLWarning getNextWarning() {
    try {
      return (SQLWarning) getNextException();
    } catch (ClassCastException ex) {
      throw new Error("SQLWarning chain holds value that is not a SQLWarning");
    }
  }

  public void setNextWarning(SQLWarning w) {
    setNextException(w);
  }

}
