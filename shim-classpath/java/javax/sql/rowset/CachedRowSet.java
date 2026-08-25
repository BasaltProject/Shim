/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset;

public interface CachedRowSet extends javax.sql.RowSet, javax.sql.rowset.Joinable {
  boolean COMMIT_ON_ACCEPT_CHANGES = true;

  void acceptChanges() throws javax.sql.rowset.spi.SyncProviderException;
  void acceptChanges(java.sql.Connection connection) throws javax.sql.rowset.spi.SyncProviderException;
  boolean columnUpdated(java.lang.String string) throws java.sql.SQLException;
  boolean columnUpdated(int i) throws java.sql.SQLException;
  void commit() throws java.sql.SQLException;
  javax.sql.rowset.CachedRowSet createCopy() throws java.sql.SQLException;
  javax.sql.rowset.CachedRowSet createCopyNoConstraints() throws java.sql.SQLException;
  javax.sql.rowset.CachedRowSet createCopySchema() throws java.sql.SQLException;
  javax.sql.RowSet createShared() throws java.sql.SQLException;
  void execute(java.sql.Connection connection) throws java.sql.SQLException;
  int[] getKeyColumns() throws java.sql.SQLException;
  java.sql.ResultSet getOriginal() throws java.sql.SQLException;
  java.sql.ResultSet getOriginalRow() throws java.sql.SQLException;
  int getPageSize();
  javax.sql.rowset.RowSetWarning getRowSetWarnings() throws java.sql.SQLException;
  boolean getShowDeleted() throws java.sql.SQLException;
  javax.sql.rowset.spi.SyncProvider getSyncProvider() throws java.sql.SQLException;
  java.lang.String getTableName() throws java.sql.SQLException;
  boolean nextPage() throws java.sql.SQLException;
  void populate(java.sql.ResultSet resultSet, int i) throws java.sql.SQLException;
  void populate(java.sql.ResultSet resultSet) throws java.sql.SQLException;
  boolean previousPage() throws java.sql.SQLException;
  void release() throws java.sql.SQLException;
  void restoreOriginal() throws java.sql.SQLException;
  void rollback() throws java.sql.SQLException;
  void rollback(java.sql.Savepoint savepoint) throws java.sql.SQLException;
  void rowSetPopulated(javax.sql.RowSetEvent rowSetEvent, int i) throws java.sql.SQLException;
  void setKeyColumns(int[] is) throws java.sql.SQLException;
  void setMetaData(javax.sql.RowSetMetaData rowSetMetaData) throws java.sql.SQLException;
  void setOriginalRow() throws java.sql.SQLException;
  void setPageSize(int i) throws java.sql.SQLException;
  void setShowDeleted(boolean flag) throws java.sql.SQLException;
  void setSyncProvider(java.lang.String string) throws java.sql.SQLException;
  void setTableName(java.lang.String string) throws java.sql.SQLException;
  int size();
  java.util.Collection<?> toCollection() throws java.sql.SQLException;
  java.util.Collection<?> toCollection(java.lang.String string) throws java.sql.SQLException;
  java.util.Collection<?> toCollection(int i) throws java.sql.SQLException;
  void undoDelete() throws java.sql.SQLException;
  void undoInsert() throws java.sql.SQLException;
  void undoUpdate() throws java.sql.SQLException;
}
