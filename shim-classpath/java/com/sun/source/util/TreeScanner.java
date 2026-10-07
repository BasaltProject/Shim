/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.util;

public class TreeScanner<R, P> implements com.sun.source.tree.TreeVisitor<R, P> {
  public TreeScanner() {
  }

  public R reduce(R r, R r2) {
    return r;
  }
  public R scan(com.sun.source.tree.Tree tree, P p) {
    return tree == null ? null : tree.accept(this, p);
  }
  public R scan(java.lang.Iterable<? extends com.sun.source.tree.Tree> iterable, P p) {
    R r = null;
    if (iterable != null) {
      boolean first = true;
      for (com.sun.source.tree.Tree node : iterable) {
        r = first ? scan(node, p) : scanAndReduce(node, p, r);
        first = false;
      }
    }
    return r;
  }
  public R visitAnnotatedType(com.sun.source.tree.AnnotatedTypeTree annotatedTypeTree, P p) {
    R r = scan(annotatedTypeTree.getAnnotations(), p);
    r = scanAndReduce(annotatedTypeTree.getUnderlyingType(), p, r);
    return r;
  }
  public R visitAnnotation(com.sun.source.tree.AnnotationTree annotationTree, P p) {
    R r = scan(annotationTree.getAnnotationType(), p);
    r = scanAndReduce(annotationTree.getArguments(), p, r);
    return r;
  }
  public R visitAnyPattern(com.sun.source.tree.AnyPatternTree anyPatternTree, P p) {
    return null;
  }
  public R visitArrayAccess(com.sun.source.tree.ArrayAccessTree arrayAccessTree, P p) {
    R r = scan(arrayAccessTree.getExpression(), p);
    r = scanAndReduce(arrayAccessTree.getIndex(), p, r);
    return r;
  }
  public R visitArrayType(com.sun.source.tree.ArrayTypeTree arrayTypeTree, P p) {
    return scan(arrayTypeTree.getType(), p);
  }
  public R visitAssert(com.sun.source.tree.AssertTree assertTree, P p) {
    R r = scan(assertTree.getCondition(), p);
    r = scanAndReduce(assertTree.getDetail(), p, r);
    return r;
  }
  public R visitAssignment(com.sun.source.tree.AssignmentTree assignmentTree, P p) {
    R r = scan(assignmentTree.getVariable(), p);
    r = scanAndReduce(assignmentTree.getExpression(), p, r);
    return r;
  }
  public R visitBinary(com.sun.source.tree.BinaryTree binaryTree, P p) {
    R r = scan(binaryTree.getLeftOperand(), p);
    r = scanAndReduce(binaryTree.getRightOperand(), p, r);
    return r;
  }
  public R visitBindingPattern(com.sun.source.tree.BindingPatternTree bindingPatternTree, P p) {
    return scan(bindingPatternTree.getVariable(), p);
  }
  public R visitBlock(com.sun.source.tree.BlockTree blockTree, P p) {
    return scan(blockTree.getStatements(), p);
  }
  public R visitBreak(com.sun.source.tree.BreakTree breakTree, P p) {
    return null;
  }
  public R visitCase(com.sun.source.tree.CaseTree caseTree, P p) {
    R r = scan(caseTree.getLabels(), p);
    r = scanAndReduce(caseTree.getGuard(), p, r);
    if (caseTree.getCaseKind() == com.sun.source.tree.CaseTree.CaseKind.RULE) {
      r = scanAndReduce(caseTree.getBody(), p, r);
    } else {
      r = scanAndReduce(caseTree.getStatements(), p, r);
    }
    return r;
  }
  public R visitCatch(com.sun.source.tree.CatchTree catchTree, P p) {
    R r = scan(catchTree.getParameter(), p);
    r = scanAndReduce(catchTree.getBlock(), p, r);
    return r;
  }
  public R visitClass(com.sun.source.tree.ClassTree classTree, P p) {
    R r = scan(classTree.getModifiers(), p);
    r = scanAndReduce(classTree.getTypeParameters(), p, r);
    r = scanAndReduce(classTree.getExtendsClause(), p, r);
    r = scanAndReduce(classTree.getImplementsClause(), p, r);
    r = scanAndReduce(classTree.getPermitsClause(), p, r);
    r = scanAndReduce(classTree.getMembers(), p, r);
    return r;
  }
  public R visitCompilationUnit(com.sun.source.tree.CompilationUnitTree compilationUnitTree, P p) {
    R r = scan(compilationUnitTree.getPackage(), p);
    r = scanAndReduce(compilationUnitTree.getImports(), p, r);
    r = scanAndReduce(compilationUnitTree.getTypeDecls(), p, r);
    r = scanAndReduce(compilationUnitTree.getModule(), p, r);
    return r;
  }
  public R visitCompoundAssignment(com.sun.source.tree.CompoundAssignmentTree compoundAssignmentTree, P p) {
    R r = scan(compoundAssignmentTree.getVariable(), p);
    r = scanAndReduce(compoundAssignmentTree.getExpression(), p, r);
    return r;
  }
  public R visitConditionalExpression(com.sun.source.tree.ConditionalExpressionTree conditionalExpressionTree, P p) {
    R r = scan(conditionalExpressionTree.getCondition(), p);
    r = scanAndReduce(conditionalExpressionTree.getTrueExpression(), p, r);
    r = scanAndReduce(conditionalExpressionTree.getFalseExpression(), p, r);
    return r;
  }
  public R visitConstantCaseLabel(com.sun.source.tree.ConstantCaseLabelTree constantCaseLabelTree, P p) {
    return scan(constantCaseLabelTree.getConstantExpression(), p);
  }
  public R visitContinue(com.sun.source.tree.ContinueTree continueTree, P p) {
    return null;
  }
  public R visitDeconstructionPattern(com.sun.source.tree.DeconstructionPatternTree deconstructionPatternTree, P p) {
    R r = scan(deconstructionPatternTree.getDeconstructor(), p);
    r = scanAndReduce(deconstructionPatternTree.getNestedPatterns(), p, r);
    return r;
  }
  public R visitDefaultCaseLabel(com.sun.source.tree.DefaultCaseLabelTree defaultCaseLabelTree, P p) {
    return null;
  }
  public R visitDoWhileLoop(com.sun.source.tree.DoWhileLoopTree doWhileLoopTree, P p) {
    R r = scan(doWhileLoopTree.getStatement(), p);
    r = scanAndReduce(doWhileLoopTree.getCondition(), p, r);
    return r;
  }
  public R visitEmptyStatement(com.sun.source.tree.EmptyStatementTree emptyStatementTree, P p) {
    return null;
  }
  public R visitEnhancedForLoop(com.sun.source.tree.EnhancedForLoopTree enhancedForLoopTree, P p) {
    R r = scan(enhancedForLoopTree.getVariable(), p);
    r = scanAndReduce(enhancedForLoopTree.getExpression(), p, r);
    r = scanAndReduce(enhancedForLoopTree.getStatement(), p, r);
    return r;
  }
  public R visitErroneous(com.sun.source.tree.ErroneousTree erroneousTree, P p) {
    return null;
  }
  public R visitExports(com.sun.source.tree.ExportsTree exportsTree, P p) {
    R r = scan(exportsTree.getPackageName(), p);
    r = scanAndReduce(exportsTree.getModuleNames(), p, r);
    return r;
  }
  public R visitExpressionStatement(com.sun.source.tree.ExpressionStatementTree expressionStatementTree, P p) {
    return scan(expressionStatementTree.getExpression(), p);
  }
  public R visitForLoop(com.sun.source.tree.ForLoopTree forLoopTree, P p) {
    R r = scan(forLoopTree.getInitializer(), p);
    r = scanAndReduce(forLoopTree.getCondition(), p, r);
    r = scanAndReduce(forLoopTree.getUpdate(), p, r);
    r = scanAndReduce(forLoopTree.getStatement(), p, r);
    return r;
  }
  public R visitIdentifier(com.sun.source.tree.IdentifierTree identifierTree, P p) {
    return null;
  }
  public R visitIf(com.sun.source.tree.IfTree ifTree, P p) {
    R r = scan(ifTree.getCondition(), p);
    r = scanAndReduce(ifTree.getThenStatement(), p, r);
    r = scanAndReduce(ifTree.getElseStatement(), p, r);
    return r;
  }
  public R visitImport(com.sun.source.tree.ImportTree importTree, P p) {
    return scan(importTree.getQualifiedIdentifier(), p);
  }
  public R visitInstanceOf(com.sun.source.tree.InstanceOfTree instanceOfTree, P p) {
    R r = scan(instanceOfTree.getExpression(), p);
    if (instanceOfTree.getPattern() != null) {
      r = scanAndReduce(instanceOfTree.getPattern(), p, r);
    } else {
      r = scanAndReduce(instanceOfTree.getType(), p, r);
    }
    return r;
  }
  public R visitIntersectionType(com.sun.source.tree.IntersectionTypeTree intersectionTypeTree, P p) {
    return scan(intersectionTypeTree.getBounds(), p);
  }
  public R visitLabeledStatement(com.sun.source.tree.LabeledStatementTree labeledStatementTree, P p) {
    return scan(labeledStatementTree.getStatement(), p);
  }
  public R visitLambdaExpression(com.sun.source.tree.LambdaExpressionTree lambdaExpressionTree, P p) {
    R r = scan(lambdaExpressionTree.getParameters(), p);
    r = scanAndReduce(lambdaExpressionTree.getBody(), p, r);
    return r;
  }
  public R visitLiteral(com.sun.source.tree.LiteralTree literalTree, P p) {
    return null;
  }
  public R visitMemberReference(com.sun.source.tree.MemberReferenceTree memberReferenceTree, P p) {
    R r = scan(memberReferenceTree.getQualifierExpression(), p);
    r = scanAndReduce(memberReferenceTree.getTypeArguments(), p, r);
    return r;
  }
  public R visitMemberSelect(com.sun.source.tree.MemberSelectTree memberSelectTree, P p) {
    return scan(memberSelectTree.getExpression(), p);
  }
  public R visitMethod(com.sun.source.tree.MethodTree methodTree, P p) {
    R r = scan(methodTree.getModifiers(), p);
    r = scanAndReduce(methodTree.getReturnType(), p, r);
    r = scanAndReduce(methodTree.getTypeParameters(), p, r);
    r = scanAndReduce(methodTree.getParameters(), p, r);
    r = scanAndReduce(methodTree.getReceiverParameter(), p, r);
    r = scanAndReduce(methodTree.getThrows(), p, r);
    r = scanAndReduce(methodTree.getBody(), p, r);
    r = scanAndReduce(methodTree.getDefaultValue(), p, r);
    return r;
  }
  public R visitMethodInvocation(com.sun.source.tree.MethodInvocationTree methodInvocationTree, P p) {
    R r = scan(methodInvocationTree.getTypeArguments(), p);
    r = scanAndReduce(methodInvocationTree.getMethodSelect(), p, r);
    r = scanAndReduce(methodInvocationTree.getArguments(), p, r);
    return r;
  }
  public R visitModifiers(com.sun.source.tree.ModifiersTree modifiersTree, P p) {
    return scan(modifiersTree.getAnnotations(), p);
  }
  public R visitModule(com.sun.source.tree.ModuleTree moduleTree, P p) {
    R r = scan(moduleTree.getAnnotations(), p);
    r = scanAndReduce(moduleTree.getName(), p, r);
    r = scanAndReduce(moduleTree.getDirectives(), p, r);
    return r;
  }
  public R visitNewArray(com.sun.source.tree.NewArrayTree newArrayTree, P p) {
    R r = scan(newArrayTree.getType(), p);
    r = scanAndReduce(newArrayTree.getDimensions(), p, r);
    r = scanAndReduce(newArrayTree.getInitializers(), p, r);
    r = scanAndReduce(newArrayTree.getAnnotations(), p, r);
    for (java.lang.Iterable<? extends com.sun.source.tree.Tree> dimAnno : newArrayTree.getDimAnnotations()) {
      r = scanAndReduce(dimAnno, p, r);
    }
    return r;
  }
  public R visitNewClass(com.sun.source.tree.NewClassTree newClassTree, P p) {
    R r = scan(newClassTree.getEnclosingExpression(), p);
    r = scanAndReduce(newClassTree.getIdentifier(), p, r);
    r = scanAndReduce(newClassTree.getTypeArguments(), p, r);
    r = scanAndReduce(newClassTree.getArguments(), p, r);
    r = scanAndReduce(newClassTree.getClassBody(), p, r);
    return r;
  }
  public R visitOpens(com.sun.source.tree.OpensTree opensTree, P p) {
    R r = scan(opensTree.getPackageName(), p);
    r = scanAndReduce(opensTree.getModuleNames(), p, r);
    return r;
  }
  public R visitOther(com.sun.source.tree.Tree tree, P p) {
    return null;
  }
  public R visitPackage(com.sun.source.tree.PackageTree packageTree, P p) {
    R r = scan(packageTree.getAnnotations(), p);
    r = scanAndReduce(packageTree.getPackageName(), p, r);
    return r;
  }
  public R visitParameterizedType(com.sun.source.tree.ParameterizedTypeTree parameterizedTypeTree, P p) {
    R r = scan(parameterizedTypeTree.getType(), p);
    r = scanAndReduce(parameterizedTypeTree.getTypeArguments(), p, r);
    return r;
  }
  public R visitParenthesized(com.sun.source.tree.ParenthesizedTree parenthesizedTree, P p) {
    return scan(parenthesizedTree.getExpression(), p);
  }
  public R visitPatternCaseLabel(com.sun.source.tree.PatternCaseLabelTree patternCaseLabelTree, P p) {
    return scan(patternCaseLabelTree.getPattern(), p);
  }
  public R visitPrimitiveType(com.sun.source.tree.PrimitiveTypeTree primitiveTypeTree, P p) {
    return null;
  }
  public R visitProvides(com.sun.source.tree.ProvidesTree providesTree, P p) {
    R r = scan(providesTree.getServiceName(), p);
    r = scanAndReduce(providesTree.getImplementationNames(), p, r);
    return r;
  }
  public R visitRequires(com.sun.source.tree.RequiresTree requiresTree, P p) {
    return scan(requiresTree.getModuleName(), p);
  }
  public R visitReturn(com.sun.source.tree.ReturnTree returnTree, P p) {
    return scan(returnTree.getExpression(), p);
  }
  public R visitSwitch(com.sun.source.tree.SwitchTree switchTree, P p) {
    R r = scan(switchTree.getExpression(), p);
    r = scanAndReduce(switchTree.getCases(), p, r);
    return r;
  }
  public R visitSwitchExpression(com.sun.source.tree.SwitchExpressionTree switchExpressionTree, P p) {
    R r = scan(switchExpressionTree.getExpression(), p);
    r = scanAndReduce(switchExpressionTree.getCases(), p, r);
    return r;
  }
  public R visitSynchronized(com.sun.source.tree.SynchronizedTree synchronizedTree, P p) {
    R r = scan(synchronizedTree.getExpression(), p);
    r = scanAndReduce(synchronizedTree.getBlock(), p, r);
    return r;
  }
  public R visitThrow(com.sun.source.tree.ThrowTree throwTree, P p) {
    return scan(throwTree.getExpression(), p);
  }
  public R visitTry(com.sun.source.tree.TryTree tryTree, P p) {
    R r = scan(tryTree.getResources(), p);
    r = scanAndReduce(tryTree.getBlock(), p, r);
    r = scanAndReduce(tryTree.getCatches(), p, r);
    r = scanAndReduce(tryTree.getFinallyBlock(), p, r);
    return r;
  }
  public R visitTypeCast(com.sun.source.tree.TypeCastTree typeCastTree, P p) {
    R r = scan(typeCastTree.getType(), p);
    r = scanAndReduce(typeCastTree.getExpression(), p, r);
    return r;
  }
  public R visitTypeParameter(com.sun.source.tree.TypeParameterTree typeParameterTree, P p) {
    R r = scan(typeParameterTree.getAnnotations(), p);
    r = scanAndReduce(typeParameterTree.getBounds(), p, r);
    return r;
  }
  public R visitUnary(com.sun.source.tree.UnaryTree unaryTree, P p) {
    return scan(unaryTree.getExpression(), p);
  }
  public R visitUnionType(com.sun.source.tree.UnionTypeTree unionTypeTree, P p) {
    return scan(unionTypeTree.getTypeAlternatives(), p);
  }
  public R visitUses(com.sun.source.tree.UsesTree usesTree, P p) {
    return scan(usesTree.getServiceName(), p);
  }
  public R visitVariable(com.sun.source.tree.VariableTree variableTree, P p) {
    R r = scan(variableTree.getModifiers(), p);
    r = scanAndReduce(variableTree.getType(), p, r);
    r = scanAndReduce(variableTree.getNameExpression(), p, r);
    r = scanAndReduce(variableTree.getInitializer(), p, r);
    return r;
  }
  public R visitWhileLoop(com.sun.source.tree.WhileLoopTree whileLoopTree, P p) {
    R r = scan(whileLoopTree.getCondition(), p);
    r = scanAndReduce(whileLoopTree.getStatement(), p, r);
    return r;
  }
  public R visitWildcard(com.sun.source.tree.WildcardTree wildcardTree, P p) {
    return scan(wildcardTree.getBound(), p);
  }
  public R visitYield(com.sun.source.tree.YieldTree yieldTree, P p) {
    return scan(yieldTree.getValue(), p);
  }

  private R scanAndReduce(com.sun.source.tree.Tree node, P p, R r) {
    return reduce(scan(node, p), r);
  }

  private R scanAndReduce(java.lang.Iterable<? extends com.sun.source.tree.Tree> nodes, P p, R r) {
    return reduce(scan(nodes, p), r);
  }
}
