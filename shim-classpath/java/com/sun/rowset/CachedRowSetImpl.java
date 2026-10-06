/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.rowset;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import javax.sql.RowSet;
import javax.sql.RowSetEvent;
import javax.sql.RowSetInternal;
import javax.sql.RowSetMetaData;
import javax.sql.rowset.BaseRowSet;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetWarning;
import javax.sql.rowset.spi.SyncFactory;
import javax.sql.rowset.spi.SyncFactoryException;
import javax.sql.rowset.spi.SyncProvider;
import javax.sql.rowset.spi.SyncProviderException;

public class CachedRowSetImpl extends BaseRowSet implements RowSet, RowSetInternal, Serializable, Cloneable, CachedRowSet {
  private static final long serialVersionUID = 1884577171200622428L;

  public static final class Row implements Serializable {
    private static final long serialVersionUID = 5047859032611314762L;

    public Object[] orig;
    public Object[] cur;
    public boolean inserted;
    public boolean updated;
    public boolean deleted;
    public BitSet changed;

    Row(Object[] values) {
      cur = values;
      orig = values.clone();
      changed = new BitSet(values.length);
    }

    Row copy() {
      Row r = new Row(cur.clone());
      r.orig = orig.clone();
      r.inserted = inserted;
      r.updated = updated;
      r.deleted = deleted;
      r.changed = (BitSet) changed.clone();
      return r;
    }
  }

  RowSetMetaDataImpl RowSetMD;
  private ArrayList<Row> rows = new ArrayList<Row>();
  int cursor = -1;
  private Row insertRow;
  private boolean onInsertRow;
  private boolean lastNull;
  boolean showDeleted;
  private int[] keyCols = new int[0];
  private String tableName;
  private transient SyncProvider provider;
  private transient Connection conn;
  private transient RowSetWarning rowsetWarning;
  private transient SQLWarning warnings;
  private int pageSize;
  private int pageStart = 1;
  private transient ResultSet pagedSource;
  private boolean closed;
  private int[] matchColumns = new int[0];
  private String[] matchColumnNames = new String[0];

  public CachedRowSetImpl() throws SQLException {
    initParams();
    try {
      provider = SyncFactory.getInstance("com.sun.rowset.providers.RIOptimisticProvider");
    } catch (SyncFactoryException e) {
      throw new SQLException(e.getMessage());
    }
    setType(ResultSet.TYPE_SCROLL_INSENSITIVE);
    setConcurrency(ResultSet.CONCUR_UPDATABLE);
    setReadOnly(true);
    setMaxRows(0);
  }

  public CachedRowSetImpl(java.util.Hashtable<?, ?> env) throws SQLException {
    this();
  }

  public void loadRows(List<Object[]> values, List<String> kinds, List<Object[]> updates) {
    rows = new ArrayList<Row>();
    cursor = -1;
    for (int i = 0; i < values.size(); i++) {
      Row r = new Row(values.get(i));
      String kind = kinds == null ? "currentRow" : kinds.get(i);
      if (kind.equals("insertRow")) {
        r.inserted = true;
        r.orig = new Object[r.cur.length];
      } else if (kind.equals("deleteRow")) {
        r.deleted = true;
      } else if (kind.equals("modifyRow") && updates != null) {
        boolean[] has = (boolean[]) updates.get(i)[0];
        for (int c = 0; c < has.length; c++) {
          if (has[c]) {
            r.cur[c] = updates.get(i)[c + 1];
            r.changed.set(c);
            r.updated = true;
          }
        }
      }
      rows.add(r);
    }
  }

  public java.util.List<Row> rowsView() {
    return rows;
  }

  public static final class RowAccess {
  }

  protected void checkOpen() throws SQLException {
    if (closed) {
      throw new SQLException("Operation not allowed on a closed RowSet");
    }
  }

  // population and execution

  public void populate(ResultSet data) throws SQLException {
    populate(data, 1);
  }

  public void populate(ResultSet data, int start) throws SQLException {
    if (data == null) {
      throw new SQLException("Invalid ResultSet object supplied to populate method");
    }
    ResultSetMetaData rsmd = data.getMetaData();
    RowSetMD = new RowSetMetaDataImpl();
    initMetaData(RowSetMD, rsmd);
    rows = new ArrayList<Row>();
    cursor = -1;
    int count = RowSetMD.getColumnCount();
    int max = getMaxRows();
    int n = 0;
    if (start > 1) {
      if (data.getType() == ResultSet.TYPE_FORWARD_ONLY) {
        for (int i = 1; i < start; i++) {
          if (!data.next()) {
            throw new SQLException("Start position beyond the end of the ResultSet");
          }
        }
      } else if (!data.absolute(start - 1)) {
        throw new SQLException("Start position beyond the end of the ResultSet");
      }
    }
    while (data.next()) {
      Object[] values = new Object[count];
      for (int i = 1; i <= count; i++) {
        Object v = data.getObject(i);
        if (v instanceof java.sql.Struct) {
          v = new javax.sql.rowset.serial.SerialStruct((java.sql.Struct) v, getTypeMap() == null ? new java.util.HashMap<String, Class<?>>() : getTypeMap());
        } else if (v instanceof java.sql.Array) {
          v = new javax.sql.rowset.serial.SerialArray((java.sql.Array) v, getTypeMap() == null ? new java.util.HashMap<String, Class<?>>() : getTypeMap());
        } else if (v instanceof java.sql.Blob) {
          v = new javax.sql.rowset.serial.SerialBlob((java.sql.Blob) v);
        } else if (v instanceof java.sql.Clob) {
          v = new javax.sql.rowset.serial.SerialClob((java.sql.Clob) v);
        }
        values[i - 1] = v;
      }
      rows.add(new Row(values));
      n++;
      if (max > 0 && n >= max) {
        break;
      }
      if (pageSize > 0 && n >= pageSize) {
        pagedSource = data;
        break;
      }
    }
    pageStart = start;
    if (pageSize == 0) {
      pagedSource = null;
    }
    notifyRowSetChanged();
  }

  private static void initMetaData(RowSetMetaDataImpl md, ResultSetMetaData rsmd) throws SQLException {
    int n = rsmd.getColumnCount();
    md.setColumnCount(n);
    for (int i = 1; i <= n; i++) {
      md.setAutoIncrement(i, rsmd.isAutoIncrement(i));
      md.setCaseSensitive(i, rsmd.isCaseSensitive(i));
      md.setCurrency(i, rsmd.isCurrency(i));
      md.setNullable(i, rsmd.isNullable(i));
      md.setSigned(i, rsmd.isSigned(i));
      md.setSearchable(i, rsmd.isSearchable(i));
      md.setColumnDisplaySize(i, rsmd.getColumnDisplaySize(i));
      md.setColumnLabel(i, rsmd.getColumnLabel(i));
      md.setColumnName(i, rsmd.getColumnName(i));
      md.setSchemaName(i, rsmd.getSchemaName(i));
      md.setPrecision(i, rsmd.getPrecision(i));
      md.setScale(i, rsmd.getScale(i));
      md.setTableName(i, rsmd.getTableName(i));
      md.setCatalogName(i, rsmd.getCatalogName(i));
      md.setColumnType(i, rsmd.getColumnType(i));
      md.setColumnTypeName(i, rsmd.getColumnTypeName(i));
    }
  }

  public void execute(Connection conn) throws SQLException {
    this.conn = conn;
    execute();
  }

