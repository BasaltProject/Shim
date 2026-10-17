/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.management;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMX;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerDelegate;
import javax.management.MBeanServerNotification;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryEval;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.loading.ClassLoaderRepository;

public final class MBeanServerImpl implements MBeanServer {
  private static final class Entry {
    final DynamicMBean mbean;
    final Object resource;
    final String className;

    Entry(DynamicMBean mbean, Object resource, String className) {
      this.mbean = mbean;
      this.resource = resource;
      this.className = className;
    }
  }

  private static final class Registration {
    final ObjectName name;
    final NotificationListener listener;
    final NotificationFilter filter;
    final Object handback;

    Registration(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) {
      this.name = name;
      this.listener = listener;
      this.filter = filter;
      this.handback = handback;
    }
  }

  private final List<Registration> registrations = new ArrayList<Registration>();
  private final Map<ObjectName, Entry> beans = new LinkedHashMap<ObjectName, Entry>();
  private final String defaultDomain;
  private final MBeanServerDelegate delegate;
  private final MBeanServer outer;
  private long notificationSequence = 1;

  public MBeanServerImpl(String defaultDomain, MBeanServer outer, MBeanServerDelegate delegate) {
    this.defaultDomain = defaultDomain == null ? "DefaultDomain" : defaultDomain;
    this.outer = outer;
    this.delegate = delegate == null ? new MBeanServerDelegate() : delegate;
    try {
      registerInternal(this.delegate, MBeanServerDelegate.DELEGATE_NAME, true);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static RuntimeOperationsException bad(String message, String context) {
    return new RuntimeOperationsException(new IllegalArgumentException(message), context);
  }

  private ObjectName fixDomain(ObjectName name) {
    if (name.getDomain().length() == 0) {
      try {
        return new ObjectName(defaultDomain + ":" + name.getKeyPropertyListString());
      } catch (javax.management.MalformedObjectNameException e) {
        throw new IllegalStateException(e);
      }
    }
    return name;
  }

  private static Class<?> findStandardInterface(Class<?> c) {
    for (Class<?> k = c; k != null; k = k.getSuperclass()) {
      String want = k.getName() + "MBean";
      for (Class<?> i : k.getInterfaces()) {
        if (i.getName().equals(want) && java.lang.reflect.Modifier.isPublic(i.getModifiers())) {
          return i;
        }
      }
    }
    return null;
  }

  private static Class<?> findMXInterface(Class<?> c) {
    for (Class<?> k = c; k != null; k = k.getSuperclass()) {
      for (Class<?> i : allInterfaces(k)) {
        if (JMX.isMXBeanInterface(i)) {
          return i;
        }
      }
    }
    return null;
  }

  private static List<Class<?>> allInterfaces(Class<?> k) {
    List<Class<?>> out = new ArrayList<Class<?>>();
    for (Class<?> i : k.getInterfaces()) {
      out.add(i);
      out.addAll(allInterfaces(i));
    }
    return out;
  }

  private static DynamicMBean wrap(Object object) throws NotCompliantMBeanException {
    if (object instanceof DynamicMBean) {
      return (DynamicMBean) object;
    }
    Class<?> c = object.getClass();
    Class<?> std = findStandardInterface(c);
    Class<?> mx = findMXInterface(c);
    if (std != null && mx != null) {
      throw new NotCompliantMBeanException("Class " + c.getName() + " implements both a Standard MBean interface "
          + "and an MXBean interface");
    }
    if (std != null) {
      return new MBeanSupport(object, std, false);
    }
    if (mx != null) {
      return new MBeanSupport(object, mx, true);
    }
    throw new NotCompliantMBeanException("MBean class " + c.getName() + " does not implement DynamicMBean, and "
        + "neither follows the Standard MBean conventions (javax.management.NotCompliantMBeanException: Class "
        + c.getName() + " is not a JMX compliant Standard MBean) nor the MXBean conventions "
        + "(javax.management.NotCompliantMBeanException: " + c.getName() + ": Class " + c.getName()
        + " is not a JMX compliant MXBean)");
  }

  private ObjectInstance registerInternal(Object object, ObjectName name, boolean internal)
      throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
    String ctx = "Exception occurred trying to register the MBean";
    if (object == null) {
      throw new NullPointerException("object is null");
    }
    DynamicMBean mbean = wrap(object);
    if (name != null && !name.isPattern()) {
      name = fixDomain(name);
    }
    Object resource = object;
    if (object instanceof javax.management.StandardMBean) {
      resource = ((javax.management.StandardMBean) object).getImplementation();
    }
    String className = mbean instanceof MBeanSupport ? resource.getClass().getName() : classNameOf(object, mbean);
    ObjectName result = name;
    if (object instanceof MBeanRegistration) {
      try {
        result = ((MBeanRegistration) object).preRegister(this, name);
      } catch (RuntimeException e) {
        throw new RuntimeMBeanException(e, "RuntimeException thrown in preRegister method");
      } catch (Error e) {
        throw new RuntimeErrorException(e, "Error thrown in preRegister method");
      } catch (Exception e) {
        throw new MBeanRegistrationException(e, "Exception thrown in preRegister method");
      }
    }
    boolean ok = false;
    try {
      if (result == null) {
        throw bad("No object name specified", ctx);
      }
      if (result.isPattern()) {
        throw new RuntimeOperationsException(new IllegalArgumentException(
            "Repository: cannot add mbean for pattern name " + result));
      }
      result = fixDomain(result);
      if (result.getDomain().equals("JMImplementation") && !internal) {
        throw new RuntimeOperationsException(new IllegalArgumentException(
            "Repository: domain name cannot be JMImplementation"));
      }
      synchronized (this) {
        if (beans.containsKey(result)) {
          throw new InstanceAlreadyExistsException(result.toString());
        }
        beans.put(result, new Entry(mbean, resource, className));
      }
      ok = true;
    } finally {
      if (!ok && object instanceof MBeanRegistration) {
        ((MBeanRegistration) object).postRegister(Boolean.FALSE);
      }
    }
    if (object instanceof MBeanRegistration) {
      ((MBeanRegistration) object).postRegister(Boolean.TRUE);
    }
    send(MBeanServerNotification.REGISTRATION_NOTIFICATION, result);
    return new ObjectInstance(result, className);
  }

  private static String classNameOf(Object object, DynamicMBean mbean) {
    String name = null;
    try {
      name = mbean.getMBeanInfo().getClassName();
    } catch (RuntimeException e) {
      name = null;
    }
    return name == null ? object.getClass().getName() : name;
  }

  private void send(String type, ObjectName name) {
    long seq;
    synchronized (this) {
      seq = notificationSequence++;
    }
    delegate.sendNotification(new MBeanServerNotification(type, MBeanServerDelegate.DELEGATE_NAME, seq, name));
  }

  private Entry entry(ObjectName name) throws InstanceNotFoundException {
    if (name == null) {
      throw bad("Object name cannot be null", "Exception occurred trying to get the MBean");
    }
    Entry e;
    synchronized (this) {
      e = beans.get(fixDomain(name));
    }
    if (e == null) {
      throw new InstanceNotFoundException(name.toString());
    }
    return e;
  }

  @Override
  public ObjectInstance registerMBean(Object object, ObjectName name) throws InstanceAlreadyExistsException,
      MBeanRegistrationException, NotCompliantMBeanException {
    return registerInternal(object, name, false);
  }

  @Override
  public void unregisterMBean(ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException {
    if (name == null) {
      throw bad("Object name cannot be null", "Exception occurred trying to unregister the MBean");
    }
    Entry e = entry(name);
    if (name.equals(MBeanServerDelegate.DELEGATE_NAME)) {
      throw new RuntimeMBeanException(new IllegalArgumentException(
          "The MBeanServerDelegate MBean cannot be unregistered"), "RuntimeException thrown in preDeregister method");
    }
    Object object = e.resource;
    ObjectName key = fixDomain(name);
    if (object instanceof MBeanRegistration) {
      try {
        ((MBeanRegistration) object).preDeregister();
      } catch (RuntimeException ex) {
        throw new RuntimeMBeanException(ex, "RuntimeException thrown in preDeregister method");
      } catch (Error ex) {
        throw new RuntimeErrorException(ex, "Error thrown in preDeregister method");
      } catch (Exception ex) {
        throw new MBeanRegistrationException(ex, "Exception thrown in preDeregister method");
      }
    }
    synchronized (this) {
      beans.remove(key);
    }
    if (object instanceof MBeanRegistration) {
      ((MBeanRegistration) object).postDeregister();
    }
    send(MBeanServerNotification.UNREGISTRATION_NOTIFICATION, key);
  }

  @Override
  public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException {
    Entry e = entry(name);
    return new ObjectInstance(fixDomain(name), e.className);
  }

  private List<ObjectName> matching(ObjectName pattern, QueryExp query) {
    List<ObjectName> out = new ArrayList<ObjectName>();
    List<ObjectName> all;
    synchronized (this) {
      all = new ArrayList<ObjectName>(beans.keySet());
    }
    ObjectName p = pattern == null ? ObjectName.WILDCARD : pattern;
    for (ObjectName n : all) {
      if (p.isPattern() ? !p.apply(n) : !p.equals(n)) {
        continue;
      }
      if (query != null && !test(query, n)) {
        continue;
      }
      out.add(n);
    }
    return out;
  }

  private boolean test(QueryExp query, ObjectName name) {
    MBeanServer previous = QueryEval.getMBeanServer();
    query.setMBeanServer(outer == null ? this : outer);
    new QueryEval() { }.setMBeanServer(outer == null ? this : outer);
    try {
      return query.apply(name);
    } catch (Exception e) {
      return false;
    } finally {
      new QueryEval() { }.setMBeanServer(previous);
    }
  }

  @Override
  public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) {
    Set<ObjectInstance> out = new HashSet<ObjectInstance>();
    for (ObjectName n : matching(name, query)) {
      Entry e;
      synchronized (this) {
        e = beans.get(n);
      }
      if (e != null) {
        out.add(new ObjectInstance(n, e.className));
      }
    }
    return out;
  }

  @Override
  public Set<ObjectName> queryNames(ObjectName name, QueryExp query) {
    return new HashSet<ObjectName>(matching(name, query));
  }

  @Override
  public boolean isRegistered(ObjectName name) {
    if (name == null) {
      throw bad("Object name cannot be null", "Object name cannot be null");
    }
    synchronized (this) {
      return beans.containsKey(fixDomain(name));
    }
  }

  @Override
  public Integer getMBeanCount() {
    synchronized (this) {
      return beans.size();
    }
  }

  private static RuntimeException wrapRuntime(RuntimeException e) {
    if (e instanceof RuntimeOperationsException || e instanceof RuntimeMBeanException) {
      return e;
    }
    return new RuntimeMBeanException(e, e.toString());
  }

  @Override
  public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException,
      InstanceNotFoundException, ReflectionException {
    if (name == null) {
      throw bad("Object name cannot be null", "Exception occurred trying to invoke the getter on the MBean");
    }
    if (attribute == null) {
      throw bad("Attribute cannot be null", "Exception occurred trying to invoke the getter on the MBean");
    }
    Entry e = entry(name);
    try {
      return e.mbean.getAttribute(attribute);
    } catch (RuntimeException ex) {
      throw wrapRuntime(ex);
    } catch (Error ex) {
      throw new RuntimeErrorException(ex, ex.toString());
    }
  }

  @Override
  public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException,
      ReflectionException {
    if (name == null) {
      throw bad("ObjectName name cannot be null", "Exception occurred trying to invoke the getter on the MBean");
    }
    if (attributes == null) {
      throw bad("Attributes cannot be null", "Exception occurred trying to invoke the getter on the MBean");
    }
    Entry e = entry(name);
    try {
      return e.mbean.getAttributes(attributes);
    } catch (RuntimeException ex) {
      throw wrapRuntime(ex);
    }
  }

  @Override
  public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException,
      AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
    if (name == null) {
      throw bad("Object name cannot be null", "Exception occurred trying to invoke the setter on the MBean");
    }
    if (attribute == null) {
      throw bad("Attribute cannot be null", "Exception occurred trying to invoke the setter on the MBean");
    }
    Entry e = entry(name);
    try {
      e.mbean.setAttribute(attribute);
    } catch (RuntimeException ex) {
      throw wrapRuntime(ex);
    } catch (Error ex) {
      throw new RuntimeErrorException(ex, ex.toString());
    }
  }

