/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.constant;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Objects;

public interface MethodHandleDesc extends ConstantDesc {

  static DirectMethodHandleDesc of(DirectMethodHandleDesc.Kind kind, ClassDesc owner, String name, MethodTypeDesc type) {
    return ofMethod(kind, owner, name, type);
  }

  static DirectMethodHandleDesc ofMethod(DirectMethodHandleDesc.Kind kind, ClassDesc owner, String name, MethodTypeDesc type) {
    Objects.requireNonNull(kind, "kind");
    Objects.requireNonNull(owner, "owner");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(type, "type");
    return new ConstantDescsImpl.DirectMethodHandleDescImpl(kind, owner, name, type);
  }

  static DirectMethodHandleDesc ofField(DirectMethodHandleDesc.Kind kind, ClassDesc owner, String name, ClassDesc fieldType) {
    Objects.requireNonNull(kind, "kind");
    Objects.requireNonNull(owner, "owner");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(fieldType, "fieldType");
    MethodTypeDesc mtd;
    switch (kind) {
      case GETTER:
        mtd = MethodTypeDesc.of(fieldType, owner);
        break;
      case SETTER:
        mtd = MethodTypeDesc.of(ConstantDescs.CD_void, owner, fieldType);
        break;
      case STATIC_GETTER:
        mtd = MethodTypeDesc.of(fieldType);
        break;
      case STATIC_SETTER:
        mtd = MethodTypeDesc.of(ConstantDescs.CD_void, fieldType);
        break;
      default:
        throw new IllegalArgumentException("Invalid field kind: " + kind);
    }
    return new ConstantDescsImpl.DirectMethodHandleDescImpl(kind, owner, name, mtd);
  }

  static DirectMethodHandleDesc ofConstructor(ClassDesc owner, ClassDesc... paramDescs) {
    Objects.requireNonNull(owner, "owner");
    MethodTypeDesc mtd = MethodTypeDesc.of(owner, paramDescs);
    return new ConstantDescsImpl.DirectMethodHandleDescImpl(DirectMethodHandleDesc.Kind.CONSTRUCTOR, owner, "<init>", mtd);
  }

  MethodTypeDesc invocationType();

  default MethodHandleDesc asType(MethodTypeDesc type) {
    Objects.requireNonNull(type, "type");
    return invocationType().equals(type) ? this : new ConstantDescsImpl.AsTypeMethodHandleDesc(this, type);
  }

  @Override
  MethodHandle resolveConstantDesc(MethodHandles.Lookup lookup) throws ReflectiveOperationException;
}
