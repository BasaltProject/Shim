/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 21

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;

public class SwitchModern {
    enum Priority { LOW, MEDIUM, HIGH, CRITICAL }

    public static void main(String[] args) throws Throwable {
        testTypeSwitch();
        testEnumSwitch();
        testDirectBootstraps();
        System.out.println("ALL SWITCH MODERN TESTS PASSED");
    }

    private static String matchType(Object obj) {
        return switch (obj) {
            case null -> "null";
            case String s -> "string: " + s;
            case Integer i -> "int: " + i;
            case Double d when d > 0 -> "positive double: " + d;
            case Double d -> "other double: " + d;
            default -> "default: " + obj.getClass().getSimpleName();
        };
    }

    private static void testTypeSwitch() {
        System.out.println("match null: " + matchType(null));
        System.out.println("match string: " + matchType("hello"));
        System.out.println("match int: " + matchType(42));
        System.out.println("match pos double: " + matchType(3.14));
        System.out.println("match neg double: " + matchType(-1.5));
        System.out.println("match bool: " + matchType(Boolean.TRUE));
    }

    private static int matchEnum(Priority p) {
        return switch (p) {
            case null -> -1;
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH, CRITICAL -> 3;
        };
    }

    private static void testEnumSwitch() {
        System.out.println("enum null: " + matchEnum(null));
        System.out.println("enum LOW: " + matchEnum(Priority.LOW));
        System.out.println("enum MEDIUM: " + matchEnum(Priority.MEDIUM));
        System.out.println("enum HIGH: " + matchEnum(Priority.HIGH));
        System.out.println("enum CRITICAL: " + matchEnum(Priority.CRITICAL));
    }

    private static void testDirectBootstraps() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType mt = MethodType.methodType(int.class, Object.class, int.class);

        var cs = SwitchBootstraps.typeSwitch(lookup, "typeSwitch", mt, String.class, Integer.class);
        var mh = cs.getTarget();

        System.out.println("direct typeSwitch str: " + mh.invokeWithArguments("hello", 0));
        System.out.println("direct typeSwitch int: " + mh.invokeWithArguments(123, 0));
        System.out.println("direct typeSwitch restart 1: " + mh.invokeWithArguments("hello", 1));
        System.out.println("direct typeSwitch null: " + mh.invokeWithArguments(null, 0));

        MethodType enumMt = MethodType.methodType(int.class, Priority.class, int.class);
        var enumCs = SwitchBootstraps.enumSwitch(lookup, "enumSwitch", enumMt, "LOW", "MEDIUM", "HIGH", "CRITICAL");
        var enumMh = enumCs.getTarget();

        System.out.println("direct enumSwitch LOW: " + enumMh.invokeWithArguments(Priority.LOW, 0));
        System.out.println("direct enumSwitch CRITICAL: " + enumMh.invokeWithArguments(Priority.CRITICAL, 0));
        System.out.println("direct enumSwitch null: " + enumMh.invokeWithArguments(null, 0));
    }
}
