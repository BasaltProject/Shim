/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;

/** Declares source classes and members, and resolves type trees to types. */
final class Enter implements CSym.Completer {
  final Symtab st;
  final Tx tx;
  final Log log;
  final Map<CompilationUnitTree, Env.CuEnv> cuEnvs = new IdentityHashMap<CompilationUnitTree, Env.CuEnv>();
  final Map<CSym, Env> classEnvs = new IdentityHashMap<CSym, Env>();
  final Map<Tree, CSym> classOf = new IdentityHashMap<Tree, CSym>();
  final Map<Tree, Object> symOf = new IdentityHashMap<Tree, Object>();
  final List<CSym> sourceClasses = new ArrayList<CSym>();
  Attr attrRef;
  boolean headerPhase;
  final Map<CSym, Env> localOuter = new IdentityHashMap<CSym, Env>();

  Enter(Symtab st, Tx tx, Log log) {
    this.st = st;
    this.tx = tx;
    this.log = log;
  }

  /* ---------- entering classes ---------- */

  void enterUnits(List<? extends CompilationUnitTree> units) {
    for (CompilationUnitTree cu : units) {
      Env.CuEnv ce = new Env.CuEnv(cu);
      cuEnvs.put(cu, ce);
      ExpressionTree pn = cu.getPackageName();
      ce.pkg = pn == null ? "" : pn.toString();
      for (Tree d : cu.getTypeDecls()) {
        if (d instanceof ClassTree) {
          ClassTree ct = (ClassTree) d;
          String simple = ct.getSimpleName().toString();
          String internal = (ce.pkg.isEmpty() ? "" : ce.pkg.replace('.', '/') + "/") + simple;
          if (st.classes.containsKey(internal) && st.classes.get(internal).isSource) {
            log.cu = cu;
            log.error(d, "compiler.err.duplicate.class", "duplicate class: " + internal.replace('/', '.'));
            continue;
          }
          enterClass(ct, ce, null, internal, null);
        }
      }
    }
  }

  CSym enterClass(ClassTree t, Env.CuEnv ce, CSym outer, String internal, Env localEnv) {
    CSym c = new CSym(internal);
    c.isSource = true;
    c.tree = t;
    c.cu = ce.tree;
    c.name = t.getSimpleName().toString();
    c.outerClass = outer;
    c.completer = this;
    c.enterRef = this;
    c.flags = classFlags(t, outer);
    st.enter(c);
    classOf.put(t, c);
    symOf.put(t, c);
    sourceClasses.add(c);
    if (localEnv != null) {
      localOuter.put(c, localEnv);
    }
    for (Tree m : t.getMembers()) {
      if (m instanceof ClassTree) {
        ClassTree mt = (ClassTree) m;
        String mn = mt.getSimpleName().toString();
        if (c.memberClasses.containsKey(mn)) {
          log.cu = ce.tree;
          log.error(m, "compiler.err.already.defined", "class " + mn + " is already defined in class " + c.fullName());
          continue;
        }
        CSym mc = enterClass(mt, ce, c, internal + "$" + mn, null);
        c.memberClasses.put(mn, mc);
      }
    }
    return c;
  }

  private int classFlags(ClassTree t, CSym outer) {
    int f = modFlags(t.getModifiers());
    switch (t.getKind()) {
      case INTERFACE:
        f |= Sym.INTERFACE | Sym.ABSTRACT;
        break;
      case ANNOTATION_TYPE:
        f |= Sym.INTERFACE | Sym.ABSTRACT | Sym.ANNOTATION;
        break;
      case ENUM:
        f |= Sym.ENUM;
        break;
      case RECORD:
        f |= Sym.RECORD | Sym.FINAL;
        break;
      default:
        break;
    }
    if (outer != null) {
      if (t.getKind() != Tree.Kind.CLASS) {
        f |= Sym.STATIC;
      }
      if (outer.isInterface()) {
        f |= Sym.STATIC | Sym.PUBLIC;
      }
    }
    if (t.getKind() == Tree.Kind.ENUM) {
      boolean bodies = false;
      for (Tree m : t.getMembers()) {
        if (m instanceof VariableTree && ((N.Variable) m).enumConstant) {
          ExpressionTree init = ((VariableTree) m).getInitializer();
          if (init instanceof NewClassTree && ((NewClassTree) init).getClassBody() != null) {
            bodies = true;
          }
        }
      }
      if (!bodies) {
        f |= Sym.FINAL;
      }
    }
    return f;
  }

