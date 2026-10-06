/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.rowset;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.rowset.WebRowSet;
import com.sun.rowset.internal.WebRowSetXmlReader;
import com.sun.rowset.internal.WebRowSetXmlWriter;

public class WebRowSetImpl extends CachedRowSetImpl implements WebRowSet {
  private static final long serialVersionUID = -8771775154092422943L;

  public WebRowSetImpl() throws SQLException {
    super();
  }

  public WebRowSetImpl(java.util.Hashtable<?, ?> env) throws SQLException {
    super(env);
  }

  public void writeXml(ResultSet rs, Writer writer) throws SQLException {
    populate(rs);
    writeXml(writer);
  }

  public void writeXml(Writer writer) throws SQLException {
    new WebRowSetXmlWriter().writeXML(this, writer);
  }

  public void readXml(Reader reader) throws SQLException {
    new WebRowSetXmlReader().readXML(this, reader);
  }

  public void readXml(InputStream iStream) throws SQLException, IOException {
    if (iStream == null) {
      throw new SQLException("Invalid input stream handle");
    }
    readXml(new InputStreamReader(iStream, "UTF-8"));
  }

  public void writeXml(OutputStream oStream) throws SQLException, IOException {
    Writer w = new OutputStreamWriter(oStream, "UTF-8");
    writeXml(w);
    w.flush();
  }

  public void writeXml(ResultSet rs, OutputStream oStream) throws SQLException, IOException {
    populate(rs);
    writeXml(oStream);
  }
}
