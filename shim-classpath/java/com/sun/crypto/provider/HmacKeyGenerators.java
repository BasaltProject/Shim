/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

final class HmacKeyGenerators {
  private HmacKeyGenerators() {
  }

  public static final class HmacMD5 extends KeyGeneratorCore {
    public HmacMD5() {
      super("HmacMD5", 512);
    }
  }

  public static final class HmacSHA1 extends KeyGeneratorCore {
    public HmacSHA1() {
      super("HmacSHA1", 512);
    }
  }

  public static final class HmacSHA224 extends KeyGeneratorCore {
    public HmacSHA224() {
      super("HmacSHA224", 224);
    }
  }

  public static final class HmacSHA256 extends KeyGeneratorCore {
    public HmacSHA256() {
      super("HmacSHA256", 256);
    }
  }

  public static final class HmacSHA384 extends KeyGeneratorCore {
    public HmacSHA384() {
      super("HmacSHA384", 384);
    }
  }

  public static final class HmacSHA512 extends KeyGeneratorCore {
    public HmacSHA512() {
      super("HmacSHA512", 512);
    }
  }

  public static final class HmacSHA512_224 extends KeyGeneratorCore {
    public HmacSHA512_224() {
      super("HmacSHA512/224", 224);
    }
  }

  public static final class HmacSHA512_256 extends KeyGeneratorCore {
    public HmacSHA512_256() {
      super("HmacSHA512/256", 256);
    }
  }

  public static final class HmacSHA3_224 extends KeyGeneratorCore {
    public HmacSHA3_224() {
      super("HmacSHA3-224", 224);
    }
  }

  public static final class HmacSHA3_256 extends KeyGeneratorCore {
    public HmacSHA3_256() {
      super("HmacSHA3-256", 256);
    }
  }

  public static final class HmacSHA3_384 extends KeyGeneratorCore {
    public HmacSHA3_384() {
      super("HmacSHA3-384", 384);
    }
  }

  public static final class HmacSHA3_512 extends KeyGeneratorCore {
    public HmacSHA3_512() {
      super("HmacSHA3-512", 512);
    }
  }
}
