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

/** ML-KEM (FIPS 203) core operations. */
public final class MLKEM {
  private static final int N = 256;
  private static final int Q = 3329;
  private static final int[] ZETAS = new int[128];
  private static final int[] GAMMAS = new int[128];

  static {
    for (int i = 0; i < 128; i++) {
      int rev = Integer.reverse(i) >>> 25;
      ZETAS[i] = pow17(rev);
      GAMMAS[i] = pow17(2 * rev + 1);
    }
  }

  private static int pow17(int e) {
    int r = 1;
    for (int i = 0; i < e; i++) {
      r = r * 17 % Q;
    }
    return r;
  }

  public enum Params {
    ML_KEM_512("ML-KEM-512", 2, 3, 2, 10, 4, 1),
    ML_KEM_768("ML-KEM-768", 3, 2, 2, 10, 4, 2),
    ML_KEM_1024("ML-KEM-1024", 4, 2, 2, 11, 5, 3);

    public final String name;
    final int k;
    final int eta1;
    final int eta2;
    final int du;
    final int dv;
    public final int oidLast;

    Params(String name, int k, int eta1, int eta2, int du, int dv, int oidLast) {
      this.name = name;
      this.k = k;
      this.eta1 = eta1;
      this.eta2 = eta2;
      this.du = du;
      this.dv = dv;
      this.oidLast = oidLast;
    }

    public int publicKeySize() {
      return 384 * k + 32;
    }

    public int privateKeySize() {
      return 768 * k + 96;
    }

    public int ciphertextSize() {
      return 32 * (du * k + dv);
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

  private MLKEM() {
  }

  private static byte[] g(byte[]... in) {
    Keccak h = Keccak.sha3_512();
    for (byte[] b : in) {
      h.absorb(b);
    }
    return h.squeeze(64);
  }

  private static byte[] h(byte[] in) {
    return Keccak.sha3_256().absorb(in).squeeze(32);
  }

  private static byte[] j(byte[] z, byte[] c) {
    return Keccak.shake256().absorb(z).absorb(c).squeeze(32);
  }

  private static byte[] prf(byte[] s, int n, int len) {
    return Keccak.shake256().absorb(s).absorb(new byte[] {(byte) n}).squeeze(len);
  }

  private static int[] cbd(byte[] b, int eta) {
    int[] f = new int[N];
    for (int i = 0; i < N; i++) {
      int x = 0;
      int y = 0;
      for (int j = 0; j < eta; j++) {
        int bit = 2 * i * eta + j;
        x += (b[bit >> 3] >> (bit & 7)) & 1;
        bit += eta;
        y += (b[bit >> 3] >> (bit & 7)) & 1;
      }
      f[i] = Math.floorMod(x - y, Q);
    }
    return f;
  }

  private static int[] sampleNtt(byte[] rho, int a, int b) {
    Keccak x = Keccak.shake128().absorb(rho).absorb(new byte[] {(byte) a, (byte) b});
    int[] r = new int[N];
    int n = 0;
    byte[] c = new byte[3];
    while (n < N) {
      x.squeeze(c, 0, 3);
      int d1 = (c[0] & 0xff) + 256 * (c[1] & 0x0f);
      int d2 = ((c[1] & 0xff) >> 4) + 16 * (c[2] & 0xff);
      if (d1 < Q) {
        r[n++] = d1;
      }
      if (d2 < Q && n < N) {
        r[n++] = d2;
      }
    }
    return r;
  }

  private static void ntt(int[] f) {
    int i = 1;
    for (int len = 128; len >= 2; len >>= 1) {
      for (int start = 0; start < N; start += 2 * len) {
        int zeta = ZETAS[i++];
        for (int j = start; j < start + len; j++) {
          int t = zeta * f[j + len] % Q;
          f[j + len] = Math.floorMod(f[j] - t, Q);
          f[j] = (f[j] + t) % Q;
        }
      }
    }
  }

  private static void invNtt(int[] f) {
    int i = 127;
    for (int len = 2; len <= 128; len <<= 1) {
      for (int start = 0; start < N; start += 2 * len) {
        int zeta = ZETAS[i--];
        for (int j = start; j < start + len; j++) {
          int t = f[j];
          f[j] = (t + f[j + len]) % Q;
          f[j + len] = zeta * Math.floorMod(f[j + len] - t, Q) % Q;
        }
      }
    }
    for (int j = 0; j < N; j++) {
      f[j] = f[j] * 3303 % Q;
    }
  }

  private static void mulAdd(int[] acc, int[] f, int[] g) {
    for (int i = 0; i < 128; i++) {
      int a0 = f[2 * i];
      int a1 = f[2 * i + 1];
      int b0 = g[2 * i];
      int b1 = g[2 * i + 1];
      acc[2 * i] = (acc[2 * i] + a0 * b0 + (a1 * b1 % Q) * GAMMAS[i]) % Q;
      acc[2 * i + 1] = (acc[2 * i + 1] + a0 * b1 + a1 * b0) % Q;
    }
  }

  private static byte[] encode(int[] f, int d) {
    byte[] out = new byte[32 * d];
    int bit = 0;
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < d; j++) {
        out[bit >> 3] |= (byte) (((f[i] >> j) & 1) << (bit & 7));
        bit++;
      }
    }
    return out;
  }

