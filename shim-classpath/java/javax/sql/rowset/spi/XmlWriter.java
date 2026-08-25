/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset.spi;

public interface XmlWriter extends javax.sql.RowSetWriter {
  void writeXML(javax.sql.rowset.WebRowSet webRowSet, java.io.Writer writer) throws java.sql.SQLException;
}
