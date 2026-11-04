/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;

public final class Parser {
  public static final class Diag {
    public final int pos;
    public final String code;
    public final String message;
    public final String[] args;

    Diag(int pos, String code, String message, String... args) {
      this.pos = pos;
      this.code = code;
      this.message = message;
      this.args = args;
    }
  }

  private static final class Fail extends RuntimeException {
    Fail() {
      super(null, null, false, false);
    }
  }

  private final List<Token> toks;
  private final String src;
  private final String fileName;
  public final List<Diag> diags = new ArrayList<Diag>();
  private int i;
  private int lastEnd;
  private boolean noLambda;
  private boolean inInterface;
  private String className = "";
  private int errPos = -1;
  private N.CompilationUnit curCu;
  private final java.util.IdentityHashMap<N.Modifiers, String> modDocs =
      new java.util.IdentityHashMap<N.Modifiers, String>();

  private void attachDoc(Tree t, N.Modifiers mods) {
    String d = mods == null ? null : modDocs.get(mods);
    if (d != null && curCu != null) {
      curCu.docComments.put(t, d);
    }
  }
  private final java.util.IdentityHashMap<N.Variable, N.Modifiers> compMods =
      new java.util.IdentityHashMap<N.Variable, N.Modifiers>();
  private final java.util.IdentityHashMap<N.Modifiers, Boolean> annotationMods =
      new java.util.IdentityHashMap<N.Modifiers, Boolean>();

  public Parser(String src, String fileName) {
    this.src = src;
    this.fileName = fileName;
    Lexer lx = new Lexer(src);
    this.toks = lx.lex();
    for (int k = 0; k < lx.errors.size(); k++) {
      diags.add(new Diag(lx.errors.get(k)[0], "compiler.err.illegal.char", lx.errorMessages.get(k)));
    }
  }

  private Token tok() {
    return toks.get(i);
  }

  private Token peek(int k) {
    return toks.get(Math.min(i + k, toks.size() - 1));
  }

  private boolean at(String s) {
    return tok().is(s);
  }

  private boolean peekIs(int k, String s) {
    return peek(k).is(s);
  }

  private boolean atIdent() {
    return tok().kind == Token.IDENT;
  }

  private boolean atIdent(String s) {
    return tok().kind == Token.IDENT && tok().text.equals(s);
  }

  private boolean atEof() {
    return tok().kind == Token.EOF;
  }

  private Token next() {
    Token t = toks.get(i);
    if (i < toks.size() - 1) {
      i++;
    }
    lastEnd = t.end;
    return t;
  }

  private boolean accept(String s) {
    if (at(s)) {
      next();
      return true;
    }
    return false;
  }

  private int start() {
    return tok().start;
  }

  private Fail error(int pos, String code, String msg, String... args) {
    report(pos, code, msg, args);
    return new Fail();
  }

  private void report(int pos, String code, String msg, String... args) {
    if (pos > errPos) {
      diags.add(new Diag(pos, code, msg, args));
    }
    if (pos > errPos) {
      errPos = pos;
    }
  }

  private void expectedSoft(String what) {
    if (atEof()) {
      report(lastEnd, "compiler.err.premature.eof", "reached end of file while parsing");
      return;
    }
    if (what.equals("<identifier>")) {
      report(lastEnd, "compiler.err.expected", "<identifier> expected", what);
      return;
    }
    report(lastEnd, "compiler.err.expected", "'" + what + "' expected", "'" + what + "'");
  }

  private Fail expected(String what) {
    expectedSoft(what);
    return new Fail();
  }

  private void expect(String s) {
    if (!accept(s)) {
      expectedSoft(s);
    }
  }

  private <T extends N.Base> T fin(T t, int start) {
    t.start = start;
    t.end = lastEnd;
    if (t.pos < 0) {
      t.pos = start;
    }
    return t;
  }

  private static <T> List<T> list() {
    return new ArrayList<T>();
  }

  private NameImpl name(String s) {
    return NameImpl.of(s);
  }

  private N.Identifier ident(String s, int start, int end) {
    N.Identifier id = new N.Identifier();
    id.name = name(s);
    id.start = start;
    id.end = end;
    id.pos = start;
    return id;
  }

  private NameImpl identName() {
    Token t = tok();
    if (t.kind == Token.IDENT) {
      next();
      return name(t.text);
    }
    if (t.is("_")) {
      next();
      return name("");
    }
    throw expected("<identifier>");
  }

  private N.Identifier parseIdentifier() {
    int s = start();
    NameImpl n = identName();
    return fin(identOf(n), s);
  }

  private N.Identifier identOf(javax.lang.model.element.Name n) {
    N.Identifier id = new N.Identifier();
    id.name = n;
    return id;
  }

  private N.Base qualident() {
    int s = start();
    N.Base e = parseIdentifier();
    while (at(".") && (peek(1).kind == Token.IDENT)) {
      int dotPos = tok().start;
      next();
      N.MemberSelect ms = new N.MemberSelect();
      ms.expression = (com.sun.source.tree.ExpressionTree) e;
      ms.identifier = identName();
      ms.pos = dotPos;
      e = fin(ms, s);
    }
    return e;
  }

  /* ---------- sync / recovery ---------- */

  private void skipToStatementEnd() {
    int depth = 0;
    while (!atEof()) {
      if (at("{")) {
        depth++;
      } else if (at("}")) {
        if (depth == 0) {
          return;
        }
        depth--;
      } else if (at(";") && depth == 0) {
        next();
        return;
      }
      next();
    }
  }

  /* ---------- compilation unit ---------- */

  public N.CompilationUnit parseCompilationUnit() {
    N.CompilationUnit cu = new N.CompilationUnit();
    curCu = cu;
    cu.sourceText = src;
    List<com.sun.source.tree.ImportTree> imports = new ArrayList<com.sun.source.tree.ImportTree>();
    List<Tree> typeDecls = new ArrayList<Tree>();
    cu.imports = imports;
    cu.typeDecls = typeDecls;
    cu.packageAnnotations = new ArrayList<com.sun.source.tree.AnnotationTree>();
    List<Tree> implicitMembers = new ArrayList<Tree>();
    int cuStart = start();
    N.Modifiers pendingMods = null;
    boolean first = true;
    while (!atEof()) {
      int before = i;
      try {
        if (accept(";")) {
          continue;
        }
        if (at("import") && !first || (at("import") && cu.packageName == null && pendingMods == null)) {
          imports.add(parseImport());
          continue;
        }
        if (pendingMods == null) {
          pendingMods = parseModifiers();
        }
        if (at("package") && first && cu.packageName == null) {
          int ps = pendingMods.start < 0 ? start() : pendingMods.start;
          next();
          N.Package pk = new N.Package();
          pk.annotations = pendingMods.annotations;
          pk.packageName = (com.sun.source.tree.ExpressionTree) qualident();
          expect(";");
          cu.package_ = fin(pk, ps);
          attachDoc(pk, pendingMods);
          cu.packageName = pk.packageName;
          cu.packageAnnotations = pendingMods.annotations;
          pendingMods = null;
          first = false;
          continue;
        }
        first = false;
        if (at("import")) {
          imports.add(parseImport());
          continue;
        }
        if ((atIdent("open") && peek(1).kind == Token.IDENT && peek(1).text.equals("module"))
            || (atIdent("module") && peek(1).kind == Token.IDENT && (peekIs(2, "{") || peekIs(2, ".")))) {
          cu.module = parseModule(pendingMods);
          pendingMods = null;
          continue;
        }
        if (atTypeDeclStart()) {
          typeDecls.add(parseTypeDecl(pendingMods));
          pendingMods = null;
          continue;
        }
        if (isMemberStart()) {
          implicitMembers.addAll(parseMember(pendingMods, false));
          pendingMods = null;
          continue;
        }
        pendingMods = null;
        throw error(tok().start, "compiler.err.expected4", "class, interface, enum, or record expected");
      } catch (Fail f) {
        pendingMods = null;
        first = false;
        if (i == before) {
          next();
        }
        skipToTopLevelSync();
      }
    }
    if (!implicitMembers.isEmpty()) {
      N.Class c = new N.Class();
      c.kind = Tree.Kind.CLASS;
      N.Modifiers m = new N.Modifiers();
      m.flags = EnumSet.of(Modifier.FINAL);
      m.annotations = new ArrayList<com.sun.source.tree.AnnotationTree>();
      c.modifiers = m;
      String base = fileName == null ? "" : fileName;
      int slash = Math.max(base.lastIndexOf('/'), base.lastIndexOf('\\'));
      base = base.substring(slash + 1);
      if (base.endsWith(".java")) {
        base = base.substring(0, base.length() - 5);
      }
      c.simpleName = name(base);
      c.typeParameters = new ArrayList<com.sun.source.tree.TypeParameterTree>();
      c.implementsClause = new ArrayList<Tree>();
      c.permitsClause = new ArrayList<Tree>();
      c.members = implicitMembers;
      c.start = implicitMembers.get(0) instanceof N.Base ? ((N.Base) implicitMembers.get(0)).start : 0;
      c.end = lastEnd;
      typeDecls.add(c);
    }
    cu.start = cuStart;
    cu.end = cu.module != null ? src.length() : lastEnd;
    cu.pos = cuStart;
    return cu;
  }

  private void skipToTopLevelSync() {
    int depth = 0;
    while (!atEof()) {
      if (at("{")) {
        depth++;
      } else if (at("}")) {
        if (depth > 0) {
          depth--;
          if (depth == 0) {
            next();
            return;
          }
        }
      } else if (at(";") && depth == 0) {
        next();
        return;
      } else if (depth == 0 && (at("class") || at("interface") || at("enum") || at("import") || at("@"))
          && !(peek(1).is("interface") && false)) {
        return;
      }
      next();
    }
  }

  private boolean atTypeDeclStart() {
    if (at("class") || at("interface") || at("enum")) {
      return true;
    }
    if (at("@") && peekIs(1, "interface")) {
      return true;
    }
    if (atIdent("record") && peek(1).kind == Token.IDENT && (peekIs(2, "(") || peekIs(2, "<"))) {
      return true;
    }
    return false;
  }

  private boolean isMemberStart() {
    return tok().kind == Token.IDENT || at("<") || at("void") || isPrimitiveStart() || at("{") || at("static");
  }

  private N.Import parseImport() {
    int s = start();
    expect("import");
    N.Import im = new N.Import();
    if (accept("static")) {
      im.static_ = true;
    } else if (atIdent("module") && peek(1).kind == Token.IDENT) {
      next();
      im.module = true;
    }
    int qs = start();
    N.Base q = parseIdentifier();
    while (at(".")) {
      int dot = tok().start;
      next();
      N.MemberSelect ms = new N.MemberSelect();
      ms.expression = (com.sun.source.tree.ExpressionTree) q;
      ms.pos = dot;
      if (at("*")) {
        next();
        ms.identifier = name("*");
      } else {
        ms.identifier = identName();
      }
      q = fin(ms, qs);
      if (ms.identifier.toString().equals("*")) {
        break;
      }
    }
    im.qualifiedIdentifier = q;
    expect(";");
    return fin(im, s);
  }

