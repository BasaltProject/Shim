/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

public class TypeDeclSnippet extends DeclarationSnippet {
  TypeDeclSnippet(String id, SubKind sk, String source) {
    super(id, Kind.TYPE_DECL, sk, source);
  }
}
