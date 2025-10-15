/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface Driver {
  Connection connect(String p0, java.util.Properties p1) throws SQLException;

  boolean acceptsURL(String p0) throws SQLException;

  DriverPropertyInfo[] getPropertyInfo(String p0, java.util.Properties p1) throws SQLException;

  int getMajorVersion();

  int getMinorVersion();

  boolean jdbcCompliant();

  java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException;
}
