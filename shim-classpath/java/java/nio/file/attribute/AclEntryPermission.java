/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file.attribute;

public enum AclEntryPermission {
  READ_DATA,
  WRITE_DATA,
  APPEND_DATA,
  READ_NAMED_ATTRS,
  WRITE_NAMED_ATTRS,
  EXECUTE,
  DELETE_CHILD,
  READ_ATTRIBUTES,
  WRITE_ATTRIBUTES,
  DELETE,
  READ_ACL,
  WRITE_ACL,
  WRITE_OWNER,
  SYNCHRONIZE;

  public static final AclEntryPermission LIST_DIRECTORY = READ_DATA;
  public static final AclEntryPermission ADD_FILE = WRITE_DATA;
  public static final AclEntryPermission ADD_SUBDIRECTORY = APPEND_DATA;
}
