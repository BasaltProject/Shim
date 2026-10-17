/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface ResultSet extends Wrapper, AutoCloseable {
  int FETCH_FORWARD = 1000;
  int FETCH_REVERSE = 1001;
  int FETCH_UNKNOWN = 1002;
  int TYPE_FORWARD_ONLY = 1003;
  int TYPE_SCROLL_INSENSITIVE = 1004;
  int TYPE_SCROLL_SENSITIVE = 1005;
  int CONCUR_READ_ONLY = 1007;
  int CONCUR_UPDATABLE = 1008;
  int HOLD_CURSORS_OVER_COMMIT = 1;
  int CLOSE_CURSORS_AT_COMMIT = 2;

  boolean next() throws SQLException;

  void close() throws SQLException;

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

  java.io.InputStream getAsciiStream(int p0) throws SQLException;

  java.io.InputStream getUnicodeStream(int p0) throws SQLException;

  java.io.InputStream getBinaryStream(int p0) throws SQLException;

  String getString(String p0) throws SQLException;

  boolean getBoolean(String p0) throws SQLException;

  byte getByte(String p0) throws SQLException;

  short getShort(String p0) throws SQLException;

  int getInt(String p0) throws SQLException;

  long getLong(String p0) throws SQLException;

  float getFloat(String p0) throws SQLException;

  double getDouble(String p0) throws SQLException;

  java.math.BigDecimal getBigDecimal(String p0, int p1) throws SQLException;

  byte[] getBytes(String p0) throws SQLException;

  Date getDate(String p0) throws SQLException;

  Time getTime(String p0) throws SQLException;

  Timestamp getTimestamp(String p0) throws SQLException;

  java.io.InputStream getAsciiStream(String p0) throws SQLException;

  java.io.InputStream getUnicodeStream(String p0) throws SQLException;

  java.io.InputStream getBinaryStream(String p0) throws SQLException;

  SQLWarning getWarnings() throws SQLException;

  void clearWarnings() throws SQLException;

  String getCursorName() throws SQLException;

  ResultSetMetaData getMetaData() throws SQLException;

  Object getObject(int p0) throws SQLException;

  Object getObject(String p0) throws SQLException;

  int findColumn(String p0) throws SQLException;

  java.io.Reader getCharacterStream(int p0) throws SQLException;

  java.io.Reader getCharacterStream(String p0) throws SQLException;

  java.math.BigDecimal getBigDecimal(int p0) throws SQLException;

  java.math.BigDecimal getBigDecimal(String p0) throws SQLException;

  boolean isBeforeFirst() throws SQLException;

  boolean isAfterLast() throws SQLException;

  boolean isFirst() throws SQLException;

  boolean isLast() throws SQLException;

  void beforeFirst() throws SQLException;

  void afterLast() throws SQLException;

  boolean first() throws SQLException;

  boolean last() throws SQLException;

  int getRow() throws SQLException;

  boolean absolute(int p0) throws SQLException;

  boolean relative(int p0) throws SQLException;

  boolean previous() throws SQLException;

  void setFetchDirection(int p0) throws SQLException;

  int getFetchDirection() throws SQLException;

  void setFetchSize(int p0) throws SQLException;

  int getFetchSize() throws SQLException;

  int getType() throws SQLException;

  int getConcurrency() throws SQLException;

  boolean rowUpdated() throws SQLException;

  boolean rowInserted() throws SQLException;

  boolean rowDeleted() throws SQLException;

  void updateNull(int p0) throws SQLException;

  void updateBoolean(int p0, boolean p1) throws SQLException;

  void updateByte(int p0, byte p1) throws SQLException;

  void updateShort(int p0, short p1) throws SQLException;

  void updateInt(int p0, int p1) throws SQLException;

  void updateLong(int p0, long p1) throws SQLException;

  void updateFloat(int p0, float p1) throws SQLException;

  void updateDouble(int p0, double p1) throws SQLException;

  void updateBigDecimal(int p0, java.math.BigDecimal p1) throws SQLException;

  void updateString(int p0, String p1) throws SQLException;

  void updateBytes(int p0, byte[] p1) throws SQLException;

  void updateDate(int p0, Date p1) throws SQLException;

  void updateTime(int p0, Time p1) throws SQLException;

  void updateTimestamp(int p0, Timestamp p1) throws SQLException;

  void updateAsciiStream(int p0, java.io.InputStream p1, int p2) throws SQLException;

  void updateBinaryStream(int p0, java.io.InputStream p1, int p2) throws SQLException;

  void updateCharacterStream(int p0, java.io.Reader p1, int p2) throws SQLException;

  void updateObject(int p0, Object p1, int p2) throws SQLException;

  void updateObject(int p0, Object p1) throws SQLException;

  void updateNull(String p0) throws SQLException;

  void updateBoolean(String p0, boolean p1) throws SQLException;

  void updateByte(String p0, byte p1) throws SQLException;

  void updateShort(String p0, short p1) throws SQLException;

  void updateInt(String p0, int p1) throws SQLException;

  void updateLong(String p0, long p1) throws SQLException;

  void updateFloat(String p0, float p1) throws SQLException;

  void updateDouble(String p0, double p1) throws SQLException;

  void updateBigDecimal(String p0, java.math.BigDecimal p1) throws SQLException;

  void updateString(String p0, String p1) throws SQLException;

  void updateBytes(String p0, byte[] p1) throws SQLException;

  void updateDate(String p0, Date p1) throws SQLException;

  void updateTime(String p0, Time p1) throws SQLException;

  void updateTimestamp(String p0, Timestamp p1) throws SQLException;

  void updateAsciiStream(String p0, java.io.InputStream p1, int p2) throws SQLException;

  void updateBinaryStream(String p0, java.io.InputStream p1, int p2) throws SQLException;

  void updateCharacterStream(String p0, java.io.Reader p1, int p2) throws SQLException;

  void updateObject(String p0, Object p1, int p2) throws SQLException;

  void updateObject(String p0, Object p1) throws SQLException;

  void insertRow() throws SQLException;

  void updateRow() throws SQLException;

  void deleteRow() throws SQLException;

  void refreshRow() throws SQLException;

  void cancelRowUpdates() throws SQLException;

  void moveToInsertRow() throws SQLException;

  void moveToCurrentRow() throws SQLException;

  Statement getStatement() throws SQLException;

  Object getObject(int p0, java.util.Map<String, Class<?>> p1) throws SQLException;

  Ref getRef(int p0) throws SQLException;

  Blob getBlob(int p0) throws SQLException;

  Clob getClob(int p0) throws SQLException;

  Array getArray(int p0) throws SQLException;

  Object getObject(String p0, java.util.Map<String, Class<?>> p1) throws SQLException;

  Ref getRef(String p0) throws SQLException;

  Blob getBlob(String p0) throws SQLException;

  Clob getClob(String p0) throws SQLException;

  Array getArray(String p0) throws SQLException;

  Date getDate(int p0, java.util.Calendar p1) throws SQLException;

  Date getDate(String p0, java.util.Calendar p1) throws SQLException;

  Time getTime(int p0, java.util.Calendar p1) throws SQLException;

  Time getTime(String p0, java.util.Calendar p1) throws SQLException;

  Timestamp getTimestamp(int p0, java.util.Calendar p1) throws SQLException;

  Timestamp getTimestamp(String p0, java.util.Calendar p1) throws SQLException;

  java.net.URL getURL(int p0) throws SQLException;

  java.net.URL getURL(String p0) throws SQLException;

  void updateRef(int p0, Ref p1) throws SQLException;

  void updateRef(String p0, Ref p1) throws SQLException;

  void updateBlob(int p0, Blob p1) throws SQLException;

  void updateBlob(String p0, Blob p1) throws SQLException;

  void updateClob(int p0, Clob p1) throws SQLException;

  void updateClob(String p0, Clob p1) throws SQLException;

  void updateArray(int p0, Array p1) throws SQLException;

  void updateArray(String p0, Array p1) throws SQLException;

  RowId getRowId(int p0) throws SQLException;

  RowId getRowId(String p0) throws SQLException;

  void updateRowId(int p0, RowId p1) throws SQLException;

  void updateRowId(String p0, RowId p1) throws SQLException;

  int getHoldability() throws SQLException;

  boolean isClosed() throws SQLException;

  void updateNString(int p0, String p1) throws SQLException;

  void updateNString(String p0, String p1) throws SQLException;

  void updateNClob(int p0, NClob p1) throws SQLException;

  void updateNClob(String p0, NClob p1) throws SQLException;

  NClob getNClob(int p0) throws SQLException;

  NClob getNClob(String p0) throws SQLException;

  SQLXML getSQLXML(int p0) throws SQLException;

  SQLXML getSQLXML(String p0) throws SQLException;

  void updateSQLXML(int p0, SQLXML p1) throws SQLException;

  void updateSQLXML(String p0, SQLXML p1) throws SQLException;

  String getNString(int p0) throws SQLException;

  String getNString(String p0) throws SQLException;

  java.io.Reader getNCharacterStream(int p0) throws SQLException;

  java.io.Reader getNCharacterStream(String p0) throws SQLException;

  void updateNCharacterStream(int p0, java.io.Reader p1, long p2) throws SQLException;

  void updateNCharacterStream(String p0, java.io.Reader p1, long p2) throws SQLException;

  void updateAsciiStream(int p0, java.io.InputStream p1, long p2) throws SQLException;

  void updateBinaryStream(int p0, java.io.InputStream p1, long p2) throws SQLException;

  void updateCharacterStream(int p0, java.io.Reader p1, long p2) throws SQLException;

  void updateAsciiStream(String p0, java.io.InputStream p1, long p2) throws SQLException;

  void updateBinaryStream(String p0, java.io.InputStream p1, long p2) throws SQLException;

  void updateCharacterStream(String p0, java.io.Reader p1, long p2) throws SQLException;

  void updateBlob(int p0, java.io.InputStream p1, long p2) throws SQLException;

  void updateBlob(String p0, java.io.InputStream p1, long p2) throws SQLException;

  void updateClob(int p0, java.io.Reader p1, long p2) throws SQLException;

  void updateClob(String p0, java.io.Reader p1, long p2) throws SQLException;

  void updateNClob(int p0, java.io.Reader p1, long p2) throws SQLException;

  void updateNClob(String p0, java.io.Reader p1, long p2) throws SQLException;

  void updateNCharacterStream(int p0, java.io.Reader p1) throws SQLException;

  void updateNCharacterStream(String p0, java.io.Reader p1) throws SQLException;

  void updateAsciiStream(int p0, java.io.InputStream p1) throws SQLException;

  void updateBinaryStream(int p0, java.io.InputStream p1) throws SQLException;

  void updateCharacterStream(int p0, java.io.Reader p1) throws SQLException;

  void updateAsciiStream(String p0, java.io.InputStream p1) throws SQLException;

  void updateBinaryStream(String p0, java.io.InputStream p1) throws SQLException;

  void updateCharacterStream(String p0, java.io.Reader p1) throws SQLException;

  void updateBlob(int p0, java.io.InputStream p1) throws SQLException;

  void updateBlob(String p0, java.io.InputStream p1) throws SQLException;

  void updateClob(int p0, java.io.Reader p1) throws SQLException;

  void updateClob(String p0, java.io.Reader p1) throws SQLException;

  void updateNClob(int p0, java.io.Reader p1) throws SQLException;

  void updateNClob(String p0, java.io.Reader p1) throws SQLException;

  <T> T getObject(int p0, Class<T> p1) throws SQLException;

  <T> T getObject(String p0, Class<T> p1) throws SQLException;

  default void updateObject(int p0, Object p1, SQLType p2, int p3) throws SQLException {
    throw new SQLFeatureNotSupportedException("updateObject not implemented");
  }

  default void updateObject(String p0, Object p1, SQLType p2, int p3) throws SQLException {
    throw new SQLFeatureNotSupportedException("updateObject not implemented");
  }

  default void updateObject(int p0, Object p1, SQLType p2) throws SQLException {
    throw new SQLFeatureNotSupportedException("updateObject not implemented");
  }

  default void updateObject(String p0, Object p1, SQLType p2) throws SQLException {
    throw new SQLFeatureNotSupportedException("updateObject not implemented");
  }
}
