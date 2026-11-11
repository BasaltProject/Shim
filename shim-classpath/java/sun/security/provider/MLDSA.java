/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.security.MessageDigest;
import java.util.Arrays;

/** ML-DSA (FIPS 204) core operations. */
public final class MLDSA {
  static final int Q = 8380417;
  static final int D = 13;
  private static final int N = 256;
  private static final int[] ZETAS = new int[256];
  private static final int INV_N = 8347681;

  static {
    for (int i = 0; i < 256; i++) {
      long r = 1;
      int e = Integer.reverse(i) >>> 24;
      for (int j = 0; j < e; j++) {
        r = r * 1753 % Q;
      }
      ZETAS[i] = (int) r;
    }
  }

  public enum Params {
    ML_DSA_44("ML-DSA-44", 4, 4, 2, 39, 78, 1 << 17, (Q - 1) / 88, 80, 32, 17),
    ML_DSA_65("ML-DSA-65", 6, 5, 4, 49, 196, 1 << 19, (Q - 1) / 32, 55, 48, 18),
    ML_DSA_87("ML-DSA-87", 8, 7, 2, 60, 120, 1 << 19, (Q - 1) / 32, 75, 64, 19);

    public final String name;
    final int k;
    final int l;
    final int eta;
    final int tau;
    final int beta;
    final int gamma1;
    final int gamma2;
    final int omega;
    final int ctilde;
    public final int oidLast;

    Params(String name, int k, int l, int eta, int tau, int beta, int gamma1, int gamma2, int omega, int ctilde,
        int oidLast) {
      this.name = name;
      this.k = k;
      this.l = l;
      this.eta = eta;
      this.tau = tau;
      this.beta = beta;
      this.gamma1 = gamma1;
      this.gamma2 = gamma2;
      this.omega = omega;
      this.ctilde = ctilde;
      this.oidLast = oidLast;
    }

    int etaBits() {
      return eta == 2 ? 3 : 4;
    }

    int zBits() {
      return gamma1 == (1 << 17) ? 18 : 20;
    }

    int w1Bits() {
      return gamma2 == (Q - 1) / 88 ? 6 : 4;
    }

    public int publicKeySize() {
      return 32 + 320 * k;
    }

    public int privateKeySize() {
      return 128 + 32 * (l + k) * etaBits() + 416 * k;
    }

    public int signatureSize() {
      return ctilde + 32 * l * zBits() + omega + k;
    }

    public static Params byName(String n) {
      for (Params p : values()) {
        if (p.name.equalsIgnoreCase(n)) {
          return p;
        }
      }
      return null;
    }

    public static Params byOid(int last) {
      for (Params p : values()) {
        if (p.oidLast == last) {
          return p;
        }
      }
      return null;
    }
  }

  private MLDSA() {
  }

  private static int mod(long x) {
    int r = (int) (x % Q);
    return r < 0 ? r + Q : r;
  }

  private static int centered(int x) {
    x = mod(x);
    return x > (Q - 1) / 2 ? x - Q : x;
  }

  private static int modPm(int r, int m) {
    int x = Math.floorMod(r, m);
    return x > m / 2 ? x - m : x;
  }

  private static byte[] h(int len, byte[]... in) {
    Keccak x = Keccak.shake256();
    for (byte[] b : in) {
      x.absorb(b);
    }
    return x.squeeze(len);
  }

  private static void ntt(int[] w) {
    int m = 0;
    for (int len = 128; len >= 1; len >>= 1) {
      for (int start = 0; start < N; start += 2 * len) {
        long z = ZETAS[++m];
        for (int j = start; j < start + len; j++) {
          int t = (int) (z * w[j + len] % Q);
          w[j + len] = w[j] - t < 0 ? w[j] - t + Q : w[j] - t;
          w[j] = w[j] + t >= Q ? w[j] + t - Q : w[j] + t;
        }
      }
    }
  }

