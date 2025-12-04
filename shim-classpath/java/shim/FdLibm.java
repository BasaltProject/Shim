/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package shim;

public final class FdLibm {
  private FdLibm() { }

  private static int hi(double x) { return (int) (Double.doubleToRawLongBits(x) >> 32); }
  private static int lo(double x) { return (int) Double.doubleToRawLongBits(x); }
  private static double withHi(double x, int h) {
    return Double.longBitsToDouble(((long) h << 32) | (Double.doubleToRawLongBits(x) & 0xffffffffL));
  }
  private static double make(int h, int l) {
    return Double.longBitsToDouble(((long) h << 32) | (l & 0xffffffffL));
  }

  private static final double P1 = 1.66666666666666019037e-01, P2 = -2.77777777770155933842e-03,
      P3 = 6.61375632143793436117e-05, P4 = -1.65339022054652515390e-06, P5 = 4.13813679705723846039e-08;
  private static final double LN2HI = 6.93147180369123816490e-01, LN2LO = 1.90821492927058770002e-10,
      INVLN2 = 1.44269504088896338700e+00;

  public static double exp(double x) {
    double hi = 0, lo = 0, c, t, y;
    int k = 0;
    int h = hi(x);
    int xsb = (h >>> 31) & 1;
    h &= 0x7fffffff;
    if (h >= 0x40862E42) {
      if (h >= 0x7ff00000) {
        if (((h & 0xfffff) | lo(x)) != 0) return x + x;
        return xsb == 0 ? x : 0.0;
      }
      if (x > 7.09782712893383973096e+02) return Double.POSITIVE_INFINITY;
      if (x < -7.45133219101941108420e+02) return 0.0;
    }
    if (h > 0x3fd62e42) {
      if (h < 0x3FF0A2B2) {
        hi = x - (xsb == 0 ? LN2HI : -LN2HI);
        lo = xsb == 0 ? LN2LO : -LN2LO;
        k = 1 - xsb - xsb;
      } else {
        k = (int) (INVLN2 * x + (xsb == 0 ? 0.5 : -0.5));
        t = k;
        hi = x - t * LN2HI;
        lo = t * LN2LO;
      }
      x = hi - lo;
    } else if (h < 0x3e300000) {
      if (1.0e300 + x > 1.0) return 1.0 + x;
    } else {
      k = 0;
    }
    t = x * x;
    c = x - t * (P1 + t * (P2 + t * (P3 + t * (P4 + t * P5))));
    if (k == 0) return 1.0 - ((x * c) / (c - 2.0) - x);
    y = 1.0 - ((lo - (x * c) / (2.0 - c)) - hi);
    if (k >= -1021) {
      if (k == 1024) return y * 2.0 * 0x1p1023;
      return y * make(0x3ff00000 + (k << 20), 0);
    }
    return y * make(0x3ff00000 + ((k + 1000) << 20), 0) * 0x1p-1000;
  }

  private static final double S1 = -1.66666666666666324348e-01, S2 = 8.33333333332248946124e-03,
      S3 = -1.98412698298579493134e-04, S4 = 2.75573137070700676789e-06, S5 = -2.50507602534068634195e-08,
      S6 = 1.58969099521155010221e-10;
  private static final double C1 = 4.16666666666666019037e-02, C2 = -1.38888888888741095749e-03,
      C3 = 2.48015872894767294178e-05, C4 = -2.75573143513906633035e-07, C5 = 2.08757232129817482790e-09,
      C6 = -1.13596475577881948265e-11;

  private static double kSin(double x, double y, int iy) {
    int ix = hi(x) & 0x7fffffff;
    if (ix < 0x3e400000) {
      if ((int) x == 0) return x;
    }
    double z = x * x, v = z * x, r = S2 + z * (S3 + z * (S4 + z * (S5 + z * S6)));
    if (iy == 0) return x + v * (S1 + z * r);
    return x - ((z * (0.5 * y - v * r) - y) - v * S1);
  }

  private static double kCos(double x, double y) {
    int ix = hi(x) & 0x7fffffff;
    if (ix < 0x3e400000) {
      if ((int) x == 0) return 1.0;
    }
    double z = x * x, r = z * (C1 + z * (C2 + z * (C3 + z * (C4 + z * (C5 + z * C6)))));
    if (ix < 0x3FD33333) return 1.0 - (0.5 * z - (z * r - x * y));
    double qx = ix > 0x3fe90000 ? 0.28125 : make(ix - 0x00200000, 0);
    double hz = 0.5 * z - qx;
    double a = 1.0 - qx;
    return a - (hz - (z * r - x * y));
  }

  private static final double INVPIO2 = 6.36619772367581382433e-01, PIO2_1 = 1.57079632673412561417e+00,
      PIO2_1T = 6.07710050650619224932e-11, PIO2_2 = 6.07710050630396597660e-11,
      PIO2_2T = 2.02226624879595063154e-21, PIO2_3 = 2.02226624871116645580e-21,
      PIO2_3T = 8.47842766036889956997e-32;

  // returns n, fills y; null y when unsupported
  private static int remPio2(double x, double[] y) {
    int h = hi(x);
    int ix = h & 0x7fffffff;
    double t = Math.abs(x);
    int n = (int) (t * INVPIO2 + 0.5);
    double fn = n;
    double r = t - fn * PIO2_1;
    double w = fn * PIO2_1T;
    double y0 = r - w;
    int j = ix >> 20;
    int i = j - ((hi(y0) >> 20) & 0x7ff);
    if (i > 16) {
      t = r;
      w = fn * PIO2_2;
      r = t - w;
      w = fn * PIO2_2T - ((t - r) - w);
      y0 = r - w;
      i = j - ((hi(y0) >> 20) & 0x7ff);
      if (i > 49) {
        t = r;
        w = fn * PIO2_3;
        r = t - w;
        w = fn * PIO2_3T - ((t - r) - w);
        y0 = r - w;
      }
    }
    double y1 = (r - y0) - w;
    if (h < 0) {
      y[0] = -y0;
      y[1] = -y1;
      return -n;
    }
    y[0] = y0;
    y[1] = y1;
    return n;
  }


