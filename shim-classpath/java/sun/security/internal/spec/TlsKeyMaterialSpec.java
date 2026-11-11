/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.internal.spec;

import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class TlsKeyMaterialSpec implements KeySpec, SecretKey {
  static final long serialVersionUID = 812912859129525028L;

  private final SecretKey clientMacKey;
  private final SecretKey serverMacKey;
  private final SecretKey clientCipherKey;
  private final SecretKey serverCipherKey;
  private final IvParameterSpec clientIv;
  private final IvParameterSpec serverIv;

  public TlsKeyMaterialSpec(SecretKey clientMacKey, SecretKey serverMacKey) {
    this(clientMacKey, serverMacKey, null, null, null, null);
  }

  public TlsKeyMaterialSpec(SecretKey clientMacKey, SecretKey serverMacKey, SecretKey clientCipherKey,
      SecretKey serverCipherKey) {
    this(clientMacKey, serverMacKey, clientCipherKey, null, serverCipherKey, null);
  }

  public TlsKeyMaterialSpec(SecretKey clientMacKey, SecretKey serverMacKey, SecretKey clientCipherKey,
      IvParameterSpec clientIv, SecretKey serverCipherKey, IvParameterSpec serverIv) {
    this.clientMacKey = clientMacKey;
    this.serverMacKey = serverMacKey;
    this.clientCipherKey = clientCipherKey;
    this.serverCipherKey = serverCipherKey;
    this.clientIv = clientIv;
    this.serverIv = serverIv;
  }

  public String getAlgorithm() {
    return "TlsKeyMaterial";
  }

  public String getFormat() {
    return null;
  }

  public byte[] getEncoded() {
    return null;
  }

  public SecretKey getClientMacKey() {
    return clientMacKey;
  }

  public SecretKey getServerMacKey() {
    return serverMacKey;
  }

  public SecretKey getClientCipherKey() {
    return clientCipherKey;
  }

  public IvParameterSpec getClientIv() {
    return clientIv;
  }

  public SecretKey getServerCipherKey() {
    return serverCipherKey;
  }

  public IvParameterSpec getServerIv() {
    return serverIv;
  }
}
