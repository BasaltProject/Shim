/*
 * Copyright (c) 2026 BasaltMC.
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.constant;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.util.Objects;

abstract class AbstractClassDesc implements ClassDesc {
  @Override
  public ClassDesc arrayType() {
    return ClassDesc.ofDescriptor("[" + descriptorString());
  }

  @Override
  public ClassDesc arrayType(int rank) {
    if (rank <= 0) {
      throw new IllegalArgumentException("rank must be > 0: " + rank);
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < rank; ++i) {
      sb.append('[');
    }
    sb.append(descriptorString());
    return ClassDesc.ofDescriptor(sb.toString());
  }

  @Override
  public String displayName() {
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

  @Override
  public Class<?> resolveConstantDesc(MethodHandles.Lookup lookup) throws ReflectiveOperationException {
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
