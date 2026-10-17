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

package java.util.logging;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class Logger {
  private static final Handler[] kEmptyHandlers = new Handler[0];
  private static final int kOffValue = Level.OFF.intValue();

  static final String kSystemLoggerRbName = "sun.util.logging.resources.logging";

  private static final LoggerBundle kSystemBundle = LoggerBundle.get(kSystemLoggerRbName, null);
  private static final LoggerBundle kNoResourceBundle = LoggerBundle.get(null, null);

  private static final class LoggerBundle {
    final String resourceBundleName;
    final ResourceBundle userBundle;

    private LoggerBundle(String resourceBundleName, ResourceBundle bundle) {
      this.resourceBundleName = resourceBundleName;
      this.userBundle = bundle;
    }

    boolean isSystemBundle() {
      return kSystemLoggerRbName.equals(resourceBundleName);
    }

    static LoggerBundle get(String name, ResourceBundle bundle) {
      return new LoggerBundle(name, bundle);
    }
  }

  private static final class ConfigurationData {
    volatile boolean useParentHandlers;
    volatile Filter filter;
    volatile Level levelObject;
    volatile int levelValue;
    volatile CopyOnWriteArrayList<Handler> handlers = new CopyOnWriteArrayList<Handler>();

    ConfigurationData() {
      useParentHandlers = true;
      levelValue = Level.INFO.intValue();
    }

    void setUseParentHandlers(boolean flag) {
      useParentHandlers = flag;
    }

    void setFilter(Filter f) {
      filter = f;
    }

    void setLevelObject(Level l) {
      levelObject = l;
    }

    void setLevelValue(int v) {
      levelValue = v;
    }

    void addHandler(Handler h) {
      handlers.add(h);
    }

    void removeHandler(Handler h) {
      handlers.remove(h);
    }
  }

  private volatile ConfigurationData config;
  private volatile LogManager manager;
  private String name;
  private volatile LoggerBundle loggerBundle = kNoResourceBundle;
  private boolean anonymous;
  private static final Object kTreeLock = new Object();
  private volatile Logger parent;
  private ArrayList<LogManager.LoggerWeakRef> kids;
  private final boolean isSystemLogger;

  public static final String GLOBAL_LOGGER_NAME = "global";

  @Deprecated
  public static final Logger global = new Logger(GLOBAL_LOGGER_NAME);

  public static final Logger getGlobal() {
    LogManager.getLogManager();
    return global;
  }

  protected Logger(String name, String resourceBundleName) {
    this(name, resourceBundleName, null, LogManager.getLogManager(), false);
  }

  Logger(String name, String resourceBundleName, Class<?> caller, LogManager manager, boolean isSystemLogger) {
    this.manager = manager;
    this.isSystemLogger = isSystemLogger;
    this.config = new ConfigurationData();
    this.name = name;
    setupResourceInfo(resourceBundleName, caller);
  }

  final void mergeWithSystemLogger(Logger system) {
    if (system.config.levelObject != null && this.config.levelObject == null) {
      this.config.setLevelObject(system.config.levelObject);
    }
  }

  private Logger(String name) {
    this.name = name;
    this.isSystemLogger = true;
    config = new ConfigurationData();
  }

  void setLogManager(LogManager manager) {
    this.manager = manager;
  }

  private void checkPermission() throws SecurityException {
    if (!anonymous) {
      if (manager == null) {
        manager = LogManager.getLogManager();
      }
      manager.checkPermission();
    }
  }

  private static Logger demandLogger(String name, String resourceBundleName, Class<?> caller) {
    LogManager manager = LogManager.getLogManager();
    return manager.demandLogger(name, resourceBundleName, caller);
  }

  private static Class<?> callerClass() {
    try {
      return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> s.skip(2).findFirst())
          .map(StackWalker.StackFrame::getDeclaringClass).orElse(null);
    } catch (RuntimeException e) {
      return null;
    }
  }

  public static Logger getLogger(String name) {
    return demandLogger(name, null, callerClass());
  }

  public static Logger getLogger(String name, String resourceBundleName) {
    Class<?> callerClass = callerClass();
    Logger result = demandLogger(name, resourceBundleName, callerClass);
    result.setupResourceInfo(resourceBundleName, callerClass);
    return result;
  }

  static Logger getPlatformLogger(String name) {
    LogManager manager = LogManager.getLogManager();
    return manager.demandSystemLogger(name, kSystemLoggerRbName, null);
  }

  public static Logger getAnonymousLogger() {
    return getAnonymousLogger(null);
  }

  public static Logger getAnonymousLogger(String resourceBundleName) {
    LogManager manager = LogManager.getLogManager();
    manager.drainLoggerRefQueueBounded();
    final Logger result = new Logger(null, resourceBundleName, callerClass(), manager, false);
    result.anonymous = true;
    Logger root = manager.getLogger("");
    result.doSetParent(root);
    return result;
  }

  public ResourceBundle getResourceBundle() {
    return findResourceBundle(getResourceBundleName(), true);
  }

  public String getResourceBundleName() {
    return loggerBundle.resourceBundleName;
  }

  public void setFilter(Filter newFilter) throws SecurityException {
    checkPermission();
    config.setFilter(newFilter);
  }

  public Filter getFilter() {
    return config.filter;
  }

  public void log(LogRecord record) {
    if (!isLoggable(record.getLevel())) {
      return;
    }
    Filter theFilter = config.filter;
    if (theFilter != null && !theFilter.isLoggable(record)) {
      return;
    }
    Logger logger = this;
    while (logger != null) {
      final Handler[] loggerHandlers = isSystemLogger ? logger.accessCheckedHandlers() : logger.getHandlers();
      for (Handler handler : loggerHandlers) {
        handler.publish(record);
      }
      final boolean useParentHdls = isSystemLogger ? logger.config.useParentHandlers : logger.getUseParentHandlers();
      if (!useParentHdls) {
        break;
      }
      logger = isSystemLogger ? logger.parent : logger.getParent();
    }
  }

  private void doLog(LogRecord lr) {
    lr.setLoggerName(name);
    final LoggerBundle lb = getEffectiveLoggerBundle();
    final ResourceBundle bundle = lb.userBundle;
    final String ebname = lb.resourceBundleName;
    if (ebname != null && bundle != null) {
      lr.setResourceBundleName(ebname);
      lr.setResourceBundle(bundle);
    }
    log(lr);
  }

  public void log(Level level, String msg) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    doLog(lr);
  }

  public void log(Level level, Supplier<String> msgSupplier) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msgSupplier.get());
    doLog(lr);
  }

  public void log(Level level, String msg, Object param1) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    Object[] params = {param1};
    lr.setParameters(params);
    doLog(lr);
  }

  public void log(Level level, String msg, Object[] params) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    lr.setParameters(params);
    doLog(lr);
  }

  public void log(Level level, String msg, Throwable thrown) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    lr.setThrown(thrown);
    doLog(lr);
  }

  public void log(Level level, Throwable thrown, Supplier<String> msgSupplier) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msgSupplier.get());
    lr.setThrown(thrown);
    doLog(lr);
  }

  public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    lr.setSourceClassName(sourceClass);
    lr.setSourceMethodName(sourceMethod);
    doLog(lr);
  }

  public void logp(Level level, String sourceClass, String sourceMethod, Supplier<String> msgSupplier) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msgSupplier.get());
    lr.setSourceClassName(sourceClass);
    lr.setSourceMethodName(sourceMethod);
    doLog(lr);
  }

  public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object param1) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    lr.setSourceClassName(sourceClass);
    lr.setSourceMethodName(sourceMethod);
    Object[] params = {param1};
    lr.setParameters(params);
    doLog(lr);
  }

  public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object[] params) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    lr.setSourceClassName(sourceClass);
    lr.setSourceMethodName(sourceMethod);
    lr.setParameters(params);
    doLog(lr);
  }

  public void logp(Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    lr.setSourceClassName(sourceClass);
    lr.setSourceMethodName(sourceMethod);
    lr.setThrown(thrown);
    doLog(lr);
  }

  public void logp(Level level, String sourceClass, String sourceMethod, Throwable thrown,
      Supplier<String> msgSupplier) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msgSupplier.get());
    lr.setSourceClassName(sourceClass);
    lr.setSourceMethodName(sourceMethod);
    lr.setThrown(thrown);
    doLog(lr);
  }

  private void doLog(LogRecord lr, String rbname) {
    lr.setLoggerName(name);
    if (rbname != null) {
      lr.setResourceBundleName(rbname);
      lr.setResourceBundle(findResourceBundle(rbname, false));
    }
    log(lr);
  }

  private void doLog(LogRecord lr, ResourceBundle rb) {
    lr.setLoggerName(name);
    if (rb != null) {
      lr.setResourceBundleName(rb.getBaseBundleName());
      lr.setResourceBundle(rb);
    }
    log(lr);
  }

  @Deprecated
  public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    lr.setSourceClassName(sourceClass);
    lr.setSourceMethodName(sourceMethod);
    doLog(lr, bundleName);
  }

  @Deprecated
  public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg,
      Object param1) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    lr.setSourceClassName(sourceClass);
    lr.setSourceMethodName(sourceMethod);
    Object[] params = {param1};
    lr.setParameters(params);
    doLog(lr, bundleName);
  }

  @Deprecated
  public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg,
      Object[] params) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    lr.setSourceClassName(sourceClass);
    lr.setSourceMethodName(sourceMethod);
    lr.setParameters(params);
    doLog(lr, bundleName);
  }

  public void logrb(Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg,
      Object... params) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    lr.setSourceClassName(sourceClass);
    lr.setSourceMethodName(sourceMethod);
    if (params != null && params.length != 0) {
      lr.setParameters(params);
    }
    doLog(lr, bundle);
  }

  public void logrb(Level level, ResourceBundle bundle, String msg, Object... params) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    if (params != null && params.length != 0) {
      lr.setParameters(params);
    }
    doLog(lr, bundle);
  }

  @Deprecated
  public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg,
      Throwable thrown) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    lr.setSourceClassName(sourceClass);
    lr.setSourceMethodName(sourceMethod);
    lr.setThrown(thrown);
    doLog(lr, bundleName);
  }

  public void logrb(Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg,
      Throwable thrown) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    lr.setSourceClassName(sourceClass);
    lr.setSourceMethodName(sourceMethod);
    lr.setThrown(thrown);
    doLog(lr, bundle);
  }

  public void logrb(Level level, ResourceBundle bundle, String msg, Throwable thrown) {
    if (!isLoggable(level)) {
      return;
    }
    LogRecord lr = new LogRecord(level, msg);
    lr.setThrown(thrown);
    doLog(lr, bundle);
  }

  public void entering(String sourceClass, String sourceMethod) {
    logp(Level.FINER, sourceClass, sourceMethod, "ENTRY");
  }

  public void entering(String sourceClass, String sourceMethod, Object param1) {
    logp(Level.FINER, sourceClass, sourceMethod, "ENTRY {0}", param1);
  }

  public void entering(String sourceClass, String sourceMethod, Object[] params) {
    String msg = "ENTRY";
    if (params == null) {
      logp(Level.FINER, sourceClass, sourceMethod, msg);
      return;
    }
    if (!isLoggable(Level.FINER)) {
      return;
    }
    if (params.length > 0) {
      final StringBuilder b = new StringBuilder(msg);
      for (int i = 0; i < params.length; i++) {
        b.append(' ').append('{').append(i).append('}');
      }
      msg = b.toString();
    }
    logp(Level.FINER, sourceClass, sourceMethod, msg, params);
  }

  public void exiting(String sourceClass, String sourceMethod) {
    logp(Level.FINER, sourceClass, sourceMethod, "RETURN");
  }

  public void exiting(String sourceClass, String sourceMethod, Object result) {
    logp(Level.FINER, sourceClass, sourceMethod, "RETURN {0}", result);
  }

  public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
    if (!isLoggable(Level.FINER)) {
      return;
    }
    LogRecord lr = new LogRecord(Level.FINER, "THROW");
    lr.setSourceClassName(sourceClass);
    lr.setSourceMethodName(sourceMethod);
    lr.setThrown(thrown);
    doLog(lr);
  }

  public void severe(String msg) {
    log(Level.SEVERE, msg);
  }

  public void warning(String msg) {
    log(Level.WARNING, msg);
  }

  public void info(String msg) {
    log(Level.INFO, msg);
  }

  public void config(String msg) {
    log(Level.CONFIG, msg);
  }

  public void fine(String msg) {
    log(Level.FINE, msg);
  }

  public void finer(String msg) {
    log(Level.FINER, msg);
  }

  public void finest(String msg) {
    log(Level.FINEST, msg);
  }

  public void severe(Supplier<String> msgSupplier) {
    log(Level.SEVERE, msgSupplier);
  }

  public void warning(Supplier<String> msgSupplier) {
    log(Level.WARNING, msgSupplier);
  }

  public void info(Supplier<String> msgSupplier) {
    log(Level.INFO, msgSupplier);
  }

  public void config(Supplier<String> msgSupplier) {
    log(Level.CONFIG, msgSupplier);
  }

  public void fine(Supplier<String> msgSupplier) {
    log(Level.FINE, msgSupplier);
  }

  public void finer(Supplier<String> msgSupplier) {
    log(Level.FINER, msgSupplier);
  }

  public void finest(Supplier<String> msgSupplier) {
    log(Level.FINEST, msgSupplier);
  }

  public void setLevel(Level newLevel) throws SecurityException {
    checkPermission();
    synchronized (kTreeLock) {
      config.setLevelObject(newLevel);
      updateEffectiveLevel();
    }
  }

  final boolean isLevelInitialized() {
    return config.levelObject != null;
  }

  public Level getLevel() {
    return config.levelObject;
  }

  public boolean isLoggable(Level level) {
    int levelValue = config.levelValue;
    if (level.intValue() < levelValue || levelValue == kOffValue) {
      return false;
    }
    return true;
  }

  public String getName() {
    return name;
  }

  public void addHandler(Handler handler) throws SecurityException {
    Objects.requireNonNull(handler);
    checkPermission();
    config.addHandler(handler);
  }

  public void removeHandler(Handler handler) throws SecurityException {
    checkPermission();
    if (handler == null) {
      return;
    }
    config.removeHandler(handler);
  }

  public Handler[] getHandlers() {
    return accessCheckedHandlers();
  }

  Handler[] accessCheckedHandlers() {
    return config.handlers.toArray(kEmptyHandlers);
  }

  public void setUseParentHandlers(boolean useParentHandlers) {
    checkPermission();
    config.setUseParentHandlers(useParentHandlers);
  }

  public boolean getUseParentHandlers() {
    return config.useParentHandlers;
  }

  private ResourceBundle catalog() {
    return loggerBundle.userBundle;
  }

  private static ClassLoader loaderOf(Class<?> caller) {
    if (caller != null && caller.getClassLoader() != null) {
      return caller.getClassLoader();
    }
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return cl != null ? cl : ClassLoader.getSystemClassLoader();
  }

  private synchronized ResourceBundle findResourceBundle(String name, boolean useCallersClassLoader) {
    if (name == null) {
      return null;
    }
    Locale currentLocale = Locale.getDefault();
    final LoggerBundle lb = loggerBundle;
    if (lb.userBundle != null && name.equals(lb.resourceBundleName)) {
      return lb.userBundle;
    }
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    if (cl == null) {
      cl = ClassLoader.getSystemClassLoader();
    }
    try {
      return ResourceBundle.getBundle(name, currentLocale, cl);
    } catch (MissingResourceException ex) {
      // fall through to the caller class loader
    }
    if (useCallersClassLoader && callerLoader != null) {
      try {
        return ResourceBundle.getBundle(name, currentLocale, callerLoader);
      } catch (MissingResourceException ex) {
        // not found either
      }
    }
    return null;
  }

  private ClassLoader callerLoader;

  private synchronized void setupResourceInfo(String name, Class<?> caller) {
    final LoggerBundle lb = loggerBundle;
    if (lb.resourceBundleName != null) {
      if (lb.resourceBundleName.equals(name)) {
        return;
      } else if (name != null) {
        throw new IllegalArgumentException(lb.resourceBundleName + " != " + name);
      }
    }
    if (name == null) {
      return;
    }
    callerLoader = loaderOf(caller);
    ResourceBundle bundle = findResourceBundle(name, true);
    if (bundle == null) {
      throw new MissingResourceException("Can't find " + name + " bundle from ", name, "");
    }
    this.loggerBundle = LoggerBundle.get(name, bundle);
  }

  public void setResourceBundle(ResourceBundle bundle) {
    checkPermission();
    final String baseName = bundle.getBaseBundleName();
    if (baseName == null || baseName.isEmpty()) {
      throw new IllegalArgumentException("resource bundle must have a name");
    }
    synchronized (this) {
      LoggerBundle lb = loggerBundle;
      final boolean canReplaceResourceBundle = lb.resourceBundleName == null || lb.resourceBundleName.equals(baseName);
      if (!canReplaceResourceBundle) {
        throw new IllegalArgumentException("can't replace resource bundle");
      }
      loggerBundle = LoggerBundle.get(baseName, bundle);
    }
  }

  public Logger getParent() {
    return parent;
  }

  public void setParent(Logger parent) {
    if (parent == null) {
      throw new NullPointerException();
    }
    if (manager == null) {
      manager = LogManager.getLogManager();
    }
    manager.checkPermission();
    doSetParent(parent);
  }

  private void doSetParent(Logger newParent) {
    synchronized (kTreeLock) {
      LogManager.LoggerWeakRef ref = null;
      if (parent != null) {
        for (Iterator<LogManager.LoggerWeakRef> iter = parent.kids.iterator(); iter.hasNext();) {
          ref = iter.next();
          Logger kid = ref.get();
          if (kid == this) {
            iter.remove();
            break;
          } else {
            ref = null;
          }
        }
      }
      parent = newParent;
      if (parent.kids == null) {
        parent.kids = new ArrayList<LogManager.LoggerWeakRef>(2);
      }
      if (ref == null) {
        ref = manager.new LoggerWeakRef(this);
      }
      ref.setParentRef(new WeakReference<Logger>(parent));
      parent.kids.add(ref);
      updateEffectiveLevel();
    }
  }

  final void removeChildLogger(LogManager.LoggerWeakRef child) {
    synchronized (kTreeLock) {
      for (Iterator<LogManager.LoggerWeakRef> iter = kids.iterator(); iter.hasNext();) {
        LogManager.LoggerWeakRef ref = iter.next();
        if (ref == child) {
          iter.remove();
          return;
        }
      }
    }
  }

  private void updateEffectiveLevel() {
    int newLevelValue;
    final ConfigurationData cfg = config;
    final Level levelObject = cfg.levelObject;
    if (levelObject != null) {
      newLevelValue = levelObject.intValue();
    } else {
      if (parent != null) {
        newLevelValue = parent.config.levelValue;
      } else {
        newLevelValue = Level.INFO.intValue();
      }
    }
    if (cfg.levelValue == newLevelValue) {
      return;
    }
    cfg.setLevelValue(newLevelValue);
    if (kids != null) {
      for (int i = 0; i < kids.size(); i++) {
        LogManager.LoggerWeakRef ref = kids.get(i);
        Logger kid = ref.get();
        if (kid != null) {
          kid.updateEffectiveLevel();
        }
      }
    }
  }

  private LoggerBundle getEffectiveLoggerBundle() {
    LoggerBundle lb = loggerBundle;
    if (lb.isSystemBundle()) {
      return kSystemBundle;
    }
    ResourceBundle b = getResourceBundle();
    if (b != null && b == lb.userBundle) {
      return lb;
    } else if (b != null) {
      String rbName = getResourceBundleName();
      return LoggerBundle.get(rbName, b);
    } else {
      Logger target = parent;
      while (target != null) {
        final LoggerBundle trb = target.loggerBundle;
        if (trb.isSystemBundle()) {
          return kSystemBundle;
        }
        if (trb.userBundle != null) {
          return trb;
        }
        final String rbn = isSystemLogger ? trb.resourceBundleName : target.getResourceBundleName();
        if (rbn != null) {
          ResourceBundle rb = target.findResourceBundle(rbn, true);
          if (rb != null) {
            return LoggerBundle.get(rbn, rb);
          }
        }
        target = isSystemLogger ? target.parent : target.getParent();
      }
    }
    return kNoResourceBundle;
  }
}
