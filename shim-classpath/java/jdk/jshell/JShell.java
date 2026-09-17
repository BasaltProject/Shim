/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jshell;

import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.tools.*;

/** In-process evaluation engine: each snippet is wrapped in a class, compiled and run. */
public class JShell implements AutoCloseable {
  final PrintStream out;
  final PrintStream err;
  final List<Snippet> snippets = new ArrayList<Snippet>();
  final Map<Snippet, Snippet.Status> status = new LinkedHashMap<Snippet, Snippet.Status>();
  final Map<Snippet, List<Diag>> diags = new LinkedHashMap<Snippet, List<Diag>>();
  final Map<String, byte[]> classes = new LinkedHashMap<String, byte[]>();
  final List<Consumer<SnippetEvent>> eventListeners = new ArrayList<Consumer<SnippetEvent>>();
  final List<Consumer<JShell>> shutdownListeners = new ArrayList<Consumer<JShell>>();
  final List<String> classpath = new ArrayList<String>();
  final List<String> options;
  final BiFunction<Snippet, Integer, String> idGen;
  final Supplier<String> tempNames;
  final ReplLoader loader;
  final Map<Snippet, String> values = new LinkedHashMap<Snippet, String>();
  int nextId = 1;
  int tempCounter = 1;
  int wrapperCounter = 1;
  boolean closed;
  SourceCodeAnalysis analysis;

  /** Class loader holding the wrapper classes. */
  final class ReplLoader extends ClassLoader {
    ReplLoader(ClassLoader parent) {
      super(parent);
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
      byte[] b = classes.get(name);
      if (b == null) {
        throw new ClassNotFoundException(name);
      }
      return defineClass(name, b, 0, b.length);
    }
  }

