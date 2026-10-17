/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

final class ConstructKeys {
  private ConstructKeys() {
  }

  static PublicKey constructPublicKey(byte[] encodedKey, String encodedKeyAlgorithm)
      throws InvalidKeyException, NoSuchAlgorithmException {
    try {
      KeyFactory keyFactory = KeyFactory.getInstance(encodedKeyAlgorithm);
      return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
    } catch (NoSuchAlgorithmException nsae) {
      throw new NoSuchAlgorithmException("No installed providers can create keys for the " + encodedKeyAlgorithm
          + "algorithm", nsae);
    } catch (InvalidKeySpecException ikse) {
      throw new InvalidKeyException("Cannot construct public key", ikse);
    }
  }

  static PrivateKey constructPrivateKey(byte[] encodedKey, String encodedKeyAlgorithm)
      throws InvalidKeyException, NoSuchAlgorithmException {
    try {
      KeyFactory keyFactory = KeyFactory.getInstance(encodedKeyAlgorithm);
      return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
    } catch (NoSuchAlgorithmException nsae) {
      throw new NoSuchAlgorithmException("No installed providers can create keys for the " + encodedKeyAlgorithm
          + "algorithm", nsae);
    } catch (InvalidKeySpecException ikse) {
      throw new InvalidKeyException("Cannot construct private key", ikse);
    }
  }

  static SecretKey constructSecretKey(byte[] encodedKey, String encodedKeyAlgorithm) {
    return new SecretKeySpec(encodedKey, encodedKeyAlgorithm);
  }

  static Key constructKey(byte[] encoding, String keyAlgorithm, int keyType) throws InvalidKeyException,
      NoSuchAlgorithmException {
    Key result = null;
    switch (keyType) {
    case Cipher.SECRET_KEY:
      result = constructSecretKey(encoding, keyAlgorithm);
      break;
    case Cipher.PRIVATE_KEY:
      result = constructPrivateKey(encoding, keyAlgorithm);
      break;
    case Cipher.PUBLIC_KEY:
      result = constructPublicKey(encoding, keyAlgorithm);
      break;
    default:
      break;
    }
    return result;
  }
}