  private N.Module parseModule(N.Modifiers mods) {
    int s = mods != null && !mods.annotations.isEmpty() ? mods.start : start();
    N.Module m = new N.Module();
    attachDoc(m, mods);
    m.annotations = mods == null ? new ArrayList<com.sun.source.tree.AnnotationTree>() : mods.annotations;
    m.moduleType = ModuleTree.ModuleKind.STRONG;
    if (atIdent("open")) {
      next();
      m.moduleType = ModuleTree.ModuleKind.OPEN;
    }
    next();
    m.name = (com.sun.source.tree.ExpressionTree) qualident();
    expect("{");
    List<com.sun.source.tree.DirectiveTree> dirs = new ArrayList<com.sun.source.tree.DirectiveTree>();
    while (!at("}") && !atEof()) {
      int ds = start();
      if (atIdent("requires")) {
        next();
        N.Requires r = new N.Requires();
        while (true) {
          if (atIdent("transitive") && !(peekIs(1, ";") || peekIs(1, "."))) {
            next();
            r.transitive = true;
          } else if (at("static")) {
            next();
            r.static_ = true;
          } else {
            break;
          }
        }
        r.moduleName = (com.sun.source.tree.ExpressionTree) qualident();
        expect(";");
        dirs.add(fin(r, ds));
      } else if (atIdent("exports") || atIdent("opens")) {
        boolean exp = atIdent("exports");
        next();
        com.sun.source.tree.ExpressionTree pn = (com.sun.source.tree.ExpressionTree) qualident();
        List<com.sun.source.tree.ExpressionTree> to = new ArrayList<com.sun.source.tree.ExpressionTree>();
        if (atIdent("to")) {
          next();
          to.add((com.sun.source.tree.ExpressionTree) qualident());
          while (accept(",")) {
            to.add((com.sun.source.tree.ExpressionTree) qualident());
          }
        }
        expect(";");
        if (exp) {
          N.Exports e = new N.Exports();
          e.packageName = pn;
          e.moduleNames = to.isEmpty() ? null : to;
          dirs.add(fin(e, ds));
        } else {
          N.Opens o = new N.Opens();
          o.packageName = pn;
          o.moduleNames = to.isEmpty() ? null : to;
          dirs.add(fin(o, ds));
        }
      } else if (atIdent("uses")) {
        next();
        N.Uses u = new N.Uses();
        u.serviceName = (com.sun.source.tree.ExpressionTree) qualident();
        expect(";");
        dirs.add(fin(u, ds));
      } else if (atIdent("provides")) {
        next();
        N.Provides p = new N.Provides();
        p.serviceName = (com.sun.source.tree.ExpressionTree) qualident();
        if (!atIdent("with")) {
          throw expected("with");
        }
        next();
        List<com.sun.source.tree.ExpressionTree> impl = new ArrayList<com.sun.source.tree.ExpressionTree>();
        impl.add((com.sun.source.tree.ExpressionTree) qualident());
        while (accept(",")) {
          impl.add((com.sun.source.tree.ExpressionTree) qualident());
        }
        p.implementationNames = impl;
        expect(";");
        dirs.add(fin(p, ds));
      } else {
        throw error(tok().start, "compiler.err.invalid.module.directive", "module directive keyword expected");
      }
    }
    expect("}");
    m.directives = dirs;
    fin(m, s);
    m.end = src.length();
    return m;
  }

  /* ---------- modifiers & annotations ---------- */

  private static boolean isModifierKeyword(Token t) {
    if (t.kind != Token.KEYWORD) {
      return false;
    }
    switch (t.text) {
      case "public":
      case "protected":
      case "private":
      case "static":
      case "abstract":
      case "final":
      case "native":
      case "synchronized":
      case "transient":
      case "volatile":
      case "strictfp":
      case "default":
        return true;
      default:
        return false;
    }
  }

  private N.Modifiers parseModifiers() {
    int s = start();
    String doc = tok().doc;
    N.Modifiers m = new N.Modifiers();
    m.annotations = new ArrayList<com.sun.source.tree.AnnotationTree>();
    Set<Modifier> flags = EnumSet.noneOf(Modifier.class);
    boolean any = false;
    while (true) {
      Token t = tok();
      if (t.is("@") && peekIs(1, "interface")) {
        next();
        annotationMods.put(m, Boolean.TRUE);
        any = true;
        break;
      } else if (t.is("@")) {
        ((List<com.sun.source.tree.AnnotationTree>) m.annotations).add(parseAnnotation());
        any = true;
      } else if (isModifierKeyword(t)) {
        if (t.is("default") && (peekIs(1, ":") || peekIs(1, "->"))) {
          break;
        }
        next();
        flags.add(Modifier.valueOf(t.text.toUpperCase()));
        any = true;
      } else if (t.kind == Token.IDENT && t.text.equals("sealed") && modifierContext(1)) {
        next();
        flags.add(Modifier.SEALED);
        any = true;
      } else if (t.kind == Token.IDENT && t.text.equals("non") && peekIs(1, "-") && peek(2).kind == Token.IDENT
          && peek(2).text.equals("sealed") && peek(1).start == t.end && peek(2).start == peek(1).end) {
        next();
        next();
        next();
        flags.add(Modifier.NON_SEALED);
        any = true;
      } else {
        break;
      }
    }
    m.flags = flags;
    if (doc != null) {
      modDocs.put(m, doc);
    }
    m.start = any ? s : -1;
    m.end = any ? lastEnd : -1;
    m.pos = m.start;
    return m;
  }

  private boolean modifierContext(int k) {
    Token n = peek(k);
    return n.kind == Token.KEYWORD && (isModifierKeyword(n) || n.text.equals("class") || n.text.equals("interface"))
        || n.is("@") || (n.kind == Token.IDENT && (n.text.equals("non") || n.text.equals("record")
        || n.text.equals("sealed")));
  }

  private N.Annotation parseAnnotation() {
    return parseAnnotation(false);
  }

  private N.Annotation parseAnnotation(boolean typeAnn) {
    int s = start();
    expect("@");
    N.Annotation a = new N.Annotation();
    a.kind = typeAnn ? Tree.Kind.TYPE_ANNOTATION : Tree.Kind.ANNOTATION;
    a.annotationType = (com.sun.source.tree.Tree) qualident();
    List<com.sun.source.tree.ExpressionTree> args = new ArrayList<com.sun.source.tree.ExpressionTree>();
    if (accept("(")) {
      if (!at(")")) {
        do {
          args.add(annotationValue(true));
        } while (accept(","));
      }
      expect(")");
    }
    a.arguments = args;
    return fin(a, s);
  }

  private com.sun.source.tree.ExpressionTree annotationValue(boolean allowAssign) {
    int s = start();
    if (allowAssign && atIdent() && peekIs(1, "=")) {
      N.Identifier id = parseIdentifier();
      expect("=");
      N.Assignment as = new N.Assignment();
      as.variable = id;
      as.expression = annotationValue(false);
      return fin(as, s);
    }
    if (at("@")) {
      return parseAnnotation();
    }
    if (at("{")) {
      next();
      N.NewArray na = new N.NewArray();
      na.annotations = new ArrayList<com.sun.source.tree.AnnotationTree>();
      na.dimAnnotations = new ArrayList<java.util.List<? extends com.sun.source.tree.AnnotationTree>>();
      na.dimensions = new ArrayList<com.sun.source.tree.ExpressionTree>();
      List<com.sun.source.tree.ExpressionTree> inits = new ArrayList<com.sun.source.tree.ExpressionTree>();
      while (!at("}") && !atEof()) {
        inits.add(annotationValue(false));
        if (!accept(",")) {
          break;
        }
      }
      expect("}");
      na.initializers = inits;
      return fin(na, s);
    }
    return parseTernary();
  }

  /* ---------- types ---------- */

  private boolean isPrimitiveStart() {
    Token t = tok();
    if (t.kind != Token.KEYWORD) {
      return false;
    }
    switch (t.text) {
      case "int":
      case "long":
      case "short":
      case "byte":
      case "char":
      case "boolean":
      case "float":
      case "double":
        return true;
      default:
        return false;
    }
  }

  private N.PrimitiveType primitive() {
    int s = start();
    String t = next().text;
    N.PrimitiveType p = new N.PrimitiveType();
    p.primitiveTypeKind = t.equals("void") ? TypeKind.VOID : TypeKind.valueOf(t.toUpperCase());
    return fin(p, s);
  }

  private List<com.sun.source.tree.AnnotationTree> typeAnnotations() {
    List<com.sun.source.tree.AnnotationTree> l = null;
    while (at("@") && !peekIs(1, "interface")) {
      if (l == null) {
        l = new ArrayList<com.sun.source.tree.AnnotationTree>();
      }
      l.add(parseAnnotation(true));
    }
    return l;
  }

  private N.Base annotated(List<com.sun.source.tree.AnnotationTree> anns, N.Base type, int s) {
    if (anns == null || anns.isEmpty()) {
      return type;
    }
    N.AnnotatedType at = new N.AnnotatedType();
    at.annotations = anns;
    at.underlyingType = (com.sun.source.tree.ExpressionTree) type;
    return fin(at, s);
  }

  public N.Base parseType() {
    return parseType(true);
  }

  private N.Base parseType(boolean allowDims) {
    int s = start();
    List<com.sun.source.tree.AnnotationTree> anns = typeAnnotations();
    N.Base t;
    if (isPrimitiveStart()) {
      t = primitive();
    } else if (at("?")) {
      return wildcard();
    } else if (atIdent() || at("_")) {
      t = classType();
    } else if (at("void")) {
      t = primitive();
    } else {
      throw expected("<identifier>");
    }
    if (allowDims) {
      t = bracketsOpt(t, s);
    }
    if (anns != null && !anns.isEmpty()) {
      t = annotateInnermost(t, anns);
    }
    return t;
  }

  private N.Base annotateInnermost(N.Base type, List<com.sun.source.tree.AnnotationTree> anns) {
    N.Base parent = null;
    int mode = 0;
    N.Base cur = type;
    while (true) {
      if (cur instanceof N.ArrayType) {
        parent = cur;
        mode = 1;
        cur = (N.Base) ((N.ArrayType) cur).type;
      } else if (cur instanceof N.MemberSelect) {
        parent = cur;
        mode = 2;
        cur = (N.Base) ((N.MemberSelect) cur).expression;
      } else if (cur instanceof N.ParameterizedType) {
        parent = cur;
        mode = 3;
        cur = (N.Base) ((N.ParameterizedType) cur).type;
      } else if (cur instanceof N.AnnotatedType && ((N.AnnotatedType) cur).underlyingType instanceof N.ArrayType) {
        parent = cur;
        mode = 4;
        cur = (N.Base) ((N.AnnotatedType) cur).underlyingType;
      } else {
        break;
      }
    }
    N.AnnotatedType at = new N.AnnotatedType();
    at.annotations = anns;
    at.underlyingType = (com.sun.source.tree.ExpressionTree) cur;
    at.start = ((N.Base) anns.get(0)).start;
    at.pos = at.start;
    at.end = cur.end;
    if (parent == null) {
      return at;
    }
    switch (mode) {
      case 1:
        ((N.ArrayType) parent).type = at;
        break;
      case 2:
        ((N.MemberSelect) parent).expression = at;
        break;
      case 3:
        ((N.ParameterizedType) parent).type = at;
        break;
      default:
        ((N.AnnotatedType) parent).underlyingType = at;
        break;
    }
    if (parent.start > at.start) {
      parent.start = at.start;
    }
    N.Base walk = type;
    while (walk != parent && walk != null) {
      if (walk.start > at.start) {
        walk.start = at.start;
      }
      walk = walk instanceof N.ArrayType ? (N.Base) ((N.ArrayType) walk).type
          : walk instanceof N.MemberSelect ? (N.Base) ((N.MemberSelect) walk).expression
          : walk instanceof N.ParameterizedType ? (N.Base) ((N.ParameterizedType) walk).type : null;
    }
    return type;
  }

  private N.Base wildcard() {
    int s = start();
    expect("?");
    N.Wildcard w = new N.Wildcard();
    if (accept("extends")) {
      w.kind = Tree.Kind.EXTENDS_WILDCARD;
      w.bound = parseType();
    } else if (accept("super")) {
      w.kind = Tree.Kind.SUPER_WILDCARD;
      w.bound = parseType();
    } else {
      w.kind = Tree.Kind.UNBOUNDED_WILDCARD;
    }
    return fin(w, s);
  }

