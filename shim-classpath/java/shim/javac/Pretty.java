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
import javax.lang.model.element.Modifier;

public final class Pretty implements TreeVisitor<Void, Void> {
  private static final int kNotExpression = -1;
  private static final int kNoPrec = 0;
  private static final int kAssignPrec = 1;
  private static final int kAssignopPrec = 2;
  private static final int kCondPrec = 3;
  private static final int kOrPrec = 4;
  private static final int kAndPrec = 5;
  private static final int kBitorPrec = 6;
  private static final int kBitxorPrec = 7;
  private static final int kBitandPrec = 8;
  private static final int kEqPrec = 9;
  private static final int kOrdPrec = 10;
  private static final int kShiftPrec = 11;
  private static final int kAddPrec = 12;
  private static final int kMulPrec = 13;
  private static final int kPrefixPrec = 14;
  private static final int kPostfixPrec = 15;

  private static final String LS = System.lineSeparator();

  private final StringBuilder out = new StringBuilder();
  private int lmargin;
  private int prec;
  private String enclClassName;

  private Pretty() {
  }

  public static String print(Tree tree) {
    Pretty p = new Pretty();
    if (tree instanceof ExpressionStatementTree) {
      p.printStat(tree);
    } else {
      p.printExpr(tree, kNoPrec);
    }
    return p.out.toString();
  }

  private void align() {
    for (int i = 0; i < lmargin; i++) {
      out.append(' ');
    }
  }

  private void indent() {
    lmargin += 4;
  }

  private void undent() {
    lmargin -= 4;
  }

  private void open(int contextPrec, int ownPrec) {
    if (ownPrec < contextPrec) {
      out.append('(');
    }
  }

  private void close(int contextPrec, int ownPrec) {
    if (ownPrec < contextPrec) {
      out.append(')');
    }
  }

  private void print(Object s) {
    out.append(s);
  }

  private void println() {
    out.append(LS);
  }

  private void printExpr(Tree tree, int newPrec) {
    int prev = this.prec;
    try {
      this.prec = newPrec;
      if (tree == null) {
        print("/*missing*/");
      } else {
        tree.accept(this, null);
      }
    } finally {
      this.prec = prev;
    }
  }

  private void printExpr(Tree tree) {
    printExpr(tree, kNoPrec);
  }

  private void printStat(Tree tree) {
    printExpr(tree, kNotExpression);
  }

  private void printExprs(List<? extends Tree> trees, String sep) {
    boolean first = true;
    if (trees == null) {
      return;
    }
    for (Tree t : trees) {
      if (!first) {
        print(sep);
      }
      printExpr(t);
      first = false;
    }
  }

  private void printExprs(List<? extends Tree> trees) {
    printExprs(trees, ", ");
  }

  private void printStats(List<? extends Tree> trees) {
    if (trees == null) {
      return;
    }
    for (Tree t : trees) {
      align();
      printStat(t);
      println();
    }
  }

  private void printAnnotations(List<? extends AnnotationTree> trees) {
    if (trees == null) {
      return;
    }
    for (AnnotationTree t : trees) {
      printStat(t);
      println();
      align();
    }
  }

  private void printTypeAnnotations(List<? extends AnnotationTree> trees) {
    if (trees == null) {
      return;
    }
    for (AnnotationTree t : trees) {
      printExpr(t);
      print(" ");
    }
  }

  private static final Modifier[] kOrder = {Modifier.PUBLIC, Modifier.PRIVATE, Modifier.PROTECTED, Modifier.STATIC,
      Modifier.FINAL, Modifier.SYNCHRONIZED, Modifier.VOLATILE, Modifier.TRANSIENT, Modifier.NATIVE,
      Modifier.ABSTRACT, Modifier.STRICTFP, Modifier.DEFAULT, Modifier.SEALED, Modifier.NON_SEALED};

  private void printFlags(java.util.Set<Modifier> flags, boolean annotationType) {
    StringBuilder sb = new StringBuilder();
    for (Modifier m : kOrder) {
      if (flags.contains(m)) {
        if (sb.length() > 0) {
          sb.append(' ');
        }
        sb.append(m.toString());
      }
    }
    print(sb);
    if (sb.length() > 0) {
      print(" ");
    }
    if (annotationType) {
      print("@");
    }
  }

