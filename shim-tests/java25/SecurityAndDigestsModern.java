/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

public class SecurityAndDigestsModern {

  private static String toHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      int val = b & 0xFF;
      if (val < 16) {
        sb.append('0');
      }
      sb.append(Integer.toHexString(val));
    }
    return sb.toString();
  }

  public static void main(String[] args) throws Exception {
    testMessageDigests();
    testByteBufferAndDigestIntoArray();
    testExceptions();
    testSecureRandom();
    testKeyAndPrincipalHierarchy();
    System.out.println("SecurityAndDigestsModern passed successfully");
  }

  private static void testMessageDigests() throws Exception {
    String message = "The quick brown fox jumps over the lazy dog";
    byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);

    MessageDigest md5 = MessageDigest.getInstance("MD5");
    System.out.println("md5 algorithm: " + md5.getAlgorithm());
    System.out.println("md5 length: " + md5.getDigestLength());
    System.out.println("md5 msg: " + toHex(md5.digest(msgBytes)));
    System.out.println("md5 empty: " + toHex(md5.digest(new byte[0])));

    MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
    System.out.println("sha1 algorithm: " + sha1.getAlgorithm());
    System.out.println("sha1 length: " + sha1.getDigestLength());
    System.out.println("sha1 msg: " + toHex(sha1.digest(msgBytes)));
    System.out.println("sha1 empty: " + toHex(sha1.digest(new byte[0])));

    MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
    System.out.println("sha256 algorithm: " + sha256.getAlgorithm());
    System.out.println("sha256 length: " + sha256.getDigestLength());
    System.out.println("sha256 msg: " + toHex(sha256.digest(msgBytes)));
    System.out.println("sha256 empty: " + toHex(sha256.digest(new byte[0])));

    byte[] d1 = sha256.digest(msgBytes);
    byte[] d2 = sha256.digest(msgBytes);
    System.out.println("isEqual same: " + MessageDigest.isEqual(d1, d2));
    System.out.println("isEqual nulls: " + MessageDigest.isEqual(null, null));
    System.out.println("isEqual diff: " + MessageDigest.isEqual(d1, new byte[32]));
  }

  private static void testByteBufferAndDigestIntoArray() throws Exception {
    String message = "The quick brown fox jumps over the lazy dog";
    byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);

    MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
    ByteBuffer buffer = ByteBuffer.wrap(msgBytes);
    sha256.update(buffer);
    byte[] fromBuf = sha256.digest();
    System.out.println("sha256 from buffer: " + toHex(fromBuf));

    byte[] outBuf = new byte[32];
    sha256.update(msgBytes);
    int written = sha256.digest(outBuf, 0, outBuf.length);
    System.out.println("sha256 written: " + written);
    System.out.println("sha256 outBuf: " + toHex(outBuf));
  }

  private static void testExceptions() throws Exception {
    try {
      MessageDigest.getInstance("NON_EXISTENT_DIGEST");
      System.out.println("Expected NoSuchAlgorithmException");
    } catch (NoSuchAlgorithmException e) {
      System.out.println("caught NoSuchAlgorithmException: true");
    }

    try {
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
      sha256.update((byte) 42);
      sha256.digest(new byte[10], 0, 10);
      System.out.println("Expected DigestException");
    } catch (DigestException e) {
      System.out.println("caught DigestException: true");
    }
  }

  private static void testSecureRandom() throws Exception {
    SecureRandom sr = new SecureRandom();
    System.out.println("sr algo not null: " + (sr.getAlgorithm() != null));

    byte[] bytes = new byte[32];
    sr.nextBytes(bytes);
    System.out.println("bytes length: " + bytes.length);

    byte[] seed = sr.generateSeed(16);
    System.out.println("generated seed length: " + seed.length);

    byte[] staticSeed = SecureRandom.getSeed(8);
    System.out.println("static seed length: " + staticSeed.length);

    SecureRandom srSha1 = SecureRandom.getInstance("SHA1PRNG");
    System.out.println("srSha1 algo: " + srSha1.getAlgorithm());

    SecureRandom srStrong = SecureRandom.getInstanceStrong();
    System.out.println("srStrong not null: " + (srStrong != null));

    int bounded = sr.nextInt(100);
    System.out.println("bounded in range: " + (bounded >= 0 && bounded < 100));
  }

  private static void testKeyAndPrincipalHierarchy() {
    Principal principal = new Principal() {
      @Override
      public String getName() {
        return "admin-user";
      }
    };
    System.out.println("principal name: " + principal.getName());

    PublicKey pubKey = new PublicKey() {
      @Override
      public String getAlgorithm() {
        return "RSA";
      }

      @Override
      public String getFormat() {
        return "X.509";
      }

      @Override
      public byte[] getEncoded() {
        return new byte[] {1, 2, 3, 4};
      }
    };

    PrivateKey privKey = new PrivateKey() {
      @Override
      public String getAlgorithm() {
        return "RSA";
      }

      @Override
      public String getFormat() {
        return "PKCS#8";
      }

      @Override
      public byte[] getEncoded() {
        return new byte[] {5, 6, 7, 8};
      }
    };

    System.out.println("pubKey algo: " + pubKey.getAlgorithm());
    System.out.println("pubKey format: " + pubKey.getFormat());
    System.out.println("pubKey encoded len: " + pubKey.getEncoded().length);
    System.out.println("privKey algo: " + privKey.getAlgorithm());
    System.out.println("privKey format: " + privKey.getFormat());
    System.out.println("privKey encoded len: " + privKey.getEncoded().length);
  }
}