  JShell(Builder b) {
    out = b.out == null ? System.out : b.out;
    err = b.err == null ? System.err : b.err;
    options = b.options;
    idGen = b.idGen;
    tempNames = b.tempNames;
    loader = new ReplLoader(ClassLoader.getSystemClassLoader());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static JShell create() throws IllegalStateException {
    return builder().build();
  }

  public void close() {
    if (!closed) {
      closed = true;
      for (Consumer<JShell> c : new ArrayList<Consumer<JShell>>(shutdownListeners)) {
        c.accept(this);
      }
    }
  }

  public void stop() {
  }

  private void check() {
    if (closed) {
      throw new IllegalStateException("JShell (" + this + ") has been closed.");
    }
  }

  public void addToClasspath(String path) {
    check();
    classpath.add(path);
  }

  public synchronized SourceCodeAnalysis sourceCodeAnalysis() {
    check();
    if (analysis == null) {
      analysis = new AnalysisImpl(this);
    }
    return analysis;
  }

  public Stream<Snippet> snippets() {
    check();
    return new ArrayList<Snippet>(snippets).stream();
  }

  private <T extends Snippet> Stream<T> active(Class<T> c) {
    check();
    List<T> out = new ArrayList<T>();
    for (Snippet s : snippets) {
      if (c.isInstance(s) && status(s).isActive()) {
        out.add(c.cast(s));
      }
    }
    return out.stream();
  }

  public Stream<VarSnippet> variables() {
    return active(VarSnippet.class);
  }

  public Stream<MethodSnippet> methods() {
    return active(MethodSnippet.class);
  }

  public Stream<TypeDeclSnippet> types() {
    return active(TypeDeclSnippet.class);
  }

  public Stream<ImportSnippet> imports() {
    return active(ImportSnippet.class);
  }

  public Snippet.Status status(Snippet snippet) {
    Snippet.Status s = status.get(snippet);
    return s == null ? Snippet.Status.NONEXISTENT : s;
  }

  public Stream<Diag> diagnostics(Snippet snippet) {
    check();
    List<Diag> d = diags.get(snippet);
    return d == null ? Stream.<Diag>empty() : new ArrayList<Diag>(d).stream();
  }

  public Stream<String> unresolvedDependencies(DeclarationSnippet snippet) {
    check();
    return Stream.<String>empty();
  }

  public String varValue(VarSnippet snippet) throws IllegalStateException {
    check();
    if (!status(snippet).isDefined()) {
      throw new IllegalArgumentException("Snippet parameter of varValue is not valid: " + snippet);
    }
    try {
      Class<?> k = loader.loadClass(snippet.wrapperClass);
      java.lang.reflect.Field f = k.getField(snippet.name);
      return valueString(f.get(null));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public Subscription onSnippetEvent(Consumer<SnippetEvent> listener) throws IllegalStateException {
    check();
    eventListeners.add(listener);
    return new Subscription(this, listener);
  }

  public Subscription onShutdown(Consumer<JShell> listener) throws IllegalStateException {
    check();
    shutdownListeners.add(listener);
    return new Subscription(this, listener);
  }

  public void unsubscribe(Subscription s) {
    eventListeners.remove(s.consumer);
    shutdownListeners.remove(s.consumer);
  }

  public List<SnippetEvent> drop(Snippet snippet) throws IllegalStateException {
    check();
    List<SnippetEvent> ev = new ArrayList<SnippetEvent>();
    Snippet.Status prev = status(snippet);
    if (!prev.isActive()) {
      throw new IllegalArgumentException("Snippet cannot be dropped: " + snippet);
    }
    status.put(snippet, Snippet.Status.DROPPED);
    ev.add(new SnippetEvent(snippet, prev, Snippet.Status.DROPPED, false, null, null, null));
    fire(ev);
    return ev;
  }

  private void fire(List<SnippetEvent> ev) {
    for (SnippetEvent e : ev) {
      for (Consumer<SnippetEvent> l : new ArrayList<Consumer<SnippetEvent>>(eventListeners)) {
        l.accept(e);
      }
    }
  }

  /* ---------- evaluation ---------- */

  public List<SnippetEvent> eval(String input) throws IllegalStateException {
    check();
    List<SnippetEvent> all = new ArrayList<SnippetEvent>();
    SourceCodeAnalysis a = sourceCodeAnalysis();
    String rest = input;
    while (true) {
      SourceCodeAnalysis.CompletionInfo ci = a.analyzeCompletion(rest);
      if (ci.completeness() == SourceCodeAnalysis.Completeness.EMPTY) {
        break;
      }
      String src = ci.source();
      if (src == null || ci.completeness() == SourceCodeAnalysis.Completeness.DEFINITELY_INCOMPLETE
          || ci.completeness() == SourceCodeAnalysis.Completeness.UNKNOWN) {
        src = rest;
        ci = new Done(src);
      }
      all.addAll(evalOne(src.trim()));
      rest = ci.remaining();
      if (rest == null || rest.trim().isEmpty()) {
        break;
      }
    }
    fire(all);
    return all;
  }

  private static final class Done implements SourceCodeAnalysis.CompletionInfo {
    final String s;

    Done(String s) {
      this.s = s;
    }

    public SourceCodeAnalysis.Completeness completeness() {
      return SourceCodeAnalysis.Completeness.COMPLETE;
    }

    public String remaining() {
      return "";
    }

    public String source() {
      return s;
    }
  }

  String newId(Snippet s) {
    int n = nextId++;
    return idGen == null ? String.valueOf(n) : idGen.apply(s, Integer.valueOf(n));
  }

  private String importsHeader(Snippet self) {
    StringBuilder sb = new StringBuilder("package REPL;\n");
    for (Snippet s : snippets) {
      if (s instanceof ImportSnippet && status(s).isActive()) {
        sb.append(s.source.endsWith(";") ? s.source : s.source + ";").append('\n');
      }
    }
    Map<String, Snippet> latest = new LinkedHashMap<String, Snippet>();
    for (Snippet s : snippets) {
      if (s == self || !status(s).isActive() || s.wrapperClass == null) {
        continue;
      }
      if (s.kind == Snippet.Kind.VAR || s.kind == Snippet.Kind.METHOD || s.kind == Snippet.Kind.TYPE_DECL
          || s.subKind == Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND) {
        latest.put(s.kind + ":" + s.name, s);
      }
    }
    for (Snippet s : latest.values()) {
      if (s.kind == Snippet.Kind.TYPE_DECL) {
        sb.append("import ").append(s.wrapperClass).append('.').append(s.name).append(";\n");
      } else {
        sb.append("import static ").append(s.wrapperClass).append('.').append(s.name).append(";\n");
      }
    }
    return sb.toString();
  }

  static final class Unit {
    String kindTag;
    String text;
    int start;
    int end;
    Tree tree;
    List<Tree> trees = new ArrayList<Tree>();
    String exprText;
    boolean isVar;
    String parseText;
  }

  static final class SrcFile extends SimpleJavaFileObject {
    final String code;

    SrcFile(String name, String code) {
      super(URI.create("string:///" + name.replace('.', '/') + ".java"), Kind.SOURCE);
      this.code = code;
    }

    public CharSequence getCharContent(boolean b) {
      return code;
    }
  }

  static final class OutFile extends SimpleJavaFileObject {
    final java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();

    OutFile(String name) {
      super(URI.create("mem:///" + name.replace('.', '/') + ".class"), Kind.CLASS);
    }

    public java.io.OutputStream openOutputStream() {
      return bytes;
    }
  }

  /** Compiles one wrapper; returns diagnostics and stores classes on success. */
  List<Diagnostic<? extends JavaFileObject>> compile(String className, String source, boolean store) {
    JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
    final DiagnosticCollector<JavaFileObject> dc = new DiagnosticCollector<JavaFileObject>();
    final Map<String, OutFile> outs = new LinkedHashMap<String, OutFile>();
    StandardJavaFileManager std = jc.getStandardFileManager(dc, null, null);
    JavaFileManager fm = new ForwardingJavaFileManager<StandardJavaFileManager>(std) {
      public JavaFileObject getJavaFileForOutput(Location l, String cn, JavaFileObject.Kind k, FileObject sibling) {
        OutFile o = new OutFile(cn);
        outs.put(cn, o);
        return o;
      }

      public ClassLoader getClassLoader(Location l) {
        return loader;
      }
    };
    List<String> opts = new ArrayList<String>(options);
    if (!classpath.isEmpty()) {
      StringBuilder cp = new StringBuilder();
      for (String c : classpath) {
        if (cp.length() > 0) {
          cp.append(java.io.File.pathSeparatorChar);
        }
        cp.append(c);
      }
      opts.add("-classpath");
      opts.add(cp.toString());
    }
    boolean ok = jc.getTask(null, fm, dc, opts, null, java.util.Collections.singletonList(
        new SrcFile(className, source))).call();
    if (ok && store) {
      for (Map.Entry<String, OutFile> e : outs.entrySet()) {
        classes.put(e.getKey(), e.getValue().bytes.toByteArray());
      }
    }
    return dc.getDiagnostics();
  }

  /** Parses source text and returns diagnostics-free trees, or null if it does not parse. */
  CompilationUnitTree parseUnit(String code, String name, JavacTask[] taskOut) {
    JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> dc = new DiagnosticCollector<JavaFileObject>();
    JavacTask task = (JavacTask) jc.getTask(null, null, dc, java.util.Collections.<String>emptyList(), null,
        java.util.Collections.singletonList(new SrcFile(name, code)));
    try {
      for (CompilationUnitTree cu : task.parse()) {
        if (!dc.getDiagnostics().isEmpty()) {
          return null;
        }
        if (taskOut != null) {
          taskOut[0] = task;
        }
        return cu;
      }
    } catch (Exception e) {
      return null;
    }
    return null;
  }

  Unit classify(String src) {
    String s = src.trim();
    Unit u = new Unit();
    u.text = s;
    if (s.startsWith("import ") || s.startsWith("import\t")) {
      String code = (s.endsWith(";") ? s : s + ";") + "\nclass $W {}";
      CompilationUnitTree cu = parseUnit(code, "$W", null);
      if (cu != null && !cu.getImports().isEmpty()) {
        u.kindTag = "import";
        u.tree = cu.getImports().get(0);
        return u;
      }
      u.kindTag = "error";
      return u;
    }
    if (s.matches("(?s)^(final\\s+)?var\\s+[A-Za-z_$][A-Za-z0-9_$]*\\s*=.*")) {
      String repl = s.replaceFirst("\\bvar\\b", "Object");
      String v = repl.endsWith(";") ? repl : repl + ";";
      CompilationUnitTree cu = parseUnit("class $W {\n" + v + "\n}", "$W", null);
      if (cu != null) {
        ClassTree ct = (ClassTree) cu.getTypeDecls().get(0);
        u.kindTag = "member";
        u.isVar = true;
        u.parseText = v;
        u.text = s;
        u.trees.addAll(ct.getMembers());
        return u;
      }
    }
    String[] variants = s.endsWith(";") ? new String[] {s} : new String[] {s, s + ";"};
    for (String v : variants) {
      String prefix = "class $W {\n";
      CompilationUnitTree cu = parseUnit(prefix + v + "\n}", "$W", null);
      if (cu != null && cu.getTypeDecls().size() == 1) {
        ClassTree ct = (ClassTree) cu.getTypeDecls().get(0);
        boolean decl = !ct.getMembers().isEmpty();
        for (Tree m : ct.getMembers()) {
          if (m instanceof BlockTree || (m instanceof MethodTree && ((MethodTree) m).getReturnType() == null
              && !((MethodTree) m).getName().contentEquals("<init>"))) {
            decl = false;
          }
          if (m instanceof MethodTree && ((MethodTree) m).getName().contentEquals("<init>")) {
            decl = false;
          }
        }
        if (decl) {
          u.text = v;
          u.kindTag = "member";
          u.trees.addAll(ct.getMembers());
          return u;
        }
      }
    }
    for (String v : variants) {
      CompilationUnitTree cu = parseUnit("class $W { void m() {\n" + v + "\n} }", "$W", null);
      if (cu != null) {
        ClassTree ct = (ClassTree) cu.getTypeDecls().get(0);
        MethodTree mt = (MethodTree) ct.getMembers().get(ct.getMembers().size() - 1);
        List<? extends StatementTree> st = mt.getBody().getStatements();
        if (st.size() == 1 && st.get(0) instanceof ExpressionStatementTree) {
          u.text = v.endsWith(";") ? v.substring(0, v.length() - 1) : v;
          u.kindTag = "expr";
          u.exprText = u.text;
          return u;
        }
        u.text = v;
        u.kindTag = "stmt";
        return u;
      }
    }
    CompilationUnitTree cu = parseUnit("class $W { Object o = (\n" + s + "\n); }", "$W", null);
    if (cu != null) {
      u.kindTag = "expr";
      u.exprText = s;
      return u;
    }
    u.kindTag = "error";
    return u;
  }

  private String typeOfExpression(String expr) {
    String code = "class $T { static void m() { var $v = (\n" + expr + "\n); } }";
    JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> dc = new DiagnosticCollector<JavaFileObject>();
    StandardJavaFileManager std = jc.getStandardFileManager(dc, null, null);
    JavaFileManager fm = new ForwardingJavaFileManager<StandardJavaFileManager>(std) {
      public ClassLoader getClassLoader(Location l) {
        return loader;
      }
    };
    final JavacTask task = (JavacTask) jc.getTask(null, fm, dc, java.util.Collections.<String>emptyList(), null,
        java.util.Collections.singletonList(new SrcFile("$T", importsHeader(null) + code)));
    try {
      Iterable<? extends CompilationUnitTree> cus = task.parse();
      task.analyze();
      if (!dc.getDiagnostics().isEmpty()) {
        return null;
      }
      final Trees trees = Trees.instance(task);
      final String[] res = new String[1];
      for (CompilationUnitTree cu : cus) {
        new TreePathScanner<Void, Void>() {
          public Void visitVariable(VariableTree v, Void p) {
            if (v.getName().contentEquals("$v")) {
              javax.lang.model.type.TypeMirror t = trees.getTypeMirror(getCurrentPath());
              res[0] = t == null ? null : t.toString();
              if (t != null && (t.getKind() == javax.lang.model.type.TypeKind.NULL)) {
                res[0] = "Object";
              }
            }
            return super.visitVariable(v, p);
          }
        }.scan(cu, null);
      }
      String t = res[0];
      if (t != null && (t.contains("capture") || t.contains("<anonymous") || t.contains("&"))) {
        return "Object";
      }
      return t;
    } catch (Exception e) {
      return null;
    }
  }

  private static String slice(String all, SourcePositions sp, CompilationUnitTree cu, Tree t, int off) {
    long s = sp.getStartPosition(cu, t);
    long e = sp.getEndPosition(cu, t);
    return all.substring((int) s - off, (int) e - off);
  }

  List<SnippetEvent> evalOne(String src) {
    List<SnippetEvent> ev = new ArrayList<SnippetEvent>();
    Unit u = classify(src);
    if (u.kindTag.equals("error")) {
      ErroneousSnippet es = new ErroneousSnippet("e" + (nextId++), src);
      snippets.add(es);
      status.put(es, Snippet.Status.REJECTED);
      List<Diag> ds = new ArrayList<Diag>();
      ds.add(new SimpleDiag("compiler.err.illegal.start.of.expr", "illegal start of expression", 0, true));
      diags.put(es, ds);
      ev.add(new SnippetEvent(es, Snippet.Status.NONEXISTENT, Snippet.Status.REJECTED, false, null, null, null));
      return ev;
    }
    if (u.kindTag.equals("import")) {
      return doImport(u, src);
    }
    if (u.kindTag.equals("member")) {
      for (Tree m : u.trees) {
        List<SnippetEvent> r = null;
        if (m instanceof ClassTree) {
          r = doDecl(u, (ClassTree) m);
        } else if (m instanceof MethodTree) {
          r = doMethod(u, (MethodTree) m);
        } else if (m instanceof VariableTree) {
          r = doVar(u, (VariableTree) m);
        }
        if (r != null) {
          ev.addAll(r);
        }
      }
      return ev;
    }
    if (u.kindTag.equals("expr")) {
      return doExpr(u);
    }
    return doStmt(u);
  }

  /* ---------- snippet kinds ---------- */

  private List<SnippetEvent> doImport(Unit u, String src) {
    ImportTree it = (ImportTree) u.tree;
    String full = it.getQualifiedIdentifier().toString();
    boolean onDemand = full.endsWith(".*");
    Snippet.SubKind sk = it.isStatic() ? (onDemand ? Snippet.SubKind.STATIC_IMPORT_ON_DEMAND_SUBKIND
        : Snippet.SubKind.SINGLE_STATIC_IMPORT_SUBKIND) : (onDemand ? Snippet.SubKind.TYPE_IMPORT_ON_DEMAND_SUBKIND
        : Snippet.SubKind.SINGLE_TYPE_IMPORT_SUBKIND);
    ImportSnippet s = new ImportSnippet(null, sk, src);
    return finish(s, u, "", "", new String[0], "import");
  }

  private List<SnippetEvent> doDecl(Unit u, ClassTree ct) {
    Snippet.SubKind sk;
    switch (ct.getKind()) {
      case INTERFACE:
        sk = Snippet.SubKind.INTERFACE_SUBKIND;
        break;
      case ENUM:
        sk = Snippet.SubKind.ENUM_SUBKIND;
        break;
      case RECORD:
        sk = Snippet.SubKind.RECORD_SUBKIND;
        break;
      case ANNOTATION_TYPE:
        sk = Snippet.SubKind.ANNOTATION_TYPE_SUBKIND;
        break;
      default:
        sk = Snippet.SubKind.CLASS_SUBKIND;
        break;
    }
    TypeDeclSnippet s = new TypeDeclSnippet(null, sk, u.text);
    s.name = ct.getSimpleName().toString();
    return finish(s, u, "public static ", null, new String[] {"type", slicesFor(u, ct)}, "type");
  }

  private String slicesFor(Unit u, Tree t) {
    return null;
  }

  private List<SnippetEvent> doMethod(Unit u, MethodTree mt) {
    MethodSnippet s = new MethodSnippet(null, u.text);
    s.name = mt.getName().toString();
    StringBuilder pt = new StringBuilder();
    for (VariableTree p : mt.getParameters()) {
      if (pt.length() > 0) {
        pt.append(',');
      }
      pt.append(p.getType());
    }
    s.paramTypes = pt.toString();
    s.signature = "(" + pt + ")" + mt.getReturnType();
    return finish(s, u, "public static ", null, new String[] {"method"}, "method");
  }

  private List<SnippetEvent> doVar(Unit u, VariableTree vt) {
    String typeText = vt.getType() == null ? null : vt.getType().toString();
    String init = vt.getInitializer() == null ? null : vt.getInitializer().toString();
    ExpressionTree ie = vt.getInitializer();
    String initSrc = null;
    if (ie != null) {
      String all = "class $W {\n" + (u.parseText != null ? u.parseText : u.text) + "\n}";
      JavacTask[] tk = new JavacTask[1];
      CompilationUnitTree cu = parseUnit(all, "$W", tk);
      Trees tr = Trees.instance(tk[0]);
      final VariableTree[] found = new VariableTree[1];
      new TreePathScanner<Void, Void>() {
        public Void visitVariable(VariableTree v, Void p) {
          if (found[0] == null && v.getName().equals(vt.getName())) {
            found[0] = v;
          }
          return super.visitVariable(v, p);
        }
      }.scan(cu, null);
      SourcePositions sp = tr.getSourcePositions();
      if (found[0] != null && found[0].getInitializer() != null) {
        initSrc = slice(all, sp, cu, found[0].getInitializer(), 0);
        if (found[0].getType() != null && vt.getType() != null) {
          typeText = slice(all, sp, cu, found[0].getType(), 0);
        }
      }
    }
    boolean isVar = u.isVar || vt.getType() == null || "var".equals(typeText);
    if (isVar) {
      String t = initSrc == null ? null : typeOfExpression(initSrc);
      if (t == null) {
        ErroneousSnippet es = new ErroneousSnippet("e", u.text);
        return finish(es, u, null, null, new String[] {"error"}, "error");
      }
      typeText = t;
    }
    VarSnippet s = new VarSnippet(null, initSrc == null ? Snippet.SubKind.VAR_DECLARATION_SUBKIND
        : Snippet.SubKind.VAR_DECLARATION_WITH_INITIALIZER_SUBKIND, u.text);
    s.name = vt.getName().toString();
    s.typeName = typeText;
    return finish(s, u, null, typeText, new String[] {"var", initSrc}, "var");
  }

  private List<SnippetEvent> doExpr(Unit u) {
    String e = u.exprText;
    String name = null;
    Snippet.SubKind sk = Snippet.SubKind.OTHER_EXPRESSION_SUBKIND;
    Snippet existing = null;
    if (e.matches("[A-Za-z_$][A-Za-z0-9_$]*")) {
      existing = latestVar(e);
      if (existing != null) {
        sk = Snippet.SubKind.VAR_VALUE_SUBKIND;
      }
    } else if (e.matches("[A-Za-z_$][A-Za-z0-9_$]*\\s*=[^=].*")) {
      String n = e.substring(0, e.indexOf('=')).trim();
      if (latestVar(n) != null) {
        sk = Snippet.SubKind.ASSIGNMENT_SUBKIND;
        name = n;
      }
    }
    String type = typeOfExpression(e);
    boolean isVoid = type != null && type.equals("void");
    ExpressionSnippet s;
    if (sk == Snippet.SubKind.VAR_VALUE_SUBKIND) {
      s = new ExpressionSnippet(null, sk, u.text);
      s.name = e;
      s.typeName = ((VarSnippet) existing).typeName;
      return finish(s, u, null, s.typeName, new String[] {"exprvar", e}, "exprvar");
    }
    if (sk == Snippet.SubKind.ASSIGNMENT_SUBKIND) {
      s = new ExpressionSnippet(null, sk, u.text);
      s.name = name;
      s.typeName = ((VarSnippet) latestVar(name)).typeName;
      return finish(s, u, null, s.typeName, new String[] {"exprassign", e}, "exprassign");
    }
    if (type == null && e.trim().equals("null")) {
      type = "Object";
    }
    if (type == null && parseUnit("class $W { void m() {\n" + e + ";\n} }", "$W", null) != null) {
      List<Diagnostic<? extends JavaFileObject>> probe = compile("REPL.$Probe" + nextId,
          importsHeader(null) + "public class $Probe" + nextId + " { static Object do_it$() throws Throwable {\n" + e
          + ";\nreturn null; } }\n", false);
      if (noErrors(probe)) {
        isVoid = true;
        type = "void";
      }
    }
    if (type == null) {
      ErroneousSnippet es = new ErroneousSnippet("e", u.text);
      return finish(es, u, null, null, new String[] {"error"}, "error");
    }
    if (isVoid) {
      StatementSnippet st = new StatementSnippet(null, u.text);
      return finish(st, u, null, null, new String[] {"exprvoid", e}, "exprvoid");
    }
    VarSnippet tv = new VarSnippet(null, Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND, u.text);
    tv.typeName = type;
    return finish(tv, u, null, type, new String[] {"exprtemp", e}, "exprtemp");
  }

  private List<SnippetEvent> doStmt(Unit u) {
    StatementSnippet s = new StatementSnippet(null, u.text);
    return finish(s, u, null, null, new String[] {"stmt", u.text}, "stmt");
  }

  private Snippet latestVar(String name) {
    Snippet r = null;
    for (Snippet s : snippets) {
      if (s instanceof VarSnippet && status(s).isActive() && name.equals(s.name)) {
        r = s;
      }
    }
    return r;
  }

  /** Builds the wrapper source for a snippet and runs the compile and execute cycle. */
  private List<SnippetEvent> finish(Snippet s, Unit u, String modifiers, String type, String[] info, String what) {
    Snippet fresh = s;
    String id = null;
    Snippet replaced = null;
    if (s.kind == Snippet.Kind.VAR && s.subKind != Snippet.SubKind.TEMP_VAR_EXPRESSION_SUBKIND || s.kind
        == Snippet.Kind.METHOD || s.kind == Snippet.Kind.TYPE_DECL) {
      for (Snippet o : snippets) {
        if (status(o).isActive() && o.kind == s.kind && java.util.Objects.equals(o.name, s.name)
            && java.util.Objects.equals(o.paramTypes, s.paramTypes)
            && java.util.Objects.equals(o.typeName, s.typeName)) {
          replaced = o;
        }
      }
    }
    if (replaced != null) {
      id = replaced.id;
    } else {
      id = newId(fresh);
    }
    fresh = reid(fresh, id);
    String cls = "REPL.$JShell$" + (wrapperCounter++);
    fresh.wrapperClass = cls;
    String simple = cls.substring(cls.lastIndexOf('.') + 1);
    StringBuilder body = new StringBuilder();
    String call = null;
    int offset = 0;
    String name = fresh.name;
    String header = importsHeader(fresh) + "public class " + simple + " {\n";
    if (what.equals("error")) {
      snippets.add(fresh);
      status.put(fresh, Snippet.Status.REJECTED);
      List<SnippetEvent> er = new ArrayList<SnippetEvent>();
      er.add(new SnippetEvent(fresh, Snippet.Status.NONEXISTENT, Snippet.Status.REJECTED, false, null, null, null));
      return er;
    }
    if (what.equals("import")) {
      body.append("");
    } else if (what.equals("type") || what.equals("method")) {
      String text = strippedModifiers(u.text, what);
      body.append(modifiers).append(text).append('\n');
      offset = header.length() + modifiers.length();
    } else if (what.equals("var")) {
      String initSrc = info[1];
      body.append("public static ").append(type).append(' ').append(name).append(";\n");
      body.append("public static Object do_it$() throws Throwable {\n");
      if (initSrc != null) {
        if (initSrc.trim().startsWith("{")) {
          body.append(name).append(" = new ").append(type).append(initSrc).append(";\n");
        } else {
          body.append(name).append(" = ").append(initSrc).append(";\n");
        }
      }
      body.append("return ").append(name).append(";\n}\n");
      call = "do_it$";
    } else if (what.equals("exprvar")) {
      body.append("public static Object do_it$() throws Throwable { return ").append(info[1]).append("; }\n");
      call = "do_it$";
    } else if (what.equals("exprassign")) {
      body.append("public static Object do_it$() throws Throwable { return (").append(info[1]).append("); }\n");
      call = "do_it$";
    } else if (what.equals("exprvoid")) {
      body.append("public static Object do_it$() throws Throwable { ").append(info[1]).append(";\nreturn null; }\n");
      call = "do_it$";
    } else if (what.equals("exprtemp")) {
      String tn = tempNames != null ? tempNames.get() : "$" + (tempCounter++);
      fresh.name = tn;
      body.append("public static ").append(type).append(' ').append(tn).append(";\n");
      body.append("public static Object do_it$() throws Throwable { ").append(tn).append(" = ").append(info[1])
          .append(";\nreturn ").append(tn).append("; }\n");
      call = "do_it$";
    } else {
      body.append("public static Object do_it$() throws Throwable {\n").append(info[1]).append("\nreturn null; }\n");
      call = "do_it$";
    }
    String full = header + body + "}\n";
    fresh.wrapperSource = full;
    fresh.wrapperOffset = what.equals("type") || what.equals("method") ? offset : 0;
    List<SnippetEvent> ev = new ArrayList<SnippetEvent>();
    Snippet.Status prev = Snippet.Status.NONEXISTENT;
    snippets.add(fresh);
    if (what.equals("import")) {
      ImportTree it = (ImportTree) u.tree;
      String fn = it.getQualifiedIdentifier().toString();
      fresh.name = fn;
      ((ImportSnippet) fresh).fullname = fn.endsWith(".*") ? fn.substring(0, fn.length() - 2) : fn;
      ((ImportSnippet) fresh).isStatic = it.isStatic();
      List<Diagnostic<? extends JavaFileObject>> d = compile(cls, header + "}\n", false);
      return statusEvents(fresh, ev, d, null, null, prev, noErrors(d));
    }
    List<Diagnostic<? extends JavaFileObject>> d = compile(cls, full, true);
    boolean err = false;
    for (Diagnostic<? extends JavaFileObject> x : d) {
      if (x.getKind() == Diagnostic.Kind.ERROR) {
        err = true;
      }
    }
    if (err) {
      classes.remove(cls);
      return statusEvents(fresh, ev, d, null, null, prev, false);
    }
    String value = null;
    JShellException exc = null;
    if (call != null) {
      try {
        Class<?> k = loader.loadClass(cls);
        java.lang.reflect.Method m = k.getMethod(call);
        Object r = m.invoke(null);
        if (fresh.kind == Snippet.Kind.STATEMENT) {
          value = "";
        } else if (fresh.subKind.hasValue() || fresh.kind == Snippet.Kind.VAR) {
          value = valueString(r);
        }
      } catch (java.lang.reflect.InvocationTargetException ite) {
        exc = toException(ite.getCause());
      } catch (Exception e) {
        exc = toException(e);
      }
    }
    return statusEvents(fresh, ev, d, value, exc, prev, true);
  }

  private static boolean noErrors(List<Diagnostic<? extends JavaFileObject>> d) {
    for (Diagnostic<? extends JavaFileObject> x : d) {
      if (x.getKind() == Diagnostic.Kind.ERROR) {
        return false;
      }
    }
    return true;
  }

  private JShellException toException(Throwable t) {
    JShellException cause = t.getCause() == null || t.getCause() == t ? null : toException(t.getCause());
    return new EvalException(t.getMessage(), t.getClass().getName(), t.getStackTrace(), cause);
  }

  private List<SnippetEvent> statusEvents(Snippet fresh, List<SnippetEvent> ev,
      List<Diagnostic<? extends JavaFileObject>> d, String value, JShellException exc, Snippet.Status prev,
      boolean ok) {
    List<Diag> ds = new ArrayList<Diag>();
    for (Diagnostic<? extends JavaFileObject> x : d) {
      ds.add(new SimpleDiag(x.getCode(), x.getMessage(Locale.getDefault()), x.getPosition() - fresh.wrapperOffset,
          x.getKind() == Diagnostic.Kind.ERROR));
    }
    diags.put(fresh, ds);
    Snippet.Status st = ok ? Snippet.Status.VALID : Snippet.Status.REJECTED;
    status.put(fresh, st);
    ev.add(new SnippetEvent(fresh, prev, st, false, null, value, exc));
    if (ok && fresh instanceof PersistentSnippet && fresh.name != null) {
      for (Snippet old : new ArrayList<Snippet>(snippets)) {
        if (old != fresh && status(old).isActive() && old.kind == fresh.kind && fresh.name != null
            && fresh.name.equals(old.name) && sameSignature(old, fresh)) {
          status.put(old, Snippet.Status.OVERWRITTEN);
          if (old.id.equals(fresh.id)) {
            ev.set(0, new SnippetEvent(fresh, Snippet.Status.VALID, Snippet.Status.VALID, false, null,
                ev.get(0).value(), ev.get(0).exception()));
          }
          ev.add(new SnippetEvent(old, Snippet.Status.VALID, Snippet.Status.OVERWRITTEN, false, fresh, null, null));
        }
      }
    }
    return ev;
  }

  private boolean sameSignature(Snippet a, Snippet b) {
    if (a instanceof MethodSnippet) {
      return java.util.Objects.equals(a.paramTypes, b.paramTypes);
    }
    return true;
  }

  private Snippet reid(Snippet s, String id) {
    Snippet n;
    if (s instanceof ImportSnippet) {
      n = new ImportSnippet(id, s.subKind, s.source);
    } else if (s instanceof TypeDeclSnippet) {
      n = new TypeDeclSnippet(id, s.subKind, s.source);
    } else if (s instanceof MethodSnippet) {
      n = new MethodSnippet(id, s.source);
    } else if (s instanceof VarSnippet) {
      n = new VarSnippet(id, s.subKind, s.source);
    } else if (s instanceof ExpressionSnippet) {
      n = new ExpressionSnippet(id, s.subKind, s.source);
    } else if (s instanceof ErroneousSnippet) {
      n = new ErroneousSnippet(id, s.source);
    } else {
      n = new StatementSnippet(id, s.source);
    }
    n.name = s.name;
    n.typeName = s.typeName == null ? null : s.typeName.replaceAll("REPL\\.\\$JShell\\$[0-9]+\\.", "");
    n.signature = s.signature;
    n.paramTypes = s.paramTypes;
    return n;
  }

  private static String strippedModifiers(String text, String what) {
    String t = text.trim();
    boolean changed = true;
    while (changed) {
      changed = false;
      for (String m : new String[] {"public ", "private ", "protected ", "static ", "final ", "abstract "}) {
        if (t.startsWith(m) && !(what.equals("type") && m.equals("final ") && false)) {
          t = t.substring(m.length()).trim();
          changed = true;
        }
      }
    }
    return t;
  }

  static String valueString(Object v) {
    if (v == null) {
      return "null";
    }
    if (v instanceof String) {
      return "\"" + escape((String) v) + "\"";
    }
    if (v instanceof Character) {
      return "'" + escape(String.valueOf(v)).replace("\\\"", "\"") + "'";
    }
    if (v.getClass().isArray()) {
      StringBuilder sb = new StringBuilder();
      Class<?> ct = v.getClass().getComponentType();
      int n = java.lang.reflect.Array.getLength(v);
      sb.append(ct.getTypeName()).append('[').append(n).append("] { ");
      for (int i = 0; i < n; i++) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(valueString(java.lang.reflect.Array.get(v, i)));
      }
      return sb.append(" }").toString();
    }
    return String.valueOf(v);
  }

  private static String escape(String s) {
    StringBuilder sb = new StringBuilder();
    for (char c : s.toCharArray()) {
      switch (c) {
        case '\n':
          sb.append("\\n");
          break;
        case '\t':
          sb.append("\\t");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\b':
          sb.append("\\b");
          break;
        case '\f':
          sb.append("\\f");
          break;
        case '"':
          sb.append("\\\"");
          break;
        case '\\':
          sb.append("\\\\");
          break;
        case '\'':
          sb.append("\\'");
          break;
        default:
          sb.append(c);
          break;
      }
    }
    return sb.toString();
  }

  static final class SimpleDiag extends Diag {
    final String code;
    final String message;
    final long pos;
    final boolean error;

    SimpleDiag(String code, String message, long pos, boolean error) {
      this.code = code;
      this.message = message;
      this.pos = pos;
      this.error = error;
    }

    public String getCode() {
      return code;
    }

    public long getEndPosition() {
      return pos;
    }

    public String getMessage(Locale locale) {
      return message;
    }

    public long getPosition() {
      return pos;
    }

    public long getStartPosition() {
      return pos;
    }

    public boolean isError() {
      return error;
    }
  }

  public static class Builder {
    PrintStream out;
    PrintStream err;
    List<String> options = new ArrayList<String>();
    BiFunction<Snippet, Integer, String> idGen;
    Supplier<String> tempNames;

    Builder() {
    }

    public Builder in(InputStream in) {
      return this;
    }

    public Builder out(PrintStream out) {
      this.out = out;
      return this;
    }

    public Builder err(PrintStream err) {
      this.err = err;
      return this;
    }

    public Builder console(JShellConsole console) {
      return this;
    }

    public Builder idGenerator(BiFunction<Snippet, Integer, String> g) {
      idGen = g;
      return this;
    }

    public Builder tempVariableNameGenerator(Supplier<String> s) {
      tempNames = s;
      return this;
    }

    public Builder compilerOptions(String... o) {
      options.addAll(java.util.Arrays.asList(o));
      return this;
    }

    public Builder remoteVMOptions(String... o) {
      return this;
    }

    public Builder executionEngine(String spec) {
      return this;
    }

    public Builder executionEngine(jdk.jshell.spi.ExecutionControlProvider p, Map<String, String> m) {
      return this;
    }

    public Builder fileManager(Function<StandardJavaFileManager, StandardJavaFileManager> f) {
      return this;
    }

    public JShell build() throws IllegalStateException {
      return new JShell(this);
    }
  }

  public class Subscription {
    final Object consumer;

    Subscription(JShell j, Consumer<?> c) {
      consumer = c;
    }
  }
}
