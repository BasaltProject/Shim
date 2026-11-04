/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import javax.lang.model.element.Name;

public final class NameImpl implements Name {
  private static final java.util.concurrent.ConcurrentHashMap<String, NameImpl> TABLE =
      new java.util.concurrent.ConcurrentHashMap<String, NameImpl>();
  private final String s;

  private NameImpl(String s) {
    this.s = s;
  }

  public static NameImpl of(String s) {
    NameImpl n = TABLE.get(s);
    if (n == null) {
      NameImpl fresh = new NameImpl(s);
      n = TABLE.putIfAbsent(s, fresh);
      if (n == null) {
        n = fresh;
      }
    }
    return n;
  }

  public boolean contentEquals(CharSequence cs) {
    return s.contentEquals(cs);
  }

  public int length() {
    return s.length();
  }

  public char charAt(int index) {
    return s.charAt(index);
  }

  public CharSequence subSequence(int start, int end) {
    return s.subSequence(start, end);
  }

  public boolean equals(Object o) {
    return o instanceof NameImpl && ((NameImpl) o).s.equals(s);
  }

  public int hashCode() {
    return s.hashCode();
  }

  public String toString() {
    return s;
  }
}
