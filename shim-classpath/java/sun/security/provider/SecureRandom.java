/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandomParameters;
import java.security.SecureRandomSpi;
import java.util.Arrays;

public final class SecureRandom extends SecureRandomSpi {
  private static final long serialVersionUID = 3581829991155417889L;

  private static final int DIGEST_SIZE = 20;
  private transient MessageDigest digest;
  private byte[] state;
  private byte[] remainder;
  private int remCount;

  public SecureRandom() {
    init(null);
  }

  public SecureRandom(byte[] seed) {
    init(seed);
  }

  public SecureRandom(SecureRandomParameters params) {
    if (params != null) {
      throw new IllegalArgumentException("Unsupported params: " + params.getClass());
    }
    init(null);
  }

  private void init(byte[] seed) {
    try {
      digest = MessageDigest.getInstance("SHA", "SUN");
    } catch (NoSuchAlgorithmException | java.security.NoSuchProviderException e) {
      throw new InternalError("internal error: SHA-1 not available.", e);
    }
    if (seed != null) {
      engineSetSeed(seed);
    }
  }

  private static final class SeederHolder {
    static final SecureRandom seeder;

    static {
      seeder = new SecureRandom(NativeSeed.generate(DIGEST_SIZE));
      byte[] extra = new byte[DIGEST_SIZE];
      seeder.engineNextBytes(extra);
    }
  }

  @Override
  public synchronized void engineSetSeed(byte[] seed) {
    if (state != null) {
      digest.update(state);
      for (int i = 0; i < state.length; i++) {
        state[i] = 0;
      }
    }
    state = digest.digest(seed);
    remCount = 0;
  }

  private static void updateState(byte[] state, byte[] output) {
    int last = 1;
    int v;
    byte t;
    boolean zf = false;
    for (int i = 0; i < state.length; i++) {
      v = (int) state[i] + (int) output[i] + last;
      t = (byte) v;
      zf = zf | (state[i] != t);
      state[i] = t;
      last = v >> 8;
    }
    if (!zf) {
      state[0]++;
    }
  }

  @Override
  public synchronized void engineNextBytes(byte[] result) {
    int index = 0;
    int todo;
    byte[] output = remainder;
    if (state == null) {
      byte[] seed = new byte[DIGEST_SIZE];
      SeederHolder.seeder.engineNextBytes(seed);
      state = digest.digest(seed);
    }
    int r = remCount;
    if (r > 0) {
      todo = (result.length - index) < (DIGEST_SIZE - r) ? (result.length - index) : (DIGEST_SIZE - r);
      for (int i = 0; i < todo; i++) {
        result[i] = output[r];
        output[r++] = 0;
      }
      remCount += todo;
      index += todo;
    }
    while (index < result.length) {
      digest.update(state);
      output = digest.digest();
      updateState(state, output);
      todo = (result.length - index) > DIGEST_SIZE ? DIGEST_SIZE : result.length - index;
      for (int i = 0; i < todo; i++) {
        result[index++] = output[i];
        output[i] = 0;
      }
      remCount += todo;
    }
    remainder = output;
    remCount %= DIGEST_SIZE;
  }

  @Override
  public byte[] engineGenerateSeed(int numBytes) {
    byte[] b = new byte[numBytes];
    SeederHolder.seeder.engineNextBytes(b);
    return b;
  }

  private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    try {
      digest = MessageDigest.getInstance("SHA", "SUN");
    } catch (NoSuchAlgorithmException | java.security.NoSuchProviderException e) {
      throw new InternalError("internal error: SHA-1 not available.", e);
    }
    if (remainder != null && remCount < 0) {
      remainder = Arrays.copyOf(remainder, DIGEST_SIZE);
    }
  }
}
