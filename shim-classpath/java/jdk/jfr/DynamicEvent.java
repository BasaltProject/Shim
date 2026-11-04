/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

final class DynamicEvent extends Event {
  final EventInfo info;
  final Object[] values;

  DynamicEvent(EventInfo info) {
    this.info = info;
    this.values = new Object[info.declared.size()];
  }

  void setValue(int index, Object value) {
    if (index < 0 || index >= values.length) {
      throw new IndexOutOfBoundsException("Index " + index + " is out of bounds [0, " + values.length + "]");
    }
    values[index] = value;
  }
}
