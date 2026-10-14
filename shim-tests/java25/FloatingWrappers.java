/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.util.*;

public class FloatingWrappers {
    public static void main(String[] args) throws Exception {
        {
            double[] values = {Double.NEGATIVE_INFINITY, -1, -0.0, 0, 1,
                    Double.POSITIVE_INFINITY, Double.longBitsToDouble(0x7ff8000000000001L), Double.longBitsToDouble(0xfff8000000000042L)};
            Set<Double> set = new HashSet<Double>();
            for (double a : values) {
                Double boxed = Double.valueOf(a);
                set.add(boxed);
                System.out.println(boxed.isNaN() + ":" + boxed.isInfinite());
                System.out.println(Double.doubleToLongBits(a) + ":" + boxed.hashCode() + ":" + Double.hashCode(a) + ":" + Double.isFinite(a));
                for (double b : values) {
                    Double other = Double.valueOf(b);
                    Comparable<Double> comparable = boxed;
                    System.out.println(boxed.equals(other) + ":" + Double.compare(a, b) + ":" + comparable.compareTo(other));
                }
                if (boxed.equals(null) || boxed.equals("wrong")) throw new AssertionError();
            }
            System.out.println("Double set " + set.size());
            System.out.println(Double.class.getField("MIN_VALUE").getDouble(null));
            System.out.println(Double.class.getField("MIN_NORMAL").getDouble(null));
            for (String field : new String[] {"MIN_EXPONENT", "MAX_EXPONENT", "SIZE", "BYTES", "PRECISION"})
                System.out.println(field + ":" + Double.class.getField(field).getInt(null));
            try { Double.valueOf(0).compareTo(null); throw new AssertionError(); }
            catch (NullPointerException expected) { System.out.println("null rejected"); }
        }
        {
            float[] values = {Float.NEGATIVE_INFINITY, -1, -0.0f, 0, 1,
                    Float.POSITIVE_INFINITY, Float.intBitsToFloat(0x7fc00001), Float.intBitsToFloat(0xffc00042)};
            Set<Float> set = new HashSet<Float>();
            for (float a : values) {
                Float boxed = Float.valueOf(a);
                set.add(boxed);
                System.out.println(boxed.isNaN() + ":" + boxed.isInfinite());
                System.out.println(Float.floatToIntBits(a) + ":" + boxed.hashCode() + ":" + Float.hashCode(a) + ":" + Float.isFinite(a));
                for (float b : values) {
                    Float other = Float.valueOf(b);
                    Comparable<Float> comparable = boxed;
                    System.out.println(boxed.equals(other) + ":" + Float.compare(a, b) + ":" + comparable.compareTo(other));
                }
                if (boxed.equals(null) || boxed.equals("wrong")) throw new AssertionError();
            }
            System.out.println("Float set " + set.size());
            System.out.println(Float.class.getField("MIN_VALUE").getFloat(null));
            System.out.println(Float.class.getField("MIN_NORMAL").getFloat(null));
            for (String field : new String[] {"MIN_EXPONENT", "MAX_EXPONENT", "SIZE", "BYTES", "PRECISION"})
                System.out.println(field + ":" + Float.class.getField(field).getInt(null));
            try { Float.valueOf(0).compareTo(null); throw new AssertionError(); }
            catch (NullPointerException expected) { System.out.println("null rejected"); }
        }
    }
}
