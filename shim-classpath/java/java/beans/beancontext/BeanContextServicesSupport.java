/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.beans.beancontext;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TooManyListenersException;

public class BeanContextServicesSupport extends BeanContextSupport implements BeanContextServices {
  private static final long serialVersionUID = -8494482757288719206L;

  protected transient HashMap<Object, BCSSServiceProvider> services;
  protected transient int serializable = 0;
  protected transient BCSSProxyServiceProvider proxy;
  protected transient ArrayList<BeanContextServicesListener> bcsListeners;

  public BeanContextServicesSupport(BeanContextServices peer, Locale lcle, boolean dtime, boolean visible) {
    super(peer, lcle, dtime, visible);
  }

  public BeanContextServicesSupport(BeanContextServices peer, Locale lcle, boolean dtime) {
    this(peer, lcle, dtime, true);
  }

  public BeanContextServicesSupport(BeanContextServices peer, Locale lcle) { this(peer, lcle, false, true); }

  public BeanContextServicesSupport(BeanContextServices peer) { this(peer, null, false, true); }

  public BeanContextServicesSupport() { this(null, null, false, true); }

  public void initialize() {
    super.initialize();
    services = new HashMap<Object, BCSSServiceProvider>(serializable + 1);
    bcsListeners = new ArrayList<BeanContextServicesListener>(1);
  }

  public BeanContextServices getBeanContextServicesPeer() { return (BeanContextServices) getBeanContextChildPeer(); }

  protected BCSChild createBCSChild(Object targetChild, Object peer) { return new BCSSChild(targetChild, peer); }

  protected BCSSServiceProvider createBCSSServiceProvider(Class<?> sc, BeanContextServiceProvider bcsp) {
    return new BCSSServiceProvider(sc, bcsp);
  }

  public void addBeanContextServicesListener(BeanContextServicesListener bcsl) {
    if (bcsl == null) {
      throw new NullPointerException("bcsl");
    }
    synchronized (bcsListeners) {
      if (bcsListeners.contains(bcsl)) {
        return;
      } else {
        bcsListeners.add(bcsl);
      }
    }
  }

  public void removeBeanContextServicesListener(BeanContextServicesListener bcsl) {
    if (bcsl == null) {
      throw new NullPointerException("bcsl");
    }
    synchronized (bcsListeners) {
      if (!bcsListeners.contains(bcsl)) {
        return;
      } else {
        bcsListeners.remove(bcsl);
      }
    }
  }

  public boolean addService(Class<?> serviceClass, BeanContextServiceProvider bcsp) {
    return addService(serviceClass, bcsp, true);
  }

  protected boolean addService(Class<?> serviceClass, BeanContextServiceProvider bcsp, boolean fireEvent) {
    if (serviceClass == null) {
      throw new NullPointerException("serviceClass");
    }
    if (bcsp == null) {
      throw new NullPointerException("bcsp");
    }
    synchronized (BeanContext.globalHierarchyLock) {
      if (services.containsKey(serviceClass)) {
        return false;
      } else {
        services.put(serviceClass, createBCSSServiceProvider(serviceClass, bcsp));
        if (bcsp instanceof Serializable) {
          serializable++;
        }
        if (!fireEvent) {
          return true;
        }
        BeanContextServiceAvailableEvent bcssae = new BeanContextServiceAvailableEvent(getBeanContextServicesPeer(),
            serviceClass);
        fireServiceAdded(bcssae);
        synchronized (children) {
          Iterator<Object> i = children.keySet().iterator();
          while (i.hasNext()) {
            Object c = i.next();
            if (c instanceof BeanContextServices) {
              ((BeanContextServicesListener) c).serviceAvailable(bcssae);
            }
          }
        }
        return true;
      }
    }
  }

  public void revokeService(Class<?> serviceClass, BeanContextServiceProvider bcsp, boolean revokeCurrentServicesNow) {
    if (serviceClass == null) {
      throw new NullPointerException("serviceClass");
    }
    if (bcsp == null) {
      throw new NullPointerException("bcsp");
    }
    synchronized (BeanContext.globalHierarchyLock) {
      if (!services.containsKey(serviceClass)) {
        return;
      }
      BCSSServiceProvider bcsssp = services.get(serviceClass);
      if (!bcsssp.getServiceProvider().equals(bcsp)) {
        throw new IllegalArgumentException("service provider mismatch");
      }
      services.remove(serviceClass);
      if (bcsp instanceof Serializable) {
        serializable--;
      }
      Iterator<BCSChild> i = bcsChildren();
      while (i.hasNext()) {
        BCSSChild bcssc = (BCSSChild) i.next();
        bcssc.revokeService(serviceClass, false, revokeCurrentServicesNow);
      }
      fireServiceRevoked(serviceClass, revokeCurrentServicesNow);
    }
  }

