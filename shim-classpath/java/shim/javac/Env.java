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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Lexical environment of attribution. */
final class Env {
  final Env outer;
  final CuEnv cu;
  CSym cls;
  boolean isClassEnv;
  MSym method;
  boolean staticCtx;
  Map<String, VSym> vars;
  Map<String, CSym> localClasses;
  Map<String, TVSym> tvars;
  Object scopeOwner;
  Ty returnType;
  Tree lambda;
  Tree enclosingStmt;
  boolean ctorPrologue;
  List<Ty> lambdaReturns;
  boolean inLambdaBody;
  int loopDepth;
  List<String> labels;
  Env switchEnv;
  AttrStat.YieldCtx yieldCtx;
  boolean breakableSwitch;

  AttrStat.YieldCtx switchEnv_ctx() {
    return yieldCtx;
  }

  void setYieldCtx(AttrStat.YieldCtx y) {
    yieldCtx = y;
  }

  Env(Env outer, CuEnv cu) {
    this.outer = outer;
    this.cu = cu;
    if (outer != null) {
      cls = outer.cls;
      method = outer.method;
      staticCtx = outer.staticCtx;
      scopeOwner = outer.scopeOwner;
      returnType = outer.returnType;
      lambda = outer.lambda;
      lambdaReturns = outer.lambdaReturns;
      inLambdaBody = outer.inLambdaBody;
      ctorPrologue = outer.ctorPrologue;
      yieldCtx = outer.yieldCtx;
    }
  }

  Env dup() {
    return new Env(this, cu);
  }

  void addVar(VSym v) {
    if (vars == null) {
      vars = new HashMap<String, VSym>();
    }
    vars.put(v.name, v);
  }

  void addLocalClass(CSym c) {
    if (localClasses == null) {
      localClasses = new HashMap<String, CSym>();
    }
    localClasses.put(c.name, c);
  }

  void addTvar(TVSym t) {
    if (tvars == null) {
      tvars = new HashMap<String, TVSym>();
    }
    tvars.put(t.name, t);
  }

  /** Finds a local variable visible from here without crossing a class boundary. */
  VSym findLocal(String name) {
    for (Env e = this; e != null; e = e.outer) {
      if (e.vars != null) {
        VSym v = e.vars.get(name);
        if (v != null) {
          return v;
        }
      }
      if (e.isClassEnv) {
        return null;
      }
    }
    return null;
  }

  static final class CuEnv {
    final CompilationUnitTree tree;
    String pkg = "";
    final Map<String, CSym> single = new HashMap<String, CSym>();
    final List<String> onDemandPkgs = new ArrayList<String>();
    final List<CSym> onDemandClasses = new ArrayList<CSym>();
    final Map<String, List<CSym>> staticSingle = new HashMap<String, List<CSym>>();
    final List<CSym> staticOnDemand = new ArrayList<CSym>();
    final Map<String, List<CSym>> staticSingleTypes = new HashMap<String, List<CSym>>();
    boolean resolved;

    CuEnv(CompilationUnitTree tree) {
      this.tree = tree;
    }
  }
}
