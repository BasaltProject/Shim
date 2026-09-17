/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;

/** Detects assert statements that belong to a class itself rather than to a nested class. */
final class AssertScan {
  private AssertScan() {
  }

  static boolean hasAssert(Attr a, final CSym c) {
    final boolean[] found = new boolean[1];
    new TreeScanner<Void, Void>() {
      public Void visitClass(ClassTree t, Void p) {
        if (t == c.tree) {
          return super.visitClass(t, p);
        }
        return null;
      }

      public Void visitNewClass(NewClassTree t, Void p) {
        scan(t.getEnclosingExpression(), p);
        scan(t.getArguments(), p);
        return null;
      }

      public Void visitAssert(AssertTree t, Void p) {
        found[0] = true;
        return null;
      }
    }.scan(c.tree, null);
    return found[0];
  }
}
