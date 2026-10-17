/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface CallableStatement extends PreparedStatement {
  void registerOutParameter(int p0, int p1) throws SQLException;

  void registerOutParameter(int p0, int p1, int p2) throws SQLException;

  boolean wasNull() throws SQLException;

  String getString(int p0) throws SQLException;

  boolean getBoolean(int p0) throws SQLException;

  byte getByte(int p0) throws SQLException;

  short getShort(int p0) throws SQLException;

  int getInt(int p0) throws SQLException;

  long getLong(int p0) throws SQLException;

  float getFloat(int p0) throws SQLException;

  double getDouble(int p0) throws SQLException;

  java.math.BigDecimal getBigDecimal(int p0, int p1) throws SQLException;

  byte[] getBytes(int p0) throws SQLException;

  Date getDate(int p0) throws SQLException;

  Time getTime(int p0) throws SQLException;

  Timestamp getTimestamp(int p0) throws SQLException;

  Object getObject(int p0) throws SQLException;

  java.math.BigDecimal getBigDecimal(int p0) throws SQLException;

  Object getObject(int p0, java.util.Map<String, Class<?>> p1) throws SQLException;

  Ref getRef(int p0) throws SQLException;

  Blob getBlob(int p0) throws SQLException;

  Clob getClob(int p0) throws SQLException;

  Array getArray(int p0) throws SQLException;

  Date getDate(int p0, java.util.Calendar p1) throws SQLException;

  Time getTime(int p0, java.util.Calendar p1) throws SQLException;

  Timestamp getTimestamp(int p0, java.util.Calendar p1) throws SQLException;

  void registerOutParameter(int p0, int p1, String p2) throws SQLException;

  void registerOutParameter(String p0, int p1) throws SQLException;

  void registerOutParameter(String p0, int p1, int p2) throws SQLException;

  void registerOutParameter(String p0, int p1, String p2) throws SQLException;

  java.net.URL getURL(int p0) throws SQLException;

  void setURL(String p0, java.net.URL p1) throws SQLException;

  void setNull(String p0, int p1) throws SQLException;

  void setBoolean(String p0, boolean p1) throws SQLException;

  void setByte(String p0, byte p1) throws SQLException;

  void setShort(String p0, short p1) throws SQLException;

  void setInt(String p0, int p1) throws SQLException;

  void setLong(String p0, long p1) throws SQLException;

  void setFloat(String p0, float p1) throws SQLException;

  void setDouble(String p0, double p1) throws SQLException;

  void setBigDecimal(String p0, java.math.BigDecimal p1) throws SQLException;

  void setString(String p0, String p1) throws SQLException;

  void setBytes(String p0, byte[] p1) throws SQLException;

  void setDate(String p0, Date p1) throws SQLException;

  void setTime(String p0, Time p1) throws SQLException;

  void setTimestamp(String p0, Timestamp p1) throws SQLException;

  void setAsciiStream(String p0, java.io.InputStream p1, int p2) throws SQLException;

  void setBinaryStream(String p0, java.io.InputStream p1, int p2) throws SQLException;

  void setObject(String p0, Object p1, int p2, int p3) throws SQLException;

  void setObject(String p0, Object p1, int p2) throws SQLException;

  void setObject(String p0, Object p1) throws SQLException;

  void setCharacterStream(String p0, java.io.Reader p1, int p2) throws SQLException;

  void setDate(String p0, Date p1, java.util.Calendar p2) throws SQLException;

  void setTime(String p0, Time p1, java.util.Calendar p2) throws SQLException;

  void setTimestamp(String p0, Timestamp p1, java.util.Calendar p2) throws SQLException;

  void setNull(String p0, int p1, String p2) throws SQLException;

  String getString(String p0) throws SQLException;

  boolean getBoolean(String p0) throws SQLException;

  byte getByte(String p0) throws SQLException;

  short getShort(String p0) throws SQLException;

  int getInt(String p0) throws SQLException;

  long getLong(String p0) throws SQLException;

  float getFloat(String p0) throws SQLException;

  double getDouble(String p0) throws SQLException;

  byte[] getBytes(String p0) throws SQLException;

  Date getDate(String p0) throws SQLException;

  Time getTime(String p0) throws SQLException;

  Timestamp getTimestamp(String p0) throws SQLException;

  Object getObject(String p0) throws SQLException;

  java.math.BigDecimal getBigDecimal(String p0) throws SQLException;

  Object getObject(String p0, java.util.Map<String, Class<?>> p1) throws SQLException;

  Ref getRef(String p0) throws SQLException;

  Blob getBlob(String p0) throws SQLException;

  Clob getClob(String p0) throws SQLException;

  Array getArray(String p0) throws SQLException;

  Date getDate(String p0, java.util.Calendar p1) throws SQLException;

  Time getTime(String p0, java.util.Calendar p1) throws SQLException;

  Timestamp getTimestamp(String p0, java.util.Calendar p1) throws SQLException;

  java.net.URL getURL(String p0) throws SQLException;

  RowId getRowId(int p0) throws SQLException;

  RowId getRowId(String p0) throws SQLException;

  void setRowId(String p0, RowId p1) throws SQLException;

  void setNString(String p0, String p1) throws SQLException;

  void setNCharacterStream(String p0, java.io.Reader p1, long p2) throws SQLException;

  void setNClob(String p0, NClob p1) throws SQLException;

  void setClob(String p0, java.io.Reader p1, long p2) throws SQLException;

  void setBlob(String p0, java.io.InputStream p1, long p2) throws SQLException;

  void setNClob(String p0, java.io.Reader p1, long p2) throws SQLException;

  NClob getNClob(int p0) throws SQLException;

  NClob getNClob(String p0) throws SQLException;

  void setSQLXML(String p0, SQLXML p1) throws SQLException;

  SQLXML getSQLXML(int p0) throws SQLException;

  SQLXML getSQLXML(String p0) throws SQLException;

  String getNString(int p0) throws SQLException;

  String getNString(String p0) throws SQLException;

  java.io.Reader getNCharacterStream(int p0) throws SQLException;

  java.io.Reader getNCharacterStream(String p0) throws SQLException;

  java.io.Reader getCharacterStream(int p0) throws SQLException;

  java.io.Reader getCharacterStream(String p0) throws SQLException;

  void setBlob(String p0, Blob p1) throws SQLException;

  void setClob(String p0, Clob p1) throws SQLException;

  void setAsciiStream(String p0, java.io.InputStream p1, long p2) throws SQLException;

  void setBinaryStream(String p0, java.io.InputStream p1, long p2) throws SQLException;

  void setCharacterStream(String p0, java.io.Reader p1, long p2) throws SQLException;

  void setAsciiStream(String p0, java.io.InputStream p1) throws SQLException;

  void setBinaryStream(String p0, java.io.InputStream p1) throws SQLException;

  void setCharacterStream(String p0, java.io.Reader p1) throws SQLException;

  void setNCharacterStream(String p0, java.io.Reader p1) throws SQLException;

  void setClob(String p0, java.io.Reader p1) throws SQLException;

  void setBlob(String p0, java.io.InputStream p1) throws SQLException;

  void setNClob(String p0, java.io.Reader p1) throws SQLException;

  <T> T getObject(int p0, Class<T> p1) throws SQLException;

  <T> T getObject(String p0, Class<T> p1) throws SQLException;

  default void setObject(String p0, Object p1, SQLType p2, int p3) throws SQLException {
    throw new SQLFeatureNotSupportedException("setObject not implemented");
  }

  default void setObject(String p0, Object p1, SQLType p2) throws SQLException {
    throw new SQLFeatureNotSupportedException("setObject not implemented");
  }

  default void registerOutParameter(int p0, SQLType p1) throws SQLException {
    throw new SQLFeatureNotSupportedException("registerOutParameter not implemented");
  }

  default void registerOutParameter(int p0, SQLType p1, int p2) throws SQLException {
    throw new SQLFeatureNotSupportedException("registerOutParameter not implemented");
  }

  default void registerOutParameter(int p0, SQLType p1, String p2) throws SQLException {
    throw new SQLFeatureNotSupportedException("registerOutParameter not implemented");
  }

  default void registerOutParameter(String p0, SQLType p1) throws SQLException {
    throw new SQLFeatureNotSupportedException("registerOutParameter not implemented");
  }

  default void registerOutParameter(String p0, SQLType p1, int p2) throws SQLException {
    throw new SQLFeatureNotSupportedException("registerOutParameter not implemented");
  }

  default void registerOutParameter(String p0, SQLType p1, String p2) throws SQLException {
    throw new SQLFeatureNotSupportedException("registerOutParameter not implemented");
  }
}
