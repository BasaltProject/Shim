/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.constant;

import java.lang.invoke.MethodHandles;

public interface ConstantDesc {
  Object resolveConstantDesc(MethodHandles.Lookup lookup) throws ReflectiveOperationException;
}
