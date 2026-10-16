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

public class ExecutorsModern {
  interface Action { Object run() throws Throwable; }

  static String clean(String text) {
    return text == null ? null : text.replaceAll("active threads = \\d+", "active threads = N")
        .replaceAll("ExecutorsModern\\$\\$Lambda[^ ]*@[0-9a-f]+", "LAMBDA").replaceAll("Lambda-\\d+@(0x)?[0-9a-f]+", "LAMBDA")
        .replaceAll("@(0x)?[0-9a-f]{4,}", "@H");
  }

  static void probe(String label, Action action) {
    try {
      System.out.println(label + ":" + clean(String.valueOf(action.run())));
    } catch (Throwable error) {
      boolean generated = error instanceof NullPointerException || error instanceof ClassCastException;
      Throwable cause = error.getCause();
      System.out.println(label + ":" + error.getClass().getName() + (generated ? "" : ":" + clean(error.getMessage()))
          + (cause != null && !generated ? "<-" + cause.getClass().getName() + ":" + clean(cause.getMessage()) : ""));
    }
  }

  public static void main(String[] args) throws Exception {
    probe("fixed-pool-futures", () -> {
      ExecutorService ex = Executors.newFixedThreadPool(3);
      List<Future<Integer>> fs = new ArrayList<>();
      for (int i = 0; i < 6; ++i) { final int n = i; fs.add(ex.submit(() -> n * n)); }
      int sum = 0; for (Future<Integer> f : fs) sum += f.get();
      ex.shutdown();
      return sum + ":" + ex.awaitTermination(5, TimeUnit.SECONDS) + ":" + ex.isShutdown() + ":" + ex.isTerminated();
    });
    probe("single-thread-order", () -> {
      ExecutorService ex = Executors.newSingleThreadExecutor();
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < 8; ++i) { final int n = i; ex.execute(() -> sb.append(n)); }
      ex.shutdown(); ex.awaitTermination(5, TimeUnit.SECONDS);
      return sb;
    });
    probe("future-exception", () -> { ExecutorService ex = Executors.newSingleThreadExecutor(); try { return ex.submit(() -> { throw new IllegalStateException("boom"); }).get(); } finally { ex.shutdown(); } });
    probe("future-cancel", () -> {
      ExecutorService ex = Executors.newSingleThreadExecutor();
      CountDownLatch started = new CountDownLatch(1);
      Future<?> f = ex.submit(() -> { started.countDown(); try { Thread.sleep(5000); } catch (InterruptedException e) { } });
      started.await();
      boolean c = f.cancel(true);
      ex.shutdownNow();
      return c + ":" + f.isCancelled() + ":" + f.isDone();
    });
    probe("future-get-cancelled", () -> { FutureTask<Integer> t = new FutureTask<>(() -> 1); t.cancel(false); return t.get(); });
    probe("future-timeout", () -> { ExecutorService ex = Executors.newSingleThreadExecutor(); try { return ex.submit(() -> { Thread.sleep(2000); return 1; }).get(50, TimeUnit.MILLISECONDS); } finally { ex.shutdownNow(); } });
    probe("future-task-states", () -> { FutureTask<String> t = new FutureTask<>(() -> "x"); String before = t.isDone() + "," + t.state(); t.run(); return before + ":" + t.isDone() + "," + t.state() + "," + t.resultNow(); });
    probe("invoke-all", () -> { ExecutorService ex = Executors.newFixedThreadPool(2); try { List<Callable<Integer>> tasks = List.of(() -> 1, () -> 2, () -> 3); int s = 0; for (Future<Integer> f : ex.invokeAll(tasks)) s += f.get(); return s; } finally { ex.shutdown(); } });
    probe("invoke-any", () -> { ExecutorService ex = Executors.newFixedThreadPool(2); try { return ex.invokeAny(List.of(() -> { Thread.sleep(500); return "slow"; }, () -> "fast")); } finally { ex.shutdownNow(); } });
    probe("rejected-after-shutdown", () -> { ExecutorService ex = Executors.newSingleThreadExecutor(); ex.shutdown(); ex.execute(() -> { }); return 0; });
    probe("submit-null", () -> { ExecutorService ex = Executors.newSingleThreadExecutor(); try { return ex.submit((Runnable) null); } finally { ex.shutdown(); } });
    probe("shutdown-now-list", () -> {
      ExecutorService ex = Executors.newSingleThreadExecutor();
      CountDownLatch started = new CountDownLatch(1);
      ex.execute(() -> { started.countDown(); try { Thread.sleep(3000); } catch (InterruptedException e) { } });
      started.await();
      for (int i = 0; i < 3; ++i) ex.execute(() -> { });
      List<Runnable> pending = ex.shutdownNow();
      return pending.size();
    });
    probe("tpe-config", () -> {
      ThreadPoolExecutor ex = new ThreadPoolExecutor(1, 2, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1));
      ex.prestartAllCoreThreads();
      String s = ex.getCorePoolSize() + "," + ex.getMaximumPoolSize() + "," + ex.getPoolSize() + "," + ex.getKeepAliveTime(TimeUnit.MILLISECONDS) + "," + ex.getQueue().remainingCapacity();
      ex.shutdownNow();
      return s;
    });
    probe("tpe-reject-abort", () -> {
      ThreadPoolExecutor ex = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1));
      CountDownLatch gate = new CountDownLatch(1);
      try { for (int i = 0; i < 3; ++i) ex.execute(() -> { try { gate.await(); } catch (InterruptedException e) { } }); return "none"; }
      finally { gate.countDown(); ex.shutdown(); }
    });
    probe("tpe-reject-caller-runs", () -> {
      ThreadPoolExecutor ex = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
      CountDownLatch gate = new CountDownLatch(1);
      ex.execute(() -> { try { gate.await(); } catch (InterruptedException e) { } });
      Thread[] ran = new Thread[1];
      ex.execute(() -> ran[0] = Thread.currentThread());
      gate.countDown(); ex.shutdown();
      return ran[0] == Thread.currentThread();
    });
    probe("tpe-reject-discard", () -> {
      ThreadPoolExecutor ex = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.DiscardPolicy());
      CountDownLatch gate = new CountDownLatch(1);
      ex.execute(() -> { try { gate.await(); } catch (InterruptedException e) { } });
      AtomicInteger ran = new AtomicInteger();
      ex.execute(ran::incrementAndGet);
      gate.countDown(); ex.shutdown(); ex.awaitTermination(2, TimeUnit.SECONDS);
      return ran.get();
    });
    probe("tpe-counts", () -> {
      ThreadPoolExecutor ex = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
      for (int i = 0; i < 5; ++i) ex.execute(() -> { });
      ex.shutdown(); ex.awaitTermination(5, TimeUnit.SECONDS);
      return ex.getCompletedTaskCount() + ":" + ex.getTaskCount() + ":" + ex.getActiveCount();
    });
    probe("tpe-hooks", () -> {
      List<String> log = Collections.synchronizedList(new ArrayList<>());
      ThreadPoolExecutor ex = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>()) {
        protected void beforeExecute(Thread t, Runnable r) { log.add("before"); }
        protected void afterExecute(Runnable r, Throwable t) { log.add("after:" + (t == null)); }
        protected void terminated() { log.add("terminated"); }
      };
      ex.execute(() -> log.add("run"));
      ex.shutdown(); ex.awaitTermination(5, TimeUnit.SECONDS);
      return log;
    });
    probe("thread-factory-names", () -> {
      ThreadFactory tf = Executors.defaultThreadFactory();
      Thread t = tf.newThread(() -> { });
      return t.getName().startsWith("pool-") + ":" + t.isDaemon() + ":" + t.getPriority();
    });
    probe("custom-thread-factory", () -> {
      AtomicInteger n = new AtomicInteger();
      ExecutorService ex = Executors.newFixedThreadPool(2, r -> { Thread t = new Thread(r, "custom-" + n.incrementAndGet()); t.setDaemon(true); return t; });
      String name = ex.submit(() -> Thread.currentThread().getName()).get();
      ex.shutdown();
      return name;
    });
    probe("cached-pool", () -> { ExecutorService ex = Executors.newCachedThreadPool(); int s = 0; List<Callable<Integer>> tasks = List.of(() -> 1, () -> 2, () -> 3); for (Future<Integer> f : ex.invokeAll(tasks)) s += f.get(); ex.shutdown(); return s; });
    probe("scheduled-delay", () -> {
      ScheduledExecutorService ex = Executors.newScheduledThreadPool(1);
      long start = System.nanoTime();
      ScheduledFuture<String> f = ex.schedule(() -> "done", 100, TimeUnit.MILLISECONDS);
      String r = f.get();
      long ms = (System.nanoTime() - start) / 1_000_000;
      ex.shutdown();
      return r + ":" + (ms >= 95 && ms < 1000);
    });
    probe("scheduled-fixed-rate", () -> {
      ScheduledExecutorService ex = Executors.newScheduledThreadPool(1);
      AtomicInteger n = new AtomicInteger();
      CountDownLatch latch = new CountDownLatch(3);
      ScheduledFuture<?> f = ex.scheduleAtFixedRate(() -> { n.incrementAndGet(); latch.countDown(); }, 0, 20, TimeUnit.MILLISECONDS);
      latch.await(5, TimeUnit.SECONDS);
      f.cancel(false);
      ex.shutdown();
      return n.get() >= 3;
    });
    probe("scheduled-fixed-delay", () -> {
      ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
      CountDownLatch latch = new CountDownLatch(2);
      ex.scheduleWithFixedDelay(latch::countDown, 10, 10, TimeUnit.MILLISECONDS);
      boolean ok = latch.await(5, TimeUnit.SECONDS);
      ex.shutdownNow();
      return ok;
    });
    probe("scheduled-cancel", () -> {
      ScheduledExecutorService ex = Executors.newScheduledThreadPool(1);
      ScheduledFuture<?> f = ex.schedule(() -> { }, 10, TimeUnit.SECONDS);
      boolean c = f.cancel(false);
      ex.shutdownNow();
      return c + ":" + f.isCancelled() + ":" + (f.getDelay(TimeUnit.SECONDS) > 0);
    });
    probe("scheduled-exception-stops", () -> {
      ScheduledExecutorService ex = Executors.newScheduledThreadPool(1);
      AtomicInteger n = new AtomicInteger();
      ScheduledFuture<?> f = ex.scheduleAtFixedRate(() -> { if (n.incrementAndGet() == 2) throw new RuntimeException("stop"); }, 0, 10, TimeUnit.MILLISECONDS);
      try { f.get(5, TimeUnit.SECONDS); return "no"; } catch (ExecutionException e) { return e.getCause().getMessage() + ":" + n.get(); } finally { ex.shutdownNow(); }
    });
    probe("completion-service", () -> {
      ExecutorService ex = Executors.newFixedThreadPool(2);
      CompletionService<Integer> cs = new ExecutorCompletionService<>(ex);
      cs.submit(() -> { Thread.sleep(200); return 1; });
      cs.submit(() -> 2);
      int first = cs.take().get(); int second = cs.take().get();
      ex.shutdown();
      return first + "," + second;
    });
    probe("virtual-thread-executor", () -> { try (ExecutorService ex = Executors.newVirtualThreadPerTaskExecutor()) { return ex.submit(() -> Thread.currentThread().isVirtual()).get(); } });
    probe("try-with-resources-executor", () -> { ExecutorService ex = Executors.newFixedThreadPool(1); Future<Integer> f; try (ex) { f = ex.submit(() -> 7); } return f.get() + ":" + ex.isTerminated(); });

    probe("completable-basic", () -> CompletableFuture.supplyAsync(() -> 20).thenApply(x -> x + 1).thenCombine(CompletableFuture.completedFuture(2), (a, b) -> a * b).get());
    probe("completable-compose", () -> CompletableFuture.supplyAsync(() -> 5).thenCompose(x -> CompletableFuture.supplyAsync(() -> x * 3)).join());
    probe("completable-exception", () -> CompletableFuture.supplyAsync(() -> { throw new IllegalArgumentException("bad"); }).exceptionally(t -> t.getClass().getSimpleName() + ":" + t.getCause().getMessage()).join());
    probe("completable-handle", () -> CompletableFuture.<Integer>failedFuture(new RuntimeException("x")).handle((v, t) -> v == null ? "failed:" + t.getMessage() : "ok").join());
    probe("completable-join-throws", () -> CompletableFuture.supplyAsync(() -> { throw new IllegalStateException("inner"); }).join());
    probe("completable-get-throws", () -> CompletableFuture.supplyAsync(() -> { throw new IllegalStateException("inner"); }).get());
    probe("completable-all-of", () -> { CompletableFuture<Integer> a = CompletableFuture.supplyAsync(() -> 1), b = CompletableFuture.supplyAsync(() -> 2); CompletableFuture.allOf(a, b).join(); return a.join() + b.join(); });
    probe("completable-any-of", () -> CompletableFuture.anyOf(CompletableFuture.completedFuture("a"), new CompletableFuture<String>()).join());
    probe("completable-timeout", () -> new CompletableFuture<String>().orTimeout(50, TimeUnit.MILLISECONDS).join());
    probe("completable-complete-on-timeout", () -> new CompletableFuture<String>().completeOnTimeout("dflt", 50, TimeUnit.MILLISECONDS).join());
    probe("completable-manual", () -> { CompletableFuture<String> f = new CompletableFuture<>(); String s = f.isDone() + ","; f.complete("v"); return s + f.isDone() + "," + f.complete("w") + "," + f.join() + "," + f.getNow("x"); });
    probe("completable-cancel", () -> { CompletableFuture<String> f = new CompletableFuture<>(); f.cancel(true); return f.isCancelled() + ":" + f.isCompletedExceptionally(); });
    probe("completable-when-complete", () -> { List<String> log = new ArrayList<>(); CompletableFuture.completedFuture(1).whenComplete((v, t) -> log.add("v" + v)).join(); return log; });
    probe("completable-accept-run", () -> { StringBuilder sb = new StringBuilder(); CompletableFuture.completedFuture("a").thenAccept(sb::append).thenRun(() -> sb.append("!")).join(); return sb; });
    probe("completable-either", () -> CompletableFuture.completedFuture(1).applyToEither(new CompletableFuture<Integer>(), x -> x + 10).join());
    probe("completable-state", () -> { CompletableFuture<String> f = CompletableFuture.completedFuture("s"); return f.state() + ":" + f.resultNow(); });
    probe("completable-delayed", () -> { long start = System.nanoTime(); String r = CompletableFuture.supplyAsync(() -> "x", CompletableFuture.delayedExecutor(80, TimeUnit.MILLISECONDS)).join(); return r + ":" + ((System.nanoTime() - start) / 1_000_000 >= 70); });

    probe("latch", () -> { CountDownLatch l = new CountDownLatch(2); l.countDown(); String s = l.getCount() + ":" + l.await(10, TimeUnit.MILLISECONDS); l.countDown(); return s + ":" + l.getCount() + ":" + l.await(10, TimeUnit.MILLISECONDS); });
    probe("latch-negative", () -> new CountDownLatch(-1));
    probe("semaphore", () -> { Semaphore s = new Semaphore(2); s.acquire(); boolean a = s.tryAcquire(); boolean b = s.tryAcquire(); s.release(2); return a + ":" + b + ":" + s.availablePermits() + ":" + s.tryAcquire(3) + ":" + s.drainPermits(); });
    probe("semaphore-fair", () -> { Semaphore s = new Semaphore(1, true); return s.isFair() + ":" + s.tryAcquire(10, TimeUnit.MILLISECONDS) + ":" + s.tryAcquire(10, TimeUnit.MILLISECONDS) + ":" + s; });
    probe("cyclic-barrier", () -> {
      CyclicBarrier b = new CyclicBarrier(3, () -> { });
      AtomicInteger arrived = new AtomicInteger();
      Thread[] ts = new Thread[2];
      for (int i = 0; i < 2; ++i) { ts[i] = new Thread(() -> { try { b.await(); arrived.incrementAndGet(); } catch (Exception e) { } }); ts[i].start(); }
      b.await();
      for (Thread t : ts) t.join();
      return arrived.get() + ":" + b.getParties() + ":" + b.getNumberWaiting() + ":" + b.isBroken();
    });
    probe("barrier-timeout", () -> { CyclicBarrier b = new CyclicBarrier(2); try { b.await(30, TimeUnit.MILLISECONDS); return "no"; } catch (TimeoutException e) { return "timeout:" + b.isBroken(); } });
    probe("phaser", () -> { Phaser p = new Phaser(1); int a = p.arrive(); int b = p.arriveAndAwaitAdvance(); return a + ":" + b + ":" + p.getPhase() + ":" + p.getRegisteredParties() + ":" + p.register() + ":" + p.getRegisteredParties(); });
    probe("exchanger", () -> {
      Exchanger<String> ex = new Exchanger<>();
      String[] other = new String[1];
      Thread t = new Thread(() -> { try { other[0] = ex.exchange("from-thread"); } catch (InterruptedException e) { } });
      t.start();
      String mine = ex.exchange("from-main");
      t.join();
      return mine + ":" + other[0];
    });
    probe("reentrant-lock", () -> { ReentrantLock l = new ReentrantLock(); l.lock(); l.lock(); String s = l.getHoldCount() + ":" + l.isLocked() + ":" + l.isHeldByCurrentThread(); l.unlock(); l.unlock(); return s + ":" + l.isLocked() + ":" + l.tryLock() + ":" + l.isFair(); });
    probe("reentrant-lock-unlock-illegal", () -> { new ReentrantLock().unlock(); return 0; });
    probe("lock-condition", () -> {
      ReentrantLock l = new ReentrantLock(); Condition c = l.newCondition();
      boolean[] flag = new boolean[1];
      Thread t = new Thread(() -> { l.lock(); try { flag[0] = true; c.signalAll(); } finally { l.unlock(); } });
      l.lock();
      try { t.start(); while (!flag[0]) c.await(); } finally { l.unlock(); }
      t.join();
      return flag[0];
    });
    probe("condition-timeout", () -> { ReentrantLock l = new ReentrantLock(); Condition c = l.newCondition(); l.lock(); try { return c.await(20, TimeUnit.MILLISECONDS); } finally { l.unlock(); } });
    probe("condition-without-lock", () -> { new ReentrantLock().newCondition().signal(); return 0; });
    probe("rw-lock", () -> { ReentrantReadWriteLock rw = new ReentrantReadWriteLock(); rw.readLock().lock(); rw.readLock().lock(); String s = rw.getReadLockCount() + ":" + rw.isWriteLocked() + ":" + rw.writeLock().tryLock(); rw.readLock().unlock(); rw.readLock().unlock(); boolean w = rw.writeLock().tryLock(); return s + ":" + w + ":" + rw.isWriteLocked() + ":" + rw.getWriteHoldCount(); });
    probe("stamped-lock", () -> { StampedLock sl = new StampedLock(); long o = sl.tryOptimisticRead(); boolean v = sl.validate(o); long w = sl.writeLock(); boolean v2 = sl.validate(o); sl.unlockWrite(w); return v + ":" + v2 + ":" + sl.isWriteLocked() + ":" + (sl.tryReadLock() != 0); });
    probe("lock-support", () -> { long t0 = System.nanoTime(); LockSupport.parkNanos(20_000_000L); return (System.nanoTime() - t0) >= 15_000_000L; });
    probe("lock-support-unpark", () -> { Thread t = new Thread(LockSupport::park); t.start(); Thread.sleep(50); LockSupport.unpark(t); t.join(2000); return !t.isAlive(); });

    probe("atomic-int", () -> { AtomicInteger a = new AtomicInteger(5); return a.incrementAndGet() + ":" + a.getAndAdd(4) + ":" + a.compareAndSet(10, 1) + ":" + a.updateAndGet(x -> x * 7) + ":" + a.accumulateAndGet(3, Integer::sum) + ":" + a; });
    probe("atomic-long", () -> { AtomicLong a = new AtomicLong(Long.MAX_VALUE); return a.incrementAndGet() + ":" + a.getAndSet(5) + ":" + a.addAndGet(2) + ":" + a.intValue() + ":" + a.compareAndExchange(7, 9) + ":" + a.get(); });
    probe("atomic-bool-ref", () -> { AtomicBoolean b = new AtomicBoolean(); AtomicReference<String> r = new AtomicReference<>("a"); return b.getAndSet(true) + ":" + b.compareAndSet(true, false) + ":" + r.getAndUpdate(s -> s + "b") + ":" + r.accumulateAndGet("c", String::concat) + ":" + r.compareAndSet("abc", "z") + ":" + r; });
    probe("atomic-arrays", () -> { AtomicIntegerArray a = new AtomicIntegerArray(3); a.set(1, 4); a.incrementAndGet(1); a.addAndGet(2, 9); AtomicReferenceArray<String> r = new AtomicReferenceArray<>(2); r.set(0, "q"); return a + ":" + r + ":" + a.length() + ":" + a.compareAndSet(0, 0, 3); });
    probe("long-adder", () -> { LongAdder a = new LongAdder(); for (int i = 0; i < 100; ++i) a.add(i); a.increment(); return a.sum() + ":" + a.sumThenReset() + ":" + a.sum() + ":" + a; });
    probe("accumulators", () -> { LongAccumulator m = new LongAccumulator(Long::max, Long.MIN_VALUE); m.accumulate(5); m.accumulate(9); m.accumulate(2); DoubleAdder d = new DoubleAdder(); d.add(1.5); d.add(2.5); return m.get() + ":" + d.sum() + ":" + m; });
    probe("stamped-ref", () -> { AtomicStampedReference<String> r = new AtomicStampedReference<>("a", 1); int[] h = new int[1]; String v = r.get(h); boolean ok = r.compareAndSet("a", "b", 1, 2); return v + h[0] + ":" + ok + ":" + r.getReference() + r.getStamp() + ":" + r.compareAndSet("b", "c", 1, 3); });
    probe("thread-local-random", () -> { int v = ThreadLocalRandom.current().nextInt(5, 10); return v >= 5 && v < 10; });
    probe("thread-local-random-bad", () -> ThreadLocalRandom.current().nextInt(5, 5));

    probe("time-unit", () -> TimeUnit.SECONDS.toMillis(3) + ":" + TimeUnit.MILLISECONDS.toSeconds(2500) + ":" + TimeUnit.HOURS.toMinutes(2) + ":" + TimeUnit.DAYS.toHours(1) + ":" + TimeUnit.NANOSECONDS.convert(1, TimeUnit.MICROSECONDS) + ":" + TimeUnit.SECONDS.toNanos(Long.MAX_VALUE) + ":" + TimeUnit.valueOf("MINUTES"));
    probe("blocking-queue-producer-consumer", () -> {
      BlockingQueue<Integer> q = new ArrayBlockingQueue<>(2);
      Thread prod = new Thread(() -> { try { for (int i = 0; i < 10; ++i) q.put(i); q.put(-1); } catch (InterruptedException e) { } });
      prod.start();
      int sum = 0; for (int v; (v = q.take()) >= 0;) sum += v;
      prod.join();
      return sum;
    });
    probe("lbq-timeouts", () -> { LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>(1); q.put(1); return q.offer(2, 20, TimeUnit.MILLISECONDS) + ":" + q.poll(20, TimeUnit.MILLISECONDS) + ":" + q.poll(20, TimeUnit.MILLISECONDS); });
    probe("lbq-drain", () -> { LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>(List.of(1, 2, 3, 4)); List<Integer> out = new ArrayList<>(); return q.drainTo(out, 3) + ":" + out + ":" + q; });
    probe("delay-queue", () -> {
      class D implements Delayed {
        final long at; final String n; D(String n, long ms) { this.n = n; at = System.nanoTime() + ms * 1_000_000; }
        public long getDelay(TimeUnit u) { return u.convert(at - System.nanoTime(), TimeUnit.NANOSECONDS); }
        public int compareTo(Delayed o) { return Long.compare(at, ((D) o).at); }
        public String toString() { return n; }
      }
      DelayQueue<D> q = new DelayQueue<>(); q.add(new D("late", 120)); q.add(new D("early", 30));
      return q.poll() + ":" + q.take() + ":" + q.take() + ":" + q.size();
    });
    probe("priority-blocking", () -> { PriorityBlockingQueue<Integer> q = new PriorityBlockingQueue<>(); for (int x : new int[] {5, 1, 3}) q.add(x); return q.take() + ":" + q.poll() + ":" + q.size() + ":" + q.peek(); });
    probe("synchronous-queue", () -> { SynchronousQueue<String> q = new SynchronousQueue<>(); Thread t = new Thread(() -> { try { q.put("hand-off"); } catch (InterruptedException e) { } }); t.start(); String r = q.take(); t.join(); return r + ":" + q.offer("x") + ":" + q.poll() + ":" + q.size(); });
    probe("linked-blocking-deque", () -> { LinkedBlockingDeque<Integer> d = new LinkedBlockingDeque<>(3); d.addFirst(1); d.addLast(2); d.offerFirst(0); return d + ":" + d.offerLast(9) + ":" + d.pollLast() + ":" + d.takeFirst() + ":" + d.remainingCapacity(); });
    probe("transfer-queue", () -> { LinkedTransferQueue<Integer> q = new LinkedTransferQueue<>(); q.add(1); return q.tryTransfer(2) + ":" + q.hasWaitingConsumer() + ":" + q.size() + ":" + q.poll(); });
    probe("cow-list-semantics", () -> { CopyOnWriteArrayList<Integer> l = new CopyOnWriteArrayList<>(List.of(1, 2, 3)); Iterator<Integer> it = l.iterator(); l.add(4); StringBuilder sb = new StringBuilder(); while (it.hasNext()) sb.append(it.next()); return sb + ":" + l + ":" + l.addIfAbsent(4) + ":" + l.addIfAbsent(5) + ":" + l.size(); });
    probe("cow-list-iterator-remove", () -> { Iterator<Integer> it = new CopyOnWriteArrayList<>(List.of(1)).iterator(); it.next(); it.remove(); return 0; });
    probe("cow-list-sublist", () -> { CopyOnWriteArrayList<Integer> l = new CopyOnWriteArrayList<>(List.of(1, 2, 3, 4)); List<Integer> s = l.subList(1, 3); l.add(9); return s.size(); });
    probe("cow-ops", () -> { CopyOnWriteArrayList<Integer> l = new CopyOnWriteArrayList<>(); l.addAll(List.of(3, 1, 2)); l.sort(null); l.removeIf(x -> x == 2); l.replaceAll(x -> x * 2); return l + ":" + l.indexOf(6) + ":" + l.contains(2) + ":" + l.equals(List.of(2, 6)) + ":" + l.hashCode(); });
    probe("skip-list-order", () -> { ConcurrentSkipListMap<String, Integer> m = new ConcurrentSkipListMap<>(); for (String w : new String[] {"pear", "apple", "fig", "banana"}) m.put(w, w.length()); return m + ":" + m.firstEntry() + ":" + m.ceilingKey("b") + ":" + m.headMap("c") + ":" + m.descendingKeySet() + ":" + m.pollFirstEntry() + ":" + m.size(); });
    probe("skip-list-concurrent-modification", () -> { ConcurrentSkipListMap<Integer, Integer> m = new ConcurrentSkipListMap<>(); for (int i = 0; i < 5; ++i) m.put(i, i); int n = 0; for (int k : m.keySet()) { m.put(k + 100, 0); if (++n > 20) break; } return m.size() >= 5; });
    probe("skip-list-null", () -> new ConcurrentSkipListMap<String, Integer>().put("a", null));
    probe("skip-list-compute", () -> { ConcurrentSkipListMap<String, Integer> m = new ConcurrentSkipListMap<>(); m.merge("a", 1, Integer::sum); m.merge("a", 2, Integer::sum); m.computeIfAbsent("b", k -> 5); m.putIfAbsent("b", 9); return m; });
    probe("skip-list-set", () -> { ConcurrentSkipListSet<Integer> s = new ConcurrentSkipListSet<>(Comparator.reverseOrder()); s.addAll(List.of(1, 5, 3)); return s + ":" + s.first() + ":" + s.higher(3) + ":" + s.headSet(3); });
    probe("thread-basics", () -> { Thread t = new Thread(() -> { }, "named"); return t.getName() + ":" + t.getState() + ":" + t.isAlive() + ":" + t.isDaemon() + ":" + t.getPriority(); });
    probe("thread-join-state", () -> { Thread t = new Thread(() -> { }); t.start(); t.join(); return t.getState() + ":" + t.isAlive(); });
    probe("thread-interrupt", () -> { Thread t = new Thread(() -> { try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }); t.start(); t.interrupt(); t.join(2000); return t.isInterrupted() + ":" + t.isAlive(); });
    probe("thread-uncaught", () -> { Thread t = new Thread(() -> { throw new IllegalStateException("uncaught"); }); String[] seen = new String[1]; t.setUncaughtExceptionHandler((th, e) -> seen[0] = e.getMessage()); t.start(); t.join(); return seen[0]; });
    probe("thread-start-twice", () -> { Thread t = new Thread(() -> { }); t.start(); t.join(); t.start(); return 0; });
    probe("thread-builder", () -> { Thread t = Thread.ofPlatform().name("b-", 1).daemon(true).unstarted(() -> { }); return t.getName() + ":" + t.isDaemon(); });
    probe("virtual-thread", () -> { Thread t = Thread.ofVirtual().name("v").start(() -> { }); t.join(); return t.isVirtual() + ":" + t.getName() + ":" + t.isDaemon(); });
    probe("thread-local", () -> { ThreadLocal<Integer> tl = ThreadLocal.withInitial(() -> 7); tl.set(8); int[] other = new int[1]; Thread t = new Thread(() -> other[0] = tl.get()); t.start(); t.join(); return tl.get() + ":" + other[0]; });
    probe("inheritable-thread-local", () -> { InheritableThreadLocal<String> tl = new InheritableThreadLocal<>(); tl.set("inherit"); String[] seen = new String[1]; Thread t = new Thread(() -> seen[0] = tl.get()); t.start(); t.join(); return seen[0]; });
    probe("synchronized-wait-notify", () -> { Object lock = new Object(); boolean[] done = new boolean[1]; Thread t = new Thread(() -> { synchronized (lock) { done[0] = true; lock.notifyAll(); } }); synchronized (lock) { t.start(); while (!done[0]) lock.wait(); } return done[0]; });
    probe("wait-without-monitor", () -> { new Object().wait(); return 0; });
    probe("notify-without-monitor", () -> { new Object().notify(); return 0; });
  }
}
