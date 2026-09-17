/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

public class MethodSnippet extends DeclarationSnippet {
  MethodSnippet(String id, String source) {
    super(id, Kind.METHOD, SubKind.METHOD_SUBKIND, source);
  }

  public String parameterTypes() {
    return paramTypes;
  }

  public String signature() {
    return signature;
  }

  public String toString() {
    return "MethodSnippet:" + name + "/" + signature + "-" + source;
  }
}
