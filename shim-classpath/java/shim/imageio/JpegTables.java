/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

final class JpegTables {
  private JpegTables() { }

  static final int[] kZigzag = {0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18, 11, 4, 5, 12, 19, 26, 33, 40, 48, 41,
      34, 27, 20, 13, 6, 7, 14, 21, 28, 35, 42, 49, 56, 57, 50, 43, 36, 29, 22, 15, 23, 30, 37, 44, 51, 58, 59, 52,
      45, 38, 31, 39, 46, 53, 60, 61, 54, 47, 55, 62, 63};

  static final int[] kLumaQuant = {16, 11, 10, 16, 24, 40, 51, 61, 12, 12, 14, 19, 26, 58, 60, 55, 14, 13, 16, 24, 40,
      57, 69, 56, 14, 17, 22, 29, 51, 87, 80, 62, 18, 22, 37, 56, 68, 109, 103, 77, 24, 35, 55, 64, 81, 104, 113, 92,
      49, 64, 78, 87, 103, 121, 120, 101, 72, 92, 95, 98, 112, 100, 103, 99};

  static final int[] kChromaQuant = {17, 18, 24, 47, 99, 99, 99, 99, 18, 21, 26, 66, 99, 99, 99, 99, 24, 26, 56, 99,
      99, 99, 99, 99, 47, 66, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,
      99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99};

  static final int[] kDcLumaBits = {0, 1, 5, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0};
  static final int[] kDcLumaVals = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
  static final int[] kDcChromaBits = {0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0};
  static final int[] kDcChromaVals = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
  static final int[] kAcLumaBits = {0, 2, 1, 3, 3, 2, 4, 3, 5, 5, 4, 4, 0, 0, 1, 0x7d};
  static final int[] kAcLumaVals = {0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12, 0x21, 0x31, 0x41, 0x06, 0x13,
      0x51, 0x61, 0x07, 0x22, 0x71, 0x14, 0x32, 0x81, 0x91, 0xa1, 0x08, 0x23, 0x42, 0xb1, 0xc1, 0x15, 0x52, 0xd1,
      0xf0, 0x24, 0x33, 0x62, 0x72, 0x82, 0x09, 0x0a, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x25, 0x26, 0x27, 0x28, 0x29,
      0x2a, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x53, 0x54,
      0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75, 0x76,
      0x77, 0x78, 0x79, 0x7a, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97,
      0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7,
      0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5, 0xd6, 0xd7,
      0xd8, 0xd9, 0xda, 0xe1, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xf1, 0xf2, 0xf3, 0xf4, 0xf5,
      0xf6, 0xf7, 0xf8, 0xf9, 0xfa};
  static final int[] kAcChromaBits = {0, 2, 1, 2, 4, 4, 3, 4, 7, 5, 4, 4, 0, 1, 2, 0x77};
  static final int[] kAcChromaVals = {0x00, 0x01, 0x02, 0x03, 0x11, 0x04, 0x05, 0x21, 0x31, 0x06, 0x12, 0x41, 0x51,
      0x07, 0x61, 0x71, 0x13, 0x22, 0x32, 0x81, 0x08, 0x14, 0x42, 0x91, 0xa1, 0xb1, 0xc1, 0x09, 0x23, 0x33, 0x52,
      0xf0, 0x15, 0x62, 0x72, 0xd1, 0x0a, 0x16, 0x24, 0x34, 0xe1, 0x25, 0xf1, 0x17, 0x18, 0x19, 0x1a, 0x26, 0x27,
      0x28, 0x29, 0x2a, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x53,
      0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75,
      0x76, 0x77, 0x78, 0x79, 0x7a, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93, 0x94, 0x95,
      0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5,
      0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5,
      0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xf2, 0xf3, 0xf4, 0xf5,
      0xf6, 0xf7, 0xf8, 0xf9, 0xfa};

  static final double[][] kCos = new double[8][8];

  static {
    for (int x = 0; x < 8; x++) {
      for (int u = 0; u < 8; u++) {
        kCos[x][u] = (u == 0 ? Math.sqrt(0.125) : 0.5) * Math.cos((2 * x + 1) * u * Math.PI / 16);
      }
    }
  }

  static void idct(int[] coef, int[] out) {
    int[] ws = new int[64];
    for (int c = 0; c < 8; c++) {
      idct1(coef, c, 8, ws, c, 8, 11, false, null);
    }
    for (int r = 0; r < 8; r++) {
      idct1(ws, r * 8, 1, null, r * 8, 1, 18, true, out);
    }
  }

