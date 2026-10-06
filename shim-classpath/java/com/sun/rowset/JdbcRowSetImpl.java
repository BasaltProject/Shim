/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.rowset;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import javax.sql.DataSource;
import javax.sql.RowSetMetaData;
import javax.sql.rowset.BaseRowSet;
import javax.sql.rowset.JdbcRowSet;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetWarning;

public class JdbcRowSetImpl extends BaseRowSet implements JdbcRowSet, javax.sql.RowSet {
  private static final long serialVersionUID = -3591946023893483003L;

  private Connection conn;
  private PreparedStatement ps;
  private ResultSet rs;
  private RowSetMetaDataImpl rowsMD;
  private int[] matchColumns = new int[0];
  private String[] matchColumnNames = new String[0];
  private boolean showDeleted;

  public JdbcRowSetImpl() {
    initParams();
    try {
      setShowDeleted(false);
      setQueryTimeout(0);
      setMaxRows(0);
      setMaxFieldSize(0);
      setEscapeProcessing(true);
      setConcurrency(ResultSet.CONCUR_UPDATABLE);
      setType(ResultSet.TYPE_SCROLL_INSENSITIVE);
      setReadOnly(true);
      setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
  }

  public JdbcRowSetImpl(Connection con) throws SQLException {
    this();
    conn = con;
  }

  public JdbcRowSetImpl(String url, String user, String password) throws SQLException {
    this();
    setUsername(user);
    setPassword(password);
    setUrl(url);
  }

  public JdbcRowSetImpl(ResultSet res) throws SQLException {
    this();
    rs = res;
    initMetaData();
  }

  private void initMetaData() throws SQLException {
    ResultSetMetaData md = rs.getMetaData();
    rowsMD = new RowSetMetaDataImpl();
    int n = md.getColumnCount();
    rowsMD.setColumnCount(n);
    for (int i = 1; i <= n; i++) {
      rowsMD.setAutoIncrement(i, md.isAutoIncrement(i));
      rowsMD.setCaseSensitive(i, md.isCaseSensitive(i));
      rowsMD.setCurrency(i, md.isCurrency(i));
      rowsMD.setNullable(i, md.isNullable(i));
      rowsMD.setSigned(i, md.isSigned(i));
      rowsMD.setSearchable(i, md.isSearchable(i));
      rowsMD.setColumnDisplaySize(i, md.getColumnDisplaySize(i));
      rowsMD.setColumnLabel(i, md.getColumnLabel(i));
      rowsMD.setColumnName(i, md.getColumnName(i));
      rowsMD.setSchemaName(i, md.getSchemaName(i));
      rowsMD.setPrecision(i, md.getPrecision(i));
      rowsMD.setScale(i, md.getScale(i));
      rowsMD.setTableName(i, md.getTableName(i));
      rowsMD.setCatalogName(i, md.getCatalogName(i));
      rowsMD.setColumnType(i, md.getColumnType(i));
      rowsMD.setColumnTypeName(i, md.getColumnTypeName(i));
    }
  }

  private Connection connect() throws SQLException {
    if (conn != null) {
      return conn;
    }
    if (getDataSourceName() != null) {
      try {
        javax.naming.Context ctx = new javax.naming.InitialContext();
        DataSource ds = (DataSource) ctx.lookup(getDataSourceName());
        conn = getUsername() != null ? ds.getConnection(getUsername(), getPassword()) : ds.getConnection();
      } catch (javax.naming.NamingException e) {
        throw new SQLException("Unable to connect through the DataSource: " + e.getMessage());
      }
    } else if (getUrl() != null) {
      conn = DriverManager.getConnection(getUrl(), getUsername(), getPassword());
    } else {
      throw new SQLException("Unable to connect to database");
    }
    return conn;
  }

  private ResultSet rs() throws SQLException {
    if (rs == null) {
      throw new SQLException("Invalid operation: the JdbcRowSet has not been executed");
    }
    return rs;
  }

  public void execute() throws SQLException {
    if (getCommand() == null) {
      throw new SQLException("Command property is not set");
    }
    Connection c = connect();
    ps = c.prepareStatement(getCommand(), getType(), getConcurrency());
    Object[] params = getParams();
    for (int i = 0; i < params.length; i++) {
      com.sun.rowset.internal.CachedRowSetReader.setParamPublic(ps, i + 1, params[i]);
    }
    rs = ps.executeQuery();
    initMetaData();
    notifyRowSetChanged();
  }

  public ResultSetMetaData getMetaData() throws SQLException {
    return rowsMD != null ? rowsMD : rs().getMetaData();
  }

  public void close() throws SQLException {
    if (rs != null) {
      rs.close();
    }
    if (ps != null) {
      ps.close();
    }
    if (conn != null) {
      conn.close();
    }
  }

  public void commit() throws SQLException {
    connect().commit();
  }

  public void rollback() throws SQLException {
    connect().rollback();
  }

  public void rollback(Savepoint s) throws SQLException {
    connect().rollback(s);
  }

  public boolean getAutoCommit() throws SQLException {
    return connect().getAutoCommit();
  }

  public void setAutoCommit(boolean autoCommit) throws SQLException {
    connect().setAutoCommit(autoCommit);
  }

  public RowSetWarning getRowSetWarnings() throws SQLException {
    return null;
  }

  public boolean getShowDeleted() throws SQLException {
    return showDeleted;
  }

  public void setShowDeleted(boolean value) throws SQLException {
    showDeleted = value;
  }

  public int getType() throws SQLException {
    return super.getType();
  }

  public void setMatchColumn(int columnIdx) throws SQLException {
    if (columnIdx < 0) {
      throw new SQLException("Match column index must be greater than 0");
    }
    matchColumns = new int[] {columnIdx};
    matchColumnNames = new String[] {null};
  }

  public void setMatchColumn(int[] columnIdxes) throws SQLException {
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
    matchColumnNames = columnNames.clone();
    matchColumns = new int[columnNames.length];
    java.util.Arrays.fill(matchColumns, -1);
  }

  public int[] getMatchColumnIndexes() throws SQLException {
    if (matchColumns.length == 0) {
      throw new SQLException("Set Match Columns before getting them");
    }
    int[] out = new int[matchColumns.length];
    for (int i = 0; i < out.length; i++) {
      out[i] = matchColumns[i] > 0 ? matchColumns[i] : rs().findColumn(matchColumnNames[i]);
    }
    return out;
  }

  public String[] getMatchColumnNames() throws SQLException {
    if (matchColumns.length == 0) {
      throw new SQLException("Set Match Columns before getting them");
    }
    String[] out = new String[matchColumns.length];
    for (int i = 0; i < out.length; i++) {
      out[i] = matchColumnNames[i] != null ? matchColumnNames[i] : getMetaData().getColumnName(matchColumns[i]);
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

  public boolean isWrapperFor(Class<?> interfaces) throws SQLException {
    return interfaces.isInstance(this);
  }

  public <T> T unwrap(Class<T> iface) throws SQLException {
    if (isWrapperFor(iface)) {
      return iface.cast(this);
    }
    throw new SQLException("unwrap failed for:" + iface);
  }

  public boolean absolute(int p0) throws java.sql.SQLException {
    return rs().absolute(p0);
  }

  public void afterLast() throws java.sql.SQLException {
    rs().afterLast();
  }

  public void beforeFirst() throws java.sql.SQLException {
    rs().beforeFirst();
  }

  public void cancelRowUpdates() throws java.sql.SQLException {
    rs().cancelRowUpdates();
  }

  public void clearWarnings() throws java.sql.SQLException {
    rs().clearWarnings();
  }

  public void deleteRow() throws java.sql.SQLException {
    rs().deleteRow();
  }

  public int findColumn(java.lang.String p0) throws java.sql.SQLException {
    return rs().findColumn(p0);
  }

  public boolean first() throws java.sql.SQLException {
    return rs().first();
  }

  public java.sql.Array getArray(java.lang.String p0) throws java.sql.SQLException {
    return rs().getArray(p0);
  }

  public java.sql.Array getArray(int p0) throws java.sql.SQLException {
    return rs().getArray(p0);
  }

  public java.io.InputStream getAsciiStream(java.lang.String p0) throws java.sql.SQLException {
    return rs().getAsciiStream(p0);
  }

  public java.io.InputStream getAsciiStream(int p0) throws java.sql.SQLException {
    return rs().getAsciiStream(p0);
  }

  public java.math.BigDecimal getBigDecimal(java.lang.String p0, int p1) throws java.sql.SQLException {
    return rs().getBigDecimal(p0, p1);
  }

  public java.math.BigDecimal getBigDecimal(java.lang.String p0) throws java.sql.SQLException {
    return rs().getBigDecimal(p0);
  }

  public java.math.BigDecimal getBigDecimal(int p0, int p1) throws java.sql.SQLException {
    return rs().getBigDecimal(p0, p1);
  }

  public java.math.BigDecimal getBigDecimal(int p0) throws java.sql.SQLException {
    return rs().getBigDecimal(p0);
  }

  public java.io.InputStream getBinaryStream(java.lang.String p0) throws java.sql.SQLException {
    return rs().getBinaryStream(p0);
  }

  public java.io.InputStream getBinaryStream(int p0) throws java.sql.SQLException {
    return rs().getBinaryStream(p0);
  }

  public java.sql.Blob getBlob(java.lang.String p0) throws java.sql.SQLException {
    return rs().getBlob(p0);
  }

  public java.sql.Blob getBlob(int p0) throws java.sql.SQLException {
    return rs().getBlob(p0);
  }

  public boolean getBoolean(java.lang.String p0) throws java.sql.SQLException {
    return rs().getBoolean(p0);
  }

  public boolean getBoolean(int p0) throws java.sql.SQLException {
    return rs().getBoolean(p0);
  }

  public byte getByte(java.lang.String p0) throws java.sql.SQLException {
    return rs().getByte(p0);
  }

  public byte getByte(int p0) throws java.sql.SQLException {
    return rs().getByte(p0);
  }

  public byte[] getBytes(java.lang.String p0) throws java.sql.SQLException {
    return rs().getBytes(p0);
  }

  public byte[] getBytes(int p0) throws java.sql.SQLException {
    return rs().getBytes(p0);
  }

  public java.io.Reader getCharacterStream(java.lang.String p0) throws java.sql.SQLException {
    return rs().getCharacterStream(p0);
  }

  public java.io.Reader getCharacterStream(int p0) throws java.sql.SQLException {
    return rs().getCharacterStream(p0);
  }

  public java.sql.Clob getClob(java.lang.String p0) throws java.sql.SQLException {
    return rs().getClob(p0);
  }

  public java.sql.Clob getClob(int p0) throws java.sql.SQLException {
    return rs().getClob(p0);
  }

  public java.lang.String getCursorName() throws java.sql.SQLException {
    return rs().getCursorName();
  }

  public java.sql.Date getDate(java.lang.String p0, java.util.Calendar p1) throws java.sql.SQLException {
    return rs().getDate(p0, p1);
  }

  public java.sql.Date getDate(java.lang.String p0) throws java.sql.SQLException {
    return rs().getDate(p0);
  }

  public java.sql.Date getDate(int p0, java.util.Calendar p1) throws java.sql.SQLException {
    return rs().getDate(p0, p1);
  }

  public java.sql.Date getDate(int p0) throws java.sql.SQLException {
    return rs().getDate(p0);
  }

  public double getDouble(java.lang.String p0) throws java.sql.SQLException {
    return rs().getDouble(p0);
  }

  public double getDouble(int p0) throws java.sql.SQLException {
    return rs().getDouble(p0);
  }

  public float getFloat(java.lang.String p0) throws java.sql.SQLException {
    return rs().getFloat(p0);
  }

  public float getFloat(int p0) throws java.sql.SQLException {
    return rs().getFloat(p0);
  }

  public int getHoldability() throws java.sql.SQLException {
    return rs().getHoldability();
  }

  public int getInt(java.lang.String p0) throws java.sql.SQLException {
    return rs().getInt(p0);
  }

  public int getInt(int p0) throws java.sql.SQLException {
    return rs().getInt(p0);
  }

  public long getLong(java.lang.String p0) throws java.sql.SQLException {
    return rs().getLong(p0);
  }

  public long getLong(int p0) throws java.sql.SQLException {
    return rs().getLong(p0);
  }

  public java.io.Reader getNCharacterStream(java.lang.String p0) throws java.sql.SQLException {
    return rs().getNCharacterStream(p0);
  }

  public java.io.Reader getNCharacterStream(int p0) throws java.sql.SQLException {
    return rs().getNCharacterStream(p0);
  }

  public java.sql.NClob getNClob(java.lang.String p0) throws java.sql.SQLException {
    return rs().getNClob(p0);
  }

  public java.sql.NClob getNClob(int p0) throws java.sql.SQLException {
    return rs().getNClob(p0);
  }

  public java.lang.String getNString(java.lang.String p0) throws java.sql.SQLException {
    return rs().getNString(p0);
  }

  public java.lang.String getNString(int p0) throws java.sql.SQLException {
    return rs().getNString(p0);
  }

  public <T> T getObject(java.lang.String p0, java.lang.Class<T> p1) throws java.sql.SQLException {
    return rs().getObject(p0, p1);
  }

  public java.lang.Object getObject(java.lang.String p0, java.util.Map<java.lang.String, java.lang.Class<?>> p1) throws java.sql.SQLException {
    return rs().getObject(p0, p1);
  }

  public java.lang.Object getObject(java.lang.String p0) throws java.sql.SQLException {
    return rs().getObject(p0);
  }

  public <T> T getObject(int p0, java.lang.Class<T> p1) throws java.sql.SQLException {
    return rs().getObject(p0, p1);
  }

  public java.lang.Object getObject(int p0, java.util.Map<java.lang.String, java.lang.Class<?>> p1) throws java.sql.SQLException {
    return rs().getObject(p0, p1);
  }

  public java.lang.Object getObject(int p0) throws java.sql.SQLException {
    return rs().getObject(p0);
  }

  public java.sql.Ref getRef(java.lang.String p0) throws java.sql.SQLException {
    return rs().getRef(p0);
  }

  public java.sql.Ref getRef(int p0) throws java.sql.SQLException {
    return rs().getRef(p0);
  }

  public int getRow() throws java.sql.SQLException {
    return rs().getRow();
  }

  public java.sql.RowId getRowId(java.lang.String p0) throws java.sql.SQLException {
    return rs().getRowId(p0);
  }

  public java.sql.RowId getRowId(int p0) throws java.sql.SQLException {
    return rs().getRowId(p0);
  }

  public java.sql.SQLXML getSQLXML(java.lang.String p0) throws java.sql.SQLException {
    return rs().getSQLXML(p0);
  }

  public java.sql.SQLXML getSQLXML(int p0) throws java.sql.SQLException {
    return rs().getSQLXML(p0);
  }

  public short getShort(java.lang.String p0) throws java.sql.SQLException {
    return rs().getShort(p0);
  }

  public short getShort(int p0) throws java.sql.SQLException {
    return rs().getShort(p0);
  }

  public java.sql.Statement getStatement() throws java.sql.SQLException {
    return rs().getStatement();
  }

  public java.lang.String getString(java.lang.String p0) throws java.sql.SQLException {
    return rs().getString(p0);
  }

  public java.lang.String getString(int p0) throws java.sql.SQLException {
    return rs().getString(p0);
  }

  public java.sql.Time getTime(java.lang.String p0, java.util.Calendar p1) throws java.sql.SQLException {
    return rs().getTime(p0, p1);
  }

  public java.sql.Time getTime(java.lang.String p0) throws java.sql.SQLException {
    return rs().getTime(p0);
  }

  public java.sql.Time getTime(int p0, java.util.Calendar p1) throws java.sql.SQLException {
    return rs().getTime(p0, p1);
  }

  public java.sql.Time getTime(int p0) throws java.sql.SQLException {
    return rs().getTime(p0);
  }

  public java.sql.Timestamp getTimestamp(java.lang.String p0, java.util.Calendar p1) throws java.sql.SQLException {
    return rs().getTimestamp(p0, p1);
  }

  public java.sql.Timestamp getTimestamp(java.lang.String p0) throws java.sql.SQLException {
    return rs().getTimestamp(p0);
  }

  public java.sql.Timestamp getTimestamp(int p0, java.util.Calendar p1) throws java.sql.SQLException {
    return rs().getTimestamp(p0, p1);
  }

  public java.sql.Timestamp getTimestamp(int p0) throws java.sql.SQLException {
    return rs().getTimestamp(p0);
  }

  public java.net.URL getURL(java.lang.String p0) throws java.sql.SQLException {
    return rs().getURL(p0);
  }

  public java.net.URL getURL(int p0) throws java.sql.SQLException {
    return rs().getURL(p0);
  }

  public java.io.InputStream getUnicodeStream(java.lang.String p0) throws java.sql.SQLException {
    return rs().getUnicodeStream(p0);
  }

  public java.io.InputStream getUnicodeStream(int p0) throws java.sql.SQLException {
    return rs().getUnicodeStream(p0);
  }

  public java.sql.SQLWarning getWarnings() throws java.sql.SQLException {
    return rs().getWarnings();
  }

  public void insertRow() throws java.sql.SQLException {
    rs().insertRow();
  }

  public boolean isAfterLast() throws java.sql.SQLException {
    return rs().isAfterLast();
  }

  public boolean isBeforeFirst() throws java.sql.SQLException {
    return rs().isBeforeFirst();
  }

  public boolean isClosed() throws java.sql.SQLException {
    return rs().isClosed();
  }

  public boolean isFirst() throws java.sql.SQLException {
    return rs().isFirst();
  }

  public boolean isLast() throws java.sql.SQLException {
    return rs().isLast();
  }

  public boolean last() throws java.sql.SQLException {
    return rs().last();
  }

  public void moveToCurrentRow() throws java.sql.SQLException {
    rs().moveToCurrentRow();
  }

  public void moveToInsertRow() throws java.sql.SQLException {
    rs().moveToInsertRow();
  }

  public boolean next() throws java.sql.SQLException {
    return rs().next();
  }

  public boolean previous() throws java.sql.SQLException {
    return rs().previous();
  }

  public void refreshRow() throws java.sql.SQLException {
    rs().refreshRow();
  }

  public boolean relative(int p0) throws java.sql.SQLException {
    return rs().relative(p0);
  }

  public boolean rowDeleted() throws java.sql.SQLException {
    return rs().rowDeleted();
  }

  public boolean rowInserted() throws java.sql.SQLException {
    return rs().rowInserted();
  }

  public boolean rowUpdated() throws java.sql.SQLException {
    return rs().rowUpdated();
  }

  public void updateArray(java.lang.String p0, java.sql.Array p1) throws java.sql.SQLException {
    rs().updateArray(p0, p1);
  }

  public void updateArray(int p0, java.sql.Array p1) throws java.sql.SQLException {
    rs().updateArray(p0, p1);
  }

  public void updateAsciiStream(java.lang.String p0, java.io.InputStream p1, int p2) throws java.sql.SQLException {
    rs().updateAsciiStream(p0, p1, p2);
  }

  public void updateAsciiStream(java.lang.String p0, java.io.InputStream p1, long p2) throws java.sql.SQLException {
    rs().updateAsciiStream(p0, p1, p2);
  }

  public void updateAsciiStream(java.lang.String p0, java.io.InputStream p1) throws java.sql.SQLException {
    rs().updateAsciiStream(p0, p1);
  }

  public void updateAsciiStream(int p0, java.io.InputStream p1, int p2) throws java.sql.SQLException {
    rs().updateAsciiStream(p0, p1, p2);
  }

  public void updateAsciiStream(int p0, java.io.InputStream p1, long p2) throws java.sql.SQLException {
    rs().updateAsciiStream(p0, p1, p2);
  }

  public void updateAsciiStream(int p0, java.io.InputStream p1) throws java.sql.SQLException {
    rs().updateAsciiStream(p0, p1);
  }

  public void updateBigDecimal(java.lang.String p0, java.math.BigDecimal p1) throws java.sql.SQLException {
    rs().updateBigDecimal(p0, p1);
  }

  public void updateBigDecimal(int p0, java.math.BigDecimal p1) throws java.sql.SQLException {
    rs().updateBigDecimal(p0, p1);
  }

  public void updateBinaryStream(java.lang.String p0, java.io.InputStream p1, int p2) throws java.sql.SQLException {
    rs().updateBinaryStream(p0, p1, p2);
  }

  public void updateBinaryStream(java.lang.String p0, java.io.InputStream p1, long p2) throws java.sql.SQLException {
    rs().updateBinaryStream(p0, p1, p2);
  }

  public void updateBinaryStream(java.lang.String p0, java.io.InputStream p1) throws java.sql.SQLException {
    rs().updateBinaryStream(p0, p1);
  }

  public void updateBinaryStream(int p0, java.io.InputStream p1, int p2) throws java.sql.SQLException {
    rs().updateBinaryStream(p0, p1, p2);
  }

  public void updateBinaryStream(int p0, java.io.InputStream p1, long p2) throws java.sql.SQLException {
    rs().updateBinaryStream(p0, p1, p2);
  }

  public void updateBinaryStream(int p0, java.io.InputStream p1) throws java.sql.SQLException {
    rs().updateBinaryStream(p0, p1);
  }

  public void updateBlob(java.lang.String p0, java.io.InputStream p1, long p2) throws java.sql.SQLException {
    rs().updateBlob(p0, p1, p2);
  }

  public void updateBlob(java.lang.String p0, java.io.InputStream p1) throws java.sql.SQLException {
    rs().updateBlob(p0, p1);
  }

  public void updateBlob(java.lang.String p0, java.sql.Blob p1) throws java.sql.SQLException {
    rs().updateBlob(p0, p1);
  }

  public void updateBlob(int p0, java.io.InputStream p1, long p2) throws java.sql.SQLException {
    rs().updateBlob(p0, p1, p2);
  }

  public void updateBlob(int p0, java.io.InputStream p1) throws java.sql.SQLException {
    rs().updateBlob(p0, p1);
  }

  public void updateBlob(int p0, java.sql.Blob p1) throws java.sql.SQLException {
    rs().updateBlob(p0, p1);
  }

  public void updateBoolean(java.lang.String p0, boolean p1) throws java.sql.SQLException {
    rs().updateBoolean(p0, p1);
  }

  public void updateBoolean(int p0, boolean p1) throws java.sql.SQLException {
    rs().updateBoolean(p0, p1);
  }

  public void updateByte(java.lang.String p0, byte p1) throws java.sql.SQLException {
    rs().updateByte(p0, p1);
  }

  public void updateByte(int p0, byte p1) throws java.sql.SQLException {
    rs().updateByte(p0, p1);
  }

  public void updateBytes(java.lang.String p0, byte[] p1) throws java.sql.SQLException {
    rs().updateBytes(p0, p1);
  }

  public void updateBytes(int p0, byte[] p1) throws java.sql.SQLException {
    rs().updateBytes(p0, p1);
  }

  public void updateCharacterStream(java.lang.String p0, java.io.Reader p1, int p2) throws java.sql.SQLException {
    rs().updateCharacterStream(p0, p1, p2);
  }

  public void updateCharacterStream(java.lang.String p0, java.io.Reader p1, long p2) throws java.sql.SQLException {
    rs().updateCharacterStream(p0, p1, p2);
  }

  public void updateCharacterStream(java.lang.String p0, java.io.Reader p1) throws java.sql.SQLException {
    rs().updateCharacterStream(p0, p1);
  }

  public void updateCharacterStream(int p0, java.io.Reader p1, int p2) throws java.sql.SQLException {
    rs().updateCharacterStream(p0, p1, p2);
  }

  public void updateCharacterStream(int p0, java.io.Reader p1, long p2) throws java.sql.SQLException {
    rs().updateCharacterStream(p0, p1, p2);
  }

  public void updateCharacterStream(int p0, java.io.Reader p1) throws java.sql.SQLException {
    rs().updateCharacterStream(p0, p1);
  }

  public void updateClob(java.lang.String p0, java.io.Reader p1, long p2) throws java.sql.SQLException {
    rs().updateClob(p0, p1, p2);
  }

  public void updateClob(java.lang.String p0, java.io.Reader p1) throws java.sql.SQLException {
    rs().updateClob(p0, p1);
  }

  public void updateClob(java.lang.String p0, java.sql.Clob p1) throws java.sql.SQLException {
    rs().updateClob(p0, p1);
  }

  public void updateClob(int p0, java.io.Reader p1, long p2) throws java.sql.SQLException {
    rs().updateClob(p0, p1, p2);
  }

  public void updateClob(int p0, java.io.Reader p1) throws java.sql.SQLException {
    rs().updateClob(p0, p1);
  }

  public void updateClob(int p0, java.sql.Clob p1) throws java.sql.SQLException {
    rs().updateClob(p0, p1);
  }

  public void updateDate(java.lang.String p0, java.sql.Date p1) throws java.sql.SQLException {
    rs().updateDate(p0, p1);
  }

  public void updateDate(int p0, java.sql.Date p1) throws java.sql.SQLException {
    rs().updateDate(p0, p1);
  }

  public void updateDouble(java.lang.String p0, double p1) throws java.sql.SQLException {
    rs().updateDouble(p0, p1);
  }

  public void updateDouble(int p0, double p1) throws java.sql.SQLException {
    rs().updateDouble(p0, p1);
  }

  public void updateFloat(java.lang.String p0, float p1) throws java.sql.SQLException {
    rs().updateFloat(p0, p1);
  }

  public void updateFloat(int p0, float p1) throws java.sql.SQLException {
    rs().updateFloat(p0, p1);
  }

  public void updateInt(java.lang.String p0, int p1) throws java.sql.SQLException {
    rs().updateInt(p0, p1);
  }

  public void updateInt(int p0, int p1) throws java.sql.SQLException {
    rs().updateInt(p0, p1);
  }

  public void updateLong(java.lang.String p0, long p1) throws java.sql.SQLException {
    rs().updateLong(p0, p1);
  }

  public void updateLong(int p0, long p1) throws java.sql.SQLException {
    rs().updateLong(p0, p1);
  }

  public void updateNCharacterStream(java.lang.String p0, java.io.Reader p1, long p2) throws java.sql.SQLException {
    rs().updateNCharacterStream(p0, p1, p2);
  }

  public void updateNCharacterStream(java.lang.String p0, java.io.Reader p1) throws java.sql.SQLException {
    rs().updateNCharacterStream(p0, p1);
  }

  public void updateNCharacterStream(int p0, java.io.Reader p1, long p2) throws java.sql.SQLException {
    rs().updateNCharacterStream(p0, p1, p2);
  }

  public void updateNCharacterStream(int p0, java.io.Reader p1) throws java.sql.SQLException {
    rs().updateNCharacterStream(p0, p1);
  }

  public void updateNClob(java.lang.String p0, java.io.Reader p1, long p2) throws java.sql.SQLException {
    rs().updateNClob(p0, p1, p2);
  }

  public void updateNClob(java.lang.String p0, java.io.Reader p1) throws java.sql.SQLException {
    rs().updateNClob(p0, p1);
  }

  public void updateNClob(java.lang.String p0, java.sql.NClob p1) throws java.sql.SQLException {
    rs().updateNClob(p0, p1);
  }

  public void updateNClob(int p0, java.io.Reader p1, long p2) throws java.sql.SQLException {
    rs().updateNClob(p0, p1, p2);
  }

  public void updateNClob(int p0, java.io.Reader p1) throws java.sql.SQLException {
    rs().updateNClob(p0, p1);
  }

  public void updateNClob(int p0, java.sql.NClob p1) throws java.sql.SQLException {
    rs().updateNClob(p0, p1);
  }

  public void updateNString(java.lang.String p0, java.lang.String p1) throws java.sql.SQLException {
    rs().updateNString(p0, p1);
  }

  public void updateNString(int p0, java.lang.String p1) throws java.sql.SQLException {
    rs().updateNString(p0, p1);
  }

  public void updateNull(java.lang.String p0) throws java.sql.SQLException {
    rs().updateNull(p0);
  }

  public void updateNull(int p0) throws java.sql.SQLException {
    rs().updateNull(p0);
  }

  public void updateObject(java.lang.String p0, java.lang.Object p1, int p2) throws java.sql.SQLException {
    rs().updateObject(p0, p1, p2);
  }

  public void updateObject(java.lang.String p0, java.lang.Object p1) throws java.sql.SQLException {
    rs().updateObject(p0, p1);
  }

  public void updateObject(int p0, java.lang.Object p1, int p2) throws java.sql.SQLException {
    rs().updateObject(p0, p1, p2);
  }

  public void updateObject(int p0, java.lang.Object p1) throws java.sql.SQLException {
    rs().updateObject(p0, p1);
  }

  public void updateRef(java.lang.String p0, java.sql.Ref p1) throws java.sql.SQLException {
    rs().updateRef(p0, p1);
  }

  public void updateRef(int p0, java.sql.Ref p1) throws java.sql.SQLException {
    rs().updateRef(p0, p1);
  }

  public void updateRow() throws java.sql.SQLException {
    rs().updateRow();
  }

  public void updateRowId(java.lang.String p0, java.sql.RowId p1) throws java.sql.SQLException {
    rs().updateRowId(p0, p1);
  }

  public void updateRowId(int p0, java.sql.RowId p1) throws java.sql.SQLException {
    rs().updateRowId(p0, p1);
  }

  public void updateSQLXML(java.lang.String p0, java.sql.SQLXML p1) throws java.sql.SQLException {
    rs().updateSQLXML(p0, p1);
  }

  public void updateSQLXML(int p0, java.sql.SQLXML p1) throws java.sql.SQLException {
    rs().updateSQLXML(p0, p1);
  }

  public void updateShort(java.lang.String p0, short p1) throws java.sql.SQLException {
    rs().updateShort(p0, p1);
  }

  public void updateShort(int p0, short p1) throws java.sql.SQLException {
    rs().updateShort(p0, p1);
  }

  public void updateString(java.lang.String p0, java.lang.String p1) throws java.sql.SQLException {
    rs().updateString(p0, p1);
  }

  public void updateString(int p0, java.lang.String p1) throws java.sql.SQLException {
    rs().updateString(p0, p1);
  }

  public void updateTime(java.lang.String p0, java.sql.Time p1) throws java.sql.SQLException {
    rs().updateTime(p0, p1);
  }

  public void updateTime(int p0, java.sql.Time p1) throws java.sql.SQLException {
    rs().updateTime(p0, p1);
  }

  public void updateTimestamp(java.lang.String p0, java.sql.Timestamp p1) throws java.sql.SQLException {
    rs().updateTimestamp(p0, p1);
  }

  public void updateTimestamp(int p0, java.sql.Timestamp p1) throws java.sql.SQLException {
    rs().updateTimestamp(p0, p1);
  }

  public boolean wasNull() throws java.sql.SQLException {
    return rs().wasNull();
  }
}
