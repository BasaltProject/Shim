/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardedInvocationTransformer;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;
import jdk.dynalink.linker.support.Lookup;
import jdk.dynalink.linker.support.SimpleLinkRequest;

public final class DynamicLinker {
  private static final String CLASS_NAME = DynamicLinker.class.getName();
  private static final String RELINK_METHOD_NAME = "relink";

  private static final String INITIAL_LINK_CLASS_NAME = "java.lang.invoke.MethodHandle";
  private static final String INITIAL_LINK_METHOD_NAME = "invokeExact";

  private static final MethodHandle RELINK = Lookup.findOwnSpecial(MethodHandles.lookup(), RELINK_METHOD_NAME,
      MethodHandle.class, RelinkableCallSite.class, int.class, Object[].class);

  private final LinkerServices linkerServices;
  private final GuardedInvocationTransformer prelinkTransformer;
  private final boolean syncOnRelink;
  private final int unstableRelinkThreshold;

  DynamicLinker(LinkerServices linkerServices, GuardedInvocationTransformer prelinkTransformer, boolean syncOnRelink,
      int unstableRelinkThreshold) {
    if (unstableRelinkThreshold < 0) {
      throw new IllegalArgumentException("unstableRelinkThreshold < 0");
    }
    this.linkerServices = linkerServices;
    this.prelinkTransformer = prelinkTransformer;
    this.syncOnRelink = syncOnRelink;
    this.unstableRelinkThreshold = unstableRelinkThreshold;
  }

  public LinkerServices getLinkerServices() {
    return linkerServices;
  }

  public <T extends RelinkableCallSite> T link(T callSite) {
    callSite.initialize(createRelinkAndInvokeMethod(callSite, 0));
    return callSite;
  }

  public static StackTraceElement getLinkedCallSiteLocation() {
    final StackTraceElement[] trace = new Throwable().getStackTrace();
    for (int i = 0; i < trace.length - 1; ++i) {
      final StackTraceElement frame = trace[i];
      if (isRelinkFrame(frame) || isInitialLinkFrame(frame)) {
        return trace[i + 1];
      }
    }
    return null;
  }

  private static boolean isInitialLinkFrame(StackTraceElement frame) {
    return testFrame(frame, INITIAL_LINK_METHOD_NAME, INITIAL_LINK_CLASS_NAME);
  }

  private static boolean isRelinkFrame(StackTraceElement frame) {
    return testFrame(frame, RELINK_METHOD_NAME, CLASS_NAME);
  }

  private static boolean testFrame(StackTraceElement frame, String methodName, String className) {
    return methodName.equals(frame.getMethodName()) && className.equals(frame.getClassName());
  }

  private MethodHandle createRelinkAndInvokeMethod(RelinkableCallSite callSite, int relinkCount) {
    final MethodHandle boundRelinker = MethodHandles.insertArguments(RELINK, 0, this, callSite,
        Integer.valueOf(relinkCount));
    final MethodType type = callSite.getDescriptor().getMethodType();
    final MethodHandle collectingRelinker = boundRelinker.asCollector(Object[].class, type.parameterCount());
    return MethodHandles.foldArguments(MethodHandles.exactInvoker(type),
        collectingRelinker.asType(type.changeReturnType(MethodHandle.class)));
  }

  @SuppressWarnings("unused")
  private MethodHandle relink(RelinkableCallSite callSite, int relinkCount, Object... arguments) throws Exception {
    final CallSiteDescriptor callSiteDescriptor = callSite.getDescriptor();
    final boolean unstableDetectionEnabled = unstableRelinkThreshold > 0;
    final boolean callSiteUnstable = unstableDetectionEnabled && relinkCount >= unstableRelinkThreshold;
    final LinkRequest linkRequest = new SimpleLinkRequest(callSiteDescriptor, callSiteUnstable, arguments);
    GuardedInvocation guardedInvocation = linkerServices.getGuardedInvocation(linkRequest);
    if (guardedInvocation == null) {
      throw new NoSuchDynamicMethodException(callSiteDescriptor.toString());
    }
    if (prelinkTransformer != null) {
      guardedInvocation = prelinkTransformer.filter(guardedInvocation, linkRequest, linkerServices);
      Objects.requireNonNull(guardedInvocation, "prelink transformer returned null");
    }
    int newRelinkCount = relinkCount;
    if (unstableDetectionEnabled && newRelinkCount <= unstableRelinkThreshold && newRelinkCount++ == unstableRelinkThreshold) {
      java.util.logging.Logger.getLogger(CLASS_NAME).log(java.util.logging.Level.INFO,
          "{0} is unstable, relinking for the {1}th time", new Object[] {callSiteDescriptor, Integer.valueOf(unstableRelinkThreshold)});
    }
    final MethodHandle relinkAndInvoke = createRelinkAndInvokeMethod(callSite, newRelinkCount);
    if (syncOnRelink) {
      synchronized (callSite) {
        callSite.relink(guardedInvocation, relinkAndInvoke);
      }
    } else {
      callSite.relink(guardedInvocation, relinkAndInvoke);
    }
    return guardedInvocation.getInvocation();
  }
}
