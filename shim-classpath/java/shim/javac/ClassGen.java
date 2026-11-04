/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.*;
import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Class-level code generation. */
final class ClassGen {
  final Gen g;
  final CSym c;
  final Attr a;
  final Map<String, byte[]> extraClasses = new LinkedHashMap<String, byte[]>();
  final List<Runnable> pending = new ArrayList<Runnable>();
  final Set<CSym> referenced = new LinkedHashSet<CSym>();
  ClassBuilder cb;
  int lambdaCount;
  final Map<Tree, String> lambdaNames = new java.util.IdentityHashMap<Tree, String>();
  final Map<String, Integer> lambdaCounts = new LinkedHashMap<String, Integer>();
  int refCount;
  final List<SerialLambda> serialLambdas = new ArrayList<SerialLambda>();

  static final class SerialLambda {
    java.lang.constant.DynamicCallSiteDesc site;
    String implName;
    String implClass;
    String implDesc;
    java.lang.constant.DirectMethodHandleDesc.Kind implKind;
    String ifaceClass;
    String ifaceMethod;
    String ifaceSig;
    String invDesc;
  }
  boolean needsAssertField;
  final Set<String> declaredMethods = new LinkedHashSet<String>();
  final List<FSym> staticInitFields = new ArrayList<FSym>();

  ClassGen(Gen g, CSym c) {
    this.g = g;
    this.c = c;
    this.a = g.a;
  }

  byte[] generate() {
    return g.cf.build(ClassDesc.ofInternalName(c.internal), new java.util.function.Consumer<ClassBuilder>() {
      public void accept(ClassBuilder b) {
        cb = b;
        build();
      }
    });
  }

  ClassDesc cd(CSym k) {
    noteNested(k);
    return ClassDesc.ofInternalName(k.internal);
  }

  ClassDesc cd(Ty t) {
    Ty e = TypeOps.erasure(t, g.st.object);
    noteType(e);
    return ClassDesc.ofDescriptor(e.desc());
  }

  void noteType(Ty e) {
    while (e.tag == Ty.ARRAY) {
      e = e.elem;
    }
    if (e.tag == Ty.CLASS) {
      noteNested(e.sym);
    }
  }

  void noteNested(CSym k) {
    if (k.outerClass != null || k.local || k.anon) {
      referenced.add(k);
      if (k.outerClass != null) {
        noteNested(k.outerClass);
      }
    }
  }

  /* ---------- class structure ---------- */

  private void build() {
    cb.withVersion(g.major, 0);
    int flags = c.flags & (Sym.PUBLIC | Sym.FINAL | Sym.ABSTRACT | Sym.INTERFACE | Sym.ANNOTATION | Sym.ENUM);
    if ((c.flags & Sym.PROTECTED) != 0) {
      flags |= Sym.PUBLIC;
    }
    if (!c.isInterface()) {
      flags |= 0x20;
    }
    if (c.isEnum()) {
      flags |= Sym.ENUM;
      if (hasAbstractMethod() && !c.anon) {
        flags |= Sym.ABSTRACT;
      }
    }
    if (c.anon) {
      flags &= ~(Sym.PUBLIC | Sym.FINAL);
      flags |= (c.superclass() != null && c.superclass().sym.isEnum()) ? Sym.ENUM : 0;
      flags = c.superclass() != null && c.superclass().sym.isEnum() ? (flags | Sym.FINAL) & ~Sym.ENUM | Sym.ENUM
          : flags & ~Sym.FINAL;
    }
    cb.withFlags(flags);
    Ty sc = c.superclass();
    cb.withSuperclass(ClassDesc.ofInternalName(c.isInterface() || sc == null ? "java/lang/Object" : sc.sym.internal));
    List<ClassDesc> ifs = new ArrayList<ClassDesc>();
    for (Ty i : c.interfaces()) {
      ifs.add(cd(i));
    }
    cb.withInterfaceSymbols(ifs);
    String sig = Sigs.classSig(c);
    if (sig != null) {
      cb.with(SignatureAttribute.of(cb.constantPool().utf8Entry(sig)));
    }
    c.members();
    assignLambdaNames();
    genFields();
    genMethods();
    genBridges();
    genClinit();
    while (!pending.isEmpty()) {
      Runnable r = pending.remove(0);
      r.run();
    }
    genDeserialize();
    if (needsAssertField) {
      cb.withField("$assertionsDisabled", ClassDesc.ofDescriptor("Z"), 0x1018);
    }
    genAttributes();
  }

