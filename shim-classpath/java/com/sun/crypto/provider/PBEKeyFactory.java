/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

final class PBEKeyFactory extends PBES2KeyFactory {
  private PBEKeyFactory(String algorithm) {
    super(algorithm);
  }

  public static final class PBEWithMD5AndDES extends PBES2KeyFactory {
    public PBEWithMD5AndDES() {
      super("PBEWithMD5AndDES");
    }
  }

  public static final class PBEWithMD5AndTripleDES extends PBES2KeyFactory {
    public PBEWithMD5AndTripleDES() {
      super("PBEWithMD5AndTripleDES");
    }
  }

  public static final class PBEWithSHA1AndDESede extends PBES2KeyFactory {
    public PBEWithSHA1AndDESede() {
      super("PBEWithSHA1AndDESede");
    }
  }

  public static final class PBEWithSHA1AndRC2_40 extends PBES2KeyFactory {
    public PBEWithSHA1AndRC2_40() {
      super("PBEWithSHA1AndRC2_40");
    }
  }

  public static final class PBEWithSHA1AndRC2_128 extends PBES2KeyFactory {
    public PBEWithSHA1AndRC2_128() {
      super("PBEWithSHA1AndRC2_128");
    }
  }

  public static final class PBEWithSHA1AndRC4_40 extends PBES2KeyFactory {
    public PBEWithSHA1AndRC4_40() {
      super("PBEWithSHA1AndRC4_40");
    }
  }

  public static final class PBEWithSHA1AndRC4_128 extends PBES2KeyFactory {
    public PBEWithSHA1AndRC4_128() {
      super("PBEWithSHA1AndRC4_128");
    }
  }
}
