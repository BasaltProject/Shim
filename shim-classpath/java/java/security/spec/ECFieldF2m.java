/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

import java.math.BigInteger;

public class ECFieldF2m implements ECField {
  private final int m;
  private final int[] ks;
  private final BigInteger rp;

  public ECFieldF2m(int m) {
    if (m <= 0) {
      throw new IllegalArgumentException("m is not positive");
    }
    this.m = m;
    this.ks = null;
    this.rp = null;
  }

  public ECFieldF2m(int m, BigInteger rp) {
    this.m = m;
    this.rp = rp;
    if (m <= 0) {
      throw new IllegalArgumentException("m is not positive");
    }
    int bitCount = this.rp.bitCount();
    if (!this.rp.testBit(0) || !this.rp.testBit(m) || ((bitCount != 3) && (bitCount != 5))) {
      throw new IllegalArgumentException("rp does not represent a valid reduction polynomial");
    }
    BigInteger temp = this.rp.clearBit(0).clearBit(m);
    this.ks = new int[bitCount - 2];
    for (int i = this.ks.length - 1; i >= 0; i--) {
      int index = temp.getLowestSetBit();
      this.ks[i] = index;
      temp = temp.clearBit(index);
    }
  }

  public ECFieldF2m(int m, int[] ks) {
    this.m = m;
    this.ks = ks.clone();
    if (m <= 0) {
      throw new IllegalArgumentException("m is not positive");
    }
    if ((this.ks.length != 1) && (this.ks.length != 3)) {
      throw new IllegalArgumentException("length of ks is neither 1 nor 3");
    }
    for (int i = 0; i < this.ks.length; i++) {
      if ((this.ks[i] < 1) || (this.ks[i] > m - 1)) {
        throw new IllegalArgumentException("ks[" + i + "] is out of range");
      }
      if ((i != 0) && (this.ks[i] >= this.ks[i - 1])) {
        throw new IllegalArgumentException("values in ks are not in descending order");
      }
    }
    BigInteger r = BigInteger.ONE;
    r = r.setBit(m);
    for (int j = 0; j < this.ks.length; j++) {
      r = r.setBit(this.ks[j]);
    }
    this.rp = r;
  }

  @Override
  public int getFieldSize() {
    return m;
  }

  public int getM() {
    return m;
  }

  public BigInteger getReductionPolynomial() {
    return rp;
  }

  public int[] getMidTermsOfReductionPolynomial() {
    if (ks == null) {
      return null;
    }
    return ks.clone();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ECFieldF2m) {
      return (m == ((ECFieldF2m) obj).m) && java.util.Objects.equals(rp, ((ECFieldF2m) obj).rp);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int value = m << 5;
    value += (rp == null ? 0 : rp.hashCode());
    return value;
  }
}
