/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public enum CryptoPrimitive {
  STREAM_CIPHER,
  BLOCK_CIPHER,
  SECURE_RANDOM,
  MESSAGE_DIGEST,
  MAC,
  KEY_WRAP,
  PUBLIC_KEY_ENCRYPTION,
  SIGNATURE,
  KEY_ENCAPSULATION,
  KEY_AGREEMENT
}
