/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface ResultSetMetaData extends Wrapper {
  int columnNoNulls = 0;
  int columnNullable = 1;
  int columnNullableUnknown = 2;

  int getColumnCount() throws SQLException;

  boolean isAutoIncrement(int p0) throws SQLException;

  boolean isCaseSensitive(int p0) throws SQLException;

  boolean isSearchable(int p0) throws SQLException;

  boolean isCurrency(int p0) throws SQLException;

  int isNullable(int p0) throws SQLException;

  boolean isSigned(int p0) throws SQLException;

  int getColumnDisplaySize(int p0) throws SQLException;

  String getColumnLabel(int p0) throws SQLException;

  String getColumnName(int p0) throws SQLException;

  String getSchemaName(int p0) throws SQLException;

  int getPrecision(int p0) throws SQLException;

  int getScale(int p0) throws SQLException;

  String getTableName(int p0) throws SQLException;

  String getCatalogName(int p0) throws SQLException;

  int getColumnType(int p0) throws SQLException;

  String getColumnTypeName(int p0) throws SQLException;

  boolean isReadOnly(int p0) throws SQLException;

  boolean isWritable(int p0) throws SQLException;

  boolean isDefinitelyWritable(int p0) throws SQLException;

  String getColumnClassName(int p0) throws SQLException;
}
