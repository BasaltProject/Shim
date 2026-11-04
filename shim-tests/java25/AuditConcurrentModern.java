/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 21

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class AuditConcurrentModern {
    static void p(String s) { System.out.println(s); }

    public static void main(String[] args) throws Exception {
        Phaser ph = new Phaser(1) {
            protected boolean onAdvance(int phase, int parties) { p("advance " + phase + " " + parties); return phase >= 2; }
        };
        int N = 3;
        ph.bulkRegister(N);
        Thread[] ts = new Thread[N];
        AtomicInteger sum = new AtomicInteger();
        for (int i = 0; i < N; i++) {
            final int id = i;
            ts[i] = new Thread(() -> {
                for (int r = 0; r < 3; r++) { sum.addAndGet(id + 1); ph.arriveAndAwaitAdvance(); }
                ph.arriveAndDeregister();
            });
            ts[i].start();
        }
        for (int r = 0; r < 3; r++) ph.arriveAndAwaitAdvance();
        for (Thread t : ts) t.join();
        p("phaser " + ph.isTerminated() + " " + sum.get() + " " + (ph.getPhase() < 0));

        Exchanger<String> ex = new Exchanger<>();
        AtomicReference<String> got = new AtomicReference<>();
        Thread et = new Thread(() -> { try { String g = ex.exchange("fromT"); got.set(g); } catch (InterruptedException e) {} });
        et.start();
        p("main got " + ex.exchange("fromMain"));
        et.join();
        p("t got " + got.get());
        try { ex.exchange("x", 50, TimeUnit.MILLISECONDS); } catch (TimeoutException e) { p("timeout"); }

        CompletableFuture<Integer> cf = CompletableFuture.supplyAsync(() -> 6)
            .thenApply(x -> x * 7)
            .thenCombine(CompletableFuture.completedFuture(8), Integer::sum)
            .thenCompose(x -> CompletableFuture.supplyAsync(() -> x + 1))
            .whenComplete((v, e) -> p("when " + v + " " + e));
        p("cf " + cf.get());
        CompletableFuture<Object> bad = CompletableFuture.supplyAsync(() -> { throw new IllegalStateException("boom"); });
        try { bad.get(); } catch (ExecutionException e) { p("ee " + e.getCause()); }
        p("handle " + bad.handle((v, e) -> e.getClass().getSimpleName()).get());
        p("exc " + bad.exceptionally(e -> e.getMessage()).get());
        CompletableFuture<Void> all = CompletableFuture.allOf(CompletableFuture.runAsync(() -> {}), CompletableFuture.runAsync(() -> {}));
        all.get(); p("all " + all.isDone());
        p("any " + CompletableFuture.anyOf(CompletableFuture.completedFuture("a")).get());
        CompletableFuture<String> to = new CompletableFuture<String>().completeOnTimeout("dflt", 30, TimeUnit.MILLISECONDS);
        p("timeout " + to.get());
        CompletableFuture<String> ot = new CompletableFuture<String>().orTimeout(30, TimeUnit.MILLISECONDS);
        try { ot.get(); } catch (ExecutionException e) { p("orTimeout " + e.getCause().getClass().getName()); }

        ForkJoinPool fj = new ForkJoinPool(3);
        class Fib extends RecursiveTask<Long> {
            final int n; Fib(int n) { this.n = n; }
            protected Long compute() {
                if (n < 2) return (long) n;
                Fib a = new Fib(n - 1); a.fork();
                return new Fib(n - 2).compute() + a.join();
            }
        }
        p("fib " + fj.invoke(new Fib(20)));
        p("par " + fj.submit(() -> java.util.stream.IntStream.range(0, 1000).parallel().sum()).get());
        fj.shutdown();
        p("fjterm " + fj.awaitTermination(5, TimeUnit.SECONDS));

        try (ExecutorService vt = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> fs = new ArrayList<>();
            for (int i = 0; i < 20; i++) { final int k = i; fs.add(vt.submit(() -> "v" + k + Thread.currentThread().isVirtual())); }
            StringBuilder sb = new StringBuilder();
            for (Future<String> f : fs) sb.append(f.get()).append(',');
            p(sb.toString());
        }
        Thread v = Thread.ofVirtual().name("virt-", 5).unstarted(() -> {
            try { Thread.sleep(10); } catch (InterruptedException e) {}
            p("in " + Thread.currentThread().getName() + " " + Thread.currentThread().isDaemon());
        });
        v.start(); v.join();
        p("state " + v.getState() + " " + v.isVirtual());
        Thread pl = Thread.ofPlatform().name("plat").daemon(true).start(() -> {});
        pl.join(); p("plat " + pl.getName() + pl.isDaemon());

        ScheduledThreadPoolExecutor st = new ScheduledThreadPoolExecutor(2);
        AtomicInteger ticks = new AtomicInteger();
        CountDownLatch cd = new CountDownLatch(3);
        ScheduledFuture<?> sf = st.scheduleAtFixedRate(() -> { ticks.incrementAndGet(); cd.countDown(); }, 0, 20, TimeUnit.MILLISECONDS);
        cd.await(); sf.cancel(false);
        p("ticks>=3 " + (ticks.get() >= 3) + " cancelled " + sf.isCancelled());
        ScheduledFuture<String> one = st.schedule(() -> "once", 20, TimeUnit.MILLISECONDS);
        p("one " + one.get() + " " + (one.getDelay(TimeUnit.MILLISECONDS) <= 0));
        ScheduledFuture<?> fd = st.scheduleWithFixedDelay(() -> { throw new RuntimeException("x"); }, 0, 10, TimeUnit.MILLISECONDS);
        try { fd.get(); } catch (ExecutionException e) { p("fd " + e.getCause().getMessage()); }
        st.shutdown(); p("st " + st.awaitTermination(2, TimeUnit.SECONDS) + " " + st.getCompletedTaskCount());

        ThreadPoolExecutor tp = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.CallerRunsPolicy());
        CountDownLatch gate = new CountDownLatch(1);
        tp.execute(() -> { try { gate.await(); } catch (InterruptedException e) {} });
        tp.execute(() -> {});
        AtomicReference<String> who = new AtomicReference<>();
        tp.execute(() -> who.set(Thread.currentThread().getName()));
        p("callerRuns " + who.get() + " q=" + tp.getQueue().size() + "");
        gate.countDown(); tp.shutdown(); tp.awaitTermination(2, TimeUnit.SECONDS);
        try { tp.execute(() -> {}); } catch (RejectedExecutionException e) { p("rejected"); }

        Semaphore sem = new Semaphore(2, true);
        p("sem " + sem.tryAcquire(2) + sem.tryAcquire() + sem.availablePermits());
        CyclicBarrier cb = new CyclicBarrier(2, () -> p("barrier action"));
        Thread bt = new Thread(() -> { try { cb.await(); } catch (Exception e) {} });
        bt.start(); cb.await(); bt.join(); p("bar " + cb.getNumberWaiting() + cb.isBroken());
        ConcurrentHashMap<String, Integer> chm = new ConcurrentHashMap<>();
        for (int i = 0; i < 100; i++) chm.merge("k" + (i % 5), 1, Integer::sum);
        p(new TreeMap<>(chm).toString() + chm.reduceValues(1, Integer::sum) + chm.mappingCount());
        LongAdder la = new LongAdder(); LongAccumulator lm = new LongAccumulator(Math::max, Long.MIN_VALUE);
        try (var ex2 = Executors.newFixedThreadPool(4)) {
            for (int i = 0; i < 100; i++) { final int k = i; ex2.execute(() -> { la.add(k); lm.accumulate(k); }); }
        }
        p("adder " + la.sum() + " " + lm.get());
        flowTest();
    }

    static void flowTest() throws Exception {
        SubmissionPublisher<Integer> pub = new SubmissionPublisher<>();
        List<Integer> got = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch done = new CountDownLatch(1);
        pub.subscribe(new Flow.Subscriber<Integer>() {
            Flow.Subscription s;
            public void onSubscribe(Flow.Subscription s) { this.s = s; s.request(1); }
            public void onNext(Integer i) { got.add(i * i); s.request(1); }
            public void onError(Throwable t) {}
            public void onComplete() { done.countDown(); }
        });
        for (int i = 1; i <= 5; i++) pub.submit(i);
        pub.close(); done.await();
        p("flow " + got);
    }
}