  private void assignLambdaNames() {
    final Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
    for (Tree m : c.tree.getMembers()) {
      String owner;
      if (m instanceof MethodTree) {
        owner = ((MethodTree) m).getName().contentEquals("<init>") ? "new" : ((MethodTree) m).getName().toString();
      } else if (m instanceof VariableTree) {
        FSym f = (FSym) g.enter.symOf.get(m);
        owner = f != null && f.isStatic() ? "static" : "new";
      } else if (m instanceof BlockTree) {
        owner = ((BlockTree) m).isStatic() ? "static" : "new";
      } else {
        continue;
      }
      final String o = owner;
      new com.sun.source.util.TreeScanner<Void, Void>() {
        public Void visitClass(ClassTree t, Void p) {
          return null;
        }

        public Void visitNewClass(NewClassTree t, Void p) {
          scan(t.getEnclosingExpression(), p);
          scan(t.getArguments(), p);
          return null;
        }

        public Void visitLambdaExpression(LambdaExpressionTree t, Void p) {
          Integer k = counts.get(o);
          int v = k == null ? 0 : k.intValue();
          counts.put(o, Integer.valueOf(v + 1));
          lambdaNames.put(t, "lambda$" + o + "$" + v);
          return super.visitLambdaExpression(t, p);
        }
      }.scan(m, null);
    }
  }

  private void genDeserialize() {
    if (serialLambdas.isEmpty()) {
      return;
    }
    final ClassDesc sl = ClassDesc.ofInternalName("java/lang/invoke/SerializedLambda");
    cb.withMethodBody("$deserializeLambda$", MethodTypeDesc.ofDescriptor("(Ljava/lang/invoke/SerializedLambda;)"
        + "Ljava/lang/Object;"), 0x100a, new java.util.function.Consumer<CodeBuilder>() {
      public void accept(CodeBuilder b) {
        for (SerialLambda s : serialLambdas) {
          java.lang.classfile.Label next = b.newLabel();
          String[][] checks = {{"getImplMethodName", s.implName}, {"getFunctionalInterfaceClass",
              s.ifaceClass.substring(1, s.ifaceClass.length() - 1)}, {"getFunctionalInterfaceMethodName",
              s.ifaceMethod}, {"getFunctionalInterfaceMethodSignature", s.ifaceSig}, {"getImplClass", s.implClass},
              {"getImplMethodSignature", s.implDesc}};
          for (String[] ch : checks) {
            b.aload(0);
            b.invokevirtual(sl, ch[0], MethodTypeDesc.ofDescriptor("()Ljava/lang/String;"));
            b.ldc(ch[1]);
            b.invokevirtual(ClassDesc.ofInternalName("java/lang/Object"), "equals",
                MethodTypeDesc.ofDescriptor("(Ljava/lang/Object;)Z"));
            b.ifeq(next);
          }
          b.aload(0);
          b.invokevirtual(sl, "getImplMethodKind", MethodTypeDesc.ofDescriptor("()I"));
          b.loadConstant(s.implKind.refKind);
          b.if_icmpne(next);
          MethodTypeDesc inv = MethodTypeDesc.ofDescriptor(s.invDesc);
          for (int i = 0; i < inv.parameterCount(); i++) {
            b.aload(0);
            b.loadConstant(i);
            b.invokevirtual(sl, "getCapturedArg", MethodTypeDesc.ofDescriptor("(I)Ljava/lang/Object;"));
            ClassDesc p = inv.parameterType(i);
            if (p.isPrimitive()) {
              String box = boxOf(p.descriptorString());
              b.checkcast(ClassDesc.ofInternalName("java/lang/" + box));
              b.invokevirtual(ClassDesc.ofInternalName("java/lang/" + box), prim(p.descriptorString()) + "Value",
                  MethodTypeDesc.ofDescriptor("()" + p.descriptorString()));
            } else {
              b.checkcast(p);
            }
          }
          b.invokedynamic(s.site);
          b.areturn();
          b.labelBinding(next);
        }
        b.new_(ClassDesc.ofInternalName("java/lang/IllegalArgumentException"));
        b.dup();
        b.ldc("Invalid lambda deserialization");
        b.invokespecial(ClassDesc.ofInternalName("java/lang/IllegalArgumentException"), "<init>",
            MethodTypeDesc.ofDescriptor("(Ljava/lang/String;)V"));
        b.athrow();
      }
    });
  }

