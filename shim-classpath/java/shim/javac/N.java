/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.*;

public final class N {
  private N() {
  }

  public abstract static class Expr extends Base implements ExpressionTree {
  }

  public abstract static class Base implements Tree {
    public Tree.Kind kind;
    public int start = -1;
    public int end = -1;
    public int pos = -1;

    public Tree.Kind getKind() {
      return kind;
    }

    public String toString() {
      return Pretty.print(this);
    }
  }

  public static class AnnotatedType extends Expr implements AnnotatedTypeTree {
    public java.util.List<? extends com.sun.source.tree.AnnotationTree> annotations;
    public com.sun.source.tree.ExpressionTree underlyingType;

    public AnnotatedType() {
      this.kind = Tree.Kind.ANNOTATED_TYPE;
    }

    public java.util.List<? extends com.sun.source.tree.AnnotationTree> getAnnotations() {
      return annotations;
    }

    public com.sun.source.tree.ExpressionTree getUnderlyingType() {
      return underlyingType;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitAnnotatedType(this, d);
    }
  }

  public static class Annotation extends Expr implements AnnotationTree {
    public com.sun.source.tree.Tree annotationType;
    public java.util.List<? extends com.sun.source.tree.ExpressionTree> arguments;

    public Annotation() {
      this.kind = Tree.Kind.ANNOTATION;
    }

    public com.sun.source.tree.Tree getAnnotationType() {
      return annotationType;
    }

    public java.util.List<? extends com.sun.source.tree.ExpressionTree> getArguments() {
      return arguments;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitAnnotation(this, d);
    }
  }

  public static class AnyPattern extends Base implements AnyPatternTree {

    public AnyPattern() {
      this.kind = Tree.Kind.ANY_PATTERN;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitAnyPattern(this, d);
    }
  }

  public static class ArrayAccess extends Expr implements ArrayAccessTree {
    public com.sun.source.tree.ExpressionTree expression;
    public com.sun.source.tree.ExpressionTree index;

    public ArrayAccess() {
      this.kind = Tree.Kind.ARRAY_ACCESS;
    }

    public com.sun.source.tree.ExpressionTree getExpression() {
      return expression;
    }

    public com.sun.source.tree.ExpressionTree getIndex() {
      return index;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitArrayAccess(this, d);
    }
  }

  public static class ArrayType extends Expr implements ArrayTypeTree {
    public com.sun.source.tree.Tree type;

    public ArrayType() {
      this.kind = Tree.Kind.ARRAY_TYPE;
    }

    public com.sun.source.tree.Tree getType() {
      return type;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitArrayType(this, d);
    }
  }

  public static class Assert extends Base implements AssertTree {
    public com.sun.source.tree.ExpressionTree condition;
    public com.sun.source.tree.ExpressionTree detail;

    public Assert() {
      this.kind = Tree.Kind.ASSERT;
    }

    public com.sun.source.tree.ExpressionTree getCondition() {
      return condition;
    }

    public com.sun.source.tree.ExpressionTree getDetail() {
      return detail;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitAssert(this, d);
    }
  }

  public static class Assignment extends Expr implements AssignmentTree {
    public com.sun.source.tree.ExpressionTree expression;
    public com.sun.source.tree.ExpressionTree variable;

    public Assignment() {
      this.kind = Tree.Kind.ASSIGNMENT;
    }

    public com.sun.source.tree.ExpressionTree getExpression() {
      return expression;
    }

    public com.sun.source.tree.ExpressionTree getVariable() {
      return variable;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitAssignment(this, d);
    }
  }

  public static class Binary extends Expr implements BinaryTree {
    public com.sun.source.tree.ExpressionTree leftOperand;
    public com.sun.source.tree.ExpressionTree rightOperand;

    public Binary() {
      this.kind = Tree.Kind.MULTIPLY;
    }

    public com.sun.source.tree.ExpressionTree getLeftOperand() {
      return leftOperand;
    }

    public com.sun.source.tree.ExpressionTree getRightOperand() {
      return rightOperand;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitBinary(this, d);
    }
  }

  public static class BindingPattern extends Base implements BindingPatternTree {
    public com.sun.source.tree.VariableTree variable;

    public BindingPattern() {
      this.kind = Tree.Kind.BINDING_PATTERN;
    }

    public com.sun.source.tree.VariableTree getVariable() {
      return variable;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitBindingPattern(this, d);
    }
  }

  public static class Block extends Base implements BlockTree {
    public java.util.List<? extends com.sun.source.tree.StatementTree> statements;
    public boolean static_;

    public Block() {
      this.kind = Tree.Kind.BLOCK;
    }

    public java.util.List<? extends com.sun.source.tree.StatementTree> getStatements() {
      return statements;
    }

    public boolean isStatic() {
      return static_;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitBlock(this, d);
    }
  }

  public static class Break extends Base implements BreakTree {
    public javax.lang.model.element.Name label;