  private N.Base bracketsOpt(N.Base t, int s) {
    List<List<com.sun.source.tree.AnnotationTree>> groups = new ArrayList<List<com.sun.source.tree.AnnotationTree>>();
    while (true) {
      List<com.sun.source.tree.AnnotationTree> anns = null;
      int save = i;
      if (at("@")) {
        anns = typeAnnotations();
      }
      if (at("[") && peekIs(1, "]")) {
        next();
        next();
        groups.add(anns);
      } else {
        i = save;
        break;
      }
    }
    for (int k = groups.size() - 1; k >= 0; k--) {
      N.ArrayType a = new N.ArrayType();
      a.type = t;
      t = annotated(groups.get(k), fin(a, s), s);
    }
    return t;
  }

  private N.Base classType() {
    int s = start();
    N.Base t;
    if (at("_")) {
      t = parseIdentifier();
    } else {
      t = parseIdentifier();
    }
    if (at("<")) {
      t = typeArgs(t, s);
    }
    while (at(".") && (peek(1).kind == Token.IDENT || peek(1).is("@"))) {
      next();
      List<com.sun.source.tree.AnnotationTree> anns = typeAnnotations();
      N.MemberSelect ms = new N.MemberSelect();
      ms.expression = (com.sun.source.tree.ExpressionTree) t;
      ms.identifier = identName();
      t = fin(ms, s);
      if (anns != null) {
        t = annotated(anns, t, s);
      }
      if (at("<")) {
        t = typeArgs(t, s);
      }
    }
    return t;
  }

  private N.Base typeArgs(N.Base t, int s) {
    N.ParameterizedType pt = new N.ParameterizedType();
    pt.type = t;
    pt.typeArguments = typeArgumentList();
    return fin(pt, s);
  }

  private List<com.sun.source.tree.ExpressionTree> exprList(List<Tree> l) {
    List<com.sun.source.tree.ExpressionTree> r = new ArrayList<com.sun.source.tree.ExpressionTree>();
    for (Tree t : l) {
      r.add((com.sun.source.tree.ExpressionTree) t);
    }
    return r;
  }

  private List<Tree> typeArgumentList() {
    expect("<");
    List<Tree> args = new ArrayList<Tree>();
    if (at(">")) {
      next();
      return args;
    }
    do {
      args.add(parseType());
    } while (accept(","));
    expect(">");
    return args;
  }

  private List<com.sun.source.tree.TypeParameterTree> typeParametersOpt() {
    List<com.sun.source.tree.TypeParameterTree> l = new ArrayList<com.sun.source.tree.TypeParameterTree>();
    if (!at("<")) {
      return l;
    }
    next();
    do {
      int s = start();
      N.TypeParameter tp = new N.TypeParameter();
      List<com.sun.source.tree.AnnotationTree> anns = typeAnnotations();
      tp.annotations = anns == null ? new ArrayList<com.sun.source.tree.AnnotationTree>() : anns;
      tp.name = identName();
      List<Tree> bounds = new ArrayList<Tree>();
      if (accept("extends")) {
        bounds.add(parseType());
        while (accept("&")) {
          bounds.add(parseType());
        }
      }
      tp.bounds = bounds;
      l.add(fin(tp, s));
    } while (accept(","));
    expect(">");
    return l;
  }

  /* ---------- declarations ---------- */

  private N.Class parseTypeDecl(N.Modifiers mods) {
    int s = mods != null && mods.start >= 0 ? mods.start : start();
    if (mods == null) {
      mods = parseModifiers();
      if (mods.start >= 0) {
        s = mods.start;
      }
    }
    if (at("class")) {
      return parseClass(mods, s);
    }
    if (at("interface")) {
      return parseInterface(mods, s, annotationMods.containsKey(mods));
    }
    if (at("enum")) {
      return parseEnum(mods, s);
    }
    if (at("@") && peekIs(1, "interface")) {
      next();
      return parseInterface(mods, s, true);
    }
    if (at("interface") && annotationMods.containsKey(mods)) {
      return parseInterface(mods, s, true);
    }
    if (atIdent("record")) {
      return parseRecord(mods, s);
    }
    throw error(tok().start, "compiler.err.expected4", "class, interface, enum, or record expected");
  }

  private N.Class newClass(N.Modifiers mods, Tree.Kind kind) {
    N.Class c = new N.Class();
    attachDoc(c, mods);
    c.kind = kind;
    c.modifiers = mods;
    c.typeParameters = new ArrayList<com.sun.source.tree.TypeParameterTree>();
    c.implementsClause = new ArrayList<Tree>();
    c.permitsClause = new ArrayList<Tree>();
    return c;
  }

  private N.Class parseClass(N.Modifiers mods, int s) {
    expect("class");
    N.Class c = newClass(mods, Tree.Kind.CLASS);
    c.simpleName = identName();
    String saveName = className;
    className = c.simpleName.toString();
    c.typeParameters = typeParametersOpt();
    if (accept("extends")) {
      c.extendsClause = parseType();
    }
    if (accept("implements")) {
      List<Tree> l = new ArrayList<Tree>();
      do {
        l.add(parseType());
      } while (accept(","));
      c.implementsClause = l;
    }
    if (atIdent("permits")) {
      next();
      List<Tree> l = new ArrayList<Tree>();
      do {
        l.add(parseType());
      } while (accept(","));
      c.permitsClause = l;
    }
    c.members = classBody(false, false);
    className = saveName;
    return fin(c, s);
  }

  private N.Class parseInterface(N.Modifiers mods, int s, boolean annotation) {
    if (!annotation) {
      expect("interface");
    } else {
      expect("interface");
    }
    N.Class c = newClass(mods, annotation ? Tree.Kind.ANNOTATION_TYPE : Tree.Kind.INTERFACE);
    c.simpleName = identName();
    String saveName = className;
    className = c.simpleName.toString();
    c.typeParameters = typeParametersOpt();
    if (accept("extends")) {
      List<Tree> l = new ArrayList<Tree>();
      do {
        l.add(parseType());
      } while (accept(","));
      c.implementsClause = l;
    }
    if (atIdent("permits")) {
      next();
      List<Tree> l = new ArrayList<Tree>();
      do {
        l.add(parseType());
      } while (accept(","));
      c.permitsClause = l;
    }
    c.members = classBody(true, false);
    className = saveName;
    return fin(c, s);
  }

  private N.Class parseRecord(N.Modifiers mods, int s) {
    next();
    N.Class c = newClass(mods, Tree.Kind.RECORD);
    c.simpleName = identName();
    String saveName = className;
    className = c.simpleName.toString();
    c.typeParameters = typeParametersOpt();
    expect("(");
    List<Tree> comps = new ArrayList<Tree>();
    if (!at(")")) {
      do {
        int ps = start();
        N.Modifiers pm = parseModifiers();
        N.Base t = parseType();
        boolean varargs = false;
        if (at("...")) {
          next();
          varargs = true;
          N.ArrayType at = new N.ArrayType();
          at.type = t;
          t = fin(at, t.start);
        }
        N.Variable v = new N.Variable();
        v.varargs = varargs;
        v.modifiers = pm;
        v.type = t;
        v.name = identName();
        N.Modifiers fm = new N.Modifiers();
        fm.annotations = pm.annotations;
        fm.flags = EnumSet.of(Modifier.PRIVATE, Modifier.FINAL);
        fm.start = pm.start;
        fm.end = pm.end;
        fm.pos = pm.pos;
        v.modifiers = fm;
        compMods.put(v, pm);
        comps.add(fin(v, ps));
      } while (accept(","));
    }
    expect(")");
    if (accept("implements")) {
      List<Tree> l = new ArrayList<Tree>();
      do {
        l.add(parseType());
      } while (accept(","));
      c.implementsClause = l;
    }
    List<Tree> body = classBody(false, true);
    for (Tree bt : body) {
      if (bt instanceof N.Method && ((N.Method) bt).parameters == null) {
        List<com.sun.source.tree.VariableTree> ps = new ArrayList<com.sun.source.tree.VariableTree>();
        for (Tree ct : comps) {
          N.Variable cv = (N.Variable) ct;
          N.Variable pv = new N.Variable();
          N.Modifiers orig = compMods.get(cv);
          N.Modifiers cm = new N.Modifiers();
          cm.annotations = orig.annotations;
          cm.flags = EnumSet.noneOf(Modifier.class);
          cm.start = orig.start;
          cm.end = orig.end;
          cm.pos = orig.pos;
          pv.modifiers = cm;
          pv.type = cv.type;
          pv.name = cv.name;
          pv.start = cv.start;
          pv.pos = cv.start;
          pv.end = -1;
          ps.add(pv);
        }
        ((N.Method) bt).parameters = ps;
      }
    }
    List<Tree> members = new ArrayList<Tree>(comps);
    members.addAll(body);
    c.members = members;
    className = saveName;
    return fin(c, s);
  }

  private N.Class parseEnum(N.Modifiers mods, int s) {
    expect("enum");
    N.Class c = newClass(mods, Tree.Kind.ENUM);
    c.simpleName = identName();
    String saveName = className;
    className = c.simpleName.toString();
    if (accept("implements")) {
      List<Tree> l = new ArrayList<Tree>();
      do {
        l.add(parseType());
      } while (accept(","));
      c.implementsClause = l;
    }
    expect("{");
    List<Tree> members = new ArrayList<Tree>();
    boolean wasInterface = inInterface;
    inInterface = false;
    while (!at(";") && !at("}") && !atEof()) {
      try {
        int vs = start();
        N.Modifiers em = parseModifiers();
        N.Variable v = new N.Variable();
        attachDoc(v, em);
        N.Modifiers fm = new N.Modifiers();
        fm.annotations = em.annotations;
        fm.flags = EnumSet.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        fm.start = em.start;
        fm.end = em.end;
        v.modifiers = fm;
        v.enumConstant = true;
        v.name = identName();
        N.Identifier ty = identOf(c.simpleName);
        ty.start = vs;
        ty.end = -1;
        ty.pos = vs;
        v.type = ty;
        N.NewClass nc = new N.NewClass();
        N.Identifier ty2 = identOf(c.simpleName);
        ty2.start = vs;
        ty2.end = -1;
        ty2.pos = vs;
        nc.identifier = ty2;
        nc.typeArguments = new ArrayList<Tree>();
        int ncStart = vs;
        boolean hasArgs = at("(");
        if (hasArgs) {
          ncStart = start();
        }
        nc.arguments = hasArgs ? arguments() : new ArrayList<com.sun.source.tree.ExpressionTree>();
        boolean hasBody = at("{");
        if (hasBody) {
          N.Class body = newClass(emptyMods(), Tree.Kind.ENUM);
          body.simpleName = name("");
          int bs = start();
          ncStart = bs;
          body.members = classBody(false, false);
          body.start = vs;
          body.end = lastEnd;
          body.pos = vs;
          nc.classBody = body;
        }
        nc.start = ncStart;
        nc.pos = ncStart;
        nc.end = hasArgs || hasBody ? lastEnd : -1;
        v.initializer = nc;
        members.add(fin(v, vs));
        if (!accept(",")) {
          break;
        }
      } catch (Fail f) {
        skipEnumConstant();
      }
    }
    if (accept(";")) {
      members.addAll(classBodyMembers(false, false));
    }
    expect("}");
    inInterface = wasInterface;
    c.members = members;
    className = saveName;
    return fin(c, s);
  }

  private void skipEnumConstant() {
    int depth = 0;
    while (!atEof()) {
      if (at("{") || at("(")) {
        depth++;
      } else if (at("}") || at(")")) {
        if (depth == 0) {
          return;
        }
        depth--;
      } else if (depth == 0 && (at(",") || at(";"))) {
        if (at(",")) {
          next();
        }
        return;
      }
      next();
    }
  }

  private N.Modifiers emptyMods() {
    N.Modifiers m = new N.Modifiers();
    m.annotations = new ArrayList<com.sun.source.tree.AnnotationTree>();
    m.flags = EnumSet.noneOf(Modifier.class);
    m.start = -1;
    m.end = -1;
    m.pos = -1;
    return m;
  }

  private List<Tree> classBody(boolean intf, boolean record) {
    expect("{");
    List<Tree> members = classBodyMembers(intf, record);
    expect("}");
    return members;
  }

