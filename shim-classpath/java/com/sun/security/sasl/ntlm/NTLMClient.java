/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.sasl.ntlm;

import com.sun.security.ntlm.NTLMException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

final class NTLMClient implements SaslClient {
  private static final byte[] kSignature = {'N', 'T', 'L', 'M', 'S', 'S', 'P', 0};

  private final String hostname;
  private final String defaultDomain;
  private final boolean lm;
  private final boolean ntlm;
  private final boolean lmv2;
  private final boolean ntlmv2;
  private final Random random;
  private final String realm;
  private final String user;
  private final char[] password;
  private int step;

  NTLMClient(String serverName, Map<String, ?> props, CallbackHandler cbh) throws SaslException {
    String version = props == null ? null : (String) props.get("com.sun.security.sasl.ntlm.version");
    if (version == null) {
      version = "LMv2/NTLMv2";
    }
    switch (version) {
      case "LM": lm = true; ntlm = false; lmv2 = false; ntlmv2 = false; break;
      case "NTLM": lm = false; ntlm = true; lmv2 = false; ntlmv2 = false; break;
      case "LM/NTLM": lm = true; ntlm = true; lmv2 = false; ntlmv2 = false; break;
      case "LMv2": lm = false; ntlm = false; lmv2 = true; ntlmv2 = false; break;
      case "NTLMv2": lm = false; ntlm = false; lmv2 = false; ntlmv2 = true; break;
      case "LMv2/NTLMv2": lm = false; ntlm = false; lmv2 = true; ntlmv2 = true; break;
      default:
        throw new SaslException("NTLM: client creation failure",
            new NTLMException(NTLMException.BAD_VERSION, "Unknown version " + version));
    }
    this.defaultDomain = props == null ? null : (String) props.get("com.sun.security.sasl.ntlm.domain");
    String host = props == null ? null : (String) props.get("com.sun.security.sasl.ntlm.hostname");
    if (host == null) {
      try {
        host = java.net.InetAddress.getLocalHost().getCanonicalHostName();
      } catch (java.net.UnknownHostException e) {
        host = "localhost";
      }
    }
    this.hostname = host;
    Object r = props == null ? null : props.get("com.sun.security.sasl.ntlm.random");
    this.random = r instanceof Random ? (Random) r : new Random();
    RealmCallback rc = new RealmCallback("Realm: ", defaultDomain != null ? defaultDomain : serverName);
    NameCallback nc = new NameCallback("User name: ");
    PasswordCallback pc = new PasswordCallback("Password: ", false);
    try {
      cbh.handle(new Callback[] {rc, nc, pc});
    } catch (IOException | UnsupportedCallbackException e) {
      throw new SaslException("NTLM: Cannot perform callback to acquire realm, username or password", e);
    }
    this.realm = rc.getText() != null ? rc.getText() : defaultDomain;
    this.user = nc.getName();
    this.password = pc.getPassword();
  }

  @Override
  public String getMechanismName() {
    return "NTLM";
  }

  @Override
  public boolean hasInitialResponse() {
    return true;
  }

  @Override
  public boolean isComplete() {
    return step >= 2;
  }

  @Override
  public byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException {
    throw new IllegalStateException("Not supported");
  }

  @Override
  public byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException {
    throw new IllegalStateException("Not supported");
  }

  @Override
  public Object getNegotiatedProperty(String propName) {
    if (step < 2) {
      throw new IllegalStateException("authentication not completed");
    }
    return null;
  }

  @Override
  public void dispose() {
  }

  private static void le(ByteArrayOutputStream o, long v, int n) {
    for (int i = 0; i < n; i++) {
      o.write((int) (v >>> (8 * i)));
    }
  }

  @Override
  public byte[] evaluateChallenge(byte[] challenge) throws SaslException {
    if (step == 0) {
      step = 1;
      ByteArrayOutputStream o = new ByteArrayOutputStream();
      o.write(kSignature, 0, 8);
      le(o, 1, 4);
      le(o, 0x8207 | ((lmv2 || ntlmv2) ? 0x80000 : 0), 4);
      le(o, 0, 16);
      return o.toByteArray();
    }
    byte[] out = type3(challenge);
    step = 2;
    return out;
  }

