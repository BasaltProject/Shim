/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.util;

import java.math.BigInteger;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;

public final class NamedCurve extends ECParameterSpec {
  private final String name;
  private final String oid;

  NamedCurve(String name, String oid, EllipticCurve curve, ECPoint g, BigInteger n, int h) {
    super(curve, g, n, h);
    this.name = name;
    this.oid = oid;
  }

  public String getName() {
    return name;
  }

  public String getObjectId() {
    return oid;
  }

  @Override
  public String toString() {
    return name + " (" + oid + ")";
  }
}
