/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.kerberos;

import java.util.Arrays;
import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;

public final class EncryptionKey implements SecretKey {
  private static final long serialVersionUID = 9L;

  private int keyType;
  private byte[] keyBytes;
  private transient boolean destroyed = false;

  public EncryptionKey(byte[] keyBytes, int keyType) {
    this.keyBytes = keyBytes.clone();
    this.keyType = keyType;
  }

  public final int getKeyType() {
    if (destroyed) {
      throw new IllegalStateException("This key is no longer valid");
    }
    return keyType;
  }

  public final String getAlgorithm() {
    return getAlgorithmName(getKeyType());
  }

  static String getAlgorithmName(int eType) {
    switch (eType) {
      case 1:
        return "des-cbc-crc";
      case 2:
        return "des-cbc-md4";
      case 3:
        return "des-cbc-md5";
      case 16:
        return "des3-cbc-sha1-kd";
      case 17:
        return "aes128-cts-hmac-sha1-96";
      case 18:
        return "aes256-cts-hmac-sha1-96";
      case 23:
        return "rc4-hmac";
      case 0:
        return "NULL";
      default:
        throw new IllegalArgumentException("Unsupported encryption type: " + eType);
    }
  }

  public final String getFormat() {
    if (destroyed) {
      throw new IllegalStateException("This key is no longer valid");
    }
    return "RAW";
  }

  public final byte[] getEncoded() {
    if (destroyed) {
      throw new IllegalStateException("This key is no longer valid");
    }
    return keyBytes.clone();
  }

  public void destroy() throws DestroyFailedException {
    if (!destroyed) {
      destroyed = true;
      Arrays.fill(keyBytes, (byte) 0);
    }
  }

  public boolean isDestroyed() {
    return destroyed;
  }

  public String toString() {
    if (destroyed) {
      return "Destroyed EncryptionKey";
    }
    return "EncryptionKey: keyType=" + keyType + " keyBytes (hex dump)=" + hexDump(keyBytes);
  }

  static String hexDump(byte[] b) {
    StringBuilder sb = new StringBuilder("\n");
    for (int i = 0; i < b.length; i++) {
      if (i > 0 && i % 8 == 0) {
        sb.append('\n');
      }
      sb.append(String.format("%02X ", b[i] & 0xff));
    }
    return sb.toString();
  }

  public int hashCode() {
    int result = 17;
    if (isDestroyed()) {
      return result;
    }
    result = 37 * result + Arrays.hashCode(keyBytes);
    return 37 * result + keyType;
  }

  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof EncryptionKey)) {
      return false;
    }
    EncryptionKey otherKey = (EncryptionKey) other;
    if (isDestroyed() || otherKey.isDestroyed()) {
      return false;
    }
    if (keyType != otherKey.getKeyType() || !Arrays.equals(keyBytes, otherKey.getEncoded())) {
      return false;
    }
    return true;
  }
}
