/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi;

public interface PathSearchingVirtualMachine extends com.sun.jdi.VirtualMachine {
  java.lang.String baseDirectory();
  java.util.List<java.lang.String> bootClassPath();
  java.util.List<java.lang.String> classPath();
}