  private List<Tree> classBodyMembers(boolean intf, boolean record) {
    boolean save = inInterface;
    inInterface = intf;
    List<Tree> members = new ArrayList<Tree>();
    while (!at("}") && !atEof()) {
      int before = i;
      try {
        if (accept(";")) {
          continue;
        }
        members.addAll(parseMember(null, record));
        if (i == before) {
          next();
        }
      } catch (Fail f) {
        if (i == before) {
          next();
        }
        skipMemberSync();
      }
    }
    inInterface = save;
    return members;
  }

  private void skipMemberSync() {
    int depth = 0;
    while (!atEof()) {
      if (at("{")) {
        depth++;
      } else if (at("}")) {
        if (depth == 0) {
          return;
        }
        depth--;
        if (depth == 0) {
          next();
          return;
        }
      } else if (at(";") && depth == 0) {
        next();
        return;
      }
      next();
    }
  }

  private List<Tree> parseMember(N.Modifiers mods, boolean record) {
    List<Tree> out = new ArrayList<Tree>();
    int s = start();
    if (mods == null) {
      mods = parseModifiers();
    }
    if (mods.start >= 0) {
      s = mods.start;
    } else if (at("<")) {
      mods.start = start();
      mods.end = mods.start;
      mods.pos = mods.start;
    }
    if (at("{")) {
      N.Block b = block();
      attachDoc(b, mods);
      if (mods.flags.contains(Modifier.STATIC)) {
        b.static_ = true;
      }
      b.start = s;
      out.add(b);
      return out;
    }
    if (atTypeDeclStart()) {
      out.add(parseTypeDecl(mods));
      return out;
    }
    List<com.sun.source.tree.TypeParameterTree> tps = typeParametersOpt();
    boolean generic = !tps.isEmpty();
    if (generic && at("@")) {
      List<com.sun.source.tree.AnnotationTree> extra = new ArrayList<com.sun.source.tree.AnnotationTree>();
      while (at("@") && !peekIs(1, "interface")) {
        extra.add(parseAnnotation());
      }
      List<com.sun.source.tree.AnnotationTree> all = new ArrayList<com.sun.source.tree.AnnotationTree>(mods.annotations);
      all.addAll(extra);
      mods.annotations = all;
    }
    if (atIdent() && peekIs(1, "(") && tok().text.equals(className) && !inInterface) {
      out.add(parseMethodRest(mods, tps, null, identName(), s, true));
      return out;
    }
    if (record && atIdent() && peekIs(1, "{") && tok().text.equals(className)) {
      NameImpl nm = identName();
      N.Method m = new N.Method();
      attachDoc(m, mods);
      m.compactConstructor = true;
      m.modifiers = mods;
      m.typeParameters = tps;
      m.name = name("<init>");
      m.parameters = null;
      m.throws_ = new ArrayList<com.sun.source.tree.ExpressionTree>();
      m.body = block();
      out.add(fin(m, s));
      return out;
    }
    N.Base type;
    int ts = start();
    if (at("void")) {
      type = primitive();
    } else {
      type = parseType();
    }
    if (atIdent() || at("_")) {
      int nameStart = start();
      NameImpl nm = identName();
      if (at("(")) {
        out.add(parseMethodRest(mods, tps, type, nm, s, false));
        return out;
      }
      if (generic) {
        throw expected("(");
      }
      out.addAll(variableDeclarators(mods, type, nm, nameStart, s, true));
      expect(";");
      ((N.Base) out.get(out.size() - 1)).end = lastEnd;
      return out;
    }
    if (at("(") && type instanceof N.Identifier) {
      throw error(ts, "compiler.err.invalid.meth.decl.ret.type.req", "invalid method declaration; return type required");
    }
    throw expected("<identifier>");
  }

  private N.Method parseMethodRest(N.Modifiers mods, List<com.sun.source.tree.TypeParameterTree> tps, N.Base ret,
      NameImpl nm, int s, boolean ctor) {
    N.Method m = new N.Method();
    attachDoc(m, mods);
    m.modifiers = mods;
    m.typeParameters = tps;
    m.returnType = ret;
    m.name = ctor ? name("<init>") : nm;
    List<com.sun.source.tree.VariableTree> params = new ArrayList<com.sun.source.tree.VariableTree>();
    expect("(");
    if (!at(")")) {
      do {
        int ps = start();
        N.Modifiers pm = parseModifiers();
        N.Base t = parseType();
        boolean varargs = false;
        int saveI = i;
        List<com.sun.source.tree.AnnotationTree> vanns = at("@") ? typeAnnotations() : null;
        if (at("...")) {
          next();
          varargs = true;
          N.ArrayType a = new N.ArrayType();
          a.type = t;
          t = fin(a, t.start);
          for (Tree in = a.type; in instanceof N.ArrayType; in = ((N.ArrayType) in).type) {
            ((N.ArrayType) in).end = a.end;
          }
          if (vanns != null) {
            t = annotated(vanns, t, t.start);
            N.Base leaf = (N.Base) a.type;
            while (leaf instanceof N.ArrayType) {
              leaf = (N.Base) ((N.ArrayType) leaf).type;
            }
            leaf.end = ((N.Base) vanns.get(vanns.size() - 1)).end;
          }
        } else {
          i = saveI;
        }
        if (at("this")) {
          next();
          N.Variable rv = new N.Variable();
          rv.modifiers = pm;
          rv.type = t;
          rv.name = name("this");
          m.receiverParameter = fin(rv, ps);
          continue;
        }
        if (atIdent() && peekIs(1, ".") && peekIs(2, "this")) {
          N.Base q = parseIdentifier();
          next();
          next();
          N.Variable rv = new N.Variable();
          rv.modifiers = pm;
          rv.type = t;
          rv.name = name("this");
          rv.nameExpression = (com.sun.source.tree.ExpressionTree) q;
          m.receiverParameter = fin(rv, ps);
          continue;
        }
        N.Variable v = new N.Variable();
        v.varargs = varargs;
        v.modifiers = pm;
        v.type = bracketsOpt(t, t.start);
        v.name = identName();
        v.type = bracketsOpt((N.Base) v.type, t.start);
        params.add(fin(v, ps));
      } while (accept(","));
    }
    expect(")");
    m.parameters = params;
    while (at("[") && peekIs(1, "]")) {
      next();
      next();
    }
    List<com.sun.source.tree.ExpressionTree> thr = new ArrayList<com.sun.source.tree.ExpressionTree>();
    if (accept("throws")) {
      do {
        thr.add((com.sun.source.tree.ExpressionTree) parseType());
      } while (accept(","));
    }
    m.throws_ = thr;
    if (accept("default")) {
      m.defaultValue = annotationValue(false);
    }
    if (at("{")) {
      m.body = block();
    } else {
      if (!at(";")) {
        if (atEof()) {
          report(lastEnd, "compiler.err.premature.eof", "reached end of file while parsing");
        } else {
          report(lastEnd, "compiler.err.expected2", "'{' or ';' expected");
        }
      } else {
        next();
      }
    }
    return fin(m, s);
  }

  private List<Tree> variableDeclarators(N.Modifiers mods, N.Base type, NameImpl first, int firstStart, int s,
      boolean field) {
    List<Tree> out = new ArrayList<Tree>();
    NameImpl nm = first;
    int ns = firstStart;
    N.Base baseType = type;
    while (true) {
      N.Variable v = new N.Variable();
      attachDoc(v, mods);
      v.modifiers = mods;
      N.Base t = baseType;
      while (at("[") && peekIs(1, "]")) {
        next();
        next();
        N.ArrayType a = new N.ArrayType();
        a.type = t;
        t = fin(a, baseType == null ? ns : baseType.start);
      }
      v.type = t;
      v.name = nm;
      v.pos = ns;
      if (accept("=")) {
        v.initializer = variableInitializer();
      }
      out.add(fin(v, s));
      if (!accept(",")) {
        break;
      }
      v.end = lastEnd;
      ns = start();
      nm = identName();
    }
    return out;
  }

  private com.sun.source.tree.ExpressionTree variableInitializer() {
    if (at("{")) {
      return arrayInitializer(null, start());
    }
    return parseExpression();
  }

  private N.NewArray arrayInitializer(N.Base elemType, int s) {
    expect("{");
    N.NewArray na = new N.NewArray();
    na.type = elemType;
    na.annotations = new ArrayList<com.sun.source.tree.AnnotationTree>();
    na.dimAnnotations = new ArrayList<java.util.List<? extends com.sun.source.tree.AnnotationTree>>();
    na.dimensions = new ArrayList<com.sun.source.tree.ExpressionTree>();
    List<com.sun.source.tree.ExpressionTree> inits = new ArrayList<com.sun.source.tree.ExpressionTree>();
    while (!at("}") && !atEof()) {
      inits.add(variableInitializer());
      if (!accept(",")) {
        break;
      }
    }
    expect("}");
    na.initializers = inits;
    return fin(na, s);
  }

  /* ---------- scanning helpers ---------- */

  private Token tk(int j) {
    return toks.get(Math.min(j, toks.size() - 1));
  }

  private boolean primitiveAt(int j) {
    Token t = tk(j);
    if (t.kind != Token.KEYWORD) {
      return false;
    }
    switch (t.text) {
      case "int":
      case "long":
      case "short":
      case "byte":
      case "char":
      case "boolean":
      case "float":
      case "double":
        return true;
      default:
        return false;
    }
  }

  private int skipAnnotation(int j) {
    if (!tk(j).is("@")) {
      return j;
    }
    j++;
    if (tk(j).kind != Token.IDENT) {
      return -1;
    }
    j++;
    while (tk(j).is(".") && tk(j + 1).kind == Token.IDENT) {
      j += 2;
    }
    if (tk(j).is("(")) {
      int depth = 0;
      while (tk(j).kind != Token.EOF) {
        if (tk(j).is("(")) {
          depth++;
        } else if (tk(j).is(")")) {
          depth--;
          if (depth == 0) {
            return j + 1;
          }
        }
        j++;
      }
      return -1;
    }
    return j;
  }

  private int scanTypeArgs(int j) {
    int depth = 0;
    while (true) {
      Token t = tk(j);
      if (t.is("<")) {
        depth++;
      } else if (t.is(">")) {
        depth--;
        if (depth == 0) {
          return j + 1;
        }
      } else if (t.kind == Token.IDENT || t.is(",") || t.is(".") || t.is("?") || t.is("&") || t.is("[")
          || t.is("]") || t.is("extends") || t.is("super") || primitiveAt(j)) {
        // allowed
      } else if (t.is("@")) {
        int k = skipAnnotation(j);
        if (k < 0) {
          return -1;
        }
        j = k - 1;
      } else {
        return -1;
      }
      j++;
    }
  }

  private int scanType(int j) {
    while (tk(j).is("@")) {
      j = skipAnnotation(j);
      if (j < 0) {
        return -1;
      }
    }
    if (primitiveAt(j)) {
      j++;
    } else if (tk(j).kind == Token.IDENT) {
      j++;
      while (true) {
        if (tk(j).is("<")) {
          j = scanTypeArgs(j);
          if (j < 0) {
            return -1;
          }
        }
        if (tk(j).is(".") && (tk(j + 1).kind == Token.IDENT || tk(j + 1).is("@"))) {
          j++;
          while (tk(j).is("@")) {
            j = skipAnnotation(j);
            if (j < 0) {
              return -1;
            }
          }
          j++;
        } else {
          break;
        }
      }
    } else {
      return -1;
    }
    while (true) {
      int k = j;
      while (tk(k).is("@")) {
        k = skipAnnotation(k);
        if (k < 0) {
          return -1;
        }
      }
      if (tk(k).is("[") && tk(k + 1).is("]")) {
        j = k + 2;
      } else {
        break;
      }
    }
    return j;
  }

