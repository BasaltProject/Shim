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

import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Stream;

public abstract class PermissionCollection implements java.io.Serializable {
  private static final long serialVersionUID = -6727011328946861783L;

  private volatile boolean readOnly;

  public PermissionCollection() {
  }

  public abstract void add(Permission permission);

  public abstract boolean implies(Permission permission);

  public abstract Enumeration<Permission> elements();

  public Stream<Permission> elementsAsStream() {
    return Collections.list(elements()).stream();
  }

  public void setReadOnly() {
    readOnly = true;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  @Override
  public String toString() {
    Enumeration<Permission> enum_ = elements();
    StringBuilder sb = new StringBuilder();
    sb.append(super.toString()).append(" (\n");
    while (enum_.hasMoreElements()) {
      try {
        Permission p = enum_.nextElement();
        sb.append(' ').append(p).append('\n');
      } catch (java.util.NoSuchElementException e) {
      }
    }
    sb.append(')').append('\n');
    return sb.toString();
  }
}