  private static final int[] TWO_OVER_PI = {
      10680707, 7228996, 1387004, 2578385, 16069853, 12639074, 9804092, 4427841,
      16666979, 11263675, 12935607, 2387514, 4345298, 14681673, 3074569, 13734428,
      16653803, 1880361, 10960616, 8533493, 3062596, 8710556, 7349940, 6258241,
      3772886, 3769171, 3798172, 8675211, 12450088, 3874808, 9961438, 366607,
      15675153, 9132554, 7151469, 3571407, 2607881, 12013382, 4155038, 6285869,
      7677882, 13102053, 15825725, 473591, 9065106, 15363067, 6271263, 9264392,
      5636912, 4652155, 7056368, 13614112, 10155062, 1944035, 9527646, 15080200,
      6658437, 6231200, 6832269, 16767104, 5075751, 3212806, 1398474, 7579849,
      6349435, 12618859};
  private static final double[] PIO2 = {1.570796251296997, 7.549789415861596E-8, 5.390302529957765E-15,
      3.282003415807913E-22, 1.270655753080676E-29, 1.2293330898111133E-36, 2.7337005381646456E-44,
      2.1674168387780482E-51};
  private static final double TWO24 = 16777216.0, TWON24 = 5.960464477539063E-8;

  private static int kernelRemPio2(double[] x, double[] y, int e0, int nx) {
    int[] iq = new int[20];
    double[] f = new double[20];
    double[] q = new double[20];
    double[] fq = new double[20];
    int jk = 4, jp = jk, jx = nx - 1, jv = (e0 - 3) / 24;
    if (jv < 0) jv = 0;
    int q0 = e0 - 24 * (jv + 1);
    int j = jv - jx, m = jx + jk, i, k, n, ih, jz;
    double z, fw;
    for (i = 0; i <= m; i++, j++) f[i] = j < 0 ? 0.0 : (double) TWO_OVER_PI[j];
    for (i = 0; i <= jk; i++) {
      for (j = 0, fw = 0.0; j <= jx; j++) fw += x[j] * f[jx + i - j];
      q[i] = fw;
    }
    jz = jk;
    while (true) {
      for (i = 0, j = jz, z = q[jz]; j > 0; i++, j--) {
        fw = (double) ((int) (TWON24 * z));
        iq[i] = (int) (z - TWO24 * fw);
        z = q[j - 1] + fw;
      }
      z = Math.scalb(z, q0);
      z -= 8.0 * Math.floor(z * 0.125);
      n = (int) z;
      z -= (double) n;
      ih = 0;
      if (q0 > 0) {
        i = iq[jz - 1] >> (24 - q0);
        n += i;
        iq[jz - 1] -= i << (24 - q0);
        ih = iq[jz - 1] >> (23 - q0);
      } else if (q0 == 0) {
        ih = iq[jz - 1] >> 23;
      } else if (z >= 0.5) {
        ih = 2;
      }
      if (ih > 0) {
        n += 1;
        int carry = 0;
        for (i = 0; i < jz; i++) {
          j = iq[i];
          if (carry == 0) {
            if (j != 0) {
              carry = 1;
              iq[i] = 0x1000000 - j;
            }
          } else {
            iq[i] = 0xffffff - j;
          }
        }
        if (q0 > 0) {
          switch (q0) {
            case 1: iq[jz - 1] &= 0x7fffff; break;
            case 2: iq[jz - 1] &= 0x3fffff; break;
            default: break;
          }
        }
        if (ih == 2) {
          z = 1.0 - z;
          if (carry != 0) z -= Math.scalb(1.0, q0);
        }
      }
      if (z == 0.0) {
        j = 0;
        for (i = jz - 1; i >= jk; i--) j |= iq[i];
        if (j == 0) {
          for (k = 1; iq[jk - k] == 0; k++) { }
          for (i = jz + 1; i <= jz + k; i++) {
            f[jx + i] = (double) TWO_OVER_PI[jv + i];
            for (j = 0, fw = 0.0; j <= jx; j++) fw += x[j] * f[jx + i - j];
            q[i] = fw;
          }
          jz += k;
          continue;
        }
      }
      break;
    }
    if (z == 0.0) {
      jz -= 1;
      q0 -= 24;
      while (iq[jz] == 0) {
        jz--;
        q0 -= 24;
      }
    } else {
      z = Math.scalb(z, -q0);
      if (z >= TWO24) {
        fw = (double) ((int) (TWON24 * z));
        iq[jz] = (int) (z - TWO24 * fw);
        jz += 1;
        q0 += 24;
        iq[jz] = (int) fw;
      } else {
        iq[jz] = (int) z;
      }
    }
    fw = Math.scalb(1.0, q0);
    for (i = jz; i >= 0; i--) {
      q[i] = fw * (double) iq[i];
      fw *= TWON24;
    }
    for (i = jz; i >= 0; i--) {
      for (fw = 0.0, k = 0; k <= jp && k <= jz - i; k++) fw += PIO2[k] * q[i + k];
      fq[jz - i] = fw;
    }
    fw = 0.0;
    for (i = jz; i >= 0; i--) fw += fq[i];
    y[0] = ih == 0 ? fw : -fw;
    fw = fq[0] - fw;
    for (i = 1; i <= jz; i++) fw += fq[i];
    y[1] = ih == 0 ? fw : -fw;
    return n & 7;
  }

