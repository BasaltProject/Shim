/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.security.DrbgParameters;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandomParameters;
import java.security.SecureRandomSpi;
import java.util.Arrays;

public final class DRBG extends SecureRandomSpi {
  private static final long serialVersionUID = 9L;

  private static final int kOutLen = 32;
  private static final int kSeedLen = 55;
  private static final long kReseedInterval = 1L << 24;

  private final int strength;
  private final DrbgParameters.Capability capability;
  private final byte[] personalization;
  private transient MessageDigest sha;
  private byte[] v;
  private byte[] c;
  private long reseedCounter;
  private boolean instantiated;

  public DRBG(SecureRandomParameters params) {
    int s = 128;
    DrbgParameters.Capability cap = DrbgParameters.Capability.RESEED_ONLY;
    byte[] ps = null;
    if (params != null) {
      if (!(params instanceof DrbgParameters.Instantiation)) {
        throw new IllegalArgumentException("Unsupported params: " + params.getClass());
      }
      DrbgParameters.Instantiation inst = (DrbgParameters.Instantiation) params;
      if (inst.getStrength() > 256) {
        throw new IllegalArgumentException("strength too high: " + inst.getStrength());
      }
      if (inst.getStrength() >= 0) {
        s = inst.getStrength() < 112 ? 112 : inst.getStrength();
      }
      cap = inst.getCapability();
      ps = inst.getPersonalizationString();
    }
    this.strength = s <= 128 ? 128 : s <= 192 ? 192 : 256;
    this.capability = cap;
    this.personalization = ps;
    try {
      sha = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new InternalError(e);
    }
    instantiate();
  }

  private byte[] hashDf(byte[] input, int bits) {
    int len = (bits + 7) / 8;
    int blocks = (len + kOutLen - 1) / kOutLen;
    byte[] out = new byte[blocks * kOutLen];
    for (int i = 1; i <= blocks; i++) {
      sha.update((byte) i);
      sha.update(new byte[] {(byte) (bits >>> 24), (byte) (bits >>> 16), (byte) (bits >>> 8), (byte) bits});
      sha.update(input);
      byte[] h = sha.digest();
      System.arraycopy(h, 0, out, (i - 1) * kOutLen, kOutLen);
    }
    return Arrays.copyOf(out, len);
  }

  private static byte[] concat(byte[]... parts) {
    int n = 0;
    for (byte[] p : parts) {
      if (p != null) {
        n += p.length;
      }
    }
    byte[] r = new byte[n];
    int o = 0;
    for (byte[] p : parts) {
      if (p != null) {
        System.arraycopy(p, 0, r, o, p.length);
        o += p.length;
      }
    }
    return r;
  }

  private static void add(byte[] target, byte[] addend) {
    int carry = 0;
    int ti = target.length - 1;
    for (int ai = addend.length - 1; ti >= 0; ti--, ai--) {
      int sum = (target[ti] & 0xff) + (ai >= 0 ? addend[ai] & 0xff : 0) + carry;
      target[ti] = (byte) sum;
      carry = sum >>> 8;
    }
  }

  private static byte[] entropy(int n) {
    return NativeSeed.generate(n);
  }

  private void instantiate() {
    byte[] seedMaterial = concat(entropy(strength / 8), entropy(strength / 16), personalization);
    v = hashDf(seedMaterial, kSeedLen * 8);
    c = hashDf(concat(new byte[] {0}, v), kSeedLen * 8);
    reseedCounter = 1;
    instantiated = true;
  }

  private void reseedInternal(byte[] additional) {
    byte[] seedMaterial = concat(new byte[] {1}, v, entropy(strength / 8), additional);
    v = hashDf(seedMaterial, kSeedLen * 8);
    c = hashDf(concat(new byte[] {0}, v), kSeedLen * 8);
    reseedCounter = 1;
  }

  private void generate(byte[] out, byte[] additional) {
    if (reseedCounter > kReseedInterval) {
      reseedInternal(null);
    }
    if (additional != null && additional.length > 0) {
      sha.update((byte) 2);
      sha.update(v);
      sha.update(additional);
      byte[] w = sha.digest();
      add(v, w);
    }
    byte[] data = v.clone();
    int produced = 0;
    byte[] one = new byte[] {1};
    while (produced < out.length) {
      byte[] h = sha.digest(data);
      int n = Math.min(kOutLen, out.length - produced);
      System.arraycopy(h, 0, out, produced, n);
      produced += n;
      add(data, one);
    }
    sha.update((byte) 3);
    sha.update(v);
    byte[] h = sha.digest();
    add(v, h);
    add(v, c);
    byte[] rc = new byte[8];
    for (int i = 0; i < 8; i++) {
      rc[i] = (byte) (reseedCounter >>> (56 - 8 * i));
    }
    add(v, rc);
    reseedCounter++;
  }

  @Override
  protected synchronized void engineSetSeed(byte[] seed) {
    reseedInternal(seed);
  }

  @Override
  protected synchronized void engineNextBytes(byte[] bytes) {
    generate(bytes, null);
  }

  @Override
  protected synchronized void engineNextBytes(byte[] bytes, SecureRandomParameters params) {
    if (!(params instanceof DrbgParameters.NextBytes)) {
      throw new IllegalArgumentException("Unsupported params: " + params.getClass());
    }
    DrbgParameters.NextBytes nb = (DrbgParameters.NextBytes) params;
    if (nb.getStrength() > strength) {
      throw new IllegalArgumentException("strength too high: " + nb.getStrength());
    }
    if (nb.getPredictionResistance() && !capability.supportsPredictionResistance()) {
      throw new IllegalArgumentException("pr not available");
    }
    if (nb.getPredictionResistance()) {
      reseedInternal(nb.getAdditionalInput());
      generate(bytes, null);
    } else {
      generate(bytes, nb.getAdditionalInput());
    }
  }

  @Override
  protected byte[] engineGenerateSeed(int numBytes) {
    byte[] b = new byte[numBytes];
    NativeSeed.read("/dev/random", b, 0, numBytes);
    return b;
  }

  @Override
  protected synchronized void engineReseed(SecureRandomParameters params) {
    if (params == null) {
      reseedInternal(null);
      return;
    }
    if (!capability.supportsReseeding()) {
      throw new UnsupportedOperationException("Reseed is not supported");
    }
    if (!(params instanceof DrbgParameters.Reseed)) {
      throw new IllegalArgumentException("Unsupported params: " + params.getClass());
    }
    DrbgParameters.Reseed r = (DrbgParameters.Reseed) params;
    if (r.getPredictionResistance() && !capability.supportsPredictionResistance()) {
      throw new IllegalArgumentException("pr not available");
    }
    reseedInternal(r.getAdditionalInput());
  }

  @Override
  protected SecureRandomParameters engineGetParameters() {
    return DrbgParameters.instantiation(strength, capability, personalization);
  }

  @Override
  public String toString() {
    return "Hash_DRBG,SHA-256," + strength + "," + capability;
  }
}
