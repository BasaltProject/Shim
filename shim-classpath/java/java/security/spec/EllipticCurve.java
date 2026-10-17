/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.spec;

import java.math.BigInteger;

public class EllipticCurve {
  private final ECField field;
  private final BigInteger a;
  private final BigInteger b;
  private final byte[] seed;

  private static void checkValidity(ECField field, BigInteger c, String cName) {
    if (field instanceof ECFieldFp) {
      BigInteger p = ((ECFieldFp) field).getP();
      if (p.compareTo(c) != 1) {
        throw new IllegalArgumentException(cName + " is too large");
      } else if (c.signum() < 0) {
        throw new IllegalArgumentException(cName + " is negative");
      }
    } else if (field instanceof ECFieldF2m) {
      int m = ((ECFieldF2m) field).getM();
      if (c.bitLength() > m) {
        throw new IllegalArgumentException(cName + " is too large");
      }
    }
  }

  public EllipticCurve(ECField field, BigInteger a, BigInteger b) {
    this(field, a, b, null);
  }

  public EllipticCurve(ECField field, BigInteger a, BigInteger b, byte[] seed) {
    if (field == null) {
      throw new NullPointerException("field is null");
    }
    if (a == null) {
      throw new NullPointerException("first coefficient is null");
    }
    if (b == null) {
      throw new NullPointerException("second coefficient is null");
    }
    checkValidity(field, a, "first coefficient");
    checkValidity(field, b, "second coefficient");
    this.field = field;
    this.a = a;
    this.b = b;
    this.seed = seed == null ? null : seed.clone();
  }

  public ECField getField() {
    return field;
  }

  public BigInteger getA() {
    return a;
  }

  public BigInteger getB() {
    return b;
  }

  public byte[] getSeed() {
    if (seed == null) {
      return null;
    }
    return seed.clone();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof EllipticCurve) {
      EllipticCurve curve = (EllipticCurve) obj;
      return field.equals(curve.field) && a.equals(curve.a) && b.equals(curve.b);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (field.hashCode() << 6) + (a.hashCode() << 4) + (b.hashCode() << 2);
  }
}