  private static int remPio2Large(double x, double[] y) {
    int hx = hi(x);
    int ix = hx & 0x7fffffff;
    int e0 = (ix >> 20) - 1046;
    double z = make(ix - (e0 << 20), lo(x));
    double[] tx = new double[3];
    for (int i = 0; i < 2; i++) {
      tx[i] = (double) ((int) z);
      z = (z - tx[i]) * TWO24;
    }
    tx[2] = z;
    int nx = 3;
    while (tx[nx - 1] == 0.0) nx--;
    int n = kernelRemPio2(tx, y, e0, nx);
    if (hx < 0) {
      y[0] = -y[0];
      y[1] = -y[1];
      return -n;
    }
    return n;
  }

  public static double sin(double x) {
    int ix = hi(x) & 0x7fffffff;
    if (ix <= 0x3fe921fb) return kSin(x, 0.0, 0);
    if (ix >= 0x7ff00000) return x - x;
    double[] y = new double[2];
    int n = ix >= 0x413921fb ? remPio2Large(x, y) : remPio2(x, y);
    switch (n & 3) {
      case 0: return kSin(y[0], y[1], 1);
      case 1: return kCos(y[0], y[1]);
      case 2: return -kSin(y[0], y[1], 1);
      default: return -kCos(y[0], y[1]);
    }
  }

  public static double cos(double x) {
    int ix = hi(x) & 0x7fffffff;
    if (ix <= 0x3fe921fb) return kCos(x, 0.0);
    if (ix >= 0x7ff00000) return x - x;
    double[] y = new double[2];
    int n = ix >= 0x413921fb ? remPio2Large(x, y) : remPio2(x, y);
    switch (n & 3) {
      case 0: return kCos(y[0], y[1]);
      case 1: return -kSin(y[0], y[1], 1);
      case 2: return -kCos(y[0], y[1]);
      default: return kSin(y[0], y[1], 1);
    }
  }


  private static final double[] ATANHI = {4.63647609000806093515e-01, 7.85398163397448278999e-01,
      9.82793723247329054082e-01, 1.57079632679489655800e+00};
  private static final double[] ATANLO = {2.26987774529616870924e-17, 3.06161699786838301793e-17,
      1.39033110312309984516e-17, 6.12323399573676603587e-17};
  private static final double[] AT = {3.33333333333329318027e-01, -1.99999999998764832476e-01,
      1.42857142725034663711e-01, -1.11111104054623557880e-01, 9.09088713343650656196e-02,
      -7.69187620504482999495e-02, 6.66107313738753120669e-02, -5.83357013379057348645e-02,
      4.97687799461593236017e-02, -3.65315727442169155270e-02, 1.62858201153657823623e-02};

  public static double atan(double x) {
    int hx = hi(x);
    int ix = hx & 0x7fffffff;
    int id;
    if (ix >= 0x44100000) {
      if (ix > 0x7ff00000 || (ix == 0x7ff00000 && lo(x) != 0)) return x + x;
      return hx > 0 ? ATANHI[3] + ATANLO[3] : -ATANHI[3] - ATANLO[3];
    }
    if (ix < 0x3fdc0000) {
      if (ix < 0x3e200000) {
        if (1.0e300 + x > 1.0) return x;
      }
      id = -1;
    } else {
      x = Math.abs(x);
      if (ix < 0x3ff30000) {
        if (ix < 0x3fe60000) {
          id = 0;
          x = (2.0 * x - 1.0) / (2.0 + x);
        } else {
          id = 1;
          x = (x - 1.0) / (x + 1.0);
        }
      } else {
        if (ix < 0x40038000) {
          id = 2;
          x = (x - 1.5) / (1.0 + 1.5 * x);
        } else {
          id = 3;
          x = -1.0 / x;
        }
      }
    }
    double z = x * x;
    double w = z * z;
    double s1 = z * (AT[0] + w * (AT[2] + w * (AT[4] + w * (AT[6] + w * (AT[8] + w * AT[10])))));
    double s2 = w * (AT[1] + w * (AT[3] + w * (AT[5] + w * (AT[7] + w * AT[9]))));
    if (id < 0) return x - x * (s1 + s2);
    z = ATANHI[id] - ((x * (s1 + s2) - ATANLO[id]) - x);
    return hx < 0 ? -z : z;
  }

  public static double atan2(double y, double x) {
    final double tiny = 1.0e-300, zero = 0.0, piO4 = 7.8539816339744827900e-01, piO2 = 1.5707963267948965580e+00,
        pi = 3.1415926535897931160e+00, piLo = 1.2246467991473531772e-16;
    int hx = hi(x), lx = lo(x), hy = hi(y), ly = lo(y);
    int ix = hx & 0x7fffffff, iy = hy & 0x7fffffff;
    if ((ix | ((lx | -lx) >>> 31)) > 0x7ff00000 || (iy | ((ly | -ly) >>> 31)) > 0x7ff00000) return x + y;
    if (((hx - 0x3ff00000) | lx) == 0) return atan(y);
    int m = ((hy >> 31) & 1) | ((hx >> 30) & 2);
    if ((iy | ly) == 0) {
      switch (m) {
        case 0:
        case 1: return y;
        case 2: return pi + tiny;
        default: return -pi - tiny;
      }
    }
    if ((ix | lx) == 0) return hy < 0 ? -piO2 - tiny : piO2 + tiny;
    if (ix == 0x7ff00000) {
      if (iy == 0x7ff00000) {
        switch (m) {
          case 0: return piO4 + tiny;
          case 1: return -piO4 - tiny;
          case 2: return 3.0 * piO4 + tiny;
          default: return -3.0 * piO4 - tiny;
        }
      }
      switch (m) {
        case 0: return zero;
        case 1: return -zero;
        case 2: return pi + tiny;
        default: return -pi - tiny;
      }
    }
    if (iy == 0x7ff00000) return hy < 0 ? -piO2 - tiny : piO2 + tiny;
    int k = (iy - ix) >> 20;
    double z;
    if (k > 60) {
      z = piO2 + 0.5 * piLo;
    } else if (hx < 0 && k < -60) {
      z = 0.0;
    } else {
      z = atan(Math.abs(y / x));
    }
    switch (m) {
      case 0: return z;
      case 1: return -z;
      case 2: return pi - (z - piLo);
      default: return (z - piLo) - pi;
    }
  }