  private static String boxOf(String d) {
    switch (d.charAt(0)) {
      case 'I':
        return "Integer";
      case 'J':
        return "Long";
      case 'Z':
        return "Boolean";
      case 'B':
        return "Byte";
      case 'C':
        return "Character";
      case 'S':
        return "Short";
      case 'F':
        return "Float";
      default:
        return "Double";
    }
  }

  private static String prim(String d) {
    switch (d.charAt(0)) {
      case 'I':
        return "int";
      case 'J':
        return "long";
      case 'Z':
        return "boolean";
      case 'B':
        return "byte";
      case 'C':
        return "char";
      case 'S':
        return "short";
      case 'F':
        return "float";
      default:
        return "double";
    }
  }

  private boolean hasAbstractMethod() {
    for (MSym m : c.allMethods) {
      if (m.isAbstract()) {
        return true;
      }
    }
    return false;
  }

  /* ---------- fields ---------- */

  private void genFields() {
    for (FSym f : c.fields.values()) {
      int fl = f.flags & (Sym.PUBLIC | Sym.PRIVATE | Sym.PROTECTED | Sym.STATIC | Sym.FINAL | Sym.VOLATILE
          | Sym.TRANSIENT | Sym.ENUM);
      final FSym ff = f;
      Object cv = f.isStatic() && f.isFinal() ? a.constantOf(f) : null;
      if (f.tree != null && f.tree.getInitializer() == null) {
        cv = null;
      }
      final Object constant = cv;
      noteType(TypeOps.erasure(f.type, g.st.object));
      String sig = Sigs.fieldSig(f);
      final String fsig = sig;
      cb.withField(f.name, cd(f.type), new java.util.function.Consumer<FieldBuilder>() {
        public void accept(FieldBuilder fb) {
          fb.withFlags(fl0(ff));
          if (constant != null && (ff.type.isPrim() || ff.type.isString())) {
            fb.with(ConstantValueAttribute.of(constDesc(constant, ff.type)));
          }
          if (fsig != null) {
            fb.with(SignatureAttribute.of(cb.constantPool().utf8Entry(fsig)));
          }
          AnnotationGen.fieldAnnotations(ClassGen.this, ff, fb);
        }
      });
      if (!(constant != null && (f.type.isPrim() || f.type.isString())) && f.isStatic() && f.tree != null
          && f.tree.getInitializer() != null) {
        staticInitFields.add(f);
      }
    }
    if (Captures.needsOuterThis(c) && c.outerClass != null) {
      cb.withField(Gen.outerFieldName(c), cd(c.outerClass), 0x1010);
    }
    if (c.local || c.anon) {
      for (VSym v : g.caps.of(c)) {
        cb.withField("val$" + v.name, cd(TypeOps.resolve(v.type)), 0x1010);
      }
    }
    if (c.isEnum()) {
      cb.withField("$VALUES", ClassDesc.ofDescriptor("[L" + c.internal + ";"), 0x101a);
    }
  }

  private static int fl0(FSym f) {
    return f.flags & (Sym.PUBLIC | Sym.PRIVATE | Sym.PROTECTED | Sym.STATIC | Sym.FINAL | Sym.VOLATILE
        | Sym.TRANSIENT | Sym.ENUM);
  }

  static java.lang.constant.ConstantDesc constDesc(Object v, Ty t) {
    switch (t.tag) {
      case Ty.BOOLEAN:
        return Integer.valueOf(((Boolean) v).booleanValue() ? 1 : 0);
      case Ty.CHAR:
        return Integer.valueOf(v instanceof Character ? ((Character) v).charValue() : ((Number) v).intValue());
      case Ty.BYTE:
      case Ty.SHORT:
      case Ty.INT:
        return Integer.valueOf(ConstFold.intOf(v));
      case Ty.LONG:
        return Long.valueOf(ConstFold.longOf(v));
      case Ty.FLOAT:
        return Float.valueOf(ConstFold.floatOf(v));
      case Ty.DOUBLE:
        return Double.valueOf(ConstFold.doubleOf(v));
      default:
        return (String) v;
    }
  }

