/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.linker.support;

import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.linker.LinkRequest;

public class SimpleLinkRequest implements LinkRequest {
  private final CallSiteDescriptor callSiteDescriptor;
  private final boolean callSiteUnstable;
  private final Object[] arguments;

  public SimpleLinkRequest(CallSiteDescriptor callSiteDescriptor, boolean callSiteUnstable, Object... arguments) {
    this.callSiteDescriptor = callSiteDescriptor;
    this.callSiteUnstable = callSiteUnstable;
    this.arguments = arguments;
  }

  @Override
  public Object[] getArguments() {
    return arguments != null ? arguments.clone() : null;
  }

  @Override
  public Object getReceiver() {
    return arguments != null && arguments.length > 0 ? arguments[0] : null;
  }

  @Override
  public CallSiteDescriptor getCallSiteDescriptor() {
    return callSiteDescriptor;
  }

  @Override
  public boolean isCallSiteUnstable() {
    return callSiteUnstable;
  }

  @Override
  public LinkRequest replaceArguments(CallSiteDescriptor newCallSiteDescriptor, Object... newArguments) {
    return new SimpleLinkRequest(newCallSiteDescriptor, callSiteUnstable, newArguments);
  }
}
