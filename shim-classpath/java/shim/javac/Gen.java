/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.*;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassHierarchyResolver;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Generates class files from attributed trees. */
final class Gen {
  final Attr a;
  final Symtab st;
  final Tx tx;
  final Enter enter;
  final Captures caps;
  final Log log;
  final Map<String, byte[]> out = new LinkedHashMap<String, byte[]>();
  final ClassFile cf;
  int major = 69;
  boolean debugLines = true;

  Gen(Attr a, Log log) {
    this.a = a;
    this.st = a.st;
    this.tx = a.tx;
    this.enter = a.enter;
    this.log = log;
    this.caps = new Captures(a);
    final Symtab symtab = a.st;
    ClassHierarchyResolver resolver = new ClassHierarchyResolver() {
      public ClassHierarchyInfo getClassInfo(ClassDesc cd) {
        String d = cd.descriptorString();
        if (d.startsWith("[")) {
          return ClassHierarchyInfo.ofClass(java.lang.constant.ConstantDescs.CD_Object);
        }
        String internal = d.substring(1, d.length() - 1);
        CSym c = symtab.lookup(internal);
        if (c == null) {
          return ClassHierarchyInfo.ofClass(java.lang.constant.ConstantDescs.CD_Object);
        }
        if (c.isInterface()) {
          return ClassHierarchyInfo.ofInterface();
        }
        Ty sc = c.superclass();
        if (sc == null) {
          return ClassHierarchyInfo.ofClass(null);
        }
        return ClassHierarchyInfo.ofClass(ClassDesc.ofInternalName(sc.sym.internal));
      }
    };
    this.cf = ClassFile.of(ClassFile.StackMapsOption.GENERATE_STACK_MAPS,
        ClassFile.ClassHierarchyResolverOption.of(resolver),
        ClassFile.DeadCodeOption.PATCH_DEAD_CODE);
  }

  Map<String, byte[]> generate(List<CSym> classes) {
    for (int i = 0; i < classes.size(); i++) {
      CSym c = classes.get(i);
      if (!c.isSource || c.tree == null) {
        continue;
      }
      ClassGen g = new ClassGen(this, c);
      byte[] b = g.generate();
      out.put(c.internal, b);
      for (Map.Entry<String, byte[]> extra : g.extraClasses.entrySet()) {
        out.put(extra.getKey(), extra.getValue());
      }
    }
    return out;
  }

  void generatePackageInfos(List<? extends CompilationUnitTree> units) {
    for (CompilationUnitTree cu : units) {
      if (cu.getSourceFile() == null || !cu.getSourceFile().getName().endsWith("package-info.java")
          || cu.getPackageAnnotations() == null || cu.getPackageAnnotations().isEmpty() || cu.getPackageName() == null) {
        continue;
      }
      Env.CuEnv ce = enter.cuEnvs.get(cu);
      if (ce == null) {
        continue;
      }
      final String internal = cu.getPackageName().toString().replace('.', '/') + "/package-info";
      CSym pi = new CSym(internal);
      pi.isSource = true;
      pi.cu = cu;
      pi.name = "package-info";
      pi.flags = Sym.INTERFACE | Sym.ABSTRACT;
      ClassGen cg = new ClassGen(this, pi);
      Env env = new Env(null, ce);
      env.isClassEnv = true;
      env.cls = pi;
      env.scopeOwner = pi;
      final AnnotationGen.Result r = AnnotationGen.build(cg, cu.getPackageAnnotations(), env);
      if (r.visible.isEmpty() && r.invisible.isEmpty()) {
        continue;
      }
      out.put(internal, cf.build(ClassDesc.ofInternalName(internal), new java.util.function.Consumer<java.lang.classfile.ClassBuilder>() {
        public void accept(java.lang.classfile.ClassBuilder cb) {
          cb.withVersion(major, 0);
          cb.withFlags(0x1600);
          cb.withSuperclass(ClassDesc.ofInternalName("java/lang/Object"));
          cb.with(java.lang.classfile.attribute.SourceFileAttribute.of("package-info.java"));
          if (!r.visible.isEmpty()) {
            cb.with(java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute.of(r.visible));
          }
          if (!r.invisible.isEmpty()) {
            cb.with(java.lang.classfile.attribute.RuntimeInvisibleAnnotationsAttribute.of(r.invisible));
          }
        }
      }));
    }
  }

