/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

public class ImportSnippet extends PersistentSnippet {
  String fullname;
  boolean isStatic;

  ImportSnippet(String id, SubKind sk, String source) {
    super(id, Kind.IMPORT, sk, source);
  }

  public String fullname() {
    return fullname;
  }

  public boolean isStatic() {
    return isStatic;
  }
}
