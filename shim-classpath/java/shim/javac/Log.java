/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.List;

/** Diagnostics sink with speculative suppression and buffering. */
public final class Log {
  public interface Sink {
    void error(CompilationUnitTree cu, Tree at, String code, String message);
  }

  private final Sink sink;
  private final List<List<Object[]>> buffers = new ArrayList<List<Object[]>>();
  CompilationUnitTree cu;
  int speculative;
  int specErrors;
  int errors;

  Log(Sink sink) {
    this.sink = sink;
  }

  void begin() {
    buffers.add(new ArrayList<Object[]>());
  }

  void discard() {
    buffers.remove(buffers.size() - 1);
  }

  void commit() {
    List<Object[]> l = buffers.remove(buffers.size() - 1);
    for (Object[] e : l) {
      CompilationUnitTree saved = cu;
      cu = (CompilationUnitTree) e[0];
      error((Tree) e[1], (String) e[2], (String) e[3]);
      cu = saved;
    }
  }

  void error(Tree at, String code, String message) {
    if (speculative > 0) {
      specErrors++;
      return;
    }
    if (!buffers.isEmpty()) {
      buffers.get(buffers.size() - 1).add(new Object[] {cu, at, code, message});
      return;
    }
    errors++;
    if (sink != null) {
      sink.error(cu, at, code, message);
    }
  }
}