  private static int[] decode(byte[] in, int off, int d) {
    int[] f = new int[N];
    int bit = 0;
    for (int i = 0; i < N; i++) {
      int v = 0;
      for (int j = 0; j < d; j++) {
        v |= ((in[off + (bit >> 3)] >> (bit & 7)) & 1) << j;
        bit++;
      }
      f[i] = d == 12 ? v % Q : v;
    }
    return f;
  }

  private static int compress(int x, int d) {
    return (int) ((((long) x << d) + Q / 2) / Q) & ((1 << d) - 1);
  }

  private static int decompress(int y, int d) {
    return (y * Q + (1 << (d - 1))) >> d;
  }

  private static int[][] matrix(Params p, byte[] rho) {
    int[][] a = new int[p.k * p.k][];
    for (int i = 0; i < p.k; i++) {
      for (int j = 0; j < p.k; j++) {
        a[i * p.k + j] = sampleNtt(rho, j, i);
      }
    }
    return a;
  }

  public static byte[][] keyGen(Params p, byte[] d, byte[] z) {
    byte[] gr = g(d, new byte[] {(byte) p.k});
    byte[] rho = Arrays.copyOfRange(gr, 0, 32);
    byte[] sigma = Arrays.copyOfRange(gr, 32, 64);
    int n = 0;
    int[][] s = new int[p.k][];
    int[][] e = new int[p.k][];
    for (int i = 0; i < p.k; i++) {
      s[i] = cbd(prf(sigma, n++, 64 * p.eta1), p.eta1);
      ntt(s[i]);
    }
    for (int i = 0; i < p.k; i++) {
      e[i] = cbd(prf(sigma, n++, 64 * p.eta1), p.eta1);
      ntt(e[i]);
    }
    int[][] a = matrix(p, rho);
    byte[] ek = new byte[p.publicKeySize()];
    byte[] dkPke = new byte[384 * p.k];
    for (int i = 0; i < p.k; i++) {
      int[] t = new int[N];
      for (int j = 0; j < p.k; j++) {
        mulAdd(t, a[i * p.k + j], s[j]);
      }
      for (int c = 0; c < N; c++) {
        t[c] = (t[c] + e[i][c]) % Q;
      }
      System.arraycopy(encode(t, 12), 0, ek, 384 * i, 384);
      System.arraycopy(encode(s[i], 12), 0, dkPke, 384 * i, 384);
    }
    System.arraycopy(rho, 0, ek, 384 * p.k, 32);
    byte[] dk = new byte[p.privateKeySize()];
    System.arraycopy(dkPke, 0, dk, 0, dkPke.length);
    System.arraycopy(ek, 0, dk, dkPke.length, ek.length);
    System.arraycopy(h(ek), 0, dk, dkPke.length + ek.length, 32);
    System.arraycopy(z, 0, dk, dkPke.length + ek.length + 32, 32);
    return new byte[][] {ek, dk};
  }

