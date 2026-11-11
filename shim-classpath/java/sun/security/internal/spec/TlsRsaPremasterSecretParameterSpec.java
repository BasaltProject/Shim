/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.internal.spec;

import java.security.spec.AlgorithmParameterSpec;

public class TlsRsaPremasterSecretParameterSpec implements AlgorithmParameterSpec {
  private final byte[] encodedSecret;
  private final int clientVersion;
  private final int serverVersion;

  public TlsRsaPremasterSecretParameterSpec(int clientVersion, int serverVersion) {
    this(clientVersion, serverVersion, null);
  }

  public TlsRsaPremasterSecretParameterSpec(int clientVersion, int serverVersion, byte[] encodedSecret) {
    this.clientVersion = checkVersion(clientVersion);
    this.serverVersion = checkVersion(serverVersion);
    if (encodedSecret != null && encodedSecret.length != 48) {
      throw new IllegalArgumentException("Encoded secret is not exactly 48 bytes");
    }
    this.encodedSecret = encodedSecret == null ? null : encodedSecret.clone();
  }

  public int getClientVersion() {
    return clientVersion;
  }

  public int getServerVersion() {
    return serverVersion;
  }

  public int getMajorVersion() {
    return clientVersion >>> 8 & 0xFF;
  }

  public int getMinorVersion() {
    return clientVersion & 0xFF;
  }

  private int checkVersion(int version) {
    if (version < 0 || version > 0xFFFF) {
      throw new IllegalArgumentException("Version must be between 0 and 65,535");
    }
    return version;
  }

  public byte[] getEncodedSecret() {
    return encodedSecret == null ? null : encodedSecret.clone();
  }
}
