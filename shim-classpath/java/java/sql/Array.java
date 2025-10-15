/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface Array {
  String getBaseTypeName() throws SQLException;

  int getBaseType() throws SQLException;

  Object getArray() throws SQLException;

  Object getArray(java.util.Map<String, Class<?>> p0) throws SQLException;

  Object getArray(long p0, int p1) throws SQLException;

  Object getArray(long p0, int p1, java.util.Map<String, Class<?>> p2) throws SQLException;

  ResultSet getResultSet() throws SQLException;

  ResultSet getResultSet(java.util.Map<String, Class<?>> p0) throws SQLException;

  ResultSet getResultSet(long p0, int p1) throws SQLException;

  ResultSet getResultSet(long p0, int p1, java.util.Map<String, Class<?>> p2) throws SQLException;

  void free() throws SQLException;
}