  private byte[] type3(byte[] t2) throws SaslException {
    if (t2 == null || t2.length < 32) {
      throw new SaslException("Type3 creation failed",
          new NTLMException(NTLMException.PACKET_READ_ERROR, "Input message incorrect size"));
    }
    if (!Arrays.equals(Arrays.copyOf(t2, 8), kSignature) || t2[8] != 2) {
      throw new SaslException("Type3 creation failed",
          new NTLMException(NTLMException.PACKET_READ_ERROR, "Not a type 2 message"));
    }
    int flags = (t2[20] & 0xff) | ((t2[21] & 0xff) << 8) | ((t2[22] & 0xff) << 16) | ((t2[23] & 0xff) << 24);
    byte[] serverChallenge = Arrays.copyOfRange(t2, 24, 32);
    Charset cs = (flags & 1) != 0 ? StandardCharsets.UTF_16LE : Charset.forName("windows-1252");
    String domain = realm;
    if (user == null || password == null) {
      throw new SaslException("NTLM: username or password not provided");
    }
    if (domain == null) {
      domain = "";
    }
    try {
      byte[] ntHash = Md4.digest(new String(password).getBytes(StandardCharsets.UTF_16LE));
      byte[] lmResp = new byte[0];
      byte[] ntResp = new byte[0];
      if (lm) {
        lmResp = des3(lmHash(password), serverChallenge);
      }
      if (ntlm) {
        ntResp = des3(ntHash, serverChallenge);
      }
      byte[] clientChallenge = new byte[8];
      random.nextBytes(clientChallenge);
      if (lmv2 || ntlmv2) {
        byte[] v2 = hmac(ntHash, (user.toUpperCase(Locale.ROOT) + domain).getBytes(StandardCharsets.UTF_16LE));
        if (lmv2) {
          byte[] sig = hmac(v2, concat(serverChallenge, clientChallenge));
          lmResp = concat(sig, clientChallenge);
        }
        if (ntlmv2) {
          ByteArrayOutputStream blob = new ByteArrayOutputStream();
          le(blob, 0x0101, 4);
          le(blob, 0, 4);
          le(blob, (System.currentTimeMillis() + 11644473600000L) * 10000L, 8);
          blob.write(clientChallenge, 0, 8);
          le(blob, 0, 8);
          byte[] b = blob.toByteArray();
          ntResp = concat(hmac(v2, concat(serverChallenge, b)), b);
        }
      }
      byte[] d = domain.getBytes(cs);
      byte[] u = user.getBytes(cs);
      byte[] h = hostname.getBytes(cs);
      int base = 64;
      int lmOff = base + d.length + u.length + h.length;
      int ntOff = lmOff + lmResp.length;
      int keyOff = ntOff + ntResp.length;
      ByteArrayOutputStream o = new ByteArrayOutputStream();
      o.write(kSignature, 0, 8);
      le(o, 3, 4);
      field(o, lmResp.length, lmOff);
      field(o, ntResp.length, ntOff);
      field(o, d.length, base);
      field(o, u.length, base + d.length);
      field(o, h.length, base + d.length + u.length);
      field(o, 0, keyOff);
      le(o, 0x88200 | ((flags & 1) != 0 ? 1 : 2), 4);
      o.write(d, 0, d.length);
      o.write(u, 0, u.length);
      o.write(h, 0, h.length);
      o.write(lmResp, 0, lmResp.length);
      o.write(ntResp, 0, ntResp.length);
      return o.toByteArray();
    } catch (GeneralSecurityException e) {
      throw new SaslException("NTLM: cannot compute response", e);
    }
  }

  private static void field(ByteArrayOutputStream o, int len, int off) {
    le(o, len, 2);
    le(o, len, 2);
    le(o, off, 4);
  }

  static byte[] concat(byte[] a, byte[] b) {
    byte[] r = Arrays.copyOf(a, a.length + b.length);
    System.arraycopy(b, 0, r, a.length, b.length);
    return r;
  }

  static byte[] hmac(byte[] key, byte[] data) throws GeneralSecurityException {
    Mac m = Mac.getInstance("HmacMD5");
    m.init(new SecretKeySpec(key, "HmacMD5"));
    return m.doFinal(data);
  }

  private static byte[] desKey(byte[] k7, int off) {
    byte[] k = new byte[8];
    k[0] = (byte) (k7[off] & 0xfe);
    k[1] = (byte) ((k7[off] << 7) | ((k7[off + 1] & 0xff) >>> 1));
    k[2] = (byte) ((k7[off + 1] << 6) | ((k7[off + 2] & 0xff) >>> 2));
    k[3] = (byte) ((k7[off + 2] << 5) | ((k7[off + 3] & 0xff) >>> 3));
    k[4] = (byte) ((k7[off + 3] << 4) | ((k7[off + 4] & 0xff) >>> 4));
    k[5] = (byte) ((k7[off + 4] << 3) | ((k7[off + 5] & 0xff) >>> 5));
    k[6] = (byte) ((k7[off + 5] << 2) | ((k7[off + 6] & 0xff) >>> 6));
    k[7] = (byte) (k7[off + 6] << 1);
    return k;
  }

  private static byte[] desEncrypt(byte[] key8, byte[] data) throws GeneralSecurityException {
    Cipher c = Cipher.getInstance("DES/ECB/NoPadding");
    c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key8, "DES"));
    return c.doFinal(data);
  }

  static byte[] lmHash(char[] password) throws GeneralSecurityException {
    byte[] pw = new byte[14];
    byte[] src = new String(password).toUpperCase(Locale.ROOT).getBytes(Charset.forName("windows-1252"));
    System.arraycopy(src, 0, pw, 0, Math.min(14, src.length));
    byte[] magic = "KGS!@#$%".getBytes(StandardCharsets.US_ASCII);
    return concat(desEncrypt(desKey(pw, 0), magic), desEncrypt(desKey(pw, 7), magic));
  }

  static byte[] des3(byte[] hash16, byte[] challenge) throws GeneralSecurityException {
    byte[] k = Arrays.copyOf(hash16, 21);
    return concat(concat(desEncrypt(desKey(k, 0), challenge), desEncrypt(desKey(k, 7), challenge)),
        desEncrypt(desKey(k, 14), challenge));
  }
}
