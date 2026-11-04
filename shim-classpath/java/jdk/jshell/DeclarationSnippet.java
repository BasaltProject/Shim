/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

public abstract class DeclarationSnippet extends PersistentSnippet {
  DeclarationSnippet(String id, Kind k, SubKind sk, String source) {
    super(id, k, sk, source);
  }
}
