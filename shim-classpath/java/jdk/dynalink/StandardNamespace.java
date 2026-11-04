/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink;

public enum StandardNamespace implements Namespace {
  PROPERTY,
  ELEMENT,
  METHOD;

  public static StandardNamespace findFirst(Operation op) {
    for (Namespace ns : NamespaceOperation.getNamespaces(NamedOperation.getBaseOperation(op))) {
      if (ns instanceof StandardNamespace) {
        return (StandardNamespace) ns;
      }
    }
    return null;
  }
}
