/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset;

public interface JoinRowSet extends javax.sql.rowset.WebRowSet {
  int CROSS_JOIN = 0;
  int INNER_JOIN = 1;
  int LEFT_OUTER_JOIN = 2;
  int RIGHT_OUTER_JOIN = 3;
  int FULL_JOIN = 4;

  void addRowSet(javax.sql.RowSet[] rowSets, int[] is) throws java.sql.SQLException;
  void addRowSet(javax.sql.RowSet[] rowSets, java.lang.String[] strings) throws java.sql.SQLException;
  void addRowSet(javax.sql.RowSet rowSet, java.lang.String string) throws java.sql.SQLException;
  void addRowSet(javax.sql.RowSet rowSet, int i) throws java.sql.SQLException;
  void addRowSet(javax.sql.rowset.Joinable joinable) throws java.sql.SQLException;
  int getJoinType() throws java.sql.SQLException;
  java.lang.String[] getRowSetNames() throws java.sql.SQLException;
  java.util.Collection<?> getRowSets() throws java.sql.SQLException;
  java.lang.String getWhereClause() throws java.sql.SQLException;
  void setJoinType(int i) throws java.sql.SQLException;
  boolean supportsCrossJoin();
  boolean supportsFullJoin();
  boolean supportsInnerJoin();
  boolean supportsLeftOuterJoin();
  boolean supportsRightOuterJoin();
  javax.sql.rowset.CachedRowSet toCachedRowSet() throws java.sql.SQLException;
}
