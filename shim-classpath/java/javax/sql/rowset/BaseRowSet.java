/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset;

import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import javax.sql.RowSet;
import javax.sql.RowSetEvent;
import javax.sql.RowSetListener;

public abstract class BaseRowSet implements Serializable, Cloneable {
  private static final long serialVersionUID = 4886719666485113312L;

  public static final int UNICODE_STREAM_PARAM = 0;
  public static final int BINARY_STREAM_PARAM = 1;
  public static final int ASCII_STREAM_PARAM = 2;

  protected InputStream binaryStream;
  protected InputStream unicodeStream;
  protected InputStream asciiStream;
  protected Reader charStream;

  private String command;
  private String URL;
  private String dataSource;
  private transient String username;
  private transient String password;
  private int rowSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
  private boolean showDeleted = false;
  private int queryTimeout = 0;
  private int maxRows = 0;
  private int maxFieldSize = 0;
  private int concurrency = ResultSet.CONCUR_UPDATABLE;
  private boolean readOnly;
  private boolean escapeProcessing = true;
  private int isolation;
  private int fetchDir = ResultSet.FETCH_FORWARD;
  private int fetchSize = 0;
  private Map<String, Class<?>> map;
  private Vector<RowSetListener> listeners;
  private Hashtable<Integer, Object> params;

  public BaseRowSet() {
    listeners = new Vector<RowSetListener>();
  }

  protected void initParams() {
    params = new Hashtable<Integer, Object>();
  }

  public void addRowSetListener(RowSetListener listener) {
    listeners.add(listener);
  }

  public void removeRowSetListener(RowSetListener listener) {
    listeners.remove(listener);
  }

  private void checkParamIndex(int idx, String method) throws SQLException {
    if ((idx < 1)) {
      throw new SQLException("Invalid Parameter Index");
    }
    if (params == null) {
      throw new SQLException("Set initParams() before " + method);
    }
  }

  protected void notifyCursorMoved() throws SQLException {
    if (!listeners.isEmpty()) {
      for (RowSetListener l : new Vector<RowSetListener>(listeners)) {
        l.cursorMoved(new RowSetEvent((RowSet) this));
      }
    }
  }

  protected void notifyRowChanged() throws SQLException {
    if (!listeners.isEmpty()) {
      for (RowSetListener l : new Vector<RowSetListener>(listeners)) {
        l.rowChanged(new RowSetEvent((RowSet) this));
      }
    }
  }

  protected void notifyRowSetChanged() throws SQLException {
    if (!listeners.isEmpty()) {
      for (RowSetListener l : new Vector<RowSetListener>(listeners)) {
        l.rowSetChanged(new RowSetEvent((RowSet) this));
      }
    }
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String cmd) throws SQLException {
    if (cmd == null) {
      command = null;
    } else {
      if (cmd.length() == 0) {
        throw new SQLException("Invalid command string detected. Cannot be of length less than 0");
      }
      if (params == null) {
        throw new SQLException("Set initParams() before setCommand");
      }
      params.clear();
      command = cmd;
    }
  }

  public String getUrl() throws SQLException {
    return URL;
  }

  public void setUrl(String url) throws SQLException {
    if (url != null) {
      if (url.length() < 1) {
        throw new SQLException("Invalid url string detected. Cannot be of length less than 1");
      }
      URL = url;
    }
    dataSource = null;
  }

  public String getDataSourceName() {
    return dataSource;
  }

  public void setDataSourceName(String name) throws SQLException {
    if (name == null) {
      dataSource = null;
    } else if (name.equals("")) {
      throw new SQLException("DataSource name cannot be empty string");
    } else {
      dataSource = name;
    }
    URL = null;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String name) {
    if (name == null) {
      username = null;
    } else {
      username = name;
    }
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String pass) {
    if (pass == null) {
      password = null;
    } else {
      password = pass;
    }
  }

  public void setType(int type) throws SQLException {
    if ((type != ResultSet.TYPE_FORWARD_ONLY) && (type != ResultSet.TYPE_SCROLL_INSENSITIVE)
        && (type != ResultSet.TYPE_SCROLL_SENSITIVE)) {
      throw new SQLException("Invalid type of RowSet set. Must be either "
          + "ResultSet.TYPE_FORWARD_ONLY or ResultSet.TYPE_SCROLL_INSENSITIVE "
          + "or ResultSet.TYPE_SCROLL_SENSITIVE.");
    }
    this.rowSetType = type;
  }

  public int getType() throws SQLException {
    return rowSetType;
  }

