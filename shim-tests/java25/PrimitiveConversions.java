/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.util.function.*;

public class PrimitiveConversions {
    public static void main(String[] args) {
        testFunctions();
        testToPrimitives();
        testPrimitiveToPrimitives();
        testBiFunctions();
        testObjConsumers();
        System.out.println("ALL PRIMITIVE CONVERSIONS PASSED");
    }

    private static void testFunctions() {
        DoubleFunction<String> df = d -> "d:" + d;
        System.out.println(df.apply(3.5));
        System.out.println(df.apply(Double.NaN));
        System.out.println(df.apply(-0.0));

        IntFunction<String[]> arrayGen = String[]::new;
        String[] arr = arrayGen.apply(3);
        System.out.println("arr length: " + arr.length);

        LongFunction<String> lf = l -> "l:" + l;
        System.out.println(lf.apply(Long.MAX_VALUE));
        System.out.println(lf.apply(Long.MIN_VALUE));
    }

    private static void testToPrimitives() {
        ToDoubleFunction<String> tdf = s -> Double.parseDouble(s);
        System.out.println("tdf: " + tdf.applyAsDouble("12.75"));

        ToIntFunction<String> tif = String::length;
        System.out.println("tif: " + tif.applyAsInt("antigravity"));

        ToLongFunction<String> tlf = Long::parseLong;
        System.out.println("tlf: " + tlf.applyAsLong("9876543210123"));
    }

    private static void testPrimitiveToPrimitives() {
        DoubleToIntFunction d2i = d -> (int) d;
        System.out.println("d2i: " + d2i.applyAsInt(42.99));
        System.out.println("d2i nan: " + d2i.applyAsInt(Double.NaN));

        DoubleToLongFunction d2l = d -> Double.doubleToLongBits(d);
        System.out.println("d2l 1.0 bits: " + Long.toHexString(d2l.applyAsLong(1.0)));

        IntToDoubleFunction i2d = i -> i * 1.5;
        System.out.println("i2d: " + i2d.applyAsDouble(4));

        IntToLongFunction i2l = i -> ((long) i) << 32;
        System.out.println("i2l: " + i2l.applyAsLong(1));

        LongToDoubleFunction l2d = l -> (double) l;
        System.out.println("l2d: " + l2d.applyAsDouble(1000L));

        LongToIntFunction l2i = l -> (int) (l ^ (l >>> 32));
        System.out.println("l2i: " + l2i.applyAsInt(0x123456789abcdef0L));
    }

    private static void testBiFunctions() {
        ToDoubleBiFunction<String, String> tdbf = (a, b) -> (a.length() + b.length()) * 0.5;
        System.out.println("tdbf: " + tdbf.applyAsDouble("foo", "quux"));

        ToIntBiFunction<String, String> tibf = (a, b) -> a.length() + b.length();
        System.out.println("tibf: " + tibf.applyAsInt("abc", "defgh"));

        ToLongBiFunction<Integer, Integer> tlbf = (a, b) -> ((long) a << 32) | (b & 0xffffffffL);
        System.out.println("tlbf: " + Long.toHexString(tlbf.applyAsLong(0x1234, 0x5678)));
    }

    private static void testObjConsumers() {
        StringBuilder sb = new StringBuilder();

        ObjDoubleConsumer<StringBuilder> odc = (buf, d) -> buf.append("d=").append(d).append(";");
        odc.accept(sb, 2.5);

        ObjIntConsumer<StringBuilder> oic = (buf, i) -> buf.append("i=").append(i).append(";");
        oic.accept(sb, 42);

        ObjLongConsumer<StringBuilder> olc = (buf, l) -> buf.append("l=").append(l).append(";");
        olc.accept(sb, 1000000000000L);

        System.out.println("consumers log: " + sb);
    }
}
