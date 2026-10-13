/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadableConstants {
    static int initialized;
    static MethodType capturedType;
    static MethodHandle capturedHandle;
    static int bootstrapCalls;
    static int runtimeFailures;
    static int errorFailures;
    static int nullFailures;
    static int checkedFailures;
    static final AtomicInteger concurrentFailures = new AtomicInteger();
    static final IllegalAccessError originalError = new IllegalAccessError("direct error");

    public static CallSite failRuntime(MethodHandles.Lookup lookup, String name, MethodType type) {
        ++runtimeFailures;
        System.gc();
        throw new RuntimeException("bootstrap failure");
    }

    public static CallSite failError(MethodHandles.Lookup lookup, String name, MethodType type) {
        ++errorFailures;
        throw originalError;
    }

    public static CallSite failNull(MethodHandles.Lookup lookup, String name, MethodType type) {
        ++nullFailures;
        return null;
    }

    public static CallSite failChecked(MethodHandles.Lookup lookup, String name, MethodType type) throws Exception {
        ++checkedFailures;
        throw new Exception("checked bootstrap failure");
    }

    public static CallSite failConcurrent(MethodHandles.Lookup lookup, String name, MethodType type) {
        concurrentFailures.incrementAndGet();
        throw new RuntimeException("concurrent failure");
    }

    public static CallSite capture(MethodHandles.Lookup lookup, String name, MethodType type,
                                   MethodType constant, MethodHandle target) {
        capturedType = constant;
        capturedHandle = target;
        ++bootstrapCalls;
        return new ConstantCallSite(target);
    }
    public static class Lazy {
        static { initialized++; }
        public static int target() { return 42; }
    }

    private static Object constant(Class<?> data, String name) throws Exception {
        return data.getMethod(name).invoke(null);
    }

    private static void checkConcurrent(final Class<?> data, final String name,
                                        final boolean expectFailure) throws Exception {
        final Object gate = new Object();
        final boolean[] ready = {false};
        final boolean[] results = new boolean[8];
        Thread[] threads = new Thread[results.length];
        for (int i = 0; i < threads.length; ++i) {
            final int index = i;
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    try {
                        synchronized (gate) {
                            while (!ready[0]) gate.wait();
                        }
                        try {
                            Object result = constant(data, name);
                            results[index] = !expectFailure && "7".equals(result);
                        } catch (InvocationTargetException error) {
                            results[index] = expectFailure && error.getCause() instanceof BootstrapMethodError;
                        }
                    } catch (Exception error) {
                        results[index] = false;
                    }
                }
            });
            threads[i].start();
        }
        synchronized (gate) {
            ready[0] = true;
            gate.notifyAll();
        }
        for (Thread thread : threads) thread.join();
        for (boolean result : results) if (!result) throw new AssertionError("Concurrent linkage failed");
        System.out.println("concurrent " + expectFailure);
    }

    public static void main(String[] args) throws Exception {
        Class<?> data = Class.forName("LoadableConstantData");
        MethodType type = (MethodType) constant(data, "type");
        System.out.println(type.toMethodDescriptorString());
        System.out.println(type == constant(data, "type"));
        System.out.println(type == constant(data, "wideType"));
        System.out.println(type.equals(MethodType.methodType(long.class, int.class, String[].class)));
        System.out.println(constant(data, "concat"));
        System.out.println(type == constant(data, "wideType"));
        System.out.println(constant(data, "captureCall"));
        System.out.println(constant(data, "captureCall"));
        System.out.println(bootstrapCalls);
        System.out.println(initialized);
        MethodType lazyType = (MethodType) constant(data, "lazyType");
        System.out.println(lazyType == capturedType);
        System.out.println(constant(data, "staticHandle") == capturedHandle);
        System.out.println(lazyType.parameterType(0) == Lazy.class);
        System.out.println(lazyType.returnType() == Lazy.class);
        System.out.println(initialized);
        String[] names = {"staticHandle", "virtualHandle", "constructorHandle", "lazyHandle"};
        for (String name : names) {
            MethodHandle handle = (MethodHandle) constant(data, name);
            System.out.println(handle.type().toMethodDescriptorString());
            System.out.println(handle == constant(data, name));
        }
        System.out.println(initialized);
        for (String name : new String[] {"mismatchCall", "mismatchReturnCall"}) {
            int before = bootstrapCalls;
            for (int attempt = 0; attempt < 2; ++attempt) {
                try {
                    constant(data, name);
                    throw new AssertionError("Accepted wrong target signature");
                } catch (InvocationTargetException expected) {
                    System.out.println(expected.getCause() instanceof BootstrapMethodError);
                }
            }
            System.out.println(bootstrapCalls - before);
        }
        for (String name : new String[] {"failRuntimeCall", "failErrorCall", "failNullCall", "failCheckedCall"}) {
            for (int attempt = 0; attempt < 2; ++attempt) {
                try {
                    constant(data, name);
                    throw new AssertionError("Accepted failed bootstrap");
                } catch (InvocationTargetException expected) {
                    Throwable error = expected.getCause();
                    System.out.println(error.getClass().getSimpleName());
                    if (attempt == 0 && (name.equals("failRuntimeCall") || name.equals("failCheckedCall"))) {
                        System.out.println(error.getCause().getClass().getSimpleName());
                    }
                    if (attempt == 0 && name.equals("failErrorCall")) {
                        System.out.println(error == originalError);
                    }
                }
            }
        }
        System.out.println(runtimeFailures);
        System.out.println(errorFailures);
        System.out.println(nullFailures);
        System.out.println(checkedFailures);
        int beforeShared = bootstrapCalls;
        System.out.println(constant(data, "sharedFirstCall"));
        System.out.println(constant(data, "sharedSecondCall"));
        System.out.println(bootstrapCalls - beforeShared);
        beforeShared = bootstrapCalls;
        System.out.println(constant(data, "twoSharedCalls"));
        System.out.println(bootstrapCalls - beforeShared);
        beforeShared = bootstrapCalls;
        System.out.println(constant(data, "twoSharedCalls"));
        System.out.println(bootstrapCalls - beforeShared);
        int beforeAlias = bootstrapCalls;
        for (int i = 0; i < 2; ++i) {
            try {
                constant(data, "mismatchAliasCall");
                throw new AssertionError();
            } catch (InvocationTargetException expected) {
                System.out.println(expected.getCause() instanceof BootstrapMethodError);
            }
        }
        System.out.println(bootstrapCalls - beforeAlias);
        checkConcurrent(data, "concurrentCall", false);
        checkConcurrent(data, "failConcurrentCall", true);
        int attempts = concurrentFailures.get();
        System.out.println(attempts >= 1 && attempts <= 8);
        try {
            constant(data, "failConcurrentCall");
            throw new AssertionError();
        } catch (InvocationTargetException expected) {
            System.out.println(expected.getCause() instanceof BootstrapMethodError);
        }
        System.out.println(concurrentFailures.get() == attempts);
    }
}
