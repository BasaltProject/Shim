/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.math;

import java.io.Serializable;

public final class MathContext implements Serializable {
  private static final long serialVersionUID = 5579720004786848255L;

  private static final int kMinDigits = 0;

  public static final MathContext UNLIMITED = new MathContext(0, RoundingMode.HALF_UP);
  public static final MathContext DECIMAL32 = new MathContext(7, RoundingMode.HALF_EVEN);
  public static final MathContext DECIMAL64 = new MathContext(16, RoundingMode.HALF_EVEN);
  public static final MathContext DECIMAL128 = new MathContext(34, RoundingMode.HALF_EVEN);

  final int precision;
  final RoundingMode roundingMode;

  public MathContext(int setPrecision) {
    this(setPrecision, RoundingMode.HALF_UP);
  }

  public MathContext(int setPrecision, RoundingMode setRoundingMode) {
    if (setPrecision < kMinDigits) {
      throw new IllegalArgumentException("Digits < 0");
    }
    if (setRoundingMode == null) {
      throw new NullPointerException("null RoundingMode");
    }
    precision = setPrecision;
    roundingMode = setRoundingMode;
  }

  public MathContext(String val) {
    int setPrecision;
    if (val == null) {
      throw new NullPointerException("null String");
    }
    try {
      if (!val.startsWith("precision=")) {
        throw new RuntimeException();
      }
      int fence = val.indexOf(' ');
      int off = 10;
      setPrecision = Integer.parseInt(val, off, fence, 10);
      if (!val.startsWith("roundingMode=", fence + 1)) {
        throw new RuntimeException();
      }
      off = fence + 1 + 13;
      String str = val.substring(off, val.length());
      roundingMode = RoundingMode.valueOf(str);
    } catch (RuntimeException re) {
      throw new IllegalArgumentException("bad string format");
    }
    if (setPrecision < kMinDigits) {
      throw new IllegalArgumentException("Digits < 0");
    }
    precision = setPrecision;
  }

  public int getPrecision() {
    return precision;
  }

  public RoundingMode getRoundingMode() {
    return roundingMode;
  }

  @Override
  public boolean equals(Object x) {
    MathContext mc;
    if (!(x instanceof MathContext)) {
      return false;
    }
    mc = (MathContext) x;
    return mc.precision == this.precision && mc.roundingMode == this.roundingMode;
  }

  @Override
  public int hashCode() {
    return this.precision + roundingMode.hashCode() * 59;
  }

  @Override
  public String toString() {
    return "precision=" + precision + " roundingMode=" + roundingMode.toString();
  }

  private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    s.defaultReadObject();
    if (precision < kMinDigits) {
      String message = "MathContext: invalid digits in stream";
      throw new java.io.StreamCorruptedException(message);
    }
    if (roundingMode == null) {
      String message = "MathContext: null roundingMode in stream";
      throw new java.io.StreamCorruptedException(message);
    }
  }
}