  static int modFlags(ModifiersTree mt) {
    int f = 0;
    for (Modifier m : mt.getFlags()) {
      switch (m) {
        case PUBLIC:
          f |= Sym.PUBLIC;
          break;
        case PRIVATE:
          f |= Sym.PRIVATE;
          break;
        case PROTECTED:
          f |= Sym.PROTECTED;
          break;
        case STATIC:
          f |= Sym.STATIC;
          break;
        case FINAL:
          f |= Sym.FINAL;
          break;
        case ABSTRACT:
          f |= Sym.ABSTRACT;
          break;
        case NATIVE:
          f |= Sym.NATIVE;
          break;
        case SYNCHRONIZED:
          f |= Sym.SYNCHRONIZED;
          break;
        case TRANSIENT:
          f |= Sym.TRANSIENT;
          break;
        case VOLATILE:
          f |= Sym.VOLATILE;
          break;
        case STRICTFP:
          f |= Sym.STRICT;
          break;
        case DEFAULT:
          f |= Sym.DEFAULT;
          break;
        case SEALED:
          f |= Sym.SEALED;
          break;
        case NON_SEALED:
          f |= Sym.NON_SEALED;
          break;
        default:
          break;
      }
    }
    return f;
  }

  /** Enters a local class declared in a method body. */
  CSym enterLocal(ClassTree t, Env env) {
    CSym existing = classOf.get(t);
    if (existing != null) {
      return existing;
    }
    CSym encl = env.cls;
    String simple = t.getSimpleName().toString();
    int i = 1;
    while (st.classes.containsKey(encl.internal + "$" + i + simple)) {
      i++;
    }
    CSym c = enterClass(t, env.cu, null, encl.internal + "$" + i + simple, env);
    c.local = true;
    c.outerClass = encl;
    c.enclosingMethod = env.method;
    if (env.staticCtx) {
      c.flags |= Sym.STATIC;
    }
    c.flags = c.flags & ~Sym.STATIC | (env.staticCtx ? Sym.STATIC : 0);
    return c;
  }

  /** Enters an anonymous class body with the given supertype. */
  CSym enterAnon(ClassTree body, Env env, Ty sup) {
    CSym existing = classOf.get(body);
    if (existing != null) {
      return existing;
    }
    CSym encl = env.cls;
    int i = 1;
    while (st.classes.containsKey(encl.internal + "$" + i)) {
      i++;
    }
    CSym c = new CSym(encl.internal + "$" + i);
    c.isSource = true;
    c.tree = body;
    c.cu = env.cu.tree;
    c.name = "";
    c.anon = true;
    c.outerClass = encl;
    c.enclosingMethod = env.method;
    c.flags = env.staticCtx ? Sym.STATIC : 0;
    c.completer = this;
    c.enterRef = this;
    c.headerState = 2;
    if (sup.tag == Ty.CLASS && sup.sym.isInterface()) {
      c.superclass = st.object;
      c.interfaces.add(sup);
    } else {
      c.superclass = sup;
    }
    st.enter(c);
    classOf.put(body, c);
    symOf.put(body, c);
    sourceClasses.add(c);
    localOuter.put(c, env);
    for (Tree m : body.getMembers()) {
      if (m instanceof ClassTree) {
        String mn = ((ClassTree) m).getSimpleName().toString();
        CSym mc = enterClass((ClassTree) m, env.cu, c, c.internal + "$" + mn, null);
        c.memberClasses.put(mn, mc);
      }
    }
    return c;
  }

  /* ---------- environments ---------- */

  Env classEnv(CSym c) {
    Env e = classEnvs.get(c);
    if (e != null) {
      return e;
    }
    Env outerEnv = localOuter.get(c);
    if (outerEnv == null && c.outerClass != null) {
      outerEnv = classEnv(c.outerClass);
    }
    Env.CuEnv ce = cuEnvs.get(c.cu);
    e = new Env(outerEnv, ce);
    e.isClassEnv = true;
    e.cls = c;
    e.method = null;
    e.scopeOwner = c;
    e.staticCtx = false;
    e.ctorPrologue = false;
    e.lambda = null;
    e.lambdaReturns = null;
    e.inLambdaBody = false;
    e.returnType = null;
    classEnvs.put(c, e);
    return e;
  }

  /* ---------- completion of source classes ---------- */

