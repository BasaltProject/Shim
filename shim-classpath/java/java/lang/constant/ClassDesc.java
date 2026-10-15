/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.constant;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.TypeDescriptor;
import java.lang.reflect.Array;
import java.util.Objects;

public interface ClassDesc extends ConstantDesc, TypeDescriptor.OfField<ClassDesc> {

  static ClassDesc of(String name) {
    Objects.requireNonNull(name, "name");
    if (name.isEmpty() || name.endsWith("[]") || name.indexOf('/') >= 0 || name.indexOf(';') >= 0) {
      throw new IllegalArgumentException("Invalid class name: " + name);
    }
    String internal = name.replace('.', '/');
    return ofDescriptor("L" + internal + ";");
  }

  static ClassDesc of(String packageName, String className) {
    Objects.requireNonNull(packageName, "packageName");
    Objects.requireNonNull(className, "className");
    if (packageName.isEmpty()) {
      return of(className);
    }
    return of(packageName + "." + className);
  }

  static ClassDesc ofDescriptor(String desc) {
    Objects.requireNonNull(desc, "desc");
    if (desc.isEmpty()) {
      throw new IllegalArgumentException("Empty descriptor");
    }
    if (desc.length() == 1) {
      char c = desc.charAt(0);
      switch (c) {
        case 'Z':
        case 'B':
        case 'C':
        case 'S':
        case 'I':
        case 'J':
        case 'F':
        case 'D':
        case 'V':
          return new ConstantDescsImpl.PrimitiveDesc(desc);
        default:
          throw new IllegalArgumentException("Invalid descriptor: " + desc);
      }
    }
    if (desc.startsWith("[")) {
      int dims = 0;
      while (dims < desc.length() && desc.charAt(dims) == '[') {
        dims++;
      }
      if (dims == desc.length()) {
        throw new IllegalArgumentException("Invalid array descriptor: " + desc);
      }
      ofDescriptor(desc.substring(dims));
      return new ConstantDescsImpl.ArrayDesc(desc);
    }
    if (desc.startsWith("L") && desc.endsWith(";") && desc.length() > 2) {
      String internal = desc.substring(1, desc.length() - 1);
      if (internal.indexOf('.') >= 0 || internal.indexOf(';') >= 0) {
        throw new IllegalArgumentException("Invalid class descriptor: " + desc);
      }
      return new ConstantDescsImpl.ClassOrInterfaceDesc(desc);
    }
    throw new IllegalArgumentException("Invalid descriptor: " + desc);
  }

  @Override
  String descriptorString();

  @Override
  default boolean isArray() {
    return descriptorString().startsWith("[");
  }

  @Override
  default boolean isPrimitive() {
    return descriptorString().length() == 1;
  }

  default boolean isClassOrInterface() {
    return !isArray() && !isPrimitive();
  }

  @Override
  default ClassDesc componentType() {
    if (!isArray()) {
      return null;
    }
    return ofDescriptor(descriptorString().substring(1));
  }

  @Override
  default ClassDesc arrayType() {
    return ofDescriptor("[" + descriptorString());
  }

  default ClassDesc arrayType(int rank) {
    if (rank <= 0) {
      throw new IllegalArgumentException("rank must be > 0: " + rank);
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < rank; ++i) {
      sb.append('[');
    }
    sb.append(descriptorString());
    return ofDescriptor(sb.toString());
  }

  default String displayName() {
    if (isPrimitive()) {
      char c = descriptorString().charAt(0);
      switch (c) {
        case 'Z': return "boolean";
        case 'B': return "byte";
        case 'C': return "char";
        case 'S': return "short";
        case 'I': return "int";
        case 'J': return "long";
        case 'F': return "float";
        case 'D': return "double";
        case 'V': return "void";
        default: return descriptorString();
      }
    }
    if (isArray()) {
      return componentType().displayName() + "[]";
    }
    String s = descriptorString();
    String internal = s.substring(1, s.length() - 1);
    int slash = internal.lastIndexOf('/');
    return slash >= 0 ? internal.substring(slash + 1) : internal;
  }

  default String packageName() {
    if (!isClassOrInterface()) {
      return "";
    }
    String s = descriptorString();
    String internal = s.substring(1, s.length() - 1);
    int slash = internal.lastIndexOf('/');
    return slash >= 0 ? internal.substring(0, slash).replace('/', '.') : "";
  }

  default ClassDesc nested(String nestedName) {
    Objects.requireNonNull(nestedName, "nestedName");
    if (!isClassOrInterface()) {
      throw new IllegalStateException("Only classes or interfaces can be nested");
    }
    String s = descriptorString();
    String internal = s.substring(1, s.length() - 1);
    return ofDescriptor("L" + internal + "$" + nestedName + ";");
  }

  default ClassDesc nested(String first, String... more) {
    ClassDesc cur = nested(first);
    if (more != null) {
      for (String m : more) {
        cur = cur.nested(m);
      }
    }
    return cur;
  }

  @Override
  default Class<?> resolveConstantDesc(MethodHandles.Lookup lookup) throws ReflectiveOperationException {
    Objects.requireNonNull(lookup, "lookup");
    if (isPrimitive()) {
      char c = descriptorString().charAt(0);
      switch (c) {
        case 'Z': return boolean.class;
        case 'B': return byte.class;
        case 'C': return char.class;
        case 'S': return short.class;
        case 'I': return int.class;
        case 'J': return long.class;
        case 'F': return float.class;
        case 'D': return double.class;
        case 'V': return void.class;
      }
    }
    if (isArray()) {
      Class<?> comp = componentType().resolveConstantDesc(lookup);
      return Array.newInstance(comp, 0).getClass();
    }
    String s = descriptorString();
    String internal = s.substring(1, s.length() - 1);
    return lookup.findClass(internal.replace('/', '.'));
  }
}