  public static double cosh(double x) {
    int ix = hi(x) & 0x7fffffff;
    if (ix >= 0x7ff00000) return x * x;
    if (ix < 0x3fd62e43) {
      double t = Math.expm1(Math.abs(x));
      double w = 1.0 + t;
      if (ix < 0x3c800000) return w;
      return 1.0 + (t * t) / (w + w);
    }
    if (ix < 0x40360000) {
      double t = exp(Math.abs(x));
      return 0.5 * t + 0.5 / t;
    }
    if (ix < 0x40862E42) return 0.5 * exp(Math.abs(x));
    if (Math.abs(x) <= 7.10475860073943863426e+02) {
      double w = exp(0.5 * Math.abs(x));
      double t = 0.5 * w;
      return t * w;
    }
    return Double.POSITIVE_INFINITY;
  }

  public static double hypot(double x, double y) {
    double a, b, t1, t2, y1, y2, w;
    int j, k = 0, ha, hb;
    ha = hi(x) & 0x7fffffff;
    hb = hi(y) & 0x7fffffff;
    if (hb > ha) {
      a = y;
      b = x;
      j = ha;
      ha = hb;
      hb = j;
    } else {
      a = x;
      b = y;
    }
    a = Math.abs(a);
    b = Math.abs(b);
    if (ha - hb > 0x3c00000) return a + b;
    if (ha > 0x5f300000) {
      if (ha >= 0x7ff00000) {
        w = a + b;
        if (((ha & 0xfffff) | lo(a)) == 0) w = a;
        if (((hb ^ 0x7ff00000) | lo(b)) == 0) w = b;
        return w;
      }
      ha -= 0x25800000;
      hb -= 0x25800000;
      k += 600;
      a = withHi(a, ha);
      b = withHi(b, hb);
    }
    if (hb < 0x20b00000) {
      if (hb <= 0x000fffff) {
        if ((hb | lo(b)) == 0) return a;
        t1 = make(0x7fd00000, 0);
        b *= t1;
        a *= t1;
        k -= 1022;
      } else {
        ha += 0x25800000;
        hb += 0x25800000;
        k -= 600;
        a = withHi(a, ha);
        b = withHi(b, hb);
      }
    }
    w = a - b;
    if (w > b) {
      t1 = make(ha, 0);
      t2 = a - t1;
      w = Math.sqrt(t1 * t1 - (b * (-b) - t2 * (a + t1)));
    } else {
      a = a + a;
      y1 = make(hb, 0);
      y2 = b - y1;
      t1 = make(ha + 0x00100000, 0);
      t2 = a - t1;
      w = Math.sqrt(t1 * y1 - (w * (-w) - (t1 * y2 + t2 * b)));
    }
    if (k != 0) return make(0x3ff00000 + (k << 20), 0) * w;
    return w;
  }


  private static final double[] BP = {1.0, 1.5};
  private static final double[] DP_H = {0.0, 5.84962487220764160156e-01};
  private static final double[] DP_L = {0.0, 1.35003920212974897128e-08};
  private static final double L1 = 5.99999999999994648725e-01, L2 = 4.28571428578550184252e-01,
      L3 = 3.33333329818377432918e-01, L4 = 2.72728123808534006489e-01, L5 = 2.30660745775561754067e-01,
      L6 = 2.06975017800338417784e-01, LG2 = 6.93147180559945286227e-01, LG2_H = 6.93147182464599609375e-01,
      LG2_L = -1.90465429995776804525e-09, OVT = 8.0085662595372944372e-17, CP = 9.61796693925975554329e-01,
      CP_H = 9.61796700954437255859e-01, CP_L = -7.02846165095275826516e-09, IVLN2 = 1.44269504088896338700e+00,
      IVLN2_H = 1.44269502162933349609e+00, IVLN2_L = 1.92596299112661746887e-08;

  private static double low0(double v) { return make(hi(v), 0); }