  private boolean looksLikeLocalVar() {
    Token t = tok();
    if (t.kind == Token.IDENT || primitiveAt(i) || t.is("@")) {
      int j = scanType(i);
      if (j < 0) {
        return false;
      }
      Token n = tk(j);
      if (primitiveAt(i) && (n.is(".") || n.is("::"))) {
        return false;
      }
      return n.kind == Token.IDENT || n.is("_");
    }
    return false;
  }

  /* ---------- statements ---------- */

  public N.Block block() {
    int s = start();
    expect("{");
    N.Block b = new N.Block();
    List<com.sun.source.tree.StatementTree> stats = new ArrayList<com.sun.source.tree.StatementTree>();
    while (!at("}") && !atEof()) {
      int before = i;
      try {
        blockStatement(stats);
        if (i == before) {
          next();
        }
      } catch (Fail f) {
        if (i == before) {
          next();
        }
        skipToStatementEnd();
      }
    }
    if (atEof()) {
      report(lastEnd, "compiler.err.premature.eof", "reached end of file while parsing");
    }
    expect("}");
    b.statements = stats;
    return fin(b, s);
  }

  private void blockStatement(List<com.sun.source.tree.StatementTree> out) {
    int s = start();
    Token t = tok();
    if (t.is("@") || t.is("final") || t.is("abstract") || t.is("static") || t.is("strictfp") || atIdent("sealed")
        || (atIdent("non") && peekIs(1, "-"))) {
      N.Modifiers mods = parseModifiers();
      if (atTypeDeclStart()) {
        out.add(parseTypeDecl(mods));
        return;
      }
      N.Base type = parseType();
      out.addAll(localVars(mods, type, s));
      return;
    }
    if (t.is("class") || t.is("interface") || t.is("enum") || atTypeDeclStart()) {
      out.add(parseTypeDecl(null));
      return;
    }
    if (looksLikeLocalVar() && !(atIdent("yield") && !peek(1).is("=") && false)) {
      if (!(atIdent("yield") && !(peek(1).kind == Token.IDENT && !peek(1).is("_") && false))) {
        N.Modifiers mods = emptyMods();
        N.Base type = parseType();
        out.addAll(localVars(mods, type, s));
        return;
      }
    }
    out.add(statement());
  }

  private N.Base varOrType(N.Base type) {
    if (type instanceof N.Identifier && ((N.Identifier) type).name.toString().equals("var")) {
      return null;
    }
    return type;
  }

  private List<com.sun.source.tree.StatementTree> localVars(N.Modifiers mods, N.Base type, int s) {
    int nameStart = start();
    NameImpl nm = identName();
    List<Tree> vars = variableDeclarators(mods, varOrType(type), nm, nameStart, s, false);
    expect(";");
    List<com.sun.source.tree.StatementTree> out = new ArrayList<com.sun.source.tree.StatementTree>();
    for (Tree v : vars) {
      out.add((com.sun.source.tree.StatementTree) v);
    }
    ((N.Base) vars.get(vars.size() - 1)).end = lastEnd;
    return out;
  }

  private com.sun.source.tree.StatementTree statement() {
    return (com.sun.source.tree.StatementTree) parseStatement();
  }

  private N.Base parseStatement() {
    int s = start();
    Token t = tok();
    if (t.kind == Token.OP) {
      if (t.is("{")) {
        return block();
      }
      if (t.is(";")) {
        next();
        return fin(new N.EmptyStatement(), s);
      }
    }
    if (t.kind == Token.KEYWORD) {
      switch (t.text) {
        case "if":
          return ifStatement();
        case "for":
          return forStatement();
        case "while": {
          next();
          N.WhileLoop w = new N.WhileLoop();
          w.condition = parExpression();
          w.statement = statement();
          return fin(w, s);
        }
        case "do": {
          next();
          N.DoWhileLoop d = new N.DoWhileLoop();
          d.statement = statement();
          expect("while");
          d.condition = parExpression();
          expect(";");
          return fin(d, s);
        }
        case "try":
          return tryStatement();
        case "switch": {
          int save = i;
          N.Base sw = switchConstruct(false);
          if (at(".") || at("[") && false) {
            i = save;
            return exprStatement();
          }
          return sw;
        }
        case "synchronized": {
          next();
          N.Synchronized sy = new N.Synchronized();
          sy.expression = parExpression();
          sy.block = block();
          return fin(sy, s);
        }
        case "return": {
          next();
          N.Return r = new N.Return();
          if (!at(";")) {
            r.expression = parseExpression();
          }
          expect(";");
          return fin(r, s);
        }
        case "throw": {
          next();
          N.Throw th = new N.Throw();
          th.expression = parseExpression();
          expect(";");
          return fin(th, s);
        }
        case "break": {
          next();
          N.Break b = new N.Break();
          if (atIdent()) {
            b.label = identName();
          }
          expect(";");
          return fin(b, s);
        }
        case "continue": {
          next();
          N.Continue c = new N.Continue();
          if (atIdent()) {
            c.label = identName();
          }
          expect(";");
          return fin(c, s);
        }
        case "assert": {
          next();
          N.Assert a = new N.Assert();
          a.condition = parseExpression();
          if (accept(":")) {
            a.detail = parseExpression();
          }
          expect(";");
          return fin(a, s);
        }
        case "else":
          throw error(t.start, "compiler.err.else.without.if", "illegal start of statement");
        case "catch":
          throw error(t.start, "compiler.err.catch.without.try", "'catch' without 'try'");
        case "finally":
          throw error(t.start, "compiler.err.finally.without.try", "'finally' without 'try'");
        case "case":
        case "default":
          throw error(t.start, "compiler.err.orphaned", "orphaned " + t.text, t.text);
        default:
          break;
      }
    }
    if (t.kind == Token.IDENT) {
      if (peekIs(1, ":") ) {
        N.LabeledStatement l = new N.LabeledStatement();
        l.label = identName();
        next();
        l.statement = statement();
        return fin(l, s);
      }
      if (t.text.equals("yield") && isYieldStatement()) {
        next();
        N.Yield y = new N.Yield();
        y.value = parseExpression();
        expect(";");
        return fin(y, s);
      }
    }
    return exprStatement();
  }

  private boolean isYieldStatement() {
    Token n = peek(1);
    if (n.kind == Token.OP) {
      switch (n.text) {
        case "=":
        case ".":
        case "[":
        case "++":
        case "--":
        case ";":
        case "+=":
        case "-=":
        case "*=":
        case "/=":
        case ")":
        case ",":
        case "->":
        case ":":
        case "::":
          return false;
        case "(":
          return true;
        default:
          return true;
      }
    }
    return true;
  }

  private N.Base exprStatement() {
    int s = start();
    com.sun.source.tree.ExpressionTree e = parseExpression();
    N.ExpressionStatement es = new N.ExpressionStatement();
    es.expression = e;
    switch (e.getKind()) {
      case ASSIGNMENT:
      case PREFIX_INCREMENT:
      case PREFIX_DECREMENT:
      case POSTFIX_INCREMENT:
      case POSTFIX_DECREMENT:
      case METHOD_INVOCATION:
      case NEW_CLASS:
      case ERRONEOUS:
        break;
      default:
        if (e.getKind().asInterface() == com.sun.source.tree.CompoundAssignmentTree.class) {
          break;
        }
        diags.add(new Diag(((N.Base) e).start, "compiler.err.not.stmt", "not a statement"));
        errPos = ((N.Base) e).start;
        break;
    }
    expect(";");
    return fin(es, s);
  }

  private com.sun.source.tree.ExpressionTree parExpression() {
    int s = start();
    expect("(");
    com.sun.source.tree.ExpressionTree e = parseExpression();
    expect(")");
    N.Parenthesized p = new N.Parenthesized();
    p.expression = e;
    return fin(p, s);
  }

  private N.Base ifStatement() {
    int s = start();
    expect("if");
    N.If n = new N.If();
    n.condition = parExpression();
    n.thenStatement = statement();
    if (accept("else")) {
      n.elseStatement = statement();
    }
    return fin(n, s);
  }

  private N.Base forStatement() {
    int s = start();
    expect("for");
    expect("(");
    List<com.sun.source.tree.StatementTree> init = new ArrayList<com.sun.source.tree.StatementTree>();
    if (!at(";")) {
      int is = start();
      if (looksLikeLocalVar() || at("final") || at("@")) {
        N.Modifiers mods = parseModifiers();
        N.Base type = parseType();
        int ns = start();
        NameImpl nm = identName();
        if (at(":")) {
          N.Variable v = new N.Variable();
          v.modifiers = mods;
          v.type = varOrType(type);
          v.name = nm;
          v.pos = ns;
          fin(v, is);
          next();
          N.EnhancedForLoop f = new N.EnhancedForLoop();
          f.variable = v;
          f.expression = parseExpression();
          expect(")");
          f.statement = statement();
          return fin(f, s);
        }
        List<Tree> vars = variableDeclarators(mods, varOrType(type), nm, ns, is, false);
        for (Tree v : vars) {
          init.add((com.sun.source.tree.StatementTree) v);
        }
      } else {
        do {
          int es = start();
          N.ExpressionStatement st = new N.ExpressionStatement();
          st.expression = parseExpression();
          init.add(fin(st, es));
        } while (accept(","));
      }
    }
    expect(";");
    N.ForLoop f = new N.ForLoop();
    f.initializer = init;
    if (!at(";")) {
      f.condition = parseExpression();
    }
    expect(";");
    List<com.sun.source.tree.ExpressionStatementTree> upd = new ArrayList<com.sun.source.tree.ExpressionStatementTree>();
    if (!at(")")) {
      do {
        int es = start();
        N.ExpressionStatement st = new N.ExpressionStatement();
        st.expression = parseExpression();
        upd.add(fin(st, es));
      } while (accept(","));
    }
    f.update = upd;
    expect(")");
    f.statement = statement();
    return fin(f, s);
  }

  private N.Base tryStatement() {
    int s = start();
    expect("try");
    N.Try t = new N.Try();
    List<Tree> resources = new ArrayList<Tree>();
    if (accept("(")) {
      while (!at(")") && !atEof()) {
        int rs = start();
        if (looksLikeLocalVar() || at("final") || at("@")) {
          N.Modifiers mods = parseModifiers();
          N.Base type = parseType();
          int ns = start();
          NameImpl nm = identName();
          N.Variable v = new N.Variable();
          v.modifiers = mods;
          v.type = varOrType(type);
          v.name = nm;
          v.pos = ns;
          expect("=");
          v.initializer = parseExpression();
          resources.add(fin(v, rs));
        } else {
          resources.add(parseExpression());
        }
        if (!accept(";")) {
          break;
        }
        ((N.Base) resources.get(resources.size() - 1)).end = lastEnd;
      }
      expect(")");
    }
    t.resources = resources;
    t.block = block();
    List<com.sun.source.tree.CatchTree> catches = new ArrayList<com.sun.source.tree.CatchTree>();
    while (at("catch")) {
      int cs = start();
      next();
      expect("(");
      N.Catch c = new N.Catch();
      int ps = start();
      N.Modifiers mods = parseModifiers();
      List<Tree> alts = new ArrayList<Tree>();
      int ts = start();
      alts.add(parseType());
      while (accept("|")) {
        alts.add(parseType());
      }
      N.Base type;
      if (alts.size() > 1) {
        N.UnionType u = new N.UnionType();
        u.typeAlternatives = alts;
        type = fin(u, ts);
      } else {
        type = (N.Base) alts.get(0);
      }
      N.Variable v = new N.Variable();
      v.modifiers = mods;
      v.type = type;
      v.name = identName();
      c.parameter = fin(v, ps);
      expect(")");
      c.block = block();
      catches.add(fin(c, cs));
    }
    t.catches = catches;
    if (accept("finally")) {
      t.finallyBlock = block();
    }
    if (catches.isEmpty() && t.finallyBlock == null && resources.isEmpty()) {
      diags.add(new Diag(s, "compiler.err.try.without.catch.finally.or.resource.decls",
          "'try' without 'catch', 'finally' or resource declarations"));
    }
    return fin(t, s);
  }

