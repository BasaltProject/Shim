/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

final class ThreadData {
  final String name;
  final long id;
  final boolean virtual;
  final String[] groups;

  private ThreadData(String name, long id, boolean virtual, String[] groups) {
    this.name = name;
    this.id = id;
    this.virtual = virtual;
    this.groups = groups;
  }

  static ThreadData create(String name, long id, boolean virtual) {
    return new ThreadData(name, id, virtual, new String[0]);
  }

  static ThreadData of(Thread t) {
    java.util.ArrayList<String> g = new java.util.ArrayList<String>();
    ThreadGroup tg = t.getThreadGroup();
    while (tg != null) {
      g.add(tg.getName());
      tg = tg.getParent();
    }
    boolean v = false;
    try {
      v = t.isVirtual();
    } catch (Throwable e) {
    }
    return new ThreadData(t.getName(), t.getId(), v, g.toArray(new String[0]));
  }
}
