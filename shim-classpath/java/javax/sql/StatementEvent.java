/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementEvent extends java.util.EventObject {
  private static final long serialVersionUID = -8089573731826608315L;

  private SQLException exception;
  private PreparedStatement statement;

  public StatementEvent(PooledConnection con, PreparedStatement statement) {
    super(con);
    this.statement = statement;
    this.exception = null;
  }

  public StatementEvent(PooledConnection con, PreparedStatement statement, SQLException exception) {
    super(con);
    this.statement = statement;
    this.exception = exception;
  }

  public PreparedStatement getStatement() {
    return this.statement;
  }

  public SQLException getSQLException() {
    return this.exception;
  }
}
