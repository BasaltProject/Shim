/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 21

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipAndFutureModern {
  public static void main(String[] args) throws Throwable {
    testCompletableFutureState();
    testFutureTaskState();
    testForkJoinTaskState();
    testGzipRoundtrip();
    testZipStreamRoundtrip();
    System.out.println("ALL ZIP AND FUTURE MODERN TESTS PASSED");
  }

  private static void testCompletableFutureState() {
    CompletableFuture<String> cf = new CompletableFuture<String>();
    assertEquals(Future.State.RUNNING, cf.state(), "CF initial state");
    assertThrowsIllegalState(() -> cf.resultNow(), "resultNow on uncompleted");
    assertThrowsIllegalState(() -> cf.exceptionNow(), "exceptionNow on uncompleted");

    cf.complete("basalt_value");
    assertEquals(Future.State.SUCCESS, cf.state(), "CF completed state");
    assertEquals("basalt_value", cf.resultNow(), "CF resultNow");
    assertThrowsIllegalState(() -> cf.exceptionNow(), "exceptionNow on success");

    CompletableFuture<String> failed = new CompletableFuture<String>();
    IllegalArgumentException testEx = new IllegalArgumentException("expected failure");
    failed.completeExceptionally(testEx);
    assertEquals(Future.State.FAILED, failed.state(), "CF failed state");
    assertEquals(testEx, failed.exceptionNow(), "CF exceptionNow");
    assertThrowsIllegalState(() -> failed.resultNow(), "resultNow on failure");

    CompletableFuture<String> cancelled = new CompletableFuture<String>();
    cancelled.cancel(false);
    assertEquals(Future.State.CANCELLED, cancelled.state(), "CF cancelled state");
    assertThrowsIllegalState(() -> cancelled.resultNow(), "resultNow on cancelled");
    assertThrowsIllegalState(() -> cancelled.exceptionNow(), "exceptionNow on cancelled");
  }

  private static void testFutureTaskState() throws Exception {
    FutureTask<Integer> ft = new FutureTask<Integer>(new Callable<Integer>() {
      @Override
      public Integer call() {
        return 12345;
      }
    });
    assertEquals(Future.State.RUNNING, ft.state(), "FT initial state");
    ft.run();
    assertEquals(Future.State.SUCCESS, ft.state(), "FT success state");
    assertEquals(Integer.valueOf(12345), ft.resultNow(), "FT resultNow");
    assertThrowsIllegalState(() -> ft.exceptionNow(), "FT exceptionNow on success");

    final IllegalStateException boom = new IllegalStateException("task error");
    FutureTask<Integer> ftFail = new FutureTask<Integer>(new Callable<Integer>() {
      @Override
      public Integer call() {
        throw boom;
      }
    });
    ftFail.run();
    assertEquals(Future.State.FAILED, ftFail.state(), "FT failed state");
    assertEquals(boom, ftFail.exceptionNow(), "FT exceptionNow");
    assertThrowsIllegalState(() -> ftFail.resultNow(), "FT resultNow on failure");

    FutureTask<Integer> ftCancel = new FutureTask<Integer>(new Callable<Integer>() {
      @Override
      public Integer call() {
        return 1;
      }
    });
    ftCancel.cancel(false);
    assertEquals(Future.State.CANCELLED, ftCancel.state(), "FT cancelled state");
    assertThrowsIllegalState(() -> ftCancel.resultNow(), "FT resultNow on cancelled");
    assertThrowsIllegalState(() -> ftCancel.exceptionNow(), "FT exceptionNow on cancelled");
  }

  private static void testForkJoinTaskState() {
    ForkJoinTask<String> task = ForkJoinTask.adapt(new Callable<String>() {
      @Override
      public String call() {
        return "fork_join_done";
      }
    });
    assertEquals(Future.State.RUNNING, task.state(), "FJT initial state");
    task.invoke();
    assertEquals(Future.State.SUCCESS, task.state(), "FJT success state");
    assertEquals("fork_join_done", task.resultNow(), "FJT resultNow");
    assertThrowsIllegalState(() -> task.exceptionNow(), "FJT exceptionNow on success");

    ForkJoinTask<String> failTask = ForkJoinTask.adapt(new Callable<String>() {
      @Override
      public String call() {
        throw new RuntimeException("fj failure");
      }
    });
    try {
      failTask.invoke();
    } catch (Throwable ignored) {
    }
    assertEquals(Future.State.FAILED, failTask.state(), "FJT failed state");
    assertTrue(failTask.exceptionNow() != null, "FJT exceptionNow not null");
    assertThrowsIllegalState(() -> failTask.resultNow(), "FJT resultNow on failure");

    ForkJoinTask<String> cancelTask = ForkJoinTask.adapt(new Callable<String>() {
      @Override
      public String call() {
        return "never";
      }
    });
    cancelTask.cancel(false);
    assertEquals(Future.State.CANCELLED, cancelTask.state(), "FJT cancelled state");
    assertThrowsIllegalState(() -> cancelTask.resultNow(), "FJT resultNow on cancelled");
    assertThrowsIllegalState(() -> cancelTask.exceptionNow(), "FJT exceptionNow on cancelled");
  }

  private static void testGzipRoundtrip() throws Exception {
    String originalText = "Basalt Shim Modern GZIP stream roundtrip test with some repetitive payload data: "
        + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        + "1234567890!@#$%^&*()_+-=[]{}|;':,./<>?`~";
    byte[] data = originalText.getBytes(StandardCharsets.UTF_8);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPOutputStream gzos = new GZIPOutputStream(baos);
    gzos.write(data);
    gzos.finish();
    gzos.close();

    byte[] compressed = baos.toByteArray();
    assertTrue(compressed.length > 10, "Compressed output non empty");
    assertEquals(0x1f, compressed[0] & 0xff, "GZIP magic byte 0");
    assertEquals(0x8b, compressed[1] & 0xff, "GZIP magic byte 1");

    ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
    GZIPInputStream gzis = new GZIPInputStream(bais);
    ByteArrayOutputStream decompressedOut = new ByteArrayOutputStream();
    byte[] buf = new byte[32];
    int n;
    while ((n = gzis.read(buf)) != -1) {
      decompressedOut.write(buf, 0, n);
    }
    gzis.close();

    byte[] decompressed = decompressedOut.toByteArray();
    assertTrue(Arrays.equals(data, decompressed), "Decompressed bytes match original");
    assertEquals(originalText, new String(decompressed, StandardCharsets.UTF_8), "Decompressed text matches");
  }

  private static void testZipStreamRoundtrip() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(baos);

    ZipEntry entry1 = new ZipEntry("first.txt");
    entry1.setMethod(ZipEntry.DEFLATED);
    zos.putNextEntry(entry1);
    byte[] content1 = "First file content in zip".getBytes(StandardCharsets.UTF_8);
    zos.write(content1);
    zos.closeEntry();

    ZipEntry entry2 = new ZipEntry("dir/second.txt");
    entry2.setMethod(ZipEntry.DEFLATED);
    zos.putNextEntry(entry2);
    byte[] content2 = "Second file content inside a subdirectory in the zip archive".getBytes(StandardCharsets.UTF_8);
    zos.write(content2);
    zos.closeEntry();

    zos.finish();
    zos.close();

    byte[] zipData = baos.toByteArray();
    assertTrue(zipData.length > 50, "Zip bytes non empty");

    ByteArrayInputStream bais = new ByteArrayInputStream(zipData);
    ZipInputStream zis = new ZipInputStream(bais);

    ZipEntry read1 = zis.getNextEntry();
    assertTrue(read1 != null, "Entry 1 found");
    assertEquals("first.txt", read1.getName(), "Entry 1 name");
    ByteArrayOutputStream out1 = new ByteArrayOutputStream();
    byte[] buf = new byte[16];
    int r;
    while ((r = zis.read(buf)) != -1) {
      out1.write(buf, 0, r);
    }
    assertTrue(Arrays.equals(content1, out1.toByteArray()), "Entry 1 content matches");
    zis.closeEntry();

    ZipEntry read2 = zis.getNextEntry();
    assertTrue(read2 != null, "Entry 2 found");
    assertEquals("dir/second.txt", read2.getName(), "Entry 2 name");
    ByteArrayOutputStream out2 = new ByteArrayOutputStream();
    while ((r = zis.read(buf)) != -1) {
      out2.write(buf, 0, r);
    }
    assertTrue(Arrays.equals(content2, out2.toByteArray()), "Entry 2 content matches");
    zis.closeEntry();

    ZipEntry read3 = zis.getNextEntry();
    assertTrue(read3 == null, "No further entries");
    zis.close();
  }

  private static void assertEquals(Object expected, Object actual, String message) {
    if (expected == null && actual == null) return;
    if (expected != null && expected.equals(actual)) return;
    throw new AssertionError(message + ": expected <" + expected + "> but got <" + actual + ">");
  }

  private static void assertTrue(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError("Assertion failed: " + message);
    }
  }

  private static void assertThrowsIllegalState(ThrowingRunnable r, String message) {
    try {
      r.run();
      throw new AssertionError("Expected IllegalStateException: " + message);
    } catch (IllegalStateException expected) {
      // expected
    } catch (Throwable other) {
      throw new AssertionError("Expected IllegalStateException but got " + other + ": " + message);
    }
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Throwable;
  }
}
