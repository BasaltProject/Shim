/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql;

public interface DataSource extends CommonDataSource, java.sql.Wrapper {
  java.sql.Connection getConnection() throws java.sql.SQLException;

  java.sql.Connection getConnection(String p0, String p1) throws java.sql.SQLException;

  java.io.PrintWriter getLogWriter() throws java.sql.SQLException;

  void setLogWriter(java.io.PrintWriter p0) throws java.sql.SQLException;

  void setLoginTimeout(int p0) throws java.sql.SQLException;

  int getLoginTimeout() throws java.sql.SQLException;

  default java.sql.ConnectionBuilder createConnectionBuilder() throws java.sql.SQLException {
    throw new java.sql.SQLFeatureNotSupportedException("createConnectionBuilder not implemented");
  }
}
