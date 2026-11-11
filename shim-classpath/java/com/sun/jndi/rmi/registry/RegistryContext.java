/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.rmi.registry;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.naming.Binding;
import javax.naming.CommunicationException;
import javax.naming.CompositeName;
import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NoPermissionException;
import javax.naming.OperationNotSupportedException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.ServiceUnavailableException;
import javax.naming.spi.NamingManager;

public class RegistryContext implements Context, Referenceable {
  private final Hashtable<Object, Object> environment;
  private final Registry registry;
  private final String host;
  private final int port;

  @SuppressWarnings("unchecked")
  RegistryContext(String host, int port, Hashtable<?, ?> env) throws NamingException {
    this.host = host;
    this.port = port;
    this.environment = env == null ? new Hashtable<Object, Object>(5) : (Hashtable<Object, Object>) env.clone();
    try {
      this.registry = LocateRegistry.getRegistry(host, port);
    } catch (RemoteException e) {
      throw wrap(e);
    }
  }

  private RegistryContext(RegistryContext ctx) {
    this.environment = new Hashtable<Object, Object>(ctx.environment);
    this.registry = ctx.registry;
    this.host = ctx.host;
    this.port = ctx.port;
  }

  @Override
  public void close() {
  }

  @Override
  public Object lookup(String name) throws NamingException {
    return lookup(new CompositeName(name));
  }

  @Override
  public Object lookup(Name name) throws NamingException {
    if (name.isEmpty()) {
      return new RegistryContext(this);
    }
    Remote r;
    try {
      r = registry.lookup(name.get(0));
    } catch (NotBoundException e) {
      throw (NamingException) new NameNotFoundException(name.get(0)).initCause(e);
    } catch (RemoteException e) {
      throw wrap(e);
    }
    return decode(r, name);
  }

  private Object decode(Remote r, Name name) throws NamingException {
    try {
      if (r instanceof RemoteReference) {
        Reference ref = ((RemoteReference) r).getReference();
        return NamingManager.getObjectInstance(ref, name, this, environment);
      }
      return NamingManager.getObjectInstance(r, name, this, environment);
    } catch (RemoteException e) {
      throw wrap(e);
    } catch (NamingException e) {
      throw e;
    } catch (Exception e) {
      NamingException ne = new NamingException("Problem with object retrieved from registry");
      ne.setRootCause(e);
      throw ne;
    }
  }

  @Override
  public void bind(String name, Object obj) throws NamingException {
    bind(new CompositeName(name), obj);
  }

  @Override
  public void bind(Name name, Object obj) throws NamingException {
    if (name.isEmpty()) {
      throw new InvalidNameException("RegistryContext: Cannot bind empty name");
    }
    try {
      registry.bind(name.get(0), encode(obj));
    } catch (AlreadyBoundException e) {
      throw (NamingException) new NameAlreadyBoundException(name.get(0)).initCause(e);
    } catch (RemoteException e) {
      throw wrap(e);
    }
  }

  @Override
  public void rebind(String name, Object obj) throws NamingException {
    rebind(new CompositeName(name), obj);
  }

  @Override
  public void rebind(Name name, Object obj) throws NamingException {
    if (name.isEmpty()) {
      throw new InvalidNameException("RegistryContext: Cannot rebind empty name");
    }
    try {
      registry.rebind(name.get(0), encode(obj));
    } catch (RemoteException e) {
      throw wrap(e);
    }
  }

  @Override
  public void unbind(String name) throws NamingException {
    unbind(new CompositeName(name));
  }

  @Override
  public void unbind(Name name) throws NamingException {
    if (name.isEmpty()) {
      throw new InvalidNameException("RegistryContext: Cannot unbind empty name");
    }
    try {
      registry.unbind(name.get(0));
    } catch (NotBoundException e) {
      return;
    } catch (RemoteException e) {
      throw wrap(e);
    }
  }

  @Override
  public void rename(String oldName, String newName) throws NamingException {
    rename(new CompositeName(oldName), new CompositeName(newName));
  }

  @Override
  public void rename(Name oldName, Name newName) throws NamingException {
    bind(newName, lookup(oldName));
    unbind(oldName);
  }

