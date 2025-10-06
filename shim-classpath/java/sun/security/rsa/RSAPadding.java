/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.rsa;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

public final class RSAPadding {
  public static final int PAD_BLOCKTYPE_1 = 1;
  public static final int PAD_BLOCKTYPE_2 = 2;
  public static final int PAD_NONE = 3;
  public static final int PAD_OAEP_MGF1 = 4;

  private final int type;
  private final int paddedSize;
  private final SecureRandom random;
  private final int maxDataSize;
  private MessageDigest md;
  private MessageDigest mgfMd;
  private byte[] lHash;

  private RSAPadding(int type, int paddedSize, SecureRandom random, OAEPParameterSpec spec)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    this.type = type;
    this.paddedSize = paddedSize;
    this.random = random;
    if (paddedSize < 64) {
      throw new InvalidKeyException("Padded size must be at least 64");
    }
    switch (type) {
    case PAD_BLOCKTYPE_1:
    case PAD_BLOCKTYPE_2:
      maxDataSize = paddedSize - 11;
      break;
    case PAD_NONE:
      maxDataSize = paddedSize;
      break;
    case PAD_OAEP_MGF1:
      String mdName = "SHA-1";
      String mgfMdName = mdName;
      byte[] digestInput = null;
      try {
        if (spec != null) {
          mdName = spec.getDigestAlgorithm();
          String mgfName = spec.getMGFAlgorithm();
          if (!mgfName.equalsIgnoreCase("MGF1")) {
            throw new InvalidAlgorithmParameterException("Unsupported MGF algo: " + mgfName);
          }
          mgfMdName = ((MGF1ParameterSpec) spec.getMGFParameters()).getDigestAlgorithm();
          PSource pSrc = spec.getPSource();
          String pSrcAlgo = pSrc.getAlgorithm();
          if (!pSrcAlgo.equalsIgnoreCase("PSpecified")) {
            throw new InvalidAlgorithmParameterException("Unsupported pSource algo: " + pSrcAlgo);
          }
          digestInput = ((PSource.PSpecified) pSrc).getValue();
        }
        md = MessageDigest.getInstance(mdName);
        mgfMd = MessageDigest.getInstance(mgfMdName);
      } catch (NoSuchAlgorithmException e) {
        throw new InvalidKeyException("Digest " + mdName + " not available", e);
      }
      lHash = getInitialHash(md, digestInput);
      int digestLen = lHash.length;
      maxDataSize = paddedSize - 2 - 2 * digestLen;
      if (maxDataSize <= 0) {
        throw new InvalidKeyException("Key is too short for encryption using OAEPPadding with " + mdName
            + " and MGF1" + mgfMdName);
      }
      break;
    default:
      throw new InvalidKeyException("Invalid padding: " + type);
    }
    if (type != PAD_NONE && maxDataSize <= 0) {
      throw new InvalidKeyException("Padded size too small");
    }
  }

  private static byte[] getInitialHash(MessageDigest md, byte[] digestInput) {
    byte[] result;
    if (digestInput == null || digestInput.length == 0) {
      result = md.digest();
    } else {
      result = md.digest(digestInput);
    }
    return result;
  }

  public static RSAPadding getInstance(int type, int paddedSize, SecureRandom random) throws InvalidKeyException,
      InvalidAlgorithmParameterException {
    return new RSAPadding(type, paddedSize, random, null);
  }

  public static RSAPadding getInstance(int type, int paddedSize, SecureRandom random, OAEPParameterSpec spec)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    return new RSAPadding(type, paddedSize, random, spec);
  }

  public int getMaxDataSize() {
    return maxDataSize;
  }

  public byte[] pad(byte[] data) throws BadPaddingException {
    return pad(data, 0, data.length);
  }

  public byte[] pad(byte[] data, int ofs, int len) throws BadPaddingException {
    if (len > maxDataSize) {
      throw new BadPaddingException("Data must be shorter than " + (maxDataSize + 1) + " bytes but received "
          + len + " bytes.");
    }
    switch (type) {
    case PAD_NONE:
      return RSACore.convert(data, ofs, len);
    case PAD_BLOCKTYPE_1:
    case PAD_BLOCKTYPE_2:
      return padV15(data, ofs, len);
    case PAD_OAEP_MGF1:
      return padOaep(data, ofs, len);
    default:
      throw new AssertionError();
    }
  }

  private byte[] padV15(byte[] data, int ofs, int len) throws BadPaddingException {
    byte[] padded = new byte[paddedSize];
    System.arraycopy(data, ofs, padded, paddedSize - len, len);
    int psSize = paddedSize - 3 - len;
    int k = 0;
    padded[k++] = 0;
    padded[k++] = (byte) type;
    if (type == PAD_BLOCKTYPE_1) {
      while (psSize-- > 0) {
        padded[k++] = (byte) 0xff;
      }
    } else {
      SecureRandom r = random == null ? new SecureRandom() : random;
      byte[] tmp = new byte[16];
      int i = 0;
      while (psSize-- > 0) {
        int b;
        do {
          if (i == 0) {
            r.nextBytes(tmp);
            i = tmp.length;
          }
          b = tmp[--i] & 0xff;
        } while (b == 0);
        padded[k++] = (byte) b;
      }
    }
    return padded;
  }

  private byte[] padOaep(byte[] m, int mOfs, int mLen) {
    SecureRandom r = random == null ? new SecureRandom() : random;
    int hLen = lHash.length;
    byte[] em = new byte[paddedSize];
    byte[] seed = new byte[hLen];
    r.nextBytes(seed);
    System.arraycopy(seed, 0, em, 1, hLen);
    int dbOff = 1 + hLen;
    int dbLen = paddedSize - dbOff;
    byte[] db = new byte[dbLen];
    System.arraycopy(lHash, 0, db, 0, hLen);
    db[dbLen - mLen - 1] = 1;
    System.arraycopy(m, mOfs, db, dbLen - mLen, mLen);
    byte[] dbMask = mgf1(seed, dbLen);
    for (int i = 0; i < dbLen; i++) {
      em[dbOff + i] = (byte) (db[i] ^ dbMask[i]);
    }
    byte[] seedMask = mgf1(java.util.Arrays.copyOfRange(em, dbOff, paddedSize), hLen);
    for (int i = 0; i < hLen; i++) {
      em[1 + i] ^= seedMask[i];
    }
    return em;
  }

  private byte[] mgf1(byte[] seed, int len) {
    byte[] out = new byte[len];
    int hLen = mgfMd.getDigestLength();
    int produced = 0;
    int counter = 0;
    while (produced < len) {
      mgfMd.update(seed);
      mgfMd.update(new byte[] {(byte) (counter >>> 24), (byte) (counter >>> 16), (byte) (counter >>> 8),
        (byte) counter});
      byte[] h = mgfMd.digest();
      int n = Math.min(hLen, len - produced);
      System.arraycopy(h, 0, out, produced, n);
      produced += n;
      counter++;
    }
    return out;
  }

  public byte[] unpad(byte[] padded) throws BadPaddingException {
    if (padded.length != paddedSize) {
      throw new BadPaddingException("Decryption error");
    }
    switch (type) {
    case PAD_NONE:
      return padded;
    case PAD_BLOCKTYPE_1:
    case PAD_BLOCKTYPE_2:
      return unpadV15(padded);
    case PAD_OAEP_MGF1:
      return unpadOaep(padded);
    default:
      throw new AssertionError();
    }
  }

  private byte[] unpadV15(byte[] padded) throws BadPaddingException {
    int k = 0;
    boolean bp = false;
    if (padded[k++] != 0) {
      bp = true;
    }
    if (padded[k++] != type) {
      bp = true;
    }
    int p = 0;
    while (k < padded.length) {
      int b = padded[k++] & 0xff;
      if ((b == 0) && (p == 0)) {
        p = k;
      }
      if ((k == padded.length) && (p == 0)) {
        bp = true;
      }
      if ((type == PAD_BLOCKTYPE_1) && (b != 0xff) && (p == 0)) {
        bp = true;
      }
    }
    int n = padded.length - p;
    if (n > maxDataSize) {
      bp = true;
    }
    if (p < 11) {
      bp = true;
    }
    if (bp) {
      throw new BadPaddingException("Padding error in decryption");
    }
    byte[] data = new byte[n];
    System.arraycopy(padded, p, data, 0, n);
    return data;
  }

  private byte[] unpadOaep(byte[] padded) throws BadPaddingException {
    int hLen = lHash.length;
    boolean bp = false;
    if (padded[0] != 0) {
      bp = true;
    }
    int dbOff = 1 + hLen;
    byte[] maskedSeed = java.util.Arrays.copyOfRange(padded, 1, dbOff);
    byte[] maskedDb = java.util.Arrays.copyOfRange(padded, dbOff, padded.length);
    byte[] seedMask = mgf1(maskedDb, hLen);
    byte[] seed = new byte[hLen];
    for (int i = 0; i < hLen; i++) {
      seed[i] = (byte) (maskedSeed[i] ^ seedMask[i]);
    }
    byte[] dbMask = mgf1(seed, maskedDb.length);
    byte[] db = new byte[maskedDb.length];
    for (int i = 0; i < db.length; i++) {
      db[i] = (byte) (maskedDb[i] ^ dbMask[i]);
    }
    int diff = 0;
    for (int i = 0; i < hLen; i++) {
      diff |= db[i] ^ lHash[i];
    }
    if (diff != 0) {
      bp = true;
    }
    int p = -1;
    for (int i = hLen; i < db.length; i++) {
      if (db[i] == 1) {
        p = i + 1;
        break;
      }
      if (db[i] != 0) {
        bp = true;
        break;
      }
    }
    if (p < 0) {
      bp = true;
    }
    if (bp) {
      throw new BadPaddingException("Padding error in decryption");
    }
    return java.util.Arrays.copyOfRange(db, p, db.length);
  }
}
