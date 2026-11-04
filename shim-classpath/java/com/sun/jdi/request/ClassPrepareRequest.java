/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi.request;

public interface ClassPrepareRequest extends com.sun.jdi.request.EventRequest {
  void addClassExclusionFilter(java.lang.String string);
  void addClassFilter(java.lang.String string);
  void addClassFilter(com.sun.jdi.ReferenceType referenceType);
  void addSourceNameFilter(java.lang.String string);
}
