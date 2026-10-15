/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.math.RoundingMode;

public class ConstantsModern {
    public static void main(String[] args) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        testClassDesc(lookup);
        testMethodTypeDesc(lookup);
        testMethodHandleDesc(lookup);
        testDynamicConstantDesc(lookup);
        testDynamicCallSiteDesc();
        testDescribeConstable(lookup);
        System.out.println("ALL CONSTANTS MODERN TESTS PASSED");
    }

    private static void testClassDesc(MethodHandles.Lookup lookup) throws Throwable {
        ClassDesc cdString = ClassDesc.of("java.lang.String");
        System.out.println("cdString desc: " + cdString.descriptorString());
        System.out.println("cdString display: " + cdString.displayName());
        System.out.println("cdString pkg: " + cdString.packageName());
        System.out.println("cdString isClass: " + cdString.isClassOrInterface());
        System.out.println("cdString isArray: " + cdString.isArray());
        System.out.println("cdString isPrim: " + cdString.isPrimitive());
        System.out.println("cdString toString: " + cdString);

        ClassDesc cdInt = ConstantDescs.CD_int;
        System.out.println("cdInt desc: " + cdInt.descriptorString());
        System.out.println("cdInt display: " + cdInt.displayName());
        System.out.println("cdInt isPrim: " + cdInt.isPrimitive());
        System.out.println("cdInt toString: " + cdInt);

        ClassDesc arrString = cdString.arrayType(2);
        System.out.println("arrString desc: " + arrString.descriptorString());
        System.out.println("arrString display: " + arrString.displayName());
        System.out.println("arrString isArray: " + arrString.isArray());
        System.out.println("arrString comp: " + arrString.componentType().descriptorString());
        System.out.println("arrString toString: " + arrString);

        ClassDesc nested = cdString.nested("Inner");
        System.out.println("nested desc: " + nested.descriptorString());
        System.out.println("nested display: " + nested.displayName());

        System.out.println("resolved string: " + (cdString.resolveConstantDesc(lookup) == String.class));
        System.out.println("resolved int: " + (cdInt.resolveConstantDesc(lookup) == int.class));
        System.out.println("resolved arr: " + (arrString.resolveConstantDesc(lookup) == String[][].class));
    }

    private static void testMethodTypeDesc(MethodHandles.Lookup lookup) throws Throwable {
        MethodTypeDesc mtd = MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_String, ConstantDescs.CD_int);
        System.out.println("mtd desc: " + mtd.descriptorString());
        System.out.println("mtd display: " + mtd.displayDescriptor());
        System.out.println("mtd params: " + mtd.parameterCount());
        System.out.println("mtd param 0: " + mtd.parameterType(0).displayName());
        System.out.println("mtd param 1: " + mtd.parameterType(1).displayName());
        System.out.println("mtd return: " + mtd.returnType().displayName());
        System.out.println("mtd toString: " + mtd);

        MethodTypeDesc changedRet = mtd.changeReturnType(ConstantDescs.CD_boolean);
        System.out.println("changedRet: " + changedRet.displayDescriptor());

        MethodTypeDesc dropped = mtd.dropParameterTypes(0, 1);
        System.out.println("dropped: " + dropped.displayDescriptor());

        MethodTypeDesc inserted = mtd.insertParameterTypes(1, ConstantDescs.CD_long);
        System.out.println("inserted: " + inserted.displayDescriptor());

        MethodType resolvedMt = (MethodType) mtd.resolveConstantDesc(lookup);
        System.out.println("resolved mt: " + resolvedMt);
    }

    private static void testMethodHandleDesc(MethodHandles.Lookup lookup) throws Throwable {
        ClassDesc cdString = ConstantDescs.CD_String;
        ClassDesc cdInt = ConstantDescs.CD_int;

        MethodHandleDesc staticMhd = MethodHandleDesc.ofMethod(
            DirectMethodHandleDesc.Kind.STATIC,
            cdString,
            "valueOf",
            MethodTypeDesc.of(cdString, cdInt)
        );
        System.out.println("staticMhd inv: " + staticMhd.invocationType().displayDescriptor());
        System.out.println("staticMhd toString: " + staticMhd);

        MethodHandleDesc virtualMhd = MethodHandleDesc.ofMethod(
            DirectMethodHandleDesc.Kind.VIRTUAL,
            cdString,
            "length",
            MethodTypeDesc.of(cdInt)
        );
        System.out.println("virtualMhd inv: " + virtualMhd.invocationType().displayDescriptor());
        System.out.println("virtualMhd toString: " + virtualMhd);

        MethodHandle mh = (MethodHandle) staticMhd.resolveConstantDesc(lookup);
        Object res = mh.invokeWithArguments(42);
        System.out.println("resolved mh invocation: " + res);
    }

    private static void testDynamicConstantDesc(MethodHandles.Lookup lookup) throws Throwable {
        DynamicConstantDesc<Class<?>> dcd = DynamicConstantDesc.ofNamed(
            ConstantDescs.BSM_PRIMITIVE_CLASS,
            "I",
            ConstantDescs.CD_Class
        );
        System.out.println("dcd name: " + dcd.constantName());
        System.out.println("dcd type: " + dcd.constantType().displayName());
        System.out.println("dcd toString: " + dcd);
        System.out.println("dcd resolved: " + (dcd.resolveConstantDesc(lookup) == int.class));

        System.out.println("null constant toString: " + ConstantDescs.NULL);
        System.out.println("null constant resolved: " + ConstantDescs.NULL.resolveConstantDesc(lookup));

        System.out.println("true constant toString: " + ConstantDescs.TRUE);
        System.out.println("true constant resolved: " + ConstantDescs.TRUE.resolveConstantDesc(lookup));
    }

    private static void testDynamicCallSiteDesc() {
        DynamicCallSiteDesc cs = DynamicCallSiteDesc.of(
            ConstantDescs.BSM_PRIMITIVE_CLASS,
            "I",
            MethodTypeDesc.of(ConstantDescs.CD_Class)
        );
        System.out.println("cs name: " + cs.invocationName());
        System.out.println("cs type: " + cs.invocationType().displayDescriptor());
        System.out.println("cs toString: " + cs);
    }

    private static void testDescribeConstable(MethodHandles.Lookup lookup) throws Throwable {
        String str = "hello";
        System.out.println("str describeConstable: " + str.describeConstable().get());

        Integer i = 123;
        System.out.println("int describeConstable: " + i.describeConstable().get());

        Long l = 456L;
        System.out.println("long describeConstable: " + l.describeConstable().get());

        Float f = 1.5f;
        System.out.println("float describeConstable: " + f.describeConstable().get());

        Double d = 2.5;
        System.out.println("double describeConstable: " + d.describeConstable().get());

        Boolean b = Boolean.TRUE;
        System.out.println("bool describeConstable: " + b.describeConstable().get());

        Class<?> clazz = String.class;
        System.out.println("class describeConstable: " + clazz.describeConstable().get().descriptorString());

        MethodType mt = MethodType.methodType(void.class, String.class);
        System.out.println("mt describeConstable: " + mt.describeConstable().get().descriptorString());

        RoundingMode rm = RoundingMode.UP;
        System.out.println("enum describeConstable: " + rm.describeConstable().get());
        System.out.println("enum resolved: " + (rm.describeConstable().get().resolveConstantDesc(lookup) == RoundingMode.UP));
    }
}
