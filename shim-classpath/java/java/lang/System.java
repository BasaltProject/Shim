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

import java.io.PrintStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileDescriptor;
import java.util.Map;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.function.Supplier;

public abstract class System {
  private static class NanoTime {
    public static final long BaseInMillis = currentTimeMillis();
  }
  
  private static class Static {
    public static Properties properties = makeProperties();
  }

  private static Map<String, String> environment;
  
  private static SecurityManager securityManager;
  //   static {
  //     loadLibrary("natives");
  //   }

  public static final PrintStream out = new PrintStream
    (new BufferedOutputStream(new FileOutputStream(FileDescriptor.out)), true);

  public static final PrintStream err = new PrintStream
    (new BufferedOutputStream(new FileOutputStream(FileDescriptor.err)), true);

  public static final InputStream in
    = new BufferedInputStream(new FileInputStream(FileDescriptor.in));

  public static native void arraycopy(Object src, int srcOffset, Object dst,
                                      int dstOffset, int length);

  public static String getProperty(String name) {
    return (String) Static.properties.get(name);
  }
  
  public static String getProperty(String name, String defaultValue) {
    String result = getProperty(name);
    if (result==null) {
      return defaultValue;
    }
    return result;
  }
  
  public static String setProperty(String name, String value) {
    return (String) Static.properties.put(name, value);
  }

  public static String lineSeparator() {
    String sep = getProperty("line.separator");
    return (sep != null) ? sep : "\n";
  }

  public static Properties getProperties() {
    return Static.properties;
  }

  private static Properties makeProperties() {
    Properties properties = new Properties();

    for (String p: getNativeProperties()) {
      if (p == null) break;
      int index = p.indexOf('=');
      properties.put(p.substring(0, index), p.substring(index + 1));
    }

    for (String p: getVMProperties()) {
      if (p == null) break;
      int index = p.indexOf('=');
      properties.put(p.substring(0, index), p.substring(index + 1));
    }

    return properties;
  }
  
  private static native String[] getNativeProperties();

  private static native String[] getVMProperties();

  public static native long currentTimeMillis();

  public static native int identityHashCode(Object o);

  public static long nanoTime() {
    return (currentTimeMillis() - NanoTime.BaseInMillis) * 1000000;
  }

  public static String mapLibraryName(String name) {
    if (name != null) {
      return doMapLibraryName(name);
    } else {
      throw new NullPointerException();
    }
  }

  private static native String doMapLibraryName(String name);

  public static void load(String path) {
    ClassLoader.load(path, ClassLoader.getCaller(), false);
  }

  public static void loadLibrary(String name) {
    ClassLoader.load(name, ClassLoader.getCaller(), true);
  }

  public static void gc() {
    Runtime.getRuntime().gc();
  }

  public static void exit(int code) {
    Runtime.getRuntime().exit(code);
  }
  
  public static SecurityManager getSecurityManager() {
    return securityManager;
  }
  
  public static void setSecurityManager(SecurityManager securityManager) {
    System.securityManager = securityManager;
  }

  public static String getenv(String name) throws NullPointerException,
    SecurityException {
    if (getSecurityManager() != null) { // is this allowed?
      getSecurityManager().
        checkPermission(new RuntimePermission("getenv." + name));
    }
    return getenv().get(name);
  }

  public static Map<String, String> getenv() throws SecurityException {
    if (getSecurityManager() != null) { // is this allowed?
      getSecurityManager().checkPermission(new RuntimePermission("getenv.*"));
    }

    if (environment == null) { // build environment table
      String[] vars = getEnvironment();
      environment = new Hashtable<String, String>(vars.length);
      for (String var : vars) { // parse name-value pairs
        int equalsIndex = var.indexOf('=');
        // null names and values are forbidden
        if (equalsIndex != -1 && equalsIndex < var.length() - 1) {
          environment.put(var.substring(0, equalsIndex),
                          var.substring(equalsIndex + 1));
        }
      }
    }

    return environment;
  }

  /** Returns the native process environment. */
  private static native String[] getEnvironment();

