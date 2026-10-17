/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface Clob {
  long length() throws SQLException;

  String getSubString(long p0, int p1) throws SQLException;

  java.io.Reader getCharacterStream() throws SQLException;

  java.io.InputStream getAsciiStream() throws SQLException;

  long position(String p0, long p1) throws SQLException;

  long position(Clob p0, long p1) throws SQLException;

  int setString(long p0, String p1) throws SQLException;

  int setString(long p0, String p1, int p2, int p3) throws SQLException;

  java.io.OutputStream setAsciiStream(long p0) throws SQLException;

  java.io.Writer setCharacterStream(long p0) throws SQLException;

  void truncate(long p0) throws SQLException;

  void free() throws SQLException;

  java.io.Reader getCharacterStream(long p0, long p1) throws SQLException;
}
