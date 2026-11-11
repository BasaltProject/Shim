/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.io.ByteArrayOutputStream;
import java.security.AsymmetricKey;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;

/** Encodings and key classes for ML-KEM. */
public final class MLKEMKeys {
  private static final byte[] kOidPrefix = {0x60, (byte) 0x86, 0x48, 0x01, 0x65, 0x03, 0x04, 0x04};

  private MLKEMKeys() {
  }

  static void len(ByteArrayOutputStream o, int n) {
    if (n < 128) {
      o.write(n);
    } else if (n < 256) {
      o.write(0x81);
      o.write(n);
    } else {
      o.write(0x82);
      o.write(n >> 8);
      o.write(n);
    }
  }

  static byte[] tlv(int tag, byte[]... parts) {
    int total = 0;
    for (byte[] p : parts) {
      total += p.length;
    }
    ByteArrayOutputStream o = new ByteArrayOutputStream();
    o.write(tag);
    len(o, total);
    for (byte[] p : parts) {
      o.write(p, 0, p.length);
    }
    return o.toByteArray();
  }

  static byte[] algId(MLKEM.Params p) {
    byte[] oid = Arrays.copyOf(kOidPrefix, 9);
    oid[8] = (byte) p.oidLast;
    return tlv(0x30, tlv(0x06, oid));
  }

  /** Parses a DER element at off; returns {tag, contentStart, contentEnd}. */
  static int[] read(byte[] b, int off, int end) throws InvalidKeyException {
    if (off + 2 > end) {
      throw new InvalidKeyException("Invalid encoding");
    }
    int tag = b[off] & 0xff;
    int l = b[off + 1] & 0xff;
    int start = off + 2;
    if (l >= 0x80) {
      int nb = l & 0x7f;
      if (nb < 1 || nb > 3 || start + nb > end) {
        throw new InvalidKeyException("Invalid encoding");
      }
      l = 0;
      for (int i = 0; i < nb; i++) {
        l = (l << 8) | (b[start++] & 0xff);
      }
    }
    if (start + l > end) {
      throw new InvalidKeyException("Invalid encoding");
    }
    return new int[] {tag, start, start + l};
  }

  static MLKEM.Params paramsOf(byte[] b, int[] algSeq) throws InvalidKeyException {
    int[] oid = read(b, algSeq[1], algSeq[2]);
    if (oid[0] != 0x06 || oid[2] - oid[1] != 9) {
      throw new InvalidKeyException("Invalid algorithm identifier");
    }
    for (int i = 0; i < 8; i++) {
      if (b[oid[1] + i] != kOidPrefix[i]) {
        throw new InvalidKeyException("Unsupported algorithm");
      }
    }
    MLKEM.Params p = MLKEM.Params.byOid(b[oid[1] + 8]);
    if (p == null) {
      throw new InvalidKeyException("Unsupported algorithm");
    }
    return p;
  }

  public static final class Pub implements PublicKey, AsymmetricKey {
    private static final long serialVersionUID = 1L;
    final transient MLKEM.Params params;
    final byte[] raw;

    Pub(MLKEM.Params params, byte[] raw) {
      this.params = params;
      this.raw = raw;
    }

    @Override
    public String getAlgorithm() {
      return "ML-KEM";
    }

    @Override
    public String getFormat() {
      return "X.509";
    }

    @Override
    public byte[] getEncoded() {
      byte[] bits = new byte[raw.length + 1];
      System.arraycopy(raw, 0, bits, 1, raw.length);
      return tlv(0x30, algId(params), tlv(0x03, bits));
    }

    @Override
    public java.security.spec.AlgorithmParameterSpec getParams() {
      return new NamedParameterSpec(params.name);
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof Pub && ((Pub) o).params == params && Arrays.equals(((Pub) o).raw, raw);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(raw) ^ params.ordinal();
    }

    @Override
    public String toString() {
      return "ML-KEM public key (" + params.name + ")";
    }
  }

  public static final class Priv implements PrivateKey, AsymmetricKey {
    private static final long serialVersionUID = 1L;
    final transient MLKEM.Params params;
    final byte[] seed;
    final byte[] expanded;

    Priv(MLKEM.Params params, byte[] seed, byte[] expanded) {
      this.params = params;
      this.seed = seed;
      this.expanded = expanded;
    }

    @Override
    public String getAlgorithm() {
      return "ML-KEM";
    }

    @Override
    public String getFormat() {
      return "PKCS#8";
    }

    @Override
    public byte[] getEncoded() {
      byte[] body = seed != null ? tlv(0x80, seed) : tlv(0x04, expanded);
      return tlv(0x30, new byte[] {0x02, 0x01, 0x00}, algId(params), tlv(0x04, body));
    }

    @Override
    public java.security.spec.AlgorithmParameterSpec getParams() {
      return new NamedParameterSpec(params.name);
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof Priv && ((Priv) o).params == params && Arrays.equals(((Priv) o).expanded, expanded);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(expanded) ^ params.ordinal();
    }

    @Override
    public String toString() {
      return "ML-KEM private key (" + params.name + ")";
    }
  }

  public static Pub parsePublic(byte[] enc) throws InvalidKeyException {
    int[] seq = read(enc, 0, enc.length);
    if (seq[0] != 0x30 || seq[2] != enc.length) {
      throw new InvalidKeyException("Invalid encoding");
    }
    int[] alg = read(enc, seq[1], seq[2]);
    if (alg[0] != 0x30) {
      throw new InvalidKeyException("Invalid encoding");
    }
    MLKEM.Params p = paramsOf(enc, alg);
    int[] bits = read(enc, alg[2], seq[2]);
    if (bits[0] != 0x03 || bits[2] != seq[2] || enc[bits[1]] != 0) {
      throw new InvalidKeyException("Invalid encoding");
    }
    byte[] raw = Arrays.copyOfRange(enc, bits[1] + 1, bits[2]);
    if (raw.length != p.publicKeySize()) {
      throw new InvalidKeyException("Invalid key length");
    }
    return new Pub(p, raw);
  }

  public static Priv parsePrivate(byte[] enc) throws InvalidKeyException {
    int[] seq = read(enc, 0, enc.length);
    if (seq[0] != 0x30 || seq[2] != enc.length) {
      throw new InvalidKeyException("Invalid encoding");
    }
    int[] ver = read(enc, seq[1], seq[2]);
    int[] alg = read(enc, ver[2], seq[2]);
    if (ver[0] != 0x02 || alg[0] != 0x30) {
      throw new InvalidKeyException("Invalid encoding");
    }
    MLKEM.Params p = paramsOf(enc, alg);
    int[] oct = read(enc, alg[2], seq[2]);
    if (oct[0] != 0x04 || oct[2] != seq[2]) {
      throw new InvalidKeyException("Invalid encoding");
    }
    int[] inner = read(enc, oct[1], oct[2]);
    if (inner[2] != oct[2]) {
      throw new InvalidKeyException("Invalid encoding");
    }
    if (inner[0] == 0x80 && inner[2] - inner[1] == 64) {
      byte[] seed = Arrays.copyOfRange(enc, inner[1], inner[2]);
      byte[][] kp = MLKEM.keyGen(p, Arrays.copyOfRange(seed, 0, 32), Arrays.copyOfRange(seed, 32, 64));
      return new Priv(p, seed, kp[1]);
    }
    if (inner[0] == 0x04 && inner[2] - inner[1] == p.privateKeySize()) {
      return new Priv(p, null, Arrays.copyOfRange(enc, inner[1], inner[2]));
    }
    throw new InvalidKeyException("Invalid key length");
  }
}