  public void completeHeader(CSym c) {
    ClassTree t = c.tree;
    if (t == null) {
      return;
    }
    Env env = classEnv(c);
    log.cu = c.cu;
    for (TypeParameterTree tp : t.getTypeParameters()) {
      TVSym s = new TVSym(tp.getName().toString(), c);
      c.tparams.add(s);
    }
    int i = 0;
    for (TypeParameterTree tp : t.getTypeParameters()) {
      TVSym s = c.tparams.get(i++);
      fillBounds(s, tp, env);
    }
    Tree.Kind k = t.getKind();
    headerPhase = true;
    try {
      completeSupers(c, t, env, k);
    } finally {
      headerPhase = false;
    }
  }

  private void completeSupers(CSym c, ClassTree t, Env env, Tree.Kind k) {
    if (k == Tree.Kind.CLASS) {
      Tree ext = t.getExtendsClause();
      if (ext != null) {
        Ty s = resolveType(ext, env);
        c.superclass = s.tag == Ty.CLASS ? s : st.object;
        if (s.tag == Ty.CLASS && s.sym.isInterface()) {
          log.error(ext, "compiler.err.no.intf.expected.here", "no interface expected here");
        }
      } else {
        c.superclass = c.internal.equals("java/lang/Object") ? null : st.object;
      }
    } else if (k == Tree.Kind.ENUM) {
      CSym en = st.lookup("java/lang/Enum");
      c.superclass = Ty.cls(en, c.erasedType());
    } else if (k == Tree.Kind.RECORD) {
      c.superclass = Ty.cls(st.lookup("java/lang/Record"));
    }
    for (Tree it : t.getImplementsClause()) {
      Ty s = resolveType(it, env);
      if (s.tag == Ty.CLASS) {
        c.interfaces.add(s);
      }
    }
    if (k == Tree.Kind.ANNOTATION_TYPE) {
      c.interfaces.add(Ty.cls(st.lookup("java/lang/annotation/Annotation")));
    }
    for (Tree pt : t.getPermitsClause()) {
      Ty s = resolveType(pt, env);
      if (s.tag == Ty.CLASS) {
        c.permitted.add(s);
      }
    }
  }

  private void fillBounds(TVSym s, TypeParameterTree tp, Env env) {
    for (Tree b : tp.getBounds()) {
      s.bounds.add(resolveType(b, env));
    }
    if (s.bounds.isEmpty()) {
      s.bounds.add(st.object);
    }
  }

  public void completeMembers(CSym c) {
    ClassTree t = c.tree;
    if (t == null) {
      return;
    }
    Env env = classEnv(c);
    log.cu = c.cu;
    boolean isIntf = c.isInterface();
    List<VariableTree> comps = new ArrayList<VariableTree>();
    boolean hasCtor = false;
    for (Tree m : t.getMembers()) {
      if (m instanceof VariableTree) {
        VariableTree vt = (VariableTree) m;
        FSym f = new FSym();
        f.name = vt.getName().toString();
        f.tree = vt;
        int fl = modFlags(vt.getModifiers());
        boolean enumConst = vt instanceof N.Variable && ((N.Variable) vt).enumConstant;
        if (isIntf) {
          fl |= Sym.PUBLIC | Sym.STATIC | Sym.FINAL;
        }
        if (enumConst) {
          fl |= Sym.PUBLIC | Sym.STATIC | Sym.FINAL | Sym.ENUM;
          f.type = c.erasedType();
        } else {
          f.type = resolveType(vt.getType(), env);
        }
        boolean isComp = c.isRecord() && (fl & Sym.STATIC) == 0;
        if (isComp) {
          fl |= Sym.PRIVATE | Sym.FINAL;
          comps.add(vt);
        }
        f.flags = fl;
        if (c.fields.containsKey(f.name)) {
          log.error(m, "compiler.err.already.defined", "variable " + f.name + " is already defined in class "
              + c.fullName());
        }
        c.addField(f);
        symOf.put(vt, f);
        if (isComp) {
          c.recordComponents.add(f);
        }
      } else if (m instanceof MethodTree) {
        MethodTree mt = (MethodTree) m;
        MSym ms = enterMethod(c, mt, env, isIntf);
        if (ms.isCtor()) {
          hasCtor = true;
        }
        c.addMethod(ms);
        symOf.put(mt, ms);
      }
    }
    addImplicitMembers(c, hasCtor, comps);
  }

