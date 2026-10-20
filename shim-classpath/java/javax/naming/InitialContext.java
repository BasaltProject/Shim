/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

import java.util.Hashtable;
import javax.naming.spi.InitialContextFactory;

public class InitialContext implements Context {
  protected Hashtable<Object, Object> myProps = null;
  protected Context defaultInitCtx = null;
  protected boolean gotDefault = false;

  private static final String[] kStandardProperties = {
    Context.INITIAL_CONTEXT_FACTORY, Context.OBJECT_FACTORIES, Context.URL_PKG_PREFIXES, Context.STATE_FACTORIES,
    Context.PROVIDER_URL, Context.DNS_URL, Context.AUTHORITATIVE, Context.BATCHSIZE, Context.REFERRAL,
    Context.SECURITY_PROTOCOL, Context.SECURITY_AUTHENTICATION, Context.SECURITY_PRINCIPAL,
    Context.SECURITY_CREDENTIALS, Context.LANGUAGE, Context.APPLET
  };

  protected InitialContext(boolean lazy) throws NamingException {
    if (!lazy) {
      init(null);
    }
  }

  public InitialContext() throws NamingException {
    init(null);
  }

  @SuppressWarnings("unchecked")
  public InitialContext(Hashtable<?, ?> environment) throws NamingException {
    if (environment != null) {
      environment = (Hashtable) environment.clone();
    }
    init(environment);
  }

  @SuppressWarnings("unchecked")
  protected void init(Hashtable<?, ?> environment) throws NamingException {
    myProps = (Hashtable<Object, Object>) (environment == null ? new Hashtable<Object, Object>()
        : environment);
    for (String key : kStandardProperties) {
      if (!myProps.containsKey(key)) {
        String v = System.getProperty(key);
        if (v != null) {
          myProps.put(key, v);
        }
      }
    }
    if (myProps.get(Context.INITIAL_CONTEXT_FACTORY) != null) {
      getDefaultInitCtx();
    }
  }

  protected Context getDefaultInitCtx() throws NamingException {
    if (!gotDefault) {
      defaultInitCtx = getInitialContext(myProps);
      gotDefault = true;
    }
    if (defaultInitCtx == null) {
      throw new NoInitialContextException();
    }
    return defaultInitCtx;
  }

  private static Context getInitialContext(Hashtable<?, ?> env) throws NamingException {
    String className = env == null ? null : (String) env.get(Context.INITIAL_CONTEXT_FACTORY);
    if (className == null) {
      NoInitialContextException ne = new NoInitialContextException("Need to specify class name in environment "
          + "or system property, or in an application resource file: " + Context.INITIAL_CONTEXT_FACTORY);
      throw ne;
    }
    try {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class<?> c = cl != null ? Class.forName(className, true, cl) : Class.forName(className);
      InitialContextFactory factory = (InitialContextFactory) c.getDeclaredConstructor().newInstance();
      return factory.getInitialContext(env);
    } catch (NamingException e) {
      throw e;
    } catch (Exception e) {
      NoInitialContextException ne = new NoInitialContextException("Cannot instantiate class: " + className);
      ne.setRootCause(e);
      throw ne;
    }
  }

  protected Context getURLOrDefaultInitCtx(Name name) throws NamingException {
    return getDefaultInitCtx();
  }

  protected Context getURLOrDefaultInitCtx(String name) throws NamingException {
    return getDefaultInitCtx();
  }