  private static void invNtt(int[] w) {
    int m = 256;
    for (int len = 1; len < N; len <<= 1) {
      for (int start = 0; start < N; start += 2 * len) {
        long z = Q - ZETAS[--m];
        for (int j = start; j < start + len; j++) {
          int t = w[j];
          w[j] = t + w[j + len] >= Q ? t + w[j + len] - Q : t + w[j + len];
          int diff = t - w[j + len];
          if (diff < 0) {
            diff += Q;
          }
          w[j + len] = (int) (z * diff % Q);
        }
      }
    }
    for (int j = 0; j < N; j++) {
      w[j] = (int) ((long) INV_N * w[j] % Q);
    }
  }

  private static int[] mul(int[] a, int[] b) {
    int[] r = new int[N];
    for (int i = 0; i < N; i++) {
      r[i] = (int) ((long) a[i] * b[i] % Q);
    }
    return r;
  }

  private static void addTo(int[] a, int[] b) {
    for (int i = 0; i < N; i++) {
      a[i] = (a[i] + b[i]) % Q;
    }
  }

  private static int[] copyNtt(int[] a) {
    int[] r = a.clone();
    ntt(r);
    return r;
  }

  private static int[][][] expandA(Params p, byte[] rho) {
    int[][][] a = new int[p.k][p.l][];
    for (int r = 0; r < p.k; r++) {
      for (int s = 0; s < p.l; s++) {
        Keccak x = Keccak.shake128().absorb(rho).absorb(new byte[] {(byte) s, (byte) r});
        int[] c = new int[N];
        int n = 0;
        byte[] b = new byte[3];
        while (n < N) {
          x.squeeze(b, 0, 3);
          int z = (b[0] & 0xff) | ((b[1] & 0xff) << 8) | ((b[2] & 0x7f) << 16);
          if (z < Q) {
            c[n++] = z;
          }
        }
        a[r][s] = c;
      }
    }
    return a;
  }

  private static int[] boundedPoly(Params p, byte[] rho, int idx) {
    Keccak x = Keccak.shake256().absorb(rho).absorb(new byte[] {(byte) idx, (byte) (idx >> 8)});
    int[] c = new int[N];
    int n = 0;
    byte[] b = new byte[1];
    while (n < N) {
      x.squeeze(b, 0, 1);
      int z = b[0] & 0xff;
      for (int half = 0; half < 2 && n < N; half++) {
        int v = half == 0 ? z & 15 : z >> 4;
        if (p.eta == 2 && v < 15) {
          c[n++] = mod(2 - v % 5);
        } else if (p.eta == 4 && v < 9) {
          c[n++] = mod(4 - v);
        }
      }
    }
    return c;
  }

  private static byte[] pack(int[] vals, int bits, int offset) {
    byte[] out = new byte[32 * bits];
    int bit = 0;
    for (int i = 0; i < N; i++) {
      int v = offset - vals[i];
      if (offset == 0) {
        v = vals[i];
      }
      for (int j = 0; j < bits; j++) {
        out[bit >> 3] |= (byte) (((v >> j) & 1) << (bit & 7));
        bit++;
      }
    }
    return out;
  }

  private static int[] unpack(byte[] in, int off, int bits, int offset) {
    int[] r = new int[N];
    int bit = 0;
    for (int i = 0; i < N; i++) {
      int v = 0;
      for (int j = 0; j < bits; j++) {
        v |= ((in[off + (bit >> 3)] >> (bit & 7)) & 1) << j;
        bit++;
      }
      r[i] = offset == 0 ? v : offset - v;
    }
    return r;
  }

  private static int[] power2Round(int[] t, int[] t0) {
    int[] t1 = new int[N];
    for (int i = 0; i < N; i++) {
      int r = mod(t[i]);
      int r0 = modPm(r, 1 << D);
      t0[i] = r0;
      t1[i] = (r - r0) >> D;
    }
    return t1;
  }