    public Break() {
      this.kind = Tree.Kind.BREAK;
    }

    public javax.lang.model.element.Name getLabel() {
      return label;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitBreak(this, d);
    }
  }

  public static class Case extends Base implements CaseTree {
    public com.sun.source.tree.Tree body;
    public com.sun.source.tree.CaseTree.CaseKind caseKind;
    public com.sun.source.tree.ExpressionTree expression;
    public java.util.List<? extends com.sun.source.tree.ExpressionTree> expressions;
    public com.sun.source.tree.ExpressionTree guard;
    public java.util.List<? extends com.sun.source.tree.CaseLabelTree> labels;
    public java.util.List<? extends com.sun.source.tree.StatementTree> statements;

    public Case() {
      this.kind = Tree.Kind.CASE;
    }

    public com.sun.source.tree.Tree getBody() {
      return body;
    }

    public com.sun.source.tree.CaseTree.CaseKind getCaseKind() {
      return caseKind;
    }

    public com.sun.source.tree.ExpressionTree getExpression() {
      return expression;
    }

    public java.util.List<? extends com.sun.source.tree.ExpressionTree> getExpressions() {
      return expressions;
    }

    public com.sun.source.tree.ExpressionTree getGuard() {
      return guard;
    }

    public java.util.List<? extends com.sun.source.tree.CaseLabelTree> getLabels() {
      return labels;
    }

    public java.util.List<? extends com.sun.source.tree.StatementTree> getStatements() {
      return statements;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitCase(this, d);
    }
  }

  public static class Catch extends Base implements CatchTree {
    public com.sun.source.tree.BlockTree block;
    public com.sun.source.tree.VariableTree parameter;

    public Catch() {
      this.kind = Tree.Kind.CATCH;
    }

    public com.sun.source.tree.BlockTree getBlock() {
      return block;
    }

    public com.sun.source.tree.VariableTree getParameter() {
      return parameter;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitCatch(this, d);
    }
  }

  public static class Class extends Base implements ClassTree {
    public com.sun.source.tree.Tree extendsClause;
    public java.util.List<? extends com.sun.source.tree.Tree> implementsClause;
    public java.util.List<? extends com.sun.source.tree.Tree> members;
    public com.sun.source.tree.ModifiersTree modifiers;
    public java.util.List<? extends com.sun.source.tree.Tree> permitsClause;
    public javax.lang.model.element.Name simpleName;
    public java.util.List<? extends com.sun.source.tree.TypeParameterTree> typeParameters;

    public Class() {
      this.kind = Tree.Kind.CLASS;
    }

    public com.sun.source.tree.Tree getExtendsClause() {
      return extendsClause;
    }

    public java.util.List<? extends com.sun.source.tree.Tree> getImplementsClause() {
      return implementsClause;
    }

    public java.util.List<? extends com.sun.source.tree.Tree> getMembers() {
      return members;
    }

    public com.sun.source.tree.ModifiersTree getModifiers() {
      return modifiers;
    }

    public java.util.List<? extends com.sun.source.tree.Tree> getPermitsClause() {
      return permitsClause;
    }

    public javax.lang.model.element.Name getSimpleName() {
      return simpleName;
    }

    public java.util.List<? extends com.sun.source.tree.TypeParameterTree> getTypeParameters() {
      return typeParameters;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitClass(this, d);
    }
  }

  public static class CompilationUnit extends Base implements CompilationUnitTree {
    public java.util.List<? extends com.sun.source.tree.ImportTree> imports;
    public com.sun.source.tree.LineMap lineMap;
    public com.sun.source.tree.ModuleTree module;
    public com.sun.source.tree.PackageTree package_;
    public java.util.List<? extends com.sun.source.tree.AnnotationTree> packageAnnotations;
    public com.sun.source.tree.ExpressionTree packageName;
    public javax.tools.JavaFileObject sourceFile;
    public java.util.List<? extends com.sun.source.tree.Tree> typeDecls;
    public final java.util.IdentityHashMap<Tree, String> docComments = new java.util.IdentityHashMap<Tree, String>();
    public String sourceText;

    public CompilationUnit() {
      this.kind = Tree.Kind.COMPILATION_UNIT;
    }

    public java.util.List<? extends com.sun.source.tree.ImportTree> getImports() {
      return imports;
    }

    public com.sun.source.tree.LineMap getLineMap() {
      return lineMap;
    }

    public com.sun.source.tree.ModuleTree getModule() {
      return module;
    }

    public com.sun.source.tree.PackageTree getPackage() {
      return package_;
    }

    public java.util.List<? extends com.sun.source.tree.AnnotationTree> getPackageAnnotations() {
      return packageAnnotations;
    }

    public com.sun.source.tree.ExpressionTree getPackageName() {
      return packageName;
    }

    public javax.tools.JavaFileObject getSourceFile() {
      return sourceFile;
    }

