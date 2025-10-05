/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.Locale;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;

final class BlockCipherCore {
  static final int ECB_MODE = 0;
  static final int CBC_MODE = 1;
  static final int CFB_MODE = 2;
  static final int OFB_MODE = 3;
  static final int PCBC_MODE = 4;
  static final int CTR_MODE = 5;

  private static final int PAD_NONE = 0;
  private static final int PAD_PKCS5 = 1;
  private static final int PAD_ISO10126 = 2;

  private final SymmetricCipher raw;
  private final String algorithm;
  private final int blockSize;
  private int cipherMode = ECB_MODE;
  private int unitBytes;
  private int padding = PAD_PKCS5;
  private boolean decrypting;
  private boolean initialized;

  private byte[] iv;
  private byte[] register;
  private byte[] prevXor;
  private byte[] keystream;
  private int keystreamUsed;
  private byte[] counter;
  private final byte[] buffer;
  private int buffered;
  private long processed;
  private final byte[] scratch;

  BlockCipherCore(SymmetricCipher raw, String algorithm) {
    this.raw = raw;
    this.algorithm = algorithm;
    this.blockSize = raw.getBlockSize();
    this.unitBytes = blockSize;
    this.buffer = new byte[blockSize * 2];
    this.scratch = new byte[blockSize];
  }

  void setMode(String mode) throws NoSuchAlgorithmException {
    if (mode == null) {
      throw new NoSuchAlgorithmException("null mode");
    }
    String modeUpperCase = mode.toUpperCase(Locale.ENGLISH);
    if (modeUpperCase.equals("ECB")) {
      cipherMode = ECB_MODE;
    } else if (modeUpperCase.equals("CBC")) {
      cipherMode = CBC_MODE;
    } else if (modeUpperCase.equals("PCBC")) {
      cipherMode = PCBC_MODE;
    } else if (modeUpperCase.equals("CTR")) {
      cipherMode = CTR_MODE;
      unitBytes = 1;
      padding = PAD_NONE;
    } else if (modeUpperCase.startsWith("CFB")) {
      cipherMode = CFB_MODE;
      unitBytes = unitFor(modeUpperCase, mode, 3);
    } else if (modeUpperCase.startsWith("OFB")) {
      cipherMode = OFB_MODE;
      unitBytes = unitFor(modeUpperCase, mode, 3);
    } else {
      throw new NoSuchAlgorithmException("Cipher mode: " + mode + " not found");
    }
  }

  private int unitFor(String modeUpperCase, String mode, int prefix) throws NoSuchAlgorithmException {
    if (modeUpperCase.length() == prefix) {
      return blockSize;
    }
    int numBits;
    try {
      numBits = Integer.parseInt(modeUpperCase.substring(prefix));
      if (numBits == 0 || (numBits % 8) != 0 || (numBits / 8) > blockSize) {
        throw new NoSuchAlgorithmException("Invalid algorithm mode: " + mode);
      }
    } catch (NumberFormatException e) {
      throw new NoSuchAlgorithmException("Algorithm mode: " + mode + " not implemented");
    }
    return numBits / 8;
  }

  void setPadding(String paddingScheme) throws NoSuchPaddingException {
    if (paddingScheme == null) {
      throw new NoSuchPaddingException("null padding");
    }
    if (paddingScheme.equalsIgnoreCase("NoPadding")) {
      padding = PAD_NONE;
    } else if (paddingScheme.equalsIgnoreCase("ISO10126Padding")) {
      padding = PAD_ISO10126;
    } else if (paddingScheme.equalsIgnoreCase("PKCS5Padding")) {
      padding = PAD_PKCS5;
    } else {
      padding = PAD_NONE;
      throw new NoSuchPaddingException("Padding: " + paddingScheme + " not implemented");
    }
    if (padding != PAD_NONE && cipherMode == CTR_MODE) {
      padding = PAD_NONE;
      throw new NoSuchPaddingException("CTR mode must be used with NoPadding");
    }
  }

  int getBlockSize() {
    return blockSize;
  }

  byte[] getIV() {
    return iv == null ? null : iv.clone();
  }