  private static void idct1(int[] in, int io, int is, int[] dst, int dop, int ds, int shift, boolean last, int[] out) {
    int z2 = in[io + 2 * is];
    int z3 = in[io + 6 * is];
    int z1 = (z2 + z3) * 4433;
    int tmp2 = z1 + z3 * -15137;
    int tmp3 = z1 + z2 * 6270;
    z2 = in[io];
    z3 = in[io + 4 * is];
    int tmp0 = (z2 + z3) << 13;
    int tmp1 = (z2 - z3) << 13;
    int t10 = tmp0 + tmp3;
    int t13 = tmp0 - tmp3;
    int t11 = tmp1 + tmp2;
    int t12 = tmp1 - tmp2;
    tmp0 = in[io + 7 * is];
    tmp1 = in[io + 5 * is];
    tmp2 = in[io + 3 * is];
    tmp3 = in[io + is];
    z1 = tmp0 + tmp3;
    z2 = tmp1 + tmp2;
    z3 = tmp0 + tmp2;
    int z4 = tmp1 + tmp3;
    int z5 = (z3 + z4) * 9633;
    tmp0 *= 2446;
    tmp1 *= 16819;
    tmp2 *= 25172;
    tmp3 *= 12299;
    z1 *= -7373;
    z2 *= -20995;
    z3 *= -16069;
    z4 *= -3196;
    z3 += z5;
    z4 += z5;
    tmp0 += z1 + z3;
    tmp1 += z2 + z4;
    tmp2 += z2 + z3;
    tmp3 += z1 + z4;
    int[] r = {t10 + tmp3, t11 + tmp2, t12 + tmp1, t13 + tmp0, t13 - tmp0, t12 - tmp1, t11 - tmp2, t10 - tmp3};
    for (int k = 0; k < 8; k++) {
      int v = (r[k] + (1 << (shift - 1))) >> shift;
      if (last) {
        v += 128;
        out[dop + k] = v < 0 ? 0 : v > 255 ? 255 : v;
      } else {
        dst[dop + k * ds] = v;
      }
    }
  }

  static void fdctInt(int[] in, int[] out) {
    int[] ws = new int[64];
    for (int r = 0; r < 8; r++) {
      int o = r * 8;
      int t0 = in[o] + in[o + 7];
      int t7 = in[o] - in[o + 7];
      int t1 = in[o + 1] + in[o + 6];
      int t6 = in[o + 1] - in[o + 6];
      int t2 = in[o + 2] + in[o + 5];
      int t5 = in[o + 2] - in[o + 5];
      int t3 = in[o + 3] + in[o + 4];
      int t4 = in[o + 3] - in[o + 4];
      butterfly(ws, o, 1, t0, t1, t2, t3, t4, t5, t6, t7, true);
    }
    for (int c = 0; c < 8; c++) {
      int t0 = ws[c] + ws[c + 56];
      int t7 = ws[c] - ws[c + 56];
      int t1 = ws[c + 8] + ws[c + 48];
      int t6 = ws[c + 8] - ws[c + 48];
      int t2 = ws[c + 16] + ws[c + 40];
      int t5 = ws[c + 16] - ws[c + 40];
      int t3 = ws[c + 24] + ws[c + 32];
      int t4 = ws[c + 24] - ws[c + 32];
      butterfly(out, c, 8, t0, t1, t2, t3, t4, t5, t6, t7, false);
    }
  }

  private static int descale(int x, int n) { return (x + (1 << (n - 1))) >> n; }

  private static void butterfly(int[] dst, int o, int st, int t0, int t1, int t2, int t3, int t4, int t5, int t6,
      int t7, boolean rows) {
    int t10 = t0 + t3;
    int t13 = t0 - t3;
    int t11 = t1 + t2;
    int t12 = t1 - t2;
    int even = rows ? 11 : 15;
    int pass = 2;
    dst[o] = rows ? (t10 + t11) << pass : descale(t10 + t11, pass);
    dst[o + 4 * st] = rows ? (t10 - t11) << pass : descale(t10 - t11, pass);
    int z1 = (t12 + t13) * 4433;
    dst[o + 2 * st] = descale(z1 + t13 * 6270, even);
    dst[o + 6 * st] = descale(z1 + t12 * -15137, even);
    z1 = t4 + t7;
    int z2 = t5 + t6;
    int z3 = t4 + t6;
    int z4 = t5 + t7;
    int z5 = (z3 + z4) * 9633;
    t4 *= 2446;
    t5 *= 16819;
    t6 *= 25172;
    t7 *= 12299;
    z1 *= -7373;
    z2 *= -20995;
    z3 *= -16069;
    z4 *= -3196;
    z3 += z5;
    z4 += z5;
    dst[o + 7 * st] = descale(t4 + z1 + z3, even);
    dst[o + 5 * st] = descale(t5 + z2 + z4, even);
    dst[o + 3 * st] = descale(t6 + z2 + z3, even);
    dst[o + st] = descale(t7 + z1 + z4, even);
  }

  static void fdct(double[] block, double[] out) {
    double[] tmp = new double[64];
    for (int y = 0; y < 8; y++) {
      for (int u = 0; u < 8; u++) {
        double s = 0;
        for (int x = 0; x < 8; x++) {
          s += kCos[x][u] * block[y * 8 + x];
        }
        tmp[y * 8 + u] = s;
      }
    }
    for (int u = 0; u < 8; u++) {
      for (int v = 0; v < 8; v++) {
        double s = 0;
        for (int y = 0; y < 8; y++) {
          s += kCos[y][v] * tmp[y * 8 + u];
        }
        out[v * 8 + u] = s;
      }
    }
  }
}
