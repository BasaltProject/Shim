/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

public class ExpressionSnippet extends Snippet {
  ExpressionSnippet(String id, SubKind sk, String source) {
    super(id, Kind.EXPRESSION, sk, source);
  }

  public String name() {
    return name;
  }

  public String typeName() {
    return typeName;
  }
}
