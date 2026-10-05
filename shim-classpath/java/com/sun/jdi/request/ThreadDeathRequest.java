/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi.request;

public interface ThreadDeathRequest extends com.sun.jdi.request.EventRequest {
  default void addPlatformThreadsOnlyFilter() {
    throw new UnsupportedOperationException();
  }
  void addThreadFilter(com.sun.jdi.ThreadReference threadReference);
}
