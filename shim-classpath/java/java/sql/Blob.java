/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface Blob {
  long length() throws SQLException;

  byte[] getBytes(long p0, int p1) throws SQLException;

  java.io.InputStream getBinaryStream() throws SQLException;

  long position(byte[] p0, long p1) throws SQLException;

  long position(Blob p0, long p1) throws SQLException;

  int setBytes(long p0, byte[] p1) throws SQLException;

  int setBytes(long p0, byte[] p1, int p2, int p3) throws SQLException;

  java.io.OutputStream setBinaryStream(long p0) throws SQLException;

  void truncate(long p0) throws SQLException;

  void free() throws SQLException;

  java.io.InputStream getBinaryStream(long p0, long p1) throws SQLException;
}
