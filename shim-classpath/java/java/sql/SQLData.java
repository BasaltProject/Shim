/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface SQLData {
  String getSQLTypeName() throws SQLException;

  void readSQL(SQLInput p0, String p1) throws SQLException;

  void writeSQL(SQLOutput p0) throws SQLException;
}
