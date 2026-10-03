/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.beans;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.Namespace;
import jdk.dynalink.NamedOperation;
import jdk.dynalink.NamespaceOperation;
import jdk.dynalink.Operation;
import jdk.dynalink.StandardNamespace;
import jdk.dynalink.StandardOperation;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.TypeBasedGuardingDynamicLinker;
import jdk.dynalink.linker.support.Guards;

final class BeanLinker implements TypeBasedGuardingDynamicLinker {
  private static final MethodHandle INVOKE_STATIC_CALL = find("invokeCallee", Object.class, Object[].class);
  private static final MethodHandle GET_ELEMENT = find("getElement", Object.class, Object.class, Object.class);
  private static final MethodHandle SET_ELEMENT = find("setElement", void.class, Object.class, Object.class, Object.class);
  private static final MethodHandle REMOVE_ELEMENT = find("removeElement", Object.class, Object.class, Object.class);
  private static final MethodHandle GET_PROP = find("getProp", Object.class, ClassInfo.class, boolean.class, Object.class, Object.class);
  private static final MethodHandle SET_PROP = find("setProp", void.class, ClassInfo.class, boolean.class, Object.class, Object.class, Object.class);
  private static final MethodHandle GET_METHOD = find("getMethodObj", Object.class, ClassInfo.class, boolean.class, Object.class, Object.class);