  public static byte[][] keyGen(Params p, byte[] xi) {
    byte[] seed = h(128, xi, new byte[] {(byte) p.k, (byte) p.l});
    byte[] rho = Arrays.copyOfRange(seed, 0, 32);
    byte[] rhoP = Arrays.copyOfRange(seed, 32, 96);
    byte[] key = Arrays.copyOfRange(seed, 96, 128);
    int[][][] a = expandA(p, rho);
    int[][] s1 = new int[p.l][];
    int[][] s2 = new int[p.k][];
    for (int r = 0; r < p.l; r++) {
      s1[r] = boundedPoly(p, rhoP, r);
    }
    for (int r = 0; r < p.k; r++) {
      s2[r] = boundedPoly(p, rhoP, r + p.l);
    }
    int[][] s1h = new int[p.l][];
    for (int i = 0; i < p.l; i++) {
      s1h[i] = copyNtt(s1[i]);
    }
    byte[] pk = new byte[p.publicKeySize()];
    System.arraycopy(rho, 0, pk, 0, 32);
    int[][] t0s = new int[p.k][N];
    for (int r = 0; r < p.k; r++) {
      int[] t = new int[N];
      for (int s = 0; s < p.l; s++) {
        addTo(t, mul(a[r][s], s1h[s]));
      }
      invNtt(t);
      addTo(t, s2[r]);
      int[] t1 = power2Round(t, t0s[r]);
      System.arraycopy(pack(t1, 10, 0), 0, pk, 32 + 320 * r, 320);
    }
    byte[] tr = h(64, pk);
    byte[] sk = new byte[p.privateKeySize()];
    System.arraycopy(rho, 0, sk, 0, 32);
    System.arraycopy(key, 0, sk, 32, 32);
    System.arraycopy(tr, 0, sk, 64, 64);
    int pos = 128;
    int eb = p.etaBits();
    for (int i = 0; i < p.l; i++) {
      int[] c = centeredAll(s1[i]);
      System.arraycopy(pack(c, eb, p.eta), 0, sk, pos, 32 * eb);
      pos += 32 * eb;
    }
    for (int i = 0; i < p.k; i++) {
      int[] c = centeredAll(s2[i]);
      System.arraycopy(pack(c, eb, p.eta), 0, sk, pos, 32 * eb);
      pos += 32 * eb;
    }
    for (int i = 0; i < p.k; i++) {
      System.arraycopy(pack(t0s[i], 13, 1 << 12), 0, sk, pos, 416);
      pos += 416;
    }
    return new byte[][] {pk, sk};
  }

  private static int[] centeredAll(int[] a) {
    int[] r = new int[N];
    for (int i = 0; i < N; i++) {
      r[i] = centered(a[i]);
    }
    return r;
  }

  private static int[] sampleInBall(Params p, byte[] seed) {
    Keccak x = Keccak.shake256().absorb(seed);
    byte[] s = x.squeeze(8);
    long signs = 0;
    for (int i = 0; i < 8; i++) {
      signs |= (s[i] & 0xffL) << (8 * i);
    }
    int[] c = new int[N];
    byte[] b = new byte[1];
    for (int i = N - p.tau; i < N; i++) {
      do {
        x.squeeze(b, 0, 1);
      } while ((b[0] & 0xff) > i);
      int j = b[0] & 0xff;
      c[i] = c[j];
      c[j] = (signs & 1) == 0 ? 1 : Q - 1;
      signs >>>= 1;
    }
    return c;
  }

  private static int highBits(Params p, int r) {
    r = mod(r);
    int r0 = modPm(r, 2 * p.gamma2);
    return r - r0 == Q - 1 ? 0 : (r - r0) / (2 * p.gamma2);
  }

  private static int lowBits(Params p, int r) {
    r = mod(r);
    int r0 = modPm(r, 2 * p.gamma2);
    return r - r0 == Q - 1 ? r0 - 1 : r0;
  }

  private static int useHint(Params p, boolean hint, int r) {
    int m = (Q - 1) / (2 * p.gamma2);
    int r1 = highBits(p, r);
    int r0 = lowBits(p, r);
    if (!hint) {
      return r1;
    }
    return r0 > 0 ? (r1 + 1) % m : Math.floorMod(r1 - 1, m);
  }

