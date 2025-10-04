/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file.attribute;

import java.io.IOException;
import java.util.List;

public interface AclFileAttributeView extends FileOwnerAttributeView {
  @Override
  String name();

  List<AclEntry> getAcl() throws IOException;

  void setAcl(List<AclEntry> acl) throws IOException;
}
