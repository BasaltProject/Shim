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
import javax.security.auth.Destroyable;

public class KerberosKey implements SecretKey, Destroyable {
  private static final long serialVersionUID = -4625402278148246993L;

  private KerberosPrincipal principal;
  private int versionNum;
  private EncryptionKey key;
  private transient boolean destroyed = false;

  public KerberosKey(KerberosPrincipal principal, byte[] keyBytes, int keyType, int versionNum) {
    this.principal = principal;
    this.versionNum = versionNum;
    key = new EncryptionKey(keyBytes, keyType);
  }

  public KerberosKey(KerberosPrincipal principal, char[] password, String algorithm) {
    this.principal = principal;
    this.versionNum = 0;
    String name = principal.getName();
    String salt = principal.getRealm() + name.substring(0, name.lastIndexOf('@')).replace("/", "");
    int etype = etypeOf(algorithm);
    key = new EncryptionKey(sun.security.krb5.Crypto.string2key(etype, password, salt,
        sun.security.krb5.Crypto.defaultString2KeyParams(etype)), etype);
  }

  private static int etypeOf(String algorithm) {
    if (algorithm == null || algorithm.equalsIgnoreCase("DES")) {
      return 3;
    } else if (algorithm.equalsIgnoreCase("DESede")) {
      return 16;
    } else if (algorithm.equalsIgnoreCase("AES128")) {
      return 17;
    } else if (algorithm.equalsIgnoreCase("AES256")) {
      return 18;
    } else if (algorithm.equalsIgnoreCase("ArcFourHmac")) {
      return 23;
    }
    for (int t : new int[] {1, 3, 16, 17, 18, 19, 20, 23}) {
      if (sun.security.krb5.Crypto.name(t).equalsIgnoreCase(algorithm)) {
        return t;
      }
    }
    throw new IllegalArgumentException("Algorithm " + algorithm + " not supported");
  }

  public final KerberosPrincipal getPrincipal() {
    if (destroyed) {
      throw new IllegalStateException("This key is no longer valid");
    }
    return principal;
  }

  public final int getVersionNumber() {
    if (destroyed) {
      throw new IllegalStateException("This key is no longer valid");
    }
    return versionNum;
  }

  public final int getKeyType() {
    if (destroyed) {
      throw new IllegalStateException("This key is no longer valid");
    }
    return key.getKeyType();
  }

  public final String getAlgorithm() {
    if (destroyed) {
      throw new IllegalStateException("This key is no longer valid");
    }
    return key.getAlgorithm();
  }

  public final String getFormat() {
    if (destroyed) {
      throw new IllegalStateException("This key is no longer valid");
    }
    return key.getFormat();
  }

  public final byte[] getEncoded() {
    if (destroyed) {
      throw new IllegalStateException("This key is no longer valid");
    }
    return key.getEncoded();
  }

  public void destroy() throws DestroyFailedException {
    if (!destroyed) {
      key.destroy();
      principal = null;
      destroyed = true;
    }
  }

  public boolean isDestroyed() {
    return destroyed;
  }

  public String toString() {
    if (destroyed) {
      return "Destroyed Principal";
    }
    return "Kerberos Principal " + principal.toString() + "Key Version " + versionNum + "key " + key.toString();
  }

  public int hashCode() {
    int result = 17;
    if (isDestroyed()) {
      return result;
    }
    result = 37 * result + Arrays.hashCode(getEncoded());
    result = 37 * result + getKeyType();
    if (principal != null) {
      result = 37 * result + principal.hashCode();
    }
    return result * 37 + versionNum;
  }

  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof KerberosKey)) {
      return false;
    }
    KerberosKey otherKey = (KerberosKey) other;
    if (isDestroyed() || otherKey.isDestroyed()) {
      return false;
    }
    if (versionNum != otherKey.getVersionNumber() || getKeyType() != otherKey.getKeyType()
        || !Arrays.equals(getEncoded(), otherKey.getEncoded())) {
      return false;
    }
    if (principal == null) {
      return otherKey.getPrincipal() == null;
    } else {
      return principal.equals(otherKey.getPrincipal());
    }
  }
}
