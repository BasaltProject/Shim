/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.rowset.internal;

import java.io.Writer;
import java.sql.SQLException;
import javax.sql.RowSetInternal;
import javax.sql.rowset.WebRowSet;
import javax.sql.rowset.spi.XmlWriter;

public class WebRowSetXmlWriter implements XmlWriter, java.io.Serializable {
  private static final long serialVersionUID = 7163134986189677641L;

  public void writeXML(WebRowSet caller, Writer wrt) throws SQLException {
    WebRowSetXml.write(caller, wrt);
  }

  public boolean writeData(RowSetInternal caller) throws SQLException {
    throw new SQLException("Cannot write data to a datasource using the XML provider");
  }
}