  public interface Logger {
    public enum Level {
      ALL(Integer.MIN_VALUE, 0),
      TRACE(400, 1),
      DEBUG(500, 2),
      INFO(800, 3),
      WARNING(900, 4),
      ERROR(1000, 5),
      OFF(Integer.MAX_VALUE, 6);

      private final int severity;
      private final int level;

      Level(int severity, int level) {
        this.severity = severity;
        this.level = level;
      }

      public final String getName() {
        return name();
      }

      public final int getSeverity() {
        return severity;
      }
    }

    String getName();

    boolean isLoggable(Level level);

    default void log(Level level, String msg) {
      log(level, (ResourceBundle) null, msg, (Object[]) null);
    }

    default void log(Level level, Supplier<String> msgSupplier) {
      Objects.requireNonNull(msgSupplier, "msgSupplier");
      if (isLoggable(level)) {
        log(level, (ResourceBundle) null, msgSupplier.get(), (Object[]) null);
      }
    }

    default void log(Level level, Object obj) {
      Objects.requireNonNull(obj, "obj");
      if (isLoggable(level)) {
        log(level, (ResourceBundle) null, Objects.toString(obj), (Object[]) null);
      }
    }

    default void log(Level level, String msg, Throwable thrown) {
      log(level, (ResourceBundle) null, msg, thrown);
    }

    default void log(Level level, Supplier<String> msgSupplier, Throwable thrown) {
      Objects.requireNonNull(msgSupplier, "msgSupplier");
      if (isLoggable(level)) {
        log(level, (ResourceBundle) null, msgSupplier.get(), thrown);
      }
    }

    default void log(Level level, String format, Object... params) {
      log(level, (ResourceBundle) null, format, params);
    }

    void log(Level level, ResourceBundle bundle, String msg, Throwable thrown);

    void log(Level level, ResourceBundle bundle, String format, Object... params);
  }

  public static abstract class LoggerFinder {
    private static final LoggerFinder DEFAULT = new DefaultLoggerFinder();

    protected LoggerFinder() {
    }

    public abstract Logger getLogger(String name, Module module);

    public Logger getLocalizedLogger(String name, ResourceBundle bundle, Module module) {
      return getLogger(name, module);
    }

    public static LoggerFinder getLoggerFinder() {
      return DEFAULT;
    }
  }

  private static final class DefaultLoggerFinder extends LoggerFinder {
    @Override
    public Logger getLogger(String name, Module module) {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(module, "module");
      return new DefaultLogger(name);
    }
  }

  private static final class DefaultLogger implements Logger {
    private final String name;

    DefaultLogger(String name) {
      this.name = Objects.requireNonNull(name, "name");
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public boolean isLoggable(Level level) {
      Objects.requireNonNull(level, "level");
      return level.getSeverity() >= Level.INFO.getSeverity();
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String msg, Throwable thrown) {
      Objects.requireNonNull(level, "level");
      if (!isLoggable(level)) return;
      System.err.println("[" + level.getName() + "] [" + name + "] " + (msg != null ? msg : ""));
      if (thrown != null) {
        thrown.printStackTrace(System.err);
      }
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String format, Object... params) {
      Objects.requireNonNull(level, "level");
      if (!isLoggable(level)) return;
      String formatted;
      if (format != null && params != null && params.length > 0) {
        try {
          formatted = String.format(format, params);
        } catch (Throwable ignored) {
          formatted = format;
        }
      } else {
        formatted = format;
      }
      System.err.println("[" + level.getName() + "] [" + name + "] " + (formatted != null ? formatted : ""));
    }
  }

  public static Logger getLogger(String name) {
    Objects.requireNonNull(name, "name");
    return LoggerFinder.getLoggerFinder().getLogger(name, System.class.getModule());
  }

  public static Logger getLogger(String name, ResourceBundle bundle) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(bundle, "bundle");
    return LoggerFinder.getLoggerFinder().getLocalizedLogger(name, bundle, System.class.getModule());
  }
}