  public static double pow(double x, double y) {
    final double huge = 1.0e300, tiny = 1.0e-300, two53 = 9007199254740992.0;
    double z, ax, z_h, z_l, p_h, p_l, y1, t1, t2, r, s, t, u, v, w;
    int i, j, k, yisint, n;
    int hx = hi(x), hy = hi(y), ix = hx & 0x7fffffff, iy = hy & 0x7fffffff;
    int lx = lo(x), ly = lo(y);
    if ((iy | ly) == 0) return 1.0;
    if (ix > 0x7ff00000 || (ix == 0x7ff00000 && lx != 0) || iy > 0x7ff00000 || (iy == 0x7ff00000 && ly != 0)) {
      return x + y;
    }
    yisint = 0;
    if (hx < 0) {
      if (iy >= 0x43400000) {
        yisint = 2;
      } else if (iy >= 0x3ff00000) {
        k = (iy >> 20) - 0x3ff;
        if (k > 20) {
          j = ly >>> (52 - k);
          if ((j << (52 - k)) == ly) yisint = 2 - (j & 1);
        } else if (ly == 0) {
          j = iy >> (20 - k);
          if ((j << (20 - k)) == iy) yisint = 2 - (j & 1);
        }
      }
    }
    if (ly == 0) {
      if (iy == 0x7ff00000) {
        if (((ix - 0x3ff00000) | lx) == 0) return y - y;
        else if (ix >= 0x3ff00000) return hy >= 0 ? y : 0.0;
        else return hy < 0 ? -y : 0.0;
      }
      if (iy == 0x3ff00000) return hy < 0 ? 1.0 / x : x;
      if (hy == 0x40000000) return x * x;
      if (hy == 0x3fe00000) {
        if (hx >= 0) return Math.sqrt(x);
      }
    }
    ax = Math.abs(x);
    if (lx == 0) {
      if (ix == 0x7ff00000 || ix == 0 || ix == 0x3ff00000) {
        z = ax;
        if (hy < 0) z = 1.0 / z;
        if (hx < 0) {
          if (((ix - 0x3ff00000) | yisint) == 0) {
            z = (z - z) / (z - z);
          } else if (yisint == 1) {
            z = -z;
          }
        }
        return z;
      }
    }
    n = (hx >>> 31) - 1;
    if ((n | yisint) == 0) return (x - x) / (x - x);
    s = 1.0;
    if ((n | (yisint - 1)) == 0) s = -1.0;
    if (iy > 0x41e00000) {
      if (iy > 0x43f00000) {
        if (ix <= 0x3fefffff) return hy < 0 ? huge * huge : tiny * tiny;
        if (ix >= 0x3ff00000) return hy > 0 ? huge * huge : tiny * tiny;
      }
      if (ix < 0x3fefffff) return hy < 0 ? s * huge * huge : s * tiny * tiny;
      if (ix > 0x3ff00000) return hy > 0 ? s * huge * huge : s * tiny * tiny;
      t = ax - 1.0;
      w = (t * t) * (0.5 - t * (0.3333333333333333333333 - t * 0.25));
      u = IVLN2_H * t;
      v = t * IVLN2_L - w * IVLN2;
      t1 = low0(u + v);
      t2 = v - (t1 - u);
    } else {
      double s2, s_h, s_l, t_h, t_l;
      n = 0;
      if (ix < 0x00100000) {
        ax *= two53;
        n -= 53;
        ix = hi(ax);
      }
      n += (ix >> 20) - 0x3ff;
      j = ix & 0x000fffff;
      ix = j | 0x3ff00000;
      if (j <= 0x3988E) {
        k = 0;
      } else if (j < 0xBB67A) {
        k = 1;
      } else {
        k = 0;
        n += 1;
        ix -= 0x00100000;
      }
      ax = withHi(ax, ix);
      u = ax - BP[k];
      v = 1.0 / (ax + BP[k]);
      double ss = u * v;
      s_h = low0(ss);
      t_h = make(((ix >> 1) | 0x20000000) + 0x00080000 + (k << 18), 0);
      t_l = ax - (t_h - BP[k]);
      s_l = v * ((u - s_h * t_h) - s_h * t_l);
      s2 = ss * ss;
      r = s2 * s2 * (L1 + s2 * (L2 + s2 * (L3 + s2 * (L4 + s2 * (L5 + s2 * L6)))));
      r += s_l * (s_h + ss);
      s2 = s_h * s_h;
      t_h = low0(3.0 + s2 + r);
      t_l = r - ((t_h - 3.0) - s2);
      u = s_h * t_h;
      v = s_l * t_h + t_l * ss;
      p_h = low0(u + v);
      p_l = v - (p_h - u);
      z_h = CP_H * p_h;
      z_l = CP_L * p_h + p_l * CP + DP_L[k];
      t = n;
      t1 = low0(((z_h + z_l) + DP_H[k]) + t);
      t2 = z_l - (((t1 - t) - DP_H[k]) - z_h);
    }
    y1 = low0(y);
    p_l = (y - y1) * t1 + y * t2;
    p_h = y1 * t1;
    z = p_l + p_h;
    j = hi(z);
    i = lo(z);
    if (j >= 0x40900000) {
      if (((j - 0x40900000) | i) != 0) return s * huge * huge;
      if (p_l + OVT > z - p_h) return s * huge * huge;
    } else if ((j & 0x7fffffff) >= 0x4090cc00) {
      if (((j - 0xc090cc00) | i) != 0) return s * tiny * tiny;
      if (p_l <= z - p_h) return s * tiny * tiny;
    }
    i = j & 0x7fffffff;
    k = (i >> 20) - 0x3ff;
    n = 0;
    if (i > 0x3fe00000) {
      n = j + (0x00100000 >> (k + 1));
      k = ((n & 0x7fffffff) >> 20) - 0x3ff;
      t = make(n & ~(0x000fffff >> k), 0);
      n = ((n & 0x000fffff) | 0x00100000) >> (20 - k);
      if (j < 0) n = -n;
      p_h -= t;
    }
    t = low0(p_l + p_h);
    u = t * LG2_H;
    v = (p_l - (t - p_h)) * LG2 + t * LG2_L;
    z = u + v;
    w = v - (z - u);
    t = z * z;
    t1 = z - t * (P1 + t * (P2 + t * (P3 + t * (P4 + t * P5))));
    r = (z * t1) / (t1 - 2.0) - (w + z * w);
    z = 1.0 - (r - z);
    j = hi(z);
    j += n << 20;
    if ((j >> 20) <= 0) z = Math.scalb(z, n);
    else z = withHi(z, j);
    return s * z;
  }

  public static double cbrt(double x) {
    final int b1 = 715094163, b2 = 696219795;
    final double c = 5.42857142857142815906e-01, d = -7.05306122448979611050e-01, e = 1.41428571428571436819e+00,
        f = 1.60714285714285720630e+00, g = 3.57142857142857150787e-01;
    int hx = hi(x);
    int sign = hx & 0x80000000;
    hx ^= sign;
    if (hx >= 0x7ff00000) return x + x;
    if ((hx | lo(x)) == 0) return x;
    x = withHi(x, hx);
    double t;
    if (hx < 0x00100000) {
      t = 0x1p54 * x;
      t = make(hi(t) / 3 + b2, 0);
    } else {
      t = make(hx / 3 + b1, 0);
    }
    double r = t * t / x;
    double s = c + r * t;
    t *= g + f / (s + e + d / s);
    t = make(hi(t) + 1, 0);
    s = t * t;
    r = x / s;
    double w = t + t;
    r = (r - t) / (w + r);
    t = t + t * r;
    return withHi(t, sign | hi(t));
  }

