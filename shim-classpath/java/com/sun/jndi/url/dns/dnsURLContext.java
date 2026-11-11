/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.url.dns;

import com.sun.jndi.dns.DnsContextFactory;
import java.util.Hashtable;
import javax.naming.Binding;
import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

final class dnsURLContext implements DirContext {
  private final Hashtable<Object, Object> environment;

  @SuppressWarnings("unchecked")
  dnsURLContext(Hashtable<?, ?> env) {
    this.environment = env == null ? new Hashtable<Object, Object>() : (Hashtable<Object, Object>) env.clone();
  }

  private interface Op<T> {
    T apply(DirContext ctx, String name) throws NamingException;
  }

  private <T> T withContext(String url, Op<T> op) throws NamingException {
    if (!url.regionMatches(true, 0, "dns:", 0, 4)) {
      throw new ConfigurationException(url + " is not a valid DNS pseudo-URL");
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
      name = rest;
    }
    Hashtable<Object, Object> env = new Hashtable<Object, Object>(environment);
    Context ctx = new DnsContextFactory().getInitialContext(withProvider(env, hostPort));
    try {
      return op.apply((DirContext) ctx, java.net.URLDecoder.decode(name.replace("+", "%2B"),
          java.nio.charset.StandardCharsets.UTF_8));
    } finally {
      ctx.close();
    }
  }

  private static Hashtable<Object, Object> withProvider(Hashtable<Object, Object> env, String hostPort) {
    env.put(Context.PROVIDER_URL, hostPort.isEmpty() ? "dns:" : "dns://" + hostPort);
    return env;
  }

  @Override
  public Object lookup(Name name) throws NamingException {
    return withContext(name.toString(), (ctx, n) -> {
      return ctx.lookup(n);
    });
  }

  @Override
  public Object lookup(String name) throws NamingException {
    return withContext(name, (ctx, n) -> {
      return ctx.lookup(n);
    });
  }

  @Override
  public void bind(Name name, Object obj) throws NamingException {
    withContext(name.toString(), (ctx, n) -> {
      ctx.bind(n, obj); return null;
    });
  }

  @Override
  public void bind(String name, Object obj) throws NamingException {
    withContext(name, (ctx, n) -> {
      ctx.bind(n, obj); return null;
    });
  }

  @Override
  public void rebind(Name name, Object obj) throws NamingException {
    withContext(name.toString(), (ctx, n) -> {
      ctx.rebind(n, obj); return null;
    });
  }

  @Override
  public void rebind(String name, Object obj) throws NamingException {
    withContext(name, (ctx, n) -> {
      ctx.rebind(n, obj); return null;
    });
  }

  @Override
  public void unbind(Name name) throws NamingException {
    withContext(name.toString(), (ctx, n) -> {
      ctx.unbind(n); return null;
    });
  }

  @Override
  public void unbind(String name) throws NamingException {
    withContext(name, (ctx, n) -> {
      ctx.unbind(n); return null;
    });
  }

