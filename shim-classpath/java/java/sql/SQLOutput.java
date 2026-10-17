/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface SQLOutput {
  void writeString(String p0) throws SQLException;

  void writeBoolean(boolean p0) throws SQLException;

  void writeByte(byte p0) throws SQLException;

  void writeShort(short p0) throws SQLException;

  void writeInt(int p0) throws SQLException;

  void writeLong(long p0) throws SQLException;

  void writeFloat(float p0) throws SQLException;

  void writeDouble(double p0) throws SQLException;

  void writeBigDecimal(java.math.BigDecimal p0) throws SQLException;

  void writeBytes(byte[] p0) throws SQLException;

  void writeDate(Date p0) throws SQLException;

  void writeTime(Time p0) throws SQLException;

  void writeTimestamp(Timestamp p0) throws SQLException;

  void writeCharacterStream(java.io.Reader p0) throws SQLException;

  void writeAsciiStream(java.io.InputStream p0) throws SQLException;

  void writeBinaryStream(java.io.InputStream p0) throws SQLException;

  void writeObject(SQLData p0) throws SQLException;

  void writeRef(Ref p0) throws SQLException;

  void writeBlob(Blob p0) throws SQLException;

  void writeClob(Clob p0) throws SQLException;

  void writeStruct(Struct p0) throws SQLException;

  void writeArray(Array p0) throws SQLException;

  void writeURL(java.net.URL p0) throws SQLException;

  void writeNString(String p0) throws SQLException;

  void writeNClob(NClob p0) throws SQLException;

  void writeRowId(RowId p0) throws SQLException;

  void writeSQLXML(SQLXML p0) throws SQLException;

  default void writeObject(Object p0, SQLType p1) throws SQLException {
    throw new SQLFeatureNotSupportedException();
  }
}