  private N.Base switchConstruct(boolean expr) {
    int s = start();
    expect("switch");
    com.sun.source.tree.ExpressionTree sel = parExpression();
    expect("{");
    List<com.sun.source.tree.CaseTree> cases = new ArrayList<com.sun.source.tree.CaseTree>();
    while (!at("}") && !atEof()) {
      int before = i;
      try {
        cases.add(parseCase(expr));
        if (i == before) {
          next();
        }
      } catch (Fail f) {
        if (i == before) {
          next();
        }
        skipToStatementEnd();
      }
    }
    expect("}");
    if (expr) {
      N.SwitchExpression se = new N.SwitchExpression();
      se.expression = sel;
      se.cases = cases;
      return fin(se, s);
    }
    N.Switch sw = new N.Switch();
    sw.expression = sel;
    sw.cases = cases;
    return fin(sw, s);
  }

  private N.Case parseCase(boolean expr) {
    int s = start();
    N.Case c = new N.Case();
    List<com.sun.source.tree.CaseLabelTree> labels = new ArrayList<com.sun.source.tree.CaseLabelTree>();
    List<com.sun.source.tree.ExpressionTree> exprs = new ArrayList<com.sun.source.tree.ExpressionTree>();
    if (at("default")) {
      int ds = start();
      next();
      labels.add(fin(new N.DefaultCaseLabel(), ds));
    } else if (at("case")) {
      next();
      boolean save = noLambda;
      noLambda = true;
      try {
        do {
          labels.add(caseLabel(exprs));
        } while (accept(","));
        if (atIdent("when")) {
          next();
          c.guard = parseExpression();
        }
      } finally {
        noLambda = save;
      }
    } else {
      throw error(tok().start, "compiler.err.expected3", "case, default, or '}' expected");
    }
    c.labels = labels;
    c.expressions = exprs;
    c.expression = exprs.isEmpty() ? null : exprs.get(0);
    if (accept("->")) {
      c.caseKind = CaseTree.CaseKind.RULE;
      if (at("{")) {
        c.body = block();
      } else if (at("throw")) {
        c.body = statement();
      } else if (expr) {
        c.body = parseExpression();
        expect(";");
      } else {
        c.body = exprStatement();
      }
      c.statements = null;
    } else {
      if (!accept(":")) {
        throw expected(":");
      }
      c.caseKind = CaseTree.CaseKind.STATEMENT;
      List<com.sun.source.tree.StatementTree> stats = new ArrayList<com.sun.source.tree.StatementTree>();
      while (!at("case") && !at("default") && !at("}") && !atEof()) {
        int before = i;
        try {
          blockStatement(stats);
          if (i == before) {
            next();
          }
        } catch (Fail f) {
          if (i == before) {
            next();
          }
          skipToStatementEnd();
        }
      }
      c.statements = stats;
    }
    return fin(c, s);
  }

  private com.sun.source.tree.CaseLabelTree caseLabel(List<com.sun.source.tree.ExpressionTree> exprs) {
    int s = start();
    if (at("default")) {
      next();
      return fin(new N.DefaultCaseLabel(), s);
    }
    if (looksLikePattern()) {
      N.PatternCaseLabel l = new N.PatternCaseLabel();
      l.pattern = (com.sun.source.tree.PatternTree) parsePattern();
      return fin(l, s);
    }
    N.ConstantCaseLabel l = new N.ConstantCaseLabel();
    l.constantExpression = parseTernary();
    exprs.add(l.constantExpression);
    return fin(l, s);
  }

  private boolean looksLikePattern() {
    int j = i;
    while (tk(j).is("final") || tk(j).is("@")) {
      if (tk(j).is("final")) {
        j++;
      } else {
        j = skipAnnotation(j);
        if (j < 0) {
          return false;
        }
      }
    }
    if (tk(j).is("_") && !(tk(j + 1).is("."))) {
      return true;
    }
    int e = scanType(j);
    if (e < 0) {
      return false;
    }
    Token n = tk(e);
    return n.kind == Token.IDENT && !n.text.equals("when") || n.is("_") || n.is("(")
        || (n.kind == Token.IDENT && n.text.equals("when") && (tk(e + 1).is("->") || tk(e + 1).is(":")
        || tk(e + 1).is(",")));
  }

  private N.Base parsePattern() {
    int s = start();
    N.Modifiers mods = parseModifiers();
    if (at("_") && !peekIs(1, "(")) {
      next();
      N.AnyPattern ap = new N.AnyPattern();
      ap.start = tok().start;
      ap.pos = ap.start;
      ap.end = lastEnd;
      return ap;
    }
    N.Base type = parseType();
    if (at("(")) {
      next();
      N.DeconstructionPattern dp = new N.DeconstructionPattern();
      dp.deconstructor = (com.sun.source.tree.ExpressionTree) type;
      List<com.sun.source.tree.PatternTree> nested = new ArrayList<com.sun.source.tree.PatternTree>();
      if (!at(")")) {
        do {
          nested.add((com.sun.source.tree.PatternTree) parsePattern());
        } while (accept(","));
      }
      expect(")");
      dp.nestedPatterns = nested;
      return fin(dp, s);
    }
    N.BindingPattern bp = new N.BindingPattern();
    N.Variable v = new N.Variable();
    v.modifiers = mods;
    v.type = varOrType(type);
    int ns = start();
    v.name = identName();
    v.pos = ns;
    bp.variable = fin(v, s);
    return fin(bp, s);
  }

  /* ---------- expressions ---------- */

  public com.sun.source.tree.ExpressionTree parseExpression() {
    return assignment();
  }

  private int compoundLen() {
    Token t = tok();
    if (t.kind == Token.OP) {
      switch (t.text) {
        case "+=":
        case "-=":
        case "*=":
        case "/=":
        case "&=":
        case "|=":
        case "^=":
        case "%=":
        case "<<=":
          return 1;
        case ">":
          if (peekIs(1, ">") && peek(1).start == t.end) {
            if (peekIs(2, "=") && peek(2).start == peek(1).end) {
              return 3;
            }
            if (peekIs(2, ">") && peek(2).start == peek(1).end && peekIs(3, "=") && peek(3).start == peek(2).end) {
              return 4;
            }
          }
          return 0;
        default:
          return 0;
      }
    }
    return 0;
  }

  private Tree.Kind compoundKind(Token t, int len) {
    if (len == 3) {
      return Tree.Kind.RIGHT_SHIFT_ASSIGNMENT;
    }
    if (len == 4) {
      return Tree.Kind.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT;
    }
    switch (t.text) {
      case "+=":
        return Tree.Kind.PLUS_ASSIGNMENT;
      case "-=":
        return Tree.Kind.MINUS_ASSIGNMENT;
      case "*=":
        return Tree.Kind.MULTIPLY_ASSIGNMENT;
      case "/=":
        return Tree.Kind.DIVIDE_ASSIGNMENT;
      case "&=":
        return Tree.Kind.AND_ASSIGNMENT;
      case "|=":
        return Tree.Kind.OR_ASSIGNMENT;
      case "^=":
        return Tree.Kind.XOR_ASSIGNMENT;
      case "%=":
        return Tree.Kind.REMAINDER_ASSIGNMENT;
      default:
        return Tree.Kind.LEFT_SHIFT_ASSIGNMENT;
    }
  }

  private com.sun.source.tree.ExpressionTree assignment() {
    int s = start();
    com.sun.source.tree.ExpressionTree lhs = parseTernary();
    if (at("=")) {
      int pos = tok().start;
      next();
      N.Assignment a = new N.Assignment();
      a.variable = lhs;
      a.expression = assignment();
      a.pos = pos;
      return fin(a, s);
    }
    int len = compoundLen();
    if (len > 0) {
      Token op = tok();
      int pos = op.start;
      for (int k = 0; k < len; k++) {
        next();
      }
      N.CompoundAssignment ca = new N.CompoundAssignment();
      ca.kind = compoundKind(op, len);
      ca.variable = lhs;
      ca.expression = assignment();
      ca.pos = pos;
      return fin(ca, s);
    }
    return lhs;
  }

  private com.sun.source.tree.ExpressionTree parseTernary() {
    int s = start();
    com.sun.source.tree.ExpressionTree c = binary(1);
    if (c.getKind() == Tree.Kind.PLUS) {
      c = foldStrings(c);
    }
    if (at("?")) {
      int pos = tok().start;
      next();
      N.ConditionalExpression ce = new N.ConditionalExpression();
      ce.condition = c;
      boolean save = noLambda;
      noLambda = false;
      ce.trueExpression = assignment();
      noLambda = save;
      expect(":");
      ce.falseExpression = parseTernary();
      ce.pos = pos;
      return fin(ce, s);
    }
    return c;
  }

  private int opPrec;
  private Tree.Kind opKind;
  private int opLen;

  private boolean peekBinary() {
    Token t = tok();
    if (t.kind == Token.KEYWORD && t.text.equals("instanceof")) {
      opPrec = 7;
      opLen = 1;
      return true;
    }
    if (t.kind != Token.OP) {
      return false;
    }
    opLen = 1;
    switch (t.text) {
      case "||":
        opPrec = 1;
        opKind = Tree.Kind.CONDITIONAL_OR;
        return true;
      case "&&":
        opPrec = 2;
        opKind = Tree.Kind.CONDITIONAL_AND;
        return true;
      case "|":
        opPrec = 3;
        opKind = Tree.Kind.OR;
        return true;
      case "^":
        opPrec = 4;
        opKind = Tree.Kind.XOR;
        return true;
      case "&":
        opPrec = 5;
        opKind = Tree.Kind.AND;
        return true;
      case "==":
        opPrec = 6;
        opKind = Tree.Kind.EQUAL_TO;
        return true;
      case "!=":
        opPrec = 6;
        opKind = Tree.Kind.NOT_EQUAL_TO;
        return true;
      case "<":
        opPrec = 7;
        opKind = Tree.Kind.LESS_THAN;
        return true;
      case "<=":
        opPrec = 7;
        opKind = Tree.Kind.LESS_THAN_EQUAL;
        return true;
      case ">": {
        boolean g2 = peekIs(1, ">") && peek(1).start == t.end;
        boolean g3 = g2 && peekIs(2, ">") && peek(2).start == peek(1).end;
        if (g3) {
          if (peekIs(3, "=") && peek(3).start == peek(2).end) {
            return false;
          }
          opPrec = 8;
          opLen = 3;
          opKind = Tree.Kind.UNSIGNED_RIGHT_SHIFT;
          return true;
        }
        if (g2) {
          if (peekIs(2, "=") && peek(2).start == peek(1).end) {
            return false;
          }
          opPrec = 8;
          opLen = 2;
          opKind = Tree.Kind.RIGHT_SHIFT;
          return true;
        }
        opPrec = 7;
        if (peekIs(1, "=") && peek(1).start == t.end) {
          opLen = 2;
          opKind = Tree.Kind.GREATER_THAN_EQUAL;
        } else {
          opKind = Tree.Kind.GREATER_THAN;
        }
        return true;
      }
      case "<<":
        opPrec = 8;
        opKind = Tree.Kind.LEFT_SHIFT;
        return true;
      case "+":
        opPrec = 9;
        opKind = Tree.Kind.PLUS;
        return true;
      case "-":
        opPrec = 9;
        opKind = Tree.Kind.MINUS;
        return true;
      case "*":
        opPrec = 10;
        opKind = Tree.Kind.MULTIPLY;
        return true;
      case "/":
        opPrec = 10;
        opKind = Tree.Kind.DIVIDE;
        return true;
      case "%":
        opPrec = 10;
        opKind = Tree.Kind.REMAINDER;
        return true;
      default:
        return false;
    }
  }

