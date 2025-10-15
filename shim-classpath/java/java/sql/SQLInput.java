/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface SQLInput {
  String readString() throws SQLException;

  boolean readBoolean() throws SQLException;

  byte readByte() throws SQLException;

  short readShort() throws SQLException;

  int readInt() throws SQLException;

  long readLong() throws SQLException;

  float readFloat() throws SQLException;

  double readDouble() throws SQLException;

  java.math.BigDecimal readBigDecimal() throws SQLException;

  byte[] readBytes() throws SQLException;

  Date readDate() throws SQLException;

  Time readTime() throws SQLException;

  Timestamp readTimestamp() throws SQLException;

  java.io.Reader readCharacterStream() throws SQLException;

  java.io.InputStream readAsciiStream() throws SQLException;

  java.io.InputStream readBinaryStream() throws SQLException;

  Object readObject() throws SQLException;

  Ref readRef() throws SQLException;

  Blob readBlob() throws SQLException;

  Clob readClob() throws SQLException;

  Array readArray() throws SQLException;

  boolean wasNull() throws SQLException;

  java.net.URL readURL() throws SQLException;

  NClob readNClob() throws SQLException;

  String readNString() throws SQLException;

  SQLXML readSQLXML() throws SQLException;

  RowId readRowId() throws SQLException;

  default <T> T readObject(Class<T> p0) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }
}
