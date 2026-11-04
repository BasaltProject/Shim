/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.rowset.internal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import javax.sql.RowSetInternal;
import javax.sql.RowSetReader;
import com.sun.rowset.CachedRowSetImpl;

public class CachedRowSetReader implements RowSetReader, java.io.Serializable {
  private static final long serialVersionUID = 5049738185801363801L;

  public void readData(RowSetInternal caller) throws SQLException {
    Connection con = null;
    PreparedStatement stmt = null;
    boolean ownConnection = false;
    try {
      con = caller.getConnection();
      javax.sql.RowSet rs = (javax.sql.RowSet) caller;
      if (con == null) {
        con = connect(rs);
        ownConnection = true;
      }
      String command = rs.getCommand();
      if (command == null) {
        throw new SQLException("Result could not be generated");
      }
      stmt = con.prepareStatement(command, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      Object[] params = caller.getParams();
      for (int i = 0; i < params.length; i++) {
        setParam(stmt, i + 1, params[i]);
      }
      try {
        stmt.setMaxRows(rs.getMaxRows());
        stmt.setMaxFieldSize(rs.getMaxFieldSize());
        stmt.setEscapeProcessing(rs.getEscapeProcessing());
        stmt.setQueryTimeout(rs.getQueryTimeout());
      } catch (SQLException e) {
      }
      ResultSet data = stmt.executeQuery();
      ((CachedRowSetImpl) caller).populate(data);
      if (!ownConnection && false) {
        con.close();
      }
    } finally {
      if (stmt != null) {
        stmt.close();
      }
      if (ownConnection && con != null) {
        con.close();
      }
    }
  }

  static Connection connect(javax.sql.RowSet rs) throws SQLException {
    if (rs.getDataSourceName() != null) {
      try {
        javax.naming.Context ctx = new javax.naming.InitialContext();
        DataSource ds = (DataSource) ctx.lookup(rs.getDataSourceName());
        if (rs.getUsername() != null) {
          return ds.getConnection(rs.getUsername(), rs.getPassword());
        }
        return ds.getConnection();
      } catch (javax.naming.NamingException ex) {
        throw new SQLException("Unable to connect through the DataSource: " + ex.getMessage());
      }
    } else if (rs.getUrl() != null) {
      return DriverManager.getConnection(rs.getUrl(), rs.getUsername(), rs.getPassword());
    }
    throw new SQLException("Unable to connect to the database: no URL or DataSource name was set");
  }

  public static void setParamPublic(PreparedStatement stmt, int idx, Object param) throws SQLException {
    setParam(stmt, idx, param);
  }

  static void setParam(PreparedStatement stmt, int idx, Object param) throws SQLException {
    if (param instanceof Object[]) {
      Object[] a = (Object[]) param;
      if (a.length == 2 && a[0] == null && a[1] instanceof Integer) {
        stmt.setNull(idx, ((Integer) a[1]).intValue());
      } else if (a.length == 3 && a[0] == null && a[1] instanceof Integer && a[2] instanceof String) {
        stmt.setNull(idx, ((Integer) a[1]).intValue(), (String) a[2]);
      } else if (a.length == 2 && a[0] instanceof java.io.InputStream) {
        stmt.setBinaryStream(idx, (java.io.InputStream) a[0], ((Number) a[1]).intValue());
      } else if (a.length == 2 && a[0] instanceof java.io.Reader) {
        stmt.setCharacterStream(idx, (java.io.Reader) a[0], ((Number) a[1]).intValue());
      } else if (a.length == 2 && a[0] instanceof java.util.Date && a[1] instanceof java.util.Calendar) {
        if (a[0] instanceof java.sql.Timestamp) {
          stmt.setTimestamp(idx, (java.sql.Timestamp) a[0], (java.util.Calendar) a[1]);
        } else if (a[0] instanceof java.sql.Time) {
          stmt.setTime(idx, (java.sql.Time) a[0], (java.util.Calendar) a[1]);
        } else {
          stmt.setDate(idx, (java.sql.Date) a[0], (java.util.Calendar) a[1]);
        }
      } else if (a.length == 2 && a[1] instanceof Integer) {
        stmt.setObject(idx, a[0], ((Integer) a[1]).intValue());
      } else if (a.length == 3 && a[1] instanceof Integer && a[2] instanceof Integer) {
        stmt.setObject(idx, a[0], ((Integer) a[1]).intValue(), ((Integer) a[2]).intValue());
      } else {
        stmt.setObject(idx, param);
      }
    } else {
      stmt.setObject(idx, param);
    }
  }
}
