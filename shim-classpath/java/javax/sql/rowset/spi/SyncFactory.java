/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset.spi;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Logger;

public class SyncFactory {
  public static final String ROWSET_SYNC_PROVIDER = "rowset.provider.classname";
  public static final String ROWSET_SYNC_VENDOR = "rowset.provider.vendor";
  public static final String ROWSET_SYNC_PROVIDER_VERSION = "rowset.provider.version";

  private static final String DEFAULT_PROVIDER = "com.sun.rowset.providers.RIOptimisticProvider";
  private static final String XML_PROVIDER = "com.sun.rowset.providers.RIXMLProvider";

  private static final Object lock = new Object();
  private static Hashtable<String, SyncProvider> providers;
  private static Logger rsLogger;
  private static javax.naming.Context ic;
  private static volatile boolean loggerSet;

  private static SyncFactory syncFactory = new SyncFactory();

  private SyncFactory() {
  }

  public static SyncFactory getSyncFactory() {
    return syncFactory;
  }

  public static synchronized void registerProvider(String providerID) throws SyncFactoryException {
    ProviderImpl impl = new ProviderImpl();
    impl.setClassname(providerID);
    initMapIfNecessary();
    providers.put(providerID, impl.get());
  }

  public static synchronized void unregisterProvider(String providerID) throws SyncFactoryException {
    initMapIfNecessary();
    if (providers.containsKey(providerID)) {
      providers.remove(providerID);
    }
  }

  private static void initMapIfNecessary() throws SyncFactoryException {
    if (providers != null) {
      return;
    }
    providers = new Hashtable<String, SyncProvider>();
    SyncProvider a = newInstance(DEFAULT_PROVIDER);
    providers.put(DEFAULT_PROVIDER, a);
    providers.put(XML_PROVIDER, newInstance(XML_PROVIDER));
    String prop = System.getProperty(ROWSET_SYNC_PROVIDER);
    if (prop != null) {
      for (String name : prop.split(":")) {
        name = name.trim();
        if (name.length() > 0 && !providers.containsKey(name)) {
          try {
            providers.put(name, newInstance(name));
          } catch (SyncFactoryException e) {
          }
        }
      }
    }
  }

  private static SyncProvider newInstance(String className) throws SyncFactoryException {
    try {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class<?> c = cl != null ? Class.forName(className, true, cl) : Class.forName(className);
      return (SyncProvider) c.getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      throw new SyncFactoryException("Unable to instantiate SyncProvider: " + className);
    }
  }

  public static SyncProvider getInstance(String providerID) throws SyncFactoryException {
    if (providerID == null) {
      throw new SyncFactoryException("The providerID cannot be null");
    }
    synchronized (lock) {
      initMapIfNecessary();
      SyncProvider p = providers.get(providerID);
      if (p != null) {
        return p;
      }
      return providers.get(DEFAULT_PROVIDER);
    }
  }

  public static Enumeration<SyncProvider> getRegisteredProviders() throws SyncFactoryException {
    synchronized (lock) {
      initMapIfNecessary();
      return providers.elements();
    }
  }

  public static void setLogger(Logger logger) {
    if (logger == null) {
      throw new NullPointerException("You must provide a Logger");
    }
    rsLogger = logger;
  }

  public static void setLogger(Logger logger, java.util.logging.Level level) {
    if (logger == null) {
      throw new NullPointerException("You must provide a Logger");
    }
    logger.setLevel(level);
    rsLogger = logger;
  }

  public static Logger getLogger() throws SyncFactoryException {
    if (rsLogger == null) {
      throw new SyncFactoryException("(SyncFactory) : No logger has been set");
    }
    return rsLogger;
  }

  public static synchronized void setJNDIContext(javax.naming.Context ctx) throws SyncFactoryException {
    if (ctx == null) {
      throw new SyncFactoryException("Invalid JNDI context supplied");
    }
    ic = ctx;
  }

  private static final class ProviderImpl {
    private String className;

    void setClassname(String classname) {
      className = classname;
    }

    SyncProvider get() throws SyncFactoryException {
      return newInstance(className);
    }
  }
}
