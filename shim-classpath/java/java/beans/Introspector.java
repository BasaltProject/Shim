/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.beans;

import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.TreeMap;
import java.util.WeakHashMap;

public class Introspector {
  public static final int USE_ALL_BEANINFO = 1;
  public static final int IGNORE_IMMEDIATE_BEANINFO = 2;
  public static final int IGNORE_ALL_BEANINFO = 3;

  static final String ADD_PREFIX = "add";
  static final String REMOVE_PREFIX = "remove";
  static final String GET_PREFIX = "get";
  static final String SET_PREFIX = "set";
  static final String IS_PREFIX = "is";

  private static final Object kLock = new Object();
  private static final Map<Class<?>, SoftReference<BeanInfo>> kCache =
      new WeakHashMap<Class<?>, SoftReference<BeanInfo>>();
  private static String[] searchPath = {"sun.beans.infos"};

  private final Class<?> beanClass;
  private final Class<?> rootClass;
  private final Class<?> stopClass;
  private BeanInfo explicitBeanInfo;
  private BeanInfo superBeanInfo;
  private BeanInfo[] additionalBeanInfo;
  private final int flags;

  private Introspector(Class<?> beanClass, Class<?> rootClass, Class<?> stopClass, int flags)
      throws IntrospectionException {
    this.beanClass = beanClass;
    this.rootClass = rootClass;
    this.stopClass = stopClass;
    this.flags = flags;
    if (flags == USE_ALL_BEANINFO) {
      explicitBeanInfo = findExplicitBeanInfo(beanClass);
    }
    Class<?> superClass = beanClass.getSuperclass();
    if (superClass != stopClass) {
      int newFlags = flags;
      if (newFlags == IGNORE_IMMEDIATE_BEANINFO) {
        newFlags = USE_ALL_BEANINFO;
      }
      superBeanInfo = new Introspector(superClass, rootClass, stopClass, newFlags).build();
    }
    if (explicitBeanInfo != null) {
      additionalBeanInfo = explicitBeanInfo.getAdditionalBeanInfo();
    }
    if (additionalBeanInfo == null) {
      additionalBeanInfo = new BeanInfo[0];
    }
  }

  public static BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {
    if (beanClass == null) {
      throw new NullPointerException();
    }
    synchronized (kLock) {
      SoftReference<BeanInfo> ref = kCache.get(beanClass);
      BeanInfo cached = ref == null ? null : ref.get();
      if (cached != null) {
        return cached;
      }
    }
    BeanInfo info = new Introspector(beanClass, beanClass, null, USE_ALL_BEANINFO).build();
    synchronized (kLock) {
      SoftReference<BeanInfo> ref = kCache.get(beanClass);
      BeanInfo cached = ref == null ? null : ref.get();
      if (cached != null) {
        return cached;
      }
      kCache.put(beanClass, new SoftReference<BeanInfo>(info));
    }
    return info;
  }

  public static BeanInfo getBeanInfo(Class<?> beanClass, int flags) throws IntrospectionException {
    return getBeanInfo(beanClass, null, flags);
  }

  public static BeanInfo getBeanInfo(Class<?> beanClass, Class<?> stopClass) throws IntrospectionException {
    return getBeanInfo(beanClass, stopClass, USE_ALL_BEANINFO);
  }

  public static BeanInfo getBeanInfo(Class<?> beanClass, Class<?> stopClass, int flags)
      throws IntrospectionException {
    if (beanClass == null) {
      throw new NullPointerException();
    }
    if (stopClass == null && flags == USE_ALL_BEANINFO) {
      return getBeanInfo(beanClass);
    }
    if (stopClass != null) {
      boolean isSuper = false;
      for (Class<?> c = beanClass.getSuperclass(); c != null; c = c.getSuperclass()) {
        if (c == stopClass) {
          isSuper = true;
        }
      }
      if (!isSuper) {
        throw new IntrospectionException(stopClass.getName() + " not superclass of " + beanClass.getName());
      }
    }
    return new Introspector(beanClass, beanClass, stopClass, flags).build();
  }

