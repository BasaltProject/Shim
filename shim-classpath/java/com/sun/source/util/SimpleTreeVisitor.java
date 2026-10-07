/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.util;

public class SimpleTreeVisitor<R, P> implements com.sun.source.tree.TreeVisitor<R, P> {
  protected final R DEFAULT_VALUE;

  protected SimpleTreeVisitor() {
    DEFAULT_VALUE = null;
  }
  protected SimpleTreeVisitor(R r) {
    DEFAULT_VALUE = r;
  }

  protected R defaultAction(com.sun.source.tree.Tree tree, P p) {
    return DEFAULT_VALUE;
  }
  public final R visit(com.sun.source.tree.Tree tree, P p) {
    return tree == null ? null : tree.accept(this, p);
  }
  public final R visit(java.lang.Iterable<? extends com.sun.source.tree.Tree> iterable, P p) {
    R r = null;
    if (iterable != null) {
      for (com.sun.source.tree.Tree node : iterable) {
        r = visit(node, p);
      }
    }
    return r;
  }
  public R visitAnnotatedType(com.sun.source.tree.AnnotatedTypeTree annotatedTypeTree, P p) {
    return defaultAction(annotatedTypeTree, p);
  }
  public R visitAnnotation(com.sun.source.tree.AnnotationTree annotationTree, P p) {
    return defaultAction(annotationTree, p);
  }
  public R visitAnyPattern(com.sun.source.tree.AnyPatternTree anyPatternTree, P p) {
    return defaultAction(anyPatternTree, p);
  }
  public R visitArrayAccess(com.sun.source.tree.ArrayAccessTree arrayAccessTree, P p) {
    return defaultAction(arrayAccessTree, p);
  }
  public R visitArrayType(com.sun.source.tree.ArrayTypeTree arrayTypeTree, P p) {
    return defaultAction(arrayTypeTree, p);
  }
  public R visitAssert(com.sun.source.tree.AssertTree assertTree, P p) {
    return defaultAction(assertTree, p);
  }
  public R visitAssignment(com.sun.source.tree.AssignmentTree assignmentTree, P p) {
    return defaultAction(assignmentTree, p);
  }
  public R visitBinary(com.sun.source.tree.BinaryTree binaryTree, P p) {
    return defaultAction(binaryTree, p);
  }
  public R visitBindingPattern(com.sun.source.tree.BindingPatternTree bindingPatternTree, P p) {
    return defaultAction(bindingPatternTree, p);
  }
  public R visitBlock(com.sun.source.tree.BlockTree blockTree, P p) {
    return defaultAction(blockTree, p);
  }
  public R visitBreak(com.sun.source.tree.BreakTree breakTree, P p) {
    return defaultAction(breakTree, p);
  }
  public R visitCase(com.sun.source.tree.CaseTree caseTree, P p) {
    return defaultAction(caseTree, p);
  }
  public R visitCatch(com.sun.source.tree.CatchTree catchTree, P p) {
    return defaultAction(catchTree, p);
  }
  public R visitClass(com.sun.source.tree.ClassTree classTree, P p) {
    return defaultAction(classTree, p);
  }
  public R visitCompilationUnit(com.sun.source.tree.CompilationUnitTree compilationUnitTree, P p) {
    return defaultAction(compilationUnitTree, p);
  }
  public R visitCompoundAssignment(com.sun.source.tree.CompoundAssignmentTree compoundAssignmentTree, P p) {
    return defaultAction(compoundAssignmentTree, p);
  }
  public R visitConditionalExpression(com.sun.source.tree.ConditionalExpressionTree conditionalExpressionTree, P p) {
    return defaultAction(conditionalExpressionTree, p);
  }
  public R visitConstantCaseLabel(com.sun.source.tree.ConstantCaseLabelTree constantCaseLabelTree, P p) {
    return defaultAction(constantCaseLabelTree, p);
  }
  public R visitContinue(com.sun.source.tree.ContinueTree continueTree, P p) {
    return defaultAction(continueTree, p);
  }
  public R visitDeconstructionPattern(com.sun.source.tree.DeconstructionPatternTree deconstructionPatternTree, P p) {
    return defaultAction(deconstructionPatternTree, p);
  }
  public R visitDefaultCaseLabel(com.sun.source.tree.DefaultCaseLabelTree defaultCaseLabelTree, P p) {
    return defaultAction(defaultCaseLabelTree, p);
  }
  public R visitDoWhileLoop(com.sun.source.tree.DoWhileLoopTree doWhileLoopTree, P p) {
    return defaultAction(doWhileLoopTree, p);
  }
  public R visitEmptyStatement(com.sun.source.tree.EmptyStatementTree emptyStatementTree, P p) {
    return defaultAction(emptyStatementTree, p);
  }
  public R visitEnhancedForLoop(com.sun.source.tree.EnhancedForLoopTree enhancedForLoopTree, P p) {
    return defaultAction(enhancedForLoopTree, p);
  }
  public R visitErroneous(com.sun.source.tree.ErroneousTree erroneousTree, P p) {
    return defaultAction(erroneousTree, p);
  }
  public R visitExports(com.sun.source.tree.ExportsTree exportsTree, P p) {
    return defaultAction(exportsTree, p);
  }
  public R visitExpressionStatement(com.sun.source.tree.ExpressionStatementTree expressionStatementTree, P p) {
    return defaultAction(expressionStatementTree, p);
  }
  public R visitForLoop(com.sun.source.tree.ForLoopTree forLoopTree, P p) {
    return defaultAction(forLoopTree, p);
  }
  public R visitIdentifier(com.sun.source.tree.IdentifierTree identifierTree, P p) {
    return defaultAction(identifierTree, p);
  }
  public R visitIf(com.sun.source.tree.IfTree ifTree, P p) {
    return defaultAction(ifTree, p);
  }
  public R visitImport(com.sun.source.tree.ImportTree importTree, P p) {
    return defaultAction(importTree, p);
  }
  public R visitInstanceOf(com.sun.source.tree.InstanceOfTree instanceOfTree, P p) {
    return defaultAction(instanceOfTree, p);
  }
  public R visitIntersectionType(com.sun.source.tree.IntersectionTypeTree intersectionTypeTree, P p) {
    return defaultAction(intersectionTypeTree, p);
  }
  public R visitLabeledStatement(com.sun.source.tree.LabeledStatementTree labeledStatementTree, P p) {
    return defaultAction(labeledStatementTree, p);
  }
  public R visitLambdaExpression(com.sun.source.tree.LambdaExpressionTree lambdaExpressionTree, P p) {
    return defaultAction(lambdaExpressionTree, p);
  }
  public R visitLiteral(com.sun.source.tree.LiteralTree literalTree, P p) {
    return defaultAction(literalTree, p);
  }
  public R visitMemberReference(com.sun.source.tree.MemberReferenceTree memberReferenceTree, P p) {
    return defaultAction(memberReferenceTree, p);
  }
  public R visitMemberSelect(com.sun.source.tree.MemberSelectTree memberSelectTree, P p) {
    return defaultAction(memberSelectTree, p);
  }
  public R visitMethod(com.sun.source.tree.MethodTree methodTree, P p) {
    return defaultAction(methodTree, p);
  }
  public R visitMethodInvocation(com.sun.source.tree.MethodInvocationTree methodInvocationTree, P p) {
    return defaultAction(methodInvocationTree, p);
  }
  public R visitModifiers(com.sun.source.tree.ModifiersTree modifiersTree, P p) {
    return defaultAction(modifiersTree, p);
  }
  public R visitModule(com.sun.source.tree.ModuleTree moduleTree, P p) {
    return defaultAction(moduleTree, p);
  }
  public R visitNewArray(com.sun.source.tree.NewArrayTree newArrayTree, P p) {
    return defaultAction(newArrayTree, p);
  }
  public R visitNewClass(com.sun.source.tree.NewClassTree newClassTree, P p) {
    return defaultAction(newClassTree, p);
  }
  public R visitOpens(com.sun.source.tree.OpensTree opensTree, P p) {
    return defaultAction(opensTree, p);
  }
  public R visitOther(com.sun.source.tree.Tree tree, P p) {
    return defaultAction(tree, p);
  }
  public R visitPackage(com.sun.source.tree.PackageTree packageTree, P p) {
    return defaultAction(packageTree, p);
  }
  public R visitParameterizedType(com.sun.source.tree.ParameterizedTypeTree parameterizedTypeTree, P p) {
    return defaultAction(parameterizedTypeTree, p);
  }
  public R visitParenthesized(com.sun.source.tree.ParenthesizedTree parenthesizedTree, P p) {
    return defaultAction(parenthesizedTree, p);
  }
  public R visitPatternCaseLabel(com.sun.source.tree.PatternCaseLabelTree patternCaseLabelTree, P p) {
    return defaultAction(patternCaseLabelTree, p);
  }
  public R visitPrimitiveType(com.sun.source.tree.PrimitiveTypeTree primitiveTypeTree, P p) {
    return defaultAction(primitiveTypeTree, p);
  }
  public R visitProvides(com.sun.source.tree.ProvidesTree providesTree, P p) {
    return defaultAction(providesTree, p);
  }
  public R visitRequires(com.sun.source.tree.RequiresTree requiresTree, P p) {
    return defaultAction(requiresTree, p);
  }
  public R visitReturn(com.sun.source.tree.ReturnTree returnTree, P p) {
    return defaultAction(returnTree, p);
  }
  public R visitSwitch(com.sun.source.tree.SwitchTree switchTree, P p) {
    return defaultAction(switchTree, p);
  }
  public R visitSwitchExpression(com.sun.source.tree.SwitchExpressionTree switchExpressionTree, P p) {
    return defaultAction(switchExpressionTree, p);
  }
  public R visitSynchronized(com.sun.source.tree.SynchronizedTree synchronizedTree, P p) {
    return defaultAction(synchronizedTree, p);
  }
  public R visitThrow(com.sun.source.tree.ThrowTree throwTree, P p) {
    return defaultAction(throwTree, p);
  }
  public R visitTry(com.sun.source.tree.TryTree tryTree, P p) {
    return defaultAction(tryTree, p);
  }
  public R visitTypeCast(com.sun.source.tree.TypeCastTree typeCastTree, P p) {
    return defaultAction(typeCastTree, p);
  }
  public R visitTypeParameter(com.sun.source.tree.TypeParameterTree typeParameterTree, P p) {
    return defaultAction(typeParameterTree, p);
  }
  public R visitUnary(com.sun.source.tree.UnaryTree unaryTree, P p) {
    return defaultAction(unaryTree, p);
  }
  public R visitUnionType(com.sun.source.tree.UnionTypeTree unionTypeTree, P p) {
    return defaultAction(unionTypeTree, p);
  }
  public R visitUses(com.sun.source.tree.UsesTree usesTree, P p) {
    return defaultAction(usesTree, p);
  }
  public R visitVariable(com.sun.source.tree.VariableTree variableTree, P p) {
    return defaultAction(variableTree, p);
  }
  public R visitWhileLoop(com.sun.source.tree.WhileLoopTree whileLoopTree, P p) {
    return defaultAction(whileLoopTree, p);
  }
  public R visitWildcard(com.sun.source.tree.WildcardTree wildcardTree, P p) {
    return defaultAction(wildcardTree, p);
  }
  public R visitYield(com.sun.source.tree.YieldTree yieldTree, P p) {
    return defaultAction(yieldTree, p);
  }
}
