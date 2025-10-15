/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql;

public interface RowSetInternal {
  Object[] getParams() throws java.sql.SQLException;

  java.sql.Connection getConnection() throws java.sql.SQLException;

  void setMetaData(RowSetMetaData p0) throws java.sql.SQLException;

  java.sql.ResultSet getOriginal() throws java.sql.SQLException;

  java.sql.ResultSet getOriginalRow() throws java.sql.SQLException;
}
