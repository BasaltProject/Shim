/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

public abstract class CipherSpi {
  public CipherSpi() {
  }

  protected abstract void engineSetMode(String mode) throws NoSuchAlgorithmException;

  protected abstract void engineSetPadding(String padding) throws NoSuchPaddingException;

  protected abstract int engineGetBlockSize();

  protected abstract int engineGetOutputSize(int inputLen);

  protected abstract byte[] engineGetIV();

  protected abstract AlgorithmParameters engineGetParameters();

  protected abstract void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException;

  protected abstract void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException;

  protected abstract void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random)
      throws InvalidKeyException, InvalidAlgorithmParameterException;

  protected abstract byte[] engineUpdate(byte[] input, int inputOffset, int inputLen);

  protected abstract int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws ShortBufferException;

  protected int engineUpdate(ByteBuffer input, ByteBuffer output) throws ShortBufferException {
    try {
      return bufferCrypt(input, output, true);
    } catch (IllegalBlockSizeException e) {
      throw new ProviderException("Internal error in update()");
    } catch (BadPaddingException e) {
      throw new ProviderException("Internal error in update()");
    }
  }

  protected abstract byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen)
      throws IllegalBlockSizeException, BadPaddingException;

  protected abstract int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset)
      throws ShortBufferException, IllegalBlockSizeException, BadPaddingException;

  protected int engineDoFinal(ByteBuffer input, ByteBuffer output)
      throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
    return bufferCrypt(input, output, false);
  }

  protected byte[] engineWrap(Key key) throws IllegalBlockSizeException, InvalidKeyException {
    throw new UnsupportedOperationException();
  }

  protected Key engineUnwrap(byte[] wrappedKey, String wrappedKeyAlgorithm, int wrappedKeyType)
      throws InvalidKeyException, NoSuchAlgorithmException {
    throw new UnsupportedOperationException();
  }

  protected int engineGetKeySize(Key key) throws InvalidKeyException {
    throw new UnsupportedOperationException();
  }

  protected void engineUpdateAAD(byte[] src, int offset, int len) {
    throw new UnsupportedOperationException("The underlying Cipher implementation does not support this method");
  }

  protected void engineUpdateAAD(ByteBuffer src) {
    throw new UnsupportedOperationException("The underlying Cipher implementation does not support this method");
  }

  private int bufferCrypt(ByteBuffer input, ByteBuffer output, boolean isUpdate)
      throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
    if ((input == null) || (output == null)) {
      throw new NullPointerException("Input and output buffers must not be null");
    }
    int inPos = input.position();
    int inLimit = input.limit();
    int inLen = inLimit - inPos;
    if (isUpdate && (inLen == 0)) {
      return 0;
    }
    int outLenNeeded = engineGetOutputSize(inLen);
    if (output.remaining() < outLenNeeded) {
      throw new ShortBufferException("Need at least " + outLenNeeded + " bytes of space in output buffer");
    }
    byte[] inArray = new byte[inLen];
    input.get(inArray);
    byte[] outArray;
    int outLen;
    if (isUpdate) {
      outArray = engineUpdate(inArray, 0, inLen);
    } else {
      outArray = engineDoFinal(inArray, 0, inLen);
    }
    if (outArray == null) {
      outArray = new byte[0];
    }
    outLen = outArray.length;
    if (output.remaining() < outLen) {
      input.position(inPos);
      throw new ShortBufferException("Need at least " + outLen + " bytes of space in output buffer");
    }
    output.put(outArray);
    return outLen;
  }
}
