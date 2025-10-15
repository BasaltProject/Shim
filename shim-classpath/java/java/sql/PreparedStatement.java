/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface PreparedStatement extends Statement {
  ResultSet executeQuery() throws SQLException;

  int executeUpdate() throws SQLException;

  void setNull(int p0, int p1) throws SQLException;

  void setBoolean(int p0, boolean p1) throws SQLException;

  void setByte(int p0, byte p1) throws SQLException;

  void setShort(int p0, short p1) throws SQLException;

  void setInt(int p0, int p1) throws SQLException;

  void setLong(int p0, long p1) throws SQLException;

  void setFloat(int p0, float p1) throws SQLException;

  void setDouble(int p0, double p1) throws SQLException;

  void setBigDecimal(int p0, java.math.BigDecimal p1) throws SQLException;

  void setString(int p0, String p1) throws SQLException;

  void setBytes(int p0, byte[] p1) throws SQLException;

  void setDate(int p0, Date p1) throws SQLException;

  void setTime(int p0, Time p1) throws SQLException;

  void setTimestamp(int p0, Timestamp p1) throws SQLException;

  void setAsciiStream(int p0, java.io.InputStream p1, int p2) throws SQLException;

  void setUnicodeStream(int p0, java.io.InputStream p1, int p2) throws SQLException;

  void setBinaryStream(int p0, java.io.InputStream p1, int p2) throws SQLException;

  void clearParameters() throws SQLException;

  void setObject(int p0, Object p1, int p2) throws SQLException;

  void setObject(int p0, Object p1) throws SQLException;

  boolean execute() throws SQLException;

  void addBatch() throws SQLException;

  void setCharacterStream(int p0, java.io.Reader p1, int p2) throws SQLException;

  void setRef(int p0, Ref p1) throws SQLException;

  void setBlob(int p0, Blob p1) throws SQLException;

  void setClob(int p0, Clob p1) throws SQLException;

  void setArray(int p0, Array p1) throws SQLException;

  ResultSetMetaData getMetaData() throws SQLException;

  void setDate(int p0, Date p1, java.util.Calendar p2) throws SQLException;

  void setTime(int p0, Time p1, java.util.Calendar p2) throws SQLException;

  void setTimestamp(int p0, Timestamp p1, java.util.Calendar p2) throws SQLException;

  void setNull(int p0, int p1, String p2) throws SQLException;

  void setURL(int p0, java.net.URL p1) throws SQLException;

  ParameterMetaData getParameterMetaData() throws SQLException;

  void setRowId(int p0, RowId p1) throws SQLException;

  void setNString(int p0, String p1) throws SQLException;

  void setNCharacterStream(int p0, java.io.Reader p1, long p2) throws SQLException;

  void setNClob(int p0, NClob p1) throws SQLException;

  void setClob(int p0, java.io.Reader p1, long p2) throws SQLException;

  void setBlob(int p0, java.io.InputStream p1, long p2) throws SQLException;

  void setNClob(int p0, java.io.Reader p1, long p2) throws SQLException;

  void setSQLXML(int p0, SQLXML p1) throws SQLException;

  void setObject(int p0, Object p1, int p2, int p3) throws SQLException;

  void setAsciiStream(int p0, java.io.InputStream p1, long p2) throws SQLException;

  void setBinaryStream(int p0, java.io.InputStream p1, long p2) throws SQLException;

  void setCharacterStream(int p0, java.io.Reader p1, long p2) throws SQLException;

  void setAsciiStream(int p0, java.io.InputStream p1) throws SQLException;

  void setBinaryStream(int p0, java.io.InputStream p1) throws SQLException;

  void setCharacterStream(int p0, java.io.Reader p1) throws SQLException;

  void setNCharacterStream(int p0, java.io.Reader p1) throws SQLException;

  void setClob(int p0, java.io.Reader p1) throws SQLException;

  void setBlob(int p0, java.io.InputStream p1) throws SQLException;

  void setNClob(int p0, java.io.Reader p1) throws SQLException;

  default void setObject(int p0, Object p1, SQLType p2, int p3) throws SQLException {
    throw new SQLFeatureNotSupportedException("setObject not implemented");
  }

  default void setObject(int p0, Object p1, SQLType p2) throws SQLException {
    throw new SQLFeatureNotSupportedException("setObject not implemented");
  }

  default long executeLargeUpdate() throws SQLException {
    throw new UnsupportedOperationException("executeLargeUpdate not implemented");
  }
}
