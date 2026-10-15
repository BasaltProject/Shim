/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SystemProcessAndClassValueModern {
  public static void main(String[] args) throws Throwable {
    testClassValue();
    testSystemLogger();
    testProcessBuilder();
    System.out.println("ALL SYSTEM PROCESS AND CLASSVALUE MODERN TESTS PASSED");
  }

  private static void assertTrue(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }

  private static void assertEquals(Object expected, Object actual, String message) {
    if (expected == null ? actual != null : !expected.equals(actual)) {
      throw new AssertionError(message + ": expected <" + expected + ">, but was <" + actual + ">");
    }
  }

  private static void testClassValue() throws Exception {
    AtomicInteger computeCount = new AtomicInteger(0);

    ClassValue<String> nameValue = new ClassValue<String>() {
      @Override
      protected String computeValue(Class<?> type) {
        computeCount.incrementAndGet();
        return "Type:" + type.getSimpleName();
      }
    };

    assertEquals("Type:String", nameValue.get(String.class), "First compute String");
    assertEquals(1, computeCount.get(), "Computed once for String");
    assertEquals("Type:String", nameValue.get(String.class), "Second get String");
    assertEquals(1, computeCount.get(), "Still computed once for String (cached)");

    assertEquals("Type:Integer", nameValue.get(Integer.class), "Compute Integer");
    assertEquals(2, computeCount.get(), "Computed twice across two classes");

    nameValue.remove(String.class);
    assertEquals("Type:String", nameValue.get(String.class), "Recomputed after remove");
    assertEquals(3, computeCount.get(), "Recomputed count is 3");

    ClassValue<Object> nullValue = new ClassValue<Object>() {
      @Override
      protected Object computeValue(Class<?> type) {
        return null;
      }
    };
    assertEquals(null, nullValue.get(Double.class), "Null computeValue");

    Thread[] threads = new Thread[4];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(() -> {
        for (int j = 0; j < 100; j++) {
          nameValue.get(Thread.class);
        }
      });
      threads[i].start();
    }
    for (Thread t : threads) {
      t.join();
    }
    assertEquals("Type:Thread", nameValue.get(Thread.class), "Multi-threaded get");
  }

  private static void testSystemLogger() {
    System.Logger logger = System.getLogger("test.logger");
    assertEquals("test.logger", logger.getName(), "Logger name");

    assertTrue(logger.isLoggable(System.Logger.Level.INFO), "INFO should be loggable");
    assertTrue(logger.isLoggable(System.Logger.Level.WARNING), "WARNING should be loggable");
    assertTrue(logger.isLoggable(System.Logger.Level.ERROR), "ERROR should be loggable");
    assertTrue(logger.isLoggable(System.Logger.Level.OFF), "OFF should be loggable");
    assertTrue(!logger.isLoggable(System.Logger.Level.DEBUG), "DEBUG should not be loggable");
    assertTrue(!logger.isLoggable(System.Logger.Level.TRACE), "TRACE should not be loggable");
    assertTrue(!logger.isLoggable(System.Logger.Level.ALL), "ALL should not be loggable");

    assertEquals("ALL", System.Logger.Level.ALL.getName(), "ALL name");
    assertEquals(Integer.MIN_VALUE, System.Logger.Level.ALL.getSeverity(), "ALL severity");
    assertEquals("INFO", System.Logger.Level.INFO.getName(), "INFO name");
    assertEquals(800, System.Logger.Level.INFO.getSeverity(), "INFO severity");
    assertEquals("ERROR", System.Logger.Level.ERROR.getName(), "ERROR name");
    assertEquals(1000, System.Logger.Level.ERROR.getSeverity(), "ERROR severity");

    logger.log(System.Logger.Level.INFO, "Standard message");
    logger.log(System.Logger.Level.INFO, () -> "Lazy message supplier");
    logger.log(System.Logger.Level.INFO, 12345);
    logger.log(System.Logger.Level.INFO, "Formatted: %s = %d", "key", 42);
    logger.log(System.Logger.Level.WARNING, "Warning message", new RuntimeException("test warning"));

    System.LoggerFinder finder = System.LoggerFinder.getLoggerFinder();
    assertTrue(finder != null, "LoggerFinder must not be null");
    System.Logger found = finder.getLogger("another.logger", SystemProcessAndClassValueModern.class.getModule());
    assertEquals("another.logger", found.getName(), "Found logger name");
  }

  private static void testProcessBuilder() throws Exception {
    ProcessBuilder pb = new ProcessBuilder("echo", "BasaltShimProcess");
    assertEquals(Arrays.asList("echo", "BasaltShimProcess"), pb.command(), "Command list");

    pb.command(Arrays.asList("echo", "UpdatedCommand"));
    assertEquals(Arrays.asList("echo", "UpdatedCommand"), pb.command(), "Updated command list");

    Map<String, String> env = pb.environment();
    assertTrue(env != null, "Environment must not be null");
    env.put("TEST_KEY", "TEST_VAL");
    assertEquals("TEST_VAL", pb.environment().get("TEST_KEY"), "Environment key/val");

    File dir = new File("/tmp");
    pb.directory(dir);
    assertEquals(dir, pb.directory(), "Directory match");

    assertTrue(!pb.redirectErrorStream(), "redirectErrorStream default false");
    pb.redirectErrorStream(true);
    assertTrue(pb.redirectErrorStream(), "redirectErrorStream set true");

    assertEquals(ProcessBuilder.Redirect.Type.PIPE, pb.redirectInput().type(), "Default input redirect PIPE");
    assertEquals(ProcessBuilder.Redirect.Type.PIPE, pb.redirectOutput().type(), "Default output redirect PIPE");
    assertEquals(ProcessBuilder.Redirect.Type.PIPE, pb.redirectError().type(), "Default error redirect PIPE");

    pb.inheritIO();
    assertEquals(ProcessBuilder.Redirect.Type.INHERIT, pb.redirectInput().type(), "InheritIO input");
    assertEquals(ProcessBuilder.Redirect.Type.INHERIT, pb.redirectOutput().type(), "InheritIO output");
    assertEquals(ProcessBuilder.Redirect.Type.INHERIT, pb.redirectError().type(), "InheritIO error");

    File file = new File("test_redirect.log");
    ProcessBuilder.Redirect toFile = ProcessBuilder.Redirect.to(file);
    assertEquals(ProcessBuilder.Redirect.Type.WRITE, toFile.type(), "to file type WRITE");
    assertEquals(file, toFile.file(), "to file match");

    ProcessBuilder.Redirect fromFile = ProcessBuilder.Redirect.from(file);
    assertEquals(ProcessBuilder.Redirect.Type.READ, fromFile.type(), "from file type READ");
    assertEquals(file, fromFile.file(), "from file match");

    ProcessBuilder.Redirect appendFile = ProcessBuilder.Redirect.appendTo(file);
    assertEquals(ProcessBuilder.Redirect.Type.APPEND, appendFile.type(), "append file type APPEND");
    assertEquals(file, appendFile.file(), "append file match");

    ProcessBuilder echoPb = new ProcessBuilder("echo", "HelloModernProcess");
    Process p = echoPb.start();
    assertTrue(p != null, "Process must not be null");

    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
    String line = reader.readLine();
    assertEquals("HelloModernProcess", line, "Process standard output line");

    int exitCode = p.waitFor();
    assertEquals(0, exitCode, "Exit code 0");

    List<ProcessBuilder> pipeline = new ArrayList<ProcessBuilder>();
    pipeline.add(new ProcessBuilder("true"));
    List<Process> processes = ProcessBuilder.startPipeline(pipeline);
    assertEquals(1, processes.size(), "Pipeline size 1");
    assertEquals(0, processes.get(0).waitFor(), "Pipeline process exit code 0");
  }
}
