/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.*;
import java.util.List;

/** Reachability analysis following JLS 14.22. */
final class Flow {
  private Flow() {
  }

  static boolean isTrue(Attr a, ExpressionTree e) {
    if (e == null) {
      return true;
    }
    Object c = a.consts.get(e);
    return c instanceof Boolean && ((Boolean) c).booleanValue();
  }

  static boolean canComplete(Attr a, StatementTree s) {
    if (s == null) {
      return true;
    }
    switch (s.getKind()) {
      case BLOCK: {
        for (StatementTree st : ((BlockTree) s).getStatements()) {
          if (!canComplete(a, st)) {
            return false;
          }
        }
        return true;
      }
      case RETURN:
      case THROW:
      case BREAK:
      case CONTINUE:
      case YIELD:
        return false;
      case IF: {
        IfTree i = (IfTree) s;
        if (i.getElseStatement() == null) {
          return true;
        }
        return canComplete(a, i.getThenStatement()) || canComplete(a, i.getElseStatement());
      }
      case WHILE_LOOP: {
        WhileLoopTree w = (WhileLoopTree) s;
        return !isTrue(a, w.getCondition()) || breaksOut(w.getStatement(), null, 0);
      }
      case DO_WHILE_LOOP: {
        DoWhileLoopTree d = (DoWhileLoopTree) s;
        boolean bodyDone = canComplete(a, d.getStatement()) || continues(d.getStatement(), null, 0);
        return bodyDone && !isTrue(a, d.getCondition()) || breaksOut(d.getStatement(), null, 0);
      }
      case FOR_LOOP: {
        ForLoopTree f = (ForLoopTree) s;
        return f.getCondition() != null && !isTrue(a, f.getCondition()) || breaksOut(f.getStatement(), null, 0);
      }
      case LABELED_STATEMENT: {
        LabeledStatementTree l = (LabeledStatementTree) s;
        return canComplete(a, l.getStatement()) || breaksOut(l.getStatement(), l.getLabel().toString(), 1);
      }
      case SYNCHRONIZED:
        return canComplete(a, ((SynchronizedTree) s).getBlock());
      case TRY: {
        TryTree t = (TryTree) s;
        boolean ok = canComplete(a, t.getBlock());
        for (CatchTree c : t.getCatches()) {
          ok |= canComplete(a, c.getBlock());
        }
        if (t.getFinallyBlock() != null && !canComplete(a, t.getFinallyBlock())) {
          return false;
        }
        return ok;
      }
      case SWITCH:
        return switchCompletes(a, (SwitchTree) s);
      default:
        return true;
    }
  }

  private static boolean switchCompletes(Attr a, SwitchTree sw) {
    List<? extends CaseTree> cases = sw.getCases();
    boolean hasDefault = false;
    for (CaseTree c : cases) {
      for (CaseLabelTree l : c.getLabels()) {
        if (l instanceof DefaultCaseLabelTree) {
          hasDefault = true;
        }
      }
      if (c.getLabels().isEmpty()) {
        hasDefault = true;
      }
    }
    Object sd = a.syms.get(sw);
    boolean exhaustive = hasDefault || sd instanceof AttrStat.SwitchData && ((AttrStat.SwitchData) sd).exhaustive;
    if (!exhaustive) {
      return true;
    }
    if (cases.isEmpty()) {
      return true;
    }
    boolean arrow = cases.get(0).getCaseKind() == CaseTree.CaseKind.RULE;
    if (arrow) {
      for (CaseTree c : cases) {
        Tree body = c.getBody();
        if (body instanceof ExpressionTree) {
          return true;
        }
        if (body instanceof StatementTree && canComplete(a, (StatementTree) body)) {
          return true;
        }
        if (breaksOut((StatementTree) body, null, 0)) {
          return true;
        }
      }
      return false;
    }
    for (CaseTree c : cases) {
      List<? extends StatementTree> ss = c.getStatements();
      if (ss != null) {
        for (StatementTree st : ss) {
          if (breaksOut(st, null, 0)) {
            return true;
          }
        }
      }
    }
    CaseTree last = cases.get(cases.size() - 1);
    List<? extends StatementTree> ls = last.getStatements();
    if (ls == null || ls.isEmpty()) {
      return true;
    }
    for (StatementTree st : ls) {
      if (!canComplete(a, st)) {
        return false;
      }
    }
    return true;
  }

  /** Whether the statement contains a break that exits the enclosing statement. */
  static boolean breaksOut(Tree t, String label, int depthMode) {
    return scan(t, label, 0, false);
  }

  static boolean continues(Tree t, String label, int depthMode) {
    return scan(t, label, 0, true);
  }

  private static boolean scan(Tree t, String label, int nest, boolean wantContinue) {
    if (t == null) {
      return false;
    }
    switch (t.getKind()) {
      case BREAK: {
        if (wantContinue) {
          return false;
        }
        BreakTree b = (BreakTree) t;
        if (b.getLabel() == null) {
          return nest == 0 && label == null;
        }
        return label != null && b.getLabel().toString().equals(label);
      }
      case CONTINUE: {
        if (!wantContinue) {
          return false;
        }
        ContinueTree c = (ContinueTree) t;
        if (c.getLabel() == null) {
          return nest == 0;
        }
        return label != null && c.getLabel().toString().equals(label);
      }
      case BLOCK:
        for (StatementTree s : ((BlockTree) t).getStatements()) {
          if (scan(s, label, nest, wantContinue)) {
            return true;
          }
        }
        return false;
      case IF:
        return scan(((IfTree) t).getThenStatement(), label, nest, wantContinue)
            || scan(((IfTree) t).getElseStatement(), label, nest, wantContinue);
      case WHILE_LOOP:
        return scan(((WhileLoopTree) t).getStatement(), label, nest + 1, wantContinue);
      case DO_WHILE_LOOP:
        return scan(((DoWhileLoopTree) t).getStatement(), label, nest + 1, wantContinue);
      case FOR_LOOP:
        return scan(((ForLoopTree) t).getStatement(), label, nest + 1, wantContinue);
      case ENHANCED_FOR_LOOP:
        return scan(((EnhancedForLoopTree) t).getStatement(), label, nest + 1, wantContinue);
      case LABELED_STATEMENT:
        return scan(((LabeledStatementTree) t).getStatement(), label, nest, wantContinue);
      case SYNCHRONIZED:
        return scan(((SynchronizedTree) t).getBlock(), label, nest, wantContinue);
      case TRY: {
        TryTree tr = (TryTree) t;
        if (scan(tr.getBlock(), label, nest, wantContinue)) {
          return true;
        }
        for (CatchTree c : tr.getCatches()) {
          if (scan(c.getBlock(), label, nest, wantContinue)) {
            return true;
          }
        }
        return scan(tr.getFinallyBlock(), label, nest, wantContinue);
      }
      case SWITCH: {
        for (CaseTree c : ((SwitchTree) t).getCases()) {
          if (c.getStatements() != null) {
            for (StatementTree s : c.getStatements()) {
              if (scan(s, label, nest + (wantContinue ? 0 : 1), wantContinue)) {
                return true;
              }
            }
          } else if (c.getBody() instanceof StatementTree
              && scan(c.getBody(), label, nest + (wantContinue ? 0 : 1), wantContinue)) {
            return true;
          }
        }
        return false;
      }
      default:
        return false;
    }
  }
}
