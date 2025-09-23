/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.function.Supplier;

public class QueuedSynchronizerModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  static class Mutex extends AbstractQueuedSynchronizer {
    protected boolean isHeldExclusively() { return getState() == 1; }
    protected boolean tryAcquire(int acquires) {
      if (compareAndSetState(0, 1)) { setExclusiveOwnerThread(Thread.currentThread()); return true; }
      return false;
    }
    protected boolean tryRelease(int releases) {
      if (getState() == 0) throw new IllegalMonitorStateException();
      setExclusiveOwnerThread(null);
      setState(0);
      return true;
    }
    Condition newCondition() { return new ConditionObject(); }
    Thread owner() { return getExclusiveOwnerThread(); }
    int state() { return getState(); }
  }

  static class Latch extends AbstractQueuedSynchronizer {
    Latch(int count) { setState(count); }
    int state() { return getState(); }
    protected int tryAcquireShared(int acquires) { return getState() == 0 ? 1 : -1; }
    protected boolean tryReleaseShared(int releases) {
      for (;;) {
        int c = getState();
        if (c == 0) return false;
        int next = c - 1;
        if (compareAndSetState(c, next)) return next == 0;
      }
    }
  }

  static class LongSem extends AbstractQueuedLongSynchronizer {
    LongSem(long permits) { setState(permits); }
    long state() { return getState(); }
    protected long tryAcquireShared(long n) {
      for (;;) {
        long a = getState();
        long r = a - n;
        if (r < 0 || compareAndSetState(a, r)) return r;
      }
    }
    protected boolean tryReleaseShared(long n) {
      for (;;) {
        long a = getState();
        if (compareAndSetState(a, a + n)) return true;
      }
    }
  }

  static class Unsupported extends AbstractQueuedSynchronizer { }

  static Thread start(Runnable r) { Thread t = new Thread(r); t.start(); return t; }

  public static void main(String[] args) throws Exception {
    probe("unsupported", () -> { Unsupported u = new Unsupported(); String r = ""; try { u.acquire(1); } catch (UnsupportedOperationException e) { r += "A"; } try { u.release(1); } catch (UnsupportedOperationException e) { r += "R"; } try { u.acquireShared(1); } catch (UnsupportedOperationException e) { r += "S"; } try { u.tryAcquireNanos(1, 5); } catch (Exception e) { r += "N"; } return r; });
    probe("mutex-basic", () -> { Mutex m = new Mutex(); m.acquire(1); boolean held = m.isHeldExclusively(); boolean owner = m.owner() == Thread.currentThread(); m.release(1); return held + " " + owner + " " + m.state() + " " + m.hasQueuedThreads() + " " + m.hasContended() + " " + m.getQueueLength() + " " + m.getFirstQueuedThread(); });
    probe("mutex-contention", () -> {
      try {
        Mutex m = new Mutex();
        int[] counter = {0};
        Thread[] ts = new Thread[4];
        for (int i = 0; i < ts.length; i++) ts[i] = start(() -> { for (int k = 0; k < 500; k++) { m.acquire(1); counter[0]++; m.release(1); } });
        for (Thread t : ts) t.join();
        return counter[0] + " " + m.state() + " " + m.hasQueuedThreads();
      } catch (InterruptedException e) { return e; }
    });
    probe("mutex-queue-inspect", () -> {
      try {
        Mutex m = new Mutex();
        m.acquire(1);
        CountDownLatch started = new CountDownLatch(1);
        Thread t = start(() -> { started.countDown(); m.acquire(1); m.release(1); });
        started.await();
        while (!m.hasQueuedThreads()) Thread.sleep(1);
        String r = m.hasQueuedThreads() + " " + m.getQueueLength() + " " + (m.getFirstQueuedThread() == t) + " " + m.isQueued(t) + " " + m.isQueued(Thread.currentThread()) + " " + m.getQueuedThreads().size() + " " + m.getExclusiveQueuedThreads().size() + " " + m.getSharedQueuedThreads().size() + " " + m.hasQueuedPredecessors() + " " + m.hasContended();
        m.release(1);
        t.join();
        return r + " " + m.hasQueuedThreads();
      } catch (InterruptedException e) { return e; }
    });
    probe("is-queued-null", () -> new Mutex().isQueued(null));
    probe("try-acquire-nanos", () -> {
      try {
        Mutex m = new Mutex();
        m.acquire(1);
        boolean[] got = new boolean[1];
        long[] elapsed = new long[1];
        Thread t = start(() -> { long s = System.nanoTime(); try { got[0] = m.tryAcquireNanos(1, 50_000_000L); } catch (InterruptedException e) { got[0] = true; } elapsed[0] = (System.nanoTime() - s) / 1_000_000L; });
        t.join();
        return got[0] + " " + (elapsed[0] >= 45) + " " + m.hasQueuedThreads();
      } catch (InterruptedException e) { return e; }
    });
    probe("try-acquire-nanos-immediate", () -> { try { Mutex m = new Mutex(); return m.tryAcquireNanos(1, 0) + " " + m.tryAcquireNanos(1, 0) + " " + m.tryAcquireNanos(1, -5); } catch (InterruptedException e) { return e; } });
    probe("interruptible", () -> {
      try {
        Mutex m = new Mutex();
        m.acquire(1);
        String[] result = new String[1];
        Thread t = start(() -> { try { m.acquireInterruptibly(1); result[0] = "acquired"; } catch (InterruptedException e) { result[0] = "interrupted " + Thread.currentThread().isInterrupted(); } });
        while (!m.hasQueuedThreads()) Thread.sleep(1);
        t.interrupt();
        t.join();
        return result[0] + " " + m.hasQueuedThreads();
      } catch (InterruptedException e) { return e; }
    });
    probe("interrupt-preset", () -> { Mutex m = new Mutex(); Thread.currentThread().interrupt(); try { m.acquireInterruptibly(1); return "no"; } catch (InterruptedException e) { return "IE " + Thread.currentThread().isInterrupted(); } });
    probe("uninterruptible-keeps-flag", () -> {
      try {
        Mutex m = new Mutex();
        m.acquire(1);
        boolean[] flag = new boolean[1];
        Thread t = start(() -> { m.acquire(1); flag[0] = Thread.currentThread().isInterrupted(); m.release(1); });
        while (!m.hasQueuedThreads()) Thread.sleep(1);
        t.interrupt();
        Thread.sleep(20);
        boolean stillWaiting = m.hasQueuedThreads();
        m.release(1);
        t.join();
        return stillWaiting + " " + flag[0];
      } catch (InterruptedException e) { return e; }
    });
    probe("release-unheld", () -> new Mutex().release(1));
    probe("condition-signal", () -> {
      try {
        Mutex m = new Mutex();
        Condition c = m.newCondition();
        boolean[] ready = new boolean[1];
        String[] log = new String[1];
        Thread t = start(() -> { m.acquire(1); try { while (!ready[0]) c.await(); log[0] = "woke owner=" + (m.owner() == Thread.currentThread()); } catch (InterruptedException e) { log[0] = "IE"; } finally { m.release(1); } });
        while (true) { m.acquire(1); try { if (m.hasWaiters((AbstractQueuedSynchronizer.ConditionObject) c)) break; } finally { m.release(1); } Thread.sleep(1); }
        m.acquire(1);
        String r = m.hasWaiters((AbstractQueuedSynchronizer.ConditionObject) c) + " " + m.getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject) c) + " " + m.getWaitingThreads((AbstractQueuedSynchronizer.ConditionObject) c).size() + " " + m.owns((AbstractQueuedSynchronizer.ConditionObject) c);
        ready[0] = true;
        c.signal();
        m.release(1);
        t.join();
        return r + " " + log[0];
      } catch (InterruptedException e) { return e; }
    });
    probe("condition-signal-all", () -> {
      try {
        Mutex m = new Mutex();
        Condition c = m.newCondition();
        AtomicInteger woke = new AtomicInteger();
        boolean[] go = new boolean[1];
        Thread[] ts = new Thread[3];
        for (int i = 0; i < ts.length; i++) ts[i] = start(() -> { m.acquire(1); try { while (!go[0]) c.awaitUninterruptibly(); woke.incrementAndGet(); } finally { m.release(1); } });
        while (true) { m.acquire(1); int n = m.getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject) c); m.release(1); if (n == 3) break; Thread.sleep(1); }
        m.acquire(1);
        go[0] = true;
        c.signalAll();
        m.release(1);
        for (Thread t : ts) t.join();
        return woke.get();
      } catch (InterruptedException e) { return e; }
    });
    probe("condition-timeouts", () -> {
      try {
        Mutex m = new Mutex();
        Condition c = m.newCondition();
        m.acquire(1);
        long s = System.nanoTime();
        boolean a = c.await(30, TimeUnit.MILLISECONDS);
        long rem = c.awaitNanos(20_000_000L);
        boolean b = c.awaitUntil(new Date(System.currentTimeMillis() + 20));
        long ms = (System.nanoTime() - s) / 1_000_000L;
        boolean owned = m.isHeldExclusively();
        m.release(1);
        return a + " " + (rem <= 0) + " " + b + " " + (ms >= 60) + " " + owned;
      } catch (InterruptedException e) { return e; }
    });
    probe("condition-without-lock", () -> { Mutex m = new Mutex(); Condition c = m.newCondition(); String r = ""; try { c.signal(); } catch (IllegalMonitorStateException e) { r += "S"; } try { c.await(); } catch (IllegalMonitorStateException e) { r += "A"; } catch (InterruptedException e) { r += "I"; } try { c.signalAll(); } catch (IllegalMonitorStateException e) { r += "L"; } return r + " " + m.state(); });
    probe("condition-interrupt", () -> {
      try {
        Mutex m = new Mutex();
        Condition c = m.newCondition();
        String[] result = new String[1];
        Thread t = start(() -> { m.acquire(1); try { c.await(); result[0] = "signalled"; } catch (InterruptedException e) { result[0] = "IE owner=" + (m.owner() == Thread.currentThread()); } finally { m.release(1); } });
        while (true) { m.acquire(1); int n = m.getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject) c); m.release(1); if (n == 1) break; Thread.sleep(1); }
        t.interrupt();
        t.join();
        return result[0] + " " + m.state();
      } catch (InterruptedException e) { return e; }
    });
    probe("condition-foreign", () -> { Mutex a = new Mutex(), b = new Mutex(); Condition c = a.newCondition(); try { b.hasWaiters((AbstractQueuedSynchronizer.ConditionObject) c); return "no"; } catch (IllegalArgumentException e) { return e.getMessage() + " " + b.owns((AbstractQueuedSynchronizer.ConditionObject) c); } });
    probe("latch", () -> {
      try {
        Latch l = new Latch(3);
        AtomicInteger passed = new AtomicInteger();
        Thread[] ts = new Thread[4];
        for (int i = 0; i < ts.length; i++) ts[i] = start(() -> { try { l.acquireSharedInterruptibly(1); passed.incrementAndGet(); } catch (InterruptedException e) { } });
        while (l.getQueueLength() < 4) Thread.sleep(1);
        String before = passed.get() + " " + l.getSharedQueuedThreads().size() + " " + l.getExclusiveQueuedThreads().size();
        l.releaseShared(1); l.releaseShared(1);
        Thread.sleep(10);
        String mid = "" + passed.get();
        l.releaseShared(1);
        for (Thread t : ts) t.join();
        return before + " " + mid + " " + passed.get() + " " + l.state() + " " + l.tryAcquireSharedNanos(1, 1);
      } catch (InterruptedException e) { return e; }
    });
    probe("latch-timed", () -> { try { Latch l = new Latch(1); return l.tryAcquireSharedNanos(1, 20_000_000L) + " " + l.releaseShared(1) + " " + l.tryAcquireSharedNanos(1, 20_000_000L) + " " + l.releaseShared(1); } catch (InterruptedException e) { return e; } });
    probe("long-semaphore", () -> {
      try {
        LongSem s = new LongSem(2L);
        s.acquireShared(1L);
        s.acquireShared(1L);
        boolean none = s.tryAcquireSharedNanos(1L, 10_000_000L);
        AtomicInteger got = new AtomicInteger();
        Thread t = start(() -> { s.acquireShared(5L); got.incrementAndGet(); });
        while (!s.hasQueuedThreads()) Thread.sleep(1);
        s.releaseShared(5L);
        t.join();
        return none + " " + got.get() + " " + s.state() + " " + s.hasQueuedThreads();
      } catch (InterruptedException e) { return e; }
    });
    probe("long-condition", () -> {
      try {
        class LM extends AbstractQueuedLongSynchronizer {
          protected boolean isHeldExclusively() { return getState() == 1L; }
          protected boolean tryAcquire(long a) { return compareAndSetState(0L, 1L); }
          protected boolean tryRelease(long a) { setState(0L); return true; }
          long state() { return getState(); }
        }
        LM m = new LM();
        AbstractQueuedLongSynchronizer.ConditionObject c = m.new ConditionObject();
        m.acquire(1L);
        boolean r = c.await(10, TimeUnit.MILLISECONDS);
        long state = m.state();
        m.release(1L);
        return r + " " + state;
      } catch (InterruptedException e) { return e; }
    });
    probe("to-string", () -> { Mutex m = new Mutex(); String a = m.toString().replaceAll("@[0-9a-f]+", "@X"); m.acquire(1); return a + " | " + m.toString().replaceAll("@[0-9a-f]+", "@X"); });
    probe("ownable", () -> { Mutex m = new Mutex(); m.acquire(1); Thread o = m.owner(); m.release(1); return (o == Thread.currentThread()) + " " + m.owner(); });
    probe("fifo-order", () -> {
      try {
        Mutex m = new Mutex();
        m.acquire(1);
        List<Integer> order = Collections.synchronizedList(new ArrayList<>());
        Thread[] ts = new Thread[4];
        for (int i = 0; i < ts.length; i++) {
          final int id = i;
          ts[i] = start(() -> { m.acquire(1); order.add(id); m.release(1); });
          while (m.getQueueLength() < i + 1) Thread.sleep(1);
        }
        m.release(1);
        for (Thread t : ts) t.join();
        return order;
      } catch (InterruptedException e) { return e; }
    });
    probe("producer-consumer", () -> {
      try {
        Mutex m = new Mutex();
        Condition notEmpty = m.newCondition(), notFull = m.newCondition();
        ArrayDeque<Integer> buf = new ArrayDeque<>();
        long[] sum = {0};
        Thread prod = start(() -> { for (int i = 1; i <= 200; i++) { m.acquire(1); try { while (buf.size() >= 5) notFull.awaitUninterruptibly(); buf.add(i); notEmpty.signal(); } finally { m.release(1); } } });
        Thread cons = start(() -> { for (int i = 1; i <= 200; i++) { m.acquire(1); try { while (buf.isEmpty()) notEmpty.awaitUninterruptibly(); sum[0] += buf.poll(); notFull.signal(); } finally { m.release(1); } } });
        prod.join(); cons.join();
        return sum[0] + " " + buf.size();
      } catch (InterruptedException e) { return e; }
    });
  }
}