  private static final double[] TAN_T = {3.33333333333334091986e-01, 1.33333333333201242699e-01,
      5.39682539762260521377e-02, 2.18694882948595424599e-02, 8.86323982359930005737e-03,
      3.59207910759131235356e-03, 1.45620945432529025516e-03, 5.88041240820264096874e-04,
      2.46463134818469906812e-04, 7.81794442939557092300e-05, 7.14072491382608190305e-05,
      -1.85586374855275456654e-05, 2.59073051863633712884e-05};

  private static double kTan(double x, double y, int iy) {
    final double pio4 = 7.85398163397448278999e-01, pio4lo = 3.06161699786838301793e-17;
    double z, r, v, w, s, t, a;
    int hx = hi(x);
    int ix = hx & 0x7fffffff;
    if (ix < 0x3e300000) {
      if ((int) x == 0) {
        if (((ix | lo(x)) | (iy + 1)) == 0) return 1.0 / Math.abs(x);
        if (iy == 1) return x;
        w = x + y;
        z = low0(w);
        v = y - (z - x);
        a = -1.0 / w;
        t = low0(a);
        s = 1.0 + t * z;
        return t + a * (s + t * v);
      }
    }
    if (ix >= 0x3FE59428) {
      if (hx < 0) {
        x = -x;
        y = -y;
      }
      z = pio4 - x;
      w = pio4lo - y;
      x = z + w;
      y = 0.0;
    }
    z = x * x;
    w = z * z;
    r = TAN_T[1] + w * (TAN_T[3] + w * (TAN_T[5] + w * (TAN_T[7] + w * (TAN_T[9] + w * TAN_T[11]))));
    v = z * (TAN_T[2] + w * (TAN_T[4] + w * (TAN_T[6] + w * (TAN_T[8] + w * (TAN_T[10] + w * TAN_T[12])))));
    s = z * x;
    r = y + z * (s * (r + v) + y);
    r += TAN_T[0] * s;
    w = x + r;
    if (ix >= 0x3FE59428) {
      v = (double) iy;
      return (double) (1 - ((hx >> 30) & 2)) * (v - 2.0 * (x - (w * w / (w + v) - r)));
    }
    if (iy == 1) return w;
    z = low0(w);
    v = r - (z - x);
    a = -1.0 / w;
    t = low0(a);
    s = 1.0 + t * z;
    return t + a * (s + t * v);
  }

  public static double tan(double x) {
    int ix = hi(x) & 0x7fffffff;
    if (ix <= 0x3fe921fb) return kTan(x, 0.0, 1);
    if (ix >= 0x7ff00000) return x - x;
    double[] y = new double[2];
    int n = ix >= 0x413921fb ? remPio2Large(x, y) : remPio2(x, y);
    return kTan(y[0], y[1], 1 - ((n & 1) << 1));
  }

  private static final double LN2_HI = 6.93147180369123816490e-01, LN2_LO = 1.90821492927058770002e-10,
      TWO54 = 1.80143985094819840000e+16, LOG1 = 6.666666666666735130e-01, LOG2 = 3.999999999940941908e-01,
      LOG3 = 2.857142874366239149e-01, LOG4 = 2.222219843214978396e-01, LOG5 = 1.818357216161805012e-01,
      LOG6 = 1.531383769920937332e-01, LOG7 = 1.479819860511658591e-01;

  public static double log(double x) {
    double hfsq, f, s, z, r, w, t1, t2, dk;
    int k, hx, i, j;
    hx = hi(x);
    int lx = lo(x);
    k = 0;
    if (hx < 0x00100000) {
      if (((hx & 0x7fffffff) | lx) == 0) return Double.NEGATIVE_INFINITY;
      if (hx < 0) return Double.NaN;
      k -= 54;
      x *= TWO54;
      hx = hi(x);
    }
    if (hx >= 0x7ff00000) return x + x;
    k += (hx >> 20) - 1023;
    hx &= 0x000fffff;
    i = (hx + 0x95f64) & 0x100000;
    x = withHi(x, hx | (i ^ 0x3ff00000));
    k += i >> 20;
    f = x - 1.0;
    if ((0x000fffff & (2 + hx)) < 3) {
      if (f == 0.0) {
        if (k == 0) return 0.0;
        dk = (double) k;
        return dk * LN2_HI + dk * LN2_LO;
      }
      r = f * f * (0.5 - 0.33333333333333333 * f);
      if (k == 0) return f - r;
      dk = (double) k;
      return dk * LN2_HI - ((r - dk * LN2_LO) - f);
    }
    s = f / (2.0 + f);
    dk = (double) k;
    z = s * s;
    i = hx - 0x6147a;
    w = z * z;
    j = 0x6b851 - hx;
    t1 = w * (LOG2 + w * (LOG4 + w * LOG6));
    t2 = z * (LOG1 + w * (LOG3 + w * (LOG5 + w * LOG7)));
    i |= j;
    r = t2 + t1;
    if (i > 0) {
      hfsq = 0.5 * f * f;
      if (k == 0) return f - (hfsq - s * (hfsq + r));
      return dk * LN2_HI - ((hfsq - (s * (hfsq + r) + dk * LN2_LO)) - f);
    }
    if (k == 0) return f - s * (f - r);
    return dk * LN2_HI - ((s * (f - r) - dk * LN2_LO) - f);
  }

