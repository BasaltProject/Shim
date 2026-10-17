/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql;

public interface RowSetMetaData extends java.sql.ResultSetMetaData {
  void setColumnCount(int p0) throws java.sql.SQLException;

  void setAutoIncrement(int p0, boolean p1) throws java.sql.SQLException;

  void setCaseSensitive(int p0, boolean p1) throws java.sql.SQLException;

  void setSearchable(int p0, boolean p1) throws java.sql.SQLException;

  void setCurrency(int p0, boolean p1) throws java.sql.SQLException;

  void setNullable(int p0, int p1) throws java.sql.SQLException;

  void setSigned(int p0, boolean p1) throws java.sql.SQLException;

  void setColumnDisplaySize(int p0, int p1) throws java.sql.SQLException;

  void setColumnLabel(int p0, String p1) throws java.sql.SQLException;

  void setColumnName(int p0, String p1) throws java.sql.SQLException;

  void setSchemaName(int p0, String p1) throws java.sql.SQLException;

  void setPrecision(int p0, int p1) throws java.sql.SQLException;

  void setScale(int p0, int p1) throws java.sql.SQLException;

  void setTableName(int p0, String p1) throws java.sql.SQLException;

  void setCatalogName(int p0, String p1) throws java.sql.SQLException;

  void setColumnType(int p0, int p1) throws java.sql.SQLException;

  void setColumnTypeName(int p0, String p1) throws java.sql.SQLException;
}
