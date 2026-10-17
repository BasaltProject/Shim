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

public interface PosixFileAttributeView extends BasicFileAttributeView, FileOwnerAttributeView {
  @Override
  String name();

  @Override
  PosixFileAttributes readAttributes() throws IOException;

  void setPermissions(Set<PosixFilePermission> perms) throws IOException;

  void setGroup(GroupPrincipal group) throws IOException;
}