  public void setConcurrency(int concurrency) throws SQLException {
    if ((concurrency != ResultSet.CONCUR_READ_ONLY) && (concurrency != ResultSet.CONCUR_UPDATABLE)) {
      throw new SQLException("Invalid concurrency set. Must be either "
          + "ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE.");
    }
    this.concurrency = concurrency;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(boolean value) {
    readOnly = value;
  }

  public int getTransactionIsolation() {
    return isolation;
  }

  public void setTransactionIsolation(int level) throws SQLException {
    if ((level != java.sql.Connection.TRANSACTION_NONE)
        && (level != java.sql.Connection.TRANSACTION_READ_COMMITTED)
        && (level != java.sql.Connection.TRANSACTION_READ_UNCOMMITTED)
        && (level != java.sql.Connection.TRANSACTION_REPEATABLE_READ)
        && (level != java.sql.Connection.TRANSACTION_SERIALIZABLE)) {
      throw new SQLException("Invalid transaction isolation set. Must be "
          + "either Connection.TRANSACTION_NONE or Connection.TRANSACTION_READ_UNCOMMITTED or "
          + "Connection.TRANSACTION_READ_COMMITTED or Connection.TRANSACTION_REPEATABLE_READ or "
          + "Connection.TRANSACTION_SERIALIZABLE");
    }
    this.isolation = level;
  }

  public Map<String, Class<?>> getTypeMap() {
    return map;
  }

  public void setTypeMap(Map<String, Class<?>> map) {
    this.map = map;
  }

  public int getMaxFieldSize() throws SQLException {
    return maxFieldSize;
  }

  public void setMaxFieldSize(int max) throws SQLException {
    if (max < 0) {
      throw new SQLException("Invalid max field size set. Cannot be of value: " + max);
    }
    maxFieldSize = max;
  }

  public int getMaxRows() throws SQLException {
    return maxRows;
  }

  public void setMaxRows(int max) throws SQLException {
    if (max < 0) {
      throw new SQLException("Invalid max row size set. Cannot be of value: " + max);
    } else if (max < this.getFetchSize()) {
      throw new SQLException("Invalid max row size set. Cannot be less than the fetchSize.");
    }
    this.maxRows = max;
  }

  public void setEscapeProcessing(boolean enable) throws SQLException {
    escapeProcessing = enable;
  }

  public boolean getEscapeProcessing() throws SQLException {
    return escapeProcessing;
  }

  public void setQueryTimeout(int seconds) throws SQLException {
    if (seconds < 0) {
      throw new SQLException("Invalid query timeout value set. Cannot be of value: " + seconds);
    }
    queryTimeout = seconds;
  }

  public int getQueryTimeout() throws SQLException {
    return queryTimeout;
  }

  public void setShowDeleted(boolean value) throws SQLException {
    showDeleted = value;
  }

  public boolean getShowDeleted() throws SQLException {
    return showDeleted;
  }

  public int getConcurrency() throws SQLException {
    return concurrency;
  }

  public void setFetchDirection(int direction) throws SQLException {
    if ((getType() == ResultSet.TYPE_FORWARD_ONLY) && (direction != ResultSet.FETCH_FORWARD)) {
      throw new SQLException("Invalid Fetch Direction");
    }
    fetchDir = direction;
  }

  public int getFetchDirection() throws SQLException {
    return fetchDir;
  }

  public void setFetchSize(int rows) throws SQLException {
    if (getMaxRows() == 0 && rows >= 0) {
      fetchSize = rows;
      return;
    }
    if (rows < 0) {
      throw new SQLException("Invalid fetch size set. Cannot be of value: " + rows);
    } else if (rows > getMaxRows()) {
      throw new SQLException("Invalid fetch size set. Cannot be of value: " + rows);
    }
    fetchSize = rows;
  }

  public int getFetchSize() throws SQLException {
    return fetchSize;
  }

  public void clearParameters() throws SQLException {
    params.clear();
  }

  public Object[] getParams() throws SQLException {
    if (params == null) {
      initParams();
      Object[] paramsArray = new Object[params.size()];
      return paramsArray;
    }
    Object[] paramsArray = new Object[params.size()];
    for (int i = 0; i < params.size(); i++) {
      paramsArray[i] = params.get(Integer.valueOf(i));
      if (paramsArray[i] == null) {
        throw new SQLException("missing parameter: " + (i + 1));
      }
    }
    return paramsArray;
  }

  public void setNull(int parameterIndex, int sqlType) throws SQLException {
    checkParamIndex(parameterIndex, "setNull");
    params.put(Integer.valueOf(parameterIndex - 1), new Object[] {null, Integer.valueOf(sqlType)});
  }

  public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
    checkParamIndex(parameterIndex, "setNull");
    params.put(Integer.valueOf(parameterIndex - 1), new Object[] {null, Integer.valueOf(sqlType), typeName});
  }

