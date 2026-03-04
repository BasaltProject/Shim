/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public class CompletableFutureFailuresModern {
  static String show(CompletableFuture<?> f) {
    for (int i = 0; i < 100 && !f.isDone(); i++) {
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        break;
      }
    }
    if (!f.isDone()) {
      return "pending";
    }
    try {
      return "value " + f.get();
    } catch (ExecutionException e) {
      Throwable c = e.getCause();
      return "failed " + c.getClass().getSimpleName() + ":" + c.getMessage() + (f.isCancelled() ? " cancelled" : "");
    } catch (CancellationException e) {
      return "cancelled";
    } catch (Exception e) {
      return "other " + e;
    }
  }

  static void t(String name, Supplier<CompletableFuture<?>> s) {
    try {
      CompletableFuture<?> f = s.get();
      System.out.println(name + " = " + show(f));
    } catch (Throwable e) {
      System.out.println(name + " ! " + e.getClass().getSimpleName() + ":" + e.getMessage());
    }
  }

  static CompletableFuture<Integer> bad() {
    CompletableFuture<Integer> f = new CompletableFuture<>();
    f.completeExceptionally(new IllegalStateException("boom"));
    return f;
  }

  static CompletableFuture<Integer> good() {
    return CompletableFuture.completedFuture(5);
  }

  static CompletableFuture<Integer> pending() {
    return new CompletableFuture<>();
  }

  public static void main(String[] args) throws Exception {
    ExecutorService ex = Executors.newFixedThreadPool(2);
    Executor direct = Runnable::run;
    for (Executor e : new Executor[] {null, direct, ex}) {
      String tag = e == null ? "dflt" : e == direct ? "direct" : "pool";
      t(tag + " apply-bad", () -> e == null ? bad().thenApply(x -> x + 1) : bad().thenApplyAsync(x -> x + 1, e));
      t(tag + " apply-throw", () -> e == null ? good().thenApply(x -> { throw new RuntimeException("t1"); })
          : good().thenApplyAsync(x -> { throw new RuntimeException("t1"); }, e));
      t(tag + " accept-bad", () -> e == null ? bad().thenAccept(x -> { }) : bad().thenAcceptAsync(x -> { }, e));
      t(tag + " run-bad", () -> e == null ? bad().thenRun(() -> { }) : bad().thenRunAsync(() -> { }, e));
      t(tag + " compose-bad", () -> e == null ? bad().thenCompose(x -> good()) : bad().thenComposeAsync(x -> good(), e));
      t(tag + " compose-inner-bad", () -> e == null ? good().thenCompose(x -> bad()) : good().thenComposeAsync(x -> bad(), e));
      t(tag + " compose-throw", () -> e == null ? good().thenCompose(x -> { throw new RuntimeException("t2"); })
          : good().thenComposeAsync(x -> { throw new RuntimeException("t2"); }, e));
      t(tag + " combine-bad-left", () -> e == null ? bad().thenCombine(good(), Integer::sum)
          : bad().thenCombineAsync(good(), Integer::sum, e));
      t(tag + " combine-bad-right", () -> e == null ? good().thenCombine(bad(), Integer::sum)
          : good().thenCombineAsync(bad(), Integer::sum, e));
      t(tag + " combine-throw", () -> e == null ? good().thenCombine(good(), (a, b) -> { throw new RuntimeException("t3"); })
          : good().thenCombineAsync(good(), (a, b) -> { throw new RuntimeException("t3"); }, e));
      t(tag + " both-bad", () -> e == null ? bad().thenAcceptBoth(good(), (a, b) -> { })
          : bad().thenAcceptBothAsync(good(), (a, b) -> { }, e));
      t(tag + " runboth-bad", () -> e == null ? good().runAfterBoth(bad(), () -> { }) : good().runAfterBothAsync(bad(), () -> { }, e));
      t(tag + " either-bad", () -> e == null ? bad().applyToEither(pending(), x -> x) : bad().applyToEitherAsync(pending(), x -> x, e));
      t(tag + " either-good", () -> e == null ? pending().applyToEither(good(), x -> x * 2) : pending().applyToEitherAsync(good(), x -> x * 2, e));
      t(tag + " accepteither-bad", () -> e == null ? pending().acceptEither(bad(), x -> { }) : pending().acceptEitherAsync(bad(), x -> { }, e));
      t(tag + " handle-bad", () -> e == null ? bad().handle((v, x) -> x.getClass().getSimpleName() + "/" + String.valueOf(x.getCause()).length())
          : bad().handleAsync((v, x) -> x.getClass().getSimpleName() + "/" + String.valueOf(x.getCause()).length(), e));
      t(tag + " handle-throw", () -> e == null ? bad().handle((v, x) -> { throw new RuntimeException("t4"); })
          : bad().handleAsync((v, x) -> { throw new RuntimeException("t4"); }, e));
      t(tag + " when-bad", () -> e == null ? bad().whenComplete((v, x) -> { }) : bad().whenCompleteAsync((v, x) -> { }, e));
      t(tag + " when-throw-on-good", () -> e == null ? good().whenComplete((v, x) -> { throw new RuntimeException("t5"); })
          : good().whenCompleteAsync((v, x) -> { throw new RuntimeException("t5"); }, e));
      t(tag + " when-throw-on-bad", () -> e == null ? bad().whenComplete((v, x) -> { throw new RuntimeException("t6"); })
          : bad().whenCompleteAsync((v, x) -> { throw new RuntimeException("t6"); }, e));
      t(tag + " exceptionally", () -> bad().exceptionally(x -> 9));
      t(tag + " exceptionally-throw", () -> bad().exceptionally(x -> { throw new RuntimeException("t7"); }));
      t(tag + " exceptionallyCompose", () -> e == null ? bad().exceptionallyCompose(x -> good()) : bad().exceptionallyComposeAsync(x -> good(), e));
      t(tag + " exceptionallyCompose-bad", () -> e == null ? bad().exceptionallyCompose(x -> bad()) : bad().exceptionallyComposeAsync(x -> bad(), e));
    }
    t("allOf-bad", () -> CompletableFuture.allOf(good(), bad(), good()));
    t("allOf-empty", () -> CompletableFuture.allOf());
    t("allOf-good", () -> CompletableFuture.allOf(good(), good()));
    t("anyOf-bad", () -> CompletableFuture.anyOf(pending(), bad()));
    t("anyOf-good", () -> CompletableFuture.anyOf(pending(), good()));
    t("anyOf-empty", () -> CompletableFuture.anyOf());
    t("supplyAsync-throw", () -> CompletableFuture.supplyAsync(() -> { throw new RuntimeException("s1"); }, ex));
    t("runAsync-throw", () -> CompletableFuture.runAsync(() -> { throw new IllegalArgumentException("s2"); }, ex));
    t("failedFuture", () -> CompletableFuture.failedFuture(new Error("err")));
    t("copy-bad", () -> bad().copy());
    t("minimal-bad", () -> bad().minimalCompletionStage().toCompletableFuture());
    t("toCF-bad", () -> bad().toCompletableFuture());
    t("cancel-dep", () -> { CompletableFuture<Integer> p = pending(); CompletableFuture<Integer> d = p.thenApply(x -> x); p.cancel(true); return d; });
    t("complete-twice", () -> { CompletableFuture<Integer> p = pending(); p.complete(1); p.complete(2); p.completeExceptionally(new Error("x")); return p; });
    t("orTimeout", () -> pending().orTimeout(50, TimeUnit.MILLISECONDS));
    t("completeOnTimeout", () -> pending().completeOnTimeout(7, 50, TimeUnit.MILLISECONDS));
    t("delayedExecutor", () -> CompletableFuture.supplyAsync(() -> 3, CompletableFuture.delayedExecutor(30, TimeUnit.MILLISECONDS)));
    t("obtrude", () -> { CompletableFuture<Integer> p = good(); p.obtrudeException(new Error("o")); return p; });
    t("rejecting-executor", () -> good().thenApplyAsync(x -> x, r -> { throw new RejectedExecutionException("rej"); }));
    t("rejecting-supply", () -> CompletableFuture.supplyAsync(() -> 1, r -> { throw new RejectedExecutionException("rej2"); }));
    t("chain", () -> {
      CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> 1, ex);
      for (int i = 0; i < 200; i++) {
        final int k = i;
        f = f.thenApplyAsync(x -> x + 1, ex).thenCompose(x -> k % 5 == 0 ? CompletableFuture.supplyAsync(() -> x + 1, ex) : good().thenApply(y -> x + 1));
      }
      try {
        f.get(20, TimeUnit.SECONDS);
      } catch (Exception e) {
        return CompletableFuture.failedFuture(e);
      }
      return f;
    });
    ex.shutdown();
  }
}
