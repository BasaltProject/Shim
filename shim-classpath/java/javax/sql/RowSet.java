/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql;

public interface RowSet extends java.sql.ResultSet {
  String getUrl() throws java.sql.SQLException;

  void setUrl(String p0) throws java.sql.SQLException;

  String getDataSourceName();

  void setDataSourceName(String p0) throws java.sql.SQLException;

  String getUsername();

  void setUsername(String p0) throws java.sql.SQLException;

  String getPassword();

  void setPassword(String p0) throws java.sql.SQLException;

  int getTransactionIsolation();

  void setTransactionIsolation(int p0) throws java.sql.SQLException;

  java.util.Map<String, Class<?>> getTypeMap() throws java.sql.SQLException;

  void setTypeMap(java.util.Map<String, Class<?>> p0) throws java.sql.SQLException;

  String getCommand();

  void setCommand(String p0) throws java.sql.SQLException;

  boolean isReadOnly();

  void setReadOnly(boolean p0) throws java.sql.SQLException;

  int getMaxFieldSize() throws java.sql.SQLException;

  void setMaxFieldSize(int p0) throws java.sql.SQLException;

  int getMaxRows() throws java.sql.SQLException;

  void setMaxRows(int p0) throws java.sql.SQLException;

  boolean getEscapeProcessing() throws java.sql.SQLException;

  void setEscapeProcessing(boolean p0) throws java.sql.SQLException;

  int getQueryTimeout() throws java.sql.SQLException;

  void setQueryTimeout(int p0) throws java.sql.SQLException;

  void setType(int p0) throws java.sql.SQLException;

  void setConcurrency(int p0) throws java.sql.SQLException;

  void setNull(int p0, int p1) throws java.sql.SQLException;

  void setNull(String p0, int p1) throws java.sql.SQLException;

  void setNull(int p0, int p1, String p2) throws java.sql.SQLException;

  void setNull(String p0, int p1, String p2) throws java.sql.SQLException;

  void setBoolean(int p0, boolean p1) throws java.sql.SQLException;

  void setBoolean(String p0, boolean p1) throws java.sql.SQLException;

  void setByte(int p0, byte p1) throws java.sql.SQLException;

  void setByte(String p0, byte p1) throws java.sql.SQLException;

  void setShort(int p0, short p1) throws java.sql.SQLException;

  void setShort(String p0, short p1) throws java.sql.SQLException;

  void setInt(int p0, int p1) throws java.sql.SQLException;

  void setInt(String p0, int p1) throws java.sql.SQLException;

  void setLong(int p0, long p1) throws java.sql.SQLException;

  void setLong(String p0, long p1) throws java.sql.SQLException;

  void setFloat(int p0, float p1) throws java.sql.SQLException;

  void setFloat(String p0, float p1) throws java.sql.SQLException;

  void setDouble(int p0, double p1) throws java.sql.SQLException;

  void setDouble(String p0, double p1) throws java.sql.SQLException;

  void setBigDecimal(int p0, java.math.BigDecimal p1) throws java.sql.SQLException;

  void setBigDecimal(String p0, java.math.BigDecimal p1) throws java.sql.SQLException;

  void setString(int p0, String p1) throws java.sql.SQLException;

  void setString(String p0, String p1) throws java.sql.SQLException;

  void setBytes(int p0, byte[] p1) throws java.sql.SQLException;

  void setBytes(String p0, byte[] p1) throws java.sql.SQLException;

  void setDate(int p0, java.sql.Date p1) throws java.sql.SQLException;

  void setTime(int p0, java.sql.Time p1) throws java.sql.SQLException;

  void setTimestamp(int p0, java.sql.Timestamp p1) throws java.sql.SQLException;

  void setTimestamp(String p0, java.sql.Timestamp p1) throws java.sql.SQLException;

  void setAsciiStream(int p0, java.io.InputStream p1, int p2) throws java.sql.SQLException;

  void setAsciiStream(String p0, java.io.InputStream p1, int p2) throws java.sql.SQLException;

  void setBinaryStream(int p0, java.io.InputStream p1, int p2) throws java.sql.SQLException;

  void setBinaryStream(String p0, java.io.InputStream p1, int p2) throws java.sql.SQLException;

  void setCharacterStream(int p0, java.io.Reader p1, int p2) throws java.sql.SQLException;

  void setCharacterStream(String p0, java.io.Reader p1, int p2) throws java.sql.SQLException;

  void setAsciiStream(int p0, java.io.InputStream p1) throws java.sql.SQLException;

  void setAsciiStream(String p0, java.io.InputStream p1) throws java.sql.SQLException;

