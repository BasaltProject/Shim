/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 21

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FuturesModern {
  private static void check(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }

  public static void main(String[] args) throws Exception {
    testCompletedAndFailed();
    testAsyncSupplyAndRun();
    testChaining();
    testCombineAndBoth();
    testEither();
    testCompose();
    testHandlingAndExceptionally();
    testAllOfAndAnyOf();
    testTimeouts();
    testExecutorsAndForkJoin();
    testFlow();
    System.out.println("ALL FUTURES MODERN TESTS PASSED");
  }

  private static void testCompletedAndFailed() {
    CompletableFuture<String> cf = CompletableFuture.completedFuture("hello");
    check(cf.isDone(), "completedFuture isDone");
    check(!cf.isCompletedExceptionally(), "completedFuture isCompletedExceptionally");
    check("hello".equals(cf.join()), "completedFuture value");
    check("hello".equals(cf.getNow("default")), "completedFuture getNow");

    CompletionStage<Integer> cs = CompletableFuture.completedStage(42);
    check(Integer.valueOf(42).equals(cs.toCompletableFuture().join()), "completedStage join");

    CompletableFuture<String> failed = CompletableFuture.failedFuture(new IllegalStateException("boom"));
    check(failed.isDone(), "failed isDone");
    check(failed.isCompletedExceptionally(), "failed isCompletedExceptionally");
    try {
      failed.join();
      check(false, "failed join should throw");
    } catch (CompletionException e) {
      check(e.getCause() instanceof IllegalStateException, "failed cause");
    }
    System.out.println("completed and failed: ok");
  }

  private static void testAsyncSupplyAndRun() throws Exception {
    CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> "supplied");
    check("supplied".equals(f1.get(5, TimeUnit.SECONDS)), "supplyAsync get");

    List<String> list = Collections.synchronizedList(new ArrayList<String>());
    CompletableFuture<Void> f2 = CompletableFuture.runAsync(() -> list.add("ran"));
    f2.join();
    check(list.size() == 1 && "ran".equals(list.get(0)), "runAsync join");

    CompletableFuture<String> f3 = new CompletableFuture<String>().completeAsync(() -> "asyncCompleted");
    check("asyncCompleted".equals(f3.join()), "completeAsync join");
    System.out.println("async supply and run: ok");
  }

  private static void testChaining() {
    CompletableFuture<String> cf = new CompletableFuture<String>();
    CompletableFuture<Integer> mapped = cf.thenApply(String::length);
    List<Integer> accepted = new ArrayList<Integer>();
    CompletableFuture<Void> acc = mapped.thenAccept(accepted::add);
    List<String> ran = new ArrayList<String>();
    CompletableFuture<Void> r = acc.thenRun(() -> ran.add("done"));

    check(!mapped.isDone(), "mapped not done before complete");
    cf.complete("basalt");
    check(Integer.valueOf(6).equals(mapped.join()), "thenApply mapped");
    acc.join();
    check(accepted.size() == 1 && accepted.get(0) == 6, "thenAccept accepted");
    r.join();
    check(ran.size() == 1 && "done".equals(ran.get(0)), "thenRun ran");
    System.out.println("chaining: ok");
  }

  private static void testCombineAndBoth() {
    CompletableFuture<String> f1 = new CompletableFuture<String>();
    CompletableFuture<Integer> f2 = new CompletableFuture<Integer>();

    CompletableFuture<String> combined = f1.thenCombine(f2, (s, n) -> s + ":" + n);
    List<String> bothResult = new ArrayList<String>();
    CompletableFuture<Void> both = f1.thenAcceptBoth(f2, (s, n) -> bothResult.add(s + "=" + n));
    List<String> ranBoth = new ArrayList<String>();
    CompletableFuture<Void> ran = f1.runAfterBoth(f2, () -> ranBoth.add("bothRan"));

    f1.complete("apple");
    check(!combined.isDone(), "combined not done with only f1");
    f2.complete(10);

    check("apple:10".equals(combined.join()), "thenCombine result");
    both.join();
    check(bothResult.size() == 1 && "apple=10".equals(bothResult.get(0)), "thenAcceptBoth result");
    ran.join();
    check(ranBoth.size() == 1 && "bothRan".equals(ranBoth.get(0)), "runAfterBoth result");
    System.out.println("combine and both: ok");
  }

  private static void testEither() {
    CompletableFuture<String> f1 = new CompletableFuture<String>();
    CompletableFuture<String> f2 = new CompletableFuture<String>();

    CompletableFuture<String> either = f1.applyToEither(f2, s -> s.toUpperCase());
    List<String> eitherAccepted = new ArrayList<String>();
    CompletableFuture<Void> acc = f1.acceptEither(f2, eitherAccepted::add);
    List<String> eitherRan = new ArrayList<String>();
    CompletableFuture<Void> ran = f1.runAfterEither(f2, () -> eitherRan.add("eitherRan"));

    f2.complete("cherry");
    check("CHERRY".equals(either.join()), "applyToEither result");
    acc.join();
    check(eitherAccepted.size() == 1 && "cherry".equals(eitherAccepted.get(0)), "acceptEither result");
    ran.join();
    check(eitherRan.size() == 1 && "eitherRan".equals(eitherRan.get(0)), "runAfterEither result");
    System.out.println("either: ok");
  }

  private static void testCompose() {
    CompletableFuture<String> f1 = CompletableFuture.completedFuture("shim");
    CompletableFuture<Integer> composed = f1.thenCompose(s -> CompletableFuture.completedFuture(s.length() * 2));
    check(Integer.valueOf(8).equals(composed.join()), "thenCompose result");
    System.out.println("compose: ok");
  }

  private static void testHandlingAndExceptionally() {
    CompletableFuture<String> f1 = CompletableFuture.failedFuture(new IllegalArgumentException("bad argument"));
    CompletableFuture<String> handled = f1.handle((val, ex) -> (ex != null) ? "recovered:" + ex.getMessage() : val);
    check("recovered:bad argument".equals(handled.join()), "handle recovery");

    CompletableFuture<String> f2 = CompletableFuture.failedFuture(new RuntimeException("oops"));
    CompletableFuture<String> exceptionally = f2.exceptionally(ex -> "fallback:" + ex.getMessage());
    check("fallback:oops".equals(exceptionally.join()), "exceptionally result");

    CompletableFuture<String> f3 = CompletableFuture.failedFuture(new RuntimeException("composeOops"));
    CompletableFuture<String> exComposed = f3.exceptionallyCompose(ex -> CompletableFuture.completedFuture("composed:" + ex.getMessage()));
    check("composed:composeOops".equals(exComposed.join()), "exceptionallyCompose result");

    List<String> whenCompleteList = new ArrayList<String>();
    CompletableFuture<String> f4 = CompletableFuture.completedFuture("success");
    CompletableFuture<String> wc = f4.whenComplete((val, ex) -> whenCompleteList.add(val + ":" + (ex == null)));
    check("success".equals(wc.join()), "whenComplete returns original value");
    check(whenCompleteList.size() == 1 && "success:true".equals(whenCompleteList.get(0)), "whenComplete executed");
    System.out.println("handling and exceptionally: ok");
  }

  private static void testAllOfAndAnyOf() {
    CompletableFuture<String> f1 = new CompletableFuture<String>();
    CompletableFuture<String> f2 = new CompletableFuture<String>();
    CompletableFuture<String> f3 = new CompletableFuture<String>();

    CompletableFuture<Void> all = CompletableFuture.allOf(f1, f2, f3);
    CompletableFuture<Object> any = CompletableFuture.anyOf(f1, f2, f3);

    check(!all.isDone(), "all not done initially");
    check(!any.isDone(), "any not done initially");

    f2.complete("two");
    check(any.isDone(), "any is done after f2");
    check("two".equals(any.join()), "any result");
    check(!all.isDone(), "all still not done");

    f1.complete("one");
    f3.complete("three");
    check(all.isDone(), "all is done after all completed");
    all.join();
    System.out.println("allOf and anyOf: ok");
  }

  private static void testTimeouts() throws Exception {
    CompletableFuture<String> f1 = new CompletableFuture<String>();
    f1.completeOnTimeout("timedOutFallback", 50, TimeUnit.MILLISECONDS);
    check("timedOutFallback".equals(f1.get(2, TimeUnit.SECONDS)), "completeOnTimeout result");

    CompletableFuture<String> f2 = new CompletableFuture<String>();
    f2.orTimeout(50, TimeUnit.MILLISECONDS);
    try {
      f2.get(2, TimeUnit.SECONDS);
      check(false, "orTimeout should throw ExecutionException");
    } catch (ExecutionException e) {
      check(e.getCause() instanceof TimeoutException, "orTimeout cause TimeoutException");
    }
    System.out.println("timeouts: ok");
  }

  private static void testExecutorsAndForkJoin() throws Exception {
    ForkJoinPool pool = ForkJoinPool.commonPool();
    check(pool != null, "commonPool not null");
    check(ForkJoinPool.getCommonPoolParallelism() >= 1, "parallelism >= 1");

    ExecutorService fixed = Executors.newFixedThreadPool(2);
    Future<Integer> fut = fixed.submit(new Callable<Integer>() {
      @Override
      public Integer call() {
        return 99;
      }
    });
    check(Integer.valueOf(99).equals(fut.get(2, TimeUnit.SECONDS)), "fixed thread pool submit");
    fixed.shutdown();

    ExecutorService single = Executors.newSingleThreadExecutor();
    Future<String> sFut = single.submit(() -> {}, "ok");
    check("ok".equals(sFut.get(2, TimeUnit.SECONDS)), "single thread executor submit");
    single.shutdown();

    ExecutorService perTask = Executors.newVirtualThreadPerTaskExecutor();
    Future<String> vFut = perTask.submit(new Callable<String>() {
      @Override
      public String call() {
        return "virtualTask";
      }
    });
    check("virtualTask".equals(vFut.get(2, TimeUnit.SECONDS)), "virtual thread per task");
    perTask.shutdown();
    System.out.println("executors and forkjoin: ok");
  }

  private static void testFlow() {
    check(Flow.defaultBufferSize() == 256, "Flow defaultBufferSize is 256");

    List<Integer> received = new ArrayList<Integer>();
    Flow.Subscriber<Integer> subscriber = new Flow.Subscriber<Integer>() {
      private Flow.Subscription subscription;

      @Override
      public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(10);
      }

      @Override
      public void onNext(Integer item) {
        received.add(item);
      }

      @Override
      public void onError(Throwable throwable) {
      }

      @Override
      public void onComplete() {
        received.add(999);
      }
    };

    Flow.Publisher<Integer> publisher = new Flow.Publisher<Integer>() {
      @Override
      public void subscribe(Flow.Subscriber<? super Integer> sub) {
        sub.onSubscribe(new Flow.Subscription() {
          @Override
          public void request(long n) {
            for (int i = 0; i < n && i < 3; ++i) {
              sub.onNext(i + 1);
            }
            sub.onComplete();
          }

          @Override
          public void cancel() {
          }
        });
      }
    };

    publisher.subscribe(subscriber);
    check(received.size() == 4, "subscriber received count");
    check(received.get(0) == 1 && received.get(1) == 2 && received.get(2) == 3 && received.get(3) == 999, "subscriber items");
    System.out.println("flow: ok");
  }
}