  public static String decapitalize(String name) {
    if (name == null || name.length() == 0) {
      return name;
    }
    if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0))) {
      return name;
    }
    char[] chars = name.toCharArray();
    chars[0] = Character.toLowerCase(chars[0]);
    return new String(chars);
  }

  public static String[] getBeanInfoSearchPath() {
    synchronized (kLock) {
      return searchPath.clone();
    }
  }

  public static void setBeanInfoSearchPath(String[] path) {
    synchronized (kLock) {
      searchPath = path == null ? null : path.clone();
    }
  }

  public static void flushCaches() {
    synchronized (kLock) {
      kCache.clear();
    }
  }

  public static void flushFromCaches(Class<?> clz) {
    if (clz == null) {
      throw new NullPointerException();
    }
    synchronized (kLock) {
      kCache.remove(clz);
    }
  }

  static Method findMethod(Class<?> cls, String methodName, int argCount) {
    return findMethod(cls, methodName, argCount, null);
  }

  static Method findMethod(Class<?> cls, String methodName, int argCount, Class<?>[] args) {
    if (methodName == null) {
      return null;
    }
    Method found = null;
    for (Method m : cls.getMethods()) {
      if (!m.getName().equals(methodName) || m.getParameterTypes().length != argCount) {
        continue;
      }
      if (args != null) {
        Class<?>[] params = m.getParameterTypes();
        boolean ok = true;
        for (int i = 0; i < args.length; i++) {
          if (args[i] != null && !args[i].equals(params[i])) {
            ok = false;
          }
        }
        if (!ok) {
          continue;
        }
      }
      if (found == null || (found.isBridge() && !m.isBridge())) {
        found = m;
      }
    }
    return found;
  }

  private static BeanInfo findExplicitBeanInfo(Class<?> beanClass) {
    ClassLoader loader = beanClass.getClassLoader();
    String name = beanClass.getName() + "BeanInfo";
    BeanInfo found = instantiate(name, loader);
    if (found != null) {
      return found;
    }
    String simple = beanClass.getName();
    simple = simple.substring(simple.lastIndexOf('.') + 1);
    String[] path;
    synchronized (kLock) {
      path = searchPath == null ? null : searchPath.clone();
    }
    if (path != null) {
      for (String pkg : path) {
        found = instantiate(pkg + "." + simple + "BeanInfo", loader);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  private static BeanInfo instantiate(String className, ClassLoader loader) {
    try {
      Class<?> type;
      try {
        type = Class.forName(className, false, loader);
      } catch (ClassNotFoundException ex) {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        if (context == null || context == loader) {
          return null;
        }
        type = Class.forName(className, false, context);
      }
      if (!BeanInfo.class.isAssignableFrom(type)) {
        return null;
      }
      return (BeanInfo) type.getDeclaredConstructor().newInstance();
    } catch (Throwable ex) {
      return null;
    }
  }

  private static boolean hasPropertyChangeSupport(Class<?> cls) {
    return findMethod(cls, "addPropertyChangeListener", 1, new Class<?>[] {PropertyChangeListener.class}) != null
        && findMethod(cls, "removePropertyChangeListener", 1, new Class<?>[] {PropertyChangeListener.class}) != null;
  }

  private BeanInfo build() throws IntrospectionException {
    BeanDescriptor bd = targetBeanDescriptor();
    MethodDescriptor[] methods = targetMethods();
    EventSetDescriptor[] events = targetEvents();
    PropertyDescriptor[] properties = targetProperties();
    JavaBean javaBean = beanClass.getAnnotation(JavaBean.class);
    int defaultEvent = -1;
    int defaultProperty = -1;
    String defaultEventName = null;
    String defaultPropertyName = null;
    if (explicitBeanInfo != null) {
      PropertyDescriptor[] explicit = explicitBeanInfo.getPropertyDescriptors();
      int index = explicitBeanInfo.getDefaultPropertyIndex();
      if (explicit != null && index >= 0 && index < explicit.length) {
        defaultPropertyName = explicit[index].getName();
      }
      EventSetDescriptor[] explicitEvents = explicitBeanInfo.getEventSetDescriptors();
      int eventIndex = explicitBeanInfo.getDefaultEventIndex();
      if (explicitEvents != null && eventIndex >= 0 && eventIndex < explicitEvents.length) {
        defaultEventName = explicitEvents[eventIndex].getName();
      }
    } else if (superBeanInfo != null) {
      PropertyDescriptor[] superProps = superBeanInfo.getPropertyDescriptors();
      int index = superBeanInfo.getDefaultPropertyIndex();
      if (superProps != null && index >= 0 && index < superProps.length) {
        defaultPropertyName = superProps[index].getName();
      }
      EventSetDescriptor[] superEvents = superBeanInfo.getEventSetDescriptors();
      int eventIndex = superBeanInfo.getDefaultEventIndex();
      if (superEvents != null && eventIndex >= 0 && eventIndex < superEvents.length) {
        defaultEventName = superEvents[eventIndex].getName();
      }
    }
    if (javaBean != null) {
      if (!javaBean.defaultProperty().isEmpty()) {
        defaultPropertyName = javaBean.defaultProperty();
      }
      if (!javaBean.defaultEventSet().isEmpty()) {
        defaultEventName = javaBean.defaultEventSet();
      }
    }
    if (defaultPropertyName != null) {
      for (int i = 0; i < properties.length; i++) {
        if (properties[i].getName().equals(defaultPropertyName)) {
          defaultProperty = i;
          break;
        }
      }
    }
    if (defaultEventName != null) {
      for (int i = 0; i < events.length; i++) {
        if (events[i].getName().equals(defaultEventName)) {
          defaultEvent = i;
          break;
        }
      }
    }
    return new GenericBeanInfo(bd, events, defaultEvent, properties, defaultProperty, methods);
  }

  private BeanDescriptor targetBeanDescriptor() {
    BeanDescriptor bd = null;
    if (explicitBeanInfo != null) {
      bd = explicitBeanInfo.getBeanDescriptor();
    }
    if (bd == null) {
      bd = new BeanDescriptor(beanClass, null);
      JavaBean javaBean = beanClass.getAnnotation(JavaBean.class);
      if (javaBean != null) {
        if (!javaBean.description().isEmpty()) {
          bd.setShortDescription(javaBean.description());
        }
        bd.setPreferred(true);
      }
    }
    return bd;
  }

  private boolean excluded(Class<?> declaring) {
    return stopClass != null && declaring.isAssignableFrom(stopClass);
  }

  private static String methodKey(Method m) {
    StringBuilder sb = new StringBuilder(m.getName()).append('(');
    for (Class<?> p : m.getParameterTypes()) {
      sb.append(p.getName()).append(',');
    }
    return sb.append(')').toString();
  }

  /** Public methods this class level contributes: declared ones plus inherited interface defaults. */
  private List<Method> levelMethods(Class<?> c, boolean forDescriptors) {
    Map<String, Method> byKey = new LinkedHashMap<String, Method>();
    List<Method> declared = new ArrayList<Method>();
    for (Method m : c.getDeclaredMethods()) {
      if (Modifier.isPublic(m.getModifiers())) {
        declared.add(m);
      }
    }
    for (Method m : declared) {
      if (m.isBridge()) {
        boolean shadowed = false;
        for (Method other : declared) {
          if (!other.isBridge() && other.getName().equals(m.getName())
              && Arrays.equals(other.getParameterTypes(), m.getParameterTypes())) {
            shadowed = true;
            break;
          }
        }
        if (shadowed || inheritedAccessibly(c, m, forDescriptors)) {
          continue;
        }
      }
      String key = methodKey(m);
      Method old = byKey.get(key);
      if (old == null || (old.isBridge() && !m.isBridge())) {
        byKey.put(key, m);
      }
    }
    List<Class<?>> interfaces = new ArrayList<Class<?>>();
    collectInterfaces(c, interfaces);
    for (Class<?> iface : interfaces) {
      for (Method m : iface.getDeclaredMethods()) {
        if (!Modifier.isPublic(m.getModifiers()) || Modifier.isAbstract(m.getModifiers())) {
          continue;
        }
        String key = methodKey(m);
        Method old = byKey.get(key);
        if (old == null) {
          byKey.put(key, m);
        } else if (old.getDeclaringClass().isInterface() && old.getDeclaringClass() != iface
            && old.getDeclaringClass().isAssignableFrom(iface)) {
          byKey.put(key, m);
        }
      }
    }
    List<Method> result = new ArrayList<Method>();
    for (Method m : byKey.values()) {
      if (!excluded(m.getDeclaringClass())) {
        result.add(m);
      }
    }
    return result;
  }

  private boolean inheritedAccessibly(Class<?> c, Method bridge, boolean anyVisibility) {
    for (Class<?> s = c.getSuperclass(); s != null; s = s.getSuperclass()) {
      if (excluded(s) || (!anyVisibility && !Modifier.isPublic(s.getModifiers()))) {
        continue;
      }
      for (Method m : s.getDeclaredMethods()) {
        if (Modifier.isPublic(m.getModifiers()) && !m.isBridge() && m.getName().equals(bridge.getName())
            && Arrays.equals(m.getParameterTypes(), bridge.getParameterTypes())) {
          return true;
        }
      }
    }
    return false;
  }

  private static void collectInterfaces(Class<?> c, List<Class<?>> out) {
    for (Class<?> iface : c.getInterfaces()) {
      if (!out.contains(iface)) {
        out.add(iface);
      }
      collectInterfaces(iface, out);
    }
  }

  private MethodDescriptor[] targetMethods() {
    MethodDescriptor[] explicit = explicitBeanInfo == null ? null : explicitBeanInfo.getMethodDescriptors();
    Map<String, MethodDescriptor> methods = new HashMap<String, MethodDescriptor>();
    if (explicit == null && superBeanInfo != null) {
      addMethods(methods, superBeanInfo.getMethodDescriptors());
    }
    for (BeanInfo extra : additionalBeanInfo) {
      addMethods(methods, extra.getMethodDescriptors());
    }
    if (explicit != null) {
      addMethods(methods, explicit);
    } else {
      for (Method m : levelMethods(beanClass, true)) {
        methods.put(methodKey(m), new MethodDescriptor(m));
      }
    }
    return methods.values().toArray(new MethodDescriptor[0]);
  }

  private static void addMethods(Map<String, MethodDescriptor> map, MethodDescriptor[] mds) {
    if (mds == null) {
      return;
    }
    for (MethodDescriptor md : mds) {
      map.put(methodKey(md.getMethod()), md);
    }
  }

  private EventSetDescriptor[] targetEvents() throws IntrospectionException {
    EventSetDescriptor[] explicit = explicitBeanInfo == null ? null : explicitBeanInfo.getEventSetDescriptors();
    Map<String, EventSetDescriptor> events = new HashMap<String, EventSetDescriptor>();
    if (explicit == null && superBeanInfo != null) {
      addEvents(events, superBeanInfo.getEventSetDescriptors());
    }
    for (BeanInfo extra : additionalBeanInfo) {
      addEvents(events, extra.getEventSetDescriptors());
    }
    if (explicit != null) {
      addEvents(events, explicit);
    } else {
      List<Method> methods = levelMethods(beanClass, false);
      Map<String, Method> adders = new TreeMap<String, Method>();
      Map<String, Method> removers = new HashMap<String, Method>();
      Map<String, Method> getters = new HashMap<String, Method>();
      for (Method m : methods) {
        if (Modifier.isStatic(m.getModifiers())) {
          continue;
        }
        String name = m.getName();
        Class<?>[] params = m.getParameterTypes();
        if (name.startsWith(ADD_PREFIX) && params.length == 1 && m.getReturnType() == void.class) {
          adders.put(name, m);
        } else if (name.startsWith(REMOVE_PREFIX) && params.length == 1 && m.getReturnType() == void.class) {
          removers.put(name, m);
        } else if (name.startsWith(GET_PREFIX) && params.length == 0 && m.getReturnType().isArray()) {
          getters.put(name, m);
        }
      }
      for (Map.Entry<String, Method> e : adders.entrySet()) {
        Method add = e.getValue();
        Class<?> listenerType = add.getParameterTypes()[0];
        String name = e.getKey();
        if (!EventListener.class.isAssignableFrom(listenerType) || !name.endsWith("Listener")
            || !name.equals(ADD_PREFIX + listenerType.getSimpleName())) {
          continue;
        }
        String base = name.substring(ADD_PREFIX.length());
        Method remove = removers.get(REMOVE_PREFIX + base);
        if (remove == null || remove.getParameterTypes()[0] != listenerType) {
          continue;
        }
        Method get = getters.get(GET_PREFIX + base + "s");
        if (get != null && get.getReturnType().getComponentType() != listenerType) {
          get = null;
        }
        String eventName = decapitalize(base.substring(0, base.length() - "Listener".length()));
        if (eventName.isEmpty()) {
          continue;
        }
        List<Method> listenerMethods = new ArrayList<Method>();
        for (Method lm : listenerType.getMethods()) {
          Class<?>[] lp = lm.getParameterTypes();
          if (lp.length == 1 && lm.getReturnType() == void.class && EventObject.class.isAssignableFrom(lp[0])
              && !Modifier.isStatic(lm.getModifiers())) {
            listenerMethods.add(lm);
          }
        }
        EventSetDescriptor esd = new EventSetDescriptor(eventName, listenerType,
            listenerMethods.toArray(new Method[0]), add, remove, get);
        esd.setUnicast(false);
        for (Class<?> ex : add.getExceptionTypes()) {
          if (ex == TooManyListenersException.class) {
            esd.setUnicast(true);
          }
        }
        events.put(eventName, esd);
      }
    }
    return events.values().toArray(new EventSetDescriptor[0]);
  }

  private static void addEvents(Map<String, EventSetDescriptor> map, EventSetDescriptor[] esds) {
    if (esds == null) {
      return;
    }
    for (EventSetDescriptor esd : esds) {
      map.put(esd.getName(), esd);
    }
  }

  private PropertyDescriptor[] targetProperties() throws IntrospectionException {
    PropertyDescriptor[] explicit = explicitBeanInfo == null ? null : explicitBeanInfo.getPropertyDescriptors();
    Map<String, PropertyDescriptor> properties = new TreeMap<String, PropertyDescriptor>();
    if (explicit == null && superBeanInfo != null) {
      addProperties(properties, superBeanInfo.getPropertyDescriptors());
    }
    for (BeanInfo extra : additionalBeanInfo) {
      addProperties(properties, extra.getPropertyDescriptors());
    }
    if (explicit != null) {
      addProperties(properties, explicit);
    } else {
      for (PropertyDescriptor pd : declaredProperties()) {
        PropertyDescriptor inherited = properties.get(pd.getName());
        properties.put(pd.getName(), inherited == null ? pd : merge(pd, inherited));
      }
    }
    return properties.values().toArray(new PropertyDescriptor[0]);
  }

  private static void addProperties(Map<String, PropertyDescriptor> map, PropertyDescriptor[] pds) {
    if (pds == null) {
      return;
    }
    for (PropertyDescriptor pd : pds) {
      PropertyDescriptor old = map.get(pd.getName());
      map.put(pd.getName(), old == null ? pd : merge(pd, old));
    }
  }

  private static boolean compatible(Class<?> readType, Class<?> writeType) {
    return readType.isAssignableFrom(writeType);
  }

  private static PropertyDescriptor merge(PropertyDescriptor child, PropertyDescriptor parent) {
    Method read = child.getReadMethod();
    Method write = child.getWriteMethod();
    Method iread = null;
    Method iwrite = null;
    Class<?> itype = null;
    if (child instanceof IndexedPropertyDescriptor) {
      IndexedPropertyDescriptor ic = (IndexedPropertyDescriptor) child;
      iread = ic.getIndexedReadMethod();
      iwrite = ic.getIndexedWriteMethod();
      itype = ic.getIndexedPropertyType();
    }
    Class<?> plainType = child.getPropertyType();
    boolean changed = false;
    Method parentRead = parent.getReadMethod();
    Method parentWrite = parent.getWriteMethod();
    Class<?> parentReadType = parentRead == null ? null : parent.getPropertyType();
    if (read == null && parentRead != null && (write == null || compatible(parentReadType, plainType))) {
      read = parentRead;
      plainType = parentReadType;
      changed = true;
    }
    if (write == null && parentWrite != null) {
      Class<?> wt = parentRead == null ? parent.getPropertyType() : parentWrite.getParameterTypes()[0];
      if (read == null ? plainType == null : compatible(plainType, wt)) {
        write = parentWrite;
        if (plainType == null) {
          plainType = wt;
        }
        changed = true;
      }
    }
    if (parent instanceof IndexedPropertyDescriptor) {
      IndexedPropertyDescriptor ip = (IndexedPropertyDescriptor) parent;
      if (iread == null && ip.getIndexedReadMethod() != null
          && (itype == null || itype == ip.getIndexedPropertyType())) {
        iread = ip.getIndexedReadMethod();
        itype = ip.getIndexedPropertyType();
        changed = true;
      }
      if (iwrite == null && ip.getIndexedWriteMethod() != null
          && (itype == null || itype == ip.getIndexedPropertyType())) {
        iwrite = ip.getIndexedWriteMethod();
        itype = ip.getIndexedPropertyType();
        changed = true;
      }
    }
    if (!changed) {
      return child;
    }
    Class<?> owner = child.getClass0();
    PropertyDescriptor result;
    if (itype != null && (plainType == null || (plainType.isArray() && plainType.getComponentType() == itype))) {
      result = new IndexedPropertyDescriptor(owner, child.getName(), read, write, iread, iwrite);
      result.setTypes(plainType, itype);
    } else {
      result = new PropertyDescriptor(owner, child.getName(), read, write);
      result.setTypes(plainType, null);
    }
    result.copyFrom(child);
    return result;
  }

  private Class<?> returnType(Method m) {
    return erase(resolve(m.getGenericReturnType(), m.getDeclaringClass()), m.getReturnType());
  }

  private Class<?>[] paramTypes(Method m) {
    Type[] generic = m.getGenericParameterTypes();
    Class<?>[] raw = m.getParameterTypes();
    if (generic.length != raw.length) {
      return raw;
    }
    Class<?>[] result = new Class<?>[raw.length];
    for (int i = 0; i < raw.length; i++) {
      result[i] = erase(resolve(generic[i], m.getDeclaringClass()), raw[i]);
    }
    return result;
  }

  private Type resolve(Type type, Class<?> declaring) {
    if (type instanceof TypeVariable) {
      TypeVariable<?> variable = (TypeVariable<?>) type;
      if (variable.getGenericDeclaration() instanceof Class && declaring != rootClass) {
        Type actual = actualArgument(rootClass, (Class<?>) variable.getGenericDeclaration(), variable,
            new HashMap<TypeVariable<?>, Type>());
        if (actual != null) {
          return actual;
        }
      }
      return type;
    }
    if (type instanceof GenericArrayType) {
      Type component = resolve(((GenericArrayType) type).getGenericComponentType(), declaring);
      Class<?> erased = erase(component, Object.class);
      return Array.newInstance(erased, 0).getClass();
    }
    return type;
  }

  private static Type actualArgument(Type start, Class<?> target, TypeVariable<?> variable,
      Map<TypeVariable<?>, Type> bindings) {
    Class<?> raw;
    if (start instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) start;
      raw = (Class<?>) pt.getRawType();
      TypeVariable<?>[] params = raw.getTypeParameters();
      Type[] args = pt.getActualTypeArguments();
      for (int i = 0; i < params.length && i < args.length; i++) {
        Type arg = args[i];
        if (arg instanceof TypeVariable && bindings.containsKey(arg)) {
          arg = bindings.get(arg);
        }
        bindings.put(params[i], arg);
      }
    } else if (start instanceof Class) {
      raw = (Class<?>) start;
    } else {
      return null;
    }
    if (raw == target) {
      return bindings.get(variable);
    }
    List<Type> supers = new ArrayList<Type>();
    if (raw.getGenericSuperclass() != null) {
      supers.add(raw.getGenericSuperclass());
    }
    supers.addAll(Arrays.asList(raw.getGenericInterfaces()));
    for (Type sup : supers) {
      Type found = actualArgument(sup, target, variable, new HashMap<TypeVariable<?>, Type>(bindings));
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private static Class<?> erase(Type type, Class<?> fallback) {
    if (type instanceof Class) {
      return (Class<?>) type;
    }
    if (type instanceof ParameterizedType) {
      return (Class<?>) ((ParameterizedType) type).getRawType();
    }
    if (type instanceof TypeVariable) {
      Type[] bounds = ((TypeVariable<?>) type).getBounds();
      return bounds.length == 0 ? Object.class : erase(bounds[0], Object.class);
    }
    if (type instanceof WildcardType) {
      Type[] bounds = ((WildcardType) type).getUpperBounds();
      return bounds.length == 0 ? Object.class : erase(bounds[0], Object.class);
    }
    if (type instanceof GenericArrayType) {
      return Array.newInstance(erase(((GenericArrayType) type).getGenericComponentType(), Object.class), 0)
          .getClass();
    }
    return fallback;
  }

  private static final class Builder {
    final String name;
    final List<Method> reads = new ArrayList<Method>();
    final List<Method> writes = new ArrayList<Method>();
    final List<Method> ireads = new ArrayList<Method>();
    final List<Method> iwrites = new ArrayList<Method>();

    Builder(String name) {
      this.name = name;
    }
  }

  private static Method greaterName(List<Method> methods) {
    Method best = null;
    for (Method m : methods) {
      if (best == null || m.getName().compareTo(best.getName()) > 0) {
        best = m;
      }
    }
    return best;
  }

  private Method chooseWrite(List<Method> writes, Class<?> readType) {
    Method best = null;
    for (Method m : writes) {
      Class<?> type = paramTypes(m)[0];
      if (readType != null && !compatible(readType, type)) {
        continue;
      }
      if (best == null) {
        best = m;
        continue;
      }
      Class<?> bestType = paramTypes(best)[0];
      if (type == bestType) {
        if (m.getName().compareTo(best.getName()) > 0) {
          best = m;
        }
      } else if (bestType.isAssignableFrom(type)) {
        best = m;
      } else if (!type.isAssignableFrom(bestType) && type.getName().compareTo(bestType.getName()) < 0) {
        best = m;
      }
    }
    return best;
  }

  private List<PropertyDescriptor> declaredProperties() throws IntrospectionException {
    Map<String, Builder> builders = new TreeMap<String, Builder>();
    for (Method m : levelMethods(beanClass, false)) {
      if (Modifier.isStatic(m.getModifiers())) {
        continue;
      }
      String name = m.getName();
      Class<?>[] params = paramTypes(m);
      Class<?> returnType = returnType(m);
      String base = null;
      int kind = 0;
      if (params.length == 0) {
        if (name.startsWith(GET_PREFIX) && name.length() > 3 && returnType != void.class) {
          base = name.substring(3);
          kind = 1;
        } else if (name.startsWith(IS_PREFIX) && name.length() > 2 && returnType == boolean.class) {
          base = name.substring(2);
          kind = 1;
        }
      } else if (params.length == 1) {
        if (name.startsWith(GET_PREFIX) && name.length() > 3 && params[0] == int.class && returnType != void.class) {
          base = name.substring(3);
          kind = 3;
        } else if (name.startsWith(SET_PREFIX) && name.length() > 3 && returnType == void.class) {
          base = name.substring(3);
          kind = 2;
        }
      } else if (params.length == 2 && name.startsWith(SET_PREFIX) && name.length() > 3 && params[0] == int.class
          && returnType == void.class) {
        base = name.substring(3);
        kind = 4;
      }
      if (base == null) {
        continue;
      }
      String propName = decapitalize(base);
      Builder b = builders.get(propName);
      if (b == null) {
        b = new Builder(propName);
        builders.put(propName, b);
      }
      if (kind == 1) {
        b.reads.add(m);
      } else if (kind == 2) {
        b.writes.add(m);
      } else if (kind == 3) {
        b.ireads.add(m);
      } else {
        b.iwrites.add(m);
      }
    }
    boolean bound = hasPropertyChangeSupport(beanClass);
    List<PropertyDescriptor> result = new ArrayList<PropertyDescriptor>();
    for (Builder b : builders.values()) {
      PropertyDescriptor pd = describe(b, bound);
      if (pd != null) {
        result.add(pd);
      }
    }
    return result;
  }

  private Method chooseRead(List<Method> reads) {
    List<Method> is = new ArrayList<Method>();
    List<Method> get = new ArrayList<Method>();
    for (Method m : reads) {
      (m.getName().startsWith(IS_PREFIX) && returnType(m) == boolean.class ? is : get).add(m);
    }
    return greaterName(is.isEmpty() ? get : is);
  }

  private PropertyDescriptor describe(Builder b, boolean classBound) throws IntrospectionException {
    Method read = chooseRead(b.reads);
    Class<?> readType = read == null ? null : returnType(read);
    Method write = chooseWrite(b.writes, readType);
    Class<?> plainType = read != null ? readType : write != null ? paramTypes(write)[0] : null;

    Method iread = null;
    Method iwrite = null;
    Class<?> itype = null;
    if (!b.ireads.isEmpty() || !b.iwrites.isEmpty()) {
      iread = greaterName(b.ireads);
      if (iread != null) {
        itype = returnType(iread);
      }
      List<Method> candidates = new ArrayList<Method>();
      for (Method m : b.iwrites) {
        Class<?> t = paramTypes(m)[1];
        if (itype == null || itype == t) {
          candidates.add(m);
        }
      }
      iwrite = greaterName(candidates);
      if (itype == null && iwrite != null) {
        itype = paramTypes(iwrite)[1];
      }
      if (itype != null && plainType != null && !(plainType.isArray() && plainType.getComponentType() == itype)) {
        iread = null;
        iwrite = null;
        itype = null;
      }
    }
    if (read == null && write == null && itype == null) {
      return null;
    }
    PropertyDescriptor pd;
    if (itype != null) {
      pd = new IndexedPropertyDescriptor(beanClass, b.name, read, write, iread, iwrite);
      pd.setTypes(plainType, itype);
    } else {
      pd = new PropertyDescriptor(beanClass, b.name, read, write);
      pd.setTypes(plainType, null);
    }
    Method primary = read != null ? read : write != null ? write : iread != null ? iread : iwrite;
    boolean levelBound = primary != null && hasPropertyChangeSupport(primary.getDeclaringClass()) || classBound && primary == null;
    pd.setBound(levelBound);
    if (write != null) {
      for (Class<?> ex : write.getExceptionTypes()) {
        if (ex == PropertyVetoException.class) {
          pd.setConstrained(true);
        }
      }
    }
    applyAnnotations(pd, read != null ? read : write, iread != null ? iread : iwrite, levelBound);
    return pd;
  }

  private void applyAnnotations(PropertyDescriptor pd, Method plain, Method indexed, boolean levelBound) {
    BeanProperty plainAnnotation = annotationOf(pd.getReadMethod(), pd.getWriteMethod());
    BeanProperty flagAnnotation = plainAnnotation;
    if (pd instanceof IndexedPropertyDescriptor) {
      IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
      BeanProperty indexedAnnotation = annotationOf(ipd.getIndexedReadMethod(), ipd.getIndexedWriteMethod());
      if (indexedAnnotation != null) {
        flagAnnotation = indexedAnnotation;
        if (plainAnnotation == null) {
          plainAnnotation = indexedAnnotation;
        }
      }
    }
    Transient transientAnnotation = null;
    if (pd.getReadMethod() != null && pd.getReadMethod().getAnnotation(Transient.class) != null) {
      transientAnnotation = pd.getReadMethod().getAnnotation(Transient.class);
    } else if (pd.getWriteMethod() != null && pd.getWriteMethod().getAnnotation(Transient.class) != null) {
      transientAnnotation = pd.getWriteMethod().getAnnotation(Transient.class);
    }
    boolean expert = false;
    boolean hidden = false;
    boolean preferred = false;
    boolean required = false;
    boolean visualUpdate = false;
    Object[] enumeration = new Object[0];
    if (plainAnnotation != null) {
      if (levelBound) {
        pd.setBound(plainAnnotation.bound());
      }
      if (!plainAnnotation.description().isEmpty()) {
        pd.setShortDescription(plainAnnotation.description());
      }
    }
    if (flagAnnotation != null) {
      expert = flagAnnotation.expert();
      hidden = flagAnnotation.hidden();
      preferred = flagAnnotation.preferred();
      required = flagAnnotation.required();
      visualUpdate = flagAnnotation.visualUpdate();
      enumeration = enumerationValues(flagAnnotation.enumerationValues());
    }
    pd.setValue("required", Boolean.valueOf(required));
    pd.setValue("visualUpdate", Boolean.valueOf(visualUpdate));
    pd.setValue("enumerationValues", enumeration);
    pd.setValue("expert", Boolean.valueOf(expert));
    pd.setValue("hidden", Boolean.valueOf(hidden));
    pd.setExpert(expert);
    pd.setHidden(hidden);
    pd.setPreferred(preferred);
    if (transientAnnotation != null) {
      pd.setValue("transient", Boolean.valueOf(transientAnnotation.value()));
    }
  }

  private static BeanProperty annotationOf(Method first, Method second) {
    if (first != null && first.getAnnotation(BeanProperty.class) != null) {
      return first.getAnnotation(BeanProperty.class);
    }
    if (second != null && second.getAnnotation(BeanProperty.class) != null) {
      return second.getAnnotation(BeanProperty.class);
    }
    return null;
  }

  private Object[] enumerationValues(String[] names) {
    if (names.length == 0) {
      return new Object[0];
    }
    Set<Object> seen = new HashSet<Object>();
    Object[] result = new Object[names.length * 3];
    int n = 0;
    for (String name : names) {
      try {
        int dot = name.lastIndexOf('.');
        String className = name.substring(0, dot);
        String fieldName = name.substring(dot + 1);
        Class<?> owner = Class.forName(className, true, beanClass.getClassLoader());
        Field field = owner.getField(fieldName);
        if (!Modifier.isStatic(field.getModifiers())) {
          return new Object[0];
        }
        Object value = field.get(null);
        result[n++] = fieldName;
        result[n++] = value;
        result[n++] = name;
        seen.add(value);
      } catch (Exception e) {
        e.printStackTrace();
        return new Object[0];
      }
    }
    return result;
  }

  private static final class GenericBeanInfo extends SimpleBeanInfo {
    private final BeanDescriptor beanDescriptor;
    private final EventSetDescriptor[] events;
    private final int defaultEvent;
    private final PropertyDescriptor[] properties;
    private final int defaultProperty;
    private final MethodDescriptor[] methods;

    GenericBeanInfo(BeanDescriptor beanDescriptor, EventSetDescriptor[] events, int defaultEvent,
        PropertyDescriptor[] properties, int defaultProperty, MethodDescriptor[] methods) {
      this.beanDescriptor = beanDescriptor;
      this.events = events;
      this.defaultEvent = defaultEvent;
      this.properties = properties;
      this.defaultProperty = defaultProperty;
      this.methods = methods;
    }

    @Override
    public BeanDescriptor getBeanDescriptor() {
      return beanDescriptor;
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
      return properties;
    }

    @Override
    public int getDefaultPropertyIndex() {
      return defaultProperty;
    }

    @Override
    public EventSetDescriptor[] getEventSetDescriptors() {
      return events;
    }

    @Override
    public int getDefaultEventIndex() {
      return defaultEvent;
    }

    @Override
    public MethodDescriptor[] getMethodDescriptors() {
      return methods;
    }
  }
}
