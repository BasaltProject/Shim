/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

import java.util.Comparator;
import java.util.Locale;

public abstract class Collator implements Comparator<Object>, Cloneable {
  public static final int PRIMARY = 0;
  public static final int SECONDARY = 1;
  public static final int TERTIARY = 2;
  public static final int IDENTICAL = 3;
  public static final int NO_DECOMPOSITION = 0;
  public static final int CANONICAL_DECOMPOSITION = 1;
  public static final int FULL_DECOMPOSITION = 2;

  private int strength = TERTIARY;
  private int decmp = NO_DECOMPOSITION;

  public static synchronized Collator getInstance() {
    return getInstance(Locale.getDefault());
  }

  public static Collator getInstance(Locale desiredLocale) {
    java.util.Objects.requireNonNull(desiredLocale);
    return (Collator) RuleBasedCollator.defaultCollator().clone();
  }

  public abstract int compare(String source, String target);

  @Override
  public int compare(Object o1, Object o2) {
    return compare((String) o1, (String) o2);
  }

  public abstract CollationKey getCollationKey(String source);

  public boolean equals(String source, String target) {
    return compare(source, target) == Collator.EQUAL;
  }

  private static final int EQUAL = 0;

  public synchronized int getStrength() {
    return strength;
  }

  public synchronized void setStrength(int newStrength) {
    if (newStrength != PRIMARY && newStrength != SECONDARY && newStrength != TERTIARY && newStrength != IDENTICAL) {
      throw new IllegalArgumentException("Incorrect comparison level.");
    }
    strength = newStrength;
  }

  public synchronized int getDecomposition() {
    return decmp;
  }

  public synchronized void setDecomposition(int decompositionMode) {
    if (decompositionMode != NO_DECOMPOSITION && decompositionMode != CANONICAL_DECOMPOSITION
        && decompositionMode != FULL_DECOMPOSITION) {
      throw new IllegalArgumentException("Wrong decomposition mode.");
    }
    decmp = decompositionMode;
  }

  public static synchronized Locale[] getAvailableLocales() {
    return Locale.getAvailableLocales();
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  @Override
  public boolean equals(Object that) {
    if (this == that) {
      return true;
    }
    if (that == null || getClass() != that.getClass()) {
      return false;
    }
    Collator other = (Collator) that;
    return strength == other.strength && decmp == other.decmp;
  }

  @Override
  public abstract int hashCode();

  protected Collator() {
  }
}
