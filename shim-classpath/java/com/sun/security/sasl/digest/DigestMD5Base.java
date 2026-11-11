/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.sasl.digest;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;

abstract class DigestMD5Base {
  static final String kName = "DIGEST-MD5";
  static final String[][] kStrengthGroups = {{"3des", "rc4"}, {"des", "rc4-56"}, {"rc4-40"}};
  static final String[] kStrengthNames = {"high", "medium", "low"};
  static final byte[] kEmpty = new byte[0];
  static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("javax.security.sasl");

  protected final String protocol;
  protected final String serverName;
  protected final Map<String, ?> props;
  protected boolean completed;
  protected boolean useUtf8 = true;
  protected int negotiatedLayer;
  protected String negotiatedCipher;
  protected int sendMaxBuf = 65536;
  protected int recvMaxBuf = 65536;
  protected byte[] ha1;
  private Layer outgoing;
  private Layer incoming;
  private final boolean isClient;

  DigestMD5Base(String protocol, String serverName, Map<String, ?> props, boolean isClient) {
    this.protocol = protocol;
    this.serverName = serverName;
    this.props = props;
    this.isClient = isClient;
  }

  Charset charset() {
    return useUtf8 ? StandardCharsets.UTF_8 : StandardCharsets.ISO_8859_1;
  }

