/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;

final class Tls13Schedule {
  final String hmac;
  final int hashLength;
  private final int keyLength;
  private final byte[] emptyHash;
  private final int aead;

  byte[] handshakeSecret;
  byte[] masterSecret;
  byte[] clientHandshake;
  byte[] serverHandshake;
  byte[] clientApplication;
  byte[] serverApplication;

  Tls13Schedule(CipherSuite suite) throws GeneralSecurityException {
    this.hmac = suite.hmac;
    this.hashLength = Hkdf.hashLength(hmac);
    this.keyLength = suite.keyLength;
    this.aead = suite.aead;
    this.emptyHash = MessageDigest.getInstance(suite.hash).digest(new byte[0]);
  }

  private byte[] deriveSecret(byte[] secret, String label, byte[] transcriptHash) {
    return Hkdf.expandLabel(hmac, secret, label, transcriptHash, hashLength);
  }

  void deriveHandshake(byte[] sharedSecret, byte[] helloHash) {
    byte[] zeros = new byte[hashLength];
    byte[] early = Hkdf.extract(hmac, zeros, zeros);
    byte[] derived = deriveSecret(early, "derived", emptyHash);
    handshakeSecret = Hkdf.extract(hmac, derived, sharedSecret);
    clientHandshake = deriveSecret(handshakeSecret, "c hs traffic", helloHash);
    serverHandshake = deriveSecret(handshakeSecret, "s hs traffic", helloHash);
    byte[] derived2 = deriveSecret(handshakeSecret, "derived", emptyHash);
    masterSecret = Hkdf.extract(hmac, derived2, zeros);
  }

  void deriveApplication(byte[] serverFinishedHash) {
    clientApplication = deriveSecret(masterSecret, "c ap traffic", serverFinishedHash);
    serverApplication = deriveSecret(masterSecret, "s ap traffic", serverFinishedHash);
  }

  RecordCipher cipher(byte[] secret, boolean decrypting) {
    byte[] key = Hkdf.expandLabel(hmac, secret, "key", new byte[0], keyLength);
    byte[] iv = Hkdf.expandLabel(hmac, secret, "iv", new byte[0], 12);
    return new RecordCipher(true, aead, key, iv, decrypting);
  }

  byte[] verifyData(byte[] baseSecret, byte[] transcriptHash) {
    byte[] finishedKey = Hkdf.expandLabel(hmac, baseSecret, "finished", new byte[0], hashLength);
    return Hkdf.hmac(hmac, finishedKey, transcriptHash);
  }

  byte[] nextTrafficSecret(byte[] secret) {
    return Hkdf.expandLabel(hmac, secret, "traffic upd", new byte[0], hashLength);
  }
}