    public java.util.List<? extends com.sun.source.tree.Tree> getTypeDecls() {
      return typeDecls;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitCompilationUnit(this, d);
    }
  }

  public static class CompoundAssignment extends Expr implements CompoundAssignmentTree {
    public com.sun.source.tree.ExpressionTree expression;
    public com.sun.source.tree.ExpressionTree variable;

    public CompoundAssignment() {
      this.kind = Tree.Kind.MULTIPLY_ASSIGNMENT;
    }

    public com.sun.source.tree.ExpressionTree getExpression() {
      return expression;
    }

    public com.sun.source.tree.ExpressionTree getVariable() {
      return variable;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitCompoundAssignment(this, d);
    }
  }

  public static class ConditionalExpression extends Expr implements ConditionalExpressionTree {
    public com.sun.source.tree.ExpressionTree condition;
    public com.sun.source.tree.ExpressionTree falseExpression;
    public com.sun.source.tree.ExpressionTree trueExpression;

    public ConditionalExpression() {
      this.kind = Tree.Kind.CONDITIONAL_EXPRESSION;
    }

    public com.sun.source.tree.ExpressionTree getCondition() {
      return condition;
    }

    public com.sun.source.tree.ExpressionTree getFalseExpression() {
      return falseExpression;
    }

    public com.sun.source.tree.ExpressionTree getTrueExpression() {
      return trueExpression;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitConditionalExpression(this, d);
    }
  }

  public static class ConstantCaseLabel extends Base implements ConstantCaseLabelTree {
    public com.sun.source.tree.ExpressionTree constantExpression;

    public ConstantCaseLabel() {
      this.kind = Tree.Kind.CONSTANT_CASE_LABEL;
    }

    public com.sun.source.tree.ExpressionTree getConstantExpression() {
      return constantExpression;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitConstantCaseLabel(this, d);
    }
  }

  public static class Continue extends Base implements ContinueTree {
    public javax.lang.model.element.Name label;

    public Continue() {
      this.kind = Tree.Kind.CONTINUE;
    }

    public javax.lang.model.element.Name getLabel() {
      return label;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitContinue(this, d);
    }
  }

  public static class DeconstructionPattern extends Base implements DeconstructionPatternTree {
    public com.sun.source.tree.ExpressionTree deconstructor;
    public java.util.List<? extends com.sun.source.tree.PatternTree> nestedPatterns;

    public DeconstructionPattern() {
      this.kind = Tree.Kind.DECONSTRUCTION_PATTERN;
    }

    public com.sun.source.tree.ExpressionTree getDeconstructor() {
      return deconstructor;
    }

    public java.util.List<? extends com.sun.source.tree.PatternTree> getNestedPatterns() {
      return nestedPatterns;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitDeconstructionPattern(this, d);
    }
  }

  public static class DefaultCaseLabel extends Base implements DefaultCaseLabelTree {

    public DefaultCaseLabel() {
      this.kind = Tree.Kind.DEFAULT_CASE_LABEL;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitDefaultCaseLabel(this, d);
    }
  }

  public static class DoWhileLoop extends Base implements DoWhileLoopTree {
    public com.sun.source.tree.ExpressionTree condition;
    public com.sun.source.tree.StatementTree statement;

    public DoWhileLoop() {
      this.kind = Tree.Kind.DO_WHILE_LOOP;
    }

    public com.sun.source.tree.ExpressionTree getCondition() {
      return condition;
    }

    public com.sun.source.tree.StatementTree getStatement() {
      return statement;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitDoWhileLoop(this, d);
    }
  }

  public static class EmptyStatement extends Base implements EmptyStatementTree {

    public EmptyStatement() {
      this.kind = Tree.Kind.EMPTY_STATEMENT;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitEmptyStatement(this, d);
    }
  }

  public static class EnhancedForLoop extends Base implements EnhancedForLoopTree {
    public com.sun.source.tree.ExpressionTree expression;
    public com.sun.source.tree.StatementTree statement;
    public com.sun.source.tree.VariableTree variable;

    public EnhancedForLoop() {
      this.kind = Tree.Kind.ENHANCED_FOR_LOOP;
    }

    public com.sun.source.tree.ExpressionTree getExpression() {
      return expression;
    }

    public com.sun.source.tree.StatementTree getStatement() {
      return statement;
    }

    public com.sun.source.tree.VariableTree getVariable() {
      return variable;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitEnhancedForLoop(this, d);
    }
  }

  public static class Erroneous extends Expr implements ErroneousTree {
    public java.util.List<? extends com.sun.source.tree.Tree> errorTrees;

    public Erroneous() {
      this.kind = Tree.Kind.ERRONEOUS;
    }

    public java.util.List<? extends com.sun.source.tree.Tree> getErrorTrees() {
      return errorTrees;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitErroneous(this, d);
    }
  }

