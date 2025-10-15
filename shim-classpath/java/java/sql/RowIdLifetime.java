/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

public enum RowIdLifetime {
  ROWID_UNSUPPORTED,
  ROWID_VALID_OTHER,
  ROWID_VALID_SESSION,
  ROWID_VALID_TRANSACTION,
  ROWID_VALID_FOREVER
}
