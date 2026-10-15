/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

public enum StandardOpenOption implements OpenOption {
  READ,
  WRITE,
  APPEND,
  TRUNCATE_EXISTING,
  CREATE,
  CREATE_NEW,
  DELETE_ON_CLOSE,
  SPARSE,
  SYNC,
  DSYNC
}
