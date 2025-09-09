/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.io.ByteArrayOutputStream;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

public class LoggingModern {
  public static void main(String[] args) throws Exception {
    testLevels();
    testLogRecord();
    testLogManager();
    testLoggerHierarchyAndHandlers();
    testLazySuppliers();
    testFormattingAndStreamHandler();
    System.out.println("LoggingModern OK");
  }

  private static void testLevels() {
    Level[] levels = {
      Level.OFF, Level.SEVERE, Level.WARNING, Level.INFO,
      Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.ALL
    };
    for (Level l : levels) {
      System.out.println("level " + l.getName() + "=" + l.intValue());
    }

    Level parsedInfo = Level.parse("INFO");
    System.out.println("parsed info equals=" + parsedInfo.equals(Level.INFO));

    Level parsedNumeric = Level.parse("850");
    System.out.println("parsed numeric value=" + parsedNumeric.intValue());
    System.out.println("parsed numeric equals=" + parsedNumeric.equals(new CustomLevel("CUSTOM_850", 850)));
  }

  private static void testLogRecord() {
    LogRecord r1 = new LogRecord(Level.WARNING, "first message");
    LogRecord r2 = new LogRecord(Level.SEVERE, "second message");
    System.out.println("record sequence order=" + (r2.getSequenceNumber() > r1.getSequenceNumber()));
    System.out.println("record instant nonnull=" + (r1.getInstant() != null));
    System.out.println("record millis valid=" + (r1.getMillis() > 0));

    r1.setSourceClassName("com.example.Test");
    r1.setSourceMethodName("doWork");
    r1.setParameters(new Object[] { "arg1", 42 });
    System.out.println("record source=" + r1.getSourceClassName() + "." + r1.getSourceMethodName());
    System.out.println("record param count=" + r1.getParameters().length);
  }

  private static void testLogManager() {
    LogManager manager = LogManager.getLogManager();
    System.out.println("logmanager exists=" + (manager != null));

    Logger custom = Logger.getLogger("org.basaltmc.test.CustomLogger");
    Logger retrieved = manager.getLogger("org.basaltmc.test.CustomLogger");
    System.out.println("retrieved logger same=" + (custom == retrieved));

    boolean found = false;
    Enumeration<String> names = manager.getLoggerNames();
    while (names.hasMoreElements()) {
      if ("org.basaltmc.test.CustomLogger".equals(names.nextElement())) {
        found = true;
        break;
      }
    }
    System.out.println("logger found in names=" + found);
  }

  private static void testLoggerHierarchyAndHandlers() {
    Logger parent = Logger.getLogger("org.basaltmc.parent");
    Logger child = Logger.getLogger("org.basaltmc.parent.child");
    child.setParent(parent);

    parent.setLevel(Level.WARNING);
    child.setLevel(null);

    System.out.println("child loggable info=" + child.isLoggable(Level.INFO));
    System.out.println("child loggable warning=" + child.isLoggable(Level.WARNING));
    System.out.println("child loggable severe=" + child.isLoggable(Level.SEVERE));

    AtomicInteger parentPublishes = new AtomicInteger(0);
    parent.addHandler(new Handler() {
      public void publish(LogRecord r) { parentPublishes.incrementAndGet(); }
      public void flush() {}
      public void close() {}
    });

    child.warning("warning to child");
    System.out.println("parent received child warning=" + (parentPublishes.get() == 1));

    child.setUseParentHandlers(false);
    child.warning("another warning to child");
    System.out.println("parent received after disable=" + (parentPublishes.get() == 1));

    Filter filter = r -> r.getMessage().contains("allow");
    child.setFilter(filter);
    AtomicInteger childPublishes = new AtomicInteger(0);
    child.addHandler(new Handler() {
      public void publish(LogRecord r) { childPublishes.incrementAndGet(); }
      public void flush() {}
      public void close() {}
    });

    child.warning("drop this");
    child.warning("allow this");
    System.out.println("child filtered count=" + childPublishes.get());
  }

  private static void testLazySuppliers() {
    Logger logger = Logger.getLogger("org.basaltmc.lazy");
    logger.setLevel(Level.WARNING);
    logger.setUseParentHandlers(false);

    AtomicBoolean evaluated = new AtomicBoolean(false);
    logger.info(() -> {
      evaluated.set(true);
      return "expensive message";
    });
    System.out.println("supplier evaluated below level=" + evaluated.get());

    logger.warning(() -> {
      evaluated.set(true);
      return "necessary message";
    });
    System.out.println("supplier evaluated at level=" + evaluated.get());
  }

  private static void testFormattingAndStreamHandler() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    StreamHandler sh = new StreamHandler(baos, new Formatter() {
      public String format(LogRecord r) {
        return r.getLevel() + ": " + formatMessage(r) + "\n";
      }
    });
    sh.setLevel(Level.INFO);

    Logger logger = Logger.getLogger("org.basaltmc.streamtest");
    logger.setUseParentHandlers(false);
    logger.addHandler(sh);

    logger.log(Level.INFO, "hello {0}, count is {1}", new Object[] { "world", 99 });
    sh.flush();

    String output = baos.toString();
    System.out.println("stream output contains hello world=" + output.contains("hello world, count is 99"));
  }

  private static class CustomLevel extends Level {
    public CustomLevel(String name, int value) {
      super(name, value);
    }
  }
}
