/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.jgss.krb5;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.ietf.jgss.GSSException;
import sun.security.jgss.GSSManagerImpl;
import sun.security.krb5.Crypto;

final class LegacyTokens {
  static final int kRc4 = 23;
  static final int kDes3 = 16;
  private static final int kWrapId = 0x0201;
  private static final int kMicId = 0x0101;
  private static final int kRc4Usage = 13;
  private static final int kRc4MicUsage = 15;
  private static final int kDes3Usage = 23;

  private final int etype;
  private final byte[] key;
  private final boolean initiator;

  LegacyTokens(int etype, byte[] key, boolean initiator) {
    this.etype = etype;
    this.key = key;
    this.initiator = initiator;
  }

  static boolean handles(int etype) {
    return etype == kRc4 || etype == kDes3;
  }

  int overhead() {
    return etype == kRc4 ? 57 : 69;
  }

  static final class Unwrapped {
    final byte[] data;
    final boolean sealed;
    final long seq;

    Unwrapped(byte[] data, boolean sealed, long seq) {
      this.data = data;
      this.sealed = sealed;
      this.seq = seq;
    }
  }

  private int sgnAlg() {
    return etype == kRc4 ? 0x1100 : 0x0400;
  }

  private int checksumLength() {
    return etype == kRc4 ? 8 : 20;
  }

  private static byte[] cat(byte[]... p) {
    int n = 0;
    for (byte[] b : p) {
      n += b.length;
    }
    byte[] out = new byte[n];
    int o = 0;
    for (byte[] b : p) {
      System.arraycopy(b, 0, out, o, b.length);
      o += b.length;
    }
    return out;
  }

  private static byte[] header(int id, int sgn, int seal) {
    return new byte[] {(byte) (id >> 8), (byte) id, (byte) (sgn >> 8), (byte) sgn, (byte) (seal >> 8), (byte) seal,
        (byte) 0xff, (byte) 0xff};
  }

  private byte[] direction(boolean fromInitiator) {
    byte b = (byte) (fromInitiator ? 0 : 0xff);
    return new byte[] {b, b, b, b};
  }

  private static byte[] be32(long v) {
    return new byte[] {(byte) (v >>> 24), (byte) (v >>> 16), (byte) (v >>> 8), (byte) v};
  }

  private byte[] checksum(byte[] hdr, byte[] body, boolean mic) throws GeneralSecurityException {
    if (etype == kRc4) {
      return Arrays.copyOf(Crypto.checksum(Crypto.CKSUM_HMAC_MD5, key, mic ? kRc4MicUsage : kRc4Usage, cat(hdr, body)),
          8);
    }
    return Crypto.checksum(Crypto.CKSUM_HMAC_SHA1_DES3, key, kDes3Usage, cat(hdr, body));
  }