  public void execute() throws SQLException {
    SyncProvider p = provider;
    if (p == null) {
      throw new SQLException("Invalid SyncProvider");
    }
    p.getRowSetReader().readData(this);
  }

  public void setMetaData(RowSetMetaData md) throws SQLException {
    RowSetMD = new RowSetMetaDataImpl();
    initMetaData(RowSetMD, md);
  }

  public ResultSetMetaData getMetaData() throws SQLException {
    return RowSetMD;
  }

  public Connection getConnection() throws SQLException {
    return conn;
  }

  public ResultSet getOriginal() throws SQLException {
    CachedRowSetImpl copy = new CachedRowSetImpl();
    copy.RowSetMD = RowSetMD;
    for (Row r : rows) {
      if (!r.inserted) {
        copy.rows.add(new Row(r.orig.clone()));
      }
    }
    return copy;
  }

  public ResultSet getOriginalRow() throws SQLException {
    checkRow();
    CachedRowSetImpl copy = new CachedRowSetImpl();
    copy.RowSetMD = RowSetMD;
    copy.rows.add(new Row(rows.get(cursor).orig.clone()));
    return copy;
  }

  public void setOriginalRow() throws SQLException {
    checkRow();
    Row r = rows.get(cursor);
    r.orig = r.cur.clone();
    r.updated = false;
    r.changed.clear();
  }

  public void setOriginal() throws SQLException {
    for (Row r : rows) {
      r.orig = r.cur.clone();
      r.updated = false;
      r.inserted = false;
      r.changed.clear();
    }
  }

  // synchronization

  public void acceptChanges() throws SyncProviderException {
    try {
      acceptChanges(conn);
    } catch (SyncProviderException e) {
      throw e;
    }
  }

  public void acceptChanges(Connection con) throws SyncProviderException {
    try {
      this.conn = con;
      if (provider == null) {
        throw new SyncProviderException("Invalid SyncProvider");
      }
      boolean ok = provider.getRowSetWriter().writeData(this);
      if (!ok) {
        throw new SyncProviderException("acceptChanges failed");
      }
      if (COMMIT_ON_ACCEPT_CHANGES && con != null && !con.getAutoCommit()) {
        con.commit();
      }
      setOriginal();
      removeDeleted();
    } catch (SyncProviderException e) {
      throw e;
    } catch (SQLException e) {
      throw new SyncProviderException(e.getMessage());
    }
  }

  private void removeDeleted() {
    for (int i = rows.size() - 1; i >= 0; i--) {
      if (rows.get(i).deleted) {
        rows.remove(i);
      }
    }
    cursor = -1;
  }

  public void commit() throws SQLException {
    if (conn != null) {
      conn.commit();
    }
  }

  public void rollback() throws SQLException {
    if (conn != null) {
      conn.rollback();
    }
  }

  public void rollback(Savepoint s) throws SQLException {
    if (conn != null) {
      conn.rollback(s);
    }
  }

  public void restoreOriginal() throws SQLException {
    for (int i = rows.size() - 1; i >= 0; i--) {
      Row r = rows.get(i);
      if (r.inserted) {
        rows.remove(i);
      } else {
        r.cur = r.orig.clone();
        r.updated = false;
        r.deleted = false;
        r.changed.clear();
      }
    }
    cursor = -1;
    notifyRowSetChanged();
  }

  public void release() throws SQLException {
    rows = new ArrayList<Row>();
    cursor = -1;
    notifyRowSetChanged();
  }

  public void undoDelete() throws SQLException {
    checkRowIncludingDeleted();
    Row r = rows.get(cursor);
    if (!r.deleted) {
      return;
    }
    r.deleted = false;
    notifyRowChanged();
  }

  public void undoInsert() throws SQLException {
    checkRow();
    Row r = rows.get(cursor);
    if (!r.inserted) {
      throw new SQLException("Cannot undo insert: the current row was not inserted");
    }
    rows.remove(cursor);
    cursor--;
    notifyRowChanged();
  }

  public void undoUpdate() throws SQLException {
    cancelRowUpdates();
  }

  public boolean columnUpdated(int idx) throws SQLException {
    checkRow();
    checkColumnIndex(idx);
    return rows.get(cursor).changed.get(idx - 1);
  }

  public boolean columnUpdated(String columnName) throws SQLException {
    return columnUpdated(findColumn(columnName));
  }

  public int[] getKeyColumns() throws SQLException {
    return keyCols.clone();
  }

  public void setKeyColumns(int[] keys) throws SQLException {
    int n = RowSetMD == null ? 0 : RowSetMD.getColumnCount();
    for (int k : keys) {
      if (k < 1 || k > n) {
        throw new SQLException("Invalid column index" + k);
      }
    }
    keyCols = keys.clone();
  }

  public String getTableName() throws SQLException {
    return tableName;
  }

  public void setTableName(String tabName) throws SQLException {
    if (tabName == null) {
      throw new SQLException("Table name cannot be null");
    }
    tableName = tabName;
  }

  public SyncProvider getSyncProvider() throws SQLException {
    return provider;
  }

  public void setSyncProvider(String providerStr) throws SQLException {
    try {
      provider = SyncFactory.getInstance(providerStr);
    } catch (SyncFactoryException e) {
      throw new SQLException(e.getMessage());
    }
  }

  public RowSetWarning getRowSetWarnings() throws SQLException {
    return rowsetWarning;
  }

  public boolean getShowDeleted() throws SQLException {
    return showDeleted;
  }

  public void setShowDeleted(boolean b) throws SQLException {
    showDeleted = b;
  }

  public int size() {
    return rows.size();
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int size) throws SQLException {
    if (size < 0) {
      throw new SQLException("Page size cannot be negative");
    }
    if (size > getMaxRows() && getMaxRows() != 0) {
      throw new SQLException("Page size cannot be greater than maxRows");
    }
    pageSize = size;
  }

  public boolean nextPage() throws SQLException {
    if (pagedSource == null || pageSize == 0) {
      return false;
    }
    int count = RowSetMD.getColumnCount();
    ArrayList<Row> page = new ArrayList<Row>();
    ResultSet data = pagedSource;
    boolean more = false;
    int n = 0;
    while (n < pageSize && data.next()) {
      Object[] values = new Object[count];
      for (int i = 1; i <= count; i++) {
        values[i - 1] = data.getObject(i);
      }
      page.add(new Row(values));
      n++;
    }
    if (page.isEmpty()) {
      return false;
    }
    rows = page;
    pageStart += n;
    cursor = -1;
    notifyRowSetChanged();
    return true;
  }

  public boolean previousPage() throws SQLException {
    if (pagedSource == null || pageSize == 0 || pageStart <= pageSize) {
      return false;
    }
    if (pagedSource.getType() == ResultSet.TYPE_FORWARD_ONLY) {
      throw new SQLException("Cannot go back on a forward-only ResultSet");
    }
    int start = Math.max(1, pageStart - 2 * pageSize);
    pagedSource.absolute(start - 1);
    pageStart = start;
    return nextPage();
  }

  public void rowSetPopulated(RowSetEvent event, int numRows) throws SQLException {
    if (numRows < 0 || numRows < getFetchSize()) {
      throw new SQLException("Number of rows is less than zero or less than fetch size");
    }
    if (size() % numRows == 0) {
      notifyRowSetChanged();
    }
  }