  private static byte[] w1Encode(Params p, int[][] w1) {
    int bits = p.w1Bits();
    byte[] out = new byte[32 * bits * p.k];
    for (int i = 0; i < p.k; i++) {
      System.arraycopy(pack(w1[i], bits, 0), 0, out, 32 * bits * i, 32 * bits);
    }
    return out;
  }

  public static byte[] sign(Params p, byte[] sk, byte[] mPrime, byte[] rnd) {
    byte[] rho = Arrays.copyOfRange(sk, 0, 32);
    byte[] key = Arrays.copyOfRange(sk, 32, 64);
    byte[] tr = Arrays.copyOfRange(sk, 64, 128);
    int eb = p.etaBits();
    int pos = 128;
    int[][] s1h = new int[p.l][];
    int[][] s2h = new int[p.k][];
    int[][] t0h = new int[p.k][];
    for (int i = 0; i < p.l; i++) {
      int[] v = unpack(sk, pos, eb, p.eta);
      for (int j = 0; j < N; j++) {
        v[j] = mod(v[j]);
      }
      ntt(v);
      s1h[i] = v;
      pos += 32 * eb;
    }
    for (int i = 0; i < p.k; i++) {
      int[] v = unpack(sk, pos, eb, p.eta);
      for (int j = 0; j < N; j++) {
        v[j] = mod(v[j]);
      }
      ntt(v);
      s2h[i] = v;
      pos += 32 * eb;
    }
    for (int i = 0; i < p.k; i++) {
      int[] v = unpack(sk, pos, 13, 1 << 12);
      for (int j = 0; j < N; j++) {
        v[j] = mod(v[j]);
      }
      ntt(v);
      t0h[i] = v;
      pos += 416;
    }
    int[][][] a = expandA(p, rho);
    byte[] mu = h(64, tr, mPrime);
    byte[] rhoPP = h(64, key, rnd, mu);
    int kappa = 0;
    int zb = p.zBits();
    while (true) {
      int[][] y = new int[p.l][];
      for (int r = 0; r < p.l; r++) {
        int idx = kappa + r;
        byte[] v = h(32 * zb, rhoPP, new byte[] {(byte) idx, (byte) (idx >> 8)});
        int[] u = unpack(v, 0, zb, p.gamma1);
        for (int j = 0; j < N; j++) {
          u[j] = mod(u[j]);
        }
        y[r] = u;
      }
      int[][] yh = new int[p.l][];
      for (int i = 0; i < p.l; i++) {
        yh[i] = copyNtt(y[i]);
      }
      int[][] w = new int[p.k][];
      int[][] w1 = new int[p.k][N];
      for (int r = 0; r < p.k; r++) {
        int[] t = new int[N];
        for (int s = 0; s < p.l; s++) {
          addTo(t, mul(a[r][s], yh[s]));
        }
        invNtt(t);
        w[r] = t;
        for (int j = 0; j < N; j++) {
          w1[r][j] = highBits(p, t[j]);
        }
      }
      byte[] ct = h(p.ctilde, mu, w1Encode(p, w1));
      int[] ch = copyNtt(sampleInBall(p, ct));
      int[][] z = new int[p.l][];
      boolean ok = true;
      for (int i = 0; i < p.l && ok; i++) {
        int[] cs1 = mul(ch, s1h[i]);
        invNtt(cs1);
        addTo(cs1, y[i]);
        z[i] = cs1;
        for (int j = 0; j < N; j++) {
          if (Math.abs(centered(cs1[j])) >= p.gamma1 - p.beta) {
            ok = false;
            break;
          }
        }
      }
      int[][] hint = null;
      if (ok) {
        hint = new int[p.k][N];
        int count = 0;
        for (int i = 0; i < p.k && ok; i++) {
          int[] cs2 = mul(ch, s2h[i]);
          invNtt(cs2);
          int[] ct0 = mul(ch, t0h[i]);
          invNtt(ct0);
          for (int j = 0; j < N; j++) {
            int rr = mod(w[i][j] - cs2[j]);
            if (Math.abs(lowBits(p, rr)) >= p.gamma2 - p.beta || Math.abs(centered(ct0[j])) >= p.gamma2) {
              ok = false;
              break;
            }
            int hh = highBits(p, rr) != highBits(p, rr + ct0[j]) ? 1 : 0;
            hint[i][j] = hh;
            count += hh;
          }
        }
        if (count > p.omega) {
          ok = false;
        }
      }
      if (ok) {
        byte[] sig = new byte[p.signatureSize()];
        System.arraycopy(ct, 0, sig, 0, p.ctilde);
        int off = p.ctilde;
        for (int i = 0; i < p.l; i++) {
          System.arraycopy(pack(centeredAll(z[i]), zb, p.gamma1), 0, sig, off, 32 * zb);
          off += 32 * zb;
        }
        int idx = 0;
        for (int i = 0; i < p.k; i++) {
          for (int j = 0; j < N; j++) {
            if (hint[i][j] != 0) {
              sig[off + idx++] = (byte) j;
            }
          }
          sig[off + p.omega + i] = (byte) idx;
        }
        return sig;
      }
      kappa += p.l;
    }
  }

