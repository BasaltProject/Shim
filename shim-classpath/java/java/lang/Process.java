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

package java.lang;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public abstract class Process {
  public abstract void destroy();

  public abstract int exitValue();

  public abstract InputStream getInputStream();

  public abstract OutputStream getOutputStream();

  public abstract InputStream getErrorStream();

  public abstract int waitFor() throws InterruptedException;

  public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
    long startTime = System.nanoTime();
    long rem = unit.toNanos(timeout);
    do {
      try {
        exitValue();
        return true;
      } catch (IllegalThreadStateException ex) {
        if (rem > 0) {
          long sleepNanos = Math.min(rem, 100_000_000L);
          Thread.sleep(sleepNanos / 1_000_000L, (int) (sleepNanos % 1_000_000L));
        }
      }
      rem = unit.toNanos(timeout) - (System.nanoTime() - startTime);
    } while (rem > 0);
    return false;
  }

  public Process destroyForcibly() {
    destroy();
    return this;
  }

  public boolean isAlive() {
    try {
      exitValue();
      return false;
    } catch (IllegalThreadStateException e) {
      return true;
    }
  }

  public long pid() {
    return toHandle().pid();
  }

  public ProcessHandle toHandle() {
    throw new UnsupportedOperationException();
  }

  public boolean supportsNormalTermination() {
    return toHandle().supportsNormalTermination();
  }

  public CompletableFuture<Process> onExit() {
    return CompletableFuture.supplyAsync(() -> {
      boolean interrupted = false;
      while (true) {
        try {
          waitFor();
          break;
        } catch (InterruptedException e) {
          interrupted = true;
        }
      }
      if (interrupted) {
        Thread.currentThread().interrupt();
      }
      return this;
    });
  }

  public BufferedReader inputReader() {
    return inputReader(StandardCharsets.UTF_8);
  }

  public BufferedReader inputReader(Charset cs) {
    return new BufferedReader(new InputStreamReader(getInputStream(), cs));
  }

  public BufferedReader errorReader() {
    return errorReader(StandardCharsets.UTF_8);
  }

  public BufferedReader errorReader(Charset cs) {
    return new BufferedReader(new InputStreamReader(getErrorStream(), cs));
  }

  public BufferedWriter outputWriter() {
    return outputWriter(StandardCharsets.UTF_8);
  }

  public BufferedWriter outputWriter(Charset cs) {
    return new BufferedWriter(new OutputStreamWriter(getOutputStream(), cs));
  }

  public Stream<ProcessHandle> children() {
    return toHandle().children();
  }

  public Stream<ProcessHandle> descendants() {
    return toHandle().descendants();
  }

  public ProcessHandle.Info info() {
    return toHandle().info();
  }

  public boolean waitFor(java.time.Duration duration) throws InterruptedException {
    java.util.Objects.requireNonNull(duration, "duration");
    long nanos;
    try {
      nanos = duration.toNanos();
    } catch (ArithmeticException e) {
      nanos = duration.isNegative() ? Long.MIN_VALUE : Long.MAX_VALUE;
    }
    return waitFor(nanos, TimeUnit.NANOSECONDS);
  }
}
