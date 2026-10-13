/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2016, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2016, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.invoke;

import static shim.Assembler.*;

import shim.Assembler;
import shim.Classes;
import shim.SystemClassLoader;
import shim.VMClass;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public final class MethodType implements java.io.Serializable {
  private static final char[] Primitives = new char[] {
    'V', 'Z', 'B', 'C', 'S', 'I', 'F', 'J', 'D'
  };

  final ClassLoader loader;
  final byte[] spec;
  private volatile List<Parameter> parameters;
  private volatile Result result;
  private volatile int footprint;

  MethodType(ClassLoader loader, byte[] spec) {
    this.loader = loader;
    this.spec = spec;
  }

  MethodType(String spec) {
    this.loader = SystemClassLoader.appLoader();
    this.spec = new byte[spec.length() + 1];
    spec.getBytes(0, spec.length(), this.spec, 0);
  }

  public String toMethodDescriptorString() {
    return Classes.makeString(spec, 0, spec.length - 1);
  }

  private static String spec(Class c) {
    if (c.isPrimitive()) {
      VMClass vmc = Classes.toVMClass(c);
      for (char p: Primitives) {
        if (vmc == Classes.primitiveClass(p)) {
          return String.valueOf(p);
        }
      }
      throw new AssertionError();
    } else if (c.isArray()) {
      return "[" + spec(c.getComponentType());
    } else {
      return "L" + c.getName().replace('.', '/') + ";";
    }
  }

  private MethodType(Class rtype,
                     Class ... ptypes)
  {
    if (rtype == null || ptypes == null) {
      throw new NullPointerException();
    }
    int slots = 0;
    for (Class ptype : ptypes) {
      if (ptype == null) throw new NullPointerException();
      if (ptype == void.class) throw new IllegalArgumentException("void parameter");
      slots += ptype == long.class || ptype == double.class ? 2 : 1;
      if (slots > 255) throw new IllegalArgumentException("too many parameter slots");
    }
    loader = rtype.getClassLoader();

    StringBuilder sb = new StringBuilder();
    sb.append('(');
    parameters = new ArrayList(ptypes.length);
    int position = 0;
    for (int i = 0; i < ptypes.length; ++i) {
      String spec = spec(ptypes[i]);
      sb.append(spec);

      Type type = type(spec);

      parameters.add(new Parameter(i,
                                   position,
                                   spec,
                                   ptypes[i],
                                   type.load));

      position += type.size;
    }
    sb.append(')');

    footprint = position;

    String spec = spec(rtype);
    sb.append(spec);

    result = new Result(spec, rtype, type(spec).return_);

    String descriptor = sb.toString();
    this.spec = new byte[descriptor.length() + 1];
    descriptor.getBytes(0, descriptor.length(), this.spec, 0);
  }

  public static MethodType methodType(Class rtype) {
    return new MethodType(rtype, new Class[0]);
  }

  public static MethodType methodType(Class rtype, Class ptype) {
    return new MethodType(rtype, new Class[] {ptype});
  }

  public static MethodType methodType(Class rtype,
                                      Class ptype0,
                                      Class ... ptypes)
  {
    Class[] array = new Class[ptypes.length + 1];
    array[0] = ptype0;
    System.arraycopy(ptypes, 0, array, 1, ptypes.length);
    return methodType(rtype, array);
  }

  public static MethodType methodType(Class rtype,
                                      Class ... ptypes)
  {
    return new MethodType(rtype, ptypes);
  }

  public String toString() {
    StringBuilder text = new StringBuilder("(");
    Class[] types = parameterArray();
    for (int i = 0; i < types.length; ++i) {
      if (i != 0) text.append(',');
      text.append(types[i].getSimpleName());
    }
    return text.append(')').append(returnType().getSimpleName()).toString();
  }

  public boolean equals(Object other) {
    if (this == other) return true;
    if (!(other instanceof MethodType)) return false;
    MethodType that = (MethodType) other;
    if (returnType() != that.returnType() || parameterCount() != that.parameterCount()) return false;
    for (int i = 0; i < parameterCount(); ++i) {
      if (parameterType(i) != that.parameterType(i)) return false;
    }
    return true;
  }

  public int hashCode() {
    int hash = 31 + returnType().hashCode();
    for (Class parameter : parameterArray()) hash = 31 * hash + parameter.hashCode();
    return hash;
  }

  public int parameterCount() {
    parameters();
    return parameters.size();
  }

  public Class parameterType(int index) {
    parameters();
    if (index < 0 || index >= parameters.size()) throw new IndexOutOfBoundsException();
    return parameters.get(index).type;
  }

  public Class lastParameterType() {
    int count = parameterCount();
    return count == 0 ? void.class : parameterType(count - 1);
  }

  public List<Class<?>> parameterList() {
    List<Class<?>> list = new ArrayList<Class<?>>();
    for (Class parameter : parameterArray()) list.add(parameter);
    return Collections.unmodifiableList(list);
  }

  public String descriptorString() {
    return toMethodDescriptorString();
  }

  public static MethodType methodType(Class rtype, MethodType ptypes) {
    return methodType(rtype, ptypes.parameterArray());
  }

  public static MethodType methodType(Class rtype, List<Class<?>> ptypes) {
    return methodType(rtype, ptypes.toArray(new Class[ptypes.size()]));
  }

  public static MethodType genericMethodType(int count) {
    return genericMethodType(count, false);
  }

  public static MethodType genericMethodType(int count, boolean finalArray) {
    if (count < 0 || count > (finalArray ? 254 : 255)) throw new IllegalArgumentException();
    Class[] parameters = new Class[count + (finalArray ? 1 : 0)];
    for (int i = 0; i < count; ++i) parameters[i] = Object.class;
    if (finalArray) parameters[count] = Object[].class;
    return methodType(Object.class, parameters);
  }

  public MethodType changeReturnType(Class rtype) {
    return methodType(rtype, parameterArray());
  }

  public MethodType changeParameterType(int index, Class ptype) {
    parameterType(index); // Check bounds before modifying the copy.
    Class[] parameters = parameterArray();
    parameters[index] = ptype;
    return methodType(returnType(), parameters);
  }

  public MethodType insertParameterTypes(int position, Class... added) {
    int count = parameterCount();
    if (position < 0 || position > count) throw new IndexOutOfBoundsException();
    if (added.length > 255 - count) throw new IllegalArgumentException();
    Class[] parameters = new Class[count + added.length];
    Class[] original = parameterArray();
    System.arraycopy(original, 0, parameters, 0, position);
    System.arraycopy(added, 0, parameters, position, added.length);
    System.arraycopy(original, position, parameters, position + added.length, count - position);
    return methodType(returnType(), parameters);
  }

  public MethodType insertParameterTypes(int position, List<Class<?>> added) {
    return insertParameterTypes(position, added.toArray(new Class[added.size()]));
  }

  public MethodType appendParameterTypes(Class... added) {
    return insertParameterTypes(parameterCount(), added);
  }

  public MethodType appendParameterTypes(List<Class<?>> added) {
    return insertParameterTypes(parameterCount(), added);
  }

  public MethodType dropParameterTypes(int start, int end) {
    int count = parameterCount();
    if (start < 0 || end < start || end > count) throw new IndexOutOfBoundsException();
    Class[] parameters = new Class[count - (end - start)];
    Class[] original = parameterArray();
    System.arraycopy(original, 0, parameters, 0, start);
    System.arraycopy(original, end, parameters, start, count - end);
    return methodType(returnType(), parameters);
  }

  public boolean hasPrimitives() {
    if (returnType().isPrimitive()) return true;
    for (Class parameter : parameterArray()) if (parameter.isPrimitive()) return true;
    return false;
  }

  public boolean hasWrappers() {
    if (primitive(returnType()) != returnType()) return true;
    for (Class parameter : parameterArray()) {
      if (parameter != Void.class && primitive(parameter) != parameter) return true;
    }
    return false;
  }

  public MethodType erase() {
    Class[] parameters = parameterArray();
    for (int i = 0; i < parameters.length; ++i) {
      if (!parameters[i].isPrimitive()) parameters[i] = Object.class;
    }
    Class result = returnType();
    return methodType(result.isPrimitive() ? result : Object.class, parameters);
  }

  public MethodType generic() {
    return genericMethodType(parameterCount());
  }

  public MethodType wrap() {
    Class[] parameters = parameterArray();
    for (int i = 0; i < parameters.length; ++i) parameters[i] = wrapper(parameters[i]);
    return methodType(wrapper(returnType()), parameters);
  }

  public MethodType unwrap() {
    Class[] parameters = parameterArray();
    for (int i = 0; i < parameters.length; ++i) {
      if (parameters[i] != Void.class) parameters[i] = primitive(parameters[i]);
    }
    return methodType(primitive(returnType()), parameters);
  }

  private static Class wrapper(Class type) {
    if (type == void.class) return Void.class;
    if (type == boolean.class) return Boolean.class;
    if (type == byte.class) return Byte.class;
    if (type == char.class) return Character.class;
    if (type == short.class) return Short.class;
    if (type == int.class) return Integer.class;
    if (type == float.class) return Float.class;
    if (type == long.class) return Long.class;
    if (type == double.class) return Double.class;
    return type;
  }

  private static Class primitive(Class type) {
    if (type == Void.class) return void.class;
    if (type == Boolean.class) return boolean.class;
    if (type == Byte.class) return byte.class;
    if (type == Character.class) return char.class;
    if (type == Short.class) return short.class;
    if (type == Integer.class) return int.class;
    if (type == Float.class) return float.class;
    if (type == Long.class) return long.class;
    if (type == Double.class) return double.class;
    return type;
  }

  public int footprint() {
    parameters(); // ensure spec is parsed

    return footprint;
  }

  public Class returnType() {
    parameters(); // ensure spec is parsed

    return result.type;
  }

  public Class[] parameterArray() {
    parameters(); // ensure spec is parsed

    Class[] array = new Class[parameters.size()];
    for (int i = 0; i < parameters.size(); ++i) {
      array[i] = parameters.get(i).type;
    }

    return array;
  }

  public Iterable<Parameter> parameters() {
    if (parameters == null) {
      List<Parameter> list = new ArrayList();
      int i;
      int index = 0;
      int position = 0;
      for (i = 1; spec[i] != ')'; ++i) {
        int start = i;
        switch (spec[i]) {
        case 'L': {
          ++ i;
          while (spec[i] != ';') ++ i;
        } break;

        case '[': {
          ++ i;
          while (spec[i] == '[') ++ i;

          switch (spec[i]) {
          case 'L':
            ++ i;
            while (spec[i] != ';') ++ i;
            break;

          default:
            break;
          }
        } break;

        case 'Z':
        case 'B':
        case 'S':
        case 'C':
        case 'I':
        case 'F':
        case 'J':
        case 'D':
          break;

        default: throw new AssertionError();
        }

        String paramSpec = Classes.makeString(spec, start, (i - start) + 1);
        Type type = type(paramSpec);

        list.add(new Parameter
                 (index,
                  position,
                  paramSpec,
                  Classes.forCanonicalName(loader, paramSpec),
                  type.load));

        ++ index;
        position += type.size;
      }

      footprint = position;

      ++ i;

      String paramSpec = Classes.makeString(spec, i, spec.length - i - 1);
      Type type = type(paramSpec);

      result = new Result(paramSpec,
                          Classes.forCanonicalName(loader, paramSpec),
                          type.return_);

      parameters = list;
    }

    return parameters;
  }

  public Result result() {
    parameters(); // ensure spec has been parsed

    return result;
  }

  private static Type type(String spec) {
    switch (spec.charAt(0)) {
    case 'L':
    case '[':
      return Type.ObjectType;

    case 'Z':
    case 'B':
    case 'S':
    case 'C':
    case 'I':
      return Type.IntegerType;

    case 'F':
      return Type.FloatType;

    case 'J':
      return Type.LongType;

    case 'D':
      return Type.DoubleType;

    case 'V':
      return Type.VoidType;

    default: throw new AssertionError();
    }
  }

  private static enum Type {
    ObjectType(aload, areturn, 1),
    IntegerType(iload, ireturn, 1),
    FloatType(fload, freturn, 1),
    LongType(lload, lreturn, 2),
    DoubleType(dload, dreturn, 2),
    VoidType(-1, Assembler.return_, -1);

    public final int load;
    public final int return_;
    public final int size;

    private Type(int load, int return_, int size) {
      this.load = load;
      this.return_ = return_;
      this.size = size;
    }
  }

  public interface TypeSpec {
    public Class type();

    public String spec();
  }

  public static class Parameter implements TypeSpec {
    private final int index;
    private final int position;
    private final String spec;
    private final Class type;
    private final int load;

    private Parameter(int index,
                      int position,
                      String spec,
                      Class type,
                      int load)
    {
      this.index = index;
      this.position = position;
      this.spec = spec;
      this.type = type;
      this.load = load;
    }

    public int index() {
      return index;
    }

    public int position() {
      return position;
    }

    public String spec() {
      return spec;
    }

    public Class type() {
      return type;
    }

    public int load() {
      return load;
    }
  }

  public static class Result implements TypeSpec {
    private final String spec;
    private final Class type;
    private final int return_;

    public Result(String spec, Class type, int return_) {
      this.spec = spec;
      this.type = type;
      this.return_ = return_;
    }

    public int return_() {
      return return_; // :)
    }

    public String spec() {
      return spec;
    }

    public Class type() {
      return type;
    }
  }
}
