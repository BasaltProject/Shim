/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import com.sun.source.tree.*;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.IdentityHashMap;
import java.util.Map;

/** Low-level emission helpers shared by the expression and statement generators. */
abstract class CodeBase {
  final ClassGen cg;
  final Gen g;
  final Attr a;
  final Tx tx;
  final Symtab st;
  final CodeBuilder b;
  final CSym cls;
  MSym method;
  boolean isStatic;
  int nextSlot;
  final Map<VSym, Integer> slots = new IdentityHashMap<VSym, Integer>();
  int lastLine = -1;

  CodeBase(ClassGen cg, CodeBuilder b, MSym method) {
    this.cg = cg;
    this.g = cg.g;
    this.a = cg.a;
    this.tx = g.tx;
    this.st = g.st;
    this.b = b;
    this.cls = cg.c;
    this.method = method;
    this.isStatic = method != null && method.isStatic();
    this.nextSlot = isStatic ? 0 : 1;
  }

  static ClassDesc cdi(String internal) {
    return ClassDesc.ofInternalName(internal);
  }

  ClassDesc cd(Ty t) {
    return cg.cd(t);
  }

  ClassDesc cd(CSym c) {
    return cg.cd(c);
  }

  Ty erase(Ty t) {
    return TypeOps.erasure(TypeOps.resolve(t), st.object);
  }

  Ty obj() {
    return st.object;
  }

  int newLocal(Ty t) {
    int s = nextSlot;
    nextSlot += t.width();
    return s;
  }

  int declare(VSym v) {
    Integer s = slots.get(v);
    if (s != null) {
      return s.intValue();
    }
    int slot = newLocal(erase(v.type));
    slots.put(v, Integer.valueOf(slot));
    return slot;
  }

  void line(Tree t) {
    if (!g.debugLines || !(t instanceof N.Base) || cls.cu == null) {
      return;
    }
    long pos = ((N.Base) t).pos;
    if (pos < 0) {
      pos = ((N.Base) t).start;
    }
    if (pos < 0 || cls.cu.getLineMap() == null) {
      return;
    }
    int line = (int) cls.cu.getLineMap().getLineNumber(pos);
    if (line != lastLine) {
      b.lineNumber(line);
      lastLine = line;
    }
  }

  void load(Ty t, int slot) {
    b.loadLocal(Gen.stackKind(erase(t)), slot);
  }

  void store(Ty t, int slot) {
    b.storeLocal(Gen.stackKind(erase(t)), slot);
  }

  /* ---------- constants ---------- */

  void pushConst(Object v, Ty t) {
    switch (t.tag) {
      case Ty.BOOLEAN:
        b.loadConstant(((Boolean) v).booleanValue() ? 1 : 0);
        return;
      case Ty.CHAR:
      case Ty.BYTE:
      case Ty.SHORT:
      case Ty.INT:
        b.loadConstant(ConstFold.intOf(v));
        return;
      case Ty.LONG:
        b.loadConstant(ConstFold.longOf(v));
        return;
      case Ty.FLOAT:
        b.loadConstant(ConstFold.floatOf(v));
        return;
      case Ty.DOUBLE:
        b.loadConstant(ConstFold.doubleOf(v));
        return;
      default:
        b.ldc((String) v);
    }
  }

  void pushInt(int v) {
    b.loadConstant(v);
  }

  /* ---------- conversions ---------- */

  void convertPrim(Ty from, Ty to) {
    if (from.tag == to.tag) {
      return;
    }
    TypeKind fk = Gen.stackKind(from);
    TypeKind tk = Gen.stackKind(to);
    if (fk != tk) {
      switch (fk) {
        case INT:
          switch (tk) {
            case LONG:
              b.i2l();
              break;
            case FLOAT:
              b.i2f();
              break;
            case DOUBLE:
              b.i2d();
              break;
            default:
              break;
          }
          break;
        case LONG:
          switch (tk) {
            case INT:
              b.l2i();
              break;
            case FLOAT:
              b.l2f();
              break;
            case DOUBLE:
              b.l2d();
              break;
            default:
              break;
          }
          break;
        case FLOAT:
          switch (tk) {
            case INT:
              b.f2i();
              break;
            case LONG:
              b.f2l();
              break;
            case DOUBLE:
              b.f2d();
              break;
            default:
              break;
          }
          break;
        case DOUBLE:
          switch (tk) {
            case INT:
              b.d2i();
              break;
            case LONG:
              b.d2l();
              break;
            case FLOAT:
              b.d2f();
              break;
            default:
              break;
          }
          break;
        default:
          break;
      }
    }
    if (to.tag == Ty.BYTE && from.tag != Ty.BYTE) {
      b.i2b();
    } else if (to.tag == Ty.SHORT && from.tag != Ty.BYTE && from.tag != Ty.SHORT) {
      b.i2s();
    } else if (to.tag == Ty.CHAR) {
      b.i2c();
    }
  }

