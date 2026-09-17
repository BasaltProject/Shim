/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

public abstract class PersistentSnippet extends Snippet {
  PersistentSnippet(String id, Kind k, SubKind sk, String source) {
    super(id, k, sk, source);
  }

  public String name() {
    return name;
  }
}