  public static class Exports extends Base implements ExportsTree {
    public java.util.List<? extends com.sun.source.tree.ExpressionTree> moduleNames;
    public com.sun.source.tree.ExpressionTree packageName;

    public Exports() {
      this.kind = Tree.Kind.EXPORTS;
    }

    public java.util.List<? extends com.sun.source.tree.ExpressionTree> getModuleNames() {
      return moduleNames;
    }

    public com.sun.source.tree.ExpressionTree getPackageName() {
      return packageName;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitExports(this, d);
    }
  }

  public static class ExpressionStatement extends Base implements ExpressionStatementTree {
    public com.sun.source.tree.ExpressionTree expression;

    public ExpressionStatement() {
      this.kind = Tree.Kind.EXPRESSION_STATEMENT;
    }

    public com.sun.source.tree.ExpressionTree getExpression() {
      return expression;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitExpressionStatement(this, d);
    }
  }

  public static class ForLoop extends Base implements ForLoopTree {
    public com.sun.source.tree.ExpressionTree condition;
    public java.util.List<? extends com.sun.source.tree.StatementTree> initializer;
    public com.sun.source.tree.StatementTree statement;
    public java.util.List<? extends com.sun.source.tree.ExpressionStatementTree> update;

    public ForLoop() {
      this.kind = Tree.Kind.FOR_LOOP;
    }

    public com.sun.source.tree.ExpressionTree getCondition() {
      return condition;
    }

    public java.util.List<? extends com.sun.source.tree.StatementTree> getInitializer() {
      return initializer;
    }

    public com.sun.source.tree.StatementTree getStatement() {
      return statement;
    }

    public java.util.List<? extends com.sun.source.tree.ExpressionStatementTree> getUpdate() {
      return update;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitForLoop(this, d);
    }
  }

  public static class Identifier extends Expr implements IdentifierTree {
    public javax.lang.model.element.Name name;

    public Identifier() {
      this.kind = Tree.Kind.IDENTIFIER;
    }

    public javax.lang.model.element.Name getName() {
      return name;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitIdentifier(this, d);
    }
  }

  public static class If extends Base implements IfTree {
    public com.sun.source.tree.ExpressionTree condition;
    public com.sun.source.tree.StatementTree elseStatement;
    public com.sun.source.tree.StatementTree thenStatement;

    public If() {
      this.kind = Tree.Kind.IF;
    }

    public com.sun.source.tree.ExpressionTree getCondition() {
      return condition;
    }

    public com.sun.source.tree.StatementTree getElseStatement() {
      return elseStatement;
    }

    public com.sun.source.tree.StatementTree getThenStatement() {
      return thenStatement;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitIf(this, d);
    }
  }

  public static class Import extends Base implements ImportTree {
    public com.sun.source.tree.Tree qualifiedIdentifier;
    public boolean module;
    public boolean static_;

    public Import() {
      this.kind = Tree.Kind.IMPORT;
    }

    public com.sun.source.tree.Tree getQualifiedIdentifier() {
      return qualifiedIdentifier;
    }

    public boolean isModule() {
      return module;
    }

    public boolean isStatic() {
      return static_;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitImport(this, d);
    }
  }

  public static class InstanceOf extends Expr implements InstanceOfTree {
    public com.sun.source.tree.ExpressionTree expression;
    public com.sun.source.tree.PatternTree pattern;
    public com.sun.source.tree.Tree type;

    public InstanceOf() {
      this.kind = Tree.Kind.INSTANCE_OF;
    }

    public com.sun.source.tree.ExpressionTree getExpression() {
      return expression;
    }

    public com.sun.source.tree.PatternTree getPattern() {
      return pattern;
    }

    public com.sun.source.tree.Tree getType() {
      return type;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitInstanceOf(this, d);
    }
  }

  public static class IntersectionType extends Expr implements IntersectionTypeTree {
    public java.util.List<? extends com.sun.source.tree.Tree> bounds;

    public IntersectionType() {
      this.kind = Tree.Kind.INTERSECTION_TYPE;
    }

    public java.util.List<? extends com.sun.source.tree.Tree> getBounds() {
      return bounds;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitIntersectionType(this, d);
    }
  }

  public static class LabeledStatement extends Base implements LabeledStatementTree {
    public javax.lang.model.element.Name label;
    public com.sun.source.tree.StatementTree statement;

    public LabeledStatement() {
      this.kind = Tree.Kind.LABELED_STATEMENT;
    }

    public javax.lang.model.element.Name getLabel() {
      return label;
    }

    public com.sun.source.tree.StatementTree getStatement() {
      return statement;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitLabeledStatement(this, d);
    }
  }

  public static class LambdaExpression extends Expr implements LambdaExpressionTree {
    public com.sun.source.tree.Tree body;
    public com.sun.source.tree.LambdaExpressionTree.BodyKind bodyKind;
    public java.util.List<? extends com.sun.source.tree.VariableTree> parameters;