  @Override
  public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException,
      ReflectionException {
    if (name == null) {
      throw bad("ObjectName name cannot be null", "Exception occurred trying to invoke the setter on the MBean");
    }
    if (attributes == null) {
      throw bad("AttributeList  cannot be null", "Exception occurred trying to invoke the setter on the MBean");
    }
    Entry e = entry(name);
    try {
      return e.mbean.setAttributes(attributes);
    } catch (RuntimeException ex) {
      throw wrapRuntime(ex);
    }
  }

  @Override
  public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
      throws InstanceNotFoundException, MBeanException, ReflectionException {
    Entry e = entry(name);
    try {
      return e.mbean.invoke(operationName, params, signature);
    } catch (RuntimeException ex) {
      throw wrapRuntime(ex);
    } catch (Error ex) {
      throw new RuntimeErrorException(ex, ex.toString());
    }
  }

  @Override
  public String getDefaultDomain() {
    return defaultDomain;
  }

  @Override
  public String[] getDomains() {
    Set<String> domains = new HashSet<String>();
    synchronized (this) {
      for (ObjectName n : beans.keySet()) {
        domains.add(n.getDomain());
      }
    }
    return domains.toArray(new String[0]);
  }

  private NotificationBroadcaster broadcaster(ObjectName name) throws InstanceNotFoundException {
    Entry e = entry(name);
    Object target = e.resource;
    if (!(target instanceof NotificationBroadcaster)) {
      throw new RuntimeOperationsException(new IllegalArgumentException(name.toString()),
          "MBean " + name + " does not implement " + NotificationBroadcaster.class.getName());
    }
    return (NotificationBroadcaster) target;
  }

