/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.jgss;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;

public interface ExtendedGSSContext extends GSSContext {
  Object inquireSecContext(InquireType type) throws GSSException;

  void requestDelegPolicy(boolean state) throws GSSException;

  boolean getDelegPolicyState();
}
