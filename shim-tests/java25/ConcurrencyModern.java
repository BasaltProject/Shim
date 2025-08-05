/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrencyModern {
    public static void main(String[] args) throws Throwable {
        testAtomicInteger();
        testAtomicLong();
        testAtomicReference();
        testAtomicBoolean();
        testAddersAndAccumulators();
        testReentrantLock();
        testReentrantLockCondition();
        testReentrantReadWriteLock();
        testCountDownLatch();
        testSemaphore();
        testCopyOnWriteArrayList();
        testCopyOnWriteArraySet();
        testArrayBlockingQueue();
        testConcurrentHashMapModern();
        testThreadLocalModern();
        System.out.println("ALL CONCURRENCY MODERN TESTS PASSED");
    }

    private static void testAtomicInteger() {
        AtomicInteger ai = new AtomicInteger(10);
        int prev = ai.getAndUpdate(x -> x * 2);
        int curr = ai.updateAndGet(x -> x + 5);
        int accPrev = ai.getAndAccumulate(10, (a, b) -> a - b);
        int accCurr = ai.accumulateAndGet(4, (a, b) -> a + b);

        int ex1 = ai.compareAndExchange(19, 100);
        int ex2 = ai.compareAndExchange(19, 200);

        ai.setPlain(42);
        int plain = ai.getPlain();
        ai.setOpaque(55);
        int opaque = ai.getOpaque();
        ai.setRelease(77);
        int acq = ai.getAcquire();

        boolean weak1 = ai.weakCompareAndSetVolatile(77, 88);
        boolean weak2 = ai.weakCompareAndSetAcquire(88, 99);

        System.out.println("AtomicInteger updates: prev=" + prev + " curr=" + curr +
            " accPrev=" + accPrev + " accCurr=" + accCurr +
            " ex1=" + ex1 + " ex2=" + ex2 +
            " plain=" + plain + " opaque=" + opaque + " acq=" + acq +
            " weak1=" + weak1 + " weak2=" + weak2 + " str=" + ai);
    }

    private static void testAtomicLong() {
        AtomicLong al = new AtomicLong(100L);
        long prev = al.getAndUpdate(x -> x + 50L);
        long curr = al.updateAndGet(x -> x * 2L);
        long accPrev = al.getAndAccumulate(100L, (a, b) -> a - b);
        long accCurr = al.accumulateAndGet(5L, (a, b) -> a + b);

        long ex1 = al.compareAndExchange(205L, 500L);
        long ex2 = al.compareAndExchange(205L, 600L);

        al.setPlain(1234L);
        long plain = al.getPlain();
        al.setOpaque(5678L);
        long opaque = al.getOpaque();
        al.setRelease(9999L);
        long acq = al.getAcquire();

        System.out.println("AtomicLong updates: prev=" + prev + " curr=" + curr +
            " accPrev=" + accPrev + " accCurr=" + accCurr +
            " ex1=" + ex1 + " ex2=" + ex2 +
            " plain=" + plain + " opaque=" + opaque + " acq=" + acq +
            " str=" + al);
    }

    private static void testAtomicReference() {
        AtomicReference<String> ref = new AtomicReference<>("initial");
        String prev = ref.getAndUpdate(s -> s + "-updated");
        String curr = ref.updateAndGet(s -> "[" + s + "]");
        String accPrev = ref.getAndAccumulate("suffix", (a, b) -> a + ":" + b);
        String accCurr = ref.accumulateAndGet("end", (a, b) -> a + "." + b);

        String ex1 = ref.compareAndExchange(accCurr, "final");
        String ex2 = ref.compareAndExchange("wrong", "other");

        ref.setPlain("plainValue");
        String plain = ref.getPlain();
        ref.setRelease("releaseValue");
        String acq = ref.getAcquire();

        System.out.println("AtomicReference updates: prev=" + prev + " curr=" + curr +
            " accPrev=" + accPrev + " accCurr=" + accCurr +
            " ex1=" + ex1 + " ex2=" + ex2 +
            " plain=" + plain + " acq=" + acq);
    }

    private static void testAtomicBoolean() {
        AtomicBoolean ab = new AtomicBoolean(false);
        boolean ex1 = ab.compareAndExchange(false, true);
        boolean ex2 = ab.compareAndExchange(false, true);

        ab.setPlain(false);
        boolean plain = ab.getPlain();
        ab.setOpaque(true);
        boolean opaque = ab.getOpaque();
        ab.setRelease(false);
        boolean acq = ab.getAcquire();

        System.out.println("AtomicBoolean updates: ex1=" + ex1 + " ex2=" + ex2 +
            " plain=" + plain + " opaque=" + opaque + " acq=" + acq +
            " str=" + ab);
    }

    private static void testAddersAndAccumulators() {
        LongAdder la = new LongAdder();
        la.increment();
        la.add(9);
        la.decrement();
        long laSum = la.sum();
        long laReset = la.sumThenReset();
        long laAfterReset = la.sum();

        DoubleAdder da = new DoubleAdder();
        da.add(2.5);
        da.add(7.5);
        double daSum = da.sum();
        double daReset = da.sumThenReset();
        double daAfterReset = da.sum();

        LongAccumulator lacc = new LongAccumulator((a, b) -> Math.max(a, b), 0L);
        lacc.accumulate(10L);
        lacc.accumulate(42L);
        lacc.accumulate(7L);
        long laccVal = lacc.get();

        DoubleAccumulator dacc = new DoubleAccumulator((a, b) -> a * b, 1.0);
        dacc.accumulate(2.0);
        dacc.accumulate(3.0);
        dacc.accumulate(4.0);
        double daccVal = dacc.get();

        System.out.println("Adders and Accumulators: laSum=" + laSum + " laReset=" + laReset +
            " laAfterReset=" + laAfterReset + " daSum=" + daSum + " daReset=" + daReset +
            " daAfterReset=" + daAfterReset + " lacc=" + laccVal + " dacc=" + daccVal);
    }

    private static void testReentrantLock() throws Throwable {
        ReentrantLock lock = new ReentrantLock();
        if (lock.isLocked()) throw new AssertionError();
        lock.lock();
        if (!lock.isLocked()) throw new AssertionError();
        if (!lock.isHeldByCurrentThread()) throw new AssertionError();
        if (lock.getHoldCount() != 1) throw new AssertionError();

        lock.lock();
        if (lock.getHoldCount() != 2) throw new AssertionError();

        lock.unlock();
        if (lock.getHoldCount() != 1) throw new AssertionError();
        if (!lock.isLocked()) throw new AssertionError();

        lock.unlock();
        if (lock.isLocked()) throw new AssertionError();
        if (lock.getHoldCount() != 0) throw new AssertionError();

        boolean acquired = lock.tryLock(100, TimeUnit.MILLISECONDS);
        if (!acquired) throw new AssertionError();
        lock.unlock();

        System.out.println("ReentrantLock basic reentrancy verified");
    }

    private static void testReentrantLockCondition() throws Throwable {
        ReentrantLock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        AtomicBoolean flag = new AtomicBoolean(false);

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(50);
                lock.lock();
                try {
                    flag.set(true);
                    cond.signal();
                } finally {
                    lock.unlock();
                }
            } catch (Exception e) {
                // ignore
            }
        });
        t.start();

        lock.lock();
        try {
            while (!flag.get()) {
                cond.await();
            }
        } finally {
            lock.unlock();
        }
        t.join();

        System.out.println("ReentrantLock Condition signal/await verified: " + flag.get());
    }

    private static void testReentrantReadWriteLock() {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock r = rwl.readLock();
        ReentrantReadWriteLock.WriteLock w = rwl.writeLock();

        r.lock();
        r.lock();
        int readCount = rwl.getReadLockCount();
        int readHolds = rwl.getReadHoldCount();
        r.unlock();
        r.unlock();

        w.lock();
        w.lock();
        boolean writeLocked = rwl.isWriteLockedByCurrentThread();
        int writeHolds = rwl.getWriteHoldCount();

        // Lock downgrade
        r.lock();
        w.unlock();
        w.unlock();
        boolean stillRead = rwl.getReadLockCount() > 0;
        r.unlock();

        System.out.println("ReentrantReadWriteLock: readCount=" + readCount + " readHolds=" + readHolds +
            " writeLocked=" + writeLocked + " writeHolds=" + writeHolds +
            " downgradeSuccess=" + stillRead);
    }

    private static void testCountDownLatch() throws Throwable {
        CountDownLatch latch = new CountDownLatch(3);
        long initialCount = latch.getCount();

        latch.countDown();
        latch.countDown();
        long midCount = latch.getCount();
        latch.countDown();
        long endCount = latch.getCount();

        boolean reached = latch.await(100, TimeUnit.MILLISECONDS);
        System.out.println("CountDownLatch: initial=" + initialCount + " mid=" + midCount +
            " end=" + endCount + " reached=" + reached);
    }

    private static void testSemaphore() throws Throwable {
        Semaphore sem = new Semaphore(2);
        int initial = sem.availablePermits();

        sem.acquire();
        int after1 = sem.availablePermits();
        boolean try2 = sem.tryAcquire();
        int after2 = sem.availablePermits();
        boolean try3 = sem.tryAcquire(50, TimeUnit.MILLISECONDS);

        sem.release(2);
        int afterRelease = sem.availablePermits();
        int drained = sem.drainPermits();
        int afterDrain = sem.availablePermits();

        System.out.println("Semaphore: initial=" + initial + " after1=" + after1 +
            " try2=" + try2 + " after2=" + after2 + " try3=" + try3 +
            " afterRelease=" + afterRelease + " drained=" + drained + " afterDrain=" + afterDrain);
    }

    private static void testCopyOnWriteArrayList() {
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        list.add("alpha");
        list.add("beta");
        list.add("gamma");

        Iterator<String> it = list.iterator();
        list.add("delta");
        list.remove("alpha");

        List<String> snapshot = new ArrayList<>();
        while (it.hasNext()) {
            snapshot.add(it.next());
        }

        boolean absent1 = list.addIfAbsent("beta");
        boolean absent2 = list.addIfAbsent("epsilon");

        List<String> sub = list.subList(0, 2);
        int subSize = sub.size();

        System.out.println("CopyOnWriteArrayList: snapshot=" + snapshot +
            " liveList=" + list + " absent1=" + absent1 + " absent2=" + absent2 +
            " subSize=" + subSize);
    }

    private static void testCopyOnWriteArraySet() {
        CopyOnWriteArraySet<Integer> set = new CopyOnWriteArraySet<>();
        boolean a1 = set.add(1);
        boolean a2 = set.add(2);
        boolean a3 = set.add(1);
        int size = set.size();
        boolean c1 = set.contains(1);
        boolean c2 = set.contains(3);

        System.out.println("CopyOnWriteArraySet: a1=" + a1 + " a2=" + a2 + " a3=" + a3 +
            " size=" + size + " c1=" + c1 + " c2=" + c2);
    }

    private static void testArrayBlockingQueue() throws Throwable {
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(3);
        queue.put("first");
        queue.put("second");
        boolean off1 = queue.offer("third");
        boolean off2 = queue.offer("overflow");

        String p1 = queue.poll();
        String p2 = queue.take();
        String peek = queue.peek();
        int remCap = queue.remainingCapacity();

        List<String> drained = new ArrayList<>();
        queue.drainTo(drained);

        System.out.println("ArrayBlockingQueue: off1=" + off1 + " off2=" + off2 +
            " p1=" + p1 + " p2=" + p2 + " peek=" + peek +
            " remCap=" + remCap + " drained=" + drained);
    }

    private static void testConcurrentHashMapModern() {
        Set<String> set = ConcurrentHashMap.newKeySet();
        set.add("item1");
        set.add("item2");
        set.add("item1");
        int setSize = set.size();

        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        map.put("a", 1);
        int v1 = map.computeIfAbsent("b", k -> 2);
        int v2 = map.computeIfAbsent("a", k -> 99);
        int v3 = map.computeIfPresent("a", (k, v) -> v + 10);
        int v4 = map.compute("c", (k, v) -> (v == null) ? 5 : v + 1);
        int v5 = map.merge("a", 100, (o, n) -> o + n);

        ConcurrentHashMap.KeySetView<String, Integer> kv = map.keySet(0);
        boolean kvAdd = kv.add("newKey");

        System.out.println("ConcurrentHashMap modern: setSize=" + setSize +
            " v1=" + v1 + " v2=" + v2 + " v3=" + v3 + " v4=" + v4 + " v5=" + v5 +
            " kvAdd=" + kvAdd + " mapSize=" + map.size());
    }

    private static void testThreadLocalModern() {
        ThreadLocal<String> tl = ThreadLocal.withInitial(() -> "default-val");
        String initial = tl.get();
        tl.set("custom-val");
        String custom = tl.get();
        tl.remove();
        String afterRemove = tl.get();

        System.out.println("ThreadLocal modern: initial=" + initial +
            " custom=" + custom + " afterRemove=" + afterRemove);
    }
}
