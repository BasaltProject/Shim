/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset;

public interface JdbcRowSet extends javax.sql.RowSet, javax.sql.rowset.Joinable {
  void commit() throws java.sql.SQLException;
  boolean getAutoCommit() throws java.sql.SQLException;
  javax.sql.rowset.RowSetWarning getRowSetWarnings() throws java.sql.SQLException;
  boolean getShowDeleted() throws java.sql.SQLException;
  void rollback() throws java.sql.SQLException;
  void rollback(java.sql.Savepoint savepoint) throws java.sql.SQLException;
  void setAutoCommit(boolean flag) throws java.sql.SQLException;
  void setShowDeleted(boolean flag) throws java.sql.SQLException;
}
