/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

final class RegularEnumSet<E extends Enum<E>> extends EnumSet<E> {
  private static final long serialVersionUID = 3411599620347842686L;

  RegularEnumSet(int size, Class<E> type) {
    super(size, type);
  }
}