  private com.sun.source.tree.ExpressionTree binary(int minPrec) {
    int s = start();
    com.sun.source.tree.ExpressionTree left = unary();
    while (peekBinary() && opPrec >= minPrec) {
      Tree.Kind kind = opKind;
      int prec = opPrec;
      int len = opLen;
      int pos = tok().start;
      if (tok().is("instanceof")) {
        next();
        N.InstanceOf io = new N.InstanceOf();
        io.expression = left;
        int ps = start();
        boolean pattern = false;
        if (at("final") || at("@")) {
          pattern = true;
        } else {
          int e = scanType(i);
          if (e >= 0 && (tk(e).kind == Token.IDENT || tk(e).is("_") || tk(e).is("("))
              && !(tk(e).kind == Token.IDENT && isBinaryWord(tk(e)))) {
            pattern = true;
          }
        }
        if (pattern) {
          N.Base p = parsePattern();
          io.pattern = (com.sun.source.tree.PatternTree) p;
          if (p instanceof N.BindingPattern) {
            io.type = ((N.BindingPattern) p).variable.getType();
          } else if (p instanceof N.DeconstructionPattern) {
            io.type = ((N.DeconstructionPattern) p).deconstructor;
          }
        } else {
          io.type = parseType();
        }
        io.pos = pos;
        left = fin(io, s);
        continue;
      }
      for (int k = 0; k < len; k++) {
        next();
      }
      com.sun.source.tree.ExpressionTree right = binary(prec + 1);
      N.Binary b = new N.Binary();
      b.kind = kind;
      b.leftOperand = left;
      b.rightOperand = right;
      b.pos = pos;
      left = fin(b, s);
    }
    return left;
  }

  private static boolean isStringLit(com.sun.source.tree.ExpressionTree e) {
    return e instanceof N.Literal && e.getKind() == Tree.Kind.STRING_LITERAL;
  }

  private com.sun.source.tree.ExpressionTree foldStrings(com.sun.source.tree.ExpressionTree tree) {
    java.util.LinkedList<com.sun.source.tree.ExpressionTree> ops = new java.util.LinkedList<com.sun.source.tree.ExpressionTree>();
    java.util.LinkedList<N.Literal> lits = new java.util.LinkedList<N.Literal>();
    boolean needs = false;
    com.sun.source.tree.ExpressionTree curr = tree;
    while (true) {
      boolean last;
      com.sun.source.tree.ExpressionTree operand;
      if (curr.getKind() == Tree.Kind.PLUS) {
        operand = ((N.Binary) curr).rightOperand;
        last = false;
      } else {
        operand = curr;
        last = true;
      }
      if (isStringLit(operand)) {
        lits.addFirst((N.Literal) operand);
        if (last) {
          needs |= mergeLits(lits, ops);
        }
      } else {
        needs |= mergeLits(lits, ops);
        lits.clear();
        ops.addFirst(operand);
      }
      if (last) {
        break;
      }
      curr = ((N.Binary) curr).leftOperand;
    }
    if (!needs) {
      return tree;
    }
    com.sun.source.tree.ExpressionTree res = ops.removeFirst();
    for (com.sun.source.tree.ExpressionTree op : ops) {
      N.Binary b = new N.Binary();
      b.kind = Tree.Kind.PLUS;
      b.leftOperand = res;
      b.rightOperand = op;
      b.start = ((N.Base) res).start;
      b.pos = ((N.Base) op).start;
      b.end = ((N.Base) op).end;
      res = b;
    }
    return res;
  }

  private boolean mergeLits(java.util.LinkedList<N.Literal> lits, java.util.LinkedList<com.sun.source.tree.ExpressionTree> ops) {
    if (lits.isEmpty()) {
      return false;
    }
    if (lits.size() == 1) {
      ops.addFirst(lits.getFirst());
      return false;
    }
    StringBuilder sb = new StringBuilder();
    for (N.Literal l : lits) {
      sb.append((String) l.value);
    }
    N.Literal t = new N.Literal();
    t.kind = Tree.Kind.STRING_LITERAL;
    t.value = sb.toString();
    t.start = lits.getFirst().start;
    t.pos = t.start;
    t.end = lits.getLast().end;
    ops.addFirst(t);
    return true;
  }

  private boolean isBinaryWord(Token t) {
    return false;
  }

  private boolean isLiteralStart(Token t) {
    switch (t.kind) {
      case Token.INTLIT:
      case Token.LONGLIT:
      case Token.FLOATLIT:
      case Token.DOUBLELIT:
      case Token.CHARLIT:
      case Token.STRINGLIT:
        return true;
      default:
        return t.is("true") || t.is("false") || t.is("null");
    }
  }

  private com.sun.source.tree.ExpressionTree unary() {
    int s = start();
    Token t = tok();
    if (t.kind == Token.OP) {
      Tree.Kind k = null;
      switch (t.text) {
        case "++":
          k = Tree.Kind.PREFIX_INCREMENT;
          break;
        case "--":
          k = Tree.Kind.PREFIX_DECREMENT;
          break;
        case "+":
          k = Tree.Kind.UNARY_PLUS;
          break;
        case "-":
          k = Tree.Kind.UNARY_MINUS;
          break;
        case "!":
          k = Tree.Kind.LOGICAL_COMPLEMENT;
          break;
        case "~":
          k = Tree.Kind.BITWISE_COMPLEMENT;
          break;
        default:
          break;
      }
      if (k != null) {
        next();
        if (k == Tree.Kind.UNARY_MINUS && (tok().kind == Token.INTLIT || tok().kind == Token.LONGLIT)
            && tok().text.length() > 0 && Character.isDigit(tok().text.charAt(0))
            && tok().text.charAt(0) != '0') {
          Token lt = next();
          N.Literal lit = new N.Literal();
          if (lt.kind == Token.INTLIT) {
            lit.kind = Tree.Kind.INT_LITERAL;
            lit.value = Integer.valueOf(-((Integer) lt.value));
          } else {
            lit.kind = Tree.Kind.LONG_LITERAL;
            lit.value = Long.valueOf(-((Long) lt.value));
          }
          return postfix(fin(lit, s));
        }
        N.Unary u = new N.Unary();
        u.kind = k;
        u.expression = unary();
        return fin(u, s);
      }
    }
    if (t.is("(")) {
      if (!noLambda && lambdaAhead()) {
        return lambda();
      }
      if (castAhead()) {
        next();
        N.TypeCast tc = new N.TypeCast();
        int ts = start();
        N.Base type = parseType();
        if (at("&")) {
          List<Tree> bounds = new ArrayList<Tree>();
          bounds.add(type);
          while (accept("&")) {
            bounds.add(parseType());
          }
          N.IntersectionType it = new N.IntersectionType();
          it.bounds = bounds;
          type = fin(it, s);
        }
        expect(")");
        tc.type = type;
        tc.expression = unary();
        return fin(tc, s);
      }
    }
    return postfix(primary());
  }

  private boolean castAhead() {
    int j = i + 1;
    if (primitiveAt(j)) {
      int e = scanType(j);
      return e >= 0 && tk(e).is(")");
    }
    if (tk(j).kind != Token.IDENT && !tk(j).is("@")) {
      return false;
    }
    int e = scanType(j);
    if (e < 0) {
      return false;
    }
    while (tk(e).is("&")) {
      int e2 = scanType(e + 1);
      if (e2 < 0) {
        return false;
      }
      e = e2;
    }
    if (!tk(e).is(")")) {
      return false;
    }
    Token n = tk(e + 1);
    switch (n.kind) {
      case Token.IDENT:
      case Token.INTLIT:
      case Token.LONGLIT:
      case Token.FLOATLIT:
      case Token.DOUBLELIT:
      case Token.CHARLIT:
      case Token.STRINGLIT:
        return true;
      case Token.KEYWORD:
        switch (n.text) {
          case "this":
          case "super":
          case "new":
          case "true":
          case "false":
          case "null":
          case "switch":
          case "int":
          case "long":
          case "short":
          case "byte":
          case "char":
          case "boolean":
          case "float":
          case "double":
          case "void":
          case "_":
            return true;
          default:
            return false;
        }
      case Token.OP:
        return n.is("(") || n.is("!") || n.is("~") || n.is("@");
      default:
        return false;
    }
  }

  private boolean lambdaAhead() {
    if (at("(")) {
      int depth = 0;
      int j = i;
      while (tk(j).kind != Token.EOF) {
        if (tk(j).is("(")) {
          depth++;
        } else if (tk(j).is(")")) {
          depth--;
          if (depth == 0) {
            return tk(j + 1).is("->");
          }
        }
        j++;
      }
      return false;
    }
    return (tok().kind == Token.IDENT || at("_")) && peekIs(1, "->");
  }

  private com.sun.source.tree.ExpressionTree lambda() {
    int s = start();
    N.LambdaExpression le = new N.LambdaExpression();
    List<com.sun.source.tree.VariableTree> params = new ArrayList<com.sun.source.tree.VariableTree>();
    if (atIdent() || at("_")) {
      int ps = start();
      N.Variable v = new N.Variable();
      v.modifiers = emptyMods();
      v.name = identName();
      params.add(fin(v, ps));
    } else {
      expect("(");
      if (!at(")")) {
        boolean implicit = (atIdent() || at("_")) && (peekIs(1, ",") || peekIs(1, ")"));
        do {
          int ps = start();
          N.Variable v = new N.Variable();
          if (implicit) {
            v.modifiers = emptyMods();
            v.name = identName();
          } else {
            v.modifiers = parseModifiers();
            N.Base t = parseType();
            if (at("...")) {
              next();
              N.ArrayType a = new N.ArrayType();
              a.type = t;
              t = fin(a, t.start);
            }
            v.type = t;
            v.name = identName();
          }
          params.add(fin(v, ps));
        } while (accept(","));
      }
      expect(")");
    }
    expect("->");
    le.parameters = params;
    boolean save = noLambda;
    noLambda = false;
    if (at("{")) {
      le.bodyKind = LambdaExpressionTree.BodyKind.STATEMENT;
      le.body = block();
    } else {
      le.bodyKind = LambdaExpressionTree.BodyKind.EXPRESSION;
      le.body = parseExpression();
    }
    noLambda = save;
    return fin(le, s);
  }

  private List<com.sun.source.tree.ExpressionTree> arguments() {
    expect("(");
    List<com.sun.source.tree.ExpressionTree> args = new ArrayList<com.sun.source.tree.ExpressionTree>();
    boolean save = noLambda;
    noLambda = false;
    if (!at(")")) {
      do {
        args.add(parseExpression());
      } while (accept(","));
    }
    noLambda = save;
    expect(")");
    return args;
  }

  private N.Literal literal(Tree.Kind kind, Object value, int s) {
    N.Literal l = new N.Literal();
    l.kind = kind;
    l.value = value;
    return fin(l, s);
  }

  private com.sun.source.tree.ExpressionTree primary() {
    int s = start();
    Token t = tok();
    switch (t.kind) {
      case Token.INTLIT:
        next();
        if (t.value instanceof Integer && (Integer) t.value == Integer.MIN_VALUE && t.text.equals("2147483648")) {
          throw error(t.start, "compiler.err.int.number.too.large", "integer number too large: 2147483648",
              "2147483648");
        }
        return literal(Tree.Kind.INT_LITERAL, t.value, s);
      case Token.LONGLIT:
        next();
        if (t.value instanceof Long && (Long) t.value == Long.MIN_VALUE && t.text.startsWith("9223372036854775808")) {
          throw error(t.start, "compiler.err.int.number.too.large", "integer number too large", t.text);
        }
        return literal(Tree.Kind.LONG_LITERAL, t.value, s);
      case Token.FLOATLIT:
        next();
        return literal(Tree.Kind.FLOAT_LITERAL, t.value, s);
      case Token.DOUBLELIT:
        next();
        return literal(Tree.Kind.DOUBLE_LITERAL, t.value, s);
      case Token.CHARLIT:
        next();
        return literal(Tree.Kind.CHAR_LITERAL, t.value, s);
      case Token.STRINGLIT:
        next();
        return literal(Tree.Kind.STRING_LITERAL, t.value, s);
      case Token.IDENT: {
        if (!noLambda && peekIs(1, "->")) {
          return lambda();
        }
        N.Identifier id = parseIdentifier();
        return id;
      }
      case Token.KEYWORD:
        return keywordPrimary(t, s);
      case Token.OP:
        if (t.is("(")) {
          if (!noLambda && lambdaAhead()) {
            return lambda();
          }
          next();
          boolean save = noLambda;
          noLambda = false;
          N.Parenthesized p = new N.Parenthesized();
          p.expression = parseExpression();
          noLambda = save;
          expect(")");
          return fin(p, s);
        }
        if (t.is("@")) {
          List<com.sun.source.tree.AnnotationTree> anns = typeAnnotations();
          N.Base ty = parseType();
          return (com.sun.source.tree.ExpressionTree) annotated(anns, ty, s);
        }
        break;
      default:
        break;
    }
    return illegalExpr(t, s);
  }

