/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.jgss.krb5;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.MessageProp;
import sun.security.krb5.Crypto;

public final class Krb5Protect {
  private final int etype;
  private final byte[] key;
  private final boolean initiator;
  private final boolean acceptorSubkey;
  private long sendSeq;
  private long expected;
  private final Set<Long> seen = new HashSet<Long>();
  private boolean replayDet;
  private boolean seqDet;
  private final LegacyTokens legacy;

  public Krb5Protect(int etype, byte[] key, boolean initiator, boolean acceptorSubkey, long sendSeq, long peerSeq,
      boolean replayDet, boolean seqDet) {
    this.etype = etype;
    this.key = key;
    this.initiator = initiator;
    this.acceptorSubkey = acceptorSubkey;
    this.sendSeq = sendSeq;
    this.expected = peerSeq;
    this.replayDet = replayDet;
    this.seqDet = seqDet;
    this.legacy = LegacyTokens.handles(etype) ? new LegacyTokens(etype, key, initiator) : null;
  }

  public int etype() {
    return etype;
  }

  public byte[] key() {
    return key;
  }

  private int flags(boolean sealed) {
    return (initiator ? 0 : 1) | (sealed ? 2 : 0) | (acceptorSubkey ? 4 : 0);
  }

  private int sealUsage(boolean sender) {
    return sender == initiator ? 24 : 22;
  }

  private int signUsage(boolean sender) {
    return sender == initiator ? 25 : 23;
  }

