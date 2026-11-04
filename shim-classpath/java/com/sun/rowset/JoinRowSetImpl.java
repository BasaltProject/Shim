/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.rowset;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.RowSet;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.JoinRowSet;
import javax.sql.rowset.Joinable;
import javax.sql.rowset.RowSetMetaDataImpl;

public class JoinRowSetImpl extends WebRowSetImpl implements JoinRowSet {
  private static final long serialVersionUID = -5590501621560008453L;

  private final List<CachedRowSetImpl> sets = new ArrayList<CachedRowSetImpl>();
  private final List<Integer> matchIdx = new ArrayList<Integer>();
  private final List<String> names = new ArrayList<String>();
  private int joinType = INNER_JOIN;
  private String whereClause = "";

  public JoinRowSetImpl() throws SQLException {
    super();
  }

  public void addRowSet(Joinable rowset) throws SQLException {
    if (!(rowset instanceof RowSet)) {
      throw new SQLException("Only RowSet objects can be added");
    }
    int[] cols = rowset.getMatchColumnIndexes();
    if (cols.length == 0 || cols[0] < 1) {
      throw new SQLException("Match column must be set before adding to a JoinRowSet");
    }
    add((RowSet) rowset, cols[0]);
  }

  public void addRowSet(RowSet rowset, int columnIdx) throws SQLException {
    if (rowset instanceof Joinable) {
      ((Joinable) rowset).setMatchColumn(columnIdx);
    }
    add(rowset, columnIdx);
  }

  public void addRowSet(RowSet rowset, String columnName) throws SQLException {
    int idx = rowset.findColumn(columnName);
    if (rowset instanceof Joinable) {
      ((Joinable) rowset).setMatchColumn(idx);
    }
    add(rowset, idx);
  }

  public void addRowSet(RowSet[] rowset, int[] columnIdx) throws SQLException {
    if (rowset.length != columnIdx.length) {
      throw new SQLException("Number of row sets and match columns differ");
    }
    for (int i = 0; i < rowset.length; i++) {
      addRowSet(rowset[i], columnIdx[i]);
    }
  }

  public void addRowSet(RowSet[] rowset, String[] columnName) throws SQLException {
    if (rowset.length != columnName.length) {
      throw new SQLException("Number of row sets and match columns differ");
    }
    for (int i = 0; i < rowset.length; i++) {
      addRowSet(rowset[i], columnName[i]);
    }
  }

  private void add(RowSet rowset, int idx) throws SQLException {
    CachedRowSetImpl crs;
    if (rowset instanceof CachedRowSetImpl) {
      crs = (CachedRowSetImpl) ((CachedRowSetImpl) rowset).createCopy();
    } else {
      crs = new CachedRowSetImpl();
      crs.populate(rowset);
    }
    sets.add(crs);
    matchIdx.add(Integer.valueOf(idx));
    String t = crs.getTableName();
    names.add(t == null ? "Joinable" + sets.size() : t);
    rebuild();
  }

  private void rebuild() throws SQLException {
    CachedRowSetImpl acc = sets.get(0);
    int accMatch = matchIdx.get(0).intValue();
    List<Object[]> rowsOut = new ArrayList<Object[]>();
    ResultSetMetaData m0 = acc.getMetaData();
    int accCols = m0.getColumnCount();
    List<Object[]> accRows = new ArrayList<Object[]>();
    for (CachedRowSetImpl.Row r : acc.rowsView()) {
      accRows.add(r.cur.clone());
    }
    RowSetMetaDataImpl md = new RowSetMetaDataImpl();
    int total = accCols;
    for (int s = 1; s < sets.size(); s++) {
      total += sets.get(s).getMetaData().getColumnCount();
    }
    md.setColumnCount(total);
    int col = 1;
    for (int s = 0; s < sets.size(); s++) {
      ResultSetMetaData sm = sets.get(s).getMetaData();
      for (int i = 1; i <= sm.getColumnCount(); i++, col++) {
        md.setColumnName(col, sm.getColumnName(i));
        md.setColumnLabel(col, sm.getColumnLabel(i));
        md.setColumnType(col, sm.getColumnType(i));
        md.setColumnTypeName(col, sm.getColumnTypeName(i));
        md.setTableName(col, names.get(s));
        md.setNullable(col, sm.isNullable(i));
      }
    }
    int width = accCols;
    for (int s = 1; s < sets.size(); s++) {
      CachedRowSetImpl other = sets.get(s);
      int oMatch = matchIdx.get(s).intValue();
      List<Object[]> next = new ArrayList<Object[]>();
      int ow = other.getMetaData().getColumnCount();
      for (Object[] a : accRows) {
        for (CachedRowSetImpl.Row b : other.rowsView()) {
          Object av = a[accMatch - 1];
          Object bv = b.cur[oMatch - 1];
          if (av != null && av.equals(bv)) {
            Object[] j = new Object[width + ow];
            System.arraycopy(a, 0, j, 0, width);
            System.arraycopy(b.cur, 0, j, width, ow);
            next.add(j);
          }
        }
      }
      accRows = next;
      width += ow;
    }
    super.release();
    setMetaData(md);
    loadRows(accRows, null, null);
    whereClause = "";
  }

  public List<CachedRowSetImpl> rowSetList() {
    return sets;
  }

  public java.util.Collection<?> getRowSets() throws SQLException {
    return new ArrayList<CachedRowSetImpl>(sets);
  }

  public String[] getRowSetNames() throws SQLException {
    return names.toArray(new String[0]);
  }

  public CachedRowSet toCachedRowSet() throws SQLException {
    return createCopy();
  }

  public boolean supportsCrossJoin() {
    return false;
  }

  public boolean supportsInnerJoin() {
    return true;
  }

  public boolean supportsLeftOuterJoin() {
    return false;
  }

  public boolean supportsRightOuterJoin() {
    return false;
  }

  public boolean supportsFullJoin() {
    return false;
  }

  public void setJoinType(int joinType) throws SQLException {
    if (joinType >= CROSS_JOIN && joinType <= FULL_JOIN) {
      if (joinType != INNER_JOIN) {
        throw new SQLException("Join type not supported");
      }
      this.joinType = joinType;
    } else {
      throw new SQLException("Invalid join type");
    }
  }

  public String getWhereClause() throws SQLException {
    StringBuilder b = new StringBuilder("Select * from ");
    for (int i = 0; i < names.size(); i++) {
      b.append(i > 0 ? ", " : "").append(names.get(i));
    }
    return b.toString();
  }

  public int getJoinType() throws SQLException {
    return joinType;
  }
}
