/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public final class AccessControlContext {
  private final ProtectionDomain[] context;
  private final DomainCombiner combiner;

  public AccessControlContext(ProtectionDomain[] context) {
    this.context = context.length == 0 ? null : context.clone();
    this.combiner = null;
  }

  public AccessControlContext(AccessControlContext acc, DomainCombiner combiner) {
    this.context = acc.context;
    this.combiner = combiner;
  }

  public DomainCombiner getDomainCombiner() {
    return combiner;
  }

  public void checkPermission(Permission perm) throws AccessControlException {
    throw new AccessControlException("checking permissions is not supported");
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof AccessControlContext)) {
      return false;
    }
    AccessControlContext that = (AccessControlContext) obj;
    if (combiner != that.combiner) {
      return false;
    }
    if (context == null || that.context == null) {
      return context == that.context;
    }
    if (context.length != that.context.length) {
      return false;
    }
    for (int i = 0; i < context.length; i++) {
      if (context[i] != that.context[i] && (context[i] == null || !context[i].equals(that.context[i]))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int h = 0;
    if (context != null) {
      for (ProtectionDomain d : context) {
        if (d != null) {
          h ^= d.hashCode();
        }
      }
    }
    return h;
  }
}