    public LambdaExpression() {
      this.kind = Tree.Kind.LAMBDA_EXPRESSION;
    }

    public com.sun.source.tree.Tree getBody() {
      return body;
    }

    public com.sun.source.tree.LambdaExpressionTree.BodyKind getBodyKind() {
      return bodyKind;
    }

    public java.util.List<? extends com.sun.source.tree.VariableTree> getParameters() {
      return parameters;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitLambdaExpression(this, d);
    }
  }

  public static class Literal extends Expr implements LiteralTree {
    public java.lang.Object value;

    public Literal() {
      this.kind = Tree.Kind.INT_LITERAL;
    }

    public java.lang.Object getValue() {
      return value;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitLiteral(this, d);
    }
  }

  public static class MemberReference extends Expr implements MemberReferenceTree {
    public com.sun.source.tree.MemberReferenceTree.ReferenceMode mode;
    public javax.lang.model.element.Name name;
    public com.sun.source.tree.ExpressionTree qualifierExpression;
    public java.util.List<? extends com.sun.source.tree.ExpressionTree> typeArguments;

    public MemberReference() {
      this.kind = Tree.Kind.MEMBER_REFERENCE;
    }

    public com.sun.source.tree.MemberReferenceTree.ReferenceMode getMode() {
      return mode;
    }

    public javax.lang.model.element.Name getName() {
      return name;
    }

    public com.sun.source.tree.ExpressionTree getQualifierExpression() {
      return qualifierExpression;
    }

    public java.util.List<? extends com.sun.source.tree.ExpressionTree> getTypeArguments() {
      return typeArguments;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitMemberReference(this, d);
    }
  }

  public static class MemberSelect extends Expr implements MemberSelectTree {
    public com.sun.source.tree.ExpressionTree expression;
    public javax.lang.model.element.Name identifier;

    public MemberSelect() {
      this.kind = Tree.Kind.MEMBER_SELECT;
    }

    public com.sun.source.tree.ExpressionTree getExpression() {
      return expression;
    }

    public javax.lang.model.element.Name getIdentifier() {
      return identifier;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitMemberSelect(this, d);
    }
  }

  public static class MethodInvocation extends Expr implements MethodInvocationTree {
    public java.util.List<? extends com.sun.source.tree.ExpressionTree> arguments;
    public com.sun.source.tree.ExpressionTree methodSelect;
    public java.util.List<? extends com.sun.source.tree.Tree> typeArguments;

    public MethodInvocation() {
      this.kind = Tree.Kind.METHOD_INVOCATION;
    }

    public java.util.List<? extends com.sun.source.tree.ExpressionTree> getArguments() {
      return arguments;
    }

    public com.sun.source.tree.ExpressionTree getMethodSelect() {
      return methodSelect;
    }

    public java.util.List<? extends com.sun.source.tree.Tree> getTypeArguments() {
      return typeArguments;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitMethodInvocation(this, d);
    }
  }

  public static class Method extends Base implements MethodTree {
    public com.sun.source.tree.BlockTree body;
    public com.sun.source.tree.Tree defaultValue;
    public com.sun.source.tree.ModifiersTree modifiers;
    public javax.lang.model.element.Name name;
    public java.util.List<? extends com.sun.source.tree.VariableTree> parameters;
    public com.sun.source.tree.VariableTree receiverParameter;
    public com.sun.source.tree.Tree returnType;
    public java.util.List<? extends com.sun.source.tree.ExpressionTree> throws_;
    public java.util.List<? extends com.sun.source.tree.TypeParameterTree> typeParameters;
    public boolean compactConstructor;

    public Method() {
      this.kind = Tree.Kind.METHOD;
    }

    public com.sun.source.tree.BlockTree getBody() {
      return body;
    }

    public com.sun.source.tree.Tree getDefaultValue() {
      return defaultValue;
    }

    public com.sun.source.tree.ModifiersTree getModifiers() {
      return modifiers;
    }

    public javax.lang.model.element.Name getName() {
      return name;
    }

    public java.util.List<? extends com.sun.source.tree.VariableTree> getParameters() {
      return parameters;
    }

    public com.sun.source.tree.VariableTree getReceiverParameter() {
      return receiverParameter;
    }

    public com.sun.source.tree.Tree getReturnType() {
      return returnType;
    }

    public java.util.List<? extends com.sun.source.tree.ExpressionTree> getThrows() {
      return throws_;
    }

    public java.util.List<? extends com.sun.source.tree.TypeParameterTree> getTypeParameters() {
      return typeParameters;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitMethod(this, d);
    }
  }

  public static class Modifiers extends Base implements ModifiersTree {
    public java.util.List<? extends com.sun.source.tree.AnnotationTree> annotations;
    public java.util.Set<javax.lang.model.element.Modifier> flags;

