/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.spi;

import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.naming.CannotProceedException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.Reference;
import javax.naming.Referenceable;

public class NamingManager {
  public static final String CPE = "java.naming.spi.CannotProceedException";

  private static ObjectFactoryBuilder object_factory_builder = null;
  private static InitialContextFactoryBuilder initctx_factory_builder = null;

  NamingManager() {
  }

  public static synchronized void setObjectFactoryBuilder(ObjectFactoryBuilder builder) throws NamingException {
    if (object_factory_builder != null) {
      throw new IllegalStateException("ObjectFactoryBuilder already set");
    }
    object_factory_builder = builder;
  }

  static synchronized ObjectFactoryBuilder getObjectFactoryBuilder() {
    return object_factory_builder;
  }

  static Class<?> loadClass(String className) throws ClassNotFoundException {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    if (cl != null) {
      try {
        return Class.forName(className, true, cl);
      } catch (ClassNotFoundException e) {
      }
    }
    return Class.forName(className);
  }

  static ObjectFactory getObjectFactoryFromReference(Reference ref) {
    String className = ref.getFactoryClassName();
    if (className == null) {
      return null;
    }
    try {
      return (ObjectFactory) loadClass(className).getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      return null;
    }
  }

  public static Object getObjectInstance(Object refInfo, Name name, Context nameCtx, Hashtable<?, ?> environment)
      throws Exception {
    ObjectFactory factory;
    ObjectFactoryBuilder builder = getObjectFactoryBuilder();
    if (builder != null) {
      factory = builder.createObjectFactory(refInfo, environment);
      return factory.getObjectInstance(refInfo, name, nameCtx, environment);
    }
    Reference ref = null;
    if (refInfo instanceof Reference) {
      ref = (Reference) refInfo;
    } else if (refInfo instanceof Referenceable) {
      ref = ((Referenceable) (refInfo)).getReference();
    }
    Object answer;
    if (ref != null) {
      String f = ref.getFactoryClassName();
      if (f != null) {
        factory = getObjectFactoryFromReference(ref);
        if (factory != null) {
          return factory.getObjectInstance(ref, name, nameCtx, environment);
        }
        return refInfo;
      }
      answer = processURLAddrs(ref, name, nameCtx, environment);
      if (answer != null) {
        return answer;
      }
    }
    answer = createObjectFromFactories(refInfo, name, nameCtx, environment);
    return (answer != null) ? answer : refInfo;
  }

  private static Object processURLAddrs(Reference ref, Name name, Context nameCtx, Hashtable<?, ?> environment)
      throws NamingException {
    for (int i = 0; i < ref.size(); i++) {
      javax.naming.RefAddr addr = ref.get(i);
      if (addr instanceof javax.naming.StringRefAddr && addr.getType().equalsIgnoreCase("URL")) {
        String url = (String) addr.getContent();
        Object answer = processURL(url, name, nameCtx, environment);
        if (answer != null) {
          return answer;
        }
      }
    }
    return null;
  }

  private static Object processURL(Object refInfo, Name name, Context nameCtx, Hashtable<?, ?> environment)
      throws NamingException {
    if (refInfo instanceof String) {
      String url = (String) refInfo;
      int colon = url.indexOf(':');
      if (colon > 0) {
        Context ctx = getURLContext(url.substring(0, colon), environment);
        if (ctx != null) {
          return ctx.lookup(url);
        }
      }
    }
    return null;
  }

  private static String[] factoryList(Hashtable<?, ?> environment, String key) {
    String list = environment == null ? null : (String) environment.get(key);
    if (list == null) {
      list = System.getProperty(key);
    }
    if (list == null) {
      return new String[0];
    }
    java.util.List<String> out = new java.util.ArrayList<String>();
    StringTokenizer parser = new StringTokenizer(list, ":");
    while (parser.hasMoreTokens()) {
      out.add(parser.nextToken());
    }
    return out.toArray(new String[0]);
  }

