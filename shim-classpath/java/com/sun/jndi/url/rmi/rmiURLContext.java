/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.url.rmi;

import com.sun.jndi.rmi.registry.RegistryContextFactory;
import java.util.Hashtable;
import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;

final class rmiURLContext implements Context {
  private final Hashtable<Object, Object> environment;

  @SuppressWarnings("unchecked")
  rmiURLContext(Hashtable<?, ?> env) {
    this.environment = env == null ? new Hashtable<Object, Object>() : (Hashtable<Object, Object>) env.clone();
  }

  private interface Op<T> {
    T apply(Context ctx, String name) throws NamingException;
  }

  private <T> T resolve(String url, Op<T> op) throws NamingException {
    if (!url.regionMatches(true, 0, "rmi:", 0, 4)) {
      throw new InvalidNameException("Not an rmi URL: " + url);
    }
    String rest = url.substring(4);
    String hostPort = "";
    String name = "";
    if (rest.startsWith("//")) {
      rest = rest.substring(2);
      int slash = rest.indexOf('/');
      hostPort = slash < 0 ? rest : rest.substring(0, slash);
      name = slash < 0 ? "" : rest.substring(slash + 1);
    } else {
      name = rest.startsWith("/") ? rest.substring(1) : rest;
    }
    Hashtable<Object, Object> env = new Hashtable<Object, Object>(environment);
    env.put(Context.PROVIDER_URL, "rmi://" + hostPort);
    Context ctx = new RegistryContextFactory().getInitialContext(env);
    try {
      return op.apply(ctx, java.net.URLDecoder.decode(name.replace("+", "%2B"), java.nio.charset.StandardCharsets.UTF_8));
    } finally {
      ctx.close();
    }
  }

  @Override
  public Object lookup(String name) throws NamingException {
    return resolve(name, (c, n) -> c.lookup(new CompositeName().add(n)));
  }

  @Override
  public Object lookup(Name name) throws NamingException {
    return lookup(name.get(0));
  }

  @Override
  public void bind(String name, Object obj) throws NamingException {
    resolve(name, (c, n) -> {
      c.bind(new CompositeName().add(n), obj);
      return null;
    });
  }

  @Override
  public void bind(Name name, Object obj) throws NamingException {
    bind(name.get(0), obj);
  }

  @Override
  public void rebind(String name, Object obj) throws NamingException {
    resolve(name, (c, n) -> {
      c.rebind(new CompositeName().add(n), obj);
      return null;
    });
  }

  @Override
  public void rebind(Name name, Object obj) throws NamingException {
    rebind(name.get(0), obj);
  }

  @Override
  public void unbind(String name) throws NamingException {
    resolve(name, (c, n) -> {
      c.unbind(new CompositeName().add(n));
      return null;
    });
  }

  @Override
  public void unbind(Name name) throws NamingException {
    unbind(name.get(0));
  }

  @Override
  public void rename(String oldName, String newName) throws NamingException {
    throw new OperationNotSupportedException();
  }

  @Override
  public void rename(Name oldName, Name newName) throws NamingException {
    throw new OperationNotSupportedException();
  }

  @Override
  public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
    return resolve(name, (c, n) -> c.list(n));
  }

  @Override
  public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
    return list(name.get(0));
  }

  @Override
  public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
    return resolve(name, (c, n) -> c.listBindings(n));
  }

  @Override
  public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
    return listBindings(name.get(0));
  }

  @Override
  public void destroySubcontext(String name) throws NamingException {
    throw new OperationNotSupportedException();
  }

  @Override
  public void destroySubcontext(Name name) throws NamingException {
    throw new OperationNotSupportedException();
  }

  @Override
  public Context createSubcontext(String name) throws NamingException {
    throw new OperationNotSupportedException();
  }

  @Override
  public Context createSubcontext(Name name) throws NamingException {
    throw new OperationNotSupportedException();
  }

  @Override
  public Object lookupLink(String name) throws NamingException {
    return lookup(name);
  }

  @Override
  public Object lookupLink(Name name) throws NamingException {
    return lookup(name);
  }

  @Override
  public NameParser getNameParser(String name) throws NamingException {
    return n -> new CompositeName(n);
  }

  @Override
  public NameParser getNameParser(Name name) throws NamingException {
    return getNameParser("");
  }

  @Override
  public String composeName(String name, String prefix) throws NamingException {
    return prefix.isEmpty() ? name : prefix + "/" + name;
  }

  @Override
  public Name composeName(Name name, Name prefix) throws NamingException {
    Name r = (Name) prefix.clone();
    r.addAll(name);
    return r;
  }

  @Override
  public Object addToEnvironment(String propName, Object propVal) throws NamingException {
    return environment.put(propName, propVal);
  }

  @Override
  public Object removeFromEnvironment(String propName) throws NamingException {
    return environment.remove(propName);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Hashtable<?, ?> getEnvironment() throws NamingException {
    return (Hashtable<?, ?>) environment.clone();
  }

  @Override
  public void close() throws NamingException {
  }

  @Override
  public String getNameInNamespace() throws NamingException {
    return "";
  }
}