  @Override
  public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
    return withContext(name.toString(), (ctx, n) -> {
      return ctx.list(n);
    });
  }

  @Override
  public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
    return withContext(name, (ctx, n) -> {
      return ctx.list(n);
    });
  }

  @Override
  public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
    return withContext(name.toString(), (ctx, n) -> {
      return ctx.listBindings(n);
    });
  }

  @Override
  public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
    return withContext(name, (ctx, n) -> {
      return ctx.listBindings(n);
    });
  }

  @Override
  public void destroySubcontext(Name name) throws NamingException {
    withContext(name.toString(), (ctx, n) -> {
      ctx.destroySubcontext(n); return null;
    });
  }

  @Override
  public void destroySubcontext(String name) throws NamingException {
    withContext(name, (ctx, n) -> {
      ctx.destroySubcontext(n); return null;
    });
  }

  @Override
  public Context createSubcontext(Name name) throws NamingException {
    return withContext(name.toString(), (ctx, n) -> {
      return ctx.createSubcontext(n);
    });
  }

  @Override
  public Context createSubcontext(String name) throws NamingException {
    return withContext(name, (ctx, n) -> {
      return ctx.createSubcontext(n);
    });
  }

  @Override
  public Object lookupLink(Name name) throws NamingException {
    return withContext(name.toString(), (ctx, n) -> {
      return ctx.lookupLink(n);
    });
  }

  @Override
  public Object lookupLink(String name) throws NamingException {
    return withContext(name, (ctx, n) -> {
      return ctx.lookupLink(n);
    });
  }

  @Override
  public void bind(String string, Object object, Attributes attributes) throws NamingException {
    withContext(string, (ctx, n) -> {
      ctx.bind(n, object, attributes); return null;
    });
  }

  @Override
  public void bind(Name name, Object object, Attributes attributes) throws NamingException {
    withContext(name.toString(), (ctx, n) -> {
      ctx.bind(n, object, attributes); return null;
    });
  }

  @Override
  public DirContext createSubcontext(String string, Attributes attributes) throws NamingException {
    return withContext(string, (ctx, n) -> {
      return ctx.createSubcontext(n, attributes);
    });
  }

  @Override
  public DirContext createSubcontext(Name name, Attributes attributes) throws NamingException {
    return withContext(name.toString(), (ctx, n) -> {
      return ctx.createSubcontext(n, attributes);
    });
  }

  @Override
  public Attributes getAttributes(String string, String[] strings) throws NamingException {
    return withContext(string, (ctx, n) -> {
      return ctx.getAttributes(n, strings);
    });
  }

  @Override
  public Attributes getAttributes(String string) throws NamingException {
    return withContext(string, (ctx, n) -> {
      return ctx.getAttributes(n);
    });
  }

  @Override
  public Attributes getAttributes(Name name, String[] strings) throws NamingException {
    return withContext(name.toString(), (ctx, n) -> {
      return ctx.getAttributes(n, strings);
    });
  }

  @Override
  public Attributes getAttributes(Name name) throws NamingException {
    return withContext(name.toString(), (ctx, n) -> {
      return ctx.getAttributes(n);
    });
  }

  @Override
  public DirContext getSchema(String string) throws NamingException {
    return withContext(string, (ctx, n) -> {
      return ctx.getSchema(n);
    });
  }

  @Override
  public DirContext getSchema(Name name) throws NamingException {
    return withContext(name.toString(), (ctx, n) -> {
      return ctx.getSchema(n);
    });
  }

  @Override
  public DirContext getSchemaClassDefinition(String string) throws NamingException {
    return withContext(string, (ctx, n) -> {
      return ctx.getSchemaClassDefinition(n);
    });
  }

  @Override
  public DirContext getSchemaClassDefinition(Name name) throws NamingException {
    return withContext(name.toString(), (ctx, n) -> {
      return ctx.getSchemaClassDefinition(n);
    });
  }

  @Override
  public void modifyAttributes(String string, ModificationItem[] modificationItems) throws NamingException {
    withContext(string, (ctx, n) -> {
      ctx.modifyAttributes(n, modificationItems); return null;
    });
  }

  @Override
  public void modifyAttributes(String string, int i, Attributes attributes) throws NamingException {
    withContext(string, (ctx, n) -> {
      ctx.modifyAttributes(n, i, attributes); return null;
    });
  }

  @Override
  public void modifyAttributes(Name name, ModificationItem[] modificationItems) throws NamingException {
    withContext(name.toString(), (ctx, n) -> {
      ctx.modifyAttributes(n, modificationItems); return null;
    });
  }

  @Override
  public void modifyAttributes(Name name, int i, Attributes attributes) throws NamingException {
    withContext(name.toString(), (ctx, n) -> {
      ctx.modifyAttributes(n, i, attributes); return null;
    });
  }

  @Override
  public void rebind(String string, Object object, Attributes attributes) throws NamingException {
    withContext(string, (ctx, n) -> {
      ctx.rebind(n, object, attributes); return null;
    });
  }

  @Override
  public void rebind(Name name, Object object, Attributes attributes) throws NamingException {
    withContext(name.toString(), (ctx, n) -> {
      ctx.rebind(n, object, attributes); return null;
    });
  }

  @Override
  public NamingEnumeration<SearchResult> search(String string, String string2, Object[] objects, SearchControls searchControls) throws NamingException {
    return withContext(string, (ctx, n) -> {
      return ctx.search(n, string2, objects, searchControls);
    });
  }

  @Override
  public NamingEnumeration<SearchResult> search(String string, String string2, SearchControls searchControls) throws NamingException {
    return withContext(string, (ctx, n) -> {
      return ctx.search(n, string2, searchControls);
    });
  }

  @Override
  public NamingEnumeration<SearchResult> search(String string, Attributes attributes, String[] strings) throws NamingException {
    return withContext(string, (ctx, n) -> {
      return ctx.search(n, attributes, strings);
    });
  }

  @Override
  public NamingEnumeration<SearchResult> search(String string, Attributes attributes) throws NamingException {
    return withContext(string, (ctx, n) -> {
      return ctx.search(n, attributes);
    });
  }

  @Override
  public NamingEnumeration<SearchResult> search(Name name, String string, Object[] objects, SearchControls searchControls) throws NamingException {
    return withContext(name.toString(), (ctx, n) -> {
      return ctx.search(n, string, objects, searchControls);
    });
  }

  @Override
  public NamingEnumeration<SearchResult> search(Name name, String string, SearchControls searchControls) throws NamingException {
    return withContext(name.toString(), (ctx, n) -> {
      return ctx.search(n, string, searchControls);
    });
  }

  @Override
  public NamingEnumeration<SearchResult> search(Name name, Attributes attributes, String[] strings) throws NamingException {
    return withContext(name.toString(), (ctx, n) -> {
      return ctx.search(n, attributes, strings);
    });
  }

  @Override
  public NamingEnumeration<SearchResult> search(Name name, Attributes attributes) throws NamingException {
    return withContext(name.toString(), (ctx, n) -> {
      return ctx.search(n, attributes);
    });
  }

  @Override
  public void rename(Name oldName, Name newName) throws NamingException {
    throw new OperationNotSupportedException();
  }

  @Override
  public void rename(String oldName, String newName) throws NamingException {
    throw new OperationNotSupportedException();
  }

  @Override
  public NameParser getNameParser(Name name) throws NamingException {
    return withContext(name.toString(), (c, n) -> c.getNameParser(n));
  }

  @Override
  public NameParser getNameParser(String name) throws NamingException {
    return withContext(name, (c, n) -> c.getNameParser(n));
  }

  @Override
  public Name composeName(Name name, Name prefix) throws NamingException {
    return (Name) name.clone();
  }

  @Override
  public String composeName(String name, String prefix) throws NamingException {
    return name;
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
  public Hashtable<?, ?> getEnvironment() throws NamingException {
    return (Hashtable<?, ?>) environment.clone();
  }

  @Override
  public void close() throws NamingException {
  }

  @Override
  public String getNameInNamespace() throws NamingException {
    throw new OperationNotSupportedException();
  }
}
