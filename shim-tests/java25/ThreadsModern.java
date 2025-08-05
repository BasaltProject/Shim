/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 21

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ThreadsModern {
    public static void main(String[] args) throws Throwable {
        testThreadId();
        testSleepAndJoinDuration();
        testPlatformThreadBuilder();
        testVirtualThreadBuilder();
        testStartVirtualThread();
        testVirtualThreadPerTaskExecutor();
        System.out.println("ALL THREADS MODERN TESTS PASSED");
    }

    private static void testThreadId() {
        Thread current = Thread.currentThread();
        long tid = current.threadId();
        long id = current.getId();
        if (tid <= 0) {
            throw new AssertionError("threadId must be positive: " + tid);
        }
        if (tid != id) {
            throw new AssertionError("threadId " + tid + " does not match getId " + id);
        }
        if (current.isVirtual()) {
            throw new AssertionError("main thread should not be virtual");
        }
        Thread.onSpinWait();
        System.out.println("threadId verified: " + (tid > 0));
    }

    private static void testSleepAndJoinDuration() throws Throwable {
        Thread.sleep(Duration.ofMillis(10));
        Thread.sleep(Duration.ofMillis(-5));

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // ignore
            }
        });
        t.start();
        boolean terminated = t.join(Duration.ofSeconds(2));
        if (!terminated) {
            throw new AssertionError("join with duration timed out");
        }
        boolean joinedTerminated = t.join(Duration.ofMillis(-1));
        if (!joinedTerminated) {
            throw new AssertionError("join with negative duration on terminated thread should return true");
        }
        System.out.println("sleep and join Duration verified");
    }

    private static void testPlatformThreadBuilder() throws Throwable {
        Thread.Builder.OfPlatform builder = Thread.ofPlatform()
                .name("test-worker-", 10)
                .daemon(true)
                .priority(Thread.NORM_PRIORITY);

        AtomicBoolean ran = new AtomicBoolean(false);
        Thread t1 = builder.unstarted(() -> ran.set(true));
        if (t1.isVirtual()) {
            throw new AssertionError("platform builder should create non-virtual thread");
        }
        if (!t1.isDaemon()) {
            throw new AssertionError("thread should be daemon");
        }
        if (!"test-worker-10".equals(t1.getName())) {
            throw new AssertionError("expected name test-worker-10, got: " + t1.getName());
        }

        t1.start();
        t1.join(Duration.ofSeconds(2));
        if (!ran.get()) {
            throw new AssertionError("task did not run");
        }

        Thread t2 = builder.unstarted(() -> {});
        if (!"test-worker-11".equals(t2.getName())) {
            throw new AssertionError("expected name test-worker-11, got: " + t2.getName());
        }
        System.out.println("platform thread builder verified");
    }

    private static void testVirtualThreadBuilder() throws Throwable {
        Thread.Builder.OfVirtual builder = Thread.ofVirtual()
                .name("v-worker-", 1);

        AtomicBoolean ran = new AtomicBoolean(false);
        AtomicBoolean isVirt = new AtomicBoolean(false);
        Thread vt1 = builder.unstarted(() -> {
            ran.set(true);
            isVirt.set(Thread.currentThread().isVirtual());
        });

        if (!vt1.isVirtual()) {
            throw new AssertionError("expected isVirtual to be true");
        }
        if (!vt1.isDaemon()) {
            throw new AssertionError("virtual threads must be daemon");
        }
        if (!"v-worker-1".equals(vt1.getName())) {
            throw new AssertionError("expected name v-worker-1, got: " + vt1.getName());
        }

        try {
            vt1.setDaemon(false);
            throw new AssertionError("setDaemon(false) on virtual thread must throw");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        vt1.start();
        vt1.join(Duration.ofSeconds(2));
        if (!ran.get()) {
            throw new AssertionError("virtual thread task did not run");
        }
        if (!isVirt.get()) {
            throw new AssertionError("currentThread inside virtual thread should report isVirtual=true");
        }

        Thread.Builder.OfVirtual unnameBuilder = Thread.ofVirtual();
        Thread unnameVt = unnameBuilder.unstarted(() -> {});
        if (!"".equals(unnameVt.getName())) {
            throw new AssertionError("unnamed virtual thread name should be empty, got: " + unnameVt.getName());
        }
        System.out.println("virtual thread builder verified");
    }

    private static void testStartVirtualThread() throws Throwable {
        AtomicReference<String> result = new AtomicReference<String>();
        Thread vt = Thread.startVirtualThread(() -> {
            result.set("executed on " + (Thread.currentThread().isVirtual() ? "virtual" : "platform"));
        });
        vt.join(Duration.ofSeconds(2));
        if (!"executed on virtual".equals(result.get())) {
            throw new AssertionError("unexpected startVirtualThread result: " + result.get());
        }
        System.out.println("startVirtualThread verified: " + result.get());
    }

    private static void testVirtualThreadPerTaskExecutor() throws Throwable {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        Future<Boolean> future = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return Thread.currentThread().isVirtual();
            }
        });
        Boolean wasVirtual = future.get();
        if (!Boolean.TRUE.equals(wasVirtual)) {
            throw new AssertionError("expected task on virtual thread executor to be virtual");
        }
        executor.shutdown();
        System.out.println("virtual thread executor verified: " + wasVirtual);
    }
}
