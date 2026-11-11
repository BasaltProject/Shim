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
import javax.crypto.Mac;
import java.security.SecureRandom;
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
  private byte[] macKey;
  private String macName;
  private int macLength;
  private int wireVersion = 0x0303;
  private byte[] chainIv;
  private static final SecureRandom kIvRandom = new SecureRandom();

  RecordCipher(CipherSuite suite, byte[] key, byte[] macKey) {
    this(false, suite.aead, key, new byte[0], false);
    this.macKey = macKey;
    this.macName = suite.macName;
    this.macLength = suite.macLength;
  }

  RecordCipher(boolean tls13, int aead, byte[] key, byte[] iv, boolean decrypting) {
    this.tls13 = tls13;
    this.aead = aead;
    this.key = key;
    this.iv = iv;
    this.decrypting = decrypting;
  }

  static RecordCipher[] keyBlock12(CipherSuite suite, int version, boolean dtls, byte[] master, byte[] serverRandom,
      byte[] clientRandom) {
    int keyLen = suite.keyLength;
    boolean cbc = suite.aead == CipherSuite.kCbc;
    boolean implicit = cbc && version == TlsConnection.kTls10;
    int ivLen = implicit ? 16 : cbc ? 0 : suite.aead == CipherSuite.kAeadChacha ? 12 : 4;
    int macLen = cbc ? suite.macLength : 0;
    int total = 2 * (keyLen + ivLen + macLen);
    byte[] block = TlsPrf.prf(version, suite.hmac, master, "key expansion", TlsPrf.concat(serverRandom, clientRandom),
        total);
    int p = 0;
    byte[] cMac = Arrays.copyOfRange(block, p, p += macLen);
    byte[] sMac = Arrays.copyOfRange(block, p, p += macLen);
    byte[] cKey = Arrays.copyOfRange(block, p, p += keyLen);
    byte[] sKey = Arrays.copyOfRange(block, p, p += keyLen);
    byte[] cIv = Arrays.copyOfRange(block, p, p += ivLen);
    byte[] sIv = Arrays.copyOfRange(block, p, p += ivLen);
    if (cbc) {
      RecordCipher client = new RecordCipher(suite, cKey, cMac);
      RecordCipher server = new RecordCipher(suite, sKey, sMac);
      client.wireVersion = server.wireVersion = dtls ? version == TlsConnection.kTls11 ? 0xFEFF : 0xFEFD : version;
      if (implicit) {
        client.chainIv = cIv;
        server.chainIv = sIv;
      }
      return new RecordCipher[] {client, server};
    }
    return new RecordCipher[] {new RecordCipher(false, suite.aead, cKey, cIv, false),
        new RecordCipher(false, suite.aead, sKey, sIv, false)};
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

  private byte[] cbcMac(long seq, int type, int version, byte[] data, int off, int len)
      throws GeneralSecurityException {
    Mac mac = Mac.getInstance(macName);
    mac.init(new SecretKeySpec(macKey, macName));
    ByteWriter h = new ByteWriter();
    h.u64(seq).u8(type).u16(version).u16(len);
    mac.update(h.toByteArray());
    mac.update(data, off, len);
    return mac.doFinal();
  }

  private byte[] cbcSeal(long seq, int type, int version, byte[] data, int off, int len)
      throws GeneralSecurityException {
    byte[] mac = cbcMac(seq, type, version, data, off, len);
    int unpadded = len + macLength + 1;
    int padLen = 16 - unpadded % 16;
    byte[] plain = new byte[unpadded + padLen];
    System.arraycopy(data, off, plain, 0, len);
    System.arraycopy(mac, 0, plain, len, macLength);
    Arrays.fill(plain, len + macLength, plain.length, (byte) padLen);
    byte[] iv16 = chainIv;
    if (iv16 == null) {
      iv16 = new byte[16];
      kIvRandom.nextBytes(iv16);
    }
    Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
    c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv16));
    byte[] ct = c.doFinal(plain);
    if (chainIv != null) {
      chainIv = Arrays.copyOfRange(ct, ct.length - 16, ct.length);
      return ct;
    }
    byte[] out = new byte[16 + ct.length];
    System.arraycopy(iv16, 0, out, 0, 16);
    System.arraycopy(ct, 0, out, 16, ct.length);
    return out;
  }

  private byte[] cbcOpen(long seq, int type, int version, byte[] fragment) throws GeneralSecurityException {
    int ivLen = chainIv != null ? 0 : 16;
    int min = ivLen + Math.max(16, (macLength + 1 + 15) / 16 * 16);
    if (fragment.length < min || (fragment.length - ivLen) % 16 != 0) {
      throw new GeneralSecurityException("Invalid CBC record length");
    }
    Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
    c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), chainIv != null ? new IvParameterSpec(chainIv)
        : new IvParameterSpec(fragment, 0, 16));
    byte[] plain = c.doFinal(fragment, ivLen, fragment.length - ivLen);
    if (chainIv != null) {
      chainIv = Arrays.copyOfRange(fragment, fragment.length - 16, fragment.length);
    }
    int padLen = plain[plain.length - 1] & 0xff;
    boolean ok = padLen + 1 + macLength <= plain.length;
    int bad = 0;
    if (!ok) {
      padLen = 0;
    }
    for (int i = 0; i <= padLen; i++) {
      bad |= (plain[plain.length - 1 - i] & 0xff) ^ padLen;
    }
    int dataLen = plain.length - 1 - padLen - macLength;
    if (dataLen < 0) {
      dataLen = 0;
      ok = false;
    }
    byte[] expected = cbcMac(seq, type, version, plain, 0, dataLen);
    byte[] actual = Arrays.copyOfRange(plain, dataLen, dataLen + macLength);
    if (!ok || bad != 0 || !java.security.MessageDigest.isEqual(expected, actual)) {
      throw new GeneralSecurityException("Bad record MAC");
    }
    return Arrays.copyOf(plain, dataLen);
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
    if (aead == CipherSuite.kCbc) {
      byte[] frag = cbcSeal(sequence, contentType, wireVersion, data, off, len);
      w.u8(contentType).u16(wireVersion).u16(frag.length).bytes(frag);
      sequence++;
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
    if (aead == CipherSuite.kCbc) {
      return cbcSeal(seq, contentType, wireVersion, data, off, len);
    }
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
    if (aead == CipherSuite.kCbc) {
      return cbcOpen(seq, contentType, wireVersion, fragment);
    }
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
    if (aead == CipherSuite.kCbc) {
      byte[] pt = cbcOpen(sequence, outerType, wireVersion, fragment);
      sequence++;
      return new Plain(outerType, pt);
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
