/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.CompilationUnitTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Enter, attribute and generate a set of compilation units. */
public final class Pipeline {
  final Symtab st;
  final Tx tx;
  final Log log;
  final Enter enter;
  final Attr attr;
  final List<CompilationUnitTree> units;
  boolean attributed;

  public Pipeline(List<? extends CompilationUnitTree> units, ClassLoader loader, Log.Sink sink) {
    this.units = new ArrayList<CompilationUnitTree>(units);
    this.st = new Symtab(loader);
    this.tx = new Tx(st);
    this.log = new Log(sink);
    this.enter = new Enter(st, tx, log);
    this.attr = new Attr(st, tx, enter, log);
  }

  public void attribute() {
    if (attributed) {
      return;
    }
    attributed = true;
    enter.enterUnits(units);
    attr.attribAll();
  }

  public int errors() {
    return log.errors;
  }

  public Map<String, byte[]> generate() {
    attribute();
    if (log.errors > 0) {
      return new java.util.LinkedHashMap<String, byte[]>();
    }
    Gen gen = new Gen(attr, log);
    Map<String, byte[]> out = gen.generate(new ArrayList<CSym>(enter.sourceClasses));
    gen.generatePackageInfos(units);
    return out;
  }
}