  public static boolean verify(Params p, byte[] pk, byte[] mPrime, byte[] sig) {
    if (sig.length != p.signatureSize() || pk.length != p.publicKeySize()) {
      return false;
    }
    byte[] rho = Arrays.copyOfRange(pk, 0, 32);
    int zb = p.zBits();
    int off = p.ctilde;
    int[][] z = new int[p.l][];
    for (int i = 0; i < p.l; i++) {
      int[] u = unpack(sig, off, zb, p.gamma1);
      for (int j = 0; j < N; j++) {
        if (Math.abs(u[j]) >= p.gamma1 - p.beta) {
          return false;
        }
        u[j] = mod(u[j]);
      }
      z[i] = u;
      off += 32 * zb;
    }
    boolean[][] hint = new boolean[p.k][N];
    int idx = 0;
    for (int i = 0; i < p.k; i++) {
      int end = sig[off + p.omega + i] & 0xff;
      if (end < idx || end > p.omega) {
        return false;
      }
      int first = idx;
      while (idx < end) {
        int pos = sig[off + idx] & 0xff;
        if (idx > first && pos <= (sig[off + idx - 1] & 0xff)) {
          return false;
        }
        hint[i][pos] = true;
        idx++;
      }
    }
    for (int i = idx; i < p.omega; i++) {
      if (sig[off + i] != 0) {
        return false;
      }
    }
    byte[] ct = Arrays.copyOfRange(sig, 0, p.ctilde);
    int[][][] a = expandA(p, rho);
    byte[] tr = h(64, pk);
    byte[] mu = h(64, tr, mPrime);
    int[] ch = copyNtt(sampleInBall(p, ct));
    int[][] zh = new int[p.l][];
    for (int i = 0; i < p.l; i++) {
      zh[i] = copyNtt(z[i]);
    }
    int[][] w1 = new int[p.k][N];
    for (int r = 0; r < p.k; r++) {
      int[] t = new int[N];
      for (int s = 0; s < p.l; s++) {
        addTo(t, mul(a[r][s], zh[s]));
      }
      int[] t1 = unpack(pk, 32 + 320 * r, 10, 0);
      for (int j = 0; j < N; j++) {
        t1[j] = (int) ((long) t1[j] * (1 << D) % Q);
      }
      ntt(t1);
      int[] sub = mul(ch, t1);
      for (int j = 0; j < N; j++) {
        t[j] = Math.floorMod(t[j] - sub[j], Q);
      }
      invNtt(t);
      for (int j = 0; j < N; j++) {
        w1[r][j] = useHint(p, hint[r][j], t[j]);
      }
    }
    byte[] ct2 = h(p.ctilde, mu, w1Encode(p, w1));
    return MessageDigest.isEqual(ct, ct2);
  }
}
