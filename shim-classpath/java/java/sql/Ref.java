/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface Ref {
  String getBaseTypeName() throws SQLException;

  Object getObject(java.util.Map<String, Class<?>> p0) throws SQLException;

  Object getObject() throws SQLException;

  void setObject(Object p0) throws SQLException;
}
