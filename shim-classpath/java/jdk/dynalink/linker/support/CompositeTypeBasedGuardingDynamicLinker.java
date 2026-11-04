/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.linker.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;

public class CompositeTypeBasedGuardingDynamicLinker implements TypeBasedGuardingDynamicLinker {
  private final ClassValue<List<TypeBasedGuardingDynamicLinker>> classToLinker;

  public CompositeTypeBasedGuardingDynamicLinker(Iterable<? extends TypeBasedGuardingDynamicLinker> linkers) {
    final List<TypeBasedGuardingDynamicLinker> l = new LinkedList<>();
    for (TypeBasedGuardingDynamicLinker linker : linkers) {
      l.add(linker);
    }
    classToLinker = new ClassValue<List<TypeBasedGuardingDynamicLinker>>() {
      @Override
      protected List<TypeBasedGuardingDynamicLinker> computeValue(Class<?> clazz) {
        final List<TypeBasedGuardingDynamicLinker> list = new LinkedList<>();
        for (TypeBasedGuardingDynamicLinker linker : l) {
          if (linker.canLinkType(clazz)) {
            list.add(linker);
          }
        }
        switch (list.size()) {
          case 0:
            return Collections.emptyList();
          case 1:
            return Collections.singletonList(list.get(0));
          default:
            return Arrays.asList(list.toArray(new TypeBasedGuardingDynamicLinker[0]));
        }
      }
    };
  }

  @Override
  public boolean canLinkType(Class<?> type) {
    return !classToLinker.get(type).isEmpty();
  }

  @Override
  public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices)
      throws Exception {
    final Object obj = linkRequest.getReceiver();
    if (obj == null) {
      return null;
    }
    for (final TypeBasedGuardingDynamicLinker linker : classToLinker.get(obj.getClass())) {
      final GuardedInvocation invocation = linker.getGuardedInvocation(linkRequest, linkerServices);
      if (invocation != null) {
        return invocation;
      }
    }
    return null;
  }

  public static List<GuardingDynamicLinker> optimize(Iterable<? extends GuardingDynamicLinker> linkers) {
    final List<GuardingDynamicLinker> llinkers = new LinkedList<>();
    final List<TypeBasedGuardingDynamicLinker> tblinkers = new LinkedList<>();
    for (GuardingDynamicLinker linker : linkers) {
      if (linker instanceof TypeBasedGuardingDynamicLinker) {
        tblinkers.add((TypeBasedGuardingDynamicLinker) linker);
      } else {
        addTypeBased(llinkers, tblinkers);
        llinkers.add(linker);
      }
    }
    addTypeBased(llinkers, tblinkers);
    return llinkers;
  }

  private static void addTypeBased(List<GuardingDynamicLinker> llinkers, List<TypeBasedGuardingDynamicLinker> tblinkers) {
    switch (tblinkers.size()) {
      case 0:
        break;
      case 1:
        llinkers.addAll(tblinkers);
        break;
      default:
        llinkers.add(new CompositeTypeBasedGuardingDynamicLinker(tblinkers));
        break;
    }
    tblinkers.clear();
  }
}