  @Override
  public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter,
      Object handback) throws InstanceNotFoundException {
    if (listener == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Null listener"), "Null listener");
    }
    broadcaster(name).addNotificationListener(listener, filter, handback);
    synchronized (registrations) {
      registrations.add(new Registration(fixDomain(name), listener, filter, handback));
    }
  }

  @Override
  public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter,
      Object handback) throws InstanceNotFoundException {
    Entry l = entry(listener);
    if (!(l.resource instanceof NotificationListener)) {
      throw new RuntimeOperationsException(new IllegalArgumentException(listener.getCanonicalName()),
          "MBean " + listener.getCanonicalName() + " does not implement the NotificationListener interface");
    }
    addNotificationListener(name, (NotificationListener) l.resource, filter, handback);
  }

  @Override
  public void removeNotificationListener(ObjectName name, ObjectName listener) throws InstanceNotFoundException,
      ListenerNotFoundException {
    Entry l = entry(listener);
    removeNotificationListener(name, (NotificationListener) l.resource);
  }

  @Override
  public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter,
      Object handback) throws InstanceNotFoundException, ListenerNotFoundException {
    Entry l = entry(listener);
    removeNotificationListener(name, (NotificationListener) l.resource, filter, handback);
  }

  @Override
  public void removeNotificationListener(ObjectName name, NotificationListener listener)
      throws InstanceNotFoundException, ListenerNotFoundException {
    NotificationBroadcaster b = broadcaster(name);
    ObjectName key = fixDomain(name);
    boolean found = false;
    synchronized (registrations) {
      for (int i = registrations.size() - 1; i >= 0; i--) {
        Registration r = registrations.get(i);
        if (r.name.equals(key) && r.listener == listener) {
          registrations.remove(i);
          found = true;
        }
      }
    }
    if (!found) {
      throw new ListenerNotFoundException("Unknown listener");
    }
    b.removeNotificationListener(listener);
  }

  @Override
  public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter,
      Object handback) throws InstanceNotFoundException, ListenerNotFoundException {
    NotificationBroadcaster b = broadcaster(name);
    if (!(b instanceof NotificationEmitter)) {
      throw new RuntimeOperationsException(new IllegalArgumentException(name.toString()),
          "MBean " + name + " does not implement " + NotificationEmitter.class.getName());
    }
    ObjectName key = fixDomain(name);
    boolean found = false;
    synchronized (registrations) {
      for (int i = 0; i < registrations.size(); i++) {
        Registration r = registrations.get(i);
        if (r.name.equals(key) && r.listener == listener && r.filter == filter && r.handback == handback) {
          registrations.remove(i);
          found = true;
          break;
        }
      }
    }
    if (!found) {
      throw new ListenerNotFoundException("Unknown listener");
    }
    ((NotificationEmitter) b).removeNotificationListener(listener, filter, handback);
  }

  @Override
  public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException,
      ReflectionException {
    Entry e = entry(name);
    try {
      MBeanInfo info = e.mbean.getMBeanInfo();
      if (info == null) {
        throw new JMRuntimeExceptionHolder().exception("getMBeanInfo() returned null");
      }
      return info;
    } catch (RuntimeException ex) {
      throw wrapRuntime(ex);
    }
  }

  private static final class JMRuntimeExceptionHolder {
    RuntimeException exception(String message) {
      return new javax.management.JMRuntimeException(message);
    }
  }

  @Override
  public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException {
    Entry e = entry(name);
    if (e.className.equals(className)) {
      return true;
    }
    try {
      Class<?> target = Class.forName(className, false, e.resource.getClass().getClassLoader());
      return target.isInstance(e.resource);
    } catch (ClassNotFoundException ex) {
      return false;
    }
  }

  @Override
  public Object instantiate(String className) throws ReflectionException, MBeanException {
    return instantiate(className, null, null);
  }

  @Override
  public Object instantiate(String className, ObjectName loaderName) throws ReflectionException, MBeanException,
      InstanceNotFoundException {
    return instantiate(className, loaderName, null, null);
  }

  @Override
  public Object instantiate(String className, Object[] params, String[] signature) throws ReflectionException,
      MBeanException {
    return construct(className, ClassLoader.getSystemClassLoader(), params, signature);
  }

  @Override
  public Object instantiate(String className, ObjectName loaderName, Object[] params, String[] signature)
      throws ReflectionException, MBeanException, InstanceNotFoundException {
    ClassLoader loader = loaderName == null ? ClassLoader.getSystemClassLoader() : getClassLoader(loaderName);
    return construct(className, loader, params, signature);
  }

  private Object construct(String className, ClassLoader loader, Object[] params, String[] signature)
      throws ReflectionException, MBeanException {
    if (className == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException("The class name cannot be null"),
          "Exception occurred during MBean creation");
    }
    try {
      Class<?> c = Class.forName(className, true, loader);
      Class<?>[] types = new Class<?>[signature == null ? 0 : signature.length];
      for (int i = 0; i < types.length; i++) {
        types[i] = primitiveOrLoad(signature[i], loader);
      }
      Constructor<?> ctor = c.getConstructor(types);
      return ctor.newInstance(params == null ? new Object[0] : params);
    } catch (ClassNotFoundException ex) {
      throw new ReflectionException(ex, "The MBean class could not be loaded by the default loader repository");
    } catch (NoSuchMethodException ex) {
      throw new ReflectionException(new NoSuchMethodException("No such constructor"));
    } catch (InstantiationException ex) {
      throw new ReflectionException(ex, "The MBean class could not be instantiated");
    } catch (IllegalAccessException ex) {
      throw new ReflectionException(ex, "The MBean class could not be instantiated");
    } catch (InvocationTargetException ex) {
      Throwable t = ex.getTargetException();
      if (t instanceof RuntimeException) {
        throw new RuntimeMBeanException((RuntimeException) t, t.toString());
      }
      if (t instanceof Error) {
        throw new RuntimeErrorException((Error) t, t.toString());
      }
      throw new MBeanException((Exception) t, t.toString());
    }
  }

  private static Class<?> primitiveOrLoad(String name, ClassLoader loader) throws ClassNotFoundException {
    Map<String, Class<?>> prims = new HashMap<String, Class<?>>();
    prims.put("int", int.class);
    prims.put("long", long.class);
    prims.put("boolean", boolean.class);
    prims.put("double", double.class);
    prims.put("float", float.class);
    prims.put("short", short.class);
    prims.put("byte", byte.class);
    prims.put("char", char.class);
    Class<?> p = prims.get(name);
    return p != null ? p : Class.forName(name, false, loader);
  }

  @Override
  public ObjectInstance createMBean(String className, ObjectName name) throws ReflectionException,
      InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
    return createMBean(className, name, (Object[]) null, (String[]) null);
  }

  @Override
  public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName)
      throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
      NotCompliantMBeanException, InstanceNotFoundException {
    return createMBean(className, name, loaderName, null, null);
  }

  @Override
  public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature)
      throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
      NotCompliantMBeanException {
    return registerMBean(instantiate(className, params, signature), name);
  }

  @Override
  public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params,
      String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
      MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
    return registerMBean(instantiate(className, loaderName, params, signature), name);
  }

  @Override
  public ClassLoader getClassLoaderFor(ObjectName mbeanName) throws InstanceNotFoundException {
    return entry(mbeanName).resource.getClass().getClassLoader();
  }

  @Override
  public ClassLoader getClassLoader(ObjectName loaderName) throws InstanceNotFoundException {
    if (loaderName == null) {
      return ClassLoader.getSystemClassLoader();
    }
    Object resource = entry(loaderName).resource;
    if (resource instanceof ClassLoader) {
      return (ClassLoader) resource;
    }
    throw new RuntimeOperationsException(new IllegalArgumentException(loaderName.toString()),
        "MBean " + loaderName + " is not a class loader");
  }

  @Override
  public ClassLoaderRepository getClassLoaderRepository() {
    return new ClassLoaderRepository() {
      @Override
      public Class<?> loadClass(String className) throws ClassNotFoundException {
        return Class.forName(className, false, ClassLoader.getSystemClassLoader());
      }

      @Override
      public Class<?> loadClassWithout(ClassLoader exclude, String className) throws ClassNotFoundException {
        return loadClass(className);
      }

      @Override
      public Class<?> loadClassBefore(ClassLoader stop, String className) throws ClassNotFoundException {
        return loadClass(className);
      }
    };
  }
}
