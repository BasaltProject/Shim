/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.invoke;

public final class DirectMethodHandle extends MethodHandle {
  public DirectMethodHandle(int kind, ClassLoader loader, shim.VMMethod method) {
    super(kind, loader, method);
  }

  DirectMethodHandle(String class_, String name, String spec, int kind) {
    super(class_, name, spec, kind);
  }
}
