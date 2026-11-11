/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.dns;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import javax.naming.Binding;
import javax.naming.CommunicationException;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.ServiceUnavailableException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InvalidAttributeIdentifierException;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public final class DnsContext implements DirContext {
  private final DnsName domain;
  private final List<InetSocketAddress> servers;
  private final Hashtable<Object, Object> env;

  DnsContext(String domain, List<InetSocketAddress> servers, Hashtable<Object, Object> env)
      throws NamingException {
    this.domain = new DnsName(domain);
    this.servers = servers;
    this.env = env;
  }

  private DnsContext(DnsName domain, DnsContext parent) {
    this.domain = domain;
    this.servers = parent.servers;
    this.env = parent.env;
  }

  private String text(Name n) {
    return n.toString();
  }

  private List<String> fullLabels(String name) throws NamingException {
    if (name.endsWith(".") && !name.isEmpty() && !name.equals(".") && !domain.isEmpty()) {
      InvalidNameException e = new InvalidNameException("DNS name " + name + " not relative to " + domainText());
      e.setRemainingName(new CompositeName().add(name));
      e.setResolvedName(new CompositeName());
      throw e;
    }
    DnsName rel = new DnsName(name);
    List<String> labels = rel.wireLabels();
    labels.addAll(domain.wireLabels());
    return labels;
  }

  private String domainText() {
    return domain.isEmpty() ? "." : domain + ".";
  }

  private int intEnv(String key, int def) {
    Object v = env.get(key);
    return v == null ? def : Integer.parseInt(v.toString());
  }

  private DnsResolver.Response ask(String name, int type) throws NamingException {
    List<String> labels = fullLabels(name);
    DnsResolver.Response r;
    try {
      boolean recurse = !"false".equalsIgnoreCase(String.valueOf(env.get("com.sun.jndi.dns.recursion")));
      r = DnsResolver.query(servers, labels, type, recurse, intEnv("com.sun.jndi.dns.timeout.initial", 1000),
          intEnv("com.sun.jndi.dns.timeout.retries", 4));
    } catch (IOException e) {
      CommunicationException ce = new CommunicationException("DNS error");
      ce.setRootCause(e);
      throw ce;
    }
    NamingException failure = null;
    String suffix = " [response code " + r.rcode + "]";
    switch (r.rcode) {
      case 0:
        return r;
      case 1:
        failure = new NamingException("DNS error" + suffix);
        break;
      case 2:
        failure = new ServiceUnavailableException("DNS server failure" + suffix);
        break;
      case 3:
        failure = new NameNotFoundException("DNS name not found" + suffix);
        break;
      case 4:
        failure = new OperationNotSupportedException("DNS operation not supported" + suffix);
        break;
      case 5:
        failure = new ServiceUnavailableException("DNS service refused" + suffix);
        break;
      default:
        failure = new NamingException("DNS error" + suffix);
    }
    failure.setRemainingName(name.isEmpty() ? new CompositeName() : new CompositeName().add(name));
    if (!name.isEmpty()) {
      failure.setResolvedName(new CompositeName());
    }
    throw failure;
  }

  @Override
  public Attributes getAttributes(String name, String[] attrIds) throws NamingException {
    if (attrIds != null && attrIds.length == 0) {
      return new BasicAttributes(true);
    }
    int queryType = 255;
    List<Integer> wanted = null;
    if (attrIds != null) {
      wanted = new ArrayList<Integer>();
      for (String id : attrIds) {
        String t = id.trim();
        String[] parts = t.split("\\s+");
        if (parts.length == 2) {
          if (!parts[0].equalsIgnoreCase("IN") && !parts[0].equalsIgnoreCase("ANY") && !parts[0].equals("*")) {
            throw new InvalidAttributeIdentifierException("Unknown resource record class '" + parts[0] + "'");
          }
          t = parts[1];
        }
        int type = DnsResolver.typeOf(t);
        if (type < 0) {
          throw new InvalidAttributeIdentifierException("Unknown resource record type '" + t + "'");
        }
        wanted.add(type);
      }
      if (wanted.size() == 1 && wanted.get(0) != 255) {
        queryType = wanted.get(0);
      } else if (wanted.contains(255)) {
        wanted = null;
      }
    }
    DnsResolver.Response r = ask(name, queryType);
    BasicAttributes out = new BasicAttributes(true);
    for (DnsResolver.Record rec : r.answers) {
      if (rec.rrClass != 1 || (wanted != null && !wanted.contains(rec.type))) {
        continue;
      }
      String id = DnsResolver.typeName(rec.type);
      Attribute a = out.get(id);
      if (a == null) {
        a = new BasicAttribute(id);
        out.put(a);
      }
      a.add(rec.data);
    }
    return out;
  }

  @Override
  public Attributes getAttributes(String name) throws NamingException {
    return getAttributes(name, null);
  }

  @Override
  public Attributes getAttributes(Name name, String[] attrIds) throws NamingException {
    return getAttributes(text(name), attrIds);
  }

  @Override
  public Attributes getAttributes(Name name) throws NamingException {
    return getAttributes(text(name), null);
  }

  @Override
  public Object lookup(String name) throws NamingException {
    if (name.isEmpty()) {
      return new DnsContext(domain, this);
    }
    Object attr = env.get("com.sun.jndi.dns.lookup.attr");
    int type = attr == null ? 16 : DnsResolver.typeOf(attr.toString());
    ask(name, type < 0 ? 16 : type);
    DnsName child = new DnsName(name);
    child.addAll(domain);
    return new DnsContext(child, this);
  }

  @Override
  public Object lookup(Name name) throws NamingException {
    return lookup(text(name));
  }

  private static OperationNotSupportedException unsupported() {
    return new OperationNotSupportedException();
  }

  @Override
  public void bind(Name name, Object obj) throws NamingException {
    throw unsupported();
  }

  @Override
  public void bind(String name, Object obj) throws NamingException {
    throw unsupported();
  }

  @Override
  public void rebind(Name name, Object obj) throws NamingException {
    throw unsupported();
  }

  @Override
  public void rebind(String name, Object obj) throws NamingException {
    throw unsupported();
  }

  @Override
  public void unbind(Name name) throws NamingException {
    throw unsupported();
  }

  @Override
  public void unbind(String name) throws NamingException {
    throw unsupported();
  }

  @Override
  public void rename(Name oldName, Name newName) throws NamingException {
    throw unsupported();
  }

  @Override
  public void rename(String oldName, String newName) throws NamingException {
    throw unsupported();
  }

  @Override
  public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
    return list(text(name));
  }

  @Override
  public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
    final List<String> names = zoneNames(name);
    final int[] i = {0};
    return new NamingEnumeration<NameClassPair>() {
      @Override
      public NameClassPair next() throws NamingException {
        return new NameClassPair(names.get(i[0]++), DnsContext.class.getName(), true);
      }

      @Override
      public boolean hasMore() throws NamingException {
        return i[0] < names.size();
      }

      @Override
      public void close() throws NamingException {
        i[0] = names.size();
      }

      @Override
      public boolean hasMoreElements() {
        return i[0] < names.size();
      }

      @Override
      public NameClassPair nextElement() {
        return new NameClassPair(names.get(i[0]++), DnsContext.class.getName(), true);
      }
    };
  }

  @Override
  public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
    return listBindings(text(name));
  }

  @Override
  public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
    final List<String> names = zoneNames(name);
    final int[] i = {0};
    return new NamingEnumeration<Binding>() {
      private Binding make(String n) throws NamingException {
        return new Binding(n, DnsContext.class.getName(), lookup(n), true);
      }

      @Override
      public Binding next() throws NamingException {
        return make(names.get(i[0]++));
      }

      @Override
      public boolean hasMore() throws NamingException {
        return i[0] < names.size();
      }

      @Override
      public void close() throws NamingException {
        i[0] = names.size();
      }

      @Override
      public boolean hasMoreElements() {
        return i[0] < names.size();
      }

      @Override
      public Binding nextElement() {
        try {
          return make(names.get(i[0]++));
        } catch (NamingException e) {
          throw new java.util.NoSuchElementException(e.toString());
        }
      }
    };
  }

  private List<String> zoneNames(String name) throws NamingException {
    ask(name, 6);
    List<String> labels = fullLabels(name);
    List<String> out = new ArrayList<String>();
    try {
      List<DnsResolver.Record> zone = DnsResolver.transfer(servers, labels, intEnv("com.sun.jndi.dns.timeout.initial", 1000));
      String apex = new DnsName(String.join(".", labels)).toString().toLowerCase(Locale.ROOT);
      for (DnsResolver.Record r : zone) {
        String owner = r.name.endsWith(".") ? r.name.substring(0, r.name.length() - 1) : r.name;
        owner = owner.toLowerCase(Locale.ROOT);
        if (owner.equals(apex) || !owner.endsWith("." + apex)) {
          continue;
        }
        String rel = owner.substring(0, owner.length() - apex.length() - 1);
        if (!out.contains(rel)) {
          out.add(rel);
        }
      }
    } catch (IOException e) {
      CommunicationException ce = new CommunicationException("DNS error");
      ce.setRootCause(e);
      throw ce;
    }
    return out;
  }

  @Override
  public void destroySubcontext(Name name) throws NamingException {
    throw unsupported();
  }

  @Override
  public void destroySubcontext(String name) throws NamingException {
    throw unsupported();
  }

  @Override
  public Context createSubcontext(Name name) throws NamingException {
    throw unsupported();
  }

  @Override
  public Context createSubcontext(String name) throws NamingException {
    throw unsupported();
  }

  @Override
  public Object lookupLink(Name name) throws NamingException {
    return lookup(name);
  }

  @Override
  public Object lookupLink(String name) throws NamingException {
    return lookup(name);
  }

  @Override
  public NameParser getNameParser(Name name) throws NamingException {
    return str -> new DnsName(str);
  }

  @Override
  public NameParser getNameParser(String name) throws NamingException {
    return str -> new DnsName(str);
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
    return env.put(propName, propVal);
  }

  @Override
  public Object removeFromEnvironment(String propName) throws NamingException {
    return env.remove(propName);
  }

  @Override
  public Hashtable<?, ?> getEnvironment() throws NamingException {
    return (Hashtable<?, ?>) env.clone();
  }

  @Override
  public void close() throws NamingException {
  }

  @Override
  public String getNameInNamespace() throws NamingException {
    return domainText();
  }

  @Override
  public void bind(String name, Object obj, Attributes attrs) throws NamingException {
    throw unsupported();
  }

  @Override
  public void bind(Name name, Object obj, Attributes attrs) throws NamingException {
    throw unsupported();
  }

  @Override
  public void rebind(String name, Object obj, Attributes attrs) throws NamingException {
    throw unsupported();
  }

  @Override
  public void rebind(Name name, Object obj, Attributes attrs) throws NamingException {
    throw unsupported();
  }

  @Override
  public DirContext createSubcontext(String name, Attributes attrs) throws NamingException {
    throw unsupported();
  }

  @Override
  public DirContext createSubcontext(Name name, Attributes attrs) throws NamingException {
    throw unsupported();
  }

  @Override
  public DirContext getSchema(String name) throws NamingException {
    throw unsupported();
  }

  @Override
  public DirContext getSchema(Name name) throws NamingException {
    throw unsupported();
  }

  @Override
  public DirContext getSchemaClassDefinition(String name) throws NamingException {
    throw unsupported();
  }

  @Override
  public DirContext getSchemaClassDefinition(Name name) throws NamingException {
    throw unsupported();
  }

  @Override
  public void modifyAttributes(String name, ModificationItem[] mods) throws NamingException {
    throw unsupported();
  }

  @Override
  public void modifyAttributes(String name, int op, Attributes attrs) throws NamingException {
    throw unsupported();
  }

  @Override
  public void modifyAttributes(Name name, ModificationItem[] mods) throws NamingException {
    throw unsupported();
  }

  @Override
  public void modifyAttributes(Name name, int op, Attributes attrs) throws NamingException {
    throw unsupported();
  }

  @Override
  public NamingEnumeration<SearchResult> search(String name, String filter, Object[] args, SearchControls cons)
      throws NamingException {
    throw unsupported();
  }

  @Override
  public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons)
      throws NamingException {
    throw unsupported();
  }

  @Override
  public NamingEnumeration<SearchResult> search(String name, Attributes match, String[] attrs)
      throws NamingException {
    throw unsupported();
  }

  @Override
  public NamingEnumeration<SearchResult> search(String name, Attributes match) throws NamingException {
    throw unsupported();
  }

  @Override
  public NamingEnumeration<SearchResult> search(Name name, String filter, Object[] args, SearchControls cons)
      throws NamingException {
    throw unsupported();
  }

  @Override
  public NamingEnumeration<SearchResult> search(Name name, String filter, SearchControls cons)
      throws NamingException {
    throw unsupported();
  }

  @Override
  public NamingEnumeration<SearchResult> search(Name name, Attributes match, String[] attrs)
      throws NamingException {
    throw unsupported();
  }

  @Override
  public NamingEnumeration<SearchResult> search(Name name, Attributes match) throws NamingException {
    throw unsupported();
  }
}