  public Collection<?> toCollection() throws SQLException {
    List<Object[]> out = new ArrayList<Object[]>();
    for (Row r : rows) {
      if (!r.deleted || showDeleted) {
        out.add(r.cur.clone());
      }
    }
    return out;
  }

  public Collection<?> toCollection(int column) throws SQLException {
    checkColumnIndex(column);
    List<Object> out = new ArrayList<Object>();
    for (Row r : rows) {
      if (!r.deleted || showDeleted) {
        out.add(r.cur[column - 1]);
      }
    }
    return out;
  }

  public Collection<?> toCollection(String column) throws SQLException {
    return toCollection(findColumn(column));
  }

  public CachedRowSet createCopy() throws SQLException {
    CachedRowSetImpl c = createCopySchemaImpl();
    for (Row r : rows) {
      c.rows.add(r.copy());
    }
    return c;
  }

  public CachedRowSet createCopyNoConstraints() throws SQLException {
    CachedRowSetImpl c = (CachedRowSetImpl) createCopy();
    c.keyCols = new int[0];
    c.tableName = null;
    c.setCommand(null);
    for (Row r : c.rows) {
      r.updated = false;
      r.inserted = false;
      r.deleted = false;
      r.orig = r.cur.clone();
      r.changed.clear();
    }
    return c;
  }

  public CachedRowSet createCopySchema() throws SQLException {
    return createCopySchemaImpl();
  }

  private CachedRowSetImpl createCopySchemaImpl() throws SQLException {
    CachedRowSetImpl c = new CachedRowSetImpl();
    c.RowSetMD = RowSetMD;
    c.keyCols = keyCols.clone();
    c.tableName = tableName;
    c.showDeleted = showDeleted;
    c.provider = provider;
    c.setType(getType());
    c.setConcurrency(getConcurrency());
    return c;
  }

  public RowSet createShared() throws SQLException {
    try {
      CachedRowSetImpl clone = (CachedRowSetImpl) super.clone();
      clone.rows = rows;
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new SQLException(e.getMessage());
    }
  }

  protected Object clone() throws CloneNotSupportedException {
    CachedRowSetImpl c = (CachedRowSetImpl) super.clone();
    c.rows = new ArrayList<Row>();
    for (Row r : rows) {
      c.rows.add(r.copy());
    }
    return c;
  }

  // Joinable

  public void setMatchColumn(int columnIdx) throws SQLException {
    if (columnIdx < 0) {
      throw new SQLException("Match column index must be greater than 0");
    }
    matchColumns = new int[] {columnIdx};
    matchColumnNames = new String[] {null};
  }

  public void setMatchColumn(int[] columnIdxes) throws SQLException {
    for (int c : columnIdxes) {
      if (c < 0) {
        throw new SQLException("Match column index must be greater than 0");
      }
    }
    matchColumns = columnIdxes.clone();
    matchColumnNames = new String[columnIdxes.length];
  }

  public void setMatchColumn(String columnName) throws SQLException {
    if (columnName == null || columnName.trim().length() == 0) {
      throw new SQLException("Match column name cannot be empty");
    }
    matchColumnNames = new String[] {columnName.trim()};
    matchColumns = new int[] {-1};
  }

  public void setMatchColumn(String[] columnNames) throws SQLException {
    for (String c : columnNames) {
      if (c == null || c.trim().length() == 0) {
        throw new SQLException("Match column name cannot be empty");
      }
    }
    matchColumnNames = columnNames.clone();
    matchColumns = new int[columnNames.length];
    Arrays.fill(matchColumns, -1);
  }

  public int[] getMatchColumnIndexes() throws SQLException {
    int[] out = new int[matchColumns.length];
    for (int i = 0; i < out.length; i++) {
      out[i] = matchColumns[i] > 0 ? matchColumns[i] : (matchColumnNames[i] != null ? findColumn(matchColumnNames[i]) : matchColumns[i]);
    }
    if (out.length == 0) {
      throw new SQLException("Set Match Columns before getting them");
    }
    return out;
  }

  public String[] getMatchColumnNames() throws SQLException {
    String[] out = new String[matchColumns.length];
    for (int i = 0; i < out.length; i++) {
      out[i] = matchColumnNames[i] != null ? matchColumnNames[i] : RowSetMD.getColumnName(matchColumns[i]);
    }
    if (out.length == 0) {
      throw new SQLException("Set Match Columns before getting them");
    }
    return out;
  }

  public void unsetMatchColumn(int columnIdx) throws SQLException {
    matchColumns = new int[0];
    matchColumnNames = new String[0];
  }

  public void unsetMatchColumn(int[] columnIdxes) throws SQLException {
    matchColumns = new int[0];
    matchColumnNames = new String[0];
  }

  public void unsetMatchColumn(String columnName) throws SQLException {
    matchColumns = new int[0];
    matchColumnNames = new String[0];
  }

  public void unsetMatchColumn(String[] columnNames) throws SQLException {
    matchColumns = new int[0];
    matchColumnNames = new String[0];
  }

  // cursor movement

  boolean visible(int i) {
    return i >= 0 && i < rows.size() && (!rows.get(i).deleted || showDeleted) && accept(i);
  }

  boolean accept(int i) {
    return true;
  }

  private void checkNavigable() throws SQLException {
    checkOpen();
    if (getType() == ResultSet.TYPE_FORWARD_ONLY) {
      throw new SQLException(new Throwable().getStackTrace()[1].getMethodName() + " : Invalid cursor operation");
    }
  }

  public boolean next() throws SQLException {
    checkOpen();
    onInsertRow = false;
    int i = cursor + 1;
    if (cursor >= rows.size()) {
      return false;
    }
    while (i < rows.size() && !visible(i)) {
      i++;
    }
    cursor = i;
    if (i >= rows.size()) {
      cursor = rows.size();
      return false;
    }
    notifyCursorMoved();
    return true;
  }

  public boolean previous() throws SQLException {
    checkNavigable();
    onInsertRow = false;
    int i = Math.min(cursor, rows.size()) - 1;
    while (i >= 0 && !visible(i)) {
      i--;
    }
    if (i < 0) {
      cursor = -1;
      return false;
    }
    cursor = i;
    notifyCursorMoved();
    return true;
  }

  public boolean first() throws SQLException {
    checkNavigable();
    onInsertRow = false;
    for (int i = 0; i < rows.size(); i++) {
      if (visible(i)) {
        cursor = i;
        notifyCursorMoved();
        return true;
      }
    }
    cursor = -1;
    return false;
  }

  public boolean last() throws SQLException {
    checkNavigable();
    onInsertRow = false;
    for (int i = rows.size() - 1; i >= 0; i--) {
      if (visible(i)) {
        cursor = i;
        notifyCursorMoved();
        return true;
      }
    }
    cursor = rows.size();
    return false;
  }

  public void beforeFirst() throws SQLException {
    checkNavigable();
    onInsertRow = false;
    cursor = -1;
  }

  public void afterLast() throws SQLException {
    checkNavigable();
    onInsertRow = false;
    cursor = rows.size();
  }

  public boolean absolute(int row) throws SQLException {
    checkNavigable();
    onInsertRow = false;
    int total = size();
    if (row == 0) {
      throw new SQLException("Invalid cursor position");
    }
    int target = row > 0 ? row : total + row + 1;
    if (target < 1) {
      cursor = -1;
      return false;
    }
    if (target > total) {
      cursor = rows.size();
      return false;
    }
    int seen = 0;
    for (int i = 0; i < rows.size(); i++) {
      if (visible(i) && ++seen == target) {
        cursor = i;
        notifyCursorMoved();
        return true;
      }
    }
    return false;
  }

