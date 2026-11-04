/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink.linker;

public interface LinkRequest {
  java.lang.Object[] getArguments();
  jdk.dynalink.CallSiteDescriptor getCallSiteDescriptor();
  java.lang.Object getReceiver();
  boolean isCallSiteUnstable();
  jdk.dynalink.linker.LinkRequest replaceArguments(jdk.dynalink.CallSiteDescriptor callSiteDescriptor, java.lang.Object... objects);
}
