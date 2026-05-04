/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 25
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.invoke.MethodHandles;
import java.security.MessageDigest;
import java.util.*;

public class ClassFileModern {
  static final ClassDesc C = ClassDesc.of("Sample");

  static byte[] build(ClassFile cf) {
    return cf.build(C, cb -> {
      cb.withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_SUPER);
      cb.withInterfaceSymbols(ClassDesc.of("java.util.function.IntUnaryOperator"));
      cb.with(SourceFileAttribute.of("Sample.java"));
      cb.withField("count", ConstantDescs.CD_int, ClassFile.ACC_PRIVATE | ClassFile.ACC_STATIC);
      cb.withMethodBody("<init>", MethodTypeDesc.of(ConstantDescs.CD_void), ClassFile.ACC_PUBLIC, c -> c
          .aload(0).invokespecial(ConstantDescs.CD_Object, "<init>", MethodTypeDesc.of(ConstantDescs.CD_void))
          .return_());
      cb.withMethodBody("applyAsInt", MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int),
          ClassFile.ACC_PUBLIC, c -> {
            Label loop = c.newLabel();
            Label done = c.newLabel();
            c.iconst_0().istore(2).labelBinding(loop).iload(2).iload(1).if_icmpge(done)
                .getstatic(C, "count", ConstantDescs.CD_int).iconst_1().iadd().putstatic(C, "count", ConstantDescs.CD_int)
                .iinc(2, 1).goto_(loop).labelBinding(done).getstatic(C, "count", ConstantDescs.CD_int).ireturn();
          });
      cb.withMethodBody("guarded", MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_String),
          ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> c.trying(
              b -> b.aload(0).invokevirtual(ConstantDescs.CD_String, "length", MethodTypeDesc.of(ConstantDescs.CD_int))
                  .ireturn(),
              h -> h.catching(ClassDesc.of("java.lang.NullPointerException"), e -> e.pop().iconst_m1().ireturn())));
      cb.withMethodBody("pick", MethodTypeDesc.of(ConstantDescs.CD_String, ConstantDescs.CD_int),
          ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, c -> {
            Label a = c.newLabel();
            Label b = c.newLabel();
            Label d = c.newLabel();
            c.iload(0).tableswitch(0, 1, d, List.of(SwitchCase.of(0, a), SwitchCase.of(1, b)))
                .labelBinding(a).ldc("zero").areturn().labelBinding(b).ldc("one").areturn()
                .labelBinding(d).ldc("many").areturn();
          });
    });
  }

  static String digest(byte[] b) throws Exception {
    return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b), 0, 8);
  }

  public static void main(String[] args) throws Exception {
    ClassFile cf = ClassFile.of();
    byte[] bytes = build(cf);
    System.out.println(bytes.length + " " + digest(bytes));
    ClassModel m = cf.parse(bytes);
    System.out.println(m);
    for (ClassElement e : m) System.out.println("  " + e);
    for (MethodModel mm : m.methods()) {
      System.out.println(mm);
      mm.code().ifPresent(code -> {
        for (CodeElement e : code) System.out.println("    " + (e instanceof Label ? "Label" : e));
      });
    }
    System.out.println(m.toDebugString().replaceAll("id=-?[0-9]+", "id=N"));
    System.out.println("verify " + cf.verify(bytes));
    byte[] t = cf.transformClass(m, ClassTransform.transformingMethodBodies(CodeTransform.ACCEPT_ALL));
    System.out.println(t.length + " " + digest(t));
    byte[] dropped = ClassFile.of(ClassFile.DebugElementsOption.DROP_DEBUG).transformClass(m,
        ClassTransform.dropping(e -> e instanceof SourceFileAttribute));
    System.out.println(dropped.length + " " + digest(dropped));
    byte[] fresh = ClassFile.of(ClassFile.ConstantPoolSharingOption.NEW_POOL).transformClass(m, ClassTransform.ACCEPT_ALL);
    System.out.println(fresh.length + " " + digest(fresh));
    Class<?> k = MethodHandles.lookup().defineClass(bytes);
    Object o = k.getDeclaredConstructor().newInstance();
    System.out.println(((java.util.function.IntUnaryOperator) o).applyAsInt(5));
    System.out.println(k.getMethod("guarded", String.class).invoke(null, "abcd") + " " + k.getMethod("guarded", String.class).invoke(null, (Object) null));
    System.out.println(k.getMethod("pick", int.class).invoke(null, 1) + " " + k.getMethod("pick", int.class).invoke(null, 7));
    try {
      cf.build(C, cb -> cb.withMethodBody("x", MethodTypeDesc.of(ConstantDescs.CD_void), 1, c -> { }));
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }
}