  private static MethodHandle find(String name, Class<?> rtype, Class<?>... ptypes) {
    try {
      return MethodHandles.lookup().findStatic(BeanLinker.class, name, MethodType.methodType(rtype, ptypes));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  private final Class<?> clazz;
  private final MissingMemberHandlerFactory missingMemberHandlerFactory;

  BeanLinker(Class<?> clazz, MissingMemberHandlerFactory factory) {
    this.clazz = clazz;
    this.missingMemberHandlerFactory = factory;
  }

  @Override
  public boolean canLinkType(Class<?> type) {
    return type == clazz;
  }

  @Override
  public GuardedInvocation getGuardedInvocation(LinkRequest request, LinkerServices services) throws Exception {
    final CallSiteDescriptor desc = request.getCallSiteDescriptor();
    final Operation full = desc.getOperation();
    final Object name = NamedOperation.getName(full);
    final Operation unnamed = NamedOperation.getBaseOperation(full);
    final Namespace[] namespaces = NamespaceOperation.getNamespaces(unnamed);
    final Operation base = NamespaceOperation.getBaseOperation(unnamed);
    if (!(base instanceof StandardOperation)) {
      return null;
    }
    final MethodType type = desc.getMethodType();
    final Object receiver = request.getReceiver();
    final boolean isStatic = receiver instanceof StaticClass;
    final boolean isMethod = receiver instanceof JavaMethods;
    final Class<?> target = isStatic ? ((StaticClass) receiver).getRepresentedClass() : clazz;
    final ClassInfo info = ClassInfo.of(target);
    final MethodHandle guard = isStatic ? Guards.asType(Guards.getIdentityGuard(receiver), type.changeReturnType(boolean.class).dropParameterTypes(1, type.parameterCount()))
        : isMethod ? Guards.asType(Guards.getIdentityGuard(receiver), type.changeReturnType(boolean.class).dropParameterTypes(1, type.parameterCount()))
        : Guards.isOfClass(clazz, type);
    switch ((StandardOperation) base) {
      case GET:
      case SET:
      case REMOVE: {
        if (isMethod) {
          return null;
        }
        for (Namespace ns : namespaces) {
          final MethodHandle mh = linkNamespace((StandardOperation) base, ns, name, type, info, isStatic, target, receiver);
          if (mh != null) {
            return new GuardedInvocation(services.asType(mh, type), guard);
          }
        }
        if (missingMemberHandlerFactory != null) {
          final MethodHandle h = missingMemberHandlerFactory.createMissingMemberHandler(request, services);
          if (h != null) {
            return new GuardedInvocation(h, guard);
          }
        }
        return null;
      }
      case CALL: {
        if (!isMethod) {
          return null;
        }
        MethodHandle h = INVOKE_STATIC_CALL.asCollector(Object[].class, type.parameterCount());
        return new GuardedInvocation(services.asType(h, type), guard);
      }
      case NEW: {
        if (isStatic) {
          final List<Constructor<?>> ctors = info.constructors;
          if (ctors.isEmpty()) {
            return null;
          }
          final JavaMethods.ConstructorGroup group = new JavaMethods.ConstructorGroup(target.getName(), ctors);
          MethodHandle h = MethodHandles.insertArguments(NEW_INSTANCE_WITH, 0, group).asCollector(Object[].class,
              type.parameterCount() - 1);
          h = MethodHandles.dropArguments(h, 0, Object.class);
          return new GuardedInvocation(services.asType(h, type), guard);
        } else if (isMethod && ((JavaMethods) receiver) instanceof JavaMethods.ConstructorGroup) {
          final MethodHandle h = CALLEE_NEW.asCollector(Object[].class, type.parameterCount());
          return new GuardedInvocation(services.asType(h, type), guard);
        }
        return null;
      }
      default:
        return null;
    }
  }

  private static final MethodHandle NEW_INSTANCE_WITH = find("newInstanceWith", Object.class, JavaMethods.class, Object[].class);
  private static final MethodHandle CALLEE_NEW = find("calleeNew", Object.class, Object[].class);

  private MethodHandle linkNamespace(StandardOperation op, Namespace ns, Object name, MethodType type, ClassInfo info,
      boolean isStatic, Class<?> target, Object receiver) {
    final boolean dynamicName = name == null;
    if (ns == StandardNamespace.PROPERTY) {
      if (op == StandardOperation.GET) {
        final MethodHandle bound = MethodHandles.insertArguments(GET_PROP, 0, info, isStatic);
        if (dynamicName) {
          return bound;
        }
        final String n = String.valueOf(name);
        if (!hasReadable(info, n, isStatic, target)) {
          return null;
        }
        return MethodHandles.insertArguments(bound, 1, n);
      } else if (op == StandardOperation.SET) {
        final MethodHandle bound = MethodHandles.insertArguments(SET_PROP, 0, info, isStatic);
        if (dynamicName) {
          return bound;
        }
        final String n = String.valueOf(name);
        if (!hasWritable(info, n, isStatic)) {
          return null;
        }
        return MethodHandles.insertArguments(bound, 1, n);
      }
      return null;
    }
    if (ns == StandardNamespace.ELEMENT) {
      if (isStatic) {
        return null;
      }
      final boolean array = target.isArray();
      final boolean list = java.util.List.class.isAssignableFrom(target);
      final boolean map = Map.class.isAssignableFrom(target);
      if (!array && !list && !map) {
        return null;
      }
      MethodHandle h;
      if (op == StandardOperation.GET) {
        h = GET_ELEMENT;
      } else if (op == StandardOperation.SET) {
        h = SET_ELEMENT;
      } else {
        h = REMOVE_ELEMENT;
      }
      if (!dynamicName) {
        h = MethodHandles.insertArguments(h, 1, name);
      }
      return h;
    }
    if (ns == StandardNamespace.METHOD) {
      if (op != StandardOperation.GET) {
        return null;
      }
      final MethodHandle bound = MethodHandles.insertArguments(GET_METHOD, 0, info, isStatic);
      if (dynamicName) {
        return bound;
      }
      final String n = String.valueOf(name);
      if (findMethods(info, n, isStatic) == null) {
        return null;
      }
      return MethodHandles.insertArguments(bound, 1, n);
    }
    return null;
  }

  static JavaMethods findMethods(ClassInfo info, String n, boolean isStatic) {
    final String methodName = n.indexOf('(') >= 0 ? n.substring(0, n.indexOf('(')) : n;
    final Map<String, List<Method>> map = isStatic ? info.staticMethods : info.instanceMethods;
    List<Method> l = map.get(methodName);
    if (l == null) {
      return null;
    }
    if (n.indexOf('(') >= 0) {
      final String sig = n.substring(n.indexOf('(') + 1, n.lastIndexOf(')'));
      final List<Method> filtered = new ArrayList<>();
      for (Method m : l) {
        if (signatureOf(m.getParameterTypes()).equals(sig.replace(" ", ""))) {
          filtered.add(m);
        }
      }
      if (filtered.isEmpty()) {
        return null;
      }
      l = filtered;
    }
    return new JavaMethods.MethodGroup(methodName, l);
  }

  static String signatureOf(Class<?>[] types) {
    final StringBuilder b = new StringBuilder();
    for (int i = 0; i < types.length; i++) {
      if (i > 0) {
        b.append(',');
      }
      b.append(types[i].getName());
    }
    return b.toString();
  }

  private static boolean hasReadable(ClassInfo info, String n, boolean isStatic, Class<?> target) {
    if (isStatic) {
      return info.staticGetters.containsKey(n) || info.staticFields.containsKey(n) || info.innerClasses.containsKey(n)
          || n.equals("class");
    }
    return info.instanceGetters.containsKey(n) || info.instanceFields.containsKey(n)
        || (n.equals("length") && lengthSupported(target));
  }

  private static boolean hasWritable(ClassInfo info, String n, boolean isStatic) {
    return isStatic ? info.staticSetters.containsKey(n) || info.staticFields.containsKey(n)
        : info.instanceSetters.containsKey(n) || info.instanceFields.containsKey(n);
  }

  private static boolean lengthSupported(Class<?> c) {
    return c.isArray() || Collection.class.isAssignableFrom(c) || Map.class.isAssignableFrom(c);
  }

  static Object getProp(ClassInfo info, boolean isStatic, Object receiver, Object nameObj) throws Throwable {
    final String n = String.valueOf(nameObj);
    try {
      if (isStatic) {
        final Method g = info.staticGetters.get(n);
        if (g != null) {
          return g.invoke(null);
        }
        final Field f = info.staticFields.get(n);
        if (f != null) {
          return f.get(null);
        }
        final Class<?> inner = info.innerClasses.get(n);
        if (inner != null) {
          return StaticClass.forClass(inner);
        }
        if (n.equals("class")) {
          return info.clazz;
        }
        return null;
      }
      final Method g = info.instanceGetters.get(n);
      if (g != null) {
        return g.invoke(receiver);
      }
      final Field f = info.instanceFields.get(n);
      if (f != null) {
        return f.get(receiver);
      }
      if (n.equals("length")) {
        return getLength(receiver);
      }
      return null;
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  static void setProp(ClassInfo info, boolean isStatic, Object receiver, Object nameObj, Object value)
      throws Throwable {
    final String n = String.valueOf(nameObj);
    try {
      final Method s = isStatic ? info.staticSetters.get(n) : info.instanceSetters.get(n);
      if (s != null) {
        s.invoke(isStatic ? null : receiver, value);
        return;
      }
      final Field f = isStatic ? info.staticFields.get(n) : info.instanceFields.get(n);
      if (f != null) {
        f.set(isStatic ? null : receiver, value);
      }
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  static Object getLength(Object receiver) {
    if (receiver.getClass().isArray()) {
      return Array.getLength(receiver);
    }
    if (receiver instanceof Collection) {
      return ((Collection<?>) receiver).size();
    }
    return ((Map<?, ?>) receiver).size();
  }

  static Object getElement(Object receiver, Object key) {
    if (receiver.getClass().isArray()) {
      return Array.get(receiver, ((Number) key).intValue());
    }
    if (receiver instanceof java.util.List) {
      return ((java.util.List<?>) receiver).get(((Number) key).intValue());
    }
    return ((Map<?, ?>) receiver).get(key);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  static void setElement(Object receiver, Object key, Object value) {
    if (receiver.getClass().isArray()) {
      Array.set(receiver, ((Number) key).intValue(), value);
    } else if (receiver instanceof java.util.List) {
      ((java.util.List) receiver).set(((Number) key).intValue(), value);
    } else {
      ((Map) receiver).put(key, value);
    }
  }

  static Object removeElement(Object receiver, Object key) {
    if (receiver instanceof java.util.List) {
      return ((java.util.List<?>) receiver).remove(((Number) key).intValue());
    }
    if (receiver instanceof Map) {
      return ((Map<?, ?>) receiver).remove(key);
    }
    throw new UnsupportedOperationException();
  }

  static Object getMethodObj(ClassInfo info, boolean isStatic, Object receiver, Object name) {
    return findMethods(info, String.valueOf(name), isStatic);
  }

  static Object invokeMethod(JavaMethods m, Object[] args) throws Throwable {
    return m.invokeWith(args.length > 0 ? args[0] : null, java.util.Arrays.copyOfRange(args, 1, args.length));
  }

  static Object invokeCallee(Object[] all) throws Throwable {
    final JavaMethods callee = (JavaMethods) all[0];
    return callee.invokeWith(all.length > 1 ? all[1] : null, all.length > 2 ? java.util.Arrays.copyOfRange(all, 2, all.length) : new Object[0]);
  }

  static Object newInstance(Object[] args) throws Throwable {
    throw new UnsupportedOperationException();
  }

  static Object newInstanceWith(JavaMethods group, Object[] args) throws Throwable {
    return group.invokeWith(null, args);
  }

  static Object calleeNew(Object[] all) throws Throwable {
    final JavaMethods callee = (JavaMethods) all[0];
    return callee.invokeWith(null, java.util.Arrays.copyOfRange(all, 1, all.length));
  }
}
