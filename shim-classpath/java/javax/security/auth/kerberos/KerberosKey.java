/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.kerberos;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
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
    String salt = principal.getRealm();
    String name = principal.getName();
    String user = name.substring(0, name.lastIndexOf('@'));
    salt = salt + user.replace("/", "");
    if (algorithm == null || algorithm.equalsIgnoreCase("ArcFourHmac") || algorithm.equalsIgnoreCase("RC4")) {
      key = new EncryptionKey(Md4.digest(new String(password).getBytes(StandardCharsets.UTF_16LE)), 23);
    } else if (algorithm.equalsIgnoreCase("AES128") || algorithm.equalsIgnoreCase("AES256")) {
      boolean big = algorithm.equalsIgnoreCase("AES256");
      key = new EncryptionKey(aesStringToKey(password, salt, big ? 32 : 16), big ? 18 : 17);
    } else {
      throw new IllegalArgumentException("Algorithm " + algorithm + " not supported");
    }
  }

  private static byte[] aesStringToKey(char[] password, String salt, int keyLength) {
    try {
      SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      byte[] tkey = f.generateSecret(new PBEKeySpec(password, salt.getBytes(StandardCharsets.UTF_8), 4096,
          keyLength * 8)).getEncoded();
      return derive(tkey, "kerberos".getBytes(StandardCharsets.US_ASCII), keyLength);
    } catch (GeneralSecurityException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static byte[] derive(byte[] key, byte[] constant, int keyLength) throws GeneralSecurityException {
    byte[] block = nFold(constant, 16);
    byte[] out = new byte[keyLength];
    int pos = 0;
    Cipher c = Cipher.getInstance("AES/ECB/NoPadding");
    c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
    while (pos < keyLength) {
      block = c.doFinal(block);
      int n = Math.min(block.length, keyLength - pos);
      System.arraycopy(block, 0, out, pos, n);
      pos += n;
    }
    return out;
  }

  private static byte[] nFold(byte[] in, int outLen) {
    int inLen = in.length;
    int lcm = lcm(inLen, outLen);
    byte[] rotated = new byte[lcm];
    for (int i = 0; i < lcm / inLen; i++) {
      byte[] r = rotateRight(in, 13 * i);
      System.arraycopy(r, 0, rotated, i * inLen, inLen);
    }
    byte[] sum = new byte[outLen];
    int c;
    for (int chunk = 0; chunk < lcm / outLen; chunk++) {
      c = 0;
      byte[] next = new byte[outLen];
      for (int i = outLen - 1; i >= 0; i--) {
        int v = (sum[i] & 0xff) + (rotated[chunk * outLen + i] & 0xff) + c;
        next[i] = (byte) v;
        c = v >> 8;
      }
      for (int i = outLen - 1; c != 0 && i >= 0; i--) {
        int v = (next[i] & 0xff) + c;
        next[i] = (byte) v;
        c = v >> 8;
      }
      sum = next;
    }
    return sum;
  }

  private static byte[] rotateRight(byte[] in, int bits) {
    int n = in.length * 8;
    bits %= n;
    byte[] out = new byte[in.length];
    for (int i = 0; i < n; i++) {
      int src = i;
      int dst = (i + bits) % n;
      int bit = (in[src / 8] >> (7 - src % 8)) & 1;
      if (bit != 0) {
        out[dst / 8] |= (byte) (1 << (7 - dst % 8));
      }
    }
    return out;
  }

  private static int lcm(int a, int b) {
    int x = a;
    int y = b;
    while (y != 0) {
      int t = x % y;
      x = y;
      y = t;
    }
    return a / x * b;
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

  private static final class Md4 {
    static byte[] digest(byte[] in) {
      int[] state = {0x67452301, 0xefcdab89, 0x98badcfe, 0x10325476};
      long bitLen = (long) in.length * 8;
      int padLen = (int) ((56 - (in.length + 1) % 64 + 64) % 64);
      byte[] msg = new byte[in.length + 1 + padLen + 8];
      System.arraycopy(in, 0, msg, 0, in.length);
      msg[in.length] = (byte) 0x80;
      for (int i = 0; i < 8; i++) {
        msg[msg.length - 8 + i] = (byte) (bitLen >>> (8 * i));
      }
      int[] x = new int[16];
      for (int off = 0; off < msg.length; off += 64) {
        for (int i = 0; i < 16; i++) {
          x[i] = (msg[off + 4 * i] & 0xff) | ((msg[off + 4 * i + 1] & 0xff) << 8)
              | ((msg[off + 4 * i + 2] & 0xff) << 16) | ((msg[off + 4 * i + 3] & 0xff) << 24);
        }
        int a = state[0];
        int b = state[1];
        int c = state[2];
        int d = state[3];
        int[] s1 = {3, 7, 11, 19};
        for (int i = 0; i < 16; i++) {
          int t = a + ((b & c) | (~b & d)) + x[i];
          t = Integer.rotateLeft(t, s1[i % 4]);
          a = d;
          d = c;
          c = b;
          b = t;
        }
        int[] s2 = {3, 5, 9, 13};
        int[] o2 = {0, 4, 8, 12, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15};
        for (int i = 0; i < 16; i++) {
          int t = a + ((b & c) | (b & d) | (c & d)) + x[o2[i]] + 0x5a827999;
          t = Integer.rotateLeft(t, s2[i % 4]);
          a = d;
          d = c;
          c = b;
          b = t;
        }
        int[] s3 = {3, 9, 11, 15};
        int[] o3 = {0, 8, 4, 12, 2, 10, 6, 14, 1, 9, 5, 13, 3, 11, 7, 15};
        for (int i = 0; i < 16; i++) {
          int t = a + (b ^ c ^ d) + x[o3[i]] + 0x6ed9eba1;
          t = Integer.rotateLeft(t, s3[i % 4]);
          a = d;
          d = c;
          c = b;
          b = t;
        }
        state[0] += a;
        state[1] += b;
        state[2] += c;
        state[3] += d;
      }
      byte[] out = new byte[16];
      for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
          out[4 * i + j] = (byte) (state[i] >>> (8 * j));
        }
      }
      return out;
    }
  }
}
