/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.concurrent;

public interface Future<V> {
  enum State {
    RUNNING,
    SUCCESS,
    FAILED,
    CANCELLED
  }

  public boolean cancel(boolean mayInterruptIfRunning);
  
  public boolean isCancelled();
  
  public boolean isDone();
  
  public V get() throws InterruptedException, ExecutionException;
  
  public V get(long timeout, TimeUnit unit) throws InterruptedException, 
                                                   ExecutionException, 
                                                   TimeoutException;

  default State state() {
    if (!isDone()) {
      return State.RUNNING;
    }
    if (isCancelled()) {
      return State.CANCELLED;
    }
    try {
      get();
      return State.SUCCESS;
    } catch (ExecutionException e) {
      return State.FAILED;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return State.FAILED;
    }
  }

  default V resultNow() {
    if (!isDone()) {
      throw new IllegalStateException("Task has not completed");
    }
    State s = state();
    switch (s) {
      case SUCCESS:
        try {
          return get();
        } catch (InterruptedException | ExecutionException e) {
          throw new IllegalStateException(e);
        }
      case FAILED:
        throw new IllegalStateException("Task completed with exception");
      case CANCELLED:
        throw new IllegalStateException("Task was cancelled");
      default:
        throw new IllegalStateException("Task has not completed");
    }
  }

  default Throwable exceptionNow() {
    if (!isDone()) {
      throw new IllegalStateException("Task has not completed");
    }
    if (state() != State.FAILED) {
      throw new IllegalStateException("Task completed with state: " + state());
    }
    try {
      get();
      throw new IllegalStateException("Task completed normally");
    } catch (ExecutionException e) {
      return e.getCause() != null ? e.getCause() : e;
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }
}
