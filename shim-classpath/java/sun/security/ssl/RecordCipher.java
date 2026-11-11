/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

final class RecordCipher {
  static final int kContentChangeCipherSpec = 20;
  static final int kContentAlert = 21;
  static final int kContentHandshake = 22;
  static final int kContentApplicationData = 23;

  private final boolean tls13;
  private final byte[] key;
  private final byte[] iv;
  private final boolean decrypting;
  private final int aead;
  private long sequence;

  RecordCipher(boolean tls13, int aead, byte[] key, byte[] iv, boolean decrypting) {
    this.tls13 = tls13;
    this.aead = aead;
    this.key = key;
    this.iv = iv;
    this.decrypting = decrypting;
  }

  long sequence() {
    return sequence;
  }

  private Cipher cipher(byte[] nonce, int mode) throws GeneralSecurityException {
    if (aead == CipherSuite.kAeadChacha) {
      Cipher c = Cipher.getInstance("ChaCha20-Poly1305");
      c.init(mode, new SecretKeySpec(key, "ChaCha20"), new IvParameterSpec(nonce));
      return c;
    }
    Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
    c.init(mode, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
    return c;
  }

  private byte[] nonce13() {
    byte[] n = iv.clone();
    for (int i = 0; i < 8; i++) {
      n[n.length - 1 - i] ^= (byte) (sequence >>> (8 * i));
    }
    return n;
  }

  private byte[] xorSequence(long seq) {
    byte[] n = iv.clone();
    for (int i = 0; i < 8; i++) {
      n[n.length - 1 - i] ^= (byte) (seq >>> (8 * i));
    }
    return n;
  }

  private byte[] explicitNonce(long seq) {
    byte[] n = new byte[12];
    System.arraycopy(iv, 0, n, 0, 4);
    for (int i = 0; i < 8; i++) {
      n[4 + i] = (byte) (seq >>> (56 - 8 * i));
    }
    return n;
  }

  private byte[] explicitNonce(byte[] fragment) {
    byte[] n = new byte[12];
    System.arraycopy(iv, 0, n, 0, 4);
    System.arraycopy(fragment, 0, n, 4, 8);
    return n;
  }

  byte[] encrypt(int contentType, byte[] data, int off, int len) throws GeneralSecurityException {
    ByteWriter w = new ByteWriter();
    if (tls13) {
      int innerLen = len + 1;
      byte[] header = {(byte) kContentApplicationData, 3, 3, (byte) ((innerLen + 16) >> 8), (byte) (innerLen + 16)};
      Cipher c = cipher(nonce13(), Cipher.ENCRYPT_MODE);
      c.updateAAD(header);
      byte[] inner = new byte[innerLen];
      System.arraycopy(data, off, inner, 0, len);
      inner[len] = (byte) contentType;
      byte[] ct = c.doFinal(inner);
      sequence++;
      w.bytes(header).bytes(ct);
      return w.toByteArray();
    }
    boolean chacha = aead == CipherSuite.kAeadChacha;
    byte[] nonce = chacha ? xorSequence(sequence) : explicitNonce(sequence);
    ByteWriter aad = new ByteWriter();
    aad.u64(sequence).u8(contentType).u16(0x0303).u16(len);
    Cipher c = cipher(nonce, Cipher.ENCRYPT_MODE);
    c.updateAAD(aad.toByteArray());
    byte[] ct = c.doFinal(data, off, len);
    w.u8(contentType).u16(0x0303).u16((chacha ? 0 : 8) + ct.length);
    if (!chacha) {
      w.u64(sequence);
    }
    w.bytes(ct);
    sequence++;
    return w.toByteArray();
  }

  byte[] encryptDtls(int contentType, byte[] data, int off, int len, long seq) throws GeneralSecurityException {
    boolean chacha = aead == CipherSuite.kAeadChacha;
    byte[] nonce = chacha ? xorSequence(seq) : explicitNonce(seq);
    ByteWriter aad = new ByteWriter();
    aad.u64(seq).u8(contentType).u16(0xFEFD).u16(len);
    Cipher c = cipher(nonce, Cipher.ENCRYPT_MODE);
    c.updateAAD(aad.toByteArray());
    byte[] ct = c.doFinal(data, off, len);
    return chacha ? ct : new ByteWriter().u64(seq).bytes(ct).toByteArray();
  }

  byte[] decryptDtls(int contentType, byte[] fragment, long seq) throws GeneralSecurityException {
    boolean chacha = aead == CipherSuite.kAeadChacha;
    int skip = chacha ? 0 : 8;
    if (fragment.length < skip + 16) {
      throw new GeneralSecurityException("Insufficient buffer remaining for AEAD cipher fragment");
    }
    byte[] nonce = chacha ? xorSequence(seq) : explicitNonce(fragment);
    ByteWriter aad = new ByteWriter();
    aad.u64(seq).u8(contentType).u16(0xFEFD).u16(fragment.length - skip - 16);
    Cipher c = cipher(nonce, Cipher.DECRYPT_MODE);
    c.updateAAD(aad.toByteArray());
    return c.doFinal(fragment, skip, fragment.length - skip);
  }

  static final class Plain {
    final int type;
    final byte[] data;

    Plain(int type, byte[] data) {
      this.type = type;
      this.data = data;
    }
  }

  Plain decrypt(int outerType, byte[] fragment) throws GeneralSecurityException {
    if (tls13) {
      byte[] header = {(byte) outerType, 3, 3, (byte) (fragment.length >> 8), (byte) fragment.length};
      Cipher c = cipher(nonce13(), Cipher.DECRYPT_MODE);
      c.updateAAD(header);
      byte[] inner = c.doFinal(fragment);
      sequence++;
      int i = inner.length - 1;
      while (i >= 0 && inner[i] == 0) {
        i--;
      }
      if (i < 0) {
        throw new GeneralSecurityException("Unexpected end of content");
      }
      return new Plain(inner[i] & 0xff, Arrays.copyOf(inner, i));
    }
    boolean chacha = aead == CipherSuite.kAeadChacha;
    int skip = chacha ? 0 : 8;
    if (fragment.length < skip + 16) {
      throw new GeneralSecurityException("Insufficient buffer remaining for AEAD cipher fragment");
    }
    byte[] nonce = chacha ? xorSequence(sequence) : explicitNonce(fragment);
    int plainLen = fragment.length - skip - 16;
    ByteWriter aad = new ByteWriter();
    aad.u64(sequence).u8(outerType).u16(0x0303).u16(plainLen);
    Cipher c = cipher(nonce, Cipher.DECRYPT_MODE);
    c.updateAAD(aad.toByteArray());
    byte[] pt = c.doFinal(fragment, skip, fragment.length - skip);
    sequence++;
    return new Plain(outerType, pt);
  }
}
