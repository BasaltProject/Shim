/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public interface ShardingKeyBuilder {
  ShardingKeyBuilder subkey(Object p0, SQLType p1);

  ShardingKey build() throws SQLException;
}
