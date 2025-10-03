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

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class Permissions extends PermissionCollection {
  private static final long serialVersionUID = 4858622370623524688L;

  private final Map<Class,PermissionCollection> collections = new HashMap();

  public void add(Permission p) {
    Class c = p.getClass();
    PermissionCollection pc = collections.get(c);
    if (pc == null) {
      pc = p.newPermissionCollection();
      if (pc == null) {
        pc = new MyPermissionCollection();
      }
      collections.put(c, pc);
    }
    pc.add(p);
  }

  private static class MyPermissionCollection extends PermissionCollection {
    private final Set<Permission> permissions = new HashSet();

    public void add(Permission p) {
      permissions.add(p);
    }
  }
}
