/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

class Comparators {
  private Comparators() {
  }

  @SuppressWarnings("rawtypes")
  enum NaturalOrderComparator implements Comparator<Comparable<Object>> {
    INSTANCE;

    @Override
    public int compare(Comparable<Object> c1, Comparable<Object> c2) {
      return c1.compareTo(c2);
    }

    @Override
    public Comparator<Comparable<Object>> reversed() {
      return Comparator.reverseOrder();
    }
  }
}
