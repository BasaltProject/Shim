/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class SecureRandom extends Random {
  private static final long serialVersionUID = 4939644638883676188L;

  private static final FileInputStream urandomStream;

  static {
    FileInputStream stream = null;
    try {
      File urandom = new File("/dev/urandom");
      if (urandom.exists() && urandom.canRead()) {
        stream = new FileInputStream(urandom);
      }
    } catch (Throwable ignored) {
    }
    urandomStream = stream;
  }

  private final String algorithm;
  private final MessageDigest digest;
  private final byte[] state = new byte[32];
  private long counter = 0;
  private boolean seeded = false;

  public SecureRandom() {
    this("SHA1PRNG");
  }

  public SecureRandom(byte[] seed) {
    this("SHA1PRNG");
    setSeed(seed);
  }

  private SecureRandom(String algorithm) {
    super(0L);
    this.algorithm = algorithm;
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      try {
        md = MessageDigest.getInstance("SHA-1");
      } catch (NoSuchAlgorithmException ex) {
        throw new RuntimeException("No digest algorithm available for SecureRandom", ex);
      }
    }
    this.digest = md;
    initDefaultSeed();
  }

  public static SecureRandom getInstance(String algorithm) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "algorithm");
    String norm = algorithm.toUpperCase(Locale.ROOT);
    if ("SHA1PRNG".equals(norm) || "NATIVEPRNG".equals(norm) || "DRBG".equals(norm)
        || "DEFAULT".equals(norm) || "PKCS11".equals(norm)) {
      return new SecureRandom(algorithm);
    }
    throw new NoSuchAlgorithmException(algorithm + " SecureRandom not available");
  }

  public static SecureRandom getInstanceStrong() throws NoSuchAlgorithmException {
    return new SecureRandom("NativePRNG");
  }

  public String getAlgorithm() {
    return algorithm;
  }

  private synchronized void initDefaultSeed() {
    byte[] seedBytes = new byte[32];
    boolean entropyGathered = false;

    if (urandomStream != null) {
      try {
        synchronized (urandomStream) {
          int offset = 0;
          while (offset < seedBytes.length) {
            int read = urandomStream.read(seedBytes, offset, seedBytes.length - offset);
            if (read < 0) {
              break;
            }
            offset += read;
          }
          if (offset == seedBytes.length) {
            entropyGathered = true;
          }
        }
      } catch (IOException ignored) {
      }
    }

    if (!entropyGathered) {
      long t1 = System.currentTimeMillis();
      long t2 = System.nanoTime();
      int hash = System.identityHashCode(this);
      long mem = Runtime.getRuntime().freeMemory();

      for (int i = 0; i < 8; i++) {
        seedBytes[i] = (byte) (t1 >>> (i * 8));
        seedBytes[i + 8] = (byte) (t2 >>> (i * 8));
        seedBytes[i + 16] = (byte) (mem >>> (i * 8));
      }
      seedBytes[24] = (byte) (hash);
      seedBytes[25] = (byte) (hash >>> 8);
      seedBytes[26] = (byte) (hash >>> 16);
      seedBytes[27] = (byte) (hash >>> 24);
    }

    setSeed(seedBytes);
    this.seeded = false;
  }

  public synchronized void setSeed(byte[] seed) {
    if (seed == null || seed.length == 0) {
      return;
    }
    digest.update(state);
    digest.update(seed);
    byte[] newHash = digest.digest();
    System.arraycopy(newHash, 0, state, 0, Math.min(state.length, newHash.length));
    this.seeded = true;
  }

  @Override
  public void setSeed(long seed) {
    if (seed == 0L) {
      return;
    }
    byte[] b = new byte[8];
    for (int i = 0; i < 8; i++) {
      b[i] = (byte) (seed >>> (i * 8));
    }
    setSeed(b);
  }

  @Override
  public synchronized void nextBytes(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      return;
    }

    if ("NATIVEPRNG".equalsIgnoreCase(algorithm) && urandomStream != null) {
      try {
        synchronized (urandomStream) {
          int offset = 0;
          while (offset < bytes.length) {
            int read = urandomStream.read(bytes, offset, bytes.length - offset);
            if (read < 0) {
              break;
            }
            offset += read;
          }
          if (offset == bytes.length) {
            return;
          }
        }
      } catch (IOException ignored) {
      }
    }

    int offset = 0;
    while (offset < bytes.length) {
      digest.update(state);
      long c = ++counter;
      for (int i = 0; i < 8; i++) {
        digest.update((byte) (c >>> (i * 8)));
      }
      byte[] block = digest.digest();
      System.arraycopy(block, 0, state, 0, Math.min(state.length, block.length));

      int toCopy = Math.min(bytes.length - offset, block.length);
      System.arraycopy(block, 0, bytes, offset, toCopy);
      offset += toCopy;
    }
  }

  @Override
  protected final int next(int numBits) {
    if (numBits <= 0) {
      return 0;
    }
    int numBytes = (numBits + 7) / 8;
    byte[] b = new byte[numBytes];
    nextBytes(b);

    int val = 0;
    for (int i = 0; i < numBytes; i++) {
      val = (val << 8) | (b[i] & 0xFF);
    }
    return val >>> (numBytes * 8 - numBits);
  }

  public static byte[] getSeed(int numBytes) {
    SecureRandom sr = new SecureRandom();
    return sr.generateSeed(numBytes);
  }

  public byte[] generateSeed(int numBytes) {
    if (numBytes < 0) {
      throw new NegativeArraySizeException("Negative seed size");
    }
    byte[] seed = new byte[numBytes];
    if (urandomStream != null) {
      try {
        synchronized (urandomStream) {
          int offset = 0;
          while (offset < numBytes) {
            int read = urandomStream.read(seed, offset, numBytes - offset);
            if (read < 0) {
              break;
            }
            offset += read;
          }
          if (offset == numBytes) {
            return seed;
          }
        }
      } catch (IOException ignored) {
      }
    }

    nextBytes(seed);
    return seed;
  }
}
