/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.constant;

import java.util.Objects;

public final class ConstantDescs {
  public static final String DEFAULT_NAME = "_";

  public static final ClassDesc CD_int = ClassDesc.ofDescriptor("I");
  public static final ClassDesc CD_long = ClassDesc.ofDescriptor("J");
  public static final ClassDesc CD_float = ClassDesc.ofDescriptor("F");
  public static final ClassDesc CD_double = ClassDesc.ofDescriptor("D");
  public static final ClassDesc CD_boolean = ClassDesc.ofDescriptor("Z");
  public static final ClassDesc CD_byte = ClassDesc.ofDescriptor("B");
  public static final ClassDesc CD_char = ClassDesc.ofDescriptor("C");
  public static final ClassDesc CD_short = ClassDesc.ofDescriptor("S");
  public static final ClassDesc CD_void = ClassDesc.ofDescriptor("V");

  public static final ClassDesc CD_Object = ClassDesc.of("java.lang.Object");
  public static final ClassDesc CD_String = ClassDesc.of("java.lang.String");
  public static final ClassDesc CD_Class = ClassDesc.of("java.lang.Class");
  public static final ClassDesc CD_Number = ClassDesc.of("java.lang.Number");
  public static final ClassDesc CD_Integer = ClassDesc.of("java.lang.Integer");
  public static final ClassDesc CD_Long = ClassDesc.of("java.lang.Long");
  public static final ClassDesc CD_Float = ClassDesc.of("java.lang.Float");
  public static final ClassDesc CD_Double = ClassDesc.of("java.lang.Double");
  public static final ClassDesc CD_Boolean = ClassDesc.of("java.lang.Boolean");
  public static final ClassDesc CD_Byte = ClassDesc.of("java.lang.Byte");
  public static final ClassDesc CD_Character = ClassDesc.of("java.lang.Character");
  public static final ClassDesc CD_Short = ClassDesc.of("java.lang.Short");
  public static final ClassDesc CD_Void = ClassDesc.of("java.lang.Void");
  public static final ClassDesc CD_Throwable = ClassDesc.of("java.lang.Throwable");
  public static final ClassDesc CD_Exception = ClassDesc.of("java.lang.Exception");
  public static final ClassDesc CD_Enum = ClassDesc.of("java.lang.Enum");
  public static final ClassDesc CD_VarHandle = ClassDesc.of("java.lang.invoke.VarHandle");
  public static final ClassDesc CD_MethodHandles = ClassDesc.of("java.lang.invoke.MethodHandles");
  public static final ClassDesc CD_MethodHandles_Lookup = ClassDesc.of("java.lang.invoke.MethodHandles$Lookup");
  public static final ClassDesc CD_MethodHandle = ClassDesc.of("java.lang.invoke.MethodHandle");
  public static final ClassDesc CD_MethodType = ClassDesc.of("java.lang.invoke.MethodType");
  public static final ClassDesc CD_CallSite = ClassDesc.of("java.lang.invoke.CallSite");
  public static final ClassDesc CD_Collection = ClassDesc.of("java.util.Collection");
  public static final ClassDesc CD_List = ClassDesc.of("java.util.List");
  public static final ClassDesc CD_Set = ClassDesc.of("java.util.Set");
  public static final ClassDesc CD_Map = ClassDesc.of("java.util.Map");
  public static final ClassDesc CD_ConstantDesc = ClassDesc.of("java.lang.constant.ConstantDesc");
  public static final ClassDesc CD_DynamicConstantDesc = ClassDesc.of("java.lang.constant.DynamicConstantDesc");
  public static final ClassDesc CD_ClassDesc = ClassDesc.of("java.lang.constant.ClassDesc");
  public static final ClassDesc CD_MethodHandleDesc = ClassDesc.of("java.lang.constant.MethodHandleDesc");
  public static final ClassDesc CD_MethodTypeDesc = ClassDesc.of("java.lang.constant.MethodTypeDesc");
  public static final ClassDesc CD_DirectMethodHandleDesc = ClassDesc.of("java.lang.constant.DirectMethodHandleDesc");
  public static final ClassDesc CD_DynamicCallSiteDesc = ClassDesc.of("java.lang.constant.DynamicCallSiteDesc");
  public static final ClassDesc CD_EnumDesc = ClassDesc.of("java.lang.Enum$EnumDesc");
  public static final ClassDesc CD_VarHandleDesc = ClassDesc.of("java.lang.invoke.VarHandle$VarHandleDesc");
  public static final ClassDesc CD_MethodHandleDesc_Kind = ClassDesc.of("java.lang.constant.DirectMethodHandleDesc$Kind");
  public static final ClassDesc CD_ConstantBootstraps = ClassDesc.of("java.lang.invoke.ConstantBootstraps");

  public static final DirectMethodHandleDesc BSM_PRIMITIVE_CLASS = MethodHandleDesc.ofMethod(
      DirectMethodHandleDesc.Kind.STATIC, CD_ConstantBootstraps, "primitiveClass",
      MethodTypeDesc.of(CD_Class, CD_MethodHandles_Lookup, CD_String, CD_Class));

  public static final DirectMethodHandleDesc BSM_ENUM_CONSTANT = MethodHandleDesc.ofMethod(
      DirectMethodHandleDesc.Kind.STATIC, CD_ConstantBootstraps, "enumConstant",
      MethodTypeDesc.of(CD_Enum, CD_MethodHandles_Lookup, CD_String, CD_Class));

