/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.security;

public class ProtectionDomain {
  private final CodeSource codesource;
  private final ClassLoader classloader;
  private final Principal[] principals;
  private final PermissionCollection permissions;
  private final boolean staticPermissions;

  public final boolean staticPermissionsOnly() {
    return staticPermissions;
  }

  public ProtectionDomain(CodeSource codesource, PermissionCollection permissions) {
    this.codesource = codesource;
    if (permissions != null) {
      permissions.setReadOnly();
    }
    this.permissions = permissions;
    this.classloader = null;
    this.principals = new Principal[0];
    this.staticPermissions = true;
  }

  public ProtectionDomain(CodeSource codesource, PermissionCollection permissions, ClassLoader classloader,
      Principal[] principals) {
    this.codesource = codesource;
    if (permissions != null) {
      permissions.setReadOnly();
    }
    this.permissions = permissions;
    this.classloader = classloader;
    this.principals = principals != null ? principals.clone() : new Principal[0];
    this.staticPermissions = false;
  }

  public final CodeSource getCodeSource() {
    return codesource;
  }

  public final ClassLoader getClassLoader() {
    return classloader;
  }

  public final Principal[] getPrincipals() {
    return principals.clone();
  }

  public final PermissionCollection getPermissions() {
    return permissions;
  }

  public boolean implies(Permission perm) {
    return permissions != null && permissions.implies(perm);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ProtectionDomain  ");
    sb.append(codesource).append('\n').append(' ').append(classloader).append('\n');
    if (principals.length == 0) {
      sb.append(" <no principals>\n");
    } else {
      sb.append(" principals:\n");
      for (Principal p : principals) {
        sb.append("  ").append(p).append('\n');
      }
    }
    sb.append(' ').append(permissions).append('\n');
    return sb.toString();
  }
}
