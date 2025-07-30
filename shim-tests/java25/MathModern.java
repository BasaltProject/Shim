/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 18

public class MathModern {
    public static void main(String[] args) {
        testExactArithmetic();
        testMultiplyHigh();
        testFloorAndCeilDivMod();
        testByteAndShortUnsigned();
        testIntegerUnsigned();
        testLongUnsigned();
        System.out.println("ALL MATH MODERN TESTS PASSED");
    }

    private static void testExactArithmetic() {
        System.out.println("addExact int: " + Math.addExact(100, 200));
        System.out.println("addExact long: " + Math.addExact(100000000000L, 200000000000L));
        try {
            Math.addExact(Integer.MAX_VALUE, 1);
            throw new AssertionError();
        } catch (ArithmeticException expected) {
            System.out.println("addExact int overflow: ae");
        }
        try {
            Math.addExact(Long.MIN_VALUE, -1);
            throw new AssertionError();
        } catch (ArithmeticException expected) {
            System.out.println("addExact long overflow: ae");
        }

        System.out.println("subtractExact int: " + Math.subtractExact(500, 200));
        System.out.println("subtractExact long: " + Math.subtractExact(500000000000L, 200000000000L));
        try {
            Math.subtractExact(Integer.MIN_VALUE, 1);
            throw new AssertionError();
        } catch (ArithmeticException expected) {
            System.out.println("subtractExact int overflow: ae");
        }

        System.out.println("multiplyExact int: " + Math.multiplyExact(30, 40));
        System.out.println("multiplyExact long-int: " + Math.multiplyExact(3000000000L, 2));
        System.out.println("multiplyExact long-long: " + Math.multiplyExact(3000000000L, 2000000000L));
        try {
            Math.multiplyExact(Integer.MAX_VALUE, 2);
            throw new AssertionError();
        } catch (ArithmeticException expected) {
            System.out.println("multiplyExact int overflow: ae");
        }
        try {
            Math.multiplyExact(Long.MAX_VALUE, 2L);
            throw new AssertionError();
        } catch (ArithmeticException expected) {
            System.out.println("multiplyExact long overflow: ae");
        }

        System.out.println("incrementExact int: " + Math.incrementExact(10));
        System.out.println("decrementExact int: " + Math.decrementExact(10));
        System.out.println("negateExact int: " + Math.negateExact(10));
        try {
            Math.negateExact(Integer.MIN_VALUE);
            throw new AssertionError();
        } catch (ArithmeticException expected) {
            System.out.println("negateExact int overflow: ae");
        }
        try {
            Math.negateExact(Long.MIN_VALUE);
            throw new AssertionError();
        } catch (ArithmeticException expected) {
            System.out.println("negateExact long overflow: ae");
        }

        System.out.println("toIntExact: " + Math.toIntExact(12345L));
        try {
            Math.toIntExact(Long.MAX_VALUE);
            throw new AssertionError();
        } catch (ArithmeticException expected) {
            System.out.println("toIntExact overflow: ae");
        }
    }

    private static void testMultiplyHigh() {
        long a = 1234567890123456789L;
        long b = 987654321098765432L;
        System.out.println("multiplyHigh positive: " + Math.multiplyHigh(a, b));
        System.out.println("multiplyHigh negative: " + Math.multiplyHigh(-a, b));
        System.out.println("unsignedMultiplyHigh: " + Math.unsignedMultiplyHigh(a, b));
        System.out.println("unsignedMultiplyHigh neg: " + Math.unsignedMultiplyHigh(-a, b));
    }

    private static void testFloorAndCeilDivMod() {
        System.out.println("floorDiv int pos: " + Math.floorDiv(7, 3));
        System.out.println("floorDiv int neg: " + Math.floorDiv(-7, 3));
        System.out.println("ceilDiv int pos: " + Math.ceilDiv(7, 3));
        System.out.println("ceilDiv int neg: " + Math.ceilDiv(-7, 3));

        System.out.println("floorMod int pos: " + Math.floorMod(7, 3));
        System.out.println("floorMod int neg: " + Math.floorMod(-7, 3));
        System.out.println("ceilMod int pos: " + Math.ceilMod(7, 3));
        System.out.println("ceilMod int neg: " + Math.ceilMod(-7, 3));

        System.out.println("floorDiv long: " + Math.floorDiv(-7000000000L, 3L));
        System.out.println("ceilDiv long: " + Math.ceilDiv(-7000000000L, 3L));
        System.out.println("floorMod long: " + Math.floorMod(-7000000000L, 3L));
        System.out.println("ceilMod long: " + Math.ceilMod(-7000000000L, 3L));

        System.out.println("floorDivExact: " + Math.floorDivExact(10, 2));
        System.out.println("ceilDivExact: " + Math.ceilDivExact(10, 2));

        try {
            Math.floorDivExact(Integer.MIN_VALUE, -1);
            throw new AssertionError();
        } catch (ArithmeticException expected) {
            System.out.println("floorDivExact overflow: ae");
        }
        try {
            Math.ceilDivExact(Long.MIN_VALUE, -1L);
            throw new AssertionError();
        } catch (ArithmeticException expected) {
            System.out.println("ceilDivExact overflow: ae");
        }
    }