  private static String getURLScheme(String str) {
    int colon = str.indexOf(':');
    int slash = str.indexOf('/');
    if (colon > 0 && (slash == -1 || colon < slash)) {
      return str.substring(0, colon);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <T> T doLookup(Name name) throws NamingException {
    return (T) (new InitialContext()).lookup(name);
  }

  @SuppressWarnings("unchecked")
  public static <T> T doLookup(String name) throws NamingException {
    return (T) (new InitialContext()).lookup(name);
  }

  @Override
  public Object lookup(String name) throws NamingException {
    return getURLOrDefaultInitCtx(name).lookup(name);
  }

  @Override
  public Object lookup(Name name) throws NamingException {
    return getURLOrDefaultInitCtx(name).lookup(name);
  }

  @Override
  public void bind(String name, Object obj) throws NamingException {
    getURLOrDefaultInitCtx(name).bind(name, obj);
  }

  @Override
  public void bind(Name name, Object obj) throws NamingException {
    getURLOrDefaultInitCtx(name).bind(name, obj);
  }

  @Override
  public void rebind(String name, Object obj) throws NamingException {
    getURLOrDefaultInitCtx(name).rebind(name, obj);
  }

  @Override
  public void rebind(Name name, Object obj) throws NamingException {
    getURLOrDefaultInitCtx(name).rebind(name, obj);
  }

  @Override
  public void unbind(String name) throws NamingException {
    getURLOrDefaultInitCtx(name).unbind(name);
  }

  @Override
  public void unbind(Name name) throws NamingException {
    getURLOrDefaultInitCtx(name).unbind(name);
  }

  @Override
  public void rename(String oldName, String newName) throws NamingException {
    getURLOrDefaultInitCtx(oldName).rename(oldName, newName);
  }

  @Override
  public void rename(Name oldName, Name newName) throws NamingException {
    getURLOrDefaultInitCtx(oldName).rename(oldName, newName);
  }

  @Override
  public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
    return getURLOrDefaultInitCtx(name).list(name);
  }

  @Override
  public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
    return getURLOrDefaultInitCtx(name).list(name);
  }

  @Override
  public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
    return getURLOrDefaultInitCtx(name).listBindings(name);
  }

  @Override
  public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
    return getURLOrDefaultInitCtx(name).listBindings(name);
  }

  @Override
  public void destroySubcontext(String name) throws NamingException {
    getURLOrDefaultInitCtx(name).destroySubcontext(name);
  }

  @Override
  public void destroySubcontext(Name name) throws NamingException {
    getURLOrDefaultInitCtx(name).destroySubcontext(name);
  }

  @Override
  public Context createSubcontext(String name) throws NamingException {
    return getURLOrDefaultInitCtx(name).createSubcontext(name);
  }

  @Override
  public Context createSubcontext(Name name) throws NamingException {
    return getURLOrDefaultInitCtx(name).createSubcontext(name);
  }

  @Override
  public Object lookupLink(String name) throws NamingException {
    return getURLOrDefaultInitCtx(name).lookupLink(name);
  }

  @Override
  public Object lookupLink(Name name) throws NamingException {
    return getURLOrDefaultInitCtx(name).lookupLink(name);
  }

  @Override
  public NameParser getNameParser(String name) throws NamingException {
    return getURLOrDefaultInitCtx(name).getNameParser(name);
  }

  @Override
  public NameParser getNameParser(Name name) throws NamingException {
    return getURLOrDefaultInitCtx(name).getNameParser(name);
  }

  @Override
  public String composeName(String name, String prefix) throws NamingException {
    return name;
  }

  @Override
  public Name composeName(Name name, Name prefix) throws NamingException {
    return (Name) name.clone();
  }

  @Override
  public Object addToEnvironment(String propName, Object propVal) throws NamingException {
    myProps.put(propName, propVal);
    return getDefaultInitCtx().addToEnvironment(propName, propVal);
  }

  @Override
  public Object removeFromEnvironment(String propName) throws NamingException {
    myProps.remove(propName);
    return getDefaultInitCtx().removeFromEnvironment(propName);
  }

  @Override
  public Hashtable<?, ?> getEnvironment() throws NamingException {
    return getDefaultInitCtx().getEnvironment();
  }

  @Override
  public void close() throws NamingException {
    myProps = null;
    if (defaultInitCtx != null) {
      defaultInitCtx.close();
      defaultInitCtx = null;
    }
    gotDefault = false;
  }

  @Override
  public String getNameInNamespace() throws NamingException {
    return getDefaultInitCtx().getNameInNamespace();
  }
}
