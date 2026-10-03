/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Objects;

public class SecureLookupSupplier {
  private static final RuntimePermission GET_LOOKUP_PERMISSION = new RuntimePermission(
      SecureLookupSupplier.GET_LOOKUP_PERMISSION_NAME);

  public static final String GET_LOOKUP_PERMISSION_NAME = "dynalink.getLookup";

  private final Lookup lookup;

  public SecureLookupSupplier(Lookup lookup) {
    this.lookup = Objects.requireNonNull(lookup, "lookup");
  }

  public final Lookup getLookup() {
    final SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      sm.checkPermission(GET_LOOKUP_PERMISSION);
    }
    return lookup;
  }

  protected final Lookup getLookupPrivileged() {
    return lookup;
  }
}