    public Modifiers() {
      this.kind = Tree.Kind.MODIFIERS;
    }

    public java.util.List<? extends com.sun.source.tree.AnnotationTree> getAnnotations() {
      return annotations;
    }

    public java.util.Set<javax.lang.model.element.Modifier> getFlags() {
      return flags;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitModifiers(this, d);
    }
  }

  public static class Module extends Base implements ModuleTree {
    public java.util.List<? extends com.sun.source.tree.AnnotationTree> annotations;
    public java.util.List<? extends com.sun.source.tree.DirectiveTree> directives;
    public com.sun.source.tree.ModuleTree.ModuleKind moduleType;
    public com.sun.source.tree.ExpressionTree name;

    public Module() {
      this.kind = Tree.Kind.MODULE;
    }

    public java.util.List<? extends com.sun.source.tree.AnnotationTree> getAnnotations() {
      return annotations;
    }

    public java.util.List<? extends com.sun.source.tree.DirectiveTree> getDirectives() {
      return directives;
    }

    public com.sun.source.tree.ModuleTree.ModuleKind getModuleType() {
      return moduleType;
    }

    public com.sun.source.tree.ExpressionTree getName() {
      return name;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitModule(this, d);
    }
  }

  public static class NewArray extends Expr implements NewArrayTree {
    public java.util.List<? extends com.sun.source.tree.AnnotationTree> annotations;
    public java.util.List<? extends java.util.List<? extends com.sun.source.tree.AnnotationTree>> dimAnnotations;
    public java.util.List<? extends com.sun.source.tree.ExpressionTree> dimensions;
    public java.util.List<? extends com.sun.source.tree.ExpressionTree> initializers;
    public com.sun.source.tree.Tree type;

    public NewArray() {
      this.kind = Tree.Kind.NEW_ARRAY;
    }

    public java.util.List<? extends com.sun.source.tree.AnnotationTree> getAnnotations() {
      return annotations;
    }

    public java.util.List<? extends java.util.List<? extends com.sun.source.tree.AnnotationTree>> getDimAnnotations() {
      return dimAnnotations;
    }

    public java.util.List<? extends com.sun.source.tree.ExpressionTree> getDimensions() {
      return dimensions;
    }

    public java.util.List<? extends com.sun.source.tree.ExpressionTree> getInitializers() {
      return initializers;
    }

    public com.sun.source.tree.Tree getType() {
      return type;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitNewArray(this, d);
    }
  }

  public static class NewClass extends Expr implements NewClassTree {
    public java.util.List<? extends com.sun.source.tree.ExpressionTree> arguments;
    public com.sun.source.tree.ClassTree classBody;
    public com.sun.source.tree.ExpressionTree enclosingExpression;
    public com.sun.source.tree.ExpressionTree identifier;
    public java.util.List<? extends com.sun.source.tree.Tree> typeArguments;

    public NewClass() {
      this.kind = Tree.Kind.NEW_CLASS;
    }

    public java.util.List<? extends com.sun.source.tree.ExpressionTree> getArguments() {
      return arguments;
    }

    public com.sun.source.tree.ClassTree getClassBody() {
      return classBody;
    }

    public com.sun.source.tree.ExpressionTree getEnclosingExpression() {
      return enclosingExpression;
    }

    public com.sun.source.tree.ExpressionTree getIdentifier() {
      return identifier;
    }

    public java.util.List<? extends com.sun.source.tree.Tree> getTypeArguments() {
      return typeArguments;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitNewClass(this, d);
    }
  }

  public static class Opens extends Base implements OpensTree {
    public java.util.List<? extends com.sun.source.tree.ExpressionTree> moduleNames;
    public com.sun.source.tree.ExpressionTree packageName;

    public Opens() {
      this.kind = Tree.Kind.OPENS;
    }

    public java.util.List<? extends com.sun.source.tree.ExpressionTree> getModuleNames() {
      return moduleNames;
    }

    public com.sun.source.tree.ExpressionTree getPackageName() {
      return packageName;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitOpens(this, d);
    }
  }

  public static class Package extends Base implements PackageTree {
    public java.util.List<? extends com.sun.source.tree.AnnotationTree> annotations;
    public com.sun.source.tree.ExpressionTree packageName;

    public Package() {
      this.kind = Tree.Kind.PACKAGE;
    }

    public java.util.List<? extends com.sun.source.tree.AnnotationTree> getAnnotations() {
      return annotations;
    }

    public com.sun.source.tree.ExpressionTree getPackageName() {
      return packageName;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitPackage(this, d);
    }
  }

  public static class ParameterizedType extends Expr implements ParameterizedTypeTree {
    public com.sun.source.tree.Tree type;
    public java.util.List<? extends com.sun.source.tree.Tree> typeArguments;

    public ParameterizedType() {
      this.kind = Tree.Kind.PARAMETERIZED_TYPE;
    }

