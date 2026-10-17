/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.logging;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LogManager {
  private volatile Properties props = new Properties();
  private static final Level kDefaultLevel = Level.INFO;

  private final Map<String, LoggerWeakRef> namedLoggers = new HashMap<String, LoggerWeakRef>();
  private final LogNode root = new LogNode(null);
  private volatile Logger rootLogger;
  private volatile boolean readPrimordialConfiguration;

  private static final int kStateInitialized = 0;
  private static final int kStateInitializing = 1;
  private static final int kStateReadingConfig = 2;
  private static final int kStateUninitialized = 3;
  private static final int kStateShutdown = 4;
  private volatile int globalHandlersState = kStateUninitialized;

  private final ReentrantLock configurationLock = new ReentrantLock();
  private final CopyOnWriteArrayList<CloseOnReset> closeOnResetLoggers = new CopyOnWriteArrayList<CloseOnReset>();
  private final Map<Object, Runnable> listeners = Collections.synchronizedMap(new IdentityHashMap<Object, Runnable>());

  private static volatile LogManager manager;
  private boolean initializedCalled;
  private volatile boolean initializationDone;
  private final ReferenceQueue<Logger> loggerRefQueue = new ReferenceQueue<Logger>();
  private static final int kMaxIterations = 400;

  public static final String LOGGING_MXBEAN_NAME = "java.util.logging:type=Logging";

  private static final String kDefaultConfiguration = "handlers= java.util.logging.ConsoleHandler\n"
      + ".level= INFO\n"
      + "java.util.logging.FileHandler.pattern = %h/java%u.log\n"
      + "java.util.logging.FileHandler.limit = 50000\n"
      + "java.util.logging.FileHandler.count = 1\n"
      + "java.util.logging.FileHandler.maxLocks = 100\n"
      + "java.util.logging.FileHandler.formatter = java.util.logging.XMLFormatter\n"
      + "java.util.logging.ConsoleHandler.level = INFO\n"
      + "java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter\n";

  private static LogManager initLogManager() {
    String cname = System.getProperty("java.util.logging.manager");
    if (cname != null) {
      try {
        Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(cname);
        @SuppressWarnings("deprecation")
        Object o = clz.newInstance();
        return (LogManager) o;
      } catch (Exception ex) {
        System.err.println("Could not load Logmanager \"" + cname + "\"");
        ex.printStackTrace();
      }
    }
    return new LogManager();
  }

  private static synchronized LogManager instance() {
    if (manager == null) {
      manager = initLogManager();
    }
    return manager;
  }

  protected LogManager() {
    this(checkSubclassPermissions());
  }

  private LogManager(Void checked) {
    try {
      Runtime.getRuntime().addShutdownHook(new Thread("Logging-Cleaner") {
        @Override
        public void run() {
          LogManager mgr = manager;
          if (mgr != null) {
            mgr.shutdown();
          }
        }
      });
    } catch (IllegalStateException e) {
      // the VM is already shutting down
    }
  }

  private static Void checkSubclassPermissions() {
    return null;
  }

  private void shutdown() {
    configurationLock.lock();
    try {
      globalHandlersState = kStateShutdown;
    } finally {
      configurationLock.unlock();
    }
    reset();
  }

  final void ensureLogManagerInitialized() {
    final LogManager owner = this;
    if (initializationDone || owner != manager) {
      return;
    }
    boolean isRecursiveInitialization = false;
    synchronized (this) {
      isRecursiveInitialization = initializedCalled;
      initializedCalled = true;
    }
    if (isRecursiveInitialization) {
      return;
    }
    try {
      owner.rootLogger = owner.new RootLogger();
      owner.addLogger(owner.rootLogger);
      if (!owner.rootLogger.isLevelInitialized()) {
        owner.rootLogger.setLevel(kDefaultLevel);
      }
      @SuppressWarnings("deprecation")
      final Logger global = Logger.global;
      owner.addLogger(global);
    } finally {
      initializationDone = true;
    }
  }

  public static LogManager getLogManager() {
    LogManager mgr = instance();
    mgr.ensureLogManagerInitialized();
    return mgr;
  }

  private void readPrimordialConfiguration() {
    if (!readPrimordialConfiguration) {
      synchronized (this) {
        if (!readPrimordialConfiguration) {
          if (System.out == null) {
            return;
          }
          readPrimordialConfiguration = true;
          try {
            readConfiguration();
          } catch (Exception ex) {
            assert false : "Exception raised while reading logging configuration: " + ex;
          }
        }
      }
    }
  }

  private final class RootLogger extends Logger {
    private RootLogger() {
      super("", null, null, LogManager.this, true);
    }

    @Override
    public void log(LogRecord record) {
      initializeGlobalHandlers();
      super.log(record);
    }

    @Override
    public void addHandler(Handler h) {
      initializeGlobalHandlers();
      super.addHandler(h);
    }

    @Override
    public void removeHandler(Handler h) {
      initializeGlobalHandlers();
      super.removeHandler(h);
    }

    @Override
    Handler[] accessCheckedHandlers() {
      initializeGlobalHandlers();
      return super.accessCheckedHandlers();
    }
  }

  Logger demandLogger(String name, String resourceBundleName, Class<?> caller) {
    Logger result = getLogger(name);
    if (result == null) {
      Logger newLogger = new Logger(name, resourceBundleName, caller, this, false);
      do {
        if (addLogger(newLogger)) {
          return newLogger;
        }
        result = getLogger(name);
      } while (result == null);
    }
    return result;
  }

  Logger demandSystemLogger(String name, String resourceBundleName, Class<?> caller) {
    return demandLogger(name, resourceBundleName, caller);
  }

  private void loadLoggerHandlers(final Logger logger, final String name, final String handlersPropertyName) {
    if (!(logger instanceof Logger) || handlersPropertyName == null) {
      return;
    }
    if (getProperty(handlersPropertyName) == null) {
      return;
    }
    configurationLock.lock();
    try {
      setLoggerHandlers(logger, name, handlersPropertyName, createLoggerHandlers(name, handlersPropertyName));
    } finally {
      configurationLock.unlock();
    }
  }

  private void setLoggerHandlers(final Logger logger, final String name, final String handlersPropertyName,
      List<Handler> handlers) {
    final boolean ensureCloseOnReset = !handlers.isEmpty() && getBooleanProperty(handlersPropertyName + ".ensureCloseOnReset", true);
    int count = 0;
    for (Handler hdl : handlers) {
      logger.addHandler(hdl);
      if (++count == 1 && ensureCloseOnReset) {
        closeOnResetLoggers.addIfAbsent(CloseOnReset.create(logger));
      }
    }
  }

  private List<Handler> createLoggerHandlers(final String name, final String handlersPropertyName) {
    String[] names = parseClassNames(handlersPropertyName);
    List<Handler> handlers = new ArrayList<Handler>(names.length);
    for (String type : names) {
      try {
        Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(type);
        @SuppressWarnings("deprecation")
        Object o = clz.newInstance();
        Handler hdl = (Handler) o;
        String levs = getProperty(type + ".level");
        if (levs != null) {
          Level l = Level.findLevel(levs);
          if (l != null) {
            hdl.setLevel(l);
          } else {
            System.err.println("Can't set level for " + type);
          }
        }
        handlers.add(hdl);
      } catch (Exception ex) {
        System.err.println("Can't load log handler \"" + type + "\"");
        System.err.println("" + ex);
        ex.printStackTrace();
      }
    }
    return handlers;
  }

  private static final class CloseOnReset {
    private final Logger logger;

    private CloseOnReset(Logger ref) {
      this.logger = Objects.requireNonNull(ref);
    }

    @Override
    public boolean equals(Object other) {
      return (other instanceof CloseOnReset) && ((CloseOnReset) other).logger == logger;
    }

    @Override
    public int hashCode() {
      return System.identityHashCode(logger);
    }

    public Logger get() {
      return logger;
    }

    public static CloseOnReset create(Logger logger) {
      return new CloseOnReset(logger);
    }
  }

  final class LoggerWeakRef extends WeakReference<Logger> {
    private String name;
    private LogNode node;
    private WeakReference<Logger> parentRef;
    private boolean disposed = false;

    LoggerWeakRef(Logger logger) {
      super(logger, loggerRefQueue);
      name = logger.getName();
    }

    void dispose() {
      synchronized (this) {
        if (disposed) {
          return;
        }
        disposed = true;
      }
      final LogNode n = node;
      if (n != null) {
        synchronized (LogManager.this) {
          n.context_remove(this);
        }
        n.loggerRef = null;
        if (n.children == null || n.children.isEmpty()) {
          n.removeFromParent();
        }
        node = null;
      }
      if (parentRef != null) {
        Logger parent = parentRef.get();
        if (parent != null) {
          parent.removeChildLogger(this);
        }
        parentRef = null;
      }
    }

    void setNode(LogNode node) {
      this.node = node;
    }

    void setParentRef(WeakReference<Logger> parentRef) {
      this.parentRef = parentRef;
    }
  }

  final void drainLoggerRefQueueBounded() {
    for (int i = 0; i < kMaxIterations; i++) {
      if (loggerRefQueue == null) {
        break;
      }
      LoggerWeakRef ref = (LoggerWeakRef) loggerRefQueue.poll();
      if (ref == null) {
        break;
      }
      if (namedLoggers.get(ref.name) == ref) {
        synchronized (this) {
          namedLoggers.remove(ref.name);
        }
      }
      ref.dispose();
    }
  }

  private synchronized void ensureDefaultLoggers(Logger caller) {
    if (rootLogger == null) {
      ensureLogManagerInitialized();
    }
  }

  public boolean addLogger(Logger logger) {
    final String name = logger.getName();
    if (name == null) {
      throw new NullPointerException();
    }
    drainLoggerRefQueueBounded();
    if (addLocalLogger(logger)) {
      loadLoggerHandlers(logger, name, name + ".handlers");
      return true;
    }
    return false;
  }

  private boolean addLocalLogger(Logger logger) {
    final String name = logger.getName();
    if (name == null) {
      throw new NullPointerException();
    }
    synchronized (this) {
      LoggerWeakRef ref = namedLoggers.get(name);
      if (ref != null) {
        if (ref.refersTo(null)) {
          ref.dispose();
          namedLoggers.remove(name);
        } else {
          return false;
        }
      }
      logger.setLogManager(this);
      ref = new LoggerWeakRef(logger);
      Level level = getLevelProperty(name + ".level", null);
      if (level != null && !logger.isLevelInitialized()) {
        logger.setLevel(level);
      }
      processParentHandlers(logger, name, new HashSet<String>());
      LogNode node = getNode(name);
      node.loggerRef = ref;
      Logger parent = null;
      LogNode nodep = node.parent;
      while (nodep != null) {
        LoggerWeakRef nodeRef = nodep.loggerRef;
        if (nodeRef != null) {
          parent = nodeRef.get();
          if (parent != null) {
            break;
          }
        }
        nodep = nodep.parent;
      }
      if (parent != null) {
        logger.setParent(parent);
      }
      node.walkAndSetParent(logger);
      ref.setNode(node);
      namedLoggers.put(name, ref);
      return true;
    }
  }

  private void processParentHandlers(final Logger logger, final String name, Set<String> visited) {
    if (logger != rootLogger) {
      boolean useParent = getBooleanProperty(name + ".useParentHandlers", true);
      if (!useParent) {
        logger.setUseParentHandlers(false);
      }
    }
    int ix = 1;
    for (;;) {
      int ix2 = name.indexOf('.', ix);
      if (ix2 < 0) {
        break;
      }
      String pname = name.substring(0, ix2);
      if (getProperty(pname + ".level") != null || getProperty(pname + ".handlers") != null) {
        if (visited.add(pname)) {
          demandLogger(pname, null, null);
        }
      }
      ix = ix2 + 1;
    }
  }

  private LogNode getNode(String name) {
    if (name == null || name.isEmpty()) {
      return root;
    }
    LogNode node = root;
    while (name.length() > 0) {
      int ix = name.indexOf('.');
      String head;
      if (ix > 0) {
        head = name.substring(0, ix);
        name = name.substring(ix + 1);
      } else {
        head = name;
        name = "";
      }
      if (node.children == null) {
        node.children = new HashMap<String, LogNode>();
      }
      LogNode child = node.children.get(head);
      if (child == null) {
        child = new LogNode(node);
        node.children.put(head, child);
      }
      node = child;
    }
    return node;
  }

  public synchronized Logger getLogger(String name) {
    ensureLogManagerInitialized();
    LoggerWeakRef ref = namedLoggers.get(name);
    if (ref == null) {
      return null;
    }
    Logger logger = ref.get();
    if (logger == null) {
      ref.dispose();
      namedLoggers.remove(name);
    }
    return logger;
  }

  public synchronized Enumeration<String> getLoggerNames() {
    ensureLogManagerInitialized();
    return Collections.enumeration(new ArrayList<String>(namedLoggers.keySet()));
  }

  public void readConfiguration() throws IOException, SecurityException {
    checkPermission();
    String cname = System.getProperty("java.util.logging.config.class");
    if (cname != null) {
      try {
        try {
          Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(cname);
          @SuppressWarnings("deprecation")
          Object o = clz.newInstance();
          return;
        } catch (ClassNotFoundException ex) {
          Class<?> clz = Thread.currentThread().getContextClassLoader().loadClass(cname);
          @SuppressWarnings("deprecation")
          Object o = clz.newInstance();
          return;
        }
      } catch (Exception ex) {
        System.err.println("Logging configuration class \"" + cname + "\" failed");
        System.err.println("" + ex);
      }
    }
    String fname = getConfigurationFileName();
    File file = fname == null ? null : new File(fname);
    if (file != null && file.isFile()) {
      try (final InputStream in = new FileInputStream(file)) {
        final BufferedInputStream bin = new BufferedInputStream(in);
        readConfiguration(bin);
      }
    } else if (System.getProperty("java.util.logging.config.file") != null) {
      throw new FileNotFoundException(fname + " (No such file or directory)");
    } else {
      readConfiguration(new ByteArrayInputStream(kDefaultConfiguration.getBytes(StandardCharsets.ISO_8859_1)));
    }
  }

  String getConfigurationFileName() throws IOException {
    String fname = System.getProperty("java.util.logging.config.file");
    if (fname == null) {
      fname = System.getProperty("java.home");
      if (fname == null) {
        return null;
      }
      fname = Paths.get(fname, "conf", "logging.properties").toAbsolutePath().normalize().toString();
    }
    return fname;
  }

  public void reset() throws SecurityException {
    checkPermission();
    List<CloseOnReset> persistent;
    configurationLock.lock();
    try {
      props = new Properties();
      persistent = new ArrayList<CloseOnReset>(closeOnResetLoggers);
      closeOnResetLoggers.clear();
      if (globalHandlersState != kStateShutdown && globalHandlersState != kStateReadingConfig) {
        globalHandlersState = kStateInitialized;
      }
    } finally {
      configurationLock.unlock();
    }
    List<Logger> all = new ArrayList<Logger>();
    synchronized (this) {
      for (LoggerWeakRef ref : namedLoggers.values()) {
        Logger l = ref.get();
        if (l != null) {
          all.add(l);
        }
      }
    }
    for (Logger l : all) {
      resetLogger(l);
    }
    persistent.clear();
  }

  private void closeHandlers(Logger logger) {
    Handler[] targets = logger.getHandlers();
    for (Handler h : targets) {
      logger.removeHandler(h);
      try {
        h.close();
      } catch (Exception ex) {
        // problems closing a handler: keep going
      }
    }
  }

  private void resetLogger(Logger logger) {
    closeHandlers(logger);
    String name = logger.getName();
    if (name != null && name.isEmpty()) {
      logger.setLevel(kDefaultLevel);
    } else {
      logger.setLevel(null);
    }
  }

  private String[] parseClassNames(String propertyName) {
    String hands = getProperty(propertyName);
    if (hands == null) {
      return new String[0];
    }
    hands = hands.trim();
    int ix = 0;
    final List<String> result = new ArrayList<String>();
    while (ix < hands.length()) {
      int end = ix;
      while (end < hands.length()) {
        if (Character.isWhitespace(hands.charAt(end))) {
          break;
        }
        if (hands.charAt(end) == ',') {
          break;
        }
        end++;
      }
      String word = hands.substring(ix, end);
      ix = end + 1;
      word = word.trim();
      if (word.length() == 0) {
        continue;
      }
      result.add(word);
    }
    return result.toArray(new String[result.size()]);
  }

  public void readConfiguration(InputStream ins) throws IOException, SecurityException {
    checkPermission();
    configurationLock.lock();
    try {
      if (globalHandlersState == kStateShutdown) {
        return;
      }
      final int oldState = globalHandlersState;
      globalHandlersState = kStateReadingConfig;
      try {
        reset();
        Properties newProps = new Properties();
        newProps.load(ins);
        props = newProps;
      } finally {
        globalHandlersState = kStateUninitialized;
      }
    } finally {
      configurationLock.unlock();
    }
    setLevelsOnExistingLoggers();
    invokeConfigurationListeners();
  }

  private static String trim(String value) {
    return value == null ? null : value.trim();
  }

  public void updateConfiguration(Function<String, BiFunction<String, String, String>> mapper) throws IOException {
    checkPermission();
    ensureLogManagerInitialized();
    drainLoggerRefQueueBounded();
    String fname = getConfigurationFileName();
    File file = fname == null ? null : new File(fname);
    if (file != null && file.isFile()) {
      try (final InputStream in = new FileInputStream(file)) {
        updateConfiguration(new BufferedInputStream(in), mapper);
      }
    } else {
      updateConfiguration(new ByteArrayInputStream(kDefaultConfiguration.getBytes(StandardCharsets.ISO_8859_1)),
          mapper);
    }
  }

  public void updateConfiguration(InputStream ins, Function<String, BiFunction<String, String, String>> mapper)
      throws IOException {
    checkPermission();
    ensureLogManagerInitialized();
    drainLoggerRefQueueBounded();
    final Properties tempProps = new Properties();
    tempProps.load(ins);
    final Function<String, BiFunction<String, String, String>> map = mapper == null
        ? k -> ((o, n) -> n) : mapper;
    configurationLock.lock();
    try {
      if (globalHandlersState == kStateShutdown) {
        return;
      }
      final Properties previous = props;
      final Properties next = new Properties();
      Set<String> keys = new TreeSet<String>();
      keys.addAll(previous.stringPropertyNames());
      keys.addAll(tempProps.stringPropertyNames());
      for (String key : keys) {
        BiFunction<String, String, String> remap = map.apply(key);
        if (remap == null) {
          remap = (o, n) -> n;
        }
        String oldValue = previous.getProperty(key);
        String newValue = tempProps.getProperty(key);
        String value = remap.apply(trim(oldValue), trim(newValue));
        if (value != null) {
          next.put(key, value);
        }
      }
      final Set<String> changedLevelKeys = new TreeSet<String>();
      final Set<String> changedHandlerKeys = new TreeSet<String>();
      for (String key : keys) {
        if (!Objects.equals(previous.getProperty(key), next.getProperty(key))) {
          if (key.endsWith(".level")) {
            changedLevelKeys.add(key);
          }
          if (key.equals("handlers") || key.endsWith(".handlers")) {
            changedHandlerKeys.add(key);
          }
        }
      }
      props = next;
      for (String key : changedLevelKeys) {
        String ln = key.equals(".level") ? "" : key.substring(0, key.length() - ".level".length());
        Logger l = getLogger(ln);
        if (l != null) {
          String v = next.getProperty(key);
          Level lv = v == null ? null : Level.findLevel(v.trim());
          if (v != null && lv == null) {
            System.err.println("Bad level value for property: " + key);
            continue;
          }
          l.setLevel(lv);
        }
      }
      for (String key : changedHandlerKeys) {
        String ln = key.equals("handlers") ? "" : key.substring(0, key.length() - ".handlers".length());
        Logger l = getLogger(ln);
        if (l != null && (!ln.isEmpty() || globalHandlersState == kStateInitialized)) {
          closeHandlers(l);
          closeOnResetLoggers.remove(CloseOnReset.create(l));
          setLoggerHandlers(l, ln, key, createLoggerHandlers(ln, key));
        }
      }
    } finally {
      configurationLock.unlock();
    }
    invokeConfigurationListeners();
  }

  public String getProperty(String name) {
    return props.getProperty(name);
  }

  String getStringProperty(String name, String defaultValue) {
    String val = getProperty(name);
    if (val == null) {
      return defaultValue;
    }
    return val.trim();
  }

  int getIntProperty(String name, int defaultValue) {
    String val = getProperty(name);
    if (val == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(val.trim());
    } catch (Exception ex) {
      return defaultValue;
    }
  }

  long getLongProperty(String name, long defaultValue) {
    String val = getProperty(name);
    if (val == null) {
      return defaultValue;
    }
    try {
      return Long.parseLong(val.trim());
    } catch (Exception ex) {
      return defaultValue;
    }
  }

  boolean getBooleanProperty(String name, boolean defaultValue) {
    String val = getProperty(name);
    if (val == null) {
      return defaultValue;
    }
    val = val.toLowerCase();
    if (val.equals("true") || val.equals("1")) {
      return true;
    } else if (val.equals("false") || val.equals("0")) {
      return false;
    }
    return defaultValue;
  }

  Level getLevelProperty(String name, Level defaultValue) {
    String val = getProperty(name);
    if (val == null) {
      return defaultValue;
    }
    Level l = Level.findLevel(val.trim());
    return l != null ? l : defaultValue;
  }

  Filter getFilterProperty(String name, Filter defaultValue) {
    String val = getProperty(name);
    try {
      if (val != null) {
        Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(val);
        @SuppressWarnings("deprecation")
        Object o = clz.newInstance();
        return (Filter) o;
      }
    } catch (Exception ex) {
      // drop through
    }
    return defaultValue;
  }

  Formatter getFormatterProperty(String name, Formatter defaultValue) {
    String val = getProperty(name);
    try {
      if (val != null) {
        Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(val);
        @SuppressWarnings("deprecation")
        Object o = clz.newInstance();
        return (Formatter) o;
      }
    } catch (Exception ex) {
      // drop through
    }
    return defaultValue;
  }

  private void initializeGlobalHandlers() {
    int state = globalHandlersState;
    if (state == kStateInitialized || state == kStateShutdown) {
      return;
    }
    configurationLock.lock();
    try {
      if (globalHandlersState != kStateUninitialized && globalHandlersState != kStateInitializing) {
        if (globalHandlersState == kStateInitialized || globalHandlersState == kStateShutdown) {
          return;
        }
      }
      if (globalHandlersState == kStateUninitialized) {
        readPrimordialConfiguration();
        if (globalHandlersState != kStateUninitialized) {
          if (globalHandlersState == kStateInitialized) {
            return;
          }
        }
        globalHandlersState = kStateInitializing;
        try {
          loadLoggerHandlers(rootLogger, null, "handlers");
        } finally {
          globalHandlersState = kStateInitialized;
        }
      }
    } finally {
      configurationLock.unlock();
    }
  }

  void checkPermission() {
  }

  public void checkAccess() throws SecurityException {
    checkPermission();
  }

  private void setLevelsOnExistingLoggers() {
    Properties p = props;
    Enumeration<?> enumNames = p.propertyNames();
    while (enumNames.hasMoreElements()) {
      String key = (String) enumNames.nextElement();
      if (!key.endsWith(".level")) {
        continue;
      }
      int ix = key.length() - 6;
      String name = key.substring(0, ix);
      Level level = getLevelProperty(key, null);
      if (level == null) {
        System.err.println("Bad level value for property: " + key);
        continue;
      }
      Logger l = getLogger(name);
      if (l == null) {
        continue;
      }
      l.setLevel(level);
    }
  }

  public static synchronized LoggingMXBean getLoggingMXBean() {
    return Logging.getInstance();
  }

  public LogManager addConfigurationListener(Runnable listener) {
    final Runnable r = Objects.requireNonNull(listener);
    checkPermission();
    listeners.put(r, r);
    return this;
  }

  public void removeConfigurationListener(Runnable listener) {
    final Runnable key = Objects.requireNonNull(listener);
    checkPermission();
    listeners.remove(key);
  }

  private void invokeConfigurationListeners() {
    Runnable[] returnedListeners;
    synchronized (listeners) {
      returnedListeners = listeners.values().toArray(new Runnable[0]);
    }
    for (Runnable r : returnedListeners) {
      r.run();
    }
  }

  private static final class LogNode {
    HashMap<String, LogNode> children;
    LoggerWeakRef loggerRef;
    LogNode parent;

    LogNode(LogNode parent) {
      this.parent = parent;
    }

    void context_remove(LoggerWeakRef ref) {
      // the named logger table is kept by the manager
    }

    void walkAndSetParent(Logger newParent) {
      if (children == null) {
        return;
      }
      for (LogNode node : children.values()) {
        LoggerWeakRef ref = node.loggerRef;
        Logger logger = ref == null ? null : ref.get();
        if (logger == null) {
          node.walkAndSetParent(newParent);
        } else {
          logger.setParent(newParent);
        }
      }
    }

    void removeFromParent() {
      if (parent != null && parent.children != null) {
        parent.children.values().remove(this);
      }
    }
  }
}
