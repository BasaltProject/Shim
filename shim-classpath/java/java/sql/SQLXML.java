/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface SQLXML {
  void free() throws SQLException;

  java.io.InputStream getBinaryStream() throws SQLException;

  java.io.OutputStream setBinaryStream() throws SQLException;

  java.io.Reader getCharacterStream() throws SQLException;

  java.io.Writer setCharacterStream() throws SQLException;

  String getString() throws SQLException;

  void setString(String p0) throws SQLException;

  <T extends javax.xml.transform.Source> T getSource(Class<T> p0) throws SQLException;

  <T extends javax.xml.transform.Result> T setResult(Class<T> p0) throws SQLException;
}
