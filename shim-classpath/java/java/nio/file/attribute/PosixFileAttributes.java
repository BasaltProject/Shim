/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file.attribute;

import java.io.IOException;
import java.util.Set;

public interface PosixFileAttributes extends BasicFileAttributes {
  UserPrincipal owner();

  GroupPrincipal group();

  Set<PosixFilePermission> permissions();
}