  MSym enterMethod(CSym c, MethodTree mt, Env classEnv, boolean isIntf) {
    MSym ms = new MSym();
    ms.name = mt.getName().toString();
    ms.tree = mt;
    ms.owner = c;
    int fl = modFlags(mt.getModifiers());
    if (isIntf) {
      if ((fl & Sym.PRIVATE) == 0) {
        fl |= Sym.PUBLIC;
      }
      if ((fl & (Sym.DEFAULT | Sym.STATIC | Sym.PRIVATE)) == 0 && mt.getBody() == null) {
        fl |= Sym.ABSTRACT;
      }
    }
    if (ms.isCtor() && c.isEnum()) {
      fl = (fl & ~(Sym.PUBLIC | Sym.PROTECTED)) | Sym.PRIVATE;
    }
    Env menv = classEnv.dup();
    menv.isClassEnv = false;
    menv.method = ms;
    menv.cls = c;
    for (TypeParameterTree tp : mt.getTypeParameters()) {
      TVSym s = new TVSym(tp.getName().toString(), ms);
      ms.tparams.add(s);
      menv.addTvar(s);
    }
    int i = 0;
    for (TypeParameterTree tp : mt.getTypeParameters()) {
      fillBounds(ms.tparams.get(i++), tp, menv);
    }
    if (ms.isCtor()) {
      ms.ret = Ty.tVoid;
    } else if (mt.getReturnType() != null) {
      ms.ret = resolveType(mt.getReturnType(), menv);
    }
    ms.paramNames = new ArrayList<String>();
    List<? extends VariableTree> ps = mt.getParameters();
    if (ps != null) {
      for (VariableTree p : ps) {
        ms.params.add(resolveType(p.getType(), menv));
        ms.paramNames.add(p.getName().toString());
        if (p instanceof N.Variable && ((N.Variable) p).varargs) {
          fl |= Sym.VARARGS;
        }
      }
    }
    for (ExpressionTree th : mt.getThrows()) {
      ms.thrown.add(resolveType(th, menv));
    }
    if (mt instanceof N.Method && ((N.Method) mt).compactConstructor) {
      fl |= Sym.COMPACT_CTOR;
    }
    ms.flags = fl;
    if (mt.getDefaultValue() != null) {
      ms.hasDefaultValue = true;
    }
    return ms;
  }

  private void addImplicitMembers(CSym c, boolean hasCtor, List<VariableTree> comps) {
    boolean needCtor = !hasCtor;
    if (c.isRecord() && hasCtor) {
      needCtor = true;
      for (MSym m : c.methodsNamed("<init>")) {
        if (m.params.size() == c.recordComponents.size()) {
          boolean same = true;
          for (int i = 0; i < m.params.size(); i++) {
            if (!TypeOps.erasure(m.params.get(i), st.object).desc().equals(TypeOps.erasure(
                c.recordComponents.get(i).type, st.object).desc())) {
              same = false;
            }
          }
          if (same) {
            needCtor = false;
          }
        }
      }
    }
    if (needCtor && !c.isInterface() && !c.anon) {
      MSym ctor = new MSym();
      ctor.name = "<init>";
      ctor.owner = c;
      ctor.flags = Sym.GENERATED_CTOR | (c.flags & (Sym.PUBLIC | Sym.PROTECTED | Sym.PRIVATE));
      if (c.isEnum()) {
        ctor.flags = Sym.GENERATED_CTOR | Sym.PRIVATE;
      }
      if (c.isRecord()) {
        ctor.flags |= Sym.COMPACT_CTOR & 0;
        for (FSym f : c.recordComponents) {
          ctor.params.add(f.type);
          ctor.paramNames = ctor.paramNames == null ? new ArrayList<String>() : ctor.paramNames;
          ctor.paramNames.add(f.name);
          if (f.tree instanceof N.Variable && ((N.Variable) f.tree).varargs) {
            ctor.flags |= Sym.VARARGS;
          }
        }
      }
      c.addMethod(ctor);
    }
    if (c.isEnum()) {
      MSym values = new MSym();
      values.name = "values";
      values.flags = Sym.PUBLIC | Sym.STATIC | Sym.GENERATED_CTOR;
      values.ret = Ty.array(c.erasedType());
      c.addMethod(values);
      MSym valueOf = new MSym();
      valueOf.name = "valueOf";
      valueOf.flags = Sym.PUBLIC | Sym.STATIC | Sym.GENERATED_CTOR;
      valueOf.ret = c.erasedType();
      valueOf.params.add(st.string);
      valueOf.paramNames = new ArrayList<String>();
      valueOf.paramNames.add("name");
      c.addMethod(valueOf);
    }
    if (c.isRecord()) {
      for (FSym f : c.recordComponents) {
        boolean has = false;
        for (MSym m : c.methodsNamed(f.name)) {
          if (m.params.isEmpty()) {
            has = true;
          }
        }
        if (!has) {
          MSym acc = new MSym();
          acc.name = f.name;
          acc.flags = Sym.PUBLIC | Sym.GENERATED_CTOR;
          acc.ret = f.type;
          acc.origin = f;
          c.addMethod(acc);
        }
      }
      addRecordObjectMethod(c, "toString", st.string, new Ty[0]);
      addRecordObjectMethod(c, "hashCode", Ty.tInt, new Ty[0]);
      addRecordObjectMethod(c, "equals", Ty.tBoolean, new Ty[] {st.object});
    }
  }

