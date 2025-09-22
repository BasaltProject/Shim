/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.regex;

interface CharPredicate {
  boolean is(int ch);

  default CharPredicate and(final CharPredicate other) {
    final CharPredicate self = this;
    return new CharPredicate() {
      @Override
      public boolean is(int ch) {
        return self.is(ch) && other.is(ch);
      }
    };
  }

  default CharPredicate union(final CharPredicate other) {
    final CharPredicate self = this;
    return new CharPredicate() {
      @Override
      public boolean is(int ch) {
        return self.is(ch) || other.is(ch);
      }
    };
  }

  default CharPredicate negate() {
    final CharPredicate self = this;
    return new CharPredicate() {
      @Override
      public boolean is(int ch) {
        return !self.is(ch);
      }
    };
  }
}