  /* ---------- methods ---------- */

  private static int methodFlags(MSym m, CSym c) {
    int fl = m.flags & (Sym.PUBLIC | Sym.PRIVATE | Sym.PROTECTED | Sym.STATIC | Sym.FINAL | Sym.SYNCHRONIZED
        | Sym.NATIVE | Sym.ABSTRACT | Sym.STRICT);
    if (m.isVarargs()) {
      fl |= 0x80;
    }
    return fl;
  }

  private void genMethods() {
    for (MSym m : new ArrayList<MSym>(c.allMethods)) {
      genMethod(m);
    }
    if (c.anon) {
      genAnonCtor();
    }
    if (c.isEnum()) {
      genEnumHelpers();
    }
  }

  void declare(String name, String desc) {
    declaredMethods.add(name + desc);
  }

  private void genMethod(final MSym m) {
    if (m.isCtor() && c.anon) {
      return;
    }
    final String desc = g.methodDesc(m);
    declare(m.name, desc);
    int fl = methodFlags(m, c);
    final MethodTree tree = m.tree;
    final boolean generated = tree == null;
    boolean isAbstract = (fl & (Sym.ABSTRACT | Sym.NATIVE)) != 0;
    if (m.isCtor() && c.isEnum()) {
      fl = (fl & ~(Sym.PUBLIC | Sym.PROTECTED)) | Sym.PRIVATE;
    }
    if (c.isRecord() && generated && !m.isCtor()) {
      fl |= Sym.FINAL & (m.name.equals("toString") || m.name.equals("hashCode") || m.name.equals("equals")
          ? Sym.FINAL : 0);
      if (!(m.name.equals("toString") || m.name.equals("hashCode") || m.name.equals("equals"))) {
        fl &= ~Sym.FINAL;
      }
    }
    final int flags = fl;
    cb.withMethod(m.name, MethodTypeDesc.ofDescriptor(desc), flags, new java.util.function.Consumer<MethodBuilder>() {
      public void accept(MethodBuilder mb) {
        String sig = Sigs.methodSig(m, c);
        if (sig != null) {
          mb.with(SignatureAttribute.of(cb.constantPool().utf8Entry(sig)));
        }
        if (!m.thrown.isEmpty()) {
          List<ClassDesc> th = new ArrayList<ClassDesc>();
          for (Ty t : m.thrown) {
            th.add(cd(t));
          }
          mb.with(ExceptionsAttribute.ofSymbols(th));
        }
        AnnotationGen.methodAnnotations(ClassGen.this, m, mb);
        if (isAbstractFlags(flags)) {
          return;
        }
        mb.withCode(new java.util.function.Consumer<CodeBuilder>() {
          public void accept(CodeBuilder b) {
            Code code = new Code(ClassGen.this, b, m);
            code.genMethod(m, tree);
          }
        });
      }
    });
  }

  private static boolean isAbstractFlags(int fl) {
    return (fl & (Sym.ABSTRACT | Sym.NATIVE)) != 0;
  }

  private void genAnonCtor() {
    final Gen.Layout l = g.layout(c, null);
    final String desc = l.desc();
    declare("<init>", desc);
    cb.withMethod("<init>", MethodTypeDesc.ofDescriptor(desc), 0, new java.util.function.Consumer<MethodBuilder>() {
      public void accept(MethodBuilder mb) {
        mb.withCode(new java.util.function.Consumer<CodeBuilder>() {
          public void accept(CodeBuilder b) {
            Code code = new Code(ClassGen.this, b, null);
            code.genAnonCtor(l);
          }
        });
      }
    });
  }

  private void genEnumHelpers() {
    final String self = "L" + c.internal + ";";
    final ClassDesc arr = ClassDesc.ofDescriptor("[" + self);
    cb.withMethodBody("$values", MethodTypeDesc.ofDescriptor("()[" + self), 0x100a, new java.util.function.Consumer<CodeBuilder>() {
      public void accept(CodeBuilder b) {
        List<FSym> consts = enumConstants(c);
        b.loadConstant(consts.size());
        b.anewarray(ClassDesc.ofInternalName(c.internal));
        for (int i = 0; i < consts.size(); i++) {
          b.dup();
          b.loadConstant(i);
          b.getstatic(ClassDesc.ofInternalName(c.internal), consts.get(i).name, ClassDesc.ofDescriptor(self));
          b.aastore();
        }
        b.areturn();
      }
    });
  }

