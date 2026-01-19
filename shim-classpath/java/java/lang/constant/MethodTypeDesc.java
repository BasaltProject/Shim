/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.constant;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.TypeDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public interface MethodTypeDesc extends ConstantDesc, TypeDescriptor.OfMethod<ClassDesc, MethodTypeDesc> {
  boolean equals(Object o);

  int hashCode();


  static MethodTypeDesc of(ClassDesc returnDesc) {
    return of(returnDesc, new ClassDesc[0]);
  }

  static MethodTypeDesc of(ClassDesc returnDesc, List<ClassDesc> paramDescs) {
    Objects.requireNonNull(paramDescs, "paramDescs");
    return of(returnDesc, paramDescs.toArray(new ClassDesc[paramDescs.size()]));
  }

  static MethodTypeDesc of(ClassDesc returnDesc, ClassDesc... paramDescs) {
    Objects.requireNonNull(returnDesc, "returnDesc");
    List<ClassDesc> list;
    if (paramDescs == null || paramDescs.length == 0) {
      list = java.util.Collections.emptyList();
    } else {
      list = new ArrayList<ClassDesc>(paramDescs.length);
      for (ClassDesc p : paramDescs) {
        Objects.requireNonNull(p, "paramDesc");
        if (p.descriptorString().equals("V")) {
          throw new IllegalArgumentException("Parameter cannot be void");
        }
        list.add(p);
      }
    }
    return new ConstantDescsImpl.MethodTypeDescImpl(returnDesc, list);
  }

  static MethodTypeDesc ofDescriptor(String descriptor) {
    Objects.requireNonNull(descriptor, "descriptor");
    if (!descriptor.startsWith("(") || descriptor.indexOf(')') < 0) {
      throw new IllegalArgumentException("Invalid method descriptor: " + descriptor);
    }
    int closeParen = descriptor.indexOf(')');
    String paramsStr = descriptor.substring(1, closeParen);
    String retStr = descriptor.substring(closeParen + 1);
    ClassDesc ret = ClassDesc.ofDescriptor(retStr);
    List<ClassDesc> params = new ArrayList<ClassDesc>();
    int i = 0;
    while (i < paramsStr.length()) {
      int start = i;
      while (i < paramsStr.length() && paramsStr.charAt(i) == '[') {
        i++;
      }
      if (i >= paramsStr.length()) {
        throw new IllegalArgumentException("Invalid parameter descriptor");
      }
      char c = paramsStr.charAt(i++);
      if (c == 'L') {
        int semi = paramsStr.indexOf(';', i);
        if (semi < 0) {
          throw new IllegalArgumentException("Invalid class parameter descriptor");
        }
        i = semi + 1;
      }
      ClassDesc p = ClassDesc.ofDescriptor(paramsStr.substring(start, i));
      if (p.descriptorString().equals("V")) {
        throw new IllegalArgumentException("Parameter cannot be void");
      }
      params.add(p);
    }
    return new ConstantDescsImpl.MethodTypeDescImpl(ret, params);
  }

  @Override
  ClassDesc returnType();

  @Override
  int parameterCount();

  @Override
  ClassDesc parameterType(int index);

  @Override
  List<ClassDesc> parameterList();

  @Override
  ClassDesc[] parameterArray();

  @Override
  MethodTypeDesc changeReturnType(ClassDesc returnType);

  @Override
  MethodTypeDesc changeParameterType(int index, ClassDesc paramType);

  @Override
  MethodTypeDesc dropParameterTypes(int start, int end);

  @Override
  MethodTypeDesc insertParameterTypes(int pos, ClassDesc... paramTypes);

  @Override
  String descriptorString();

  default String displayDescriptor() {
    StringBuilder sb = new StringBuilder("(");
    List<ClassDesc> params = parameterList();
    for (int i = 0; i < params.size(); ++i) {
      if (i > 0) sb.append(',');
      sb.append(params.get(i).displayName());
    }
    sb.append(')');
    sb.append(returnType().displayName());
    return sb.toString();
  }

  @Override
  default MethodType resolveConstantDesc(MethodHandles.Lookup lookup) throws ReflectiveOperationException {
    Objects.requireNonNull(lookup, "lookup");
    Class<?> r = returnType().resolveConstantDesc(lookup);
    List<ClassDesc> pDescs = parameterList();
    Class<?>[] pTypes = new Class<?>[pDescs.size()];
    for (int i = 0; i < pDescs.size(); ++i) {
      pTypes[i] = pDescs.get(i).resolveConstantDesc(lookup);
    }
    return MethodType.methodType(r, pTypes);
  }
}
