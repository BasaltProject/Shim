/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface TreeVisitor<R, P> {
  R visitAnnotatedType(com.sun.source.tree.AnnotatedTypeTree annotatedTypeTree, P p);
  R visitAnnotation(com.sun.source.tree.AnnotationTree annotationTree, P p);
  R visitAnyPattern(com.sun.source.tree.AnyPatternTree anyPatternTree, P p);
  R visitArrayAccess(com.sun.source.tree.ArrayAccessTree arrayAccessTree, P p);
  R visitArrayType(com.sun.source.tree.ArrayTypeTree arrayTypeTree, P p);
  R visitAssert(com.sun.source.tree.AssertTree assertTree, P p);
  R visitAssignment(com.sun.source.tree.AssignmentTree assignmentTree, P p);
  R visitBinary(com.sun.source.tree.BinaryTree binaryTree, P p);
  R visitBindingPattern(com.sun.source.tree.BindingPatternTree bindingPatternTree, P p);
  R visitBlock(com.sun.source.tree.BlockTree blockTree, P p);
  R visitBreak(com.sun.source.tree.BreakTree breakTree, P p);
  R visitCase(com.sun.source.tree.CaseTree caseTree, P p);
  R visitCatch(com.sun.source.tree.CatchTree catchTree, P p);
  R visitClass(com.sun.source.tree.ClassTree classTree, P p);
  R visitCompilationUnit(com.sun.source.tree.CompilationUnitTree compilationUnitTree, P p);
  R visitCompoundAssignment(com.sun.source.tree.CompoundAssignmentTree compoundAssignmentTree, P p);
  R visitConditionalExpression(com.sun.source.tree.ConditionalExpressionTree conditionalExpressionTree, P p);
  R visitConstantCaseLabel(com.sun.source.tree.ConstantCaseLabelTree constantCaseLabelTree, P p);
  R visitContinue(com.sun.source.tree.ContinueTree continueTree, P p);
  R visitDeconstructionPattern(com.sun.source.tree.DeconstructionPatternTree deconstructionPatternTree, P p);
  R visitDefaultCaseLabel(com.sun.source.tree.DefaultCaseLabelTree defaultCaseLabelTree, P p);
  R visitDoWhileLoop(com.sun.source.tree.DoWhileLoopTree doWhileLoopTree, P p);
  R visitEmptyStatement(com.sun.source.tree.EmptyStatementTree emptyStatementTree, P p);
  R visitEnhancedForLoop(com.sun.source.tree.EnhancedForLoopTree enhancedForLoopTree, P p);
  R visitErroneous(com.sun.source.tree.ErroneousTree erroneousTree, P p);
  R visitExports(com.sun.source.tree.ExportsTree exportsTree, P p);
  R visitExpressionStatement(com.sun.source.tree.ExpressionStatementTree expressionStatementTree, P p);
  R visitForLoop(com.sun.source.tree.ForLoopTree forLoopTree, P p);
  R visitIdentifier(com.sun.source.tree.IdentifierTree identifierTree, P p);
  R visitIf(com.sun.source.tree.IfTree ifTree, P p);
  R visitImport(com.sun.source.tree.ImportTree importTree, P p);
  R visitInstanceOf(com.sun.source.tree.InstanceOfTree instanceOfTree, P p);
  R visitIntersectionType(com.sun.source.tree.IntersectionTypeTree intersectionTypeTree, P p);
  R visitLabeledStatement(com.sun.source.tree.LabeledStatementTree labeledStatementTree, P p);
  R visitLambdaExpression(com.sun.source.tree.LambdaExpressionTree lambdaExpressionTree, P p);
  R visitLiteral(com.sun.source.tree.LiteralTree literalTree, P p);
  R visitMemberReference(com.sun.source.tree.MemberReferenceTree memberReferenceTree, P p);
  R visitMemberSelect(com.sun.source.tree.MemberSelectTree memberSelectTree, P p);
  R visitMethod(com.sun.source.tree.MethodTree methodTree, P p);
  R visitMethodInvocation(com.sun.source.tree.MethodInvocationTree methodInvocationTree, P p);
  R visitModifiers(com.sun.source.tree.ModifiersTree modifiersTree, P p);
  R visitModule(com.sun.source.tree.ModuleTree moduleTree, P p);
  R visitNewArray(com.sun.source.tree.NewArrayTree newArrayTree, P p);
  R visitNewClass(com.sun.source.tree.NewClassTree newClassTree, P p);
  R visitOpens(com.sun.source.tree.OpensTree opensTree, P p);
  R visitOther(com.sun.source.tree.Tree tree, P p);
  R visitPackage(com.sun.source.tree.PackageTree packageTree, P p);
  R visitParameterizedType(com.sun.source.tree.ParameterizedTypeTree parameterizedTypeTree, P p);
  R visitParenthesized(com.sun.source.tree.ParenthesizedTree parenthesizedTree, P p);
  R visitPatternCaseLabel(com.sun.source.tree.PatternCaseLabelTree patternCaseLabelTree, P p);
  R visitPrimitiveType(com.sun.source.tree.PrimitiveTypeTree primitiveTypeTree, P p);
  R visitProvides(com.sun.source.tree.ProvidesTree providesTree, P p);
  R visitRequires(com.sun.source.tree.RequiresTree requiresTree, P p);
  R visitReturn(com.sun.source.tree.ReturnTree returnTree, P p);
  R visitSwitch(com.sun.source.tree.SwitchTree switchTree, P p);
  R visitSwitchExpression(com.sun.source.tree.SwitchExpressionTree switchExpressionTree, P p);
  R visitSynchronized(com.sun.source.tree.SynchronizedTree synchronizedTree, P p);
  R visitThrow(com.sun.source.tree.ThrowTree throwTree, P p);
  R visitTry(com.sun.source.tree.TryTree tryTree, P p);
  R visitTypeCast(com.sun.source.tree.TypeCastTree typeCastTree, P p);
  R visitTypeParameter(com.sun.source.tree.TypeParameterTree typeParameterTree, P p);
  R visitUnary(com.sun.source.tree.UnaryTree unaryTree, P p);
  R visitUnionType(com.sun.source.tree.UnionTypeTree unionTypeTree, P p);
  R visitUses(com.sun.source.tree.UsesTree usesTree, P p);
  R visitVariable(com.sun.source.tree.VariableTree variableTree, P p);
  R visitWhileLoop(com.sun.source.tree.WhileLoopTree whileLoopTree, P p);
  R visitWildcard(com.sun.source.tree.WildcardTree wildcardTree, P p);
  R visitYield(com.sun.source.tree.YieldTree yieldTree, P p);
}
