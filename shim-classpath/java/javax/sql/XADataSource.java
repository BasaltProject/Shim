/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql;

public interface XADataSource extends CommonDataSource {
  XAConnection getXAConnection() throws java.sql.SQLException;

  XAConnection getXAConnection(String p0, String p1) throws java.sql.SQLException;

  java.io.PrintWriter getLogWriter() throws java.sql.SQLException;

  void setLogWriter(java.io.PrintWriter p0) throws java.sql.SQLException;

  void setLoginTimeout(int p0) throws java.sql.SQLException;

  int getLoginTimeout() throws java.sql.SQLException;

  default XAConnectionBuilder createXAConnectionBuilder() throws java.sql.SQLException {
    throw new java.sql.SQLFeatureNotSupportedException("createXAConnectionBuilder not implemented");
  }
}