  private static byte[] pkeEncrypt(Params p, byte[] ek, byte[] m, byte[] r) {
    int[][] t = new int[p.k][];
    for (int i = 0; i < p.k; i++) {
      t[i] = decode(ek, 384 * i, 12);
    }
    byte[] rho = Arrays.copyOfRange(ek, 384 * p.k, 384 * p.k + 32);
    int[][] a = matrix(p, rho);
    int n = 0;
    int[][] y = new int[p.k][];
    for (int i = 0; i < p.k; i++) {
      y[i] = cbd(prf(r, n++, 64 * p.eta1), p.eta1);
      ntt(y[i]);
    }
    int[][] e1 = new int[p.k][];
    for (int i = 0; i < p.k; i++) {
      e1[i] = cbd(prf(r, n++, 64 * p.eta2), p.eta2);
    }
    int[] e2 = cbd(prf(r, n, 64 * p.eta2), p.eta2);
    byte[] c = new byte[p.ciphertextSize()];
    for (int i = 0; i < p.k; i++) {
      int[] u = new int[N];
      for (int j = 0; j < p.k; j++) {
        mulAdd(u, a[j * p.k + i], y[j]);
      }
      invNtt(u);
      for (int x = 0; x < N; x++) {
        u[x] = compress((u[x] + e1[i][x]) % Q, p.du);
      }
      System.arraycopy(encode(u, p.du), 0, c, 32 * p.du * i, 32 * p.du);
    }
    int[] v = new int[N];
    for (int j = 0; j < p.k; j++) {
      mulAdd(v, t[j], y[j]);
    }
    invNtt(v);
    int[] mu = decode(m, 0, 1);
    for (int x = 0; x < N; x++) {
      v[x] = compress((v[x] + e2[x] + decompress(mu[x], 1)) % Q, p.dv);
    }
    System.arraycopy(encode(v, p.dv), 0, c, 32 * p.du * p.k, 32 * p.dv);
    return c;
  }

  private static byte[] pkeDecrypt(Params p, byte[] dkPke, byte[] c) {
    int[] w = new int[N];
    for (int i = 0; i < p.k; i++) {
      int[] u = decode(c, 32 * p.du * i, p.du);
      for (int x = 0; x < N; x++) {
        u[x] = decompress(u[x], p.du);
      }
      ntt(u);
      mulAdd(w, decode(dkPke, 384 * i, 12), u);
    }
    invNtt(w);
    int[] v = decode(c, 32 * p.du * p.k, p.dv);
    int[] m = new int[N];
    for (int x = 0; x < N; x++) {
      m[x] = compress(Math.floorMod(decompress(v[x], p.dv) - w[x], Q), 1);
    }
    return encode(m, 1);
  }

  public static byte[][] encaps(Params p, byte[] ek, byte[] m) {
    byte[] kr = g(m, h(ek));
    byte[] key = Arrays.copyOfRange(kr, 0, 32);
    byte[] r = Arrays.copyOfRange(kr, 32, 64);
    return new byte[][] {key, pkeEncrypt(p, ek, m, r)};
  }

  public static byte[] decaps(Params p, byte[] dk, byte[] c) {
    byte[] dkPke = Arrays.copyOfRange(dk, 0, 384 * p.k);
    byte[] ek = Arrays.copyOfRange(dk, 384 * p.k, 768 * p.k + 32);
    byte[] hh = Arrays.copyOfRange(dk, 768 * p.k + 32, 768 * p.k + 64);
    byte[] z = Arrays.copyOfRange(dk, 768 * p.k + 64, 768 * p.k + 96);
    byte[] m = pkeDecrypt(p, dkPke, c);
    byte[] kr = g(m, hh);
    byte[] key = Arrays.copyOfRange(kr, 0, 32);
    byte[] c2 = pkeEncrypt(p, ek, m, Arrays.copyOfRange(kr, 32, 64));
    byte[] bad = j(z, c);
    return MessageDigest.isEqual(c, c2) ? key : bad;
  }

  public static boolean checkPublicKey(Params p, byte[] ek) {
    if (ek.length != p.publicKeySize()) {
      return false;
    }
    for (int i = 0; i < p.k; i++) {
      int[] f = decode(ek, 384 * i, 12);
      if (!Arrays.equals(encode(f, 12), Arrays.copyOfRange(ek, 384 * i, 384 * i + 384))) {
        return false;
      }
    }
    return true;
  }
}
