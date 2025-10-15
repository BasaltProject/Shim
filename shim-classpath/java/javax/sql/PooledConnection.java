/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql;

public interface PooledConnection {
  java.sql.Connection getConnection() throws java.sql.SQLException;

  void close() throws java.sql.SQLException;

  void addConnectionEventListener(ConnectionEventListener p0);

  void removeConnectionEventListener(ConnectionEventListener p0);

  void addStatementEventListener(StatementEventListener p0);

  void removeStatementEventListener(StatementEventListener p0);
}
