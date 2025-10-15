/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public enum PseudoColumnUsage {
  SELECT_LIST_ONLY,
  WHERE_CLAUSE_ONLY,
  NO_USAGE_RESTRICTIONS,
  USAGE_UNKNOWN
}
