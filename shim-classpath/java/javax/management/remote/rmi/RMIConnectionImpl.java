/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.remote.rmi;

import java.io.IOException;
import java.rmi.MarshalledObject;
import java.rmi.server.Unreferenced;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.remote.NotificationResult;
import javax.management.remote.TargetedNotification;
import javax.security.auth.Subject;

public class RMIConnectionImpl implements RMIConnection, Unreferenced {
  private final Subject subject;
  private final RMIServerImpl rmiServer;
  private final MBeanServer mbeanServer;
  private final ClassLoader defaultClassLoader;
  private final String connectionId;
  private boolean terminated = false;
  private final Object lock = new Object();

  private static final int MAX_BUFFER = 1000;
  private long firstSeq = 0;
  private long nextSeq = 0;
  private final java.util.LinkedList<TargetedNotification> buffer = new java.util.LinkedList<TargetedNotification>();
  private final Map<Integer, Registration> listeners = new HashMap<Integer, Registration>();
  private int nextListenerId = 1;

  private static final class Registration {
    final ObjectName name;
    final NotificationListener listener;
    final NotificationFilter filter;

    Registration(ObjectName name, NotificationListener listener, NotificationFilter filter) {
      this.name = name;
      this.listener = listener;
      this.filter = filter;
    }
  }

  public RMIConnectionImpl(RMIServerImpl rmiServer, String connectionId, ClassLoader defaultClassLoader,
      Subject subject, Map<String, ?> env) {
    if (rmiServer == null || connectionId == null) {
      throw new NullPointerException("Illegal null argument");
    }
    this.rmiServer = rmiServer;
    this.connectionId = connectionId;
    this.defaultClassLoader = defaultClassLoader;
    this.subject = subject;
    this.mbeanServer = rmiServer.getMBeanServer();
  }

  private <T> T unwrap(MarshalledObject<?> mo, Class<T> wrapClass) throws IOException {
    if (mo == null) {
      return null;
    }
    ClassLoader old = Thread.currentThread().getContextClassLoader();
    try {
      if (defaultClassLoader != null) {
        Thread.currentThread().setContextClassLoader(defaultClassLoader);
      }
      return wrapClass.cast(mo.get());
    } catch (ClassNotFoundException e) {
      throw new java.rmi.UnmarshalException(e.toString(), e);
    } finally {
      Thread.currentThread().setContextClassLoader(old);
    }
  }

  private void checkOpen() throws IOException {
    synchronized (lock) {
      if (terminated) {
        throw new IOException("The connection has been closed.");
      }
    }
  }

  public String getConnectionId() throws IOException {
    return connectionId;
  }

  public void close() throws IOException {
    synchronized (lock) {
      if (terminated) {
        return;
      }
      terminated = true;
      for (Registration r : listeners.values()) {
        try {
          mbeanServer.removeNotificationListener(r.name, r.listener, r.filter, null);
        } catch (InstanceNotFoundException e) {
        } catch (ListenerNotFoundException e) {
        }
      }
      listeners.clear();
      lock.notifyAll();
    }
    rmiServer.closeClient(this);
  }

  public void unreferenced() {
    try {
      close();
    } catch (IOException e) {
    }
  }

  public ObjectInstance createMBean(String className, ObjectName name, Subject delegationSubject)
      throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
      NotCompliantMBeanException, IOException {
    checkOpen();
    return mbeanServer.createMBean(className, name);
  }

