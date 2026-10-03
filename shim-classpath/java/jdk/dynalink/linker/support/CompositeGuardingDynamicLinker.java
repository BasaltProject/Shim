/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.linker.support;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;

public class CompositeGuardingDynamicLinker implements GuardingDynamicLinker {
  private final GuardingDynamicLinker[] linkers;

  public CompositeGuardingDynamicLinker(Iterable<? extends GuardingDynamicLinker> linkers) {
    final List<GuardingDynamicLinker> l = new LinkedList<>();
    for (GuardingDynamicLinker linker : linkers) {
      l.add(linker);
    }
    this.linkers = l.toArray(new GuardingDynamicLinker[0]);
  }

  @Override
  public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices)
      throws Exception {
    for (final GuardingDynamicLinker linker : linkers) {
      final GuardedInvocation invocation = linker.getGuardedInvocation(linkRequest, linkerServices);
      if (invocation != null) {
        return invocation;
      }
    }
    return null;
  }
}
