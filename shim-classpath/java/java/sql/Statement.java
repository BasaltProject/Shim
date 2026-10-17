/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface Statement extends Wrapper, AutoCloseable {
  int CLOSE_CURRENT_RESULT = 1;
  int KEEP_CURRENT_RESULT = 2;
  int CLOSE_ALL_RESULTS = 3;
  int SUCCESS_NO_INFO = -2;
  int EXECUTE_FAILED = -3;
  int RETURN_GENERATED_KEYS = 1;
  int NO_GENERATED_KEYS = 2;

  ResultSet executeQuery(String p0) throws SQLException;

  int executeUpdate(String p0) throws SQLException;

  void close() throws SQLException;

  int getMaxFieldSize() throws SQLException;

  void setMaxFieldSize(int p0) throws SQLException;

  int getMaxRows() throws SQLException;

  void setMaxRows(int p0) throws SQLException;

  void setEscapeProcessing(boolean p0) throws SQLException;

  int getQueryTimeout() throws SQLException;

  void setQueryTimeout(int p0) throws SQLException;

  void cancel() throws SQLException;

  SQLWarning getWarnings() throws SQLException;

  void clearWarnings() throws SQLException;

  void setCursorName(String p0) throws SQLException;

  boolean execute(String p0) throws SQLException;

  ResultSet getResultSet() throws SQLException;

  int getUpdateCount() throws SQLException;

  boolean getMoreResults() throws SQLException;

  void setFetchDirection(int p0) throws SQLException;

  int getFetchDirection() throws SQLException;

  void setFetchSize(int p0) throws SQLException;

  int getFetchSize() throws SQLException;

  int getResultSetConcurrency() throws SQLException;

  int getResultSetType() throws SQLException;

  void addBatch(String p0) throws SQLException;

  void clearBatch() throws SQLException;

  int[] executeBatch() throws SQLException;

  Connection getConnection() throws SQLException;

  boolean getMoreResults(int p0) throws SQLException;

  ResultSet getGeneratedKeys() throws SQLException;

  int executeUpdate(String p0, int p1) throws SQLException;

  int executeUpdate(String p0, int[] p1) throws SQLException;

  int executeUpdate(String p0, String[] p1) throws SQLException;

  boolean execute(String p0, int p1) throws SQLException;

  boolean execute(String p0, int[] p1) throws SQLException;

  boolean execute(String p0, String[] p1) throws SQLException;

  int getResultSetHoldability() throws SQLException;

  boolean isClosed() throws SQLException;

  void setPoolable(boolean p0) throws SQLException;

  boolean isPoolable() throws SQLException;

  void closeOnCompletion() throws SQLException;

  boolean isCloseOnCompletion() throws SQLException;

  default long getLargeUpdateCount() throws SQLException {
    throw new UnsupportedOperationException("getLargeUpdateCount not implemented");
  }

  default void setLargeMaxRows(long p0) throws SQLException {
    throw new UnsupportedOperationException("setLargeMaxRows not implemented");
  }

  default long getLargeMaxRows() throws SQLException {
    return 0;
  }

  default long[] executeLargeBatch() throws SQLException {
    throw new UnsupportedOperationException("executeLargeBatch not implemented");
  }

  default long executeLargeUpdate(String p0) throws SQLException {
    throw new UnsupportedOperationException("executeLargeUpdate not implemented");
  }

  default long executeLargeUpdate(String p0, int p1) throws SQLException {
    throw new SQLFeatureNotSupportedException("executeLargeUpdate not implemented");
  }

  default long executeLargeUpdate(String p0, int[] p1) throws SQLException {
    throw new SQLFeatureNotSupportedException("executeLargeUpdate not implemented");
  }

  default long executeLargeUpdate(String p0, String[] p1) throws SQLException {
    throw new SQLFeatureNotSupportedException("executeLargeUpdate not implemented");
  }

  default String enquoteLiteral(String p0) throws SQLException {
    return "'" + p0.replace("'", "''") + "'";
  }

  default String enquoteIdentifier(String p0, boolean p1) throws SQLException {
    int len = p0.length();
    if (len < 1 || len > 128) {
      throw new SQLException("Invalid name");
    }
    if (java.util.regex.Pattern.compile("[\\p{Alpha}][\\p{Alnum}_]*").matcher(p0).matches()) {
      return p1 ? "\"" + p0 + "\"" : p0;
    }
    if (p0.matches("^\".+\"$")) {
      p0 = p0.substring(1, len - 1);
    }
    if (java.util.regex.Pattern.compile("[^\u0000\"]+").matcher(p0).matches()) {
      return "\"" + p0 + "\"";
    }
    throw new SQLException("Invalid name");
  }

  default boolean isSimpleIdentifier(String p0) throws SQLException {
    int len = p0.length();
    return len >= 1 && len <= 128
        && java.util.regex.Pattern.compile("[\\p{Alpha}][\\p{Alnum}_]*").matcher(p0).matches();
  }

  default String enquoteNCharLiteral(String p0) throws SQLException {
    return "N'" + p0.replace("'", "''") + "'";
  }
}
