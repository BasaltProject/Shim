/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.TypeKind;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;

public class ClassFileLocals {
  static final int[] allocated = new int[1];

  public static void main(String[] args) {
    ClassFile cf = ClassFile.of();
    byte[] bytes = cf.build(ClassDesc.of("Sample"), cb -> cb.withMethodBody("m",
        MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
        c -> c.iload(0).istore(3).iload(3).ireturn()));
    ClassModel model = cf.parse(bytes);
    int original = ((CodeAttribute) model.methods().get(0).code().get()).maxLocals();
    cf.transformClass(model, ClassTransform.transformingMethodBodies(
        (CodeBuilder b, CodeElement e) -> {
          if (allocated[0] == 0) {
            allocated[0] = b.allocateLocal(TypeKind.INT) + 1;
          }
          b.with(e);
        }));
    int slot = allocated[0] - 1;
    if (slot != original) {
      throw new AssertionError("allocateLocal returned " + slot + " for a body declaring " + original + " locals");
    }
    System.out.println("ok");
  }
}