  public boolean relative(int rowsMove) throws SQLException {
    checkNavigable();
    if (rows.isEmpty() || cursor < 0 || cursor >= rows.size()) {
      throw new SQLException("Invalid cursor position");
    }
    return absolute(getRow() + rowsMove);
  }

  public boolean isBeforeFirst() throws SQLException {
    return cursor < 0 && size() > 0;
  }

  public boolean isAfterLast() throws SQLException {
    return cursor >= rows.size() && size() > 0;
  }

  public boolean isFirst() throws SQLException {
    return !onInsertRow && getRow() == 1;
  }

  public boolean isLast() throws SQLException {
    return !onInsertRow && cursor >= 0 && cursor < rows.size() && getRow() == size();
  }

  public int getRow() throws SQLException {
    if (cursor < 0 || cursor >= rows.size()) {
      return 0;
    }
    int n = 0;
    for (int i = 0; i <= cursor; i++) {
      if (visible(i)) {
        n++;
      }
    }
    return n;
  }

  // helpers

  private void checkRow() throws SQLException {
    checkOpen();
    if (cursor < 0 || cursor >= rows.size() || !visible(cursor)) {
      throw new SQLException("Invalid cursor position");
    }
  }

  private void checkRowIncludingDeleted() throws SQLException {
    checkOpen();
    if (cursor < 0 || cursor >= rows.size()) {
      throw new SQLException("Invalid cursor position");
    }
  }

  private void checkColumnIndex(int idx) throws SQLException {
    if (RowSetMD == null || idx < 1 || idx > RowSetMD.getColumnCount()) {
      throw new SQLException("Invalid column index");
    }
  }

  private Object column(int idx) throws SQLException {
    checkColumnIndex(idx);
    Object v;
    if (onInsertRow) {
      v = insertRow.cur[idx - 1];
    } else {
      checkRowIncludingDeleted();
      v = rows.get(cursor).cur[idx - 1];
    }
    lastNull = v == null;
    return v;
  }

  private void setCell(int idx, Object value) throws SQLException {
    checkOpen();
    checkColumnIndex(idx);
    if (onInsertRow) {
      insertRow.cur[idx - 1] = value;
      insertRow.changed.set(idx - 1);
      return;
    }
    checkRowIncludingDeleted();
    Row r = rows.get(cursor);
    r.cur[idx - 1] = value;
    r.changed.set(idx - 1);
    r.updated = true;
  }

  private static String readAll(java.io.InputStream in, int limit) throws SQLException {
    return new String(readBytes(in, limit));
  }

  private static byte[] readBytes(java.io.InputStream in, int limit) throws SQLException {
    try {
      java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
      byte[] buf = new byte[4096];
      int total = 0;
      int r;
      while (total < limit && (r = in.read(buf, 0, Math.min(buf.length, limit - total))) > 0) {
        out.write(buf, 0, r);
        total += r;
      }
      return out.toByteArray();
    } catch (IOException e) {
      throw new SQLException(e.getMessage());
    }
  }

  private static String readAll(java.io.Reader in, int limit) throws SQLException {
    try {
      StringBuilder sb = new StringBuilder();
      char[] buf = new char[4096];
      int total = 0;
      int r;
      while (total < limit && (r = in.read(buf, 0, Math.min(buf.length, limit - total))) > 0) {
        sb.append(buf, 0, r);
        total += r;
      }
      return sb.toString();
    } catch (IOException e) {
      throw new SQLException(e.getMessage());
    }
  }

  private static SQLException mismatch() {
    return new SQLException("Data Type Mismatch");
  }

  private static String toStr(Object v) {
    if (v instanceof byte[]) {
      return new String((byte[]) v);
    }
    return v.toString();
  }

  private static boolean toBool(Object v) throws SQLException {
    if (v instanceof Boolean) {
      return ((Boolean) v).booleanValue();
    }
    if (v instanceof Number) {
      return ((Number) v).doubleValue() != 0;
    }
    if (v instanceof String) {
      try {
        return Double.compare(Double.parseDouble((String) v), 0) != 0;
      } catch (NumberFormatException e) {
        return Boolean.parseBoolean((String) v);
      }
    }
    throw mismatch();
  }

  private static long toLong(Object v, long min, long max) throws SQLException {
    double d;
    long l;
    if (v instanceof Number) {
      if (v instanceof BigDecimal) {
        d = ((BigDecimal) v).doubleValue();
        l = ((BigDecimal) v).longValue();
      } else {
        d = ((Number) v).doubleValue();
        l = ((Number) v).longValue();
      }
    } else if (v instanceof Boolean) {
      l = ((Boolean) v).booleanValue() ? 1 : 0;
      d = l;
    } else if (v instanceof String) {
      try {
        BigDecimal bd = new BigDecimal(((String) v).trim());
        d = bd.doubleValue();
        l = bd.longValue();
      } catch (NumberFormatException e) {
        throw mismatch();
      }
    } else {
      throw mismatch();
    }
    if (d < min || d > max) {
      throw new SQLException("Conversion Error");
    }
    return l;
  }

  private static double toDouble(Object v) throws SQLException {
    if (v instanceof Number) {
      return ((Number) v).doubleValue();
    }
    if (v instanceof Boolean) {
      return ((Boolean) v).booleanValue() ? 1 : 0;
    }
    if (v instanceof String) {
      try {
        return Double.parseDouble(((String) v).trim());
      } catch (NumberFormatException e) {
        throw mismatch();
      }
    }
    throw mismatch();
  }

  private static BigDecimal toBigDecimal(Object v) throws SQLException {
    if (v instanceof BigDecimal) {
      return (BigDecimal) v;
    }
    if (v instanceof Number) {
      return new BigDecimal(((Number) v).toString());
    }
    if (v instanceof String) {
      try {
        return new BigDecimal(((String) v).trim());
      } catch (NumberFormatException e) {
        throw mismatch();
      }
    }
    if (v instanceof Boolean) {
      return ((Boolean) v).booleanValue() ? BigDecimal.ONE : BigDecimal.ZERO;
    }
    throw mismatch();
  }

  private static byte[] toBytes(Object v) throws SQLException {
    if (v instanceof byte[]) {
      return (byte[]) v;
    }
    if (v instanceof String) {
      return ((String) v).getBytes();
    }
    throw mismatch();
  }

  private static java.sql.Date toDate(Object v) throws SQLException {
    if (v instanceof java.sql.Date) {
      return (java.sql.Date) v;
    }
    if (v instanceof java.util.Date) {
      return new java.sql.Date(((java.util.Date) v).getTime());
    }
    if (v instanceof String) {
      try {
        return java.sql.Date.valueOf(((String) v).trim());
      } catch (IllegalArgumentException e) {
        throw mismatch();
      }
    }
    throw mismatch();
  }

  private static java.sql.Time toTime(Object v) throws SQLException {
    if (v instanceof java.sql.Time) {
      return (java.sql.Time) v;
    }
    if (v instanceof java.util.Date) {
      return new java.sql.Time(((java.util.Date) v).getTime());
    }
    if (v instanceof String) {
      try {
        return java.sql.Time.valueOf(((String) v).trim());
      } catch (IllegalArgumentException e) {
        throw mismatch();
      }
    }
    throw mismatch();
  }