  public static final DirectMethodHandleDesc BSM_NULL_CONSTANT = MethodHandleDesc.ofMethod(
      DirectMethodHandleDesc.Kind.STATIC, CD_ConstantBootstraps, "nullConstant",
      MethodTypeDesc.of(CD_Object, CD_MethodHandles_Lookup, CD_String, CD_Class));

  public static final DirectMethodHandleDesc BSM_VARHANDLE_FIELD = MethodHandleDesc.ofMethod(
      DirectMethodHandleDesc.Kind.STATIC, CD_ConstantBootstraps, "fieldVarHandle",
      MethodTypeDesc.of(CD_VarHandle, CD_MethodHandles_Lookup, CD_String, CD_Class, CD_Class, CD_Class));

  public static final DirectMethodHandleDesc BSM_VARHANDLE_STATIC_FIELD = MethodHandleDesc.ofMethod(
      DirectMethodHandleDesc.Kind.STATIC, CD_ConstantBootstraps, "staticFieldVarHandle",
      MethodTypeDesc.of(CD_VarHandle, CD_MethodHandles_Lookup, CD_String, CD_Class, CD_Class, CD_Class));

  public static final DirectMethodHandleDesc BSM_VARHANDLE_ARRAY = MethodHandleDesc.ofMethod(
      DirectMethodHandleDesc.Kind.STATIC, CD_ConstantBootstraps, "arrayVarHandle",
      MethodTypeDesc.of(CD_VarHandle, CD_MethodHandles_Lookup, CD_String, CD_Class, CD_Class));

  public static final DirectMethodHandleDesc BSM_INVOKE = MethodHandleDesc.ofMethod(
      DirectMethodHandleDesc.Kind.STATIC, CD_ConstantBootstraps, "invoke",
      MethodTypeDesc.of(CD_Object, CD_MethodHandles_Lookup, CD_String, CD_Class, CD_MethodHandle, CD_Object.arrayType()));

  public static final DirectMethodHandleDesc BSM_EXPLICIT_CAST = MethodHandleDesc.ofMethod(
      DirectMethodHandleDesc.Kind.STATIC, CD_ConstantBootstraps, "explicitCast",
      MethodTypeDesc.of(CD_Object, CD_MethodHandles_Lookup, CD_String, CD_Class, CD_Object));

  public static final DirectMethodHandleDesc BSM_GET_STATIC_FINAL = MethodHandleDesc.ofMethod(
      DirectMethodHandleDesc.Kind.STATIC, CD_ConstantBootstraps, "getStaticFinal",
      MethodTypeDesc.of(CD_Object, CD_MethodHandles_Lookup, CD_String, CD_Class, CD_Class));

  public static final DirectMethodHandleDesc BSM_CLASS_DATA = MethodHandleDesc.ofMethod(
      DirectMethodHandleDesc.Kind.STATIC, CD_MethodHandles, "classData",
      MethodTypeDesc.of(CD_Object, CD_MethodHandles_Lookup, CD_String, CD_Class));

  public static final DirectMethodHandleDesc BSM_CLASS_DATA_AT = MethodHandleDesc.ofMethod(
      DirectMethodHandleDesc.Kind.STATIC, CD_MethodHandles, "classDataAt",
      MethodTypeDesc.of(CD_Object, CD_MethodHandles_Lookup, CD_String, CD_Class, CD_int));

  public static final String INIT_NAME = "<init>";
  public static final String CLASS_INIT_NAME = "<clinit>";
  public static final MethodTypeDesc MTD_void = MethodTypeDesc.of(CD_void);

  public static final ConstantDesc NULL = DynamicConstantDesc.ofNamed(BSM_NULL_CONSTANT, DEFAULT_NAME, CD_Object);

  public static final DynamicConstantDesc<Boolean> TRUE = DynamicConstantDesc.ofNamed(
      BSM_GET_STATIC_FINAL, "TRUE", CD_Boolean, CD_Boolean);

  public static final DynamicConstantDesc<Boolean> FALSE = DynamicConstantDesc.ofNamed(
      BSM_GET_STATIC_FINAL, "FALSE", CD_Boolean, CD_Boolean);

  public static DirectMethodHandleDesc ofCallsiteBootstrap(
      ClassDesc bootstrapOwner, String name, ClassDesc returnType, ClassDesc... staticArguments) {
    Objects.requireNonNull(bootstrapOwner, "bootstrapOwner");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(returnType, "returnType");
    Objects.requireNonNull(staticArguments, "staticArguments");
    ClassDesc[] params = new ClassDesc[3 + staticArguments.length];
    params[0] = CD_MethodHandles_Lookup;
    params[1] = CD_String;
    params[2] = CD_MethodType;
    System.arraycopy(staticArguments, 0, params, 3, staticArguments.length);
    return MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, bootstrapOwner, name, MethodTypeDesc.of(returnType, params));
  }

  public static DirectMethodHandleDesc ofConstantBootstrap(
      ClassDesc bootstrapOwner, String name, ClassDesc returnType, ClassDesc... staticArguments) {
    Objects.requireNonNull(bootstrapOwner, "bootstrapOwner");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(returnType, "returnType");
    Objects.requireNonNull(staticArguments, "staticArguments");
    ClassDesc[] params = new ClassDesc[3 + staticArguments.length];
    params[0] = CD_MethodHandles_Lookup;
    params[1] = CD_String;
    params[2] = CD_Class;
    System.arraycopy(staticArguments, 0, params, 3, staticArguments.length);
    return MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, bootstrapOwner, name, MethodTypeDesc.of(returnType, params));
  }

  private ConstantDescs() {}
}
