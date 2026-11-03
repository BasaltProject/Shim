/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset.spi;

public interface XmlReader extends javax.sql.RowSetReader {
  void readXML(javax.sql.rowset.WebRowSet webRowSet, java.io.Reader reader) throws java.sql.SQLException;
}