  static List<FSym> enumConstants(CSym c) {
    List<FSym> out = new ArrayList<FSym>();
    c.members();
    for (FSym f : c.fields.values()) {
      if ((f.flags & Sym.ENUM) != 0) {
        out.add(f);
      }
    }
    return out;
  }

  /* ---------- static initializer ---------- */

  private void genClinit() {
    boolean need = !staticInitFields.isEmpty() || c.isEnum();
    for (Tree m : c.tree.getMembers()) {
      if (m instanceof BlockTree && ((BlockTree) m).isStatic()) {
        need = true;
      }
    }
    if (!need && !needsAssertFieldInit()) {
      return;
    }
    declare("<clinit>", "()V");
    cb.withMethodBody("<clinit>", MethodTypeDesc.ofDescriptor("()V"), 0x8, new java.util.function.Consumer<CodeBuilder>() {
      public void accept(CodeBuilder b) {
        Code code = new Code(ClassGen.this, b, null);
        code.genClinit();
      }
    });
  }

  private boolean needsAssertFieldInit() {
    return AssertScan.hasAssert(a, c);
  }

  /* ---------- bridges ---------- */

  private void genVisibilityBridges() {
    if (c.isInterface() || (c.flags & Sym.PUBLIC) == 0 || c.anon || c.local) {
      return;
    }
    Set<String> seen = new LinkedHashSet<String>();
    for (Ty s = c.superclass(); s != null && s.tag == Ty.CLASS && (s.sym.flags & Sym.PUBLIC) == 0
        && s.sym != g.st.objectSym; s = s.sym.superclass()) {
      s.sym.members();
      for (final MSym m : new ArrayList<MSym>(s.sym.allMethods)) {
        if ((m.flags & Sym.PUBLIC) == 0 || m.isStatic() || m.isCtor() || m.isFinal() || m.isAbstract()) {
          continue;
        }
        final String key = m.name + m.erasedDesc();
        if (declaredMethods.contains(key) || !seen.add(key)) {
          continue;
        }
        boolean overridden = false;
        for (MSym o : c.methodsNamed(m.name)) {
          if (o.params.size() == m.params.size()) {
            overridden = true;
          }
        }
        if (overridden) {
          continue;
        }
        declare(m.name, m.erasedDesc());
        final ClassDesc sup = ClassDesc.ofInternalName(c.superclass().sym.internal);
        cb.withMethod(m.name, MethodTypeDesc.ofDescriptor(m.erasedDesc()), 0x1041 | (m.isVarargs() ? 0x80 : 0),
            new java.util.function.Consumer<MethodBuilder>() {
              public void accept(MethodBuilder mb) {
                mb.withCode(new java.util.function.Consumer<CodeBuilder>() {
                  public void accept(CodeBuilder b) {
                    MethodTypeDesc mtd = MethodTypeDesc.ofDescriptor(m.erasedDesc());
                    b.aload(0);
                    int slot = 1;
                    for (int i = 0; i < mtd.parameterCount(); i++) {
                      TypeKind k = TypeKind.from(mtd.parameterType(i));
                      b.loadLocal(k, slot);
                      slot += k.slotSize();
                    }
                    b.invokespecial(sup, m.name, mtd);
                    b.return_(TypeKind.from(mtd.returnType()));
                  }
                });
              }
            });
      }
    }
  }

  private void genBridges() {
    genVisibilityBridges();
    Set<String> done = new LinkedHashSet<String>();
    for (final MSym m : new ArrayList<MSym>(c.allMethods)) {
      if (m.isStatic() || m.isPrivate() || m.isCtor() || m.tree == null && !c.isRecord()) {
        continue;
      }
      if (m.tree == null && (m.flags & Sym.GENERATED_CTOR) != 0 && !c.isRecord()) {
        continue;
      }
      Set<CSym> seen = new LinkedHashSet<CSym>();
      collectBridges(m, c.type(), seen, done);
    }
  }