  public synchronized boolean hasService(Class<?> serviceClass) {
    if (serviceClass == null) {
      throw new NullPointerException("serviceClass");
    }
    synchronized (BeanContext.globalHierarchyLock) {
      if (services.containsKey(serviceClass)) {
        return true;
      }
      BeanContextServices bcs = null;
      try {
        bcs = (BeanContextServices) getBeanContext();
      } catch (ClassCastException cce) {
        return false;
      }
      return bcs == null ? false : bcs.hasService(serviceClass);
    }
  }

  public Object getService(BeanContextChild child, Object requestor, Class<?> serviceClass, Object serviceSelector,
      BeanContextServiceRevokedListener bcsrl) throws TooManyListenersException {
    if (child == null) {
      throw new NullPointerException("child");
    }
    if (serviceClass == null) {
      throw new NullPointerException("serviceClass");
    }
    if (requestor == null) {
      throw new NullPointerException("requestor");
    }
    if (bcsrl == null) {
      throw new NullPointerException("bcsrl");
    }
    Object service = null;
    BeanContextServices bcs = getBeanContextServicesPeer();
    synchronized (BeanContext.globalHierarchyLock) {
      synchronized (children) {
        if (!contains(child)) {
          throw new IllegalArgumentException("not a child of this context");
        }
      }
      BCSSServiceProvider bcsssp = services.get(serviceClass);
      if (bcsssp != null) {
        BeanContextServiceProvider bcsp = bcsssp.getServiceProvider();
        service = bcsp.getService(bcs, requestor, serviceClass, serviceSelector);
        if (service != null) {
          BCSSChild bcsc = (BCSSChild) children.get(child);
          try {
            bcsc.usingService(requestor, service, serviceClass, bcsp, false, bcsrl);
          } catch (TooManyListenersException tmle) {
            bcsp.releaseService(bcs, requestor, service);
            throw tmle;
          } catch (UnsupportedOperationException uope) {
            bcsp.releaseService(bcs, requestor, service);
            throw uope;
          }
          return service;
        }
      }
      if (proxy != null) {
        service = proxy.getService(bcs, requestor, serviceClass, serviceSelector);
        if (service != null) {
          BCSSChild bcsc = (BCSSChild) children.get(child);
          try {
            bcsc.usingService(requestor, service, serviceClass, proxy, true, bcsrl);
          } catch (TooManyListenersException tmle) {
            proxy.releaseService(bcs, requestor, service);
            throw tmle;
          } catch (UnsupportedOperationException uope) {
            proxy.releaseService(bcs, requestor, service);
            throw uope;
          }
          return service;
        }
      }
    }
    return null;
  }

  public void releaseService(BeanContextChild child, Object requestor, Object service) {
    if (child == null) {
      throw new NullPointerException("child");
    }
    if (requestor == null) {
      throw new NullPointerException("requestor");
    }
    if (service == null) {
      throw new NullPointerException("service");
    }
    synchronized (BeanContext.globalHierarchyLock) {
      synchronized (children) {
        if (!contains(child)) {
          throw new IllegalArgumentException("not a child of this context");
        }
      }
      BCSSChild bcssc = (BCSSChild) children.get(child);
      bcssc.releaseService(requestor, service);
    }
  }

  public Iterator<Object> getCurrentServiceClasses() { return new BCSIterator(services.keySet().iterator()); }

  public Iterator<?> getCurrentServiceSelectors(Class<?> serviceClass) {
    BCSSServiceProvider bcsssp = services.get(serviceClass);
    return bcsssp != null ? bcsssp.getServiceProvider().getCurrentServiceSelectors(getBeanContextServicesPeer(),
        serviceClass) : null;
  }

  public void serviceAvailable(BeanContextServiceAvailableEvent bcssae) {
    synchronized (BeanContext.globalHierarchyLock) {
      if (services.containsKey(bcssae.getServiceClass())) {
        return;
      }
      fireServiceAdded(bcssae);
      Iterator<Object> i;
      synchronized (children) {
        i = children.keySet().iterator();
        while (i.hasNext()) {
          Object c = i.next();
          if (c instanceof BeanContextServices) {
            ((BeanContextServicesListener) c).serviceAvailable(bcssae);
          }
        }
      }
    }
  }

