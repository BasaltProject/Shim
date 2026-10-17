/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql;

public interface CommonDataSource {
  java.io.PrintWriter getLogWriter() throws java.sql.SQLException;

  void setLogWriter(java.io.PrintWriter p0) throws java.sql.SQLException;

  void setLoginTimeout(int p0) throws java.sql.SQLException;

  int getLoginTimeout() throws java.sql.SQLException;

  java.util.logging.Logger getParentLogger() throws java.sql.SQLFeatureNotSupportedException;

  default java.sql.ShardingKeyBuilder createShardingKeyBuilder() throws java.sql.SQLException {
    throw new java.sql.SQLFeatureNotSupportedException("createShardingKeyBuilder not implemented");
  }
}
