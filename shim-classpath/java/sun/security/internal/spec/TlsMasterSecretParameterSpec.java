/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.internal.spec;

import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.SecretKey;

public class TlsMasterSecretParameterSpec implements AlgorithmParameterSpec {
  private final SecretKey premasterSecret;
  private final int majorVersion;
  private final int minorVersion;
  private final byte[] clientRandom;
  private final byte[] serverRandom;
  private final byte[] extendedMasterSecretSessionHash;
  private final String prfHashAlg;
  private final int prfHashLength;
  private final int prfBlockSize;

  public TlsMasterSecretParameterSpec(SecretKey premasterSecret, int majorVersion, int minorVersion,
      byte[] clientRandom, byte[] serverRandom, String prfHashAlg, int prfHashLength, int prfBlockSize) {
    this(premasterSecret, majorVersion, minorVersion, clientRandom, serverRandom, new byte[0], prfHashAlg,
        prfHashLength, prfBlockSize);
  }

  public TlsMasterSecretParameterSpec(SecretKey premasterSecret, int majorVersion, int minorVersion,
      byte[] extendedMasterSecretSessionHash, String prfHashAlg, int prfHashLength, int prfBlockSize) {
    this(premasterSecret, majorVersion, minorVersion, new byte[0], new byte[0], extendedMasterSecretSessionHash,
        prfHashAlg, prfHashLength, prfBlockSize);
  }

  private TlsMasterSecretParameterSpec(SecretKey premasterSecret, int majorVersion, int minorVersion,
      byte[] clientRandom, byte[] serverRandom, byte[] sessionHash, String prfHashAlg, int prfHashLength,
      int prfBlockSize) {
    if (premasterSecret == null) {
      throw new NullPointerException("premasterSecret must not be null");
    }
    this.premasterSecret = premasterSecret;
    this.majorVersion = checkVersion(majorVersion);
    this.minorVersion = checkVersion(minorVersion);
    this.clientRandom = clientRandom.clone();
    this.serverRandom = serverRandom.clone();
    this.extendedMasterSecretSessionHash = sessionHash.clone();
    this.prfHashAlg = prfHashAlg;
    this.prfHashLength = prfHashLength;
    this.prfBlockSize = prfBlockSize;
  }

  static int checkVersion(int version) {
    if (version < 0 || version > 255) {
      throw new IllegalArgumentException("Version must be between 0 and 255");
    }
    return version;
  }

  public SecretKey getPremasterSecret() {
    return premasterSecret;
  }

  public int getMajorVersion() {
    return majorVersion;
  }

  public int getMinorVersion() {
    return minorVersion;
  }

  public byte[] getClientRandom() {
    return clientRandom.clone();
  }

  public byte[] getServerRandom() {
    return serverRandom.clone();
  }

  public byte[] getExtendedMasterSecretSessionHash() {
    return extendedMasterSecretSessionHash.clone();
  }

  public String getPRFHashAlg() {
    return prfHashAlg;
  }

  public int getPRFHashLength() {
    return prfHashLength;
  }

  public int getPRFBlockSize() {
    return prfBlockSize;
  }
}
