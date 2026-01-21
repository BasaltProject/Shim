/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.beans;

import java.lang.reflect.Method;

public class MethodDescriptor extends FeatureDescriptor {
  private final Method method;
  private ParameterDescriptor[] parameterDescriptors;

  public MethodDescriptor(Method method) {
    this(method, null);
  }

  public MethodDescriptor(Method method, ParameterDescriptor[] parameterDescriptors) {
    setName(method.getName());
    this.method = method;
    this.parameterDescriptors = parameterDescriptors != null ? parameterDescriptors.clone() : null;
  }

  MethodDescriptor(MethodDescriptor old) {
    super(old);
    method = old.method;
    if (old.parameterDescriptors != null) {
      parameterDescriptors = new ParameterDescriptor[old.parameterDescriptors.length];
      for (int i = 0; i < parameterDescriptors.length; i++) {
        parameterDescriptors[i] = new ParameterDescriptor(old.parameterDescriptors[i]);
      }
    }
  }

  public synchronized Method getMethod() {
    return method;
  }

  public ParameterDescriptor[] getParameterDescriptors() {
    return parameterDescriptors != null ? parameterDescriptors.clone() : null;
  }

  @Override
  void appendTo(StringBuilder sb) {
    appendTo(sb, "method", method);
    if (parameterDescriptors != null) {
      sb.append("; parameterDescriptors={");
      for (ParameterDescriptor pd : parameterDescriptors) {
        sb.append(pd).append(", ");
      }
      sb.setLength(sb.length() - 2);
      sb.append('}');
    }
  }
}
