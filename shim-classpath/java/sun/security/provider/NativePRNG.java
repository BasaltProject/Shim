/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.security.SecureRandomParameters;
import java.security.SecureRandomSpi;

public class NativePRNG extends SecureRandomSpi {
  private static final long serialVersionUID = -6599091113397072932L;

  private final String randomSource;
  private final String seedSource;
  private transient SecureRandom mix;

  public NativePRNG() {
    this("/dev/urandom", "/dev/random");
  }

  public NativePRNG(SecureRandomParameters params) {
    this();
    if (params != null) {
      throw new IllegalArgumentException("Unsupported params: " + params.getClass());
    }
  }

  NativePRNG(String randomSource, String seedSource) {
    this.randomSource = randomSource;
    this.seedSource = seedSource;
  }

  @Override
  protected void engineSetSeed(byte[] seed) {
    synchronized (this) {
      if (mix == null) {
        mix = new SecureRandom();
      }
      mix.engineSetSeed(seed);
    }
  }

  @Override
  protected void engineNextBytes(byte[] bytes) {
    NativeSeed.read(randomSource, bytes, 0, bytes.length);
    SecureRandom m = mix;
    if (m != null) {
      byte[] pad = new byte[bytes.length];
      m.engineNextBytes(pad);
      for (int i = 0; i < bytes.length; i++) {
        bytes[i] ^= pad[i];
      }
    }
  }

  @Override
  protected byte[] engineGenerateSeed(int numBytes) {
    byte[] b = new byte[numBytes];
    NativeSeed.read(seedSource, b, 0, numBytes);
    return b;
  }

  public static final class Blocking extends NativePRNG {
    private static final long serialVersionUID = -6396183145759983347L;

    public Blocking() {
      super("/dev/random", "/dev/random");
    }

    public Blocking(SecureRandomParameters params) {
      this();
      if (params != null) {
        throw new IllegalArgumentException("Unsupported params: " + params.getClass());
      }
    }
  }

  public static final class NonBlocking extends NativePRNG {
    private static final long serialVersionUID = -1102062982994105487L;

    public NonBlocking() {
      super("/dev/urandom", "/dev/urandom");
    }

    public NonBlocking(SecureRandomParameters params) {
      this();
      if (params != null) {
        throw new IllegalArgumentException("Unsupported params: " + params.getClass());
      }
    }
  }
}
