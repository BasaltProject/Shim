/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 11
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class DynamicConstants {
    static final Map<String, Integer> counts = new HashMap<String, Integer>();
    static final AtomicInteger concurrent = new AtomicInteger();
    static final IllegalAccessError originalError = new IllegalAccessError("constant error");
    static int calls;
    static int initialized;
    public static class Lazy { static { initialized++; } }

    public static Object bootstrap(MethodHandles.Lookup lookup, String name, Class<?> type) {
        if (!lookup.lookupClass().getName().equals("DynamicConstantData")) throw new AssertionError();
        if (name.equals("concurrentValue")) {
            concurrent.incrementAndGet();
            return new int[] {8, 9};
        }
        ++calls;
        Integer old = counts.get(name);
        counts.put(name, old == null ? 1 : old + 1);
        if (name.equals("booleanValue")) return Boolean.TRUE;
        if (name.equals("byteValue")) return Byte.valueOf((byte) -7);
        if (name.equals("shortValue")) return Short.valueOf((short) -300);
        if (name.equals("charValue")) return Character.valueOf('Q');
        if (name.equals("unusedValue")) return 77;
        if (name.equals("intValue")) { if (type != int.class) throw new AssertionError(); return 42; }
        if (name.equals("longValue")) return -1099511627776L;
        if (name.equals("floatValue")) return -1.25f;
        if (name.equals("doubleValue")) return 3.5d;
        if (name.equals("stringValue")) return new String("dynamic");
        if (name.equals("nullValue") || name.equals("nullPrimitive") || name.equals("lazyValue")) return null;
        if (name.equals("arrayValue")) return new int[] {4, 7};
        if (name.equals("widenInt")) return Byte.valueOf((byte) -7);
        if (name.equals("widenFloat")) return Long.valueOf(16777217L);
        if (name.equals("wrongPrimitive")) return "wrong";
        if (name.equals("narrowByte") || name.equals("wrongReference")) return 42;
        if (name.equals("failRuntime")) { System.gc(); throw new RuntimeException("constant failure"); }
        if (name.equals("failError")) throw originalError;
        throw new AssertionError(name);
    }

    public static float primitiveFloat(MethodHandles.Lookup lookup, String name, Class<?> type) {
        ++calls;
        counts.put(name, 1);
        return -0.0f;
    }

    public static double primitiveDouble(MethodHandles.Lookup lookup, String name, Class<?> type) {
        ++calls;
        counts.put(name, 1);
        return 42.25;
    }

    public static Object nested(MethodHandles.Lookup lookup, String name, Class<?> type, int argument) {
        ++calls;
        counts.put(name, 1);
        return argument + 1;
    }

    private static Object constant(Class<?> data, String name) throws Exception {
        return data.getMethod(name).invoke(null);
    }

    private static void concurrent(Class<?> data) throws Exception {
        final Object gate = new Object();
        final boolean[] ready = {false};
        final Object[] values = new Object[8];
        Thread[] threads = new Thread[8];
        for (int i = 0; i < threads.length; ++i) {
            final int index = i;
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    try {
                        synchronized (gate) { while (!ready[0]) gate.wait(); }
                        values[index] = constant(data, "concurrentValue");
                    } catch (Exception error) { values[index] = null; }
                }
            });
            threads[i].start();
        }
        synchronized (gate) { ready[0] = true; gate.notifyAll(); }
        for (Thread thread : threads) thread.join();
        for (Object value : values) if (value == null || value != values[0]) throw new AssertionError();
        int attempts = concurrent.get();
        System.out.println(attempts >= 1 && attempts <= 8);
        System.out.println(values[0] == constant(data, "concurrentValue"));
        System.out.println(concurrent.get() == attempts);
    }

    public static void main(String[] args) throws Exception {
        Class<?> data = Class.forName("DynamicConstantData");
        System.out.println(calls);
        System.out.println(data.getMethod("unused", boolean.class).invoke(null, false));
        System.out.println(calls);
        System.out.println(constant(data, "nestedValue"));
        System.out.println(calls);
        String[] names = {"booleanValue", "byteValue", "shortValue", "charValue", "intValue", "longValue",
                "floatValue", "doubleValue", "stringValue", "nullValue", "arrayValue", "lazyValue", "widenInt", "widenFloat", "primitiveFloat", "primitiveDouble"};
        for (String name : names) {
            Object first = constant(data, name);
            Object second = constant(data, name);
            if (first instanceof Float) System.out.println(Float.floatToRawIntBits((Float) first));
            else if (first instanceof Double) System.out.println(Double.doubleToRawLongBits((Double) first));
            else if (first instanceof int[]) System.out.println(Arrays.toString((int[]) first));
            else System.out.println(first);
            System.out.println(first == null ? second == null : first.equals(second));
            System.out.println(counts.get(name));
        }
        System.out.println(constant(data, "aliasInt"));
        System.out.println(counts.get("intValue"));
        System.out.println(initialized);
        System.out.println(data.getMethod("unused", boolean.class).invoke(null, true));
        System.out.println(counts.get("unusedValue"));
        String[] invalid = {"wrongPrimitive", "nullPrimitive", "narrowByte", "wrongReference", "failRuntime", "failError"};
        for (String name : invalid) {
            for (int attempt = 0; attempt < 2; ++attempt) {
                try { constant(data, name); throw new AssertionError(); }
                catch (InvocationTargetException expected) {
                    Throwable error = expected.getCause();
                    System.out.println(error.getClass().getSimpleName());
                    if (attempt == 0) {
                        if (name.equals("failError")) System.out.println(error == originalError);
                        else System.out.println(error.getCause().getClass().getSimpleName());
                    }
                }
            }
            System.out.println(counts.get(name));
        }
        concurrent(data);
    }
}
