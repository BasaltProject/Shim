/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface ThreadGroupReference extends com.sun.jdi.ObjectReference {
  java.lang.String name();
  com.sun.jdi.ThreadGroupReference parent();
  void resume();
  void suspend();
  java.util.List<com.sun.jdi.ThreadGroupReference> threadGroups();
  java.util.List<com.sun.jdi.ThreadReference> threads();
}
