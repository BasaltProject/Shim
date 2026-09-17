/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

public class ErroneousSnippet extends Snippet {
  Kind probable = Kind.ERRONEOUS;

  ErroneousSnippet(String id, String source) {
    super(id, Kind.ERRONEOUS, SubKind.UNKNOWN_SUBKIND, source);
  }

  public Kind probableKind() {
    return probable;
  }
}
