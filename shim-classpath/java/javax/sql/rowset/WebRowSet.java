/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset;

public interface WebRowSet extends javax.sql.rowset.CachedRowSet {
  java.lang.String PUBLIC_XML_SCHEMA = "--//Oracle Corporation//XSD Schema//EN";
  java.lang.String SCHEMA_SYSTEM_ID = "http://java.sun.com/xml/ns/jdbc/webrowset.xsd";

  void readXml(java.io.InputStream inputStream) throws java.sql.SQLException, java.io.IOException;
  void readXml(java.io.Reader reader) throws java.sql.SQLException;
  void writeXml(java.io.OutputStream outputStream) throws java.sql.SQLException, java.io.IOException;
  void writeXml(java.io.Writer writer) throws java.sql.SQLException;
  void writeXml(java.sql.ResultSet resultSet, java.io.OutputStream outputStream) throws java.sql.SQLException, java.io.IOException;
  void writeXml(java.sql.ResultSet resultSet, java.io.Writer writer) throws java.sql.SQLException;
}
