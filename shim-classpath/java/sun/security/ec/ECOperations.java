/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ec;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;

public final class ECOperations {
  private final BigInteger p;
  private final BigInteger a;
  private final BigInteger b;
  private final BigInteger n;
  private final BigInteger gx;
  private final BigInteger gy;
  private final int fieldBits;
  private final boolean aMinus3;

  public ECOperations(ECParameterSpec params) {
    EllipticCurve c = params.getCurve();
    this.p = ((ECFieldFp) c.getField()).getP();
    this.a = c.getA();
    this.b = c.getB();
    this.n = params.getOrder();
    this.gx = params.getGenerator().getAffineX();
    this.gy = params.getGenerator().getAffineY();
    this.fieldBits = p.bitLength();
    this.aMinus3 = a.equals(p.subtract(BigInteger.valueOf(3)));
  }

  public BigInteger order() {
    return n;
  }

  public BigInteger prime() {
    return p;
  }

  public int fieldBytes() {
    return (fieldBits + 7) >> 3;
  }

  public int orderBits() {
    return n.bitLength();
  }

  private static final class Point {
    final BigInteger x;
    final BigInteger y;
    final BigInteger z;

    Point(BigInteger x, BigInteger y, BigInteger z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    boolean isInfinity() {
      return z.signum() == 0;
    }
  }

  private static final Point kInfinity = new Point(BigInteger.ONE, BigInteger.ONE, BigInteger.ZERO);

  private BigInteger mul(BigInteger x, BigInteger y) {
    return x.multiply(y).mod(p);
  }

  private BigInteger sqr(BigInteger x) {
    return x.multiply(x).mod(p);
  }

  private BigInteger sub(BigInteger x, BigInteger y) {
    BigInteger r = x.subtract(y);
    return r.signum() < 0 ? r.add(p) : r;
  }

  private BigInteger add(BigInteger x, BigInteger y) {
    BigInteger r = x.add(y);
    return r.compareTo(p) >= 0 ? r.subtract(p) : r;
  }

  private Point toJacobian(ECPoint pt) {
    return new Point(pt.getAffineX(), pt.getAffineY(), BigInteger.ONE);
  }

  private Point dbl(Point q) {
    if (q.isInfinity() || q.y.signum() == 0) {
      return kInfinity;
    }
    BigInteger xx = sqr(q.x);
    BigInteger yy = sqr(q.y);
    BigInteger yyyy = sqr(yy);
    BigInteger zz = sqr(q.z);
    BigInteger s = mul(BigInteger.valueOf(4), mul(q.x, yy));
    BigInteger m;
    if (aMinus3) {
      m = mul(BigInteger.valueOf(3), mul(sub(q.x, zz), add(q.x, zz)));
    } else {
      m = add(mul(BigInteger.valueOf(3), xx), mul(a, sqr(zz)));
    }
    BigInteger x3 = sub(sqr(m), add(s, s));
    BigInteger y3 = sub(mul(m, sub(s, x3)), mul(BigInteger.valueOf(8), yyyy));
    BigInteger z3 = mul(BigInteger.TWO, mul(q.y, q.z));
    return new Point(x3, y3, z3);
  }

  private Point addPoints(Point p1, Point p2) {
    if (p1.isInfinity()) {
      return p2;
    }
    if (p2.isInfinity()) {
      return p1;
    }
    BigInteger z1z1 = sqr(p1.z);
    BigInteger z2z2 = sqr(p2.z);
    BigInteger u1 = mul(p1.x, z2z2);
    BigInteger u2 = mul(p2.x, z1z1);
    BigInteger s1 = mul(p1.y, mul(p2.z, z2z2));
    BigInteger s2 = mul(p2.y, mul(p1.z, z1z1));
    if (u1.equals(u2)) {
      if (s1.equals(s2)) {
        return dbl(p1);
      }
      return kInfinity;
    }
    BigInteger h = sub(u2, u1);
    BigInteger r = sub(s2, s1);
    BigInteger hh = sqr(h);
    BigInteger hhh = mul(h, hh);
    BigInteger v = mul(u1, hh);
    BigInteger x3 = sub(sub(sqr(r), hhh), add(v, v));
    BigInteger y3 = sub(mul(r, sub(v, x3)), mul(s1, hhh));
    BigInteger z3 = mul(h, mul(p1.z, p2.z));
    return new Point(x3, y3, z3);
  }

  private ECPoint toAffine(Point q) {
    if (q.isInfinity()) {
      return ECPoint.POINT_INFINITY;
    }
    BigInteger zInv = q.z.modInverse(p);
    BigInteger zInv2 = sqr(zInv);
    return new ECPoint(mul(q.x, zInv2), mul(q.y, mul(zInv2, zInv)));
  }

  private Point multiplyJ(Point base, BigInteger k) {
    Point[] table = new Point[16];
    table[0] = kInfinity;
    table[1] = base;
    for (int i = 2; i < 16; i++) {
      table[i] = (i & 1) == 0 ? dbl(table[i / 2]) : addPoints(table[i - 1], base);
    }
    Point acc = kInfinity;
    int bits = k.bitLength();
    int top = ((bits + 3) / 4) * 4;
    for (int i = top - 4; i >= 0; i -= 4) {
      acc = dbl(dbl(dbl(dbl(acc))));
      int window = 0;
      for (int j = 3; j >= 0; j--) {
        window = (window << 1) | (k.testBit(i + j) ? 1 : 0);
      }
      if (window != 0) {
        acc = addPoints(acc, table[window]);
      }
    }
    return acc;
  }

  public ECPoint multiply(ECPoint point, BigInteger k) {
    return toAffine(multiplyJ(toJacobian(point), k.mod(n)));
  }

  public ECPoint multiplyG(BigInteger k) {
    return toAffine(multiplyJ(new Point(gx, gy, BigInteger.ONE), k.mod(n)));
  }

  public ECPoint multiplyAdd(BigInteger k1, ECPoint q, BigInteger k2) {
    Point g = new Point(gx, gy, BigInteger.ONE);
    Point q1 = multiplyJ(g, k1.mod(n));
    Point q2 = multiplyJ(toJacobian(q), k2.mod(n));
    return toAffine(addPoints(q1, q2));
  }

  public boolean isOnCurve(ECPoint pt) {
    if (pt == ECPoint.POINT_INFINITY) {
      return false;
    }
    BigInteger x = pt.getAffineX();
    BigInteger y = pt.getAffineY();
    if (x.signum() < 0 || x.compareTo(p) >= 0 || y.signum() < 0 || y.compareTo(p) >= 0) {
      return false;
    }
    BigInteger lhs = sqr(y);
    BigInteger rhs = add(add(mul(x, sqr(x)), mul(a, x)), b);
    return lhs.equals(rhs);
  }

  public BigInteger randomScalar(SecureRandom random) {
    int bits = n.bitLength();
    BigInteger k;
    do {
      k = new BigInteger(bits + 64, random).mod(n.subtract(BigInteger.ONE)).add(BigInteger.ONE);
    } while (k.signum() == 0);
    return k;
  }
}