  /* ---------- naming helpers ---------- */

  static ClassDesc cdInternal(String internal) {
    return ClassDesc.ofInternalName(internal);
  }

  ClassDesc cd(Ty t) {
    return ClassDesc.ofDescriptor(TypeOps.erasure(t, st.object).desc());
  }

  static java.lang.classfile.TypeKind kind(Ty t) {
    switch (t.tag) {
      case Ty.BYTE:
        return java.lang.classfile.TypeKind.BYTE;
      case Ty.SHORT:
        return java.lang.classfile.TypeKind.SHORT;
      case Ty.CHAR:
        return java.lang.classfile.TypeKind.CHAR;
      case Ty.INT:
        return java.lang.classfile.TypeKind.INT;
      case Ty.LONG:
        return java.lang.classfile.TypeKind.LONG;
      case Ty.FLOAT:
        return java.lang.classfile.TypeKind.FLOAT;
      case Ty.DOUBLE:
        return java.lang.classfile.TypeKind.DOUBLE;
      case Ty.BOOLEAN:
        return java.lang.classfile.TypeKind.BOOLEAN;
      case Ty.VOID:
        return java.lang.classfile.TypeKind.VOID;
      default:
        return java.lang.classfile.TypeKind.REFERENCE;
    }
  }

  /** Stack kind: sub-int types and booleans are ints. */
  static java.lang.classfile.TypeKind stackKind(Ty t) {
    java.lang.classfile.TypeKind k = kind(t);
    switch (k) {
      case BYTE:
      case SHORT:
      case CHAR:
      case BOOLEAN:
        return java.lang.classfile.TypeKind.INT;
      default:
        return k;
    }
  }

  /* ---------- constructor layouts ---------- */

  static final class Layout {
    boolean enumPrefix;
    Ty outer;
    List<Ty> declared = new ArrayList<Ty>();
    List<VSym> captured = new ArrayList<VSym>();

    String desc() {
      StringBuilder sb = new StringBuilder("(");
      if (enumPrefix) {
        sb.append("Ljava/lang/String;I");
      }
      if (outer != null) {
        sb.append(outer.desc());
      }
      for (Ty t : declared) {
        sb.append(TypeOps.erasure(t).desc());
      }
      for (VSym v : captured) {
        sb.append(TypeOps.erasure(TypeOps.resolve(v.type)).desc());
      }
      return sb.append(")V").toString();
    }
  }

  Layout layout(CSym c, MSym ctor) {
    Layout l = new Layout();
    l.enumPrefix = c.isEnum() || (c.anon && c.superclass() != null && c.superclass().sym.isEnum());
    if (Captures.needsOuterThis(c) && c.outerClass != null) {
      l.outer = Ty.cls(c.outerClass);
    }
    if (c.anon) {
      Object nc = c.attrData;
      if (nc instanceof NewClassTree) {
        Object ci = a.info.get(nc);
        if (ci instanceof AttrCall.CallInfo) {
          AttrCall.CallInfo info = (AttrCall.CallInfo) ci;
          if (info.m != null && info.m.cls() != st.objectSym) {
            for (Ty p : info.m.params) {
              l.declared.add(TypeOps.erasure(p, st.object));
            }
            if (info.m.cls().isInner() && info.m.cls().outerClass != null) {
              l.declared.add(0, Ty.cls(info.m.cls().outerClass));
            }
          }
        }
      }
    } else if (ctor != null) {
      for (Ty p : ctor.params) {
        l.declared.add(TypeOps.erasure(p, st.object));
      }
    }
    if (c.local || c.anon) {
      l.captured.addAll(caps.of(c));
    }
    return l;
  }

  String ctorDesc(MSym ctor) {
    return layout(ctor.cls(), ctor).desc();
  }

  String methodDesc(MSym m) {
    if (m.isCtor()) {
      return ctorDesc(m);
    }
    return m.erasedDesc();
  }

  /** Name of the field holding the enclosing instance of the given class. */
  static String outerFieldName(CSym c) {
    int depth = 0;
    for (CSym o = c.outerClass; o != null && o.outerClass != null; o = o.outerClass) {
      depth++;
    }
    return "this$" + depth;
  }
}