    private static void testByteAndShortUnsigned() {
        byte b = (byte) -1;
        System.out.println("byte toUnsignedInt: " + Byte.toUnsignedInt(b));
        System.out.println("byte toUnsignedLong: " + Byte.toUnsignedLong(b));
        System.out.println("byte compareUnsigned: " + (Byte.compareUnsigned((byte) 1, (byte) -1) < 0));
        System.out.println("byte compare: " + (Byte.compare((byte) 1, (byte) -1) > 0));

        short s = (short) -1;
        System.out.println("short toUnsignedInt: " + Short.toUnsignedInt(s));
        System.out.println("short toUnsignedLong: " + Short.toUnsignedLong(s));
        System.out.println("short compareUnsigned: " + (Short.compareUnsigned((short) 1, (short) -1) < 0));
        System.out.println("short compare: " + (Short.compare((short) 1, (short) -1) > 0));
    }

    private static void testIntegerUnsigned() {
        int u1 = 100;
        int u2 = -1; // 0xFFFFFFFF
        System.out.println("int compareUnsigned: " + (Integer.compareUnsigned(u1, u2) < 0));
        System.out.println("int toUnsignedLong: " + Integer.toUnsignedLong(u2));
        System.out.println("int divideUnsigned: " + Integer.divideUnsigned(u2, 2));
        System.out.println("int remainderUnsigned: " + Integer.remainderUnsigned(u2, 2));
        System.out.println("int toUnsignedString dec: " + Integer.toUnsignedString(u2));
        System.out.println("int toUnsignedString hex: " + Integer.toUnsignedString(u2, 16));
        System.out.println("int parseUnsignedInt: " + Integer.parseUnsignedInt("4294967295"));
        System.out.println("int min: " + Integer.min(10, 20));
        System.out.println("int max: " + Integer.max(10, 20));
        System.out.println("int sum: " + Integer.sum(10, 20));

        try {
            Integer.parseUnsignedInt("-1");
            throw new AssertionError();
        } catch (NumberFormatException expected) {
            System.out.println("int parseUnsignedInt neg: nfe");
        }
        try {
            Integer.parseUnsignedInt("4294967296");
            throw new AssertionError();
        } catch (NumberFormatException expected) {
            System.out.println("int parseUnsignedInt overflow: nfe");
        }
    }

    private static void testLongUnsigned() {
        long l1 = 100L;
        long l2 = -1L; // 0xFFFFFFFFFFFFFFFF
        System.out.println("long compareUnsigned: " + (Long.compareUnsigned(l1, l2) < 0));
        System.out.println("long divideUnsigned: " + Long.divideUnsigned(l2, 2L));
        System.out.println("long remainderUnsigned: " + Long.remainderUnsigned(l2, 2L));
        System.out.println("long toUnsignedString dec: " + Long.toUnsignedString(l2));
        System.out.println("long toUnsignedString hex: " + Long.toUnsignedString(l2, 16));
        System.out.println("long parseUnsignedLong: " + Long.parseUnsignedLong("18446744073709551615"));
        System.out.println("long min: " + Long.min(10L, 20L));
        System.out.println("long max: " + Long.max(10L, 20L));
        System.out.println("long sum: " + Long.sum(10L, 20L));

        try {
            Long.parseUnsignedLong("-1");
            throw new AssertionError();
        } catch (NumberFormatException expected) {
            System.out.println("long parseUnsignedLong neg: nfe");
        }
        try {
            Long.parseUnsignedLong("18446744073709551616");
            throw new AssertionError();
        } catch (NumberFormatException expected) {
            System.out.println("long parseUnsignedLong overflow: nfe");
        }
    }
}
