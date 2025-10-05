/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.security.InvalidKeyException;

abstract class SymmetricCipher {
  abstract int getBlockSize();

  abstract void init(boolean decrypting, String algorithm, byte[] key) throws InvalidKeyException;

  abstract void encryptBlock(byte[] plain, int plainOffset, byte[] cipher, int cipherOffset);

  abstract void decryptBlock(byte[] cipher, int cipherOffset, byte[] plain, int plainOffset);
}
