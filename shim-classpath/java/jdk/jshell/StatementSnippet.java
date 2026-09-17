/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

public class StatementSnippet extends Snippet {
  StatementSnippet(String id, String source) {
    super(id, Kind.STATEMENT, SubKind.STATEMENT_SUBKIND, source);
  }
}