  void box(Ty prim) {
    Ty bx = tx.box(prim);
    b.invokestatic(cd(bx), "valueOf", MethodTypeDesc.ofDescriptor("(" + prim.desc() + ")" + bx.desc()));
  }

  void unboxTo(Ty boxed, Ty prim) {
    b.invokevirtual(cd(boxed), prim.primName() + "Value", MethodTypeDesc.ofDescriptor("()" + prim.desc()));
  }

  void checkcast(Ty erased) {
    if (erased.tag == Ty.CLASS && erased.isObject()) {
      return;
    }
    b.checkcast(cd(erased));
  }

  boolean subtypeErased(Ty a1, Ty b1) {
    return tx.isSubtype(erase(a1), erase(b1));
  }

  /**
   * Converts the value on the stack, whose bytecode type is {@code stack} and whose source type is {@code sem},
   * to the type {@code want}.
   */
  void coerce(Ty stack, Ty sem, Ty want) {
    if (want == null) {
      return;
    }
    want = TypeOps.resolve(want);
    sem = sem == null ? stack : TypeOps.resolve(sem);
    if (want.tag == Ty.VOID) {
      popValue(stack);
      return;
    }
    if (want.tag == Ty.ERROR || stack.tag == Ty.ERROR) {
      return;
    }
    if (want.isPrim()) {
      if (stack.isPrim()) {
        convertPrim(stack, want);
        return;
      }
      Ty u = tx.unboxed(sem);
      if (u == null) {
        u = tx.unboxed(stack);
      }
      if (u == null) {
        u = want;
        Ty bx = tx.box(want);
        checkcast(bx);
        unboxTo(bx, want);
        return;
      }
      Ty bx = tx.box(u);
      if (!subtypeErased(stack, bx)) {
        checkcast(bx);
      }
      unboxTo(bx, u);
      convertPrim(u, want);
      return;
    }
    if (stack.isPrim()) {
      Ty target = tx.unboxed(want);
      Ty p = stack;
      if (target != null && target.tag != stack.tag) {
        convertPrim(stack, target);
        p = target;
      }
      box(p);
      return;
    }
    if (stack.tag == Ty.NULL) {
      return;
    }
    Ty ew = erase(want);
    if (want.tag == Ty.INTER) {
      for (Ty comp : want.args) {
        Ty ec = erase(comp);
        if (!subtypeErased(stack, ec)) {
          checkcast(ec);
        }
      }
      return;
    }
    if (!subtypeErased(stack, ew)) {
      checkcast(ew);
    }
  }

  void popValue(Ty t) {
    if (t.tag == Ty.VOID) {
      return;
    }
    if (t.width() == 2) {
      b.pop2();
    } else {
      b.pop();
    }
  }

  void dupValue(Ty t) {
    if (t.width() == 2) {
      b.dup2();
    } else {
      b.dup();
    }
  }

  /** Duplicates the top value below the next stack slot (receiver of width 1). */
  void dupX1(Ty t) {
    if (t.width() == 2) {
      b.dup2_x1();
    } else {
      b.dup_x1();
    }
  }

  void dupX2(Ty t) {
    if (t.width() == 2) {
      b.dup2_x2();
    } else {
      b.dup_x2();
    }
  }

  /* ---------- descriptors ---------- */

  MethodTypeDesc mtd(String desc) {
    return MethodTypeDesc.ofDescriptor(desc);
  }

  static boolean isInterfaceOwner(Ty t) {
    return t.tag == Ty.CLASS && t.sym.isInterface();
  }

  abstract void stmt(StatementTree s);
}