  public void serviceRevoked(BeanContextServiceRevokedEvent bcssre) {
    synchronized (BeanContext.globalHierarchyLock) {
      if (services.containsKey(bcssre.getServiceClass())) {
        return;
      }
      fireServiceRevoked(bcssre);
      Iterator<Object> i;
      synchronized (children) {
        i = children.keySet().iterator();
        while (i.hasNext()) {
          Object c = i.next();
          if (c instanceof BeanContextServices) {
            ((BeanContextServicesListener) c).serviceRevoked(bcssre);
          }
        }
      }
    }
  }

  protected static final BeanContextServicesListener getChildBeanContextServicesListener(Object child) {
    try {
      return (BeanContextServicesListener) child;
    } catch (ClassCastException cce) {
      return null;
    }
  }

  protected void childJustRemovedHook(Object child, BCSChild bcsc) {
    BCSSChild bcssc = (BCSSChild) bcsc;
    bcssc.cleanupReferences();
  }

  protected synchronized void releaseBeanContextResources() {
    Object[] bcssc;
    super.releaseBeanContextResources();
    synchronized (children) {
      if (children.isEmpty()) {
        return;
      }
      bcssc = children.values().toArray();
    }
    for (int i = 0; i < bcssc.length; i++) {
      ((BCSSChild) bcssc[i]).cleanupReferences();
    }
    proxy = null;
  }

  protected synchronized void initializeBeanContextResources() {
    super.initializeBeanContextResources();
    BeanContext nbc = getBeanContext();
    if (nbc == null) {
      return;
    }
    try {
      BeanContextServices bcs = (BeanContextServices) nbc;
      proxy = new BCSSProxyServiceProvider(bcs);
    } catch (ClassCastException cce) {
      cce.getMessage();
    }
  }

  protected final void fireServiceAdded(Class<?> serviceClass) {
    BeanContextServiceAvailableEvent bcssae = new BeanContextServiceAvailableEvent(getBeanContextServicesPeer(),
        serviceClass);
    fireServiceAdded(bcssae);
  }

  protected final void fireServiceAdded(BeanContextServiceAvailableEvent bcssae) {
    Object[] copy;
    synchronized (bcsListeners) {
      copy = bcsListeners.toArray();
    }
    for (int i = 0; i < copy.length; i++) {
      ((BeanContextServicesListener) copy[i]).serviceAvailable(bcssae);
    }
  }

  protected final void fireServiceRevoked(BeanContextServiceRevokedEvent bcsre) {
    Object[] copy;
    synchronized (bcsListeners) {
      copy = bcsListeners.toArray();
    }
    for (int i = 0; i < copy.length; i++) {
      ((BeanContextServiceRevokedListener) copy[i]).serviceRevoked(bcsre);
    }
  }

  protected final void fireServiceRevoked(Class<?> serviceClass, boolean revokeNow) {
    Object[] copy;
    BeanContextServiceRevokedEvent bcsre = new BeanContextServiceRevokedEvent(getBeanContextServicesPeer(),
        serviceClass, revokeNow);
    synchronized (bcsListeners) {
      copy = bcsListeners.toArray();
    }
    for (int i = 0; i < copy.length; i++) {
      ((BeanContextServicesListener) copy[i]).serviceRevoked(bcsre);
    }
  }

  protected synchronized void bcsPreSerializationHook(ObjectOutputStream oos) throws IOException {
    oos.writeInt(serializable);
    if (serializable <= 0) {
      return;
    }
    int count = 0;
    Iterator<Map.Entry<Object, BCSSServiceProvider>> i = services.entrySet().iterator();
    while (i.hasNext() && count < serializable) {
      Map.Entry<Object, BCSSServiceProvider> entry = i.next();
      BCSSServiceProvider bcsp = null;
      try {
        bcsp = entry.getValue();
      } catch (ClassCastException cce) {
        continue;
      }
      if (bcsp.getServiceProvider() instanceof Serializable) {
        oos.writeObject(entry.getKey());
        oos.writeObject(bcsp);
        count++;
      }
    }
    if (count != serializable) {
      throw new IOException("wrote different number of service providers than expected");
    }
  }

  protected synchronized void bcsPreDeserializationHook(ObjectInputStream ois) throws IOException,
      ClassNotFoundException {
    serializable = ois.readInt();
    int count = serializable;
    while (count > 0) {
      services.put(ois.readObject(), (BCSSServiceProvider) ois.readObject());
      count--;
    }
  }

  protected class BCSSChild extends BeanContextSupport.BCSChild {
    private static final long serialVersionUID = -3263851306889194873L;

    private transient HashMap<Object, HashMap<Object, Object[]>> serviceClasses;
    private transient HashMap<Object, Object[]> serviceRequestors;