  private static Object createObjectFromFactories(Object obj, Name name, Context nameCtx,
      Hashtable<?, ?> environment) throws Exception {
    for (String fname : factoryList(environment, Context.OBJECT_FACTORIES)) {
      try {
        ObjectFactory factory = (ObjectFactory) loadClass(fname).getDeclaredConstructor().newInstance();
        Object answer = factory.getObjectInstance(obj, name, nameCtx, environment);
        if (answer != null) {
          return answer;
        }
      } catch (ClassNotFoundException e) {
      } catch (ReflectiveOperationException e) {
      }
    }
    return null;
  }

  public static Context getURLContext(String scheme, Hashtable<?, ?> environment) throws NamingException {
    String[] prefixes = factoryList(environment, Context.URL_PKG_PREFIXES);
    java.util.List<String> all = new java.util.ArrayList<String>(java.util.Arrays.asList(prefixes));
    all.add("com.sun.jndi.url");
    for (String prefix : all) {
      String cname = prefix + "." + scheme + "." + scheme + "URLContextFactory";
      try {
        ObjectFactory factory = (ObjectFactory) loadClass(cname).getDeclaredConstructor().newInstance();
        Object ctx = factory.getObjectInstance(null, null, null, environment);
        if (ctx instanceof Context) {
          return (Context) ctx;
        }
      } catch (NamingException e) {
        throw e;
      } catch (Exception e) {
      }
    }
    return null;
  }

  public static synchronized void setInitialContextFactoryBuilder(InitialContextFactoryBuilder builder)
      throws NamingException {
    if (initctx_factory_builder != null) {
      throw new IllegalStateException("InitialContextFactoryBuilder already set");
    }
    initctx_factory_builder = builder;
  }

  public static synchronized boolean hasInitialContextFactoryBuilder() {
    return initctx_factory_builder != null;
  }

  public static Context getInitialContext(Hashtable<?, ?> env) throws NamingException {
    InitialContextFactory factory;
    InitialContextFactoryBuilder builder;
    synchronized (NamingManager.class) {
      builder = initctx_factory_builder;
    }
    if (builder == null) {
      String className = env != null ? (String) env.get(Context.INITIAL_CONTEXT_FACTORY) : null;
      if (className == null) {
        NoInitialContextException ne = new NoInitialContextException(
            "Need to specify class name in environment or system property, or in an application resource file: "
                + Context.INITIAL_CONTEXT_FACTORY);
        throw ne;
      }
      try {
        factory = (InitialContextFactory) loadClass(className).getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        NoInitialContextException ne = new NoInitialContextException(
            "Cannot instantiate class: " + className);
        ne.setRootCause(e);
        throw ne;
      }
    } else {
      factory = builder.createInitialContextFactory(env);
    }
    return factory.getInitialContext(env);
  }

  public static Context getContinuationContext(CannotProceedException cpe) throws NamingException {
    Hashtable<?, ?> env = cpe.getEnvironment();
    if (env == null) {
      env = new Hashtable<Object, Object>(7);
    } else {
      env = (Hashtable<?, ?>) env.clone();
    }
    @SuppressWarnings("unchecked")
    Hashtable<Object, Object> e2 = (Hashtable<Object, Object>) env;
    e2.put(CPE, cpe);
    Object resolved = cpe.getResolvedObj();
    try {
      Object obj = getObjectInstance(resolved, cpe.getAltName(), cpe.getAltNameCtx(), e2);
      if (obj instanceof Context) {
        return (Context) obj;
      }
    } catch (NamingException e) {
      throw e;
    } catch (Exception e) {
      NamingException ne = new NamingException("Cannot continue");
      ne.setRootCause(e);
      throw ne;
    }
    throw cpe;
  }

  public static Object getStateToBind(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
      throws NamingException {
    for (String fname : factoryList(environment, Context.STATE_FACTORIES)) {
      try {
        StateFactory factory = (StateFactory) loadClass(fname).getDeclaredConstructor().newInstance();
        Object answer = factory.getStateToBind(obj, name, nameCtx, environment);
        if (answer != null) {
          return answer;
        }
      } catch (NamingException e) {
        throw e;
      } catch (ReflectiveOperationException e) {
      }
    }
    return obj;
  }
}