  private static java.sql.Timestamp toTimestamp(Object v) throws SQLException {
    if (v instanceof java.sql.Timestamp) {
      return (java.sql.Timestamp) v;
    }
    if (v instanceof java.util.Date) {
      return new java.sql.Timestamp(((java.util.Date) v).getTime());
    }
    if (v instanceof String) {
      try {
        return java.sql.Timestamp.valueOf(((String) v).trim());
      } catch (IllegalArgumentException e) {
        throw mismatch();
      }
    }
    throw mismatch();
  }

  private static java.io.InputStream toAsciiStream(Object v) throws SQLException {
    if (v instanceof String) {
      return new java.io.ByteArrayInputStream(((String) v).getBytes());
    }
    if (v instanceof java.sql.Clob) {
      return ((java.sql.Clob) v).getAsciiStream();
    }
    if (v instanceof byte[]) {
      return new java.io.ByteArrayInputStream((byte[]) v);
    }
    throw mismatch();
  }

  private static java.io.InputStream toBinaryStream(Object v) throws SQLException {
    if (v instanceof byte[]) {
      return new java.io.ByteArrayInputStream((byte[]) v);
    }
    if (v instanceof java.sql.Blob) {
      return ((java.sql.Blob) v).getBinaryStream();
    }
    if (v instanceof String) {
      return new java.io.ByteArrayInputStream(((String) v).getBytes());
    }
    throw mismatch();
  }

  private static java.io.Reader toReader(Object v) throws SQLException {
    if (v instanceof String) {
      return new java.io.StringReader((String) v);
    }
    if (v instanceof java.sql.Clob) {
      return ((java.sql.Clob) v).getCharacterStream();
    }
    if (v instanceof byte[]) {
      return new java.io.StringReader(new String((byte[]) v));
    }
    throw mismatch();
  }

  private static java.net.URL toURL(Object v) throws SQLException {
    if (v instanceof java.net.URL) {
      return (java.net.URL) v;
    }
    if (v instanceof String) {
      try {
        return new java.net.URL((String) v);
      } catch (java.net.MalformedURLException e) {
        throw mismatch();
      }
    }
    throw mismatch();
  }

  public int findColumn(String columnName) throws SQLException {
    if (RowSetMD == null) {
      throw new SQLException("Invalid column name");
    }
    int n = RowSetMD.getColumnCount();
    String wanted = columnName == null ? "" : columnName.trim();
    for (int i = 1; i <= n; i++) {
      String label = RowSetMD.getColumnLabel(i);
      if (label != null && label.equalsIgnoreCase(wanted)) {
        return i;
      }
    }
    for (int i = 1; i <= n; i++) {
      String name = RowSetMD.getColumnName(i);
      if (name != null && name.equalsIgnoreCase(wanted)) {
        return i;
      }
    }
    throw new SQLException("Invalid column name");
  }

  public boolean wasNull() throws SQLException {
    return lastNull;
  }

  // updates and row state

  public void updateRow() throws SQLException {
    checkRow();
    if (onInsertRow) {
      throw new SQLException("Cannot call updateRow() when inserting a row");
    }
    Row r = rows.get(cursor);
    if (r.changed.isEmpty()) {
      return;
    }
    r.updated = true;
    notifyRowChanged();
  }

  public void cancelRowUpdates() throws SQLException {
    checkRow();
    Row r = rows.get(cursor);
    r.cur = r.orig.clone();
    r.changed.clear();
    r.updated = false;
    notifyRowChanged();
  }

  public void moveToInsertRow() throws SQLException {
    checkOpen();
    if (getConcurrency() == ResultSet.CONCUR_READ_ONLY) {
      throw new SQLException("Invalid operation for a read-only ResultSet");
    }
    if (insertRow == null) {
      insertRow = new Row(new Object[RowSetMD.getColumnCount()]);
    }
    insertRow.changed.clear();
    onInsertRow = true;
  }

  public void moveToCurrentRow() throws SQLException {
    onInsertRow = false;
  }

  public void insertRow() throws SQLException {
    checkOpen();
    if (!onInsertRow) {
      throw new SQLException("Cursor is not on the insert row");
    }
    int n = RowSetMD.getColumnCount();
    for (int i = 1; i <= n; i++) {
      if (RowSetMD.isNullable(i) == ResultSetMetaData.columnNoNulls && insertRow.cur[i - 1] == null
          && !RowSetMD.isAutoIncrement(i) && !insertRow.changed.get(i - 1)) {
        throw new SQLException("Failed to insert Row: column " + i + " does not allow null values");
      }
    }
    Row r = new Row(insertRow.cur.clone());
    r.inserted = true;
    int at = cursor < 0 ? 0 : (cursor >= rows.size() ? rows.size() : cursor + 1);
    rows.add(at, r);
    insertRow.changed.clear();
    notifyRowChanged();
  }

  public void deleteRow() throws SQLException {
    checkRow();
    if (onInsertRow) {
      throw new SQLException("Cannot delete the insert row");
    }
    rows.get(cursor).deleted = true;
    notifyRowChanged();
  }

  public void refreshRow() throws SQLException {
    checkRow();
  }

  public boolean rowUpdated() throws SQLException {
    checkRow();
    return rows.get(cursor).updated;
  }

  public boolean rowInserted() throws SQLException {
    checkRow();
    return rows.get(cursor).inserted;
  }

  public boolean rowDeleted() throws SQLException {
    checkRowIncludingDeleted();
    return rows.get(cursor).deleted;
  }

  // misc ResultSet

  public void close() throws SQLException {
    release();
    closed = true;
  }

  public boolean isClosed() throws SQLException {
    throw new java.sql.SQLFeatureNotSupportedException("Operation not yet supported");
  }

  public SQLWarning getWarnings() throws SQLException {
    return warnings;
  }

  public void clearWarnings() throws SQLException {
    warnings = null;
  }

  public String getCursorName() throws SQLException {
    throw new SQLException("Positioned updates not supported");
  }

  public Statement getStatement() throws SQLException {
    return null;
  }

  public int getHoldability() throws SQLException {
    return ResultSet.HOLD_CURSORS_OVER_COMMIT;
  }

  public boolean isWrapperFor(Class<?> interfaces) throws SQLException {
    return interfaces.isInstance(this);
  }

  public <T> T unwrap(Class<T> iface) throws SQLException {
    if (isWrapperFor(iface)) {
      return iface.cast(this);
    }
    throw new SQLException("unwrap failed for:" + iface);
  }

  public String getString(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return toStr(v);
  }

  public String getString(String columnLabel) throws SQLException {
    return getString(findColumn(columnLabel));
  }

