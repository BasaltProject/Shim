/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface Connection extends Wrapper, AutoCloseable {
  int TRANSACTION_NONE = 0;
  int TRANSACTION_READ_UNCOMMITTED = 1;
  int TRANSACTION_READ_COMMITTED = 2;
  int TRANSACTION_REPEATABLE_READ = 4;
  int TRANSACTION_SERIALIZABLE = 8;

  Statement createStatement() throws SQLException;

  PreparedStatement prepareStatement(String p0) throws SQLException;

  CallableStatement prepareCall(String p0) throws SQLException;

  String nativeSQL(String p0) throws SQLException;

  void setAutoCommit(boolean p0) throws SQLException;

  boolean getAutoCommit() throws SQLException;

  void commit() throws SQLException;

  void rollback() throws SQLException;

  void close() throws SQLException;

  boolean isClosed() throws SQLException;

  DatabaseMetaData getMetaData() throws SQLException;

  void setReadOnly(boolean p0) throws SQLException;

  boolean isReadOnly() throws SQLException;

  void setCatalog(String p0) throws SQLException;

  String getCatalog() throws SQLException;

  void setTransactionIsolation(int p0) throws SQLException;

  int getTransactionIsolation() throws SQLException;

  SQLWarning getWarnings() throws SQLException;

  void clearWarnings() throws SQLException;

  Statement createStatement(int p0, int p1) throws SQLException;

  PreparedStatement prepareStatement(String p0, int p1, int p2) throws SQLException;

  CallableStatement prepareCall(String p0, int p1, int p2) throws SQLException;

  java.util.Map<String, Class<?>> getTypeMap() throws SQLException;

  void setTypeMap(java.util.Map<String, Class<?>> p0) throws SQLException;

  void setHoldability(int p0) throws SQLException;

  int getHoldability() throws SQLException;

  Savepoint setSavepoint() throws SQLException;

  Savepoint setSavepoint(String p0) throws SQLException;

  void rollback(Savepoint p0) throws SQLException;

  void releaseSavepoint(Savepoint p0) throws SQLException;

  Statement createStatement(int p0, int p1, int p2) throws SQLException;

  PreparedStatement prepareStatement(String p0, int p1, int p2, int p3) throws SQLException;

  CallableStatement prepareCall(String p0, int p1, int p2, int p3) throws SQLException;

  PreparedStatement prepareStatement(String p0, int p1) throws SQLException;

  PreparedStatement prepareStatement(String p0, int[] p1) throws SQLException;

  PreparedStatement prepareStatement(String p0, String[] p1) throws SQLException;

  Clob createClob() throws SQLException;

  Blob createBlob() throws SQLException;

  NClob createNClob() throws SQLException;

  SQLXML createSQLXML() throws SQLException;

  boolean isValid(int p0) throws SQLException;

  void setClientInfo(String p0, String p1) throws SQLClientInfoException;

  void setClientInfo(java.util.Properties p0) throws SQLClientInfoException;

  String getClientInfo(String p0) throws SQLException;

  java.util.Properties getClientInfo() throws SQLException;

  Array createArrayOf(String p0, Object[] p1) throws SQLException;

  Struct createStruct(String p0, Object[] p1) throws SQLException;

  void setSchema(String p0) throws SQLException;

  String getSchema() throws SQLException;

  void abort(java.util.concurrent.Executor p0) throws SQLException;

  void setNetworkTimeout(java.util.concurrent.Executor p0, int p1) throws SQLException;

  int getNetworkTimeout() throws SQLException;

  default void beginRequest() throws SQLException {
  }

  default void endRequest() throws SQLException {
  }

  default boolean setShardingKeyIfValid(ShardingKey p0, ShardingKey p1, int p2) throws SQLException {
    throw new SQLFeatureNotSupportedException("setShardingKeyIfValid not implemented");
  }

  default boolean setShardingKeyIfValid(ShardingKey p0, int p1) throws SQLException {
    throw new SQLFeatureNotSupportedException("setShardingKeyIfValid not implemented");
  }

  default void setShardingKey(ShardingKey p0, ShardingKey p1) throws SQLException {
    throw new SQLFeatureNotSupportedException("setShardingKey not implemented");
  }

  default void setShardingKey(ShardingKey p0) throws SQLException {
    throw new SQLFeatureNotSupportedException("setShardingKey not implemented");
  }
}