  public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName,
      Subject delegationSubject) throws ReflectionException, InstanceAlreadyExistsException,
      MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException,
      IOException {
    checkOpen();
    return mbeanServer.createMBean(className, name, loaderName);
  }

  public ObjectInstance createMBean(String className, ObjectName name, MarshalledObject params, String[] signature,
      Subject delegationSubject) throws ReflectionException, InstanceAlreadyExistsException,
      MBeanRegistrationException, MBeanException, NotCompliantMBeanException, IOException {
    checkOpen();
    Object[] values = unwrap(params, Object[].class);
    return mbeanServer.createMBean(className, name, values, signature);
  }

  public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName,
      MarshalledObject params, String[] signature, Subject delegationSubject) throws ReflectionException,
      InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
      InstanceNotFoundException, IOException {
    checkOpen();
    Object[] values = unwrap(params, Object[].class);
    return mbeanServer.createMBean(className, name, loaderName, values, signature);
  }

  public void unregisterMBean(ObjectName name, Subject delegationSubject) throws InstanceNotFoundException,
      MBeanRegistrationException, IOException {
    checkOpen();
    mbeanServer.unregisterMBean(name);
  }

  public ObjectInstance getObjectInstance(ObjectName name, Subject delegationSubject)
      throws InstanceNotFoundException, IOException {
    checkOpen();
    return mbeanServer.getObjectInstance(name);
  }

  public Set<ObjectInstance> queryMBeans(ObjectName name, MarshalledObject query, Subject delegationSubject)
      throws IOException {
    checkOpen();
    return mbeanServer.queryMBeans(name, unwrap(query, QueryExp.class));
  }

  public Set<ObjectName> queryNames(ObjectName name, MarshalledObject query, Subject delegationSubject)
      throws IOException {
    checkOpen();
    return mbeanServer.queryNames(name, unwrap(query, QueryExp.class));
  }

  public boolean isRegistered(ObjectName name, Subject delegationSubject) throws IOException {
    checkOpen();
    return mbeanServer.isRegistered(name);
  }

  public Integer getMBeanCount(Subject delegationSubject) throws IOException {
    checkOpen();
    return mbeanServer.getMBeanCount();
  }

  public Object getAttribute(ObjectName name, String attribute, Subject delegationSubject) throws MBeanException,
      AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
    checkOpen();
    return mbeanServer.getAttribute(name, attribute);
  }

  public AttributeList getAttributes(ObjectName name, String[] attributes, Subject delegationSubject)
      throws InstanceNotFoundException, ReflectionException, IOException {
    checkOpen();
    return mbeanServer.getAttributes(name, attributes);
  }

  public void setAttribute(ObjectName name, MarshalledObject attribute, Subject delegationSubject)
      throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException,
      ReflectionException, IOException {
    checkOpen();
    mbeanServer.setAttribute(name, unwrap(attribute, Attribute.class));
  }

  public AttributeList setAttributes(ObjectName name, MarshalledObject attributes, Subject delegationSubject)
      throws InstanceNotFoundException, ReflectionException, IOException {
    checkOpen();
    return mbeanServer.setAttributes(name, unwrap(attributes, AttributeList.class));
  }

  public Object invoke(ObjectName name, String operationName, MarshalledObject params, String[] signature,
      Subject delegationSubject) throws InstanceNotFoundException, MBeanException, ReflectionException,
      IOException {
    checkOpen();
    Object[] values = unwrap(params, Object[].class);
    return mbeanServer.invoke(name, operationName, values, signature);
  }

  public String getDefaultDomain(Subject delegationSubject) throws IOException {
    checkOpen();
    return mbeanServer.getDefaultDomain();
  }

  public String[] getDomains(Subject delegationSubject) throws IOException {
    checkOpen();
    return mbeanServer.getDomains();
  }

  public MBeanInfo getMBeanInfo(ObjectName name, Subject delegationSubject) throws InstanceNotFoundException,
      IntrospectionException, ReflectionException, IOException {
    checkOpen();
    return mbeanServer.getMBeanInfo(name);
  }

  public boolean isInstanceOf(ObjectName name, String className, Subject delegationSubject)
      throws InstanceNotFoundException, IOException {
    checkOpen();
    return mbeanServer.isInstanceOf(name, className);
  }

  public void addNotificationListener(ObjectName name, ObjectName listener, MarshalledObject filter,
      MarshalledObject handback, Subject delegationSubject) throws InstanceNotFoundException, IOException {
    checkOpen();
    mbeanServer.addNotificationListener(name, listener, unwrap(filter, NotificationFilter.class),
        unwrap(handback, Object.class));
  }

  public void removeNotificationListener(ObjectName name, ObjectName listener, Subject delegationSubject)
      throws InstanceNotFoundException, ListenerNotFoundException, IOException {
    checkOpen();
    mbeanServer.removeNotificationListener(name, listener);
  }

  public void removeNotificationListener(ObjectName name, ObjectName listener, MarshalledObject filter,
      MarshalledObject handback, Subject delegationSubject) throws InstanceNotFoundException,
      ListenerNotFoundException, IOException {
    checkOpen();
    mbeanServer.removeNotificationListener(name, listener, unwrap(filter, NotificationFilter.class),
        unwrap(handback, Object.class));
  }

  public Integer[] addNotificationListeners(ObjectName[] names, MarshalledObject[] filters, Subject[] subjects)
      throws InstanceNotFoundException, IOException {
    checkOpen();
    if (names == null || filters == null) {
      throw new IllegalArgumentException("Null arguments");
    }
    if (names.length != filters.length) {
      throw new IllegalArgumentException("The value lengths of 3 parameters are not same.");
    }
    Integer[] ids = new Integer[names.length];
    List<Registration> done = new ArrayList<Registration>();
    try {
      for (int i = 0; i < names.length; i++) {
        final Integer id;
        synchronized (lock) {
          id = Integer.valueOf(nextListenerId++);
        }
        NotificationFilter f = unwrap(filters[i], NotificationFilter.class);
        final ObjectName source = names[i];
        NotificationListener l = new NotificationListener() {
          public void handleNotification(Notification notification, Object handback) {
            if (!(notification.getSource() instanceof ObjectName)) {
              notification.setSource(source);
            }
            enqueue(new TargetedNotification(notification, id));
          }
        };
        mbeanServer.addNotificationListener(names[i], l, f, null);
        Registration r = new Registration(names[i], l, f);
        synchronized (lock) {
          listeners.put(id, r);
        }
        done.add(r);
        ids[i] = id;
      }
    } catch (InstanceNotFoundException e) {
      for (Registration r : done) {
        try {
          mbeanServer.removeNotificationListener(r.name, r.listener, r.filter, null);
        } catch (Exception ex) {
        }
      }
      throw e;
    }
    return ids;
  }

  public void removeNotificationListeners(ObjectName name, Integer[] listenerIDs, Subject delegationSubject)
      throws InstanceNotFoundException, ListenerNotFoundException, IOException {
    checkOpen();
    if (name == null || listenerIDs == null) {
      throw new IllegalArgumentException("Illegal null parameter");
    }
    for (Integer id : listenerIDs) {
      Registration r;
      synchronized (lock) {
        r = listeners.get(id);
      }
      if (r == null || !r.name.equals(name)) {
        throw new ListenerNotFoundException("Unknown listener id " + id);
      }
    }
    for (Integer id : listenerIDs) {
      Registration r;
      synchronized (lock) {
        r = listeners.remove(id);
      }
      mbeanServer.removeNotificationListener(r.name, r.listener, r.filter, null);
    }
  }

  private void enqueue(TargetedNotification tn) {
    synchronized (lock) {
      buffer.addLast(tn);
      nextSeq++;
      while (buffer.size() > MAX_BUFFER) {
        buffer.removeFirst();
        firstSeq++;
      }
      lock.notifyAll();
    }
  }

  public NotificationResult fetchNotifications(long clientSequenceNumber, int maxNotifications, long timeout)
      throws IOException {
    if (maxNotifications < 0 || timeout < 0) {
      throw new IllegalArgumentException("Illegal negative argument");
    }
    long deadline = timeout == Long.MAX_VALUE ? Long.MAX_VALUE : System.currentTimeMillis() + timeout;
    synchronized (lock) {
      while (!terminated) {
        long start = clientSequenceNumber < 0 ? nextSeq : clientSequenceNumber;
        if (start < nextSeq || timeout == 0) {
          break;
        }
        long wait = deadline == Long.MAX_VALUE ? 0 : deadline - System.currentTimeMillis();
        if (deadline != Long.MAX_VALUE && wait <= 0) {
          break;
        }
        try {
          lock.wait(wait);
        } catch (InterruptedException e) {
          break;
        }
      }
      if (terminated) {
        throw new IOException("The connection has been closed.");
      }
      long start = clientSequenceNumber < 0 ? nextSeq : clientSequenceNumber;
      long from = Math.max(start, firstSeq);
      List<TargetedNotification> skip = new ArrayList<TargetedNotification>();
      int skipCount = (int) (from - firstSeq);
      int count = 0;
      for (TargetedNotification tn : buffer) {
        if (count++ < skipCount) {
          continue;
        }
        if (skip.size() < maxNotifications) {
          skip.add(tn);
        }
      }
      return new NotificationResult(firstSeq, from + skip.size(), skip.toArray(new TargetedNotification[0]));
    }
  }
}