  private byte[] header(int id, int flags, int ec, int rrc, long seq) {
    byte[] h = new byte[16];
    h[0] = (byte) (id >> 8);
    h[1] = (byte) id;
    h[2] = (byte) flags;
    h[3] = (byte) 0xff;
    h[4] = (byte) (ec >> 8);
    h[5] = (byte) ec;
    h[6] = (byte) (rrc >> 8);
    h[7] = (byte) rrc;
    for (int i = 0; i < 8; i++) {
      h[8 + i] = (byte) (seq >>> (56 - 8 * i));
    }
    return h;
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

  private static GSSException fail(String m) {
    return new GSSException(GSSException.FAILURE, -1, m);
  }

  private static GSSException defective(String m) {
    return new GSSException(GSSException.DEFECTIVE_TOKEN, -1, m);
  }

  public synchronized byte[] wrap(byte[] msg, int off, int len, boolean conf) throws GSSException {
    byte[] data = Arrays.copyOfRange(msg, off, off + len);
    long seq = sendSeq++;
    if (legacy != null) {
      return legacy.wrap(data, conf, seq);
    }
    try {
      if (conf) {
        byte[] hdr = header(0x0504, flags(true), 0, 0, seq);
        byte[] enc = Crypto.encrypt(etype, key, sealUsage(true), cat(data, hdr));
        return cat(hdr, enc);
      }
      int type = Crypto.defaultChecksum(etype);
      byte[] sum = Crypto.checksum(type, key, sealUsage(true), cat(data, header(0x0504, flags(false), 0, 0, seq)));
      return cat(header(0x0504, flags(false), sum.length, 0, seq), data, sum);
    } catch (GeneralSecurityException e) {
      throw fail(e.getMessage());
    }
  }

  public int wrapSizeLimit(int max) {
    if (legacy != null) {
      return max - legacy.overhead();
    }
    int cksum = Crypto.checksumSize(Crypto.defaultChecksum(etype));
    return max - (57 + cksum);
  }

  private static byte[] rotateLeft(byte[] b, int n) {
    if (b.length == 0 || n % b.length == 0) {
      return b;
    }
    n %= b.length;
    byte[] out = new byte[b.length];
    System.arraycopy(b, n, out, 0, b.length - n);
    System.arraycopy(b, 0, out, b.length - n, n);
    return out;
  }

  public synchronized byte[] unwrap(byte[] tok, int off, int len, MessageProp prop) throws GSSException {
    if (legacy != null) {
      int[] h = Krb5Context.unframe(tok, off, len);
      if (h[1] != 0x0201) {
        throw defective("Kerberos GSS-API Mechanism Token:Defective Token ID!");
      }
      LegacyTokens.Unwrapped u = legacy.unwrap(tok, off, len, h);
      prop.setPrivacy(u.sealed);
      prop.setQOP(0);
      checkSeq(u.seq, prop);
      return u.data;
    }
    if (len < 16) {
      throw defective("Wrap Token (new format):Cannot read all " + (16 - len) + " bytes needed to form this token!");
    }
    int id = ((tok[off] & 0xff) << 8) | (tok[off + 1] & 0xff);
    if (id != 0x0504) {
      throw defective("Kerberos GSS-API Mechanism Token:Defective Token ID!");
    }
    int fl = tok[off + 2] & 0xff;
    boolean fromAcceptor = (fl & 1) != 0;
    if (fromAcceptor != initiator) {
      throw defective("Wrap Token (new format):Acceptor Flag Error!");
    }
    if ((tok[off + 3] & 0xff) != 0xff) {
      throw defective("Wrap Token (new format):Defective Token Filler!");
    }
    boolean sealed = (fl & 2) != 0;
    int ec = ((tok[off + 4] & 0xff) << 8) | (tok[off + 5] & 0xff);
    int rrc = ((tok[off + 6] & 0xff) << 8) | (tok[off + 7] & 0xff);
    long seq = 0;
    for (int i = 0; i < 8; i++) {
      seq = (seq << 8) | (tok[off + 8 + i] & 0xff);
    }
    byte[] body = Arrays.copyOfRange(tok, off + 16, off + len);
    byte[] out;
    try {
      if (sealed) {
        byte[] plain = Crypto.decrypt(etype, key, sealUsage(false), rotateLeft(body, rrc));
        if (plain.length < 16 + ec) {
          throw defective("Wrap Token (new format):Token too short!");
        }
        byte[] copy = Arrays.copyOfRange(plain, plain.length - 16, plain.length);
        byte[] want = header(0x0504, fl, ec, 0, seq);
        if (!Arrays.equals(Arrays.copyOf(copy, 6), Arrays.copyOf(want, 6)) || copy[2] != want[2]) {
          throw defective("Wrap Token (new format):Header mismatch!");
        }
        out = Arrays.copyOf(plain, plain.length - 16 - ec);
      } else {
        byte[] b = rotateLeft(body, rrc);
        if (b.length < ec) {
          throw defective("Wrap Token (new format):Token too short!");
        }
        byte[] data = Arrays.copyOf(b, b.length - ec);
        byte[] sum = Arrays.copyOfRange(b, b.length - ec, b.length);
        byte[] want = Crypto.checksum(Crypto.defaultChecksum(etype), key, sealUsage(false),
            cat(data, header(0x0504, fl, 0, 0, seq)));
        if (!java.security.MessageDigest.isEqual(sum, want)) {
          throw fail("Could not use AES128 Cipher - Checksum failed");
        }
        out = data;
      }
    } catch (GeneralSecurityException e) {
      throw fail("Could not use AES128 Cipher - " + e.getMessage());
    }
    prop.setPrivacy(sealed);
    prop.setQOP(0);
    checkSeq(seq, prop);
    return out;
  }

  private void checkSeq(long seq, MessageProp prop) {
    boolean dup = false;
    boolean unseq = false;
    boolean gap = false;
    if (seen.contains(seq)) {
      dup = replayDet;
    } else if (seq == expected) {
      seen.add(seq);
      expected++;
    } else if (seq > expected) {
      gap = seqDet;
      seen.add(seq);
      expected = seq + 1;
    } else {
      unseq = seqDet;
      seen.add(seq);
    }
    prop.setSupplementaryStates(dup, false, unseq, gap, dup ? 19 : unseq ? 21 : gap ? 22 : 0,
        dup ? "Duplicate token" : unseq ? "Unsequenced token" : gap ? "Token gap" : null);
  }

  public synchronized byte[] getMIC(byte[] msg, int off, int len) throws GSSException {
    byte[] data = Arrays.copyOfRange(msg, off, off + len);
    long seq = sendSeq++;
    if (legacy != null) {
      return legacy.mic(data, seq);
    }
    byte[] hdr = header(0x0404, flags(false) & ~2, 0, 0, seq);
    java.util.Arrays.fill(hdr, 3, 8, (byte) 0xff);
    try {
      byte[] sum = Crypto.checksum(Crypto.defaultChecksum(etype), key, signUsage(true), cat(data, hdr));
      return cat(hdr, sum);
    } catch (GeneralSecurityException e) {
      throw fail(e.getMessage());
    }
  }

  public synchronized void verifyMIC(byte[] tok, int off, int len, byte[] msg, int moff, int mlen, MessageProp prop)
      throws GSSException {
    if (legacy != null) {
      int[] h = Krb5Context.unframe(tok, off, len);
      if (h[1] != 0x0101) {
        throw defective("Kerberos GSS-API Mechanism Token:Defective Token ID!");
      }
      long seq = legacy.verifyMic(tok, h, Arrays.copyOfRange(msg, moff, moff + mlen));
      prop.setQOP(0);
      checkSeq(seq, prop);
      return;
    }
    if (len < 16) {
      throw defective("Kerberos GSS-API Mechanism Token:Defective Token ID!");
    }
    int id = ((tok[off] & 0xff) << 8) | (tok[off + 1] & 0xff);
    if (id != 0x0404) {
      throw defective("Kerberos GSS-API Mechanism Token:Defective Token ID!");
    }
    int fl = tok[off + 2] & 0xff;
    if (((fl & 1) != 0) != initiator) {
      throw defective("MIC Token (new format):Acceptor Flag Error!");
    }
    long seq = 0;
    for (int i = 0; i < 8; i++) {
      seq = (seq << 8) | (tok[off + 8 + i] & 0xff);
    }
    byte[] hdr = Arrays.copyOfRange(tok, off, off + 16);
    byte[] sum = Arrays.copyOfRange(tok, off + 16, off + len);
    try {
      byte[] want = Crypto.checksum(Crypto.defaultChecksum(etype), key, signUsage(false),
          cat(Arrays.copyOfRange(msg, moff, moff + mlen), hdr));
      if (!java.security.MessageDigest.isEqual(sum, want)) {
        throw new GSSException(GSSException.BAD_MIC, -1, "Corrupt checksum or sequence number in MIC token");
      }
    } catch (GeneralSecurityException e) {
      throw new GSSException(GSSException.BAD_MIC, -1, "Corrupt checksum or sequence number in MIC token");
    }
    prop.setQOP(0);
    checkSeq(seq, prop);
  }
}
