/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

final class NullCipherSpi extends CipherSpi {
  public NullCipherSpi() {
  }

  @Override
  protected void engineSetMode(String mode) {
  }

  @Override
  protected void engineSetPadding(String padding) {
  }

  @Override
  protected int engineGetBlockSize() {
    return 1;
  }

  @Override
  protected int engineGetOutputSize(int inputLen) {
    return inputLen;
  }

  @Override
  protected byte[] engineGetIV() {
    return new byte[8];
  }

  @Override
  protected AlgorithmParameters engineGetParameters() {
    return null;
  }

  @Override
  protected void engineInit(int mode, Key key, SecureRandom random) {
  }

  @Override
  protected void engineInit(int mode, Key key, AlgorithmParameterSpec params, SecureRandom random) {
  }

  @Override
  protected void engineInit(int mode, Key key, AlgorithmParameters params, SecureRandom random) {
  }

  @Override
  protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
    if (input == null) {
      return null;
    }
    byte[] output = new byte[inputLen];
    System.arraycopy(input, inputOffset, output, 0, inputLen);
    return output;
  }

  @Override
  protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) {
    if (input == null) {
      return 0;
    }
    System.arraycopy(input, inputOffset, output, outputOffset, inputLen);
    return inputLen;
  }

  @Override
  protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) {
    return engineUpdate(input, inputOffset, inputLen);
  }

  @Override
  protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) {
    return engineUpdate(input, inputOffset, inputLen, output, outputOffset);
  }

  @Override
  protected int engineGetKeySize(Key key) {
    return 0;
  }
}