  private void addRecordObjectMethod(CSym c, String name, Ty ret, Ty[] params) {
    for (MSym m : c.methodsNamed(name)) {
      if (m.params.size() == params.length) {
        return;
      }
    }
    MSym m = new MSym();
    m.name = name;
    m.flags = Sym.PUBLIC | Sym.FINAL | Sym.GENERATED_CTOR;
    m.ret = ret;
    for (Ty p : params) {
      m.params.add(p);
    }
    c.addMethod(m);
  }

  /* ---------- imports ---------- */

  void resolveImports(Env.CuEnv ce) {
    if (ce.resolved) {
      return;
    }
    ce.resolved = true;
    Env.CuEnv saveCu = ce;
    log.cu = ce.tree;
    for (ImportTree imp : ce.tree.getImports()) {
      Tree q = imp.getQualifiedIdentifier();
      String full = q.toString();
      if (imp.isStatic()) {
        int dot = full.lastIndexOf('.');
        String clsName = full.substring(0, dot);
        String member = full.substring(dot + 1);
        CSym c = qualifiedClass(clsName);
        if (c == null) {
          log.error(imp, "compiler.err.cant.resolve.location", "cannot find symbol\n  symbol:   class "
              + clsName.substring(clsName.lastIndexOf('.') + 1) + "\n  location: package "
              + (clsName.contains(".") ? clsName.substring(0, clsName.lastIndexOf('.')) : clsName));
          continue;
        }
        if (member.equals("*")) {
          ce.staticOnDemand.add(c);
        } else {
          List<CSym> l = ce.staticSingle.get(member);
          if (l == null) {
            l = new ArrayList<CSym>();
            ce.staticSingle.put(member, l);
          }
          l.add(c);
        }
        continue;
      }
      if (full.endsWith(".*")) {
        String base = full.substring(0, full.length() - 2);
        CSym c = qualifiedClass(base);
        if (c != null) {
          ce.onDemandClasses.add(c);
        } else {
          ce.onDemandPkgs.add(base);
        }
      } else {
        CSym c = qualifiedClass(full);
        if (c == null) {
          int dot = full.lastIndexOf('.');
          log.error(imp, "compiler.err.cant.resolve.location", "cannot find symbol\n  symbol:   class "
              + full.substring(dot + 1) + "\n  location: package " + (dot < 0 ? "" : full.substring(0, dot)));
          continue;
        }
        ce.single.put(c.name, c);
      }
    }
  }

