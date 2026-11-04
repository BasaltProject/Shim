/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.rowset.internal;

import java.io.Reader;
import java.sql.SQLException;
import javax.sql.RowSetInternal;
import javax.sql.rowset.WebRowSet;
import javax.sql.rowset.spi.XmlReader;

public class WebRowSetXmlReader implements XmlReader, java.io.Serializable {
  private static final long serialVersionUID = -9127058392819008014L;

  public void readXML(WebRowSet caller, Reader reader) throws SQLException {
    WebRowSetXml.read(caller, reader);
  }

  public void readData(RowSetInternal caller) throws SQLException {
    throw new SQLException("Cannot read data using the XML provider without a Reader");
  }
}