  void setBinaryStream(int p0, java.io.InputStream p1) throws java.sql.SQLException;

  void setBinaryStream(String p0, java.io.InputStream p1) throws java.sql.SQLException;

  void setCharacterStream(int p0, java.io.Reader p1) throws java.sql.SQLException;

  void setCharacterStream(String p0, java.io.Reader p1) throws java.sql.SQLException;

  void setNCharacterStream(int p0, java.io.Reader p1) throws java.sql.SQLException;

  void setObject(int p0, Object p1, int p2, int p3) throws java.sql.SQLException;

  void setObject(String p0, Object p1, int p2, int p3) throws java.sql.SQLException;

  void setObject(int p0, Object p1, int p2) throws java.sql.SQLException;

  void setObject(String p0, Object p1, int p2) throws java.sql.SQLException;

  void setObject(String p0, Object p1) throws java.sql.SQLException;

  void setObject(int p0, Object p1) throws java.sql.SQLException;

  void setRef(int p0, java.sql.Ref p1) throws java.sql.SQLException;

  void setBlob(int p0, java.sql.Blob p1) throws java.sql.SQLException;

  void setBlob(int p0, java.io.InputStream p1, long p2) throws java.sql.SQLException;

  void setBlob(int p0, java.io.InputStream p1) throws java.sql.SQLException;

  void setBlob(String p0, java.io.InputStream p1, long p2) throws java.sql.SQLException;

  void setBlob(String p0, java.sql.Blob p1) throws java.sql.SQLException;

  void setBlob(String p0, java.io.InputStream p1) throws java.sql.SQLException;

  void setClob(int p0, java.sql.Clob p1) throws java.sql.SQLException;

  void setClob(int p0, java.io.Reader p1, long p2) throws java.sql.SQLException;

  void setClob(int p0, java.io.Reader p1) throws java.sql.SQLException;

  void setClob(String p0, java.io.Reader p1, long p2) throws java.sql.SQLException;

  void setClob(String p0, java.sql.Clob p1) throws java.sql.SQLException;

  void setClob(String p0, java.io.Reader p1) throws java.sql.SQLException;

  void setArray(int p0, java.sql.Array p1) throws java.sql.SQLException;

  void setDate(int p0, java.sql.Date p1, java.util.Calendar p2) throws java.sql.SQLException;

  void setDate(String p0, java.sql.Date p1) throws java.sql.SQLException;

  void setDate(String p0, java.sql.Date p1, java.util.Calendar p2) throws java.sql.SQLException;

  void setTime(int p0, java.sql.Time p1, java.util.Calendar p2) throws java.sql.SQLException;

  void setTime(String p0, java.sql.Time p1) throws java.sql.SQLException;

  void setTime(String p0, java.sql.Time p1, java.util.Calendar p2) throws java.sql.SQLException;

  void setTimestamp(int p0, java.sql.Timestamp p1, java.util.Calendar p2) throws java.sql.SQLException;

  void setTimestamp(String p0, java.sql.Timestamp p1, java.util.Calendar p2) throws java.sql.SQLException;

  void clearParameters() throws java.sql.SQLException;

  void execute() throws java.sql.SQLException;

  void addRowSetListener(RowSetListener p0);

  void removeRowSetListener(RowSetListener p0);

  void setSQLXML(int p0, java.sql.SQLXML p1) throws java.sql.SQLException;

  void setSQLXML(String p0, java.sql.SQLXML p1) throws java.sql.SQLException;

  void setRowId(int p0, java.sql.RowId p1) throws java.sql.SQLException;

  void setRowId(String p0, java.sql.RowId p1) throws java.sql.SQLException;

  void setNString(int p0, String p1) throws java.sql.SQLException;

  void setNString(String p0, String p1) throws java.sql.SQLException;

  void setNCharacterStream(int p0, java.io.Reader p1, long p2) throws java.sql.SQLException;

  void setNCharacterStream(String p0, java.io.Reader p1, long p2) throws java.sql.SQLException;

  void setNCharacterStream(String p0, java.io.Reader p1) throws java.sql.SQLException;

  void setNClob(String p0, java.sql.NClob p1) throws java.sql.SQLException;

  void setNClob(String p0, java.io.Reader p1, long p2) throws java.sql.SQLException;

  void setNClob(String p0, java.io.Reader p1) throws java.sql.SQLException;

  void setNClob(int p0, java.io.Reader p1, long p2) throws java.sql.SQLException;

  void setNClob(int p0, java.sql.NClob p1) throws java.sql.SQLException;

  void setNClob(int p0, java.io.Reader p1) throws java.sql.SQLException;

  void setURL(int p0, java.net.URL p1) throws java.sql.SQLException;
}