  private void printTypeParameters(List<? extends TypeParameterTree> trees) {
    if (trees != null && !trees.isEmpty()) {
      print("<");
      printExprs(trees);
      print(">");
    }
  }

  private void printBlock(List<? extends Tree> stats) {
    print("{");
    println();
    indent();
    printStats(stats);
    undent();
    align();
    print("}");
  }

  private static boolean isEnumerator(Tree t) {
    return t instanceof N.Variable && ((N.Variable) t).enumConstant;
  }

  private void printEnumBody(List<? extends Tree> stats) {
    print("{");
    println();
    indent();
    boolean first = true;
    for (Tree t : stats) {
      if (isEnumerator(t)) {
        if (!first) {
          print(",");
          println();
        }
        align();
        printStat(t);
        first = false;
      }
    }
    print(";");
    println();
    for (Tree t : stats) {
      if (!isEnumerator(t)) {
        align();
        printStat(t);
        println();
      }
    }
    undent();
    align();
    print("}");
  }

  private void printParenCond(ExpressionTree cond) {
    if (cond instanceof ParenthesizedTree) {
      printExpr(cond);
    } else {
      print("(");
      printExpr(cond);
      print(")");
    }
  }

  private static String quote(String s, boolean chr) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\b':
          sb.append("\\b");
          break;
        case '\f':
          sb.append("\\f");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        case '\'':
          sb.append(chr ? "\\'" : "'");
          break;
        case '"':
          sb.append("\\\"");
          break;
        case '\\':
          sb.append("\\\\");
          break;
        default:
          if (c >= 32 && c < 127) {
            sb.append(c);
          } else {
            sb.append(String.format("\\u%04x", (int) c));
          }
      }
    }
    return sb.toString();
  }

  private static int opPrec(Tree.Kind k) {
    switch (k) {
      case POSTFIX_INCREMENT:
      case POSTFIX_DECREMENT:
        return kPostfixPrec;
      case UNARY_PLUS:
      case UNARY_MINUS:
      case LOGICAL_COMPLEMENT:
      case BITWISE_COMPLEMENT:
      case PREFIX_INCREMENT:
      case PREFIX_DECREMENT:
        return kPrefixPrec;
      case CONDITIONAL_OR:
        return kOrPrec;
      case CONDITIONAL_AND:
        return kAndPrec;
      case OR:
        return kBitorPrec;
      case XOR:
        return kBitxorPrec;
      case AND:
        return kBitandPrec;
      case EQUAL_TO:
      case NOT_EQUAL_TO:
        return kEqPrec;
      case LESS_THAN:
      case GREATER_THAN:
      case LESS_THAN_EQUAL:
      case GREATER_THAN_EQUAL:
        return kOrdPrec;
      case LEFT_SHIFT:
      case RIGHT_SHIFT:
      case UNSIGNED_RIGHT_SHIFT:
        return kShiftPrec;
      case PLUS:
      case MINUS:
        return kAddPrec;
      case MULTIPLY:
      case DIVIDE:
      case REMAINDER:
        return kMulPrec;
      default:
        return kNoPrec;
    }
  }

  private static String opName(Tree.Kind k) {
    switch (k) {
      case POSTFIX_INCREMENT:
      case PREFIX_INCREMENT:
        return "++";
      case POSTFIX_DECREMENT:
      case PREFIX_DECREMENT:
        return "--";
      case UNARY_PLUS:
      case PLUS:
      case PLUS_ASSIGNMENT:
        return "+";
      case UNARY_MINUS:
      case MINUS:
      case MINUS_ASSIGNMENT:
        return "-";
      case LOGICAL_COMPLEMENT:
        return "!";
      case BITWISE_COMPLEMENT:
        return "~";
      case CONDITIONAL_OR:
        return "||";
      case CONDITIONAL_AND:
        return "&&";
      case OR:
      case OR_ASSIGNMENT:
        return "|";
      case XOR:
      case XOR_ASSIGNMENT:
        return "^";
      case AND:
      case AND_ASSIGNMENT:
        return "&";
      case EQUAL_TO:
        return "==";
      case NOT_EQUAL_TO:
        return "!=";
      case LESS_THAN:
        return "<";
      case GREATER_THAN:
        return ">";
      case LESS_THAN_EQUAL:
        return "<=";
      case GREATER_THAN_EQUAL:
        return ">=";
      case LEFT_SHIFT:
      case LEFT_SHIFT_ASSIGNMENT:
        return "<<";
      case RIGHT_SHIFT:
      case RIGHT_SHIFT_ASSIGNMENT:
        return ">>";
      case UNSIGNED_RIGHT_SHIFT:
      case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
        return ">>>";
      case MULTIPLY:
      case MULTIPLY_ASSIGNMENT:
        return "*";
      case DIVIDE:
      case DIVIDE_ASSIGNMENT:
        return "/";
      case REMAINDER:
      case REMAINDER_ASSIGNMENT:
        return "%";
      default:
        return "?";
    }
  }

  private void printBaseElementType(Tree tree) {
    Tree t = tree;
    while (true) {
      if (t instanceof ArrayTypeTree) {
        t = ((ArrayTypeTree) t).getType();
      } else if (t instanceof AnnotatedTypeTree
          && ((AnnotatedTypeTree) t).getUnderlyingType() instanceof ArrayTypeTree) {
        t = ((AnnotatedTypeTree) t).getUnderlyingType();
      } else {
        break;
      }
    }
    printExpr(t);
  }

  private void printBrackets(Tree tree) {
    Tree elem = tree;
    while (true) {
      if (elem instanceof AnnotatedTypeTree) {
        AnnotatedTypeTree at = (AnnotatedTypeTree) elem;
        elem = at.getUnderlyingType();
        if (elem instanceof ArrayTypeTree) {
          print(' ');
          printTypeAnnotations(at.getAnnotations());
        }
      }
      if (elem instanceof ArrayTypeTree) {
        print("[]");
        elem = ((ArrayTypeTree) elem).getType();
      } else {
        break;
      }
    }
  }

  public Void visitCompilationUnit(CompilationUnitTree tree, Void p) {
    PackageTree pk = tree.getPackage();
    if (pk != null) {
      printStat(pk);
    }
    boolean firstImport = true;
    for (ImportTree im : tree.getImports()) {
      if (firstImport) {
        firstImport = false;
        println();
      }
      printStat(im);
    }
    for (Tree t : tree.getTypeDecls()) {
      printStat(t);
    }
    if (tree.getModule() != null) {
      printStat(tree.getModule());
    }
    return null;
  }

  public Void visitPackage(PackageTree tree, Void p) {
    printAnnotations(tree.getAnnotations());
    if (tree.getPackageName() != null) {
      print("package ");
      printExpr(tree.getPackageName());
      print(";");
      println();
    }
    return null;
  }

  public Void visitImport(ImportTree tree, Void p) {
    print("import ");
    if (tree.isStatic()) {
      print("static ");
    }
    if (tree.isModule()) {
      print("module ");
    }
    printExpr(tree.getQualifiedIdentifier());
    print(";");
    println();
    return null;
  }

  public Void visitModule(ModuleTree tree, Void p) {
    printAnnotations(tree.getAnnotations());
    if (tree.getModuleType() == ModuleTree.ModuleKind.OPEN) {
      print("open ");
    }
    print("module ");
    printExpr(tree.getName());
    if (tree.getDirectives().isEmpty()) {
      print(" {");
      println();
      print("}");
      println();
    } else {
      print(" {");
      println();
      indent();
      printStats(tree.getDirectives());
      undent();
      print("}");
      println();
    }
    return null;
  }

  public Void visitExports(ExportsTree tree, Void p) {
    print("exports ");
    printExpr(tree.getPackageName());
    if (tree.getModuleNames() != null) {
      print(" to ");
      printExprs(tree.getModuleNames());
    }
    print(";");
    return null;
  }

  public Void visitOpens(OpensTree tree, Void p) {
    print("opens ");
    printExpr(tree.getPackageName());
    if (tree.getModuleNames() != null) {
      print(" to ");
      printExprs(tree.getModuleNames());
    }
    print(";");
    return null;
  }

  public Void visitProvides(ProvidesTree tree, Void p) {
    print("provides ");
    printExpr(tree.getServiceName());
    print(" with ");
    printExprs(tree.getImplementationNames());
    print(";");
    return null;
  }

  public Void visitRequires(RequiresTree tree, Void p) {
    print("requires ");
    if (tree.isStatic()) {
      print("static ");
    }
    if (tree.isTransitive()) {
      print("transitive ");
    }
    printExpr(tree.getModuleName());
    print(";");
    return null;
  }

  public Void visitUses(UsesTree tree, Void p) {
    print("uses ");
    printExpr(tree.getServiceName());
    print(";");
    return null;
  }

  public Void visitClass(ClassTree tree, Void p) {
    println();
    align();
    ModifiersTree mods = tree.getModifiers();
    printAnnotations(mods.getAnnotations());
    Tree.Kind kind = tree.getKind();
    printFlags(mods.getFlags(), kind == Tree.Kind.ANNOTATION_TYPE);
    String prev = enclClassName;
    enclClassName = tree.getSimpleName().toString();
    if (kind == Tree.Kind.INTERFACE || kind == Tree.Kind.ANNOTATION_TYPE) {
      print("interface " + tree.getSimpleName());
      printTypeParameters(tree.getTypeParameters());
      if (!tree.getImplementsClause().isEmpty()) {
        print(" extends ");
        printExprs(tree.getImplementsClause());
      }
      if (!tree.getPermitsClause().isEmpty()) {
        print(" permits ");
        printExprs(tree.getPermitsClause());
      }
    } else {
      if (kind == Tree.Kind.ENUM) {
        print("enum " + tree.getSimpleName());
      } else {
        print("class " + tree.getSimpleName());
      }
      printTypeParameters(tree.getTypeParameters());
      if (tree.getExtendsClause() != null) {
        print(" extends ");
        printExpr(tree.getExtendsClause());
      }
      if (!tree.getImplementsClause().isEmpty()) {
        print(" implements ");
        printExprs(tree.getImplementsClause());
      }
      if (!tree.getPermitsClause().isEmpty()) {
        print(" permits ");
        printExprs(tree.getPermitsClause());
      }
    }
    print(" ");
    if (kind == Tree.Kind.ENUM) {
      printEnumBody(tree.getMembers());
    } else {
      printBlock(tree.getMembers());
    }
    enclClassName = prev;
    return null;
  }

  public Void visitMethod(MethodTree tree, Void p) {
    println();
    align();
    printExpr(tree.getModifiers());
    printTypeParameters(tree.getTypeParameters());
    if (tree.getName().toString().equals("<init>")) {
      print(enclClassName != null ? enclClassName : tree.getName());
    } else {
      printExpr(tree.getReturnType());
      print(" " + tree.getName());
    }
    print("(");
    if (tree.getReceiverParameter() != null) {
      printExpr(tree.getReceiverParameter());
      if (!tree.getParameters().isEmpty()) {
        print(", ");
      }
    }
    printExprs(tree.getParameters());
    print(")");
    if (!tree.getThrows().isEmpty()) {
      print(" throws ");
      printExprs(tree.getThrows());
    }
    if (tree.getDefaultValue() != null) {
      print(" default ");
      printExpr(tree.getDefaultValue());
    }
    if (tree.getBody() != null) {
      print(" ");
      printStat(tree.getBody());
    } else {
      print(";");
    }
    return null;
  }

  public Void visitVariable(VariableTree tree, Void p) {
    boolean enumConst = tree instanceof N.Variable && ((N.Variable) tree).enumConstant;
    if (enumConst) {
      print("/*public static final*/ ");
      print(tree.getName());
      if (tree.getInitializer() != null) {
        if (tree.getInitializer() instanceof NewClassTree) {
          NewClassTree init = (NewClassTree) tree.getInitializer();
          if (init.getArguments() != null && !init.getArguments().isEmpty()) {
            print("(");
            printExprs(init.getArguments());
            print(")");
          }
          if (init.getClassBody() != null && init.getClassBody().getMembers() != null) {
            print(" ");
            printBlock(init.getClassBody().getMembers());
          }
          return null;
        }
        print(" /* = ");
        printExpr(tree.getInitializer());
        print(" */");
      }
      return null;
    }
    printExpr(tree.getModifiers());
    boolean varargs = tree instanceof N.Variable && ((N.Variable) tree).varargs;
    if (varargs) {
      Tree vt = tree.getType();
      List<? extends AnnotationTree> tas = null;
      if (vt instanceof AnnotatedTypeTree) {
        tas = ((AnnotatedTypeTree) vt).getAnnotations();
        vt = ((AnnotatedTypeTree) vt).getUnderlyingType();
      }
      printExpr(((ArrayTypeTree) vt).getType());
      if (tas != null) {
        print(' ');
        printTypeAnnotations(tas);
      }
      print("... " + tree.getName());
    } else {
      printExpr(tree.getType());
      print(" " + tree.getName());
    }
    if (tree.getInitializer() != null) {
      print(" = ");
      printExpr(tree.getInitializer());
    }
    if (prec == kNotExpression) {
      print(";");
    }
    return null;
  }

  public Void visitEmptyStatement(EmptyStatementTree tree, Void p) {
    print(";");
    return null;
  }

  public Void visitBlock(BlockTree tree, Void p) {
    if (tree.isStatic()) {
      print("static ");
    }
    printBlock(tree.getStatements());
    return null;
  }

  public Void visitDoWhileLoop(DoWhileLoopTree tree, Void p) {
    print("do ");
    printStat(tree.getStatement());
    align();
    print(" while ");
    printParenCond(tree.getCondition());
    print(";");
    return null;
  }

  public Void visitWhileLoop(WhileLoopTree tree, Void p) {
    print("while ");
    printParenCond(tree.getCondition());
    print(" ");
    printStat(tree.getStatement());
    return null;
  }

  public Void visitForLoop(ForLoopTree tree, Void p) {
    print("for (");
    List<? extends StatementTree> init = tree.getInitializer();
    if (!init.isEmpty()) {
      if (init.get(0) instanceof VariableTree) {
        printExpr(init.get(0));
        for (int i = 1; i < init.size(); i++) {
          VariableTree v = (VariableTree) init.get(i);
          print(", " + v.getName());
          if (v.getInitializer() != null) {
            print(" = ");
            printExpr(v.getInitializer());
          }
        }
      } else {
        boolean first = true;
        for (StatementTree s : init) {
          if (!first) {
            print(", ");
          }
          printExpr(((ExpressionStatementTree) s).getExpression());
          first = false;
        }
      }
    }
    print("; ");
    if (tree.getCondition() != null) {
      printExpr(tree.getCondition());
    }
    print("; ");
    boolean first = true;
    for (ExpressionStatementTree s : tree.getUpdate()) {
      if (!first) {
        print(", ");
      }
      printExpr(s.getExpression());
      first = false;
    }
    print(") ");
    printStat(tree.getStatement());
    return null;
  }

  public Void visitEnhancedForLoop(EnhancedForLoopTree tree, Void p) {
    print("for (");
    printExpr(tree.getVariable());
    print(" : ");
    printExpr(tree.getExpression());
    print(") ");
    printStat(tree.getStatement());
    return null;
  }

  public Void visitLabeledStatement(LabeledStatementTree tree, Void p) {
    print(tree.getLabel() + ": ");
    printStat(tree.getStatement());
    return null;
  }

  public Void visitSwitch(SwitchTree tree, Void p) {
    print("switch ");
    printParenCond(tree.getExpression());
    print(" {");
    println();
    printStats(tree.getCases());
    align();
    print("}");
    return null;
  }

  public Void visitSwitchExpression(SwitchExpressionTree tree, Void p) {
    print("switch ");
    printParenCond(tree.getExpression());
    print(" {");
    println();
    printStats(tree.getCases());
    align();
    print("}");
    return null;
  }

  public Void visitCase(CaseTree tree, Void p) {
    List<? extends CaseLabelTree> labels = tree.getLabels();
    if (labels.size() == 1 && labels.get(0) instanceof DefaultCaseLabelTree) {
      print("default");
    } else {
      print("case ");
      printExprs(labels);
    }
    if (tree.getGuard() != null) {
      print(" when ");
      print(tree.getGuard());
    }
    if (tree.getCaseKind() == CaseTree.CaseKind.STATEMENT) {
      print(":");
      println();
      indent();
      printStats(tree.getStatements());
      undent();
      align();
    } else {
      print(" -> ");
      Tree body = tree.getBody();
      if (body instanceof ExpressionTree) {
        printExpr(body);
        print(";");
      } else {
        printStat(body);
      }
    }
    return null;
  }

  public Void visitDefaultCaseLabel(DefaultCaseLabelTree tree, Void p) {
    print("default");
    return null;
  }

  public Void visitConstantCaseLabel(ConstantCaseLabelTree tree, Void p) {
    printExpr(tree.getConstantExpression());
    return null;
  }

  public Void visitPatternCaseLabel(PatternCaseLabelTree tree, Void p) {
    printExpr(tree.getPattern());
    return null;
  }

  public Void visitSynchronized(SynchronizedTree tree, Void p) {
    print("synchronized ");
    printParenCond(tree.getExpression());
    print(" ");
    printStat(tree.getBlock());
    return null;
  }

  public Void visitTry(TryTree tree, Void p) {
    if (!tree.getResources().isEmpty()) {
      print("try (");
      boolean first = true;
      for (Tree v : tree.getResources()) {
        if (!first) {
          println();
          indent();
        }
        printStat(v);
        first = false;
      }
      print(") ");
    } else {
      print("try ");
    }
    printStat(tree.getBlock());
    for (CatchTree c : tree.getCatches()) {
      printStat(c);
    }
    if (tree.getFinallyBlock() != null) {
      print(" finally ");
      printStat(tree.getFinallyBlock());
    }
    return null;
  }

  public Void visitCatch(CatchTree tree, Void p) {
    print(" catch (");
    printExpr(tree.getParameter());
    print(") ");
    printStat(tree.getBlock());
    return null;
  }

  public Void visitConditionalExpression(ConditionalExpressionTree tree, Void p) {
    open(prec, kCondPrec);
    printExpr(tree.getCondition(), kCondPrec + 1);
    print(" ? ");
    printExpr(tree.getTrueExpression());
    print(" : ");
    printExpr(tree.getFalseExpression(), kCondPrec);
    close(prec, kCondPrec);
    return null;
  }

  public Void visitIf(IfTree tree, Void p) {
    print("if ");
    printParenCond(tree.getCondition());
    print(" ");
    printStat(tree.getThenStatement());
    if (tree.getElseStatement() != null) {
      print(" else ");
      printStat(tree.getElseStatement());
    }
    return null;
  }

  public Void visitExpressionStatement(ExpressionStatementTree tree, Void p) {
    printExpr(tree.getExpression());
    if (prec == kNotExpression) {
      print(";");
    }
    return null;
  }

  public Void visitBreak(BreakTree tree, Void p) {
    print("break");
    if (tree.getLabel() != null) {
      print(" " + tree.getLabel());
    }
    print(";");
    return null;
  }

  public Void visitYield(YieldTree tree, Void p) {
    print("yield");
    print(" ");
    printExpr(tree.getValue());
    print(";");
    return null;
  }

  public Void visitContinue(ContinueTree tree, Void p) {
    print("continue");
    if (tree.getLabel() != null) {
      print(" " + tree.getLabel());
    }
    print(";");
    return null;
  }

  public Void visitReturn(ReturnTree tree, Void p) {
    print("return");
    if (tree.getExpression() != null) {
      print(" ");
      printExpr(tree.getExpression());
    }
    print(";");
    return null;
  }

  public Void visitThrow(ThrowTree tree, Void p) {
    print("throw ");
    printExpr(tree.getExpression());
    print(";");
    return null;
  }

  public Void visitAssert(AssertTree tree, Void p) {
    print("assert ");
    printExpr(tree.getCondition());
    if (tree.getDetail() != null) {
      print(" : ");
      printExpr(tree.getDetail());
    }
    print(";");
    return null;
  }

  public Void visitMethodInvocation(MethodInvocationTree tree, Void p) {
    if (!tree.getTypeArguments().isEmpty()) {
      if (tree.getMethodSelect() instanceof MemberSelectTree) {
        MemberSelectTree left = (MemberSelectTree) tree.getMethodSelect();
        printExpr(left.getExpression());
        print(".<");
        printExprs(tree.getTypeArguments());
        print(">" + left.getIdentifier());
      } else {
        print("<");
        printExprs(tree.getTypeArguments());
        print(">");
        printExpr(tree.getMethodSelect());
      }
    } else {
      printExpr(tree.getMethodSelect());
    }
    print("(");
    printExprs(tree.getArguments());
    print(")");
    return null;
  }

  public Void visitNewClass(NewClassTree tree, Void p) {
    if (tree.getEnclosingExpression() != null) {
      printExpr(tree.getEnclosingExpression());
      print(".");
    }
    print("new ");
    if (!tree.getTypeArguments().isEmpty()) {
      print("<");
      printExprs(tree.getTypeArguments());
      print(">");
    }
    printExpr(tree.getIdentifier());
    print("(");
    printExprs(tree.getArguments());
    print(")");
    if (tree.getClassBody() != null) {
      String prev = enclClassName;
      enclClassName = null;
      printBlock(tree.getClassBody().getMembers());
      enclClassName = prev;
    }
    return null;
  }

  public Void visitNewArray(NewArrayTree tree, Void p) {
    if (tree.getType() != null) {
      print("new ");
      Tree elem = tree.getType();
      printBaseElementType(elem);
      for (ExpressionTree d : tree.getDimensions()) {
        print("[");
        printExpr(d);
        print("]");
      }
      printBrackets(elem);
    }
    if (tree.getInitializers() != null) {
      if (tree.getType() != null) {
        print("[]");
      }
      print("{");
      printExprs(tree.getInitializers());
      print("}");
    }
    return null;
  }

  public Void visitLambdaExpression(LambdaExpressionTree tree, Void p) {
    print("(");
    boolean explicit = !tree.getParameters().isEmpty() && tree.getParameters().get(0).getType() != null;
    if (explicit) {
      printExprs(tree.getParameters());
    } else {
      String sep = "";
      for (VariableTree v : tree.getParameters()) {
        print(sep);
        print(v.getName());
        sep = ",";
      }
    }
    print(")->");
    printExpr(tree.getBody());
    return null;
  }

  public Void visitParenthesized(ParenthesizedTree tree, Void p) {
    print("(");
    printExpr(tree.getExpression());
    print(")");
    return null;
  }

  public Void visitAssignment(AssignmentTree tree, Void p) {
    open(prec, kAssignPrec);
    printExpr(tree.getVariable(), kAssignPrec + 1);
    print(" = ");
    printExpr(tree.getExpression(), kAssignPrec);
    close(prec, kAssignPrec);
    return null;
  }

  public Void visitCompoundAssignment(CompoundAssignmentTree tree, Void p) {
    open(prec, kAssignopPrec);
    printExpr(tree.getVariable(), kAssignopPrec + 1);
    print(" " + opName(tree.getKind()) + "= ");
    printExpr(tree.getExpression(), kAssignopPrec);
    close(prec, kAssignopPrec);
    return null;
  }

  public Void visitUnary(UnaryTree tree, Void p) {
    int own = opPrec(tree.getKind());
    String name = opName(tree.getKind());
    open(prec, own);
    if (tree.getKind() != Tree.Kind.POSTFIX_INCREMENT && tree.getKind() != Tree.Kind.POSTFIX_DECREMENT) {
      print(name);
      printExpr(tree.getExpression(), own);
    } else {
      printExpr(tree.getExpression(), own);
      print(name);
    }
    close(prec, own);
    return null;
  }

  public Void visitBinary(BinaryTree tree, Void p) {
    int own = opPrec(tree.getKind());
    String name = opName(tree.getKind());
    open(prec, own);
    printExpr(tree.getLeftOperand(), own);
    print(" " + name + " ");
    printExpr(tree.getRightOperand(), own + 1);
    close(prec, own);
    return null;
  }

  public Void visitTypeCast(TypeCastTree tree, Void p) {
    open(prec, kPrefixPrec);
    print("(");
    printExpr(tree.getType());
    print(")");
    printExpr(tree.getExpression(), kPrefixPrec);
    close(prec, kPrefixPrec);
    return null;
  }

  public Void visitInstanceOf(InstanceOfTree tree, Void p) {
    open(prec, kOrdPrec);
    printExpr(tree.getExpression(), kOrdPrec);
    print(" instanceof ");
    if (tree.getPattern() != null) {
      printExpr(tree.getPattern());
    } else {
      printExpr(tree.getType(), kOrdPrec + 1);
    }
    close(prec, kOrdPrec);
    return null;
  }

  public Void visitBindingPattern(BindingPatternTree tree, Void p) {
    printExpr(tree.getVariable());
    return null;
  }

  public Void visitAnyPattern(AnyPatternTree tree, Void p) {
    print("_");
    return null;
  }

  public Void visitDeconstructionPattern(DeconstructionPatternTree tree, Void p) {
    printExpr(tree.getDeconstructor());
    print("(");
    printExprs(tree.getNestedPatterns());
    print(")");
    return null;
  }

  public Void visitArrayAccess(ArrayAccessTree tree, Void p) {
    printExpr(tree.getExpression());
    print("[");
    printExpr(tree.getIndex());
    print("]");
    return null;
  }

  public Void visitMemberSelect(MemberSelectTree tree, Void p) {
    printExpr(tree.getExpression(), kPostfixPrec);
    print("." + tree.getIdentifier());
    return null;
  }

  public Void visitMemberReference(MemberReferenceTree tree, Void p) {
    printExpr(tree.getQualifierExpression());
    print("::");
    if (tree.getTypeArguments() != null) {
      print("<");
      printExprs(tree.getTypeArguments());
      print(">");
    }
    print(tree.getMode() == MemberReferenceTree.ReferenceMode.INVOKE ? tree.getName() : "new");
    return null;
  }

  public Void visitIdentifier(IdentifierTree tree, Void p) {
    print(tree.getName());
    return null;
  }

  public Void visitLiteral(LiteralTree tree, Void p) {
    Object v = tree.getValue();
    switch (tree.getKind()) {
      case INT_LITERAL:
        print(v.toString());
        break;
      case LONG_LITERAL:
        print(v + "L");
        break;
      case FLOAT_LITERAL:
        print(v + "F");
        break;
      case DOUBLE_LITERAL:
        print(v.toString());
        break;
      case CHAR_LITERAL:
        print("'" + quote(String.valueOf(v), true) + "'");
        break;
      case BOOLEAN_LITERAL:
        print(((Boolean) v) ? "true" : "false");
        break;
      case NULL_LITERAL:
        print("null");
        break;
      default:
        print("\"" + quote(v.toString(), false) + "\"");
        break;
    }
    return null;
  }

  public Void visitPrimitiveType(PrimitiveTypeTree tree, Void p) {
    print(tree.getPrimitiveTypeKind().toString().toLowerCase());
    return null;
  }

  public Void visitArrayType(ArrayTypeTree tree, Void p) {
    printBaseElementType(tree);
    printBrackets(tree);
    return null;
  }

  public Void visitParameterizedType(ParameterizedTypeTree tree, Void p) {
    printExpr(tree.getType());
    print("<");
    printExprs(tree.getTypeArguments());
    print(">");
    return null;
  }

  public Void visitUnionType(UnionTypeTree tree, Void p) {
    printExprs(tree.getTypeAlternatives(), " | ");
    return null;
  }

  public Void visitIntersectionType(IntersectionTypeTree tree, Void p) {
    printExprs(tree.getBounds(), " & ");
    return null;
  }

  public Void visitTypeParameter(TypeParameterTree tree, Void p) {
    if (!tree.getAnnotations().isEmpty()) {
      printTypeAnnotations(tree.getAnnotations());
    }
    print(tree.getName());
    if (!tree.getBounds().isEmpty()) {
      print(" extends ");
      printExprs(tree.getBounds(), " & ");
    }
    return null;
  }

  public Void visitWildcard(WildcardTree tree, Void p) {
    switch (tree.getKind()) {
      case UNBOUNDED_WILDCARD:
        print("?");
        break;
      case EXTENDS_WILDCARD:
        print("? extends ");
        break;
      default:
        print("? super ");
        break;
    }
    if (tree.getKind() != Tree.Kind.UNBOUNDED_WILDCARD) {
      printExpr(tree.getBound());
    }
    return null;
  }

  public Void visitModifiers(ModifiersTree tree, Void p) {
    printAnnotations(tree.getAnnotations());
    printFlags(tree.getFlags(), false);
    return null;
  }

  public Void visitAnnotation(AnnotationTree tree, Void p) {
    print("@");
    printExpr(tree.getAnnotationType());
    if (!tree.getArguments().isEmpty()) {
      print("(");
      printExprs(tree.getArguments());
      print(")");
    }
    return null;
  }

  public Void visitAnnotatedType(AnnotatedTypeTree tree, Void p) {
    Tree under = tree.getUnderlyingType();
    if (under instanceof MemberSelectTree) {
      MemberSelectTree access = (MemberSelectTree) under;
      printExpr(access.getExpression(), kPostfixPrec);
      print(".");
      printTypeAnnotations(tree.getAnnotations());
      print(access.getIdentifier());
    } else if (under instanceof ArrayTypeTree) {
      printBaseElementType(tree);
      printBrackets(tree);
    } else {
      printTypeAnnotations(tree.getAnnotations());
      printExpr(under);
    }
    return null;
  }

  public Void visitErroneous(ErroneousTree tree, Void p) {
    print("(ERROR)");
    return null;
  }

  public Void visitOther(Tree tree, Void p) {
    print("(UNKNOWN: " + tree.getKind() + ")");
    return null;
  }
}
