/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.constant;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DynamicCallSiteDesc {
  private final DirectMethodHandleDesc bootstrapMethod;
  private final String invocationName;
  private final MethodTypeDesc invocationType;
  private final ConstantDesc[] bootstrapArgs;

  private DynamicCallSiteDesc(DirectMethodHandleDesc bootstrapMethod,
                              String invocationName,
                              MethodTypeDesc invocationType,
                              ConstantDesc[] bootstrapArgs) {
    this.bootstrapMethod = Objects.requireNonNull(bootstrapMethod, "bootstrapMethod");
    this.invocationName = Objects.requireNonNull(invocationName, "invocationName");
    this.invocationType = Objects.requireNonNull(invocationType, "invocationType");
    this.bootstrapArgs = bootstrapArgs != null ? bootstrapArgs.clone() : new ConstantDesc[0];
  }

  public static DynamicCallSiteDesc of(DirectMethodHandleDesc bootstrapMethod,
                                       String invocationName,
                                       MethodTypeDesc invocationType,
                                       ConstantDesc... bootstrapArgs) {
    return new DynamicCallSiteDesc(bootstrapMethod, invocationName, invocationType, bootstrapArgs);
  }

  public static DynamicCallSiteDesc of(DirectMethodHandleDesc bootstrapMethod,
                                       String invocationName,
                                       MethodTypeDesc invocationType) {
    return new DynamicCallSiteDesc(bootstrapMethod, invocationName, invocationType, new ConstantDesc[0]);
  }

  public static DynamicCallSiteDesc of(DirectMethodHandleDesc bootstrapMethod,
                                       MethodTypeDesc invocationType) {
    return of(bootstrapMethod, ConstantDescs.DEFAULT_NAME, invocationType);
  }

  public DirectMethodHandleDesc bootstrapMethod() {
    return bootstrapMethod;
  }

  public String invocationName() {
    return invocationName;
  }

  public MethodTypeDesc invocationType() {
    return invocationType;
  }

  public ConstantDesc[] bootstrapArgs() {
    return bootstrapArgs.clone();
  }

  public List<ConstantDesc> bootstrapArgsList() {
    return Collections.unmodifiableList(Arrays.asList(bootstrapArgs));
  }

  public DynamicCallSiteDesc withArgs(ConstantDesc... bootstrapArgs) {
    Objects.requireNonNull(bootstrapArgs, "bootstrapArgs");
    return new DynamicCallSiteDesc(bootstrapMethod, invocationName, invocationType, bootstrapArgs);
  }

  public DynamicCallSiteDesc withNameAndType(String invocationName, MethodTypeDesc invocationType) {
    Objects.requireNonNull(invocationName, "invocationName");
    Objects.requireNonNull(invocationType, "invocationType");
    return new DynamicCallSiteDesc(bootstrapMethod, invocationName, invocationType, bootstrapArgs);
  }

  public CallSite resolveCallSiteDesc(MethodHandles.Lookup lookup) throws Throwable {
    Objects.requireNonNull(lookup, "lookup");
    MethodHandle bsm = bootstrapMethod.resolveConstantDesc(lookup);
    MethodType type = invocationType.resolveConstantDesc(lookup);

    List<Object> args = new ArrayList<Object>(3 + bootstrapArgs.length);
    args.add(lookup);
    args.add(invocationName);
    args.add(type);
    for (ConstantDesc argDesc : bootstrapArgs) {
      if (argDesc == null) {
        args.add(null);
      } else {
        args.add(argDesc.resolveConstantDesc(lookup));
      }
    }
    return (CallSite) bsm.invokeWithArguments(args);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DynamicCallSiteDesc)) return false;
    DynamicCallSiteDesc that = (DynamicCallSiteDesc) o;
    return bootstrapMethod.equals(that.bootstrapMethod)
        && invocationName.equals(that.invocationName)
        && invocationType.equals(that.invocationType)
        && Arrays.equals(bootstrapArgs, that.bootstrapArgs);
  }

  @Override
  public int hashCode() {
    int res = bootstrapMethod.hashCode();
    res = 31 * res + invocationName.hashCode();
    res = 31 * res + invocationType.hashCode();
    res = 31 * res + Arrays.hashCode(bootstrapArgs);
    return res;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("DynamicCallSiteDesc[");
    sb.append(bootstrapMethod.owner().displayName()).append("::").append(bootstrapMethod.methodName()).append('(');
    boolean hasName = !invocationName.equals(ConstantDescs.DEFAULT_NAME);
    if (hasName) {
      sb.append(invocationName).append('/');
    }
    for (int i = 0; i < bootstrapArgs.length; ++i) {
      if (i > 0) sb.append(',');
      sb.append(bootstrapArgs[i]);
    }
    sb.append("):").append(invocationType.displayDescriptor()).append(']');
    return sb.toString();
  }
}