  public boolean getBoolean(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return false;
    }
    return toBool(v);
  }

  public boolean getBoolean(String columnLabel) throws SQLException {
    return getBoolean(findColumn(columnLabel));
  }

  public byte getByte(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return (byte) 0;
    }
    try {
      return (byte) toLong(v, Byte.MIN_VALUE, Byte.MAX_VALUE);
    } catch (SQLException e) {
      if ("Data Type Mismatch".equals(e.getMessage()) && v instanceof String) {
        throw new SQLException("getByte Failed on value ( " + v + " ) in column " + columnIndex);
      }
      throw e;
    }
  }

  public byte getByte(String columnLabel) throws SQLException {
    return getByte(findColumn(columnLabel));
  }

  public short getShort(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return (short) 0;
    }
    try {
      return (short) toLong(v, Short.MIN_VALUE, Short.MAX_VALUE);
    } catch (SQLException e) {
      if ("Data Type Mismatch".equals(e.getMessage()) && v instanceof String) {
        throw new SQLException("getShort Failed on value ( " + v + " ) in column " + columnIndex);
      }
      throw e;
    }
  }

  public short getShort(String columnLabel) throws SQLException {
    return getShort(findColumn(columnLabel));
  }

  public int getInt(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return 0;
    }
    try {
      return (int) toLong(v, Integer.MIN_VALUE, Integer.MAX_VALUE);
    } catch (SQLException e) {
      if ("Data Type Mismatch".equals(e.getMessage()) && v instanceof String) {
        throw new SQLException("getInt Failed on value ( " + v + " ) in column " + columnIndex);
      }
      throw e;
    }
  }

  public int getInt(String columnLabel) throws SQLException {
    return getInt(findColumn(columnLabel));
  }

  public long getLong(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return 0L;
    }
    try {
      return toLong(v, Long.MIN_VALUE, Long.MAX_VALUE);
    } catch (SQLException e) {
      if ("Data Type Mismatch".equals(e.getMessage()) && v instanceof String) {
        throw new SQLException("getLong Failed on value ( " + v + " ) in column " + columnIndex);
      }
      throw e;
    }
  }

  public long getLong(String columnLabel) throws SQLException {
    return getLong(findColumn(columnLabel));
  }

  public float getFloat(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return 0f;
    }
    try {
      return (float) toDouble(v);
    } catch (SQLException e) {
      if ("Data Type Mismatch".equals(e.getMessage()) && v instanceof String) {
        throw new SQLException("getFloat Failed on value ( " + v + " ) in column " + columnIndex);
      }
      throw e;
    }
  }

  public float getFloat(String columnLabel) throws SQLException {
    return getFloat(findColumn(columnLabel));
  }

  public double getDouble(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return 0d;
    }
    try {
      return toDouble(v);
    } catch (SQLException e) {
      if ("Data Type Mismatch".equals(e.getMessage()) && v instanceof String) {
        throw new SQLException("getDouble Failed on value ( " + v + " ) in column " + columnIndex);
      }
      throw e;
    }
  }

  public double getDouble(String columnLabel) throws SQLException {
    return getDouble(findColumn(columnLabel));
  }

  public java.math.BigDecimal getBigDecimal(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    try {
      return toBigDecimal(v);
    } catch (SQLException e) {
      if ("Data Type Mismatch".equals(e.getMessage()) && v instanceof String) {
        throw new SQLException("getBigDecimal Failed on value ( " + v + " ) in column " + columnIndex);
      }
      throw e;
    }
  }

  public java.math.BigDecimal getBigDecimal(String columnLabel) throws SQLException {
    return getBigDecimal(findColumn(columnLabel));
  }

  public byte[] getBytes(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return toBytes(v);
  }

  public byte[] getBytes(String columnLabel) throws SQLException {
    return getBytes(findColumn(columnLabel));
  }

  public java.sql.Date getDate(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return toDate(v);
  }

  public java.sql.Date getDate(String columnLabel) throws SQLException {
    return getDate(findColumn(columnLabel));
  }

  public java.sql.Time getTime(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return toTime(v);
  }

  public java.sql.Time getTime(String columnLabel) throws SQLException {
    return getTime(findColumn(columnLabel));
  }

  public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return toTimestamp(v);
  }

  public java.sql.Timestamp getTimestamp(String columnLabel) throws SQLException {
    return getTimestamp(findColumn(columnLabel));
  }

  public java.io.InputStream getAsciiStream(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return toAsciiStream(v);
  }

  public java.io.InputStream getAsciiStream(String columnLabel) throws SQLException {
    return getAsciiStream(findColumn(columnLabel));
  }

  public java.io.InputStream getBinaryStream(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return toBinaryStream(v);
  }

  public java.io.InputStream getBinaryStream(String columnLabel) throws SQLException {
    return getBinaryStream(findColumn(columnLabel));
  }

  public java.io.Reader getCharacterStream(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return toReader(v);
  }

  public java.io.Reader getCharacterStream(String columnLabel) throws SQLException {
    return getCharacterStream(findColumn(columnLabel));
  }

  public java.io.Reader getNCharacterStream(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return toReader(v);
  }

  public java.io.Reader getNCharacterStream(String columnLabel) throws SQLException {
    return getNCharacterStream(findColumn(columnLabel));
  }

  public Object getObject(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return v;
  }

  public Object getObject(String columnLabel) throws SQLException {
    return getObject(findColumn(columnLabel));
  }

  public java.sql.Ref getRef(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return (java.sql.Ref) v;
  }

  public java.sql.Ref getRef(String columnLabel) throws SQLException {
    return getRef(findColumn(columnLabel));
  }

  public java.sql.Blob getBlob(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return (java.sql.Blob) v;
  }

  public java.sql.Blob getBlob(String columnLabel) throws SQLException {
    return getBlob(findColumn(columnLabel));
  }

  public java.sql.Clob getClob(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return (java.sql.Clob) v;
  }

  public java.sql.Clob getClob(String columnLabel) throws SQLException {
    return getClob(findColumn(columnLabel));
  }

  public java.sql.NClob getNClob(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return (java.sql.NClob) v;
  }

  public java.sql.NClob getNClob(String columnLabel) throws SQLException {
    return getNClob(findColumn(columnLabel));
  }

  public java.sql.Array getArray(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return (java.sql.Array) v;
  }

  public java.sql.Array getArray(String columnLabel) throws SQLException {
    return getArray(findColumn(columnLabel));
  }

  public java.net.URL getURL(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return toURL(v);
  }

  public java.net.URL getURL(String columnLabel) throws SQLException {
    return getURL(findColumn(columnLabel));
  }

  public java.sql.RowId getRowId(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return (java.sql.RowId) v;
  }

  public java.sql.RowId getRowId(String columnLabel) throws SQLException {
    return getRowId(findColumn(columnLabel));
  }

  public java.sql.SQLXML getSQLXML(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return (java.sql.SQLXML) v;
  }

  public java.sql.SQLXML getSQLXML(String columnLabel) throws SQLException {
    return getSQLXML(findColumn(columnLabel));
  }

  public String getNString(int columnIndex) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    return toStr(v);
  }

  public String getNString(String columnLabel) throws SQLException {
    return getNString(findColumn(columnLabel));
  }

  public Object getObject(int columnIndex, java.util.Map<String, Class<?>> map) throws SQLException {
    return getObject(columnIndex);
  }

  public Object getObject(String columnLabel, java.util.Map<String, Class<?>> map) throws SQLException {
    return getObject(findColumn(columnLabel));
  }

  public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
    Object v = column(columnIndex);
    if (v == null) {
      return null;
    }
    if (type.isInstance(v)) {
      return type.cast(v);
    }
    if (type == String.class) {
      return type.cast(toStr(v));
    } else if (type == Integer.class) {
      return type.cast(Integer.valueOf((int) toLong(v, Integer.MIN_VALUE, Integer.MAX_VALUE)));
    } else if (type == Long.class) {
      return type.cast(Long.valueOf(toLong(v, Long.MIN_VALUE, Long.MAX_VALUE)));
    } else if (type == Boolean.class) {
      return type.cast(Boolean.valueOf(toBool(v)));
    } else if (type == Double.class) {
      return type.cast(Double.valueOf(toDouble(v)));
    } else if (type == java.math.BigDecimal.class) {
      return type.cast(toBigDecimal(v));
    }
    throw new SQLException("Data Type Mismatch");
  }

  public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
    return getObject(findColumn(columnLabel), type);
  }

  public java.sql.Date getDate(int columnIndex, java.util.Calendar cal) throws SQLException {
    return getDate(columnIndex);
  }

  public java.sql.Date getDate(String columnLabel, java.util.Calendar cal) throws SQLException {
    return getDate(findColumn(columnLabel));
  }

  public java.sql.Time getTime(int columnIndex, java.util.Calendar cal) throws SQLException {
    return getTime(columnIndex);
  }

  public java.sql.Time getTime(String columnLabel, java.util.Calendar cal) throws SQLException {
    return getTime(findColumn(columnLabel));
  }

  public java.sql.Timestamp getTimestamp(int columnIndex, java.util.Calendar cal) throws SQLException {
    return getTimestamp(columnIndex);
  }

  public java.sql.Timestamp getTimestamp(String columnLabel, java.util.Calendar cal) throws SQLException {
    return getTimestamp(findColumn(columnLabel));
  }

  public java.sql.Ref getRef(int columnIndex, java.util.Map<String, Class<?>> map) throws SQLException {
    return getRef(columnIndex);
  }

  public java.math.BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
    java.math.BigDecimal v = getBigDecimal(columnIndex);
    return v == null ? null : v.setScale(scale, java.math.RoundingMode.HALF_UP);
  }

  public java.math.BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
    return getBigDecimal(findColumn(columnLabel), scale);
  }

  public java.io.InputStream getUnicodeStream(int columnIndex) throws SQLException {
    return getAsciiStream(columnIndex);
  }

  public java.io.InputStream getUnicodeStream(String columnLabel) throws SQLException {
    return getAsciiStream(findColumn(columnLabel));
  }

  public void updateBoolean(int columnIndex, boolean x) throws SQLException {
    setCell(columnIndex, Boolean.valueOf(x));
  }

  public void updateBoolean(String columnLabel, boolean x) throws SQLException {
    updateBoolean(findColumn(columnLabel), x);
  }

  public void updateByte(int columnIndex, byte x) throws SQLException {
    setCell(columnIndex, Byte.valueOf(x));
  }

  public void updateByte(String columnLabel, byte x) throws SQLException {
    updateByte(findColumn(columnLabel), x);
  }

  public void updateShort(int columnIndex, short x) throws SQLException {
    setCell(columnIndex, Short.valueOf(x));
  }

  public void updateShort(String columnLabel, short x) throws SQLException {
    updateShort(findColumn(columnLabel), x);
  }

  public void updateInt(int columnIndex, int x) throws SQLException {
    setCell(columnIndex, Integer.valueOf(x));
  }

  public void updateInt(String columnLabel, int x) throws SQLException {
    updateInt(findColumn(columnLabel), x);
  }

  public void updateLong(int columnIndex, long x) throws SQLException {
    setCell(columnIndex, Long.valueOf(x));
  }

  public void updateLong(String columnLabel, long x) throws SQLException {
    updateLong(findColumn(columnLabel), x);
  }

  public void updateFloat(int columnIndex, float x) throws SQLException {
    setCell(columnIndex, Float.valueOf(x));
  }

  public void updateFloat(String columnLabel, float x) throws SQLException {
    updateFloat(findColumn(columnLabel), x);
  }

  public void updateDouble(int columnIndex, double x) throws SQLException {
    setCell(columnIndex, Double.valueOf(x));
  }

  public void updateDouble(String columnLabel, double x) throws SQLException {
    updateDouble(findColumn(columnLabel), x);
  }

  public void updateBigDecimal(int columnIndex, java.math.BigDecimal x) throws SQLException {
    setCell(columnIndex, x);
  }

  public void updateBigDecimal(String columnLabel, java.math.BigDecimal x) throws SQLException {
    updateBigDecimal(findColumn(columnLabel), x);
  }

  public void updateString(int columnIndex, String x) throws SQLException {
    setCell(columnIndex, x);
  }

  public void updateString(String columnLabel, String x) throws SQLException {
    updateString(findColumn(columnLabel), x);
  }

  public void updateBytes(int columnIndex, byte[] x) throws SQLException {
    setCell(columnIndex, x);
  }

  public void updateBytes(String columnLabel, byte[] x) throws SQLException {
    updateBytes(findColumn(columnLabel), x);
  }

  public void updateDate(int columnIndex, java.sql.Date x) throws SQLException {
    setCell(columnIndex, x);
  }

  public void updateDate(String columnLabel, java.sql.Date x) throws SQLException {
    updateDate(findColumn(columnLabel), x);
  }

  public void updateTime(int columnIndex, java.sql.Time x) throws SQLException {
    setCell(columnIndex, x);
  }

  public void updateTime(String columnLabel, java.sql.Time x) throws SQLException {
    updateTime(findColumn(columnLabel), x);
  }

  public void updateTimestamp(int columnIndex, java.sql.Timestamp x) throws SQLException {
    setCell(columnIndex, x);
  }

  public void updateTimestamp(String columnLabel, java.sql.Timestamp x) throws SQLException {
    updateTimestamp(findColumn(columnLabel), x);
  }

  public void updateObject(int columnIndex, Object x) throws SQLException {
    setCell(columnIndex, x);
  }

  public void updateObject(String columnLabel, Object x) throws SQLException {
    updateObject(findColumn(columnLabel), x);
  }

  public void updateRef(int columnIndex, java.sql.Ref x) throws SQLException {
    setCell(columnIndex, x);
  }

  public void updateRef(String columnLabel, java.sql.Ref x) throws SQLException {
    updateRef(findColumn(columnLabel), x);
  }

  public void updateBlob(int columnIndex, java.sql.Blob x) throws SQLException {
    setCell(columnIndex, x);
  }

  public void updateBlob(String columnLabel, java.sql.Blob x) throws SQLException {
    updateBlob(findColumn(columnLabel), x);
  }

  public void updateClob(int columnIndex, java.sql.Clob x) throws SQLException {
    setCell(columnIndex, x);
  }

  public void updateClob(String columnLabel, java.sql.Clob x) throws SQLException {
    updateClob(findColumn(columnLabel), x);
  }

  public void updateNClob(int columnIndex, java.sql.NClob x) throws SQLException {
    setCell(columnIndex, x);
  }

  public void updateNClob(String columnLabel, java.sql.NClob x) throws SQLException {
    updateNClob(findColumn(columnLabel), x);
  }

  public void updateArray(int columnIndex, java.sql.Array x) throws SQLException {
    setCell(columnIndex, x);
  }

  public void updateArray(String columnLabel, java.sql.Array x) throws SQLException {
    updateArray(findColumn(columnLabel), x);
  }

  public void updateRowId(int columnIndex, java.sql.RowId x) throws SQLException {
    setCell(columnIndex, x);
  }

  public void updateRowId(String columnLabel, java.sql.RowId x) throws SQLException {
    updateRowId(findColumn(columnLabel), x);
  }

  public void updateSQLXML(int columnIndex, java.sql.SQLXML x) throws SQLException {
    setCell(columnIndex, x);
  }

  public void updateSQLXML(String columnLabel, java.sql.SQLXML x) throws SQLException {
    updateSQLXML(findColumn(columnLabel), x);
  }

  public void updateNString(int columnIndex, String x) throws SQLException {
    setCell(columnIndex, x);
  }

  public void updateNString(String columnLabel, String x) throws SQLException {
    updateNString(findColumn(columnLabel), x);
  }

  public void updateAsciiStream(int columnIndex, java.io.InputStream x, int length) throws SQLException {
    setCell(columnIndex, x == null ? null : readAll(x, length));
  }

  public void updateAsciiStream(String columnLabel, java.io.InputStream x, int length) throws SQLException {
    updateAsciiStream(findColumn(columnLabel), x, length);
  }

  public void updateAsciiStream(int columnIndex, java.io.InputStream x, long length) throws SQLException {
    updateAsciiStream(columnIndex, x, (int) length);
  }

  public void updateAsciiStream(String columnLabel, java.io.InputStream x, long length) throws SQLException {
    updateAsciiStream(findColumn(columnLabel), x, (int) length);
  }

  public void updateAsciiStream(int columnIndex, java.io.InputStream x) throws SQLException {
    updateAsciiStream(columnIndex, x, Integer.MAX_VALUE);
  }

  public void updateAsciiStream(String columnLabel, java.io.InputStream x) throws SQLException {
    updateAsciiStream(findColumn(columnLabel), x, Integer.MAX_VALUE);
  }

  public void updateBinaryStream(int columnIndex, java.io.InputStream x, int length) throws SQLException {
    setCell(columnIndex, x == null ? null : readAll(x, length));
  }

  public void updateBinaryStream(String columnLabel, java.io.InputStream x, int length) throws SQLException {
    updateBinaryStream(findColumn(columnLabel), x, length);
  }

  public void updateBinaryStream(int columnIndex, java.io.InputStream x, long length) throws SQLException {
    updateBinaryStream(columnIndex, x, (int) length);
  }

  public void updateBinaryStream(String columnLabel, java.io.InputStream x, long length) throws SQLException {
    updateBinaryStream(findColumn(columnLabel), x, (int) length);
  }

  public void updateBinaryStream(int columnIndex, java.io.InputStream x) throws SQLException {
    updateBinaryStream(columnIndex, x, Integer.MAX_VALUE);
  }

  public void updateBinaryStream(String columnLabel, java.io.InputStream x) throws SQLException {
    updateBinaryStream(findColumn(columnLabel), x, Integer.MAX_VALUE);
  }

  public void updateCharacterStream(int columnIndex, java.io.Reader x, int length) throws SQLException {
    setCell(columnIndex, x == null ? null : readAll(x, length));
  }

  public void updateCharacterStream(String columnLabel, java.io.Reader x, int length) throws SQLException {
    updateCharacterStream(findColumn(columnLabel), x, length);
  }

  public void updateCharacterStream(int columnIndex, java.io.Reader x, long length) throws SQLException {
    updateCharacterStream(columnIndex, x, (int) length);
  }

  public void updateCharacterStream(String columnLabel, java.io.Reader x, long length) throws SQLException {
    updateCharacterStream(findColumn(columnLabel), x, (int) length);
  }

  public void updateCharacterStream(int columnIndex, java.io.Reader x) throws SQLException {
    updateCharacterStream(columnIndex, x, Integer.MAX_VALUE);
  }

  public void updateCharacterStream(String columnLabel, java.io.Reader x) throws SQLException {
    updateCharacterStream(findColumn(columnLabel), x, Integer.MAX_VALUE);
  }

  public void updateNCharacterStream(int columnIndex, java.io.Reader x, int length) throws SQLException {
    setCell(columnIndex, x == null ? null : readAll(x, length));
  }

  public void updateNCharacterStream(String columnLabel, java.io.Reader x, int length) throws SQLException {
    updateNCharacterStream(findColumn(columnLabel), x, length);
  }

  public void updateNCharacterStream(int columnIndex, java.io.Reader x, long length) throws SQLException {
    updateNCharacterStream(columnIndex, x, (int) length);
  }

  public void updateNCharacterStream(String columnLabel, java.io.Reader x, long length) throws SQLException {
    updateNCharacterStream(findColumn(columnLabel), x, (int) length);
  }

  public void updateNCharacterStream(int columnIndex, java.io.Reader x) throws SQLException {
    updateNCharacterStream(columnIndex, x, Integer.MAX_VALUE);
  }

  public void updateNCharacterStream(String columnLabel, java.io.Reader x) throws SQLException {
    updateNCharacterStream(findColumn(columnLabel), x, Integer.MAX_VALUE);
  }

  public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
    updateObject(columnIndex, x);
  }

  public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
    updateObject(findColumn(columnLabel), x);
  }

  public void updateNull(int columnIndex) throws SQLException {
    setCell(columnIndex, null);
  }

  public void updateNull(String columnLabel) throws SQLException {
    updateNull(findColumn(columnLabel));
  }

  public void updateBlob(int columnIndex, java.io.InputStream x, long length) throws SQLException {
    setCell(columnIndex, x == null ? null : new javax.sql.rowset.serial.SerialBlob(readBytes(x, (int) length)));
  }

  public void updateBlob(String columnLabel, java.io.InputStream x, long length) throws SQLException {
    updateBlob(findColumn(columnLabel), x, length);
  }

  public void updateBlob(int columnIndex, java.io.InputStream x) throws SQLException {
    updateBlob(columnIndex, x, Integer.MAX_VALUE);
  }

  public void updateBlob(String columnLabel, java.io.InputStream x) throws SQLException {
    updateBlob(findColumn(columnLabel), x, Integer.MAX_VALUE);
  }

  public void updateClob(int columnIndex, java.io.Reader x, long length) throws SQLException {
    setCell(columnIndex, x == null ? null : new javax.sql.rowset.serial.SerialClob(readAll(x, (int) length).toCharArray()));
  }

  public void updateClob(String columnLabel, java.io.Reader x, long length) throws SQLException {
    updateClob(findColumn(columnLabel), x, length);
  }

  public void updateClob(int columnIndex, java.io.Reader x) throws SQLException {
    updateClob(columnIndex, x, Integer.MAX_VALUE);
  }

  public void updateClob(String columnLabel, java.io.Reader x) throws SQLException {
    updateClob(findColumn(columnLabel), x, Integer.MAX_VALUE);
  }

  public void updateNClob(int columnIndex, java.io.Reader x, long length) throws SQLException {
    updateClob(columnIndex, x, length);
  }

  public void updateNClob(String columnLabel, java.io.Reader x, long length) throws SQLException {
    updateClob(findColumn(columnLabel), x, length);
  }

  public void updateNClob(int columnIndex, java.io.Reader x) throws SQLException {
    updateClob(columnIndex, x);
  }

  public void updateNClob(String columnLabel, java.io.Reader x) throws SQLException {
    updateClob(findColumn(columnLabel), x);
  }

  public void updateURL(int columnIndex, java.net.URL x) throws SQLException {
    throw new SQLException("Feature not supported");
  }

  public void updateURL(String columnLabel, java.net.URL x) throws SQLException {
    throw new SQLException("Feature not supported");
  }
}