  /** Resolves a dotted name to a class, trying package prefixes first and nested classes after. */
  CSym qualifiedClass(String dotted) {
    String[] parts = dotted.split("\\.");
    for (int split = parts.length; split >= 1; split--) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < split; i++) {
        if (i > 0) {
          sb.append('/');
        }
        sb.append(parts[i]);
      }
      CSym c = st.lookup(sb.toString());
      if (c == null) {
        continue;
      }
      boolean ok = true;
      for (int i = split; i < parts.length; i++) {
        CSym m = memberClassOf(c, parts[i]);
        if (m == null) {
          ok = false;
          break;
        }
        c = m;
      }
      if (ok) {
        return c;
      }
    }
    return null;
  }

  /* ---------- type lookup ---------- */

  CSym memberClassOf(CSym c, String name) {
    return memberClassOf(c, name, new LinkedHashSet<CSym>());
  }

  private CSym memberClassOf(CSym c, String name, Set<CSym> seen) {
    if (!seen.add(c)) {
      return null;
    }
    CSym m = c.memberClasses.get(name);
    if (m != null) {
      return m;
    }
    if (c.reflect != null) {
      m = st.lookup(c.internal + "$" + name);
      if (m != null && m.outerClass == c) {
        c.memberClasses.put(name, m);
        return m;
      }
    }
    if (c.headerState == 1) {
      return null;
    }
    Ty sc = c.superclass();
    if (sc != null && sc.tag == Ty.CLASS) {
      m = memberClassOf(sc.sym, name, seen);
      if (m != null) {
        return m;
      }
    }
    for (Ty i : c.interfaces()) {
      if (i.tag == Ty.CLASS) {
        m = memberClassOf(i.sym, name, seen);
        if (m != null) {
          return m;
        }
      }
    }
    return null;
  }

  /** Finds a type named name visible from env: a class or type variable. */
  Sym findType(Env env, String name) {
    for (Env e = env; e != null; e = e.outer) {
      if (e.localClasses != null) {
        CSym c = e.localClasses.get(name);
        if (c != null) {
          return c;
        }
      }
      if (e.tvars != null) {
        TVSym t = e.tvars.get(name);
        if (t != null) {
          return t;
        }
      }
      if (e.isClassEnv) {
        CSym c = e.cls;
        if (c.name.equals(name) && !c.anon) {
          return c;
        }
        for (TVSym t : c.typeParams()) {
          if (t.name.equals(name)) {
            return t;
          }
        }
        CSym m = memberClassOf(c, name);
        if (m != null) {
          return m;
        }
      }
    }
    return findTopType(env.cu, name);
  }

  CSym findTopType(Env.CuEnv ce, String name) {
    resolveImports(ce);
    CSym c = ce.single.get(name);
    if (c != null) {
      return c;
    }
    String pkgPath = ce.pkg.isEmpty() ? "" : ce.pkg.replace('.', '/') + "/";
    c = st.lookup(pkgPath + name);
    if (c != null) {
      return c;
    }
    for (String p : ce.onDemandPkgs) {
      c = st.lookup(p.replace('.', '/') + "/" + name);
      if (c != null) {
        return c;
      }
    }
    for (CSym oc : ce.onDemandClasses) {
      c = memberClassOf(oc, name);
      if (c != null) {
        return c;
      }
    }
    List<CSym> sl = ce.staticSingle.get(name);
    if (sl != null) {
      for (CSym oc : sl) {
        c = memberClassOf(oc, name);
        if (c != null) {
          return c;
        }
      }
    }
    for (CSym oc : ce.staticOnDemand) {
      c = memberClassOf(oc, name);
      if (c != null) {
        return c;
      }
    }
    return st.lookup("java/lang/" + name);
  }

  /* ---------- resolving type trees ---------- */

  Ty resolveType(Tree t, Env env) {
    if (t == null) {
      return Ty.tError;
    }
    switch (t.getKind()) {
      case PRIMITIVE_TYPE: {
        Ty p = Ty.primByName(((PrimitiveTypeTree) t).getPrimitiveTypeKind().toString().toLowerCase());
        return p == null ? Ty.tError : p;
      }
      case IDENTIFIER: {
        String n = ((IdentifierTree) t).getName().toString();
        Sym s = findType(env, n);
        if (s == null) {
          if (headerPhase) {
            log.error(t, "compiler.err.cant.resolve", "cannot find symbol\n  symbol: class " + n);
          } else {
            log.error(t, "compiler.err.cant.resolve.location", "cannot find symbol\n  symbol:   class " + n
                + "\n  location: " + locationOf(env));
          }
          return Ty.tError;
        }
        if (s instanceof TVSym) {
          return ((TVSym) s).type;
        }
        CSym c = (CSym) s;
        return Ty.cls(c);
      }
      case MEMBER_SELECT: {
        MemberSelectTree ms = (MemberSelectTree) t;
        Object q = resolveQualifier(ms.getExpression(), env);
        String n = ms.getIdentifier().toString();
        if (q instanceof CSym) {
          CSym m = memberClassOf((CSym) q, n);
          if (m == null) {
            log.error(t, "compiler.err.cant.resolve.location", "cannot find symbol\n  symbol:   class " + n
                + "\n  location: class " + ((CSym) q).fullName());
            return Ty.tError;
          }
          return Ty.cls(m);
        }
        if (q instanceof String) {
          String pkg = (String) q;
          CSym c = st.lookup(pkg.replace('.', '/') + "/" + n);
          if (c == null) {
            log.error(t, "compiler.err.cant.resolve.location", "cannot find symbol\n  symbol:   class " + n
                + "\n  location: package " + pkg);
            return Ty.tError;
          }
          return Ty.cls(c);
        }
        return Ty.tError;
      }
      case PARAMETERIZED_TYPE: {
        ParameterizedTypeTree pt = (ParameterizedTypeTree) t;
        Ty base = resolveType(pt.getType(), env);
        if (base.tag != Ty.CLASS) {
          return base;
        }
        List<? extends Tree> as = pt.getTypeArguments();
        if (as.isEmpty()) {
          return Ty.cls(base.sym, diamondMarker());
        }
        Ty[] args = new Ty[as.size()];
        for (int i = 0; i < args.length; i++) {
          args[i] = resolveType(as.get(i), env);
        }
        return Ty.cls(base.sym, args);
      }
      case ARRAY_TYPE:
        return Ty.array(resolveType(((ArrayTypeTree) t).getType(), env));
      case ANNOTATED_TYPE:
        return resolveType(((AnnotatedTypeTree) t).getUnderlyingType(), env);
      case UNBOUNDED_WILDCARD:
        return Ty.wild(0, null);
      case EXTENDS_WILDCARD:
        return Ty.wild(1, resolveType(((WildcardTree) t).getBound(), env));
      case SUPER_WILDCARD:
        return Ty.wild(2, resolveType(((WildcardTree) t).getBound(), env));
      case UNION_TYPE: {
        List<Ty> alts = new ArrayList<Ty>();
        for (Tree a : ((UnionTypeTree) t).getTypeAlternatives()) {
          alts.add(resolveType(a, env));
        }
        return tx.lub(alts);
      }
      case INTERSECTION_TYPE: {
        List<Ty> bs = new ArrayList<Ty>();
        for (Tree a : ((IntersectionTypeTree) t).getBounds()) {
          bs.add(resolveType(a, env));
        }
        return Ty.inter(bs);
      }
      default:
        log.error(t, "compiler.err.illegal.start.of.type", "illegal start of type");
        return Ty.tError;
    }
  }

  /** Marker arguments of a diamond: a single null-bound wildcard, recognized by identity. */
  static final Ty DIAMOND = Ty.wild(0, null);

  static Ty[] diamondMarker() {
    return new Ty[] {DIAMOND};
  }

  static boolean isDiamond(Ty t) {
    return t.tag == Ty.CLASS && t.args.length == 1 && t.args[0] == DIAMOND;
  }

  /** Resolves a qualifier of a type name to a class (CSym) or a package name (String). */
  Object resolveQualifier(Tree q, Env env) {
    if (q.getKind() == Tree.Kind.IDENTIFIER) {
      String n = ((IdentifierTree) q).getName().toString();
      Sym s = findType(env, n);
      if (s instanceof CSym) {
        return s;
      }
      return n;
    }
    if (q.getKind() == Tree.Kind.MEMBER_SELECT) {
      MemberSelectTree ms = (MemberSelectTree) q;
      Object b = resolveQualifier(ms.getExpression(), env);
      String n = ms.getIdentifier().toString();
      if (b instanceof CSym) {
        CSym m = memberClassOf((CSym) b, n);
        return m != null ? (Object) m : "";
      }
      String pkg = (String) b;
      CSym c = st.lookup(pkg.replace('.', '/') + "/" + n);
      if (c != null) {
        return c;
      }
      return pkg + "." + n;
    }
    if (q.getKind() == Tree.Kind.PARAMETERIZED_TYPE || q.getKind() == Tree.Kind.ANNOTATED_TYPE) {
      Ty t = resolveType(q, env);
      return t.tag == Ty.CLASS ? (Object) t.sym : "";
    }
    return "";
  }

  String locationOf(Env env) {
    CSym c = env.cls;
    if (c == null) {
      return "package";
    }
    if (c.anon) {
      return "class <anonymous " + (c.interfaces.isEmpty() ? c.superclass : c.interfaces.get(0)) + ">";
    }
    return (c.isInterface() ? "interface " : "class ") + c.fullName();
  }
}
