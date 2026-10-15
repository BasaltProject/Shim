/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

public enum FileVisitResult {
  CONTINUE,
  TERMINATE,
  SKIP_SUBTREE,
  SKIP_SIBLINGS
}