    BCSSChild(Object bcc, Object peer) { super(bcc, peer); }

    void usingService(Object requestor, Object service, Class<?> serviceClass, BeanContextServiceProvider bcsp,
        boolean isDelegated, BeanContextServiceRevokedListener bcsrl) throws TooManyListenersException,
        UnsupportedOperationException {
      if (serviceClasses == null) {
        serviceClasses = new HashMap<Object, HashMap<Object, Object[]>>(1);
      }
      HashMap<Object, Object[]> reqs = serviceClasses.get(serviceClass);
      if (reqs == null) {
        reqs = new HashMap<Object, Object[]>(1);
        serviceClasses.put(serviceClass, reqs);
      }
      reqs.put(service, new Object[] {requestor, bcsp, bcsrl, Boolean.valueOf(isDelegated)});
    }

    void releaseService(Object requestor, Object service) {
      if (serviceClasses == null) {
        return;
      }
      for (Iterator<HashMap<Object, Object[]>> i = serviceClasses.values().iterator(); i.hasNext();) {
        HashMap<Object, Object[]> reqs = i.next();
        Object[] rec = reqs.get(service);
        if (rec != null && rec[0] == requestor) {
          reqs.remove(service);
          BeanContextServiceProvider bcsp = (BeanContextServiceProvider) rec[1];
          bcsp.releaseService(getBeanContextServicesPeer(), requestor, service);
          return;
        }
      }
    }

    void revokeService(Class<?> serviceClass, boolean isDelegated, boolean revokeNow) {
      if (serviceClasses == null) {
        return;
      }
      HashMap<Object, Object[]> reqs = serviceClasses.get(serviceClass);
      if (reqs == null) {
        return;
      }
      BeanContextServiceRevokedEvent bcsre = new BeanContextServiceRevokedEvent(getBeanContextServicesPeer(),
          serviceClass, revokeNow);
      for (Object[] rec : new ArrayList<Object[]>(reqs.values())) {
        if (((Boolean) rec[3]).booleanValue() == isDelegated && rec[2] != null) {
          ((BeanContextServiceRevokedListener) rec[2]).serviceRevoked(bcsre);
        }
      }
      if (revokeNow) {
        reqs.clear();
      }
    }

    void cleanupReferences() {
      if (serviceClasses == null) {
        return;
      }
      for (HashMap<Object, Object[]> reqs : new ArrayList<HashMap<Object, Object[]>>(serviceClasses.values())) {
        for (Map.Entry<Object, Object[]> e : new ArrayList<Map.Entry<Object, Object[]>>(reqs.entrySet())) {
          Object[] rec = e.getValue();
          ((BeanContextServiceProvider) rec[1]).releaseService(getBeanContextServicesPeer(), rec[0], e.getKey());
        }
      }
      serviceClasses = null;
      serviceRequestors = null;
    }
  }

  protected static class BCSSServiceProvider implements Serializable {
    private static final long serialVersionUID = 861278251667444782L;

    protected BeanContextServiceProvider serviceProvider;

    BCSSServiceProvider(Class<?> sc, BeanContextServiceProvider bcsp) {
      super();
      serviceProvider = bcsp;
    }

    protected BeanContextServiceProvider getServiceProvider() { return serviceProvider; }
  }

  protected class BCSSProxyServiceProvider implements BeanContextServiceProvider, BeanContextServiceRevokedListener {
    private final BeanContextServices backingCtx;

    BCSSProxyServiceProvider(BeanContextServices bcs) { backingCtx = bcs; }

    public Object getService(BeanContextServices bcs, Object requestor, Class<?> serviceClass, Object serviceSelector) {
      Object service = null;
      try {
        service = backingCtx.getService(bcs, requestor, serviceClass, serviceSelector, this);
      } catch (TooManyListenersException tmle) {
        return null;
      }
      return service;
    }

    public void releaseService(BeanContextServices bcs, Object requestor, Object service) {
      backingCtx.releaseService(bcs, requestor, service);
    }

    public Iterator<?> getCurrentServiceSelectors(BeanContextServices bcs, Class<?> serviceClass) {
      return backingCtx.getCurrentServiceSelectors(serviceClass);
    }

    public void serviceRevoked(BeanContextServiceRevokedEvent bcsre) {
      Iterator<BCSChild> i = bcsChildren();
      while (i.hasNext()) {
        BCSSChild bcsc = (BCSSChild) i.next();
        bcsc.revokeService(bcsre.getServiceClass(), true, bcsre.isCurrentServiceInvalidNow());
      }
    }
  }
}
