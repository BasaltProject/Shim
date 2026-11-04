/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.rowset.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.RowSetInternal;
import javax.sql.rowset.spi.SyncProviderException;
import javax.sql.rowset.spi.TransactionalWriter;
import com.sun.rowset.CachedRowSetImpl;

public class CachedRowSetWriter implements TransactionalWriter, java.io.Serializable {
  private static final long serialVersionUID = 1489979042494221568L;

  private Connection con;

  public boolean writeData(RowSetInternal caller) throws SQLException {
    CachedRowSetImpl crs = (CachedRowSetImpl) caller;
    con = caller.getConnection();
    if (con == null) {
      con = CachedRowSetReader.connect(crs);
    }
    ResultSetMetaData md = crs.getMetaData();
    String table = crs.getTableName();
    if (table == null) {
      table = md.getTableName(1);
    }
    if (table == null || table.length() == 0) {
      throw new SQLException("Table name is not set; call setTableName before acceptChanges");
    }
    int n = md.getColumnCount();
    int[] keys = crs.getKeyColumns();
    boolean ok = true;
    for (CachedRowSetImpl.Row r : crs.rowsView()) {
      if (r.deleted && r.inserted) {
        continue;
      }
      if (r.inserted) {
        ok &= insert(table, md, n, r);
      } else if (r.deleted) {
        ok &= delete(table, md, n, keys, r);
      } else if (r.updated) {
        ok &= update(table, md, n, keys, r);
      }
    }
    return ok;
  }

  private boolean insert(String table, ResultSetMetaData md, int n, CachedRowSetImpl.Row r) throws SQLException {
    StringBuilder cols = new StringBuilder();
    StringBuilder qs = new StringBuilder();
    List<Object> vals = new ArrayList<Object>();
    for (int i = 1; i <= n; i++) {
      if (md.isAutoIncrement(i) && r.cur[i - 1] == null) {
        continue;
      }
      if (cols.length() > 0) {
        cols.append(", ");
        qs.append(", ");
      }
      cols.append(md.getColumnName(i));
      qs.append('?');
      vals.add(r.cur[i - 1]);
    }
    return exec("INSERT INTO " + table + " (" + cols + ") VALUES (" + qs + ")", vals, 1);
  }

  private boolean delete(String table, ResultSetMetaData md, int n, int[] keys, CachedRowSetImpl.Row r)
      throws SQLException {
    List<Object> vals = new ArrayList<Object>();
    String where = where(md, n, keys, r, vals);
    return exec("DELETE FROM " + table + " WHERE " + where, vals, 1);
  }

  private boolean update(String table, ResultSetMetaData md, int n, int[] keys, CachedRowSetImpl.Row r)
      throws SQLException {
    StringBuilder set = new StringBuilder();
    List<Object> vals = new ArrayList<Object>();
    for (int i = 1; i <= n; i++) {
      if (r.changed.get(i - 1)) {
        if (set.length() > 0) {
          set.append(", ");
        }
        set.append(md.getColumnName(i)).append(" = ?");
        vals.add(r.cur[i - 1]);
      }
    }
    if (set.length() == 0) {
      return true;
    }
    String where = where(md, n, keys, r, vals);
    return exec("UPDATE " + table + " SET " + set + " WHERE " + where, vals, 1);
  }

  private String where(ResultSetMetaData md, int n, int[] keys, CachedRowSetImpl.Row r, List<Object> vals)
      throws SQLException {
    StringBuilder w = new StringBuilder();
    List<Integer> cols = new ArrayList<Integer>();
    if (keys != null && keys.length > 0) {
      for (int k : keys) {
        cols.add(Integer.valueOf(k));
      }
    } else {
      for (int i = 1; i <= n; i++) {
        cols.add(Integer.valueOf(i));
      }
    }
    for (Integer c : cols) {
      if (w.length() > 0) {
        w.append(" AND ");
      }
      Object v = r.orig[c.intValue() - 1];
      if (v == null) {
        w.append(md.getColumnName(c.intValue())).append(" IS NULL");
      } else {
        w.append(md.getColumnName(c.intValue())).append(" = ?");
        vals.add(v);
      }
    }
    return w.toString();
  }

  private boolean exec(String sql, List<Object> vals, int expected) throws SQLException {
    PreparedStatement ps = con.prepareStatement(sql);
    try {
      for (int i = 0; i < vals.size(); i++) {
        ps.setObject(i + 1, vals.get(i));
      }
      int c = ps.executeUpdate();
      if (c != expected) {
        throw new SyncProviderException("Row conflict detected while executing: " + sql);
      }
      return true;
    } finally {
      ps.close();
    }
  }

  public void commit() throws SQLException {
    if (con != null) {
      con.commit();
    }
  }

  public void rollback() throws SQLException {
    if (con != null) {
      con.rollback();
    }
  }

  public void rollback(java.sql.Savepoint s) throws SQLException {
    if (con != null) {
      con.rollback(s);
    }
  }
}