  public static double log10(double x) {
    final double ivln10 = 4.34294481903251816668e-01, log10_2hi = 3.01029995663611771306e-01,
        log10_2lo = 3.69423907715893078616e-13;
    double y, z;
    int i, k, hx;
    hx = hi(x);
    int lx = lo(x);
    k = 0;
    if (hx < 0x00100000) {
      if (((hx & 0x7fffffff) | lx) == 0) return Double.NEGATIVE_INFINITY;
      if (hx < 0) return Double.NaN;
      k -= 54;
      x *= TWO54;
      hx = hi(x);
    }
    if (hx >= 0x7ff00000) return x + x;
    k += (hx >> 20) - 1023;
    i = (k & 0x80000000) >>> 31;
    hx = (hx & 0x000fffff) | ((0x3ff - i) << 20);
    y = (double) (k + i);
    x = withHi(x, hx);
    z = y * log10_2lo + ivln10 * log(x);
    return z + y * log10_2hi;
  }

  private static final double PIO2_HI = 1.57079632679489655800e+00, PIO2_LO = 6.12323399573676603587e-17,
      PIO4_HI = 7.85398163397448278999e-01, PS0 = 1.66666666666666657415e-01, PS1 = -3.25565818622400915405e-01,
      PS2 = 2.01212532134862925881e-01, PS3 = -4.00555345006794114027e-02, PS4 = 7.91534994289814532176e-04,
      PS5 = 3.47933107596021167570e-05, QS1 = -2.40339491173441421878e+00, QS2 = 2.02094576023350569471e+00,
      QS3 = -6.88283971605453293030e-01, QS4 = 7.70381505559019352791e-02;

  private static double asinP(double t) {
    return t * (PS0 + t * (PS1 + t * (PS2 + t * (PS3 + t * (PS4 + t * PS5)))));
  }

  private static double asinQ(double t) {
    return 1.0 + t * (QS1 + t * (QS2 + t * (QS3 + t * QS4)));
  }

  public static double asin(double x) {
    double t = 0.0, w, p, q, c, r, s;
    int hx = hi(x);
    int ix = hx & 0x7fffffff;
    if (ix >= 0x3ff00000) {
      if (((ix - 0x3ff00000) | lo(x)) == 0) return x * PIO2_HI + x * PIO2_LO;
      return (x - x) / (x - x);
    } else if (ix < 0x3fe00000) {
      if (ix < 0x3e400000) {
        if (1.0e300 + x > 1.0) return x;
      } else {
        t = x * x;
      }
      p = asinP(t);
      q = asinQ(t);
      w = p / q;
      return x + x * w;
    }
    w = 1.0 - Math.abs(x);
    t = w * 0.5;
    p = asinP(t);
    q = asinQ(t);
    s = Math.sqrt(t);
    if (ix >= 0x3FEF3333) {
      w = p / q;
      t = PIO2_HI - (2.0 * (s + s * w) - PIO2_LO);
    } else {
      w = low0(s);
      c = (t - w * w) / (s + w);
      r = p / q;
      p = 2.0 * s * r - (PIO2_LO - 2.0 * c);
      q = PIO4_HI - 2.0 * w;
      t = PIO4_HI - (p - q);
    }
    return hx > 0 ? t : -t;
  }

  public static double acos(double x) {
    final double pi = 3.14159265358979311600e+00;
    double z, p, q, r, w, s, c, df;
    int hx = hi(x);
    int ix = hx & 0x7fffffff;
    if (ix >= 0x3ff00000) {
      if (((ix - 0x3ff00000) | lo(x)) == 0) {
        if (hx > 0) return 0.0;
        return pi + 2.0 * PIO2_LO;
      }
      return (x - x) / (x - x);
    }
    if (ix < 0x3fe00000) {
      if (ix <= 0x3c600000) return PIO2_HI + PIO2_LO;
      z = x * x;
      p = asinP(z);
      q = asinQ(z);
      r = p / q;
      return PIO2_HI - (x - (PIO2_LO - x * r));
    } else if (hx < 0) {
      z = (1.0 + x) * 0.5;
      p = asinP(z);
      q = asinQ(z);
      s = Math.sqrt(z);
      r = p / q;
      w = r * s - PIO2_LO;
      return pi - 2.0 * (s + w);
    }
    z = (1.0 - x) * 0.5;
    s = Math.sqrt(z);
    df = low0(s);
    c = (z - df * df) / (s + df);
    p = asinP(z);
    q = asinQ(z);
    r = p / q;
    w = r * s + c;
    return 2.0 * (df + w);
  }

  public static double expm1(double x) {
    final double huge = 1.0e300, tiny = 1.0e-300, oThreshold = 7.09782712893383973096e+02,
        q1 = -3.33333333333331316428e-02, q2 = 1.58730158725481460165e-03, q3 = -7.93650757867487942473e-05,
        q4 = 4.00821782732936239552e-06, q5 = -2.01099218183624371326e-07;
    double y, hfx, hxs, r1, t, e, hi, lo, c = 0.0;
    int k, xsb;
    int hx = hi(x);
    xsb = hx & 0x80000000;
    hx &= 0x7fffffff;
    if (hx >= 0x4043687A) {
      if (hx >= 0x40862E42) {
        if (hx >= 0x7ff00000) {
          if (((hx & 0xfffff) | lo(x)) != 0) return x + x;
          return xsb == 0 ? x : -1.0;
        }
        if (x > oThreshold) return Double.POSITIVE_INFINITY;
      }
      if (xsb != 0) {
        if (x + tiny < 0.0) return tiny - 1.0;
      }
    }
    if (hx > 0x3fd62e42) {
      if (hx < 0x3FF0A2B2) {
        if (xsb == 0) {
          hi = x - LN2_HI;
          lo = LN2_LO;
          k = 1;
        } else {
          hi = x + LN2_HI;
          lo = -LN2_LO;
          k = -1;
        }
      } else {
        k = (int) (1.44269504088896338700e+00 * x + (xsb == 0 ? 0.5 : -0.5));
        t = k;
        hi = x - t * LN2_HI;
        lo = t * LN2_LO;
      }
      x = hi - lo;
      c = (hi - x) - lo;
    } else if (hx < 0x3c900000) {
      t = huge + x;
      return x - (t - (huge + x));
    } else {
      k = 0;
    }
    hfx = 0.5 * x;
    hxs = x * hfx;
    r1 = 1.0 + hxs * (q1 + hxs * (q2 + hxs * (q3 + hxs * (q4 + hxs * q5))));
    t = 3.0 - r1 * hfx;
    e = hxs * ((r1 - t) / (6.0 - x * t));
    if (k == 0) return x - (x * e - hxs);
    double twopk = make(0x3ff00000 + (k << 20), 0);
    e = (x * (e - c) - c);
    e -= hxs;
    if (k == -1) return 0.5 * (x - e) - 0.5;
    if (k == 1) {
      if (x < -0.25) return -2.0 * (e - (x + 0.5));
      return 1.0 + 2.0 * (x - e);
    }
    if (k <= -2 || k > 56) {
      y = 1.0 - (e - x);
      if (k == 1024) y = y * 2.0 * 0x1p1023;
      else y = y * twopk;
      return y - 1.0;
    }
    if (k < 20) {
      t = make(0x3ff00000 - (0x200000 >> k), 0);
      y = t - (e - x);
      y = y * twopk;
    } else {
      t = make((0x3ff - k) << 20, 0);
      y = x - (e + t);
      y += 1.0;
      y = y * twopk;
    }
    return y;
  }