  static byte[] md5(byte[]... parts) {
    try {
      MessageDigest d = MessageDigest.getInstance("MD5");
      for (byte[] p : parts) {
        d.update(p);
      }
      return d.digest();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }

  static byte[] bytes(String s, Charset cs) {
    return s.getBytes(cs);
  }

  static String hex(byte[] b) {
    StringBuilder sb = new StringBuilder(b.length * 2);
    for (byte x : b) {
      sb.append(Character.forDigit((x >> 4) & 0xf, 16)).append(Character.forDigit(x & 0xf, 16));
    }
    return sb.toString();
  }

  static byte[] concat(byte[]... parts) {
    int n = 0;
    for (byte[] p : parts) {
      n += p.length;
    }
    byte[] out = new byte[n];
    int o = 0;
    for (byte[] p : parts) {
      System.arraycopy(p, 0, out, o, p.length);
      o += p.length;
    }
    return out;
  }

  static byte[] ascii(String s) {
    return s.getBytes(StandardCharsets.ISO_8859_1);
  }

  static List<String[]> parse(byte[] data, Charset cs) throws SaslException {
    String s = new String(data, cs);
    List<String[]> out = new ArrayList<String[]>();
    int i = 0;
    int n = s.length();
    while (i < n) {
      while (i < n && (s.charAt(i) == ',' || s.charAt(i) == ' ' || s.charAt(i) == '\t' || s.charAt(i) == '\r'
          || s.charAt(i) == '\n')) {
        i++;
      }
      if (i >= n) {
        break;
      }
      int eq = s.indexOf('=', i);
      if (eq < 0) {
        throw new SaslException("DIGEST-MD5: Invalid digest response format: " + s);
      }
      String name = s.substring(i, eq).trim().toLowerCase(java.util.Locale.ROOT);
      i = eq + 1;
      while (i < n && s.charAt(i) == ' ') {
        i++;
      }
      StringBuilder v = new StringBuilder();
      if (i < n && s.charAt(i) == '"') {
        i++;
        while (i < n && s.charAt(i) != '"') {
          if (s.charAt(i) == '\\' && i + 1 < n) {
            i++;
          }
          v.append(s.charAt(i++));
        }
        i++;
      } else {
        while (i < n && s.charAt(i) != ',') {
          v.append(s.charAt(i++));
        }
      }
      out.add(new String[] {name, v.toString().trim()});
    }
    return out;
  }

  static String quote(String s) {
    return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }

  byte[] a2(boolean clientSide, String digestUri, String qop) {
    String prefix = clientSide ? "AUTHENTICATE:" : ":";
    String suffix = qop.equals("auth") ? "" : ":00000000000000000000000000000000";
    return bytes(prefix + digestUri + suffix, StandardCharsets.UTF_8);
  }

  static String response(byte[] ha1, String nonce, String nc, String cnonce, String qop, byte[] a2) {
    byte[] hexHa2 = ascii(hex(md5(a2)));
    return hex(md5(ascii(hex(ha1)), ascii(":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":"), hexHa2));
  }

  static boolean propTrue(Map<String, ?> props, String key, boolean def) {
    Object v = props == null ? null : props.get(key);
    return v == null ? def : "true".equalsIgnoreCase(String.valueOf(v));
  }

  static int propInt(Map<String, ?> props, String key, int def) throws SaslException {
    Object v = props == null ? null : props.get(key);
    if (v == null) {
      return def;
    }
    try {
      return Integer.parseInt(String.valueOf(v));
    } catch (NumberFormatException e) {
      throw new SaslException("Property must be string representation of integer: " + key);
    }
  }

  static String[] qopPreference(Map<String, ?> props) throws SaslException {
    Object v = props == null ? null : props.get(Sasl.QOP);
    if (v == null) {
      return new String[] {"auth"};
    }
    List<String> out = new ArrayList<String>();
    for (String t : String.valueOf(v).split("[,\\s]+")) {
      if (t.isEmpty()) {
        continue;
      }
      if (!t.equals("auth") && !t.equals("auth-int") && !t.equals("auth-conf")) {
        throw new SaslException("Invalid QOP string: " + t);
      }
      out.add(t);
    }
    return out.toArray(new String[0]);
  }

  static String[] strengthPreference(Map<String, ?> props) {
    Object v = props == null ? null : props.get(Sasl.STRENGTH);
    if (v == null) {
      return kStrengthNames;
    }
    List<String> out = new ArrayList<String>();
    for (String t : String.valueOf(v).split("[,\\s]+")) {
      if (!t.isEmpty()) {
        out.add(t.toLowerCase(java.util.Locale.ROOT));
      }
    }
    return out.toArray(new String[0]);
  }

  static String strengthOf(String cipher) {
    for (int i = 0; i < kStrengthGroups.length; i++) {
      for (String c : kStrengthGroups[i]) {
        if (c.equals(cipher)) {
          return kStrengthNames[i];
        }
      }
    }
    return null;
  }

  void setupLayers(int layer, String cipher) throws SaslException {
    negotiatedLayer = layer;
    negotiatedCipher = cipher;
    if (layer == 0) {
      return;
    }
    byte[] ha1Raw = ha1;
    String c2sSign = "Digest session key to client-to-server signing key magic constant";
    String s2cSign = "Digest session key to server-to-client signing key magic constant";
    String c2sSeal = "Digest H(A1) to client-to-server sealing key magic constant";
    String s2cSeal = "Digest H(A1) to server-to-client sealing key magic constant";
    byte[] kiOut = md5(ha1Raw, ascii(isClient ? c2sSign : s2cSign));
    byte[] kiIn = md5(ha1Raw, ascii(isClient ? s2cSign : c2sSign));
    if (layer == 1) {
      outgoing = new DigestIntegrity(kiOut);
      incoming = new DigestIntegrity(kiIn);
    } else {
      outgoing = new DigestPrivacy(kiOut, sealingKey(isClient ? c2sSeal : s2cSeal, cipher), cipher, true);
      incoming = new DigestPrivacy(kiIn, sealingKey(isClient ? s2cSeal : c2sSeal, cipher), cipher, false);
    }
    int overhead = layer == 1 ? 16 : 26;
    rawSendSize = Math.max(sendMaxBuf - overhead, 0);
  }

  protected int rawSendSize;

  private byte[] sealingKey(String magic, String cipher) {
    int n = cipher.equals("rc4-40") ? 5 : cipher.equals("rc4-56") ? 7 : 16;
    return md5(java.util.Arrays.copyOf(ha1, n), ascii(magic));
  }

  public byte[] wrap(byte[] outgoingBytes, int offset, int len) throws SaslException {
    if (!completed) {
      throw new IllegalStateException("DIGEST-MD5 authentication not completed");
    }
    if (negotiatedLayer == 0) {
      throw new IllegalStateException("DIGEST-MD5 supports neither integrity nor privacy");
    }
    return outgoing.wrap(outgoingBytes, offset, len);
  }

  public byte[] unwrap(byte[] incomingBytes, int offset, int len) throws SaslException {
    if (!completed) {
      throw new IllegalStateException("DIGEST-MD5 authentication not completed");
    }
    if (negotiatedLayer == 0) {
      throw new IllegalStateException("DIGEST-MD5 supports neither integrity nor privacy");
    }
    return incoming.unwrap(incomingBytes, offset, len);
  }

  public boolean isComplete() {
    return completed;
  }

  public void dispose() throws SaslException {
  }

  public Object getNegotiatedProperty(String propName) {
    if (!completed) {
      throw new IllegalStateException("DIGEST-MD5 authentication not completed");
    }
    switch (propName) {
    case Sasl.QOP:
      return negotiatedLayer == 0 ? "auth" : negotiatedLayer == 1 ? "auth-int" : "auth-conf";
    case Sasl.STRENGTH:
      return negotiatedLayer == 2 ? strengthOf(negotiatedCipher) : null;
    case Sasl.MAX_BUFFER:
      return null;
    case Sasl.RAW_SEND_SIZE:
      return negotiatedLayer == 0 ? "0" : Integer.toString(rawSendSize);
    case Sasl.BOUND_SERVER_NAME:
      return serverName;
    default:
      return null;
    }
  }

  private abstract class Layer {
    final byte[] ki;
    final Cipher cipher;
    final String name;
    int seq;

    Layer(byte[] ki, byte[] kc, String cipherName, boolean encrypt) throws SaslException {
      this.ki = ki;
      this.name = cipherName;
      if (kc == null) {
        this.cipher = null;
        return;
      }
      try {
        Cipher c;
        int mode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
        if (cipherName.startsWith("rc4")) {
          c = Cipher.getInstance("RC4");
          c.init(mode, new SecretKeySpec(kc, "RC4"));
        } else if (cipherName.equals("des")) {
          c = Cipher.getInstance("DES/CBC/NoPadding");
          c.init(mode, new SecretKeySpec(expandDes(kc, 0), "DES"), new IvParameterSpec(
              java.util.Arrays.copyOfRange(kc, 8, 16)));
        } else {
          c = Cipher.getInstance("DESede/CBC/NoPadding");
          byte[] k1 = expandDes(kc, 0);
          byte[] k2 = expandDes(kc, 7);
          c.init(mode, new SecretKeySpec(concat(k1, k2, k1), "DESede"), new IvParameterSpec(
              java.util.Arrays.copyOfRange(kc, 8, 16)));
        }
        this.cipher = c;
      } catch (GeneralSecurityException e) {
        throw new SaslException("DIGEST-MD5: Error initializing cipher", e);
      }
    }

    byte[] mac(int seqnum, byte[] msg, int off, int len) {
      try {
        Mac m = Mac.getInstance("HmacMD5");
        m.init(new SecretKeySpec(ki, "HmacMD5"));
        m.update(new byte[] {(byte) (seqnum >>> 24), (byte) (seqnum >>> 16), (byte) (seqnum >>> 8), (byte) seqnum});
        m.update(msg, off, len);
        return java.util.Arrays.copyOf(m.doFinal(), 10);
      } catch (GeneralSecurityException e) {
        throw new IllegalStateException(e);
      }
    }

    byte[] trailer() {
      return new byte[] {0, 1, (byte) (seq >>> 24), (byte) (seq >>> 16), (byte) (seq >>> 8), (byte) seq};
    }

    int receivedSeq(byte[] in, int off, int len) {
      int t = off + len - 4;
      return ((in[t] & 0xff) << 24) | ((in[t + 1] & 0xff) << 16) | ((in[t + 2] & 0xff) << 8) | (in[t + 3] & 0xff);
    }

    void checkSeq(int got) throws SaslException {
      if (got != seq) {
        throw new SaslException("DIGEST-MD5: Out of order sequencing of messages from server. Got: " + got
            + " Expected: " + seq);
      }
    }

    abstract byte[] wrap(byte[] msg, int off, int len) throws SaslException;

    abstract byte[] unwrap(byte[] in, int off, int len) throws SaslException;
  }

  private final class DigestIntegrity extends Layer {
    DigestIntegrity(byte[] ki) throws SaslException {
      super(ki, null, null, true);
    }

    @Override
    byte[] wrap(byte[] msg, int off, int len) {
      byte[] m = mac(seq, msg, off, len);
      byte[] out = concat(java.util.Arrays.copyOfRange(msg, off, off + len), m, trailer());
      seq++;
      return out;
    }

    @Override
    byte[] unwrap(byte[] in, int off, int len) throws SaslException {
      if (len < 16) {
        throw new SaslException("DIGEST-MD5: Invalid message format");
      }
      int bodyLen = len - 16;
      int got = receivedSeq(in, off, len);
      byte[] body = java.util.Arrays.copyOfRange(in, off, off + bodyLen);
      byte[] macBytes = java.util.Arrays.copyOfRange(in, off + bodyLen, off + bodyLen + 10);
      if (!MessageDigest.isEqual(mac(got, body, 0, bodyLen), macBytes)) {
        logger.log(java.util.logging.Level.INFO, "DIGEST41:Unmatched MACs");
        return kEmpty;
      }
      checkSeq(got);
      seq++;
      return body;
    }
  }

  private final class DigestPrivacy extends Layer {
    DigestPrivacy(byte[] ki, byte[] kc, String cipherName, boolean encrypt) throws SaslException {
      super(ki, kc, cipherName, encrypt);
    }

    @Override
    byte[] wrap(byte[] msg, int off, int len) {
      byte[] m = mac(seq, msg, off, len);
      int block = name.startsWith("rc4") ? 0 : 8;
      int pad = block == 0 ? 0 : block - ((len + 10) % block);
      byte[] plain = new byte[len + pad + 10];
      System.arraycopy(msg, off, plain, 0, len);
      for (int i = 0; i < pad; i++) {
        plain[len + i] = (byte) pad;
      }
      System.arraycopy(m, 0, plain, len + pad, 10);
      byte[] out = concat(cipher.update(plain), trailer());
      seq++;
      return out;
    }

    @Override
    byte[] unwrap(byte[] in, int off, int len) throws SaslException {
      if (len < 6) {
        throw new SaslException("DIGEST-MD5: Invalid message format");
      }
      int got = receivedSeq(in, off, len);
      byte[] plain = cipher.update(in, off, len - 6);
      int pad = name.startsWith("rc4") ? 0 : plain.length < 11 ? 99 : plain[plain.length - 11] & 0xff;
      int bodyLen = plain.length - 10 - pad;
      if (bodyLen < 0 || pad > 8) {
        logger.log(java.util.logging.Level.INFO, "DIGEST41:Unmatched MACs");
        return kEmpty;
      }
      byte[] body = java.util.Arrays.copyOf(plain, bodyLen);
      byte[] macBytes = java.util.Arrays.copyOfRange(plain, plain.length - 10, plain.length);
      if (!MessageDigest.isEqual(mac(got, body, 0, bodyLen), macBytes)) {
        logger.log(java.util.logging.Level.INFO, "DIGEST41:Unmatched MACs");
        return kEmpty;
      }
      checkSeq(got);
      seq++;
      return body;
    }
  }

  static byte[] expandDes(byte[] in, int off) {
    byte[] key = new byte[8];
    long bits = 0;
    for (int i = 0; i < 7; i++) {
      bits = (bits << 8) | (in[off + i] & 0xffL);
    }
    for (int i = 0; i < 8; i++) {
      int b = (int) ((bits >>> (49 - 7 * i)) & 0x7f);
      int v = b << 1;
      if ((Integer.bitCount(v) & 1) == 0) {
        v |= 1;
      }
      key[i] = (byte) v;
    }
    return key;
  }
}