  @Override
  public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
    return list(new CompositeName(name));
  }

  @Override
  public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
    if (!name.isEmpty()) {
      throw new javax.naming.NotContextException("RegistryContext: can only list \"\"");
    }
    List<NameClassPair> out = new ArrayList<NameClassPair>();
    try {
      for (String n : registry.list()) {
        out.add(new NameClassPair(n, registry.lookup(n).getClass().getName()));
      }
    } catch (NotBoundException | RemoteException e) {
      throw e instanceof RemoteException ? wrap((RemoteException) e) : new NamingException(e.toString());
    }
    return new Enumerator<NameClassPair>(out);
  }

  @Override
  public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
    return listBindings(new CompositeName(name));
  }

  @Override
  public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
    if (!name.isEmpty()) {
      throw new javax.naming.NotContextException("RegistryContext: can only list \"\"");
    }
    List<Binding> out = new ArrayList<Binding>();
    try {
      for (String n : registry.list()) {
        Name single = new CompositeName().add(n);
        out.add(new Binding(n, decode(registry.lookup(n), single)));
      }
    } catch (NotBoundException | RemoteException e) {
      throw e instanceof RemoteException ? wrap((RemoteException) e) : new NamingException(e.toString());
    }
    return new Enumerator<Binding>(out);
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
    return nameParser;
  }

  @Override
  public NameParser getNameParser(Name name) throws NamingException {
    return nameParser;
  }

  private static final NameParser nameParser = new NameParser() {
    @Override
    public Name parse(String name) throws NamingException {
      return new CompositeName(name);
    }
  };

  @Override
  public String composeName(String name, String prefix) throws NamingException {
    return composeName(new CompositeName(name), new CompositeName(prefix)).toString();
  }

  @Override
  public Name composeName(Name name, Name prefix) throws NamingException {
    Name result = (Name) prefix.clone();
    result.addAll(name);
    return result;
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
  public String getNameInNamespace() throws NamingException {
    return "";
  }

  @Override
  public Reference getReference() throws NamingException {
    String url = "rmi://" + (host == null ? "" : host) + (port > 0 && port != Registry.REGISTRY_PORT ? ":" + port : "");
    return new Reference(RegistryContext.class.getName(), new javax.naming.StringRefAddr(
        RegistryContextFactory.ADDRESS_TYPE, url), RegistryContextFactory.class.getName(), null);
  }

  private Remote encode(Object obj) throws NamingException {
    if (obj instanceof Remote) {
      return (Remote) obj;
    }
    if (obj instanceof Reference) {
      return wrapReference((Reference) obj);
    }
    if (obj instanceof Referenceable) {
      return wrapReference(((Referenceable) obj).getReference());
    }
    throw new IllegalArgumentException("RegistryContext: object to bind must be Remote, Reference, or Referenceable");
  }

  private static Remote wrapReference(Reference ref) throws NamingException {
    try {
      return new ReferenceWrapper(ref);
    } catch (RemoteException e) {
      throw wrap(e);
    }
  }

  static NamingException wrap(RemoteException e) {
    NamingException ne;
    if (e instanceof ConnectException) {
      ne = new ServiceUnavailableException();
    } else if (e instanceof AccessException) {
      ne = new NoPermissionException();
    } else if (e instanceof java.rmi.StubNotFoundException || e instanceof java.rmi.UnknownHostException
        || e instanceof java.rmi.server.SocketSecurityException) {
      ne = new ConfigurationException();
    } else if (e instanceof java.rmi.server.ExportException || e instanceof ConnectIOException
        || e instanceof java.rmi.MarshalException || e instanceof java.rmi.UnmarshalException
        || e instanceof NoSuchObjectException) {
      ne = new CommunicationException();
    } else {
      ne = new NamingException();
    }
    ne.setRootCause(e);
    return ne;
  }

  private static final class Enumerator<T> implements NamingEnumeration<T> {
    private final java.util.Iterator<T> it;

    Enumerator(List<T> list) {
      this.it = list.iterator();
    }

    @Override
    public T next() {
      return it.next();
    }

    @Override
    public boolean hasMore() {
      return it.hasNext();
    }

    @Override
    public void close() {
    }

    @Override
    public boolean hasMoreElements() {
      return it.hasNext();
    }

    @Override
    public T nextElement() {
      return it.next();
    }
  }
}
