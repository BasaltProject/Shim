/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql;

import java.sql.SQLException;

public class ConnectionEvent extends java.util.EventObject {
  private static final long serialVersionUID = -4843217645290030002L;

  private SQLException ex = null;

  public ConnectionEvent(PooledConnection con) {
    super(con);
  }

  public ConnectionEvent(PooledConnection con, SQLException ex) {
    super(con);
    this.ex = ex;
  }

  public SQLException getSQLException() {
    return ex;
  }
}
