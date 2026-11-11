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
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;

final class NTLMServer implements SaslServer {
  private static final byte[] kSignature = {'N', 'T', 'L', 'M', 'S', 'S', 'P', 0};

  private final boolean v2;
  private final String defaultDomain;
  private final Random random;
  private final CallbackHandler cbh;
  private byte[] challenge;
  private String authzId;
  private int step;

  NTLMServer(String serverName, Map<String, ?> props, CallbackHandler cbh) throws SaslException {
    this.cbh = cbh;
    String version = props == null ? null : (String) props.get("com.sun.security.sasl.ntlm.version");
    if (version == null) {
      version = "LMv2/NTLMv2";
    }
    switch (version) {
      case "LM":
      case "NTLM":
      case "LM/NTLM":
        v2 = false;
        break;
      case "LMv2":
      case "NTLMv2":
      case "LMv2/NTLMv2":
        v2 = true;
        break;
      default:
        throw new SaslException("NTLM: server creation failure",
            new NTLMException(NTLMException.BAD_VERSION, "Unknown version " + version));
    }
    String domain = props == null ? null : (String) props.get("com.sun.security.sasl.ntlm.domain");
    this.defaultDomain = domain != null ? domain : serverName;
    Object r = props == null ? null : props.get("com.sun.security.sasl.ntlm.random");
    this.random = r instanceof Random ? (Random) r : new Random();
  }

  @Override
  public String getMechanismName() {
    return "NTLM";
  }

  @Override
  public boolean isComplete() {
    return step >= 2;
  }

  @Override
  public String getAuthorizationID() {
    if (!isComplete()) {
      throw new IllegalStateException("authentication not complete");
    }
    return authzId;
  }

  @Override
  public byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException {
    throw new IllegalStateException("Not supported yet.");
  }

  @Override
  public byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException {
    throw new IllegalStateException("Not supported yet.");
  }

  @Override
  public Object getNegotiatedProperty(String propName) {
    if (!isComplete()) {
      throw new IllegalStateException("authentication not complete");
    }
    return Sasl.QOP.equals(propName) ? "auth" : null;
  }

  @Override
  public void dispose() {
  }

  @Override
  public byte[] evaluateResponse(byte[] response) throws SaslException {
    try {
      if (step++ == 0) {
        return type2();
      }
      return verify(response);
    } catch (GeneralSecurityException e) {
      throw new SaslException("NTLM: generate response failure", e);
    }
  }

  private static void le(ByteArrayOutputStream o, long v, int n) {
    for (int i = 0; i < n; i++) {
      o.write((int) (v >>> (8 * i)));
    }
  }

  private byte[] type2() {
    challenge = new byte[8];
    random.nextBytes(challenge);
    byte[] target = defaultDomain == null ? new byte[0] : defaultDomain.getBytes(StandardCharsets.UTF_16LE);
    ByteArrayOutputStream o = new ByteArrayOutputStream();
    o.write(kSignature, 0, 8);
    le(o, 2, 4);
    le(o, target.length, 2);
    le(o, target.length, 2);
    le(o, 32, 4);
    le(o, 0x90205, 4);
    o.write(challenge, 0, 8);
    o.write(target, 0, target.length);
    return o.toByteArray();
  }

  private static int u16(byte[] b, int off) throws NTLMException {
    if (off < 0 || off + 2 > b.length) {
      throw new NTLMException(NTLMException.PACKET_READ_ERROR, "Input message incorrect size");
    }
    return (b[off] & 0xff) | ((b[off + 1] & 0xff) << 8);
  }

  private static long u32(byte[] b, int off) throws NTLMException {
    return u16(b, off) | ((long) u16(b, off + 2) << 16);
  }

  private static byte[] field(byte[] b, int off) throws NTLMException {
    int len = u16(b, off);
    long at = u32(b, off + 4);
    if (at + len > b.length) {
      throw new NTLMException(NTLMException.PACKET_READ_ERROR, "Input message incorrect size");
    }
    return Arrays.copyOfRange(b, (int) at, (int) at + len);
  }

  private byte[] verify(byte[] t3) throws SaslException, NTLMException, GeneralSecurityException {
    if (t3 == null || t3.length < 52) {
      throw new NTLMException(NTLMException.PACKET_READ_ERROR, "Input message incorrect size");
    }
    if (!Arrays.equals(Arrays.copyOf(t3, 8), kSignature) || u32(t3, 8) != 3) {
      throw new NTLMException(NTLMException.PACKET_READ_ERROR, "Not a type 3 message");
    }
    byte[] lmResp = field(t3, 12);
    byte[] ntResp = field(t3, 20);
    long flags = t3.length >= 64 ? u32(t3, 60) : 0;
    Charset cs = (flags & 1) != 0 ? StandardCharsets.UTF_16LE : Charset.forName("windows-1252");
    String domain = new String(field(t3, 28), cs);
    String user = new String(field(t3, 36), cs);
    RealmCallback rc = new RealmCallback("Domain: ", domain);
    NameCallback nc = new NameCallback("Name: ", user);
    PasswordCallback pc = new PasswordCallback("Password: ", false);
    try {
      cbh.handle(new Callback[] {rc, nc, pc});
    } catch (IOException | UnsupportedCallbackException e) {
      throw new SaslException("NTLM: Cannot perform callback to acquire realm, username or password", e);
    }
    String realm = rc.getText() != null ? rc.getText() : domain;
    String name = nc.getName() != null ? nc.getName() : user;
    char[] password = pc.getPassword();
    if (password == null) {
      throw new NTLMException(NTLMException.USER_UNKNOWN, "Unknown user");
    }
    byte[] ntHash = Md4.digest(new String(password).getBytes(StandardCharsets.UTF_16LE));
    boolean ok = false;
    if (v2) {
      byte[] key = NTLMClient.hmac(ntHash, (name.toUpperCase(Locale.ROOT) + realm).getBytes(StandardCharsets.UTF_16LE));
      if (lmResp.length == 24) {
        byte[] cc = Arrays.copyOfRange(lmResp, 16, 24);
        ok |= MessageDigest.isEqual(NTLMClient.hmac(key, NTLMClient.concat(challenge, cc)), Arrays.copyOf(lmResp, 16));
      }
      if (ntResp.length > 24) {
        byte[] blob = Arrays.copyOfRange(ntResp, 16, ntResp.length);
        ok |= MessageDigest.isEqual(NTLMClient.hmac(key, NTLMClient.concat(challenge, blob)), Arrays.copyOf(ntResp, 16));
      }
    } else {
      if (lmResp.length == 24) {
        ok |= MessageDigest.isEqual(NTLMClient.des3(NTLMClient.lmHash(password), challenge), lmResp);
      }
      if (ntResp.length == 24) {
        ok |= MessageDigest.isEqual(NTLMClient.des3(ntHash, challenge), ntResp);
      }
    }
    if (!ok) {
      throw new NTLMException(NTLMException.AUTH_FAILED, "None of LM and NTLM verified");
    }
    authzId = name;
    return null;
  }
}
