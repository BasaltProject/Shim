/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.rowset;

import java.sql.SQLException;
import javax.sql.rowset.FilteredRowSet;
import javax.sql.rowset.Predicate;

public class FilteredRowSetImpl extends WebRowSetImpl implements FilteredRowSet {
  private static final long serialVersionUID = 6178454588413509360L;

  private Predicate p;

  public FilteredRowSetImpl() throws SQLException {
    super();
  }

  public FilteredRowSetImpl(java.util.Hashtable<?, ?> env) throws SQLException {
    super(env);
  }

  public void setFilter(Predicate p) throws SQLException {
    this.p = p;
  }

  public Predicate getFilter() {
    return p;
  }

  @Override
  boolean accept(int i) {
    if (p == null) {
      return true;
    }
    int saved = cursor;
    boolean savedInsert = false;
    cursor = i;
    try {
      return p.evaluate(this);
    } finally {
      cursor = saved;
    }
  }

  @Override
  public void updateObject(int columnIndex, Object x) throws SQLException {
    if (p != null && !p.evaluate(x, columnIndex)) {
      throw new SQLException("Value is not allowed by the filter");
    }
    super.updateObject(columnIndex, x);
  }
}
