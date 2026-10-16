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
import java.util.function.Supplier;

public class ForkJoinModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  static class Fib extends RecursiveTask<Integer> {
    final int n;
    Fib(int n) { this.n = n; }
    protected Integer compute() {
      if (n <= 1) return n;
      Fib f1 = new Fib(n - 1);
      f1.fork();
      Fib f2 = new Fib(n - 2);
      return f2.compute() + f1.join();
    }
  }

  static class SumAction extends RecursiveAction {
    final long[] data; final int lo, hi; final AtomicLong total;
    SumAction(long[] d, int lo, int hi, AtomicLong t) { data = d; this.lo = lo; this.hi = hi; total = t; }
    protected void compute() {
      if (hi - lo <= 64) { long s = 0; for (int i = lo; i < hi; i++) s += data[i]; total.addAndGet(s); return; }
      int mid = (lo + hi) >>> 1;
      invokeAll(new SumAction(data, lo, mid, total), new SumAction(data, mid, hi, total));
    }
  }

  static class Boom extends RecursiveTask<Integer> {
    protected Integer compute() { throw new IllegalStateException("boom"); }
  }

  static class CustomEx extends RuntimeException {
    public CustomEx(String m) { super(m); }
  }

  static class CountedSum extends CountedCompleter<Void> {
    final int[] a; final int lo, hi; final AtomicInteger sum;
    CountedSum(CountedCompleter<?> p, int[] a, int lo, int hi, AtomicInteger sum) { super(p); this.a = a; this.lo = lo; this.hi = hi; this.sum = sum; }
    public void compute() {
      int l = lo, h = hi;
      while (h - l > 8) {
        int mid = (l + h) >>> 1;
        addToPendingCount(1);
        new CountedSum(this, a, mid, h, sum).fork();
        h = mid;
      }
      int s = 0;
      for (int i = l; i < h; i++) s += a[i];
      sum.addAndGet(s);
      tryComplete();
    }
  }

  static String norm(String s) { return s.replaceAll("@[0-9a-f]+", "@X").replaceAll("steals = \\d+", "steals = N").replaceAll("tasks = \\d+", "tasks = N"); }

  public static void main(String[] args) throws Exception {
    ForkJoinPool pool = new ForkJoinPool(4);
    probe("fib", () -> pool.invoke(new Fib(20)));
    probe("fib-submit", () -> { try { return pool.submit(new Fib(15)).get(); } catch (Exception e) { return e; } });
    probe("fib-one-thread", () -> { ForkJoinPool p = new ForkJoinPool(1); try { return p.invoke(new Fib(18)); } finally { p.shutdown(); } });
    probe("fib-external", () -> new Fib(12).invoke());
    probe("fib-join-external", () -> { Fib f = new Fib(12); f.fork(); return f.join(); });
    probe("sum-action", () -> { long[] d = new long[10000]; for (int i = 0; i < d.length; i++) d[i] = i; AtomicLong t = new AtomicLong(); pool.invoke(new SumAction(d, 0, d.length, t)); return t.get(); });
    probe("invoke-all-collection", () -> { List<Fib> l = new ArrayList<>(); for (int i = 1; i <= 8; i++) l.add(new Fib(i)); return pool.invoke(new RecursiveTask<Integer>() { protected Integer compute() { invokeAll(l); int s = 0; for (Fib f : l) s += f.join(); return s; } }); });
    probe("invoke-all-array", () -> pool.invoke(new RecursiveTask<Integer>() { protected Integer compute() { Fib a = new Fib(5), b = new Fib(6), c = new Fib(7); invokeAll(a, b, c); return a.join() + b.join() + c.join(); } }));
    probe("invoke-all-exception", () -> pool.invoke(new RecursiveTask<String>() { protected String compute() { Fib ok = new Fib(5); Boom bad = new Boom(); try { invokeAll(ok, bad); return "no"; } catch (IllegalStateException e) { return "caught " + e.getMessage().endsWith("boom") + " " + bad.isCompletedAbnormally(); } } }));
    probe("exception-join", () -> { Boom b = new Boom(); try { pool.invoke(b); return "no"; } catch (IllegalStateException e) { return e.getMessage() + " cause=" + (e.getCause() == null ? "null" : e.getCause().getClass().getSimpleName()) + " " + b.isCompletedAbnormally() + " " + b.isCancelled() + " " + b.state() + " " + b.getException().getClass().getSimpleName(); } });
    probe("exception-get", () -> { ForkJoinTask<Integer> b = pool.submit(new Boom()); try { b.get(); return "no"; } catch (ExecutionException e) { return e.getCause().getClass().getSimpleName() + ":" + e.getCause().getMessage() + " " + b.state(); } catch (InterruptedException e) { return e; } });
    probe("exception-same-thread", () -> { Boom b = new Boom(); try { b.invoke(); return "no"; } catch (IllegalStateException e) { return "direct " + (e.getCause() == null) + " " + e.getMessage(); } });
    probe("exception-custom", () -> { ForkJoinTask<Integer> t = pool.submit(new RecursiveTask<Integer>() { protected Integer compute() { throw new CustomEx("custom"); } }); t.quietlyJoin(); try { t.join(); return "no"; } catch (CustomEx e) { return e.getMessage() + " " + (e.getCause() != null) + " " + t.getException().getClass().getSimpleName(); } });
    probe("exception-checked", () -> { ForkJoinTask<Integer> t = pool.submit((Callable<Integer>) () -> { throw new java.io.IOException("io"); }); t.quietlyJoin(); return t.getException().getClass().getSimpleName() + " " + t.getException().getCause().getMessage(); });
    probe("complete-exceptionally", () -> { ForkJoinTask<Integer> t = new Fib(3); t.completeExceptionally(new java.io.IOException("x")); try { t.join(); return "no"; } catch (RuntimeException e) { return e.getClass().getSimpleName() + " " + e.getCause().getMessage() + " " + t.isDone() + " " + t.isCompletedNormally(); } });
    probe("complete-value", () -> { Fib t = new Fib(3); t.complete(99); return t.join() + " " + t.isCompletedNormally() + " " + t.state() + " " + t.resultNow(); });
    probe("cancel", () -> { Fib t = new Fib(3); boolean c = t.cancel(true); try { t.join(); return "no"; } catch (CancellationException e) { return c + " " + t.isCancelled() + " " + t.isCompletedAbnormally() + " " + t.isDone() + " " + t.state() + " " + (t.getException() instanceof CancellationException) + " " + t.cancel(false); } });
    probe("cancel-done", () -> { Fib t = new Fib(3); t.invoke(); return t.cancel(true) + " " + t.isCancelled() + " " + t.isCompletedNormally(); });
    probe("result-now", () -> { Fib t = new Fib(4); String a; try { t.resultNow(); a = "no"; } catch (IllegalStateException e) { a = e.getMessage(); } t.invoke(); String b = "" + t.resultNow(); String c; try { t.exceptionNow(); c = "no"; } catch (IllegalStateException e) { c = e.getMessage(); } return a + "|" + b + "|" + c; });
    probe("exception-now", () -> { Boom b = new Boom(); try { b.invoke(); } catch (Exception e) { } String r1 = b.exceptionNow().getClass().getSimpleName(); String r2; try { b.resultNow(); r2 = "no"; } catch (IllegalStateException e) { r2 = e.getMessage(); } Fib c = new Fib(1); c.cancel(false); String r3; try { c.exceptionNow(); r3 = "no"; } catch (IllegalStateException e) { r3 = e.getMessage(); } return r1 + "|" + r2 + "|" + r3; });
    probe("state-running", () -> new Fib(3).state() + " " + new Fib(3).isDone() + " " + new Fib(3).getException());
    probe("get-timeout", () -> { CountDownLatch l = new CountDownLatch(1); ForkJoinTask<Integer> t = pool.submit((Callable<Integer>) () -> { l.await(); return 1; }); try { t.get(30, TimeUnit.MILLISECONDS); return "no"; } catch (TimeoutException e) { l.countDown(); return "timeout "; } catch (Exception e) { return e; } });
    probe("quietly", () -> { Boom b = new Boom(); b.fork(); b.quietlyJoin(); Boom c = new Boom(); c.quietlyInvoke(); Fib f = new Fib(4); f.quietlyComplete(); return b.isCompletedAbnormally() + " " + c.isCompletedAbnormally() + " " + f.isCompletedNormally() + " " + f.getRawResult(); });
    probe("quietly-join-timeout", () -> { try { CountDownLatch l = new CountDownLatch(1); ForkJoinTask<Integer> t = pool.submit((Callable<Integer>) () -> { l.await(); return 1; }); boolean a = t.quietlyJoin(20, TimeUnit.MILLISECONDS); l.countDown(); boolean b = t.quietlyJoin(2, TimeUnit.SECONDS); return a + " " + b + " " + t.quietlyJoinUninterruptibly(1, TimeUnit.SECONDS); } catch (InterruptedException e) { return e; } });
    probe("tags", () -> { Fib t = new Fib(1); short a = t.getForkJoinTaskTag(); short b = t.setForkJoinTaskTag((short) 7); boolean c = t.compareAndSetForkJoinTaskTag((short) 7, (short) 9); boolean d = t.compareAndSetForkJoinTaskTag((short) 7, (short) 1); t.invoke(); return a + " " + b + " " + c + " " + d + " " + t.getForkJoinTaskTag() + " " + t.isDone(); });
    probe("reinitialize", () -> { Fib t = new Fib(5); int a = t.invoke(); t.reinitialize(); boolean done = t.isDone(); int b = t.invoke(); return a + " " + done + " " + b; });
    probe("adapt", () -> { AtomicInteger c = new AtomicInteger(); ForkJoinTask<?> a = ForkJoinTask.adapt(() -> c.incrementAndGet()); ForkJoinTask<String> b = ForkJoinTask.adapt(() -> c.incrementAndGet(), "res"); ForkJoinTask<Integer> d = ForkJoinTask.adapt(() -> 42); return pool.invoke(a) + " " + pool.invoke(b) + " " + pool.invoke(d) + " " + c.get(); });
    probe("adapt-null", () -> ForkJoinTask.adapt((Runnable) null));
    probe("adapt-interruptible", () -> { try { CountDownLatch started = new CountDownLatch(1); AtomicBoolean interrupted = new AtomicBoolean(); ForkJoinTask<Integer> t = ForkJoinTask.adaptInterruptible((Callable<Integer>) () -> { started.countDown(); try { Thread.sleep(10000); } catch (InterruptedException e) { interrupted.set(true); } return 1; }); pool.execute(t); started.await(); boolean c = t.cancel(true); Thread.sleep(50); return c + " " + t.isCancelled() + " " + interrupted.get(); } catch (InterruptedException e) { return e; } });
    probe("adapt-interruptible-runnable", () -> { ForkJoinTask<?> t = ForkJoinTask.adaptInterruptible(() -> { }); ForkJoinTask<String> u = ForkJoinTask.adaptInterruptible(() -> { }, "r"); return pool.invoke(t) + " " + pool.invoke(u); });
    probe("in-pool", () -> ForkJoinTask.inForkJoinPool() + " " + ForkJoinTask.getPool() + " " + pool.invoke(new RecursiveTask<String>() { protected String compute() { return ForkJoinTask.inForkJoinPool() + " " + (ForkJoinTask.getPool() == pool) + " " + (Thread.currentThread() instanceof ForkJoinWorkerThread) + " " + ((ForkJoinWorkerThread) Thread.currentThread()).getPool().equals(pool); } }));
    probe("worker-names", () -> pool.invoke(new RecursiveTask<String>() { protected String compute() { String n = Thread.currentThread().getName(); return n.replaceAll("ForkJoinPool-\\d+-worker-\\d+", "ForkJoinPool-P-worker-N") + " " + Thread.currentThread().isDaemon(); } }));
    probe("pool-index", () -> pool.invoke(new RecursiveTask<Boolean>() { protected Boolean compute() { int i = ((ForkJoinWorkerThread) Thread.currentThread()).getPoolIndex(); return i >= 0 && i < 64; } }));
    probe("queued-counts", () -> new ForkJoinPool(1).invoke(new RecursiveTask<String>() { protected String compute() { Fib a = new Fib(2); a.fork(); Fib b = new Fib(2); b.fork(); int q = ForkJoinTask.getQueuedTaskCount(); boolean u = b.tryUnfork(); int q2 = ForkJoinTask.getQueuedTaskCount(); a.join(); return q + " " + u + " " + q2 + " " + (ForkJoinTask.getSurplusQueuedTaskCount() >= -8); } }));
    probe("pool-props", () -> { ForkJoinPool p = new ForkJoinPool(3); try { return p.getParallelism() + " " + p.getAsyncMode() + " " + (p.getFactory() == ForkJoinPool.defaultForkJoinWorkerThreadFactory) + " " + p.getUncaughtExceptionHandler() + " " + p.getPoolSize() + " " + p.isQuiescent() + " " + p.getQueuedTaskCount() + " " + p.getQueuedSubmissionCount() + " " + p.hasQueuedSubmissions() + " " + p.getActiveThreadCount() + " " + p.getRunningThreadCount() + " " + p.getDelayedTaskCount() + " " + p.isShutdown() + " " + p.isTerminated() + " " + p.isTerminating(); } finally { p.shutdown(); } });
    probe("pool-async", () -> { ForkJoinPool p = new ForkJoinPool(2, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true); try { return p.getAsyncMode() + " " + p.invoke(new Fib(10)); } finally { p.shutdown(); } });
    probe("pool-bad-args", () -> { String r = ""; try { new ForkJoinPool(0); } catch (IllegalArgumentException e) { r += "A"; } try { new ForkJoinPool(-1); } catch (IllegalArgumentException e) { r += "B"; } try { new ForkJoinPool(1 << 20); } catch (IllegalArgumentException e) { r += "C"; } try { new ForkJoinPool(1, null, null, false); } catch (NullPointerException e) { r += "D"; } return r; });
    probe("pool-to-string", () -> { ForkJoinPool p = new ForkJoinPool(2); String a = norm(p.toString()); p.invoke(new Fib(5)); p.shutdown(); try { p.awaitTermination(5, TimeUnit.SECONDS); } catch (InterruptedException e) { } return a + " | " + norm(p.toString()).replaceAll("size = \\d+", "size = N"); });
    probe("shutdown-lifecycle", () -> { try { ForkJoinPool p = new ForkJoinPool(2); p.invoke(new Fib(8)); p.shutdown(); boolean term = p.awaitTermination(5, TimeUnit.SECONDS); String r = p.isShutdown() + " " + term + " " + p.isTerminated() + " " + p.isTerminating(); try { p.execute(() -> { }); r += " accepted"; } catch (RejectedExecutionException e) { r += " rejected"; } try { p.submit(new Fib(1)); r += " accepted"; } catch (RejectedExecutionException e) { r += " rejected"; } return r; } catch (InterruptedException e) { return e; } });
    probe("shutdown-now", () -> { try { ForkJoinPool p = new ForkJoinPool(2); CountDownLatch started = new CountDownLatch(1); ForkJoinTask<?> t = p.submit(() -> { started.countDown(); try { Thread.sleep(10000); } catch (InterruptedException e) { } }); started.await(); List<Runnable> rest = p.shutdownNow(); boolean term = p.awaitTermination(5, TimeUnit.SECONDS); return rest.size() + " " + term + " " + p.isShutdown(); } catch (InterruptedException e) { return e; } });
    probe("shutdown-idle", () -> { try { ForkJoinPool p = new ForkJoinPool(2); p.shutdown(); return p.awaitTermination(1, TimeUnit.SECONDS) + " " + p.isTerminated(); } catch (InterruptedException e) { return e; } });
    probe("close", () -> { ForkJoinPool p = new ForkJoinPool(2); AtomicInteger c = new AtomicInteger(); for (int i = 0; i < 20; i++) p.execute(() -> { try { Thread.sleep(2); } catch (InterruptedException e) { } c.incrementAndGet(); }); p.close(); return c.get() + " " + p.isTerminated(); });
    probe("common-pool", () -> { ForkJoinPool c = ForkJoinPool.commonPool(); c.shutdown(); List<Runnable> r = c.shutdownNow(); return (c == ForkJoinPool.commonPool()) + " " + (ForkJoinPool.getCommonPoolParallelism() >= 1) + " " + c.isShutdown() + " " + c.isTerminated() + " " + r.size() + " " + (c.getParallelism() == ForkJoinPool.getCommonPoolParallelism()) + " " + c.invoke(new Fib(10)) + " " + c.toString().replaceAll("@[0-9a-f]+", "@X").replaceAll("[0-9]+", "N"); });
    probe("common-pool-names", () -> ForkJoinPool.commonPool().invoke(new RecursiveTask<String>() { protected String compute() { return Thread.currentThread().getName().replaceAll("[0-9]+$", "N"); } }));
    probe("common-pool-set-parallelism", () -> { ForkJoinPool c = ForkJoinPool.commonPool(); int prev = c.setParallelism(2); int now = c.getParallelism(); c.setParallelism(prev); return (prev >= 1) + " " + now; });
    probe("set-parallelism", () -> { ForkJoinPool p = new ForkJoinPool(2); try { int prev = p.setParallelism(5); int now = p.getParallelism(); String bad = ""; try { p.setParallelism(0); } catch (IllegalArgumentException e) { bad = "IAE"; } return prev + " " + now + " " + bad + " " + p.invoke(new Fib(8)); } finally { p.shutdown(); } });
    probe("execute-runnable", () -> { try { ForkJoinPool p = new ForkJoinPool(2); CountDownLatch l = new CountDownLatch(3); for (int i = 0; i < 3; i++) p.execute(l::countDown); boolean ok = l.await(5, TimeUnit.SECONDS); p.shutdown(); return ok; } catch (InterruptedException e) { return e; } });
    probe("execute-null", () -> { pool.execute((Runnable) null); return "no"; });
    probe("submit-variants", () -> { try { Future<String> a = pool.submit(() -> "callable"); Future<String> b = pool.submit(() -> { }, "runnable"); Future<?> c = pool.submit(() -> { }); ForkJoinTask<Integer> d = pool.submit(new Fib(6)); return a.get() + " " + b.get() + " " + c.get() + " " + d.get(); } catch (Exception e) { return e; } });
    probe("submit-null", () -> pool.submit((Callable<Integer>) null));
    probe("invoke-all-callables", () -> { try { List<Callable<Integer>> cs = new ArrayList<>(); for (int i = 0; i < 5; i++) { final int n = i; cs.add(() -> n * n); } List<Future<Integer>> fs = pool.invokeAll(cs); int s = 0; for (Future<Integer> f : fs) s += f.get(); return s + " " + fs.size() + " " + fs.get(0).isDone(); } catch (Exception e) { return e; } });
    probe("invoke-all-uninterruptibly", () -> { try { List<Callable<Integer>> cs = new ArrayList<>(); for (int i = 0; i < 4; i++) { final int n = i; cs.add(() -> n + 1); } int s = 0; for (Future<Integer> f : pool.invokeAllUninterruptibly(cs)) s += f.get(); return s; } catch (Exception e) { return e; } });
    probe("invoke-all-timeout", () -> { try { List<Callable<Integer>> cs = new ArrayList<>(); cs.add(() -> 1); cs.add(() -> { Thread.sleep(5000); return 2; }); List<Future<Integer>> fs = pool.invokeAll(cs, 100, TimeUnit.MILLISECONDS); return fs.get(0).isDone() + " " + fs.get(0).isCancelled() + " " + fs.get(1).isCancelled() + " " + fs.get(1).isDone(); } catch (Exception e) { return e; } });
    probe("invoke-any", () -> { try { List<Callable<String>> cs = new ArrayList<>(); cs.add(() -> { throw new IllegalStateException("x"); }); cs.add(() -> "ok"); return pool.invokeAny(cs); } catch (Exception e) { return e; } });
    probe("counted-completer", () -> { int[] a = new int[1000]; for (int i = 0; i < a.length; i++) a[i] = i; AtomicInteger sum = new AtomicInteger(); CountedSum root = new CountedSum(null, a, 0, a.length, sum); pool.invoke(root); return sum.get() + " " + root.isDone() + " " + root.getPendingCount() + " " + (root.getRoot() == root) + " " + root.getCompleter(); });
    probe("counted-api", () -> { CountedCompleter<Integer> root = new CountedCompleter<Integer>(null, 2) { int r; public void compute() { } public Integer getRawResult() { return r; } protected void setRawResult(Integer v) { r = v; } public void onCompletion(CountedCompleter<?> c) { r += 100; } }; CountedCompleter<Void> child = new CountedCompleter<Void>(root, 1) { public void compute() { } }; String s = root.getPendingCount() + " " + child.getPendingCount() + " " + (child.getRoot() == root) + " " + (child.getCompleter() == root) + " "; root.addToPendingCount(1); s += root.getPendingCount() + " " + root.compareAndSetPendingCount(3, 5) + " " + root.compareAndSetPendingCount(3, 7) + " " + root.decrementPendingCountUnlessZero() + " " + root.getPendingCount() + " "; root.setPendingCount(0); child.setPendingCount(0); child.tryComplete(); return s + child.isDone() + " " + root.isDone() + " " + root.getRawResult(); });
    probe("counted-complete", () -> { CountedCompleter<Integer> root = new CountedCompleter<Integer>() { int r; public void compute() { } public Integer getRawResult() { return r; } protected void setRawResult(Integer v) { r = v; } }; root.complete(5); return root.isDone() + " " + root.getRawResult() + " " + root.isCompletedNormally(); });
    probe("counted-first-next", () -> { CountedCompleter<Void> root = new CountedCompleter<Void>(null, 1) { public void compute() { } }; CountedCompleter<Void> child = new CountedCompleter<Void>(root, 0) { public void compute() { } }; CountedCompleter<?> f = child.firstComplete(); CountedCompleter<?> n = child.nextComplete(); CountedCompleter<?> n2 = root.nextComplete(); return (f == child) + " " + (n == null) + " " + (n2 == null) + " " + root.isDone() + " " + root.getPendingCount(); });
    probe("counted-propagate", () -> { CountedCompleter<Void> root = new CountedCompleter<Void>(null, 1) { public void compute() { } }; CountedCompleter<Void> child = new CountedCompleter<Void>(root, 0) { public void compute() { } }; child.propagateCompletion(); boolean a = root.isDone(); root.setPendingCount(0); child.propagateCompletion(); return a + " " + root.isDone(); });
    probe("counted-complete-root", () -> { CountedCompleter<Void> root = new CountedCompleter<Void>(null, 5) { public void compute() { } }; CountedCompleter<Void> mid = new CountedCompleter<Void>(root, 5) { public void compute() { } }; CountedCompleter<Void> leaf = new CountedCompleter<Void>(mid, 5) { public void compute() { } }; leaf.quietlyCompleteRoot(); return root.isDone() + " " + mid.isDone() + " " + leaf.isDone(); });
    probe("counted-exception", () -> { AtomicReference<String> seen = new AtomicReference<>(""); CountedCompleter<Void> root = new CountedCompleter<Void>(null, 1) { public void compute() { } public boolean onExceptionalCompletion(Throwable ex, CountedCompleter<?> caller) { seen.updateAndGet(s -> s + "root:" + ex.getMessage()); return true; } }; CountedCompleter<Void> child = new CountedCompleter<Void>(root, 0) { public void compute() { throw new IllegalStateException("kid"); } }; try { pool.invoke(child); } catch (IllegalStateException e) { } return seen.get() + " " + root.isCompletedAbnormally() + " " + child.isCompletedAbnormally(); });
    probe("counted-help", () -> { int[] a = new int[200]; Arrays.fill(a, 1); AtomicInteger sum = new AtomicInteger(); CountedSum root = new CountedSum(null, a, 0, a.length, sum); pool.execute(root); root.helpComplete(10); root.quietlyJoin(); return sum.get(); });
    probe("managed-blocker", () -> { try { boolean[] released = {false}; ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker() { public boolean block() { released[0] = true; return true; } public boolean isReleasable() { return released[0]; } }); int[] n = {0}; ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker() { public boolean block() { return ++n[0] >= 3; } public boolean isReleasable() { return false; } }); return released[0] + " " + n[0]; } catch (InterruptedException e) { return e; } });
    probe("await-quiescence", () -> { ForkJoinPool p = new ForkJoinPool(2); AtomicInteger c = new AtomicInteger(); for (int i = 0; i < 10; i++) p.execute(() -> { try { Thread.sleep(5); } catch (InterruptedException e) { } c.incrementAndGet(); }); boolean q = p.awaitQuiescence(5, TimeUnit.SECONDS); boolean idle = p.isQuiescent(); p.shutdown(); return q + " " + c.get() + " " + idle; });
    probe("help-quiesce", () -> pool.invoke(new RecursiveTask<Integer>() { protected Integer compute() { AtomicInteger c = new AtomicInteger(); for (int i = 0; i < 5; i++) new RecursiveAction() { protected void compute() { c.incrementAndGet(); } }.fork(); ForkJoinTask.helpQuiesce(); return c.get(); } }));
    probe("schedule", () -> { try { ForkJoinPool p = new ForkJoinPool(2); long s = System.nanoTime(); ScheduledFuture<String> f = p.schedule(() -> "done", 40, TimeUnit.MILLISECONDS); boolean early = f.isDone(); String v = f.get(); long ms = (System.nanoTime() - s) / 1_000_000; ScheduledFuture<?> g = p.schedule(() -> { }, 10, TimeUnit.MILLISECONDS); g.get(); p.shutdown(); return early + " " + v + " " + (ms >= 35) + " " + f.isDone() + " " + (f.getDelay(TimeUnit.MILLISECONDS) <= 0) + " " + g.isDone(); } catch (Exception e) { return e; } });
    probe("schedule-cancel", () -> { ForkJoinPool p = new ForkJoinPool(2); ScheduledFuture<?> f = p.schedule(() -> { }, 10, TimeUnit.SECONDS); boolean c = f.cancel(false); long after = p.getDelayedTaskCount(); p.shutdown(); return c + " " + after + " " + f.isCancelled(); });
    probe("schedule-fixed-rate", () -> { try { ForkJoinPool p = new ForkJoinPool(2); AtomicInteger c = new AtomicInteger(); ScheduledFuture<?> f = p.scheduleAtFixedRate(c::incrementAndGet, 0, 10, TimeUnit.MILLISECONDS); Thread.sleep(150); f.cancel(false); int n = c.get(); Thread.sleep(50); int m = c.get(); p.shutdown(); return (n >= 5) + " " + (m - n <= 1) + " " + f.isCancelled(); } catch (InterruptedException e) { return e; } });
    probe("schedule-fixed-delay", () -> { try { ForkJoinPool p = new ForkJoinPool(2); AtomicInteger c = new AtomicInteger(); ScheduledFuture<?> f = p.scheduleWithFixedDelay(c::incrementAndGet, 0, 10, TimeUnit.MILLISECONDS); Thread.sleep(120); f.cancel(false); p.shutdown(); return c.get() >= 4; } catch (InterruptedException e) { return e; } });
    probe("schedule-bad", () -> { ForkJoinPool p = new ForkJoinPool(1); String r = ""; try { p.scheduleAtFixedRate(() -> { }, 0, 0, TimeUnit.SECONDS); } catch (IllegalArgumentException e) { r += "A"; } try { p.scheduleWithFixedDelay(() -> { }, 0, -1, TimeUnit.SECONDS); } catch (IllegalArgumentException e) { r += "B"; } try { p.schedule((Runnable) null, 1, TimeUnit.SECONDS); } catch (NullPointerException e) { r += "C"; } p.shutdown(); return r; });
    probe("schedule-after-shutdown", () -> { ForkJoinPool p = new ForkJoinPool(1); p.shutdown(); try { p.schedule(() -> { }, 1, TimeUnit.MILLISECONDS); return "accepted"; } catch (RejectedExecutionException e) { return "rejected"; } });
    probe("shutdown-delayed-policy", () -> { try { ForkJoinPool p = new ForkJoinPool(2); AtomicInteger one = new AtomicInteger(), periodic = new AtomicInteger(); p.schedule(one::incrementAndGet, 40, TimeUnit.MILLISECONDS); ScheduledFuture<?> per = p.scheduleAtFixedRate(periodic::incrementAndGet, 1000, 1000, TimeUnit.MILLISECONDS); p.shutdown(); Thread.sleep(120); return one.get() + " " + periodic.get() + " " + per.isCancelled(); } catch (InterruptedException e) { return e; } });
    probe("shutdown-cancel-delayed", () -> { try { ForkJoinPool p = new ForkJoinPool(2); p.cancelDelayedTasksOnShutdown(); AtomicInteger one = new AtomicInteger(); ScheduledFuture<?> f = p.schedule(one::incrementAndGet, 40, TimeUnit.MILLISECONDS); p.shutdown(); Thread.sleep(100); return one.get() + " " + f.isCancelled(); } catch (InterruptedException e) { return e; } });
    probe("submit-with-timeout", () -> { try { ForkJoinPool p = new ForkJoinPool(2); CountDownLatch started = new CountDownLatch(1); AtomicBoolean interrupted = new AtomicBoolean(); ForkJoinTask<Integer> t = p.submitWithTimeout(() -> { started.countDown(); try { Thread.sleep(10000); } catch (InterruptedException e) { interrupted.set(true); } return 1; }, 50, TimeUnit.MILLISECONDS, null); started.await(); t.quietlyJoin(); Thread.sleep(20); p.shutdown(); return t.isCancelled() + " " + interrupted.get(); } catch (InterruptedException e) { return e; } });
    probe("submit-with-timeout-fast", () -> { try { ForkJoinPool p = new ForkJoinPool(2); ForkJoinTask<Integer> t = p.submitWithTimeout(() -> 7, 5000, TimeUnit.MILLISECONDS, null); int v = t.get(); p.shutdown(); return v + " " + t.isCancelled(); } catch (Exception e) { return e; } });
    probe("submit-with-timeout-action", () -> { try { ForkJoinPool p = new ForkJoinPool(2); CountDownLatch release = new CountDownLatch(1); AtomicInteger acted = new AtomicInteger(); ForkJoinTask<Integer> t = p.submitWithTimeout(() -> { release.await(); return 3; }, 30, TimeUnit.MILLISECONDS, x -> { acted.incrementAndGet(); }); Thread.sleep(100); release.countDown(); int v = t.get(); p.shutdown(); return acted.get() + " " + v; } catch (Exception e) { return e; } });
    probe("uncaught-handler", () -> { try { CountDownLatch l = new CountDownLatch(1); AtomicReference<String> seen = new AtomicReference<>(); ForkJoinPool p = new ForkJoinPool(1, ForkJoinPool.defaultForkJoinWorkerThreadFactory, (t, e) -> { seen.set(e.getMessage()); l.countDown(); }, false); p.execute(() -> { throw new IllegalStateException("from-execute"); }); l.await(5, TimeUnit.SECONDS); p.shutdown(); return seen.get(); } catch (InterruptedException e) { return e; } });
    probe("custom-factory", () -> { AtomicInteger made = new AtomicInteger(); ForkJoinPool.ForkJoinWorkerThreadFactory f = p -> { made.incrementAndGet(); ForkJoinWorkerThread t = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(p); t.setName("custom-" + made.get()); return t; }; ForkJoinPool p = new ForkJoinPool(2, f, null, false); try { String n = p.invoke(new RecursiveTask<String>() { protected String compute() { return Thread.currentThread().getName().substring(0, 7); } }); return n + " " + (made.get() >= 1) + " " + (p.getFactory() == f); } finally { p.shutdown(); } });
    probe("worker-hooks", () -> { try { AtomicInteger started = new AtomicInteger(), ended = new AtomicInteger(); ForkJoinPool.ForkJoinWorkerThreadFactory f = p -> new ForkJoinWorkerThread(p) { protected void onStart() { started.incrementAndGet(); } protected void onTermination(Throwable e) { ended.incrementAndGet(); } }; ForkJoinPool p = new ForkJoinPool(2, f, null, false); p.invoke(new Fib(8)); p.shutdown(); p.awaitTermination(5, TimeUnit.SECONDS); return (started.get() >= 1) + " " + (started.get() == ended.get()); } catch (InterruptedException e) { return e; } });
    probe("stress", () -> { ForkJoinPool p = new ForkJoinPool(3); try { long total = 0; for (int i = 0; i < 20; i++) total += p.invoke(new Fib(15)); return total; } finally { p.shutdown(); } });
    probe("nested-pools", () -> { ForkJoinPool inner = new ForkJoinPool(2); try { return pool.invoke(new RecursiveTask<Integer>() { protected Integer compute() { return inner.invoke(new Fib(10)) + new Fib(5).invoke(); } }); } finally { inner.shutdown(); } });
    probe("blocked-join-other", () -> { try { ForkJoinPool p = new ForkJoinPool(2); CountDownLatch gate = new CountDownLatch(1); ForkJoinTask<Integer> slow = p.submit((Callable<Integer>) () -> { gate.await(); return 5; }); ForkJoinTask<Integer> waiter = p.submit((Callable<Integer>) () -> slow.join() + 1); Thread.sleep(30); gate.countDown(); int r = waiter.get(); p.shutdown(); return r; } catch (Exception e) { return e; } });
    pool.shutdown();
    System.out.println("end");
  }
}