  public static double sinh(double x) {
    final double shuge = 1.0e307;
    double t, w, h;
    int ix, jx, lx;
    jx = hi(x);
    ix = jx & 0x7fffffff;
    if (ix >= 0x7ff00000) return x + x;
    h = 0.5;
    if (jx < 0) h = -h;
    if (ix < 0x40360000) {
      if (ix < 0x3e300000) {
        if (shuge + x > 1.0) return x;
      }
      t = expm1(Math.abs(x));
      if (ix < 0x3ff00000) return h * (2.0 * t - t * t / (t + 1.0));
      return h * (t + t / (t + 1.0));
    }
    if (ix < 0x40862E42) return h * exp(Math.abs(x));
    lx = lo(x);
    if (ix < 0x408633CE || (ix == 0x408633ce && Integer.compareUnsigned(lx, 0x8fb9f87d) <= 0)) {
      w = exp(0.5 * Math.abs(x));
      t = h * w;
      return t * w;
    }
    return x * shuge;
  }

  public static double tanh(double x) {
    final double tiny = 1.0e-300;
    double t, z;
    int jx = hi(x);
    int ix = jx & 0x7fffffff;
    if (ix >= 0x7ff00000) {
      if (jx >= 0) return 1.0 / x + 1.0;
      return 1.0 / x - 1.0;
    }
    if (ix < 0x40360000) {
      if (ix < 0x3c800000) return x * (1.0 + x);
      if (ix >= 0x3ff00000) {
        t = expm1(2.0 * Math.abs(x));
        z = 1.0 - 2.0 / (t + 2.0);
      } else {
        t = expm1(-2.0 * Math.abs(x));
        z = -t / (t + 2.0);
      }
    } else {
      z = 1.0 - tiny;
    }
    return jx >= 0 ? z : -z;
  }

  public static double log1p(double x) {
    final double zero = 0.0, tiny = 1.0e-300, lp1 = 6.666666666666735130e-01, lp2 = 3.999999999940941908e-01,
        lp3 = 2.857142874366239149e-01, lp4 = 2.222219843214978396e-01, lp5 = 1.818357216161805012e-01,
        lp6 = 1.531383769920937332e-01, lp7 = 1.479819860511658591e-01;
    double hfsq, f = 0.0, c = 0.0, s, z, r, u;
    int k, hx, hu = 0, ax;
    hx = hi(x);
    ax = hx & 0x7fffffff;
    k = 1;
    if (hx < 0x3FDA827A) {
      if (ax >= 0x3ff00000) {
        if (x == -1.0) return Double.NEGATIVE_INFINITY;
        return (x - x) / (x - x);
      }
      if (ax < 0x3e200000) {
        if (TWO54 + x > zero && ax < 0x3c900000) return x;
        return x - x * x * 0.5;
      }
      if (hx > 0 || hx <= 0xbfd2bec3) {
        k = 0;
        f = x;
        hu = 1;
      }
    }
    if (hx >= 0x7ff00000) return x + x;
    if (k != 0) {
      if (hx < 0x43400000) {
        u = 1.0 + x;
        hu = hi(u);
        k = (hu >> 20) - 1023;
        c = k > 0 ? 1.0 - (u - x) : x - (u - 1.0);
        c /= u;
      } else {
        u = x;
        hu = hi(u);
        k = (hu >> 20) - 1023;
        c = 0;
      }
      hu &= 0x000fffff;
      if (hu < 0x6a09e) {
        u = withHi(u, hu | 0x3ff00000);
      } else {
        k += 1;
        u = withHi(u, hu | 0x3fe00000);
        hu = (0x00100000 - hu) >> 2;
      }
      f = u - 1.0;
    }
    hfsq = 0.5 * f * f;
    if (hu == 0) {
      if (f == zero) {
        if (k == 0) return zero;
        c += k * LN2_LO;
        return k * LN2_HI + c;
      }
      r = hfsq * (1.0 - 0.66666666666666666 * f);
      if (k == 0) return f - r;
      return k * LN2_HI - ((r - (k * LN2_LO + c)) - f);
    }
    s = f / (2.0 + f);
    z = s * s;
    r = z * (lp1 + z * (lp2 + z * (lp3 + z * (lp4 + z * (lp5 + z * (lp6 + z * lp7))))));
    if (k == 0) return f - (hfsq - s * (hfsq + r));
    return k * LN2_HI - ((hfsq - (s * (hfsq + r) + (k * LN2_LO + c))) - f);
  }
}