    public com.sun.source.tree.Tree getType() {
      return type;
    }

    public java.util.List<? extends com.sun.source.tree.Tree> getTypeArguments() {
      return typeArguments;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitParameterizedType(this, d);
    }
  }

  public static class Parenthesized extends Expr implements ParenthesizedTree {
    public com.sun.source.tree.ExpressionTree expression;

    public Parenthesized() {
      this.kind = Tree.Kind.PARENTHESIZED;
    }

    public com.sun.source.tree.ExpressionTree getExpression() {
      return expression;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitParenthesized(this, d);
    }
  }

  public static class PatternCaseLabel extends Base implements PatternCaseLabelTree {
    public com.sun.source.tree.PatternTree pattern;

    public PatternCaseLabel() {
      this.kind = Tree.Kind.PATTERN_CASE_LABEL;
    }

    public com.sun.source.tree.PatternTree getPattern() {
      return pattern;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitPatternCaseLabel(this, d);
    }
  }

  public static class PrimitiveType extends Expr implements PrimitiveTypeTree {
    public javax.lang.model.type.TypeKind primitiveTypeKind;

    public PrimitiveType() {
      this.kind = Tree.Kind.PRIMITIVE_TYPE;
    }

    public javax.lang.model.type.TypeKind getPrimitiveTypeKind() {
      return primitiveTypeKind;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitPrimitiveType(this, d);
    }
  }

  public static class Provides extends Base implements ProvidesTree {
    public java.util.List<? extends com.sun.source.tree.ExpressionTree> implementationNames;
    public com.sun.source.tree.ExpressionTree serviceName;

    public Provides() {
      this.kind = Tree.Kind.PROVIDES;
    }

    public java.util.List<? extends com.sun.source.tree.ExpressionTree> getImplementationNames() {
      return implementationNames;
    }

    public com.sun.source.tree.ExpressionTree getServiceName() {
      return serviceName;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitProvides(this, d);
    }
  }

  public static class Requires extends Base implements RequiresTree {
    public com.sun.source.tree.ExpressionTree moduleName;
    public boolean static_;
    public boolean transitive;

    public Requires() {
      this.kind = Tree.Kind.REQUIRES;
    }

    public com.sun.source.tree.ExpressionTree getModuleName() {
      return moduleName;
    }

    public boolean isStatic() {
      return static_;
    }

    public boolean isTransitive() {
      return transitive;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitRequires(this, d);
    }
  }

  public static class Return extends Base implements ReturnTree {
    public com.sun.source.tree.ExpressionTree expression;

    public Return() {
      this.kind = Tree.Kind.RETURN;
    }

    public com.sun.source.tree.ExpressionTree getExpression() {
      return expression;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitReturn(this, d);
    }
  }

  public static class SwitchExpression extends Expr implements SwitchExpressionTree {
    public java.util.List<? extends com.sun.source.tree.CaseTree> cases;
    public com.sun.source.tree.ExpressionTree expression;

    public SwitchExpression() {
      this.kind = Tree.Kind.SWITCH_EXPRESSION;
    }

    public java.util.List<? extends com.sun.source.tree.CaseTree> getCases() {
      return cases;
    }

    public com.sun.source.tree.ExpressionTree getExpression() {
      return expression;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitSwitchExpression(this, d);
    }
  }

  public static class Switch extends Base implements SwitchTree {
    public java.util.List<? extends com.sun.source.tree.CaseTree> cases;
    public com.sun.source.tree.ExpressionTree expression;

    public Switch() {
      this.kind = Tree.Kind.SWITCH;
    }

    public java.util.List<? extends com.sun.source.tree.CaseTree> getCases() {
      return cases;
    }

    public com.sun.source.tree.ExpressionTree getExpression() {
      return expression;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitSwitch(this, d);
    }
  }

  public static class Synchronized extends Base implements SynchronizedTree {
    public com.sun.source.tree.BlockTree block;
    public com.sun.source.tree.ExpressionTree expression;

    public Synchronized() {
      this.kind = Tree.Kind.SYNCHRONIZED;
    }

    public com.sun.source.tree.BlockTree getBlock() {
      return block;
    }

    public com.sun.source.tree.ExpressionTree getExpression() {
      return expression;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitSynchronized(this, d);
    }
  }

  public static class Throw extends Base implements ThrowTree {
    public com.sun.source.tree.ExpressionTree expression;

    public Throw() {
      this.kind = Tree.Kind.THROW;
    }

    public com.sun.source.tree.ExpressionTree getExpression() {
      return expression;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitThrow(this, d);
    }
  }

  public static class Try extends Base implements TryTree {
    public com.sun.source.tree.BlockTree block;
    public java.util.List<? extends com.sun.source.tree.CatchTree> catches;
    public com.sun.source.tree.BlockTree finallyBlock;
    public java.util.List<? extends com.sun.source.tree.Tree> resources;

