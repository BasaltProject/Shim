/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

public class VarSnippet extends DeclarationSnippet {
  VarSnippet(String id, SubKind sk, String source) {
    super(id, Kind.VAR, sk, source);
  }

  public String typeName() {
    return typeName;
  }
}