  public void setBoolean(int parameterIndex, boolean x) throws SQLException {
    checkParamIndex(parameterIndex, "setBoolean");
    params.put(Integer.valueOf(parameterIndex - 1), Boolean.valueOf(x));
  }

  public void setByte(int parameterIndex, byte x) throws SQLException {
    checkParamIndex(parameterIndex, "setByte");
    params.put(Integer.valueOf(parameterIndex - 1), Byte.valueOf(x));
  }

  public void setShort(int parameterIndex, short x) throws SQLException {
    checkParamIndex(parameterIndex, "setShort");
    params.put(Integer.valueOf(parameterIndex - 1), Short.valueOf(x));
  }

  public void setInt(int parameterIndex, int x) throws SQLException {
    checkParamIndex(parameterIndex, "setInt");
    params.put(Integer.valueOf(parameterIndex - 1), Integer.valueOf(x));
  }

  public void setLong(int parameterIndex, long x) throws SQLException {
    checkParamIndex(parameterIndex, "setLong");
    params.put(Integer.valueOf(parameterIndex - 1), Long.valueOf(x));
  }

  public void setFloat(int parameterIndex, float x) throws SQLException {
    checkParamIndex(parameterIndex, "setFloat");
    params.put(Integer.valueOf(parameterIndex - 1), Float.valueOf(x));
  }

  public void setDouble(int parameterIndex, double x) throws SQLException {
    checkParamIndex(parameterIndex, "setDouble");
    params.put(Integer.valueOf(parameterIndex - 1), Double.valueOf(x));
  }

