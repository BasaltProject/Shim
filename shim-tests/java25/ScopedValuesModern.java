/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 25

import java.lang.ScopedValue;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ScopedValuesModern {
    private static final ScopedValue<String> USERNAME = ScopedValue.newInstance();
    private static final ScopedValue<Integer> USER_ID = ScopedValue.newInstance();
    private static final ScopedValue<String> OPTIONAL_TAG = ScopedValue.newInstance();

    public static void main(String[] args) throws Throwable {
        testUnbound();
        testSingleBinding();
        testMultipleAndChainedBindings();
        testShadowing();
        testNullBinding();
        testCarrierCallAndExceptions();
        testThreadIsolation();
        System.out.println("ALL SCOPED VALUES MODERN TESTS PASSED");
    }

    private static void testUnbound() {
        if (USERNAME.isBound()) {
            throw new AssertionError("USERNAME must not be bound");
        }
        try {
            USERNAME.get();
            throw new AssertionError("get() on unbound ScopedValue must throw");
        } catch (NoSuchElementException expected) {
            // expected
        }
        if (!"guest".equals(USERNAME.orElse("guest"))) {
            throw new AssertionError("orElse on unbound failed");
        }
        try {
            USERNAME.orElseThrow(() -> new IllegalStateException("no user"));
            throw new AssertionError("orElseThrow on unbound must throw");
        } catch (IllegalStateException expected) {
            // expected
        }
        System.out.println("unbound ScopedValue verified");
    }

    private static void testSingleBinding() {
        ScopedValue.where(USERNAME, "dnlblasco").run(() -> {
            if (!USERNAME.isBound()) {
                throw new AssertionError("USERNAME should be bound");
            }
            if (!"dnlblasco".equals(USERNAME.get())) {
                throw new AssertionError("expected dnlblasco, got: " + USERNAME.get());
            }
            if (!"dnlblasco".equals(USERNAME.orElse("other"))) {
                throw new AssertionError("orElse on bound value should return bound value");
            }
        });
        if (USERNAME.isBound()) {
            throw new AssertionError("USERNAME should be unbound after scope exit");
        }
        System.out.println("single ScopedValue binding verified");
    }

    private static void testMultipleAndChainedBindings() throws Throwable {
        ScopedValue.Carrier carrier = ScopedValue.where(USERNAME, "admin").where(USER_ID, 42);
        if (!"admin".equals(carrier.get(USERNAME))) {
            throw new AssertionError("carrier.get(USERNAME) mismatch");
        }
        if (carrier.get(USER_ID) != 42) {
            throw new AssertionError("carrier.get(USER_ID) mismatch");
        }

        carrier.run(() -> {
            if (!USERNAME.isBound() || !USER_ID.isBound()) {
                throw new AssertionError("both keys should be bound");
            }
            if (!"admin".equals(USERNAME.get()) || USER_ID.get() != 42) {
                throw new AssertionError("bound values mismatch");
            }
        });
        System.out.println("multiple and chained bindings verified");
    }

    private static void testShadowing() {
        ScopedValue.where(USERNAME, "outer").run(() -> {
            if (!"outer".equals(USERNAME.get())) {
                throw new AssertionError("expected outer");
            }
            ScopedValue.where(USERNAME, "inner").run(() -> {
                if (!"inner".equals(USERNAME.get())) {
                    throw new AssertionError("expected inner");
                }
            });
            if (!"outer".equals(USERNAME.get())) {
                throw new AssertionError("expected outer to be restored");
            }
        });
        System.out.println("ScopedValue shadowing verified");
    }

    private static void testNullBinding() {
        ScopedValue.where(OPTIONAL_TAG, null).run(() -> {
            if (!OPTIONAL_TAG.isBound()) {
                throw new AssertionError("null-bound ScopedValue must report isBound == true");
            }
            if (OPTIONAL_TAG.get() != null) {
                throw new AssertionError("expected get() == null");
            }
            if (OPTIONAL_TAG.orElse("default") != null) {
                throw new AssertionError("orElse should return null when bound to null");
            }
        });
        System.out.println("null ScopedValue binding verified");
    }

    private static void testCarrierCallAndExceptions() throws Throwable {
        Integer result = ScopedValue.where(USER_ID, 21).call(() -> USER_ID.get() * 2);
        if (result != 42) {
            throw new AssertionError("call result mismatch: " + result);
        }

        try {
            ScopedValue.where(USER_ID, 1).call(() -> {
                throw new IllegalArgumentException("expected call failure");
            });
            throw new AssertionError("call should propagate exception");
        } catch (IllegalArgumentException expected) {
            // expected
        }
        System.out.println("carrier call and exception propagation verified");
    }

    private static void testThreadIsolation() throws Throwable {
        AtomicBoolean threadSawBinding = new AtomicBoolean(false);
        AtomicReference<String> threadUser = new AtomicReference<String>();

        ScopedValue.where(USERNAME, "main-thread-user").run(() -> {
            Thread t = new Thread(() -> {
                threadSawBinding.set(USERNAME.isBound());
                threadUser.set(USERNAME.orElse("unbound-thread"));
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        if (threadSawBinding.get()) {
            throw new AssertionError("new thread should not inherit scoped values from parent thread");
        }
        if (!"unbound-thread".equals(threadUser.get())) {
            throw new AssertionError("expected unbound-thread, got: " + threadUser.get());
        }
        System.out.println("thread isolation verified");
    }
}