  private void collectBridges(final MSym m, Ty from, Set<CSym> seen, Set<String> done) {
    List<Ty> supers = new ArrayList<Ty>();
    Ty sc = from.sym.superclass();
    if (sc != null) {
      supers.add(sc);
    }
    supers.addAll(from.sym.interfaces());
    for (Ty s : supers) {
      if (s.tag != Ty.CLASS || !seen.add(s.sym)) {
        continue;
      }
      Ty sup = TypeOps.asSuper(c.type(), s.sym);
      s.sym.members();
      for (final MSym o : s.sym.methodsNamed(m.name)) {
        if (o.isStatic() || o.isPrivate() || o.params.size() != m.params.size() || o == m) {
          continue;
        }
        if (!g.a.res.accessible(o, s.sym, enterEnv())) {
          continue;
        }
        Tx.MType mt = g.tx.memberType(o, c.type());
        boolean same = true;
        for (int i = 0; i < m.params.size(); i++) {
          Ty p1 = g.tx.erasure(mt.params.get(i));
          Ty p2 = g.tx.erasure(m.params.get(i));
          if (!p1.desc().equals(p2.desc())) {
            same = false;
          }
        }
        if (!same) {
          continue;
        }
        String od = o.erasedDesc();
        String md = m.erasedDesc();
        if (od.equals(md)) {
          continue;
        }
        String key = o.name + od;
        if (declaredMethods.contains(key) || !done.add(key)) {
          continue;
        }
        genBridge(m, o, od);
      }
      collectBridges(m, s, seen, done);
    }
  }

  private Env enterEnv() {
    return g.enter.classEnv(c);
  }

  private void genBridge(final MSym target, final MSym bridged, final String desc) {
    declare(bridged.name, desc);
    int fl = (target.flags & (Sym.PUBLIC | Sym.PROTECTED)) | 0x1040;
    if (c.isInterface()) {
      fl |= 0;
    }
    cb.withMethod(bridged.name, MethodTypeDesc.ofDescriptor(desc), fl, new java.util.function.Consumer<MethodBuilder>() {
      public void accept(MethodBuilder mb) {
        mb.withCode(new java.util.function.Consumer<CodeBuilder>() {
          public void accept(CodeBuilder b) {
            MethodTypeDesc mtd = MethodTypeDesc.ofDescriptor(desc);
            int slot = 1;
            b.aload(0);
            for (int i = 0; i < mtd.parameterCount(); i++) {
              ClassDesc p = mtd.parameterType(i);
              TypeKind k = TypeKind.from(p);
              b.loadLocal(k, slot);
              slot += k.slotSize();
              ClassDesc want = ClassDesc.ofDescriptor(g.tx.erasure(target.params.get(i)).desc());
              if (!p.equals(want) && !p.isPrimitive()) {
                b.checkcast(want);
              }
            }
            MethodTypeDesc tm = MethodTypeDesc.ofDescriptor(target.erasedDesc());
            if (c.isInterface()) {
              b.invokeinterface(ClassDesc.ofInternalName(c.internal), target.name, tm);
            } else {
              b.invokevirtual(ClassDesc.ofInternalName(c.internal), target.name, tm);
            }
            b.return_(TypeKind.from(mtd.returnType()));
          }
        });
      }
    });
  }

  /* ---------- attributes ---------- */