  public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
    checkParamIndex(parameterIndex, "setBigDecimal");
    params.put(Integer.valueOf(parameterIndex - 1), x);
  }

  public void setString(int parameterIndex, String x) throws SQLException {
    checkParamIndex(parameterIndex, "setString");
    params.put(Integer.valueOf(parameterIndex - 1), x);
  }

  public void setBytes(int parameterIndex, byte[] x) throws SQLException {
    checkParamIndex(parameterIndex, "setBytes");
    params.put(Integer.valueOf(parameterIndex - 1), x);
  }

  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
    checkParamIndex(parameterIndex, "setTimestamp");
    params.put(Integer.valueOf(parameterIndex - 1), x);
  }

  public void setDate(int parameterIndex, Date x) throws SQLException {
    checkParamIndex(parameterIndex, "setDate");
    params.put(Integer.valueOf(parameterIndex - 1), x);
  }

  public void setTime(int parameterIndex, Time x) throws SQLException {
    checkParamIndex(parameterIndex, "setTime");
    params.put(Integer.valueOf(parameterIndex - 1), x);
  }

  public void setRef(int parameterIndex, Ref ref) throws SQLException {
    checkParamIndex(parameterIndex, "setRef");
    params.put(Integer.valueOf(parameterIndex - 1), ref);
  }

  public void setBlob(int parameterIndex, Blob x) throws SQLException {
    checkParamIndex(parameterIndex, "setBlob");
    params.put(Integer.valueOf(parameterIndex - 1), x);
  }

  public void setClob(int parameterIndex, Clob x) throws SQLException {
    checkParamIndex(parameterIndex, "setClob");
    params.put(Integer.valueOf(parameterIndex - 1), x);
  }

  public void setArray(int parameterIndex, Array array) throws SQLException {
    checkParamIndex(parameterIndex, "setArray");
    params.put(Integer.valueOf(parameterIndex - 1), array);
  }

  public void setURL(int parameterIndex, URL x) throws SQLException {
    checkParamIndex(parameterIndex, "setURL");
    params.put(Integer.valueOf(parameterIndex - 1), x);
  }

  public void setRowId(int parameterIndex, RowId x) throws SQLException {
    checkParamIndex(parameterIndex, "setRowId");
    params.put(Integer.valueOf(parameterIndex - 1), x);
  }

  public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
    checkParamIndex(parameterIndex, "setSQLXML");
    params.put(Integer.valueOf(parameterIndex - 1), xmlObject);
  }

  public void setNString(int parameterIndex, String value) throws SQLException {
    checkParamIndex(parameterIndex, "setNString");
    params.put(Integer.valueOf(parameterIndex - 1), value);
  }

  public void setNClob(int parameterIndex, NClob value) throws SQLException {
    checkParamIndex(parameterIndex, "setNClob");
    params.put(Integer.valueOf(parameterIndex - 1), value);
  }

  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
    checkParamIndex(parameterIndex, "setTimestamp");
    params.put(Integer.valueOf(parameterIndex - 1), new Object[] {x, cal});
  }

  public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
    checkParamIndex(parameterIndex, "setDate");
    params.put(Integer.valueOf(parameterIndex - 1), new Object[] {x, cal});
  }

  public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
    checkParamIndex(parameterIndex, "setTime");
    params.put(Integer.valueOf(parameterIndex - 1), new Object[] {x, cal});
  }

  public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
    checkParamIndex(parameterIndex, "setObject");
    params.put(Integer.valueOf(parameterIndex - 1), new Object[] {x, Integer.valueOf(targetSqlType), Integer.valueOf(scale)});
  }

  public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
    checkParamIndex(parameterIndex, "setObject");
    params.put(Integer.valueOf(parameterIndex - 1), new Object[] {x, Integer.valueOf(targetSqlType)});
  }

  public void setObject(int parameterIndex, Object x) throws SQLException {
    checkParamIndex(parameterIndex, "setObject");
    params.put(Integer.valueOf(parameterIndex - 1), x);
  }

  public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
    checkParamIndex(parameterIndex, "setAsciiStream");
    asciiStream = x;
    params.put(Integer.valueOf(parameterIndex - 1), new Object[] {x, Integer.valueOf(length)});
  }

  public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
    checkParamIndex(parameterIndex, "setBinaryStream");
    binaryStream = x;
    params.put(Integer.valueOf(parameterIndex - 1), new Object[] {x, Integer.valueOf(length)});
  }

  public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
    checkParamIndex(parameterIndex, "setUnicodeStream");
    unicodeStream = x;
    params.put(Integer.valueOf(parameterIndex - 1), new Object[] {x, Integer.valueOf(length)});
  }

  public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
    checkParamIndex(parameterIndex, "setCharacterStream");
    charStream = reader;
    params.put(Integer.valueOf(parameterIndex - 1), new Object[] {reader, Integer.valueOf(length)});
  }

  public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
    checkParamIndex(parameterIndex, "setAsciiStream");
    params.put(Integer.valueOf(parameterIndex - 1), x);
  }

  public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
    checkParamIndex(parameterIndex, "setBinaryStream");
    params.put(Integer.valueOf(parameterIndex - 1), x);
  }

  public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
    checkParamIndex(parameterIndex, "setCharacterStream");
    params.put(Integer.valueOf(parameterIndex - 1), reader);
  }

  public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
    checkParamIndex(parameterIndex, "setNCharacterStream");
    params.put(Integer.valueOf(parameterIndex - 1), value);
  }

  public void setClob(int parameterIndex, Reader reader) throws SQLException {
    checkParamIndex(parameterIndex, "setClob");
    params.put(Integer.valueOf(parameterIndex - 1), reader);
  }

  public void setNClob(int parameterIndex, Reader reader) throws SQLException {
    checkParamIndex(parameterIndex, "setNClob");
    params.put(Integer.valueOf(parameterIndex - 1), reader);
  }

  public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
    checkParamIndex(parameterIndex, "setBlob");
    params.put(Integer.valueOf(parameterIndex - 1), inputStream);
  }

  public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
    checkParamIndex(parameterIndex, "setNCharacterStream");
    params.put(Integer.valueOf(parameterIndex - 1), new Object[] {value, Long.valueOf(length)});
  }

  public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
    checkParamIndex(parameterIndex, "setClob");
    params.put(Integer.valueOf(parameterIndex - 1), new Object[] {reader, Long.valueOf(length)});
  }

  public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
    checkParamIndex(parameterIndex, "setNClob");
    params.put(Integer.valueOf(parameterIndex - 1), new Object[] {reader, Long.valueOf(length)});
  }

  public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
    checkParamIndex(parameterIndex, "setBlob");
    params.put(Integer.valueOf(parameterIndex - 1), new Object[] {inputStream, Long.valueOf(length)});
  }

  public void setNull(String parameterName, int sqlType) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setBoolean(String parameterName, boolean x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setByte(String parameterName, byte x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setShort(String parameterName, short x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setInt(String parameterName, int x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setLong(String parameterName, long x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setFloat(String parameterName, float x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setDouble(String parameterName, double x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setString(String parameterName, String x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setBytes(String parameterName, byte[] x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setObject(String parameterName, Object x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setBlob(String parameterName, Blob x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setClob(String parameterName, Reader reader, long length) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setClob(String parameterName, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setClob(String parameterName, Clob x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setNClob(String parameterName, Reader reader) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setNClob(String parameterName, NClob value) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setNString(String parameterName, String value) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setRowId(String parameterName, RowId x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setDate(String parameterName, Date x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setTime(String parameterName, Time x) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }

  public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
    throw new SQLFeatureNotSupportedException("Feature not supported");
  }
}