  private byte[] sequence(long seq, byte[] cksum, boolean encrypt, boolean fromInitiator, byte[] in)
      throws GeneralSecurityException {
    if (etype == kRc4) {
      byte[] k = Crypto.hmac("HmacMD5", Crypto.hmac("HmacMD5", key, Crypto.le32(0)), cksum);
      return Crypto.rc4(k, encrypt ? cat(be32(seq), direction(fromInitiator)) : in);
    }
    Cipher c = Cipher.getInstance("DESede/CBC/NoPadding");
    c.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, new SecretKeySpec(key, "DESede"),
        new IvParameterSpec(Arrays.copyOf(cksum, 8)));
    return c.doFinal(encrypt ? cat(be32(seq), direction(fromInitiator)) : in);
  }

  private byte[] dataCipher(boolean encrypt, long seq, byte[] in) throws GeneralSecurityException {
    if (etype == kRc4) {
      byte[] local = key.clone();
      for (int i = 0; i < local.length; i++) {
        local[i] ^= (byte) 0xf0;
      }
      byte[] k = Crypto.hmac("HmacMD5", Crypto.hmac("HmacMD5", local, Crypto.le32(0)), be32(seq));
      return Crypto.rc4(k, in);
    }
    Cipher c = Cipher.getInstance("DESede/CBC/NoPadding");
    c.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, new SecretKeySpec(key, "DESede"),
        new IvParameterSpec(new byte[8]));
    return c.doFinal(in);
  }

  private byte[] pad(int dataLength) {
    int n = etype == kRc4 ? 1 : 8 - dataLength % 8;
    byte[] p = new byte[n];
    Arrays.fill(p, (byte) (etype == kRc4 ? 1 : n));
    return p;
  }

  byte[] wrap(byte[] data, boolean conf, long seq) throws GSSException {
    try {
      byte[] hdr = header(kWrapId, sgnAlg(), conf ? (etype == kRc4 ? 0x1000 : 0x0200) : 0xffff);
      byte[] plain = cat(Crypto.random(8), data, pad(data.length));
      byte[] cksum = checksum(hdr, plain, false);
      byte[] snd = sequence(seq, cksum, true, initiator, null);
      byte[] body = conf ? dataCipher(true, seq, plain) : plain;
      return Krb5Context.frame(GSSManagerImpl.KRB5, kWrapId, cat(Arrays.copyOfRange(hdr, 2, 8), snd, cksum, body));
    } catch (GeneralSecurityException e) {
      throw new GSSException(GSSException.FAILURE, -1, e.getMessage());
    }
  }

  byte[] mic(byte[] msg, long seq) throws GSSException {
    try {
      byte[] hdr = header(kMicId, sgnAlg(), 0xffff);
      byte[] cksum = checksum(hdr, msg, true);
      byte[] snd = sequence(seq, cksum, true, initiator, null);
      return Krb5Context.frame(GSSManagerImpl.KRB5, kMicId, cat(Arrays.copyOfRange(hdr, 2, 8), snd, cksum));
    } catch (GeneralSecurityException e) {
      throw new GSSException(GSSException.FAILURE, -1, e.getMessage());
    }
  }

  private static GSSException defective(String m) {
    return new GSSException(GSSException.DEFECTIVE_TOKEN, -1, m);
  }

  private long readSequence(byte[] tok, int at, byte[] cksum, String what)
      throws GSSException, GeneralSecurityException {
    byte[] snd = sequence(0, cksum, false, false, Arrays.copyOfRange(tok, at, at + 8));
    byte[] want = direction(!initiator);
    if (!Arrays.equals(Arrays.copyOfRange(snd, 4, 8), want)) {
      throw new GSSException(GSSException.BAD_MIC, -1, "Corrupt checksum or sequence number in " + what);
    }
    return ((snd[0] & 0xffL) << 24) | ((snd[1] & 0xff) << 16) | ((snd[2] & 0xff) << 8) | (snd[3] & 0xff);
  }

  Unwrapped unwrap(byte[] tok, int off, int len, int[] h) throws GSSException {
    int p = h[0];
    int end = h[2];
    int ck = checksumLength();
    if (end - p < 6 + 8 + ck + 8) {
      throw defective("Wrap Token:Cannot read all bytes needed to form this token!");
    }
    int sgn = ((tok[p] & 0xff) << 8) | (tok[p + 1] & 0xff);
    int seal = ((tok[p + 2] & 0xff) << 8) | (tok[p + 3] & 0xff);
    if (sgn != sgnAlg() || (tok[p + 4] & 0xff) != 0xff || (tok[p + 5] & 0xff) != 0xff) {
      throw defective("Kerberos GSS-API Mechanism Token:Defective Token ID!");
    }
    boolean sealed = seal == (etype == kRc4 ? 0x1000 : 0x0200);
    if (!sealed && seal != 0xffff) {
      throw defective("Kerberos GSS-API Mechanism Token:Defective Token ID!");
    }
    byte[] hdr = header(kWrapId, sgn, seal);
    byte[] cksum = Arrays.copyOfRange(tok, p + 14, p + 14 + ck);
    try {
      long seq = readSequence(tok, p + 6, cksum, "Wrap token");
      byte[] body = Arrays.copyOfRange(tok, p + 14 + ck, end);
      byte[] plain = sealed ? dataCipher(false, seq, body) : body;
      if (plain.length < 8 + 1) {
        throw defective("Wrap Token:Invalid padding on Wrap Token");
      }
      int padLen = plain[plain.length - 1] & 0xff;
      boolean badPad = padLen == 0 || padLen > (etype == kRc4 ? 1 : 8) || plain.length < 8 + padLen;
      for (int i = 0; !badPad && i < padLen; i++) {
        badPad = (plain[plain.length - 1 - i] & 0xff) != padLen;
      }
      if (badPad) {
        throw defective("Invalid padding on Wrap Token");
      }
      if (!MessageDigest.isEqual(checksum(hdr, plain, false), cksum)) {
        throw new GSSException(GSSException.BAD_MIC, -1, "Corrupt checksum or sequence number in Wrap token");
      }
      return new Unwrapped(Arrays.copyOfRange(plain, 8, plain.length - padLen), sealed, seq);
    } catch (GeneralSecurityException e) {
      throw defective("Invalid padding on Wrap Token");
    }
  }

  long verifyMic(byte[] tok, int[] h, byte[] msg) throws GSSException {
    int p = h[0];
    int ck = checksumLength();
    if (h[2] - p != 6 + 8 + ck) {
      throw defective("Kerberos GSS-API Mechanism Token:Defective Token ID!");
    }
    int sgn = ((tok[p] & 0xff) << 8) | (tok[p + 1] & 0xff);
    if (sgn != sgnAlg()) {
      throw defective("Kerberos GSS-API Mechanism Token:Defective Token ID!");
    }
    byte[] hdr = header(kMicId, sgn, 0xffff);
    byte[] cksum = Arrays.copyOfRange(tok, p + 14, p + 14 + ck);
    try {
      long seq = readSequence(tok, p + 6, cksum, "MIC token");
      if (!MessageDigest.isEqual(checksum(hdr, msg, true), cksum)) {
        throw new GSSException(GSSException.BAD_MIC, -1, "Corrupt checksum or sequence number in MIC token");
      }
      return seq;
    } catch (GeneralSecurityException e) {
      throw new GSSException(GSSException.BAD_MIC, -1, "Corrupt checksum or sequence number in MIC token");
    }
  }
}
