/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface Struct {
  String getSQLTypeName() throws SQLException;

  Object[] getAttributes() throws SQLException;

  Object[] getAttributes(java.util.Map<String, Class<?>> p0) throws SQLException;
}