  AlgorithmParameters getParameters(String algName) {
    if (cipherMode == ECB_MODE) {
      return null;
    }
    byte[] useIv = iv;
    if (useIv == null) {
      useIv = new byte[blockSize];
      new SecureRandom().nextBytes(useIv);
    }
    try {
      AlgorithmParameters params = AlgorithmParameters.getInstance(algName, "SunJCE");
      params.init(new IvParameterSpec(useIv));
      return params;
    } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidParameterSpecException e) {
      throw new RuntimeException("Cannot find " + algName + " AlgorithmParameters implementation in SunJCE provider");
    }
  }

  int getOutputSize(int inputLen) {
    int totalLen = buffered + inputLen;
    if (padding != PAD_NONE && !decrypting) {
      totalLen += blockSize - (int) ((processed + totalLen) % blockSize);
    }
    return totalLen;
  }

  void init(int opmode, byte[] keyBytes, String keyAlgorithm, AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException {
    decrypting = (opmode == Cipher.DECRYPT_MODE) || (opmode == Cipher.UNWRAP_MODE);
    byte[] ivBytes = null;
    if (params != null) {
      if (cipherMode == ECB_MODE) {
        throw new InvalidAlgorithmParameterException("ECB mode cannot use IV");
      }
      if (params instanceof IvParameterSpec) {
        ivBytes = ((IvParameterSpec) params).getIV();
        if (ivBytes == null || ivBytes.length != blockSize) {
          throw new InvalidAlgorithmParameterException("Wrong IV length: must be " + blockSize + " bytes long");
        }
      } else {
        throw new InvalidAlgorithmParameterException("Unsupported parameter: " + params);
      }
    }
    if (cipherMode == ECB_MODE) {
      ivBytes = null;
    } else if (ivBytes == null) {
      if (decrypting) {
        throw new InvalidAlgorithmParameterException("Parameters missing");
      }
      ivBytes = new byte[blockSize];
      (random == null ? new SecureRandom() : random).nextBytes(ivBytes);
    }
    raw.init(decrypting, keyAlgorithm, keyBytes);
    this.iv = ivBytes;
    resetState();
    initialized = true;
  }

  private void resetState() {
    buffered = 0;
    processed = 0;
    register = null;
    prevXor = null;
    keystream = null;
    counter = null;
    keystreamUsed = 0;
    if (iv != null) {
      register = iv.clone();
      if (cipherMode == PCBC_MODE) {
        prevXor = iv.clone();
      }
      if (cipherMode == CTR_MODE) {
        counter = iv.clone();
        keystream = new byte[blockSize];
        keystreamUsed = blockSize;
      }
    }
  }

  private void checkInitialized() {
    if (!initialized) {
      throw new IllegalStateException("Cipher not initialized");
    }
  }

  private int process(byte[] input, int inOff, int len, byte[] output, int outOff) {
    return process(input, inOff, len, output, outOff, false);
  }

  private int process(byte[] input, int inOff, int len, byte[] output, int outOff, boolean last) {
    if (len == 0) {
      return 0;
    }
    byte[] in = new byte[len];
    System.arraycopy(input, inOff, in, 0, len);
    switch (cipherMode) {
    case ECB_MODE:
      for (int i = 0; i < len; i += blockSize) {
        if (decrypting) {
          raw.decryptBlock(in, i, output, outOff + i);
        } else {
          raw.encryptBlock(in, i, output, outOff + i);
        }
      }
      break;
    case CBC_MODE:
      for (int i = 0; i < len; i += blockSize) {
        if (decrypting) {
          raw.decryptBlock(in, i, scratch, 0);
          for (int j = 0; j < blockSize; j++) {
            output[outOff + i + j] = (byte) (scratch[j] ^ register[j]);
          }
          System.arraycopy(in, i, register, 0, blockSize);
        } else {
          for (int j = 0; j < blockSize; j++) {
            scratch[j] = (byte) (in[i + j] ^ register[j]);
          }
          raw.encryptBlock(scratch, 0, output, outOff + i);
          System.arraycopy(output, outOff + i, register, 0, blockSize);
        }
      }
      break;
    case PCBC_MODE:
      for (int i = 0; i < len; i += blockSize) {
        if (decrypting) {
          raw.decryptBlock(in, i, scratch, 0);
          for (int j = 0; j < blockSize; j++) {
            output[outOff + i + j] = (byte) (scratch[j] ^ prevXor[j]);
          }
          for (int j = 0; j < blockSize; j++) {
            prevXor[j] = (byte) (output[outOff + i + j] ^ in[i + j]);
          }
        } else {
          for (int j = 0; j < blockSize; j++) {
            scratch[j] = (byte) (in[i + j] ^ prevXor[j]);
          }
          raw.encryptBlock(scratch, 0, output, outOff + i);
          for (int j = 0; j < blockSize; j++) {
            prevXor[j] = (byte) (in[i + j] ^ output[outOff + i + j]);
          }
        }
      }
      break;
    case CFB_MODE:
      for (int i = 0; i < len; i += unitBytes) {
        int n = Math.min(unitBytes, len - i);
        raw.encryptBlock(register, 0, scratch, 0);
        if (n < unitBytes) {
          for (int j = 0; j < n; j++) {
            output[outOff + i + j] = (byte) (in[i + j] ^ scratch[j]);
          }
          break;
        }
        System.arraycopy(register, unitBytes, register, 0, blockSize - unitBytes);
        for (int j = 0; j < unitBytes; j++) {
          byte c = (byte) (in[i + j] ^ scratch[j]);
          output[outOff + i + j] = c;
          register[blockSize - unitBytes + j] = decrypting ? in[i + j] : c;
        }
      }
      break;
    case OFB_MODE:
      for (int i = 0; i < len; i += unitBytes) {
        int n = Math.min(unitBytes, len - i);
        raw.encryptBlock(register, 0, scratch, 0);
        if (n < unitBytes) {
          for (int j = 0; j < n; j++) {
            output[outOff + i + j] = (byte) (in[i + j] ^ scratch[j]);
          }
          break;
        }
        for (int j = 0; j < unitBytes; j++) {
          output[outOff + i + j] = (byte) (in[i + j] ^ scratch[j]);
        }
        if (unitBytes == blockSize) {
          System.arraycopy(scratch, 0, register, 0, blockSize);
        } else {
          System.arraycopy(register, unitBytes, register, 0, blockSize - unitBytes);
          System.arraycopy(scratch, 0, register, blockSize - unitBytes, unitBytes);
        }
      }
      break;
    case CTR_MODE:
      for (int i = 0; i < len; i++) {
        if (keystreamUsed >= blockSize) {
          raw.encryptBlock(counter, 0, keystream, 0);
          for (int j = blockSize - 1; j >= 0; j--) {
            if (++counter[j] != 0) {
              break;
            }
          }
          keystreamUsed = 0;
        }
        output[outOff + i] = (byte) (in[i] ^ keystream[keystreamUsed++]);
      }
      break;
    default:
      throw new ProviderException("Unknown mode");
    }
    return len;
  }

  byte[] update(byte[] input, int inputOffset, int inputLen) {
    byte[] output = new byte[getOutputSize(inputLen)];
    try {
      int len = update(input, inputOffset, inputLen, output, 0);
      if (len == output.length) {
        return output;
      }
      byte[] copy = java.util.Arrays.copyOf(output, len);
      if (decrypting) {
        java.util.Arrays.fill(output, (byte) 0);
      }
      return copy;
    } catch (ShortBufferException e) {
      throw new ProviderException("Unexpected exception", e);
    }
  }

  int update(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws ShortBufferException {
    checkInitialized();
    int len = buffered + inputLen;
    if (padding != PAD_NONE && decrypting) {
      len -= blockSize;
    }
    len = len > 0 ? len - (len % unitBytes) : 0;
    if (output == null || (output.length - outputOffset) < len) {
      throw new ShortBufferException("Output buffer must be (at least) " + len + " bytes long");
    }
    int outLen = 0;
    if (len != 0) {
      byte[] in = new byte[len];
      int inputConsumed = len - buffered;
      int bufferedConsumed = buffered;
      if (inputConsumed < 0) {
        inputConsumed = 0;
        bufferedConsumed = len;
      }
      if (buffered != 0) {
        System.arraycopy(buffer, 0, in, 0, bufferedConsumed);
      }
      if (inputConsumed > 0) {
        System.arraycopy(input, inputOffset, in, bufferedConsumed, inputConsumed);
      }
      outLen = process(in, 0, len, output, outputOffset);
      processed += len;
      inputLen -= inputConsumed;
      inputOffset += inputConsumed;
      buffered -= bufferedConsumed;
      if (buffered > 0) {
        System.arraycopy(buffer, bufferedConsumed, buffer, 0, buffered);
      }
    }
    if (inputLen > 0) {
      System.arraycopy(input, inputOffset, buffer, buffered, inputLen);
    }
    buffered += inputLen;
    return outLen;
  }

  byte[] doFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException,
      BadPaddingException {
    try {
      byte[] output = new byte[getOutputSize(inputLen)];
      int outLen = doFinal(input, inputOffset, inputLen, output, 0);
      if (outLen == output.length) {
        return output;
      }
      byte[] copy = java.util.Arrays.copyOf(output, outLen);
      if (decrypting) {
        java.util.Arrays.fill(output, (byte) 0);
      }
      return copy;
    } catch (ShortBufferException e) {
      throw new ProviderException("Unexpected exception", e);
    }
  }

  int doFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws IllegalBlockSizeException, ShortBufferException, BadPaddingException {
    checkInitialized();
    int estOutSize = getOutputSize(inputLen);
    boolean checkLater = decrypting && padding != PAD_NONE;
    if (!checkLater && (output == null || (output.length - outputOffset) < estOutSize)) {
      throw new ShortBufferException("Output buffer must be (at least) " + estOutSize + " bytes long");
    }
    int total = buffered + inputLen;
    byte[] all = new byte[total];
    System.arraycopy(buffer, 0, all, 0, buffered);
    if (inputLen > 0) {
      System.arraycopy(input, inputOffset, all, buffered, inputLen);
    }
    int outLen;
    if (!decrypting) {
      int padLen = 0;
      if (padding != PAD_NONE) {
        padLen = blockSize - (int) ((processed + total) % blockSize);
        byte[] padded = new byte[total + padLen];
        System.arraycopy(all, 0, padded, 0, total);
        if (padding == PAD_PKCS5) {
          for (int i = total; i < padded.length; i++) {
            padded[i] = (byte) padLen;
          }
        } else {
          byte[] rnd = new byte[padLen];
          new SecureRandom().nextBytes(rnd);
          System.arraycopy(rnd, 0, padded, total, padLen);
          padded[padded.length - 1] = (byte) padLen;
        }
        all = padded;
      } else if (total % unitBytes != 0 && cipherMode != CFB_MODE && cipherMode != OFB_MODE) {
        resetStateKeepIv();
        throw new IllegalBlockSizeException("Input length not multiple of " + unitBytes + " bytes");
      }
      outLen = process(all, 0, all.length, output, outputOffset, true);
    } else if (padding != PAD_NONE) {
      if ((processed + total) % blockSize != 0) {
        resetStateKeepIv();
        throw new IllegalBlockSizeException("Input length must be multiple of " + blockSize
            + " when decrypting with padded cipher");
      }
      byte[] plain = new byte[total];
      process(all, 0, total, plain, 0);
      int newLen = 0;
      boolean ok = total != 0;
      if (total == 0) {
        newLen = 0;
        ok = true;
      } else {
        int padValue = plain[total - 1] & 0xff;
        if (padValue < 1 || padValue > blockSize) {
          ok = false;
        } else {
          int start = total - padValue;
          if (start < 0) {
            ok = false;
          } else if (padding == PAD_PKCS5) {
            for (int i = 0; i < padValue; i++) {
              if (plain[start + i] != (byte) padValue) {
                ok = false;
              }
            }
          }
          newLen = start;
        }
      }
      if (!ok) {
        java.util.Arrays.fill(plain, (byte) 0);
        resetStateKeepIv();
        throw new BadPaddingException(
            "Given final block not properly padded. Such issues can arise if a bad key is used during decryption.");
      }
      if (output == null || (output.length - outputOffset) < newLen) {
        resetStateKeepIv();
        throw new ShortBufferException("Output buffer too short: " + (output == null ? 0 : output.length
            - outputOffset) + " bytes given, " + newLen + " bytes needed");
      }
      System.arraycopy(plain, 0, output, outputOffset, newLen);
      java.util.Arrays.fill(plain, (byte) 0);
      outLen = newLen;
    } else {
      if (total % unitBytes != 0 && cipherMode != CFB_MODE && cipherMode != OFB_MODE) {
        resetStateKeepIv();
        throw new IllegalBlockSizeException("Input length not multiple of " + unitBytes + " bytes");
      }
      outLen = process(all, 0, total, output, outputOffset, true);
    }
    resetStateKeepIv();
    return outLen;
  }

  private void resetStateKeepIv() {
    resetState();
  }

  byte[] wrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
    byte[] result = null;
    try {
      byte[] encodedKey = key.getEncoded();
      if ((encodedKey == null) || (encodedKey.length == 0)) {
        throw new InvalidKeyException("Cannot get an encoding of the key to be wrapped");
      }
      result = doFinal(encodedKey, 0, encodedKey.length);
    } catch (BadPaddingException e) {
      // cannot happen when encrypting
    }
    return result;
  }

  Key unwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType) throws InvalidKeyException,
      NoSuchAlgorithmException {
    byte[] encodedKey;
    try {
      encodedKey = doFinal(wrappedKey, 0, wrappedKey.length);
    } catch (BadPaddingException ePadding) {
      throw new InvalidKeyException("The wrapped key is not padded correctly");
    } catch (IllegalBlockSizeException eBlockSize) {
      throw new InvalidKeyException("The wrapped key does not have the correct length");
    }
    return ConstructKeys.constructKey(encodedKey, wrappedKeyAlgorithm, wrappedKeyType);
  }
}
