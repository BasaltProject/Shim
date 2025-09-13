/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class ConstantDescsAndDynamicDescModern {

  public enum SampleState {
    ACTIVE,
    INACTIVE
  }

  public static void main(String[] args) throws Throwable {
    testConstantDescsConstants();
    testCanonicalDynamicConstants();
    testDynamicCallSiteTransformations();
    testValidationExceptions();
    System.out.println("ConstantDescsAndDynamicDescModern passed successfully");
  }

  private static void testConstantDescsConstants() {
    if (!"Ljava/lang/constant/DynamicCallSiteDesc;".equals(ConstantDescs.CD_DynamicCallSiteDesc.descriptorString())) {
      throw new AssertionError("CD_DynamicCallSiteDesc descriptor mismatch: " + ConstantDescs.CD_DynamicCallSiteDesc);
    }
    if (!"Ljava/lang/Enum$EnumDesc;".equals(ConstantDescs.CD_EnumDesc.descriptorString())) {
      throw new AssertionError("CD_EnumDesc descriptor mismatch: " + ConstantDescs.CD_EnumDesc);
    }
    if (!"Ljava/lang/invoke/VarHandle$VarHandleDesc;".equals(ConstantDescs.CD_VarHandleDesc.descriptorString())) {
      throw new AssertionError("CD_VarHandleDesc descriptor mismatch: " + ConstantDescs.CD_VarHandleDesc);
    }
    if (!"Ljava/lang/constant/DirectMethodHandleDesc$Kind;".equals(ConstantDescs.CD_MethodHandleDesc_Kind.descriptorString())) {
      throw new AssertionError("CD_MethodHandleDesc_Kind descriptor mismatch: " + ConstantDescs.CD_MethodHandleDesc_Kind);
    }
    if (!"Ljava/lang/invoke/ConstantBootstraps;".equals(ConstantDescs.CD_ConstantBootstraps.descriptorString())) {
      throw new AssertionError("CD_ConstantBootstraps descriptor mismatch: " + ConstantDescs.CD_ConstantBootstraps);
    }
  }

  private static void testCanonicalDynamicConstants() {
    // 1. BSM_NULL_CONSTANT canonicalization to ConstantDescs.NULL
    ConstantDesc nullDesc = DynamicConstantDesc.ofCanonical(
        ConstantDescs.BSM_NULL_CONSTANT,
        ConstantDescs.DEFAULT_NAME,
        ConstantDescs.CD_Object,
        new ConstantDesc[0]);
    if (nullDesc != ConstantDescs.NULL) {
      throw new AssertionError("Expected identity equality with ConstantDescs.NULL, got: " + nullDesc);
    }

    // 2. BSM_PRIMITIVE_CLASS canonicalization to ClassDesc
    ConstantDesc intDesc = DynamicConstantDesc.ofCanonical(
        ConstantDescs.BSM_PRIMITIVE_CLASS,
        "I",
        ConstantDescs.CD_Class,
        new ConstantDesc[0]);
    if (!ConstantDescs.CD_int.equals(intDesc)) {
      throw new AssertionError("Expected CD_int ClassDesc, got: " + intDesc);
    }

    // 3. BSM_ENUM_CONSTANT canonicalization to Enum.EnumDesc
    ClassDesc sampleEnumDesc = ClassDesc.of(SampleState.class.getName());
    ConstantDesc enumDesc = DynamicConstantDesc.ofCanonical(
        ConstantDescs.BSM_ENUM_CONSTANT,
        "ACTIVE",
        sampleEnumDesc,
        new ConstantDesc[0]);
    if (!"EnumDesc[ConstantDescsAndDynamicDescModern$SampleState.ACTIVE]".equals(enumDesc.toString())) {
      throw new AssertionError("Unexpected EnumDesc toString: " + enumDesc);
    }

    // 4. BSM_VARHANDLE_ARRAY canonicalization to VarHandle.VarHandleDesc
    ClassDesc intArrayDesc = ConstantDescs.CD_int.arrayType();
    ConstantDesc arrayVhDesc = DynamicConstantDesc.ofCanonical(
        ConstantDescs.BSM_VARHANDLE_ARRAY,
        ConstantDescs.DEFAULT_NAME,
        ConstantDescs.CD_VarHandle,
        new ConstantDesc[] { intArrayDesc });
    if (!(arrayVhDesc instanceof VarHandle.VarHandleDesc)) {
      throw new AssertionError("Expected VarHandleDesc instance, got: " + arrayVhDesc.getClass().getName());
    }
    VarHandle.VarHandleDesc vhDesc = (VarHandle.VarHandleDesc) arrayVhDesc;
    if (!ConstantDescs.CD_int.equals(vhDesc.varType())) {
      throw new AssertionError("Expected varType CD_int, got: " + vhDesc.varType());
    }

    // 5. Non-canonical fallback returns DynamicConstantDesc
    ConstantDesc customDesc = DynamicConstantDesc.ofCanonical(
        ConstantDescs.BSM_INVOKE,
        "custom",
        ConstantDescs.CD_String,
        new ConstantDesc[0]);
    if (!(customDesc instanceof DynamicConstantDesc)) {
      throw new AssertionError("Expected fallback DynamicConstantDesc, got: " + customDesc.getClass().getName());
    }
  }

  private static void testDynamicCallSiteTransformations() {
    DirectMethodHandleDesc bsm = ConstantDescs.ofCallsiteBootstrap(
        ConstantDescs.CD_MethodHandles,
        "constant",
        ConstantDescs.CD_CallSite);

    MethodTypeDesc mtd1 = MethodTypeDesc.of(ConstantDescs.CD_void);
    DynamicCallSiteDesc callSite1 = DynamicCallSiteDesc.of(bsm, "methodOne", mtd1);

    if (!"methodOne".equals(callSite1.invocationName()) || !mtd1.equals(callSite1.invocationType())) {
      throw new AssertionError("callSite1 initialization mismatch");
    }
    if (callSite1.bootstrapArgs().length != 0) {
      throw new AssertionError("expected 0 bootstrap args");
    }

    // withArgs transformation
    DynamicCallSiteDesc callSiteWithArgs = callSite1.withArgs(ConstantDescs.TRUE, ConstantDescs.CD_String);
    if (callSite1.bootstrapArgs().length != 0) {
      throw new AssertionError("original callSite1 was mutated by withArgs");
    }
    if (callSiteWithArgs.bootstrapArgs().length != 2
        || !ConstantDescs.TRUE.equals(callSiteWithArgs.bootstrapArgs()[0])
        || !ConstantDescs.CD_String.equals(callSiteWithArgs.bootstrapArgs()[1])) {
      throw new AssertionError("withArgs contents mismatch: " + java.util.Arrays.toString(callSiteWithArgs.bootstrapArgs()));
    }

    // withNameAndType transformation
    MethodTypeDesc mtd2 = MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_String);
    DynamicCallSiteDesc callSiteRenamed = callSiteWithArgs.withNameAndType("methodTwo", mtd2);
    if (!"methodTwo".equals(callSiteRenamed.invocationName()) || !mtd2.equals(callSiteRenamed.invocationType())) {
      throw new AssertionError("withNameAndType mutation mismatch");
    }
    if (callSiteRenamed.bootstrapArgs().length != 2) {
      throw new AssertionError("withNameAndType failed to preserve bootstrapArgs");
    }

    // equals and hash code
    DynamicCallSiteDesc duplicate = DynamicCallSiteDesc.of(
        bsm, "methodTwo", mtd2, ConstantDescs.TRUE, ConstantDescs.CD_String);
    if (!callSiteRenamed.equals(duplicate) || callSiteRenamed.hashCode() != duplicate.hashCode()) {
      throw new AssertionError("DynamicCallSiteDesc equality mismatch");
    }
  }

  private static void testValidationExceptions() {
    boolean caught = false;
    try {
      DynamicConstantDesc.ofCanonical(null, "name", ConstantDescs.CD_String, new ConstantDesc[0]);
    } catch (NullPointerException e) {
      caught = true;
    }
    if (!caught) {
      throw new AssertionError("Expected NPE for null bootstrapMethod in ofCanonical");
    }

    DirectMethodHandleDesc bsm = ConstantDescs.ofCallsiteBootstrap(
        ConstantDescs.CD_MethodHandles, "bsm", ConstantDescs.CD_CallSite);
    DynamicCallSiteDesc site = DynamicCallSiteDesc.of(bsm, MethodTypeDesc.of(ConstantDescs.CD_void));

    caught = false;
    try {
      site.withArgs((ConstantDesc[]) null);
    } catch (NullPointerException e) {
      caught = true;
    }
    if (!caught) {
      throw new AssertionError("Expected NPE for null args in withArgs");
    }

    caught = false;
    try {
      site.withNameAndType(null, MethodTypeDesc.of(ConstantDescs.CD_void));
    } catch (NullPointerException e) {
      caught = true;
    }
    if (!caught) {
      throw new AssertionError("Expected NPE for null name in withNameAndType");
    }
  }
}