  private com.sun.source.tree.ExpressionTree illegalExpr(Token t, int s) {
    if (atEof()) {
      report(lastEnd, "compiler.err.premature.eof", "reached end of file while parsing");
    } else {
      report(t.start, "compiler.err.illegal.start.of.expr", "illegal start of expression");
    }
    N.Erroneous er = new N.Erroneous();
    er.errorTrees = new ArrayList<Tree>();
    er.start = s;
    er.end = s;
    er.pos = s;
    return er;
  }

  private com.sun.source.tree.ExpressionTree keywordPrimary(Token t, int s) {
    switch (t.text) {
      case "true":
        next();
        return literal(Tree.Kind.BOOLEAN_LITERAL, Boolean.TRUE, s);
      case "false":
        next();
        return literal(Tree.Kind.BOOLEAN_LITERAL, Boolean.FALSE, s);
      case "null":
        next();
        return literal(Tree.Kind.NULL_LITERAL, null, s);
      case "this": {
        next();
        N.Identifier id = identOf(name("this"));
        fin(id, s);
        if (at("(")) {
          N.MethodInvocation mi = new N.MethodInvocation();
          mi.methodSelect = id;
          mi.typeArguments = new ArrayList<Tree>();
          mi.arguments = arguments();
          return fin(mi, s);
        }
        return id;
      }
      case "super": {
        next();
        N.Identifier id = identOf(name("super"));
        fin(id, s);
        if (at("(")) {
          N.MethodInvocation mi = new N.MethodInvocation();
          mi.methodSelect = id;
          mi.typeArguments = new ArrayList<Tree>();
          mi.arguments = arguments();
          return fin(mi, s);
        }
        if (!at(".") && !at("::")) {
          throw expected(".");
        }
        return id;
      }
      case "new":
        return creator(null, s);
      case "switch":
        return (com.sun.source.tree.ExpressionTree) switchConstruct(true);
      case "void": {
        N.PrimitiveType p = primitive();
        if (at(".") && peekIs(1, "class")) {
          return p;
        }
        throw expected("class");
      }
      case "int":
      case "long":
      case "short":
      case "byte":
      case "char":
      case "boolean":
      case "float":
      case "double": {
        N.Base ty = primitive();
        ty = bracketsOpt(ty, s);
        if (!at(".") && !at("::")) {
          throw expected("<identifier>");
        }
        return (com.sun.source.tree.ExpressionTree) ty;
      }
      case "_": {
        if (!noLambda && peekIs(1, "->")) {
          return lambda();
        }
        next();
        N.Identifier id = identOf(name("_"));
        return fin(id, s);
      }
      default:
        return illegalExpr(t, s);
    }
  }

  private com.sun.source.tree.ExpressionTree creator(com.sun.source.tree.ExpressionTree outer, int s) {
    expect("new");
    List<Tree> targs = new ArrayList<Tree>();
    if (at("<")) {
      targs = typeArgumentList();
    }
    int ts = start();
    List<com.sun.source.tree.AnnotationTree> anns = typeAnnotations();
    N.Base type;
    if (isPrimitiveStart()) {
      type = primitive();
    } else {
      type = classTypeForNew();
    }
    type = annotated(anns, type, ts);
    if (at("[") || at("@")) {
      return arrayCreator(type, s);
    }
    if (!at("(")) {
      throw expected("(");
    }
    N.NewClass nc = new N.NewClass();
    nc.enclosingExpression = outer;
    nc.typeArguments = targs;
    nc.identifier = (com.sun.source.tree.ExpressionTree) type;
    nc.arguments = arguments();
    if (at("{")) {
      N.Class body = newClass(emptyMods(), Tree.Kind.CLASS);
      body.simpleName = name("");
      int bs = start();
      String saveName = className;
      className = "";
      body.members = classBody(false, false);
      className = saveName;
      nc.classBody = fin(body, bs);
    }
    return fin(nc, s);
  }

  private N.Base classTypeForNew() {
    int s = start();
    N.Base t = parseIdentifier();
    if (at("<")) {
      if (peekIs(1, ">")) {
        next();
        next();
        N.ParameterizedType pt = new N.ParameterizedType();
        pt.type = t;
        pt.typeArguments = new ArrayList<Tree>();
        t = fin(pt, s);
      } else {
        t = typeArgs(t, s);
      }
    }
    while (at(".") && peek(1).kind == Token.IDENT) {
      next();
      N.MemberSelect ms = new N.MemberSelect();
      ms.expression = (com.sun.source.tree.ExpressionTree) t;
      ms.identifier = identName();
      t = fin(ms, s);
      if (at("<")) {
        if (peekIs(1, ">")) {
          next();
          next();
          N.ParameterizedType pt = new N.ParameterizedType();
          pt.type = t;
          pt.typeArguments = new ArrayList<Tree>();
          t = fin(pt, s);
        } else {
          t = typeArgs(t, s);
        }
      }
    }
    return t;
  }

  private com.sun.source.tree.ExpressionTree arrayCreator(N.Base elemType, int s) {
    N.NewArray na = new N.NewArray();
    na.annotations = new ArrayList<com.sun.source.tree.AnnotationTree>();
    List<java.util.List<? extends com.sun.source.tree.AnnotationTree>> dimAnns =
        new ArrayList<java.util.List<? extends com.sun.source.tree.AnnotationTree>>();
    List<com.sun.source.tree.ExpressionTree> dims = new ArrayList<com.sun.source.tree.ExpressionTree>();
    int extra = 0;
    while (at("[") || at("@")) {
      List<com.sun.source.tree.AnnotationTree> a = typeAnnotations();
      if (!at("[")) {
        break;
      }
      next();
      if (at("]")) {
        next();
        extra++;
      } else {
        if (extra > 0) {
          throw expected("]");
        }
        dims.add(parseExpression());
        expect("]");
      }
      dimAnns.add(a == null ? new ArrayList<com.sun.source.tree.AnnotationTree>() : a);
    }
    N.Base t = elemType;
    for (int k = 0; k < extra; k++) {
      if (k == extra - 1 && dims.isEmpty()) {
        break;
      }
      N.ArrayType at2 = new N.ArrayType();
      at2.type = t;
      t = fin(at2, elemType.start);
    }
    na.type = t;
    na.dimensions = dims;
    na.dimAnnotations = dimAnns;
    if (at("{")) {
      if (!dims.isEmpty()) {
        throw error(tok().start, "compiler.err.illegal.array.creation.both.dimension.and.initialization",
            "array creation with both dimension expression and initialization is illegal");
      }
      N.NewArray init = arrayInitializer(t, s);
      na.initializers = init.initializers;
    } else if (dims.isEmpty()) {
      throw error(tok().start, "compiler.err.array.dimension.missing", "array dimension missing");
    }
    return fin(na, s);
  }

  private com.sun.source.tree.ExpressionTree postfix(com.sun.source.tree.ExpressionTree e) {
    int s = ((N.Base) e).start;
    while (true) {
      Token t = tok();
      if (t.is(".")) {
        int dot = t.start;
        next();
        if (at("new")) {
          e = creator(e, s);
          continue;
        }
        if (at("<")) {
          List<Tree> targs = typeArgumentList();
          int ns = start();
          NameImpl nm = at("super") || at("this") ? name(next().text) : identName();
          N.MemberSelect ms = new N.MemberSelect();
          ms.expression = e;
          ms.identifier = nm;
          ms.pos = dot;
          fin(ms, s);
          if (at("(")) {
            N.MethodInvocation mi = new N.MethodInvocation();
            mi.methodSelect = ms;
            mi.typeArguments = targs;
            mi.arguments = arguments();
            e = fin(mi, s);
          } else {
            throw expected("(");
          }
          continue;
        }
        NameImpl nm;
        if (at("class")) {
          next();
          nm = name("class");
        } else if (at("this")) {
          next();
          nm = name("this");
        } else if (at("super")) {
          next();
          nm = name("super");
        } else {
          nm = identName();
        }
        N.MemberSelect ms = new N.MemberSelect();
        ms.expression = e;
        ms.identifier = nm;
        ms.pos = dot;
        e = fin(ms, s);
        if (at("(")) {
          N.MethodInvocation mi = new N.MethodInvocation();
          mi.methodSelect = e;
          mi.typeArguments = new ArrayList<Tree>();
          mi.arguments = arguments();
          e = fin(mi, s);
        }
      } else if (t.is("(") && (e instanceof N.Identifier)) {
        N.MethodInvocation mi = new N.MethodInvocation();
        mi.methodSelect = e;
        mi.typeArguments = new ArrayList<Tree>();
        mi.arguments = arguments();
        e = fin(mi, s);
      } else if (t.is("[")) {
        if (peekIs(1, "]") && (e instanceof N.Identifier || e instanceof N.MemberSelect
            || e instanceof N.PrimitiveType || e instanceof N.ArrayType || e instanceof N.ParameterizedType)) {
          N.Base ty = (N.Base) e;
          ty = bracketsOpt(ty, s);
          e = (com.sun.source.tree.ExpressionTree) ty;
          if (!at(".") && !at("::")) {
            throw expected("<identifier>");
          }
          continue;
        }
        next();
        boolean save = noLambda;
        noLambda = false;
        N.ArrayAccess aa = new N.ArrayAccess();
        aa.expression = e;
        aa.index = parseExpression();
        noLambda = save;
        expect("]");
        e = fin(aa, s);
      } else if (t.is("++") || t.is("--")) {
        next();
        N.Unary u = new N.Unary();
        u.kind = t.is("++") ? Tree.Kind.POSTFIX_INCREMENT : Tree.Kind.POSTFIX_DECREMENT;
        u.expression = e;
        e = fin(u, s);
      } else if (t.is("::")) {
        next();
        N.MemberReference mr = new N.MemberReference();
        mr.qualifierExpression = e;
        mr.typeArguments = null;
        if (at("<")) {
          mr.typeArguments = exprList(typeArgumentList());
        }
        if (at("new")) {
          next();
          mr.mode = MemberReferenceTree.ReferenceMode.NEW;
          mr.name = name("<init>");
        } else {
          mr.mode = MemberReferenceTree.ReferenceMode.INVOKE;
          mr.name = identName();
        }
        e = fin(mr, s);
      } else if (t.is("<") && genericTypeThenRef(e)) {
        N.Base ty = typeArgs((N.Base) e, s);
        ty = bracketsOpt(ty, s);
        e = (com.sun.source.tree.ExpressionTree) ty;
      } else {
        break;
      }
    }
    return e;
  }

  private boolean genericTypeThenRef(com.sun.source.tree.ExpressionTree e) {
    if (!(e instanceof N.Identifier || e instanceof N.MemberSelect)) {
      return false;
    }
    int j = scanTypeArgs(i);
    if (j < 0) {
      return false;
    }
    while (tk(j).is("[") && tk(j + 1).is("]")) {
      j += 2;
    }
    while (tk(j).is(".") && tk(j + 1).kind == Token.IDENT) {
      j += 2;
      if (tk(j).is("<")) {
        j = scanTypeArgs(j);
        if (j < 0) {
          return false;
        }
      }
    }
    return tk(j).is("::");
  }
}