    public Try() {
      this.kind = Tree.Kind.TRY;
    }

    public com.sun.source.tree.BlockTree getBlock() {
      return block;
    }

    public java.util.List<? extends com.sun.source.tree.CatchTree> getCatches() {
      return catches;
    }

    public com.sun.source.tree.BlockTree getFinallyBlock() {
      return finallyBlock;
    }

    public java.util.List<? extends com.sun.source.tree.Tree> getResources() {
      return resources;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitTry(this, d);
    }
  }

  public static class TypeCast extends Expr implements TypeCastTree {
    public com.sun.source.tree.ExpressionTree expression;
    public com.sun.source.tree.Tree type;

    public TypeCast() {
      this.kind = Tree.Kind.TYPE_CAST;
    }

    public com.sun.source.tree.ExpressionTree getExpression() {
      return expression;
    }

    public com.sun.source.tree.Tree getType() {
      return type;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitTypeCast(this, d);
    }
  }

  public static class TypeParameter extends Base implements TypeParameterTree {
    public java.util.List<? extends com.sun.source.tree.AnnotationTree> annotations;
    public java.util.List<? extends com.sun.source.tree.Tree> bounds;
    public javax.lang.model.element.Name name;

    public TypeParameter() {
      this.kind = Tree.Kind.TYPE_PARAMETER;
    }

    public java.util.List<? extends com.sun.source.tree.AnnotationTree> getAnnotations() {
      return annotations;
    }

    public java.util.List<? extends com.sun.source.tree.Tree> getBounds() {
      return bounds;
    }

    public javax.lang.model.element.Name getName() {
      return name;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitTypeParameter(this, d);
    }
  }

  public static class Unary extends Expr implements UnaryTree {
    public com.sun.source.tree.ExpressionTree expression;

    public Unary() {
      this.kind = Tree.Kind.POSTFIX_INCREMENT;
    }

    public com.sun.source.tree.ExpressionTree getExpression() {
      return expression;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitUnary(this, d);
    }
  }

  public static class UnionType extends Expr implements UnionTypeTree {
    public java.util.List<? extends com.sun.source.tree.Tree> typeAlternatives;

    public UnionType() {
      this.kind = Tree.Kind.UNION_TYPE;
    }

    public java.util.List<? extends com.sun.source.tree.Tree> getTypeAlternatives() {
      return typeAlternatives;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitUnionType(this, d);
    }
  }

  public static class Uses extends Base implements UsesTree {
    public com.sun.source.tree.ExpressionTree serviceName;

    public Uses() {
      this.kind = Tree.Kind.USES;
    }

    public com.sun.source.tree.ExpressionTree getServiceName() {
      return serviceName;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitUses(this, d);
    }
  }

  public static class Variable extends Base implements VariableTree {
    public com.sun.source.tree.ExpressionTree initializer;
    public com.sun.source.tree.ModifiersTree modifiers;
    public javax.lang.model.element.Name name;
    public com.sun.source.tree.ExpressionTree nameExpression;
    public com.sun.source.tree.Tree type;
    public boolean varargs;
    public boolean enumConstant;

    public Variable() {
      this.kind = Tree.Kind.VARIABLE;
    }

    public com.sun.source.tree.ExpressionTree getInitializer() {
      return initializer;
    }

    public com.sun.source.tree.ModifiersTree getModifiers() {
      return modifiers;
    }

    public javax.lang.model.element.Name getName() {
      return name;
    }

    public com.sun.source.tree.ExpressionTree getNameExpression() {
      return nameExpression;
    }

    public com.sun.source.tree.Tree getType() {
      return type;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitVariable(this, d);
    }
  }

  public static class WhileLoop extends Base implements WhileLoopTree {
    public com.sun.source.tree.ExpressionTree condition;
    public com.sun.source.tree.StatementTree statement;

    public WhileLoop() {
      this.kind = Tree.Kind.WHILE_LOOP;
    }

    public com.sun.source.tree.ExpressionTree getCondition() {
      return condition;
    }

    public com.sun.source.tree.StatementTree getStatement() {
      return statement;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitWhileLoop(this, d);
    }
  }

  public static class Wildcard extends Expr implements WildcardTree {
    public com.sun.source.tree.Tree bound;

    public Wildcard() {
      this.kind = Tree.Kind.UNBOUNDED_WILDCARD;
    }

    public com.sun.source.tree.Tree getBound() {
      return bound;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitWildcard(this, d);
    }
  }

  public static class Yield extends Base implements YieldTree {
    public com.sun.source.tree.ExpressionTree value;

    public Yield() {
      this.kind = Tree.Kind.YIELD;
    }

    public com.sun.source.tree.ExpressionTree getValue() {
      return value;
    }

    public <R, D> R accept(TreeVisitor<R, D> v, D d) {
      return v.visitYield(this, d);
    }
  }

}
