/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset;

public interface Joinable {
  int[] getMatchColumnIndexes() throws java.sql.SQLException;
  java.lang.String[] getMatchColumnNames() throws java.sql.SQLException;
  void setMatchColumn(int[] is) throws java.sql.SQLException;
  void setMatchColumn(java.lang.String[] strings) throws java.sql.SQLException;
  void setMatchColumn(java.lang.String string) throws java.sql.SQLException;
  void setMatchColumn(int i) throws java.sql.SQLException;
  void unsetMatchColumn(int[] is) throws java.sql.SQLException;
  void unsetMatchColumn(java.lang.String[] strings) throws java.sql.SQLException;
  void unsetMatchColumn(java.lang.String string) throws java.sql.SQLException;
  void unsetMatchColumn(int i) throws java.sql.SQLException;
}
