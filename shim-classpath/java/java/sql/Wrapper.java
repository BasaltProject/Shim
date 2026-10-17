/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface Wrapper {
  <T> T unwrap(Class<T> p0) throws SQLException;

  boolean isWrapperFor(Class<?> p0) throws SQLException;
}
