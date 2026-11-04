/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.type;

public enum TypeKind {
  BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE, VOID, NONE, NULL, ARRAY, DECLARED, ERROR, TYPEVAR, WILDCARD, PACKAGE, EXECUTABLE, OTHER, UNION, INTERSECTION, MODULE;


  public boolean isPrimitive() {
    switch (this) {
      case BOOLEAN: case BYTE: case SHORT: case INT: case LONG: case CHAR: case FLOAT: case DOUBLE:
        return true;
      default:
        return false;
    }
  }
}