  private void genAttributes() {
    String file = null;
    if (c.cu != null && c.cu.getSourceFile() != null) {
      String n = c.cu.getSourceFile().getName();
      int s = Math.max(n.lastIndexOf('/'), n.lastIndexOf('\\'));
      file = s >= 0 ? n.substring(s + 1) : n;
    }
    if (file != null) {
      cb.with(SourceFileAttribute.of(file));
    }
    CSym top = c.outermost();
    Set<CSym> inner = new LinkedHashSet<CSym>(referenced);
    if (c.outerClass != null || c.local || c.anon) {
      inner.add(c);
      noteNested(c);
    }
    for (CSym k : g.a.enter.sourceClasses) {
      if (k.outerClass == c && !k.local && !k.anon || (k.outerClass == c)) {
        inner.add(k);
      }
    }
    for (CSym k : new ArrayList<CSym>(inner)) {
      for (CSym o = k.outerClass; o != null; o = o.outerClass) {
        if (o.outerClass != null || o.local || o.anon) {
          inner.add(o);
        }
      }
    }
    List<InnerClassInfo> infos = new ArrayList<InnerClassInfo>();
    for (CSym k : inner) {
      int fl = k.flags & (Sym.PUBLIC | Sym.PRIVATE | Sym.PROTECTED | Sym.STATIC | Sym.FINAL | Sym.INTERFACE
          | Sym.ABSTRACT | Sym.ANNOTATION | Sym.ENUM);
      if (k.anon) {
        fl &= ~(Sym.FINAL);
        fl &= ~Sym.STATIC;
      }
      if (k.isRecord()) {
        fl |= Sym.STATIC | Sym.FINAL;
      }
      if (k.anon && k.superclass != null && k.superclass.tag == Ty.CLASS && k.superclass.sym.isEnum()) {
        fl = Sym.ENUM | Sym.FINAL;
      } else if (k.isEnum()) {
        fl |= Sym.STATIC;
        if (!k.anon && hasAbstract(k)) {
          fl |= Sym.ABSTRACT;
        }
      }
      if (k.isInterface()) {
        fl |= Sym.STATIC | Sym.ABSTRACT;
      }
      Optional<ClassDesc> outer = k.local || k.anon || k.outerClass == null ? Optional.<ClassDesc>empty()
          : Optional.of(ClassDesc.ofInternalName(k.outerClass.internal));
      Optional<String> nm = k.anon ? Optional.<String>empty() : Optional.of(k.name);
      infos.add(InnerClassInfo.of(ClassDesc.ofInternalName(k.internal), outer, nm, fl));
    }
    if (!infos.isEmpty()) {
      cb.with(InnerClassesAttribute.of(infos));
    }
    if (c.local || c.anon) {
      Optional<String> mn = Optional.empty();
      Optional<MethodTypeDesc> md = Optional.empty();
      Sym em = c.enclosingMethod;
      if (em instanceof MSym && ((MSym) em).owner == c.outerClass) {
        MSym m = (MSym) em;
        mn = Optional.of(m.name);
        md = Optional.of(MethodTypeDesc.ofDescriptor(g.methodDesc(m)));
      }
      cb.with(EnclosingMethodAttribute.of(ClassDesc.ofInternalName(c.outerClass.internal), mn, md));
    }
    if (c == top) {
      List<ClassDesc> members = new ArrayList<ClassDesc>();
      for (CSym k : g.a.enter.sourceClasses) {
        if (k != top && k.outermost() == top) {
          members.add(ClassDesc.ofInternalName(k.internal));
        }
      }
      if (!members.isEmpty()) {
        cb.with(NestMembersAttribute.ofSymbols(members));
      }
    } else {
      cb.with(NestHostAttribute.of(ClassDesc.ofInternalName(top.internal)));
    }
    if (c.isRecord()) {
      List<RecordComponentInfo> comps = new ArrayList<RecordComponentInfo>();
      for (FSym f : c.recordComponents) {
        List<Attribute<?>> attrs = new ArrayList<Attribute<?>>();
        String sig = Sigs.fieldSig(f);
        if (sig != null) {
          attrs.add(SignatureAttribute.of(cb.constantPool().utf8Entry(sig)));
        }
        comps.add(RecordComponentInfo.of(f.name, cd(f.type), attrs));
      }
      cb.with(RecordAttribute.of(comps));
    }
    if ((c.flags & Sym.SEALED) != 0 || !c.permitted.isEmpty()) {
      List<ClassDesc> subs = new ArrayList<ClassDesc>();
      List<Ty> ps = c.permitted.isEmpty() ? a.stat.permitted(c) : c.permitted;
      for (Ty p : ps) {
        subs.add(cd(p));
      }
      if (!subs.isEmpty()) {
        cb.with(PermittedSubclassesAttribute.ofSymbols(subs));
      }
    }
    AnnotationGen.classAnnotations(this, c);
  }

  private boolean hasAbstract(CSym k) {
    k.members();
    for (MSym m : k.allMethods) {
      if (m.isAbstract()) {
        return true;
      }
    }
    return false;
  }
}
