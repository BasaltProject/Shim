/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.sasl.digest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.RealmChoiceCallback;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

final class DigestMD5Client extends DigestMD5Base implements SaslClient {
  private final String authzid;
  private final CallbackHandler cbh;
  private int step;
  private String expectedRspauth;

  DigestMD5Client(String authzid, String protocol, String serverName, Map<String, ?> props, CallbackHandler cbh)
      throws SaslException {
    super(protocol, serverName, props, true);
    this.authzid = authzid;
    this.cbh = cbh;
    if (cbh == null) {
      throw new SaslException("Callback handler with support for RealmChoiceCallback, RealmCallback, NameCallback, "
          + "and PasswordCallback required");
    }
    sendMaxBuf = 65536;
    recvMaxBuf = propInt(props, Sasl.MAX_BUFFER, 65536);
  }

  @Override
  public String getMechanismName() {
    return kName;
  }

  @Override
  public boolean hasInitialResponse() {
    return false;
  }

  @Override
  public byte[] evaluateChallenge(byte[] challenge) throws SaslException {
    if (completed) {
      throw new IllegalStateException("DIGEST-MD5 authentication already completed");
    }
    switch (step++) {
    case 0:
      if (challenge.length == 0) {
        throw new SaslException("DIGEST-MD5 must begin with a challenge from the server");
      }
      return respond(challenge);
    case 1:
      return verify(challenge);
    default:
      throw new SaslException("DIGEST-MD5: Authentication sequence is complete");
    }
  }

  private byte[] verify(byte[] challenge) throws SaslException {
    for (String[] d : parse(challenge, charset())) {
      if (d[0].equals("rspauth")) {
        if (!d[1].equals(expectedRspauth)) {
          throw new SaslException("DIGEST-MD5: Invalid server response: rspauth mismatch");
        }
        completed = true;
        return null;
      }
    }
    throw new SaslException("DIGEST-MD5: Authentication sequence is complete: no rspauth");
  }

  private byte[] respond(byte[] challenge) throws SaslException {
    List<String> realms = new ArrayList<String>();
    String nonce = null;
    List<String> qops = new ArrayList<String>();
    List<String> ciphers = new ArrayList<String>();
    boolean utf8 = false;
    int serverMax = 65536;
    for (String[] d : parse(challenge, StandardCharsets.ISO_8859_1)) {
      switch (d[0]) {
      case "realm":
        realms.add(d[1]);
        break;
      case "nonce":
        nonce = d[1];
        break;
      case "qop":
        for (String q : d[1].split("[,\\s]+")) {
          if (!q.isEmpty()) {
            qops.add(q.toLowerCase(java.util.Locale.ROOT));
          }
        }
        break;
      case "cipher":
        for (String c : d[1].split("[,\\s]+")) {
          if (!c.isEmpty()) {
            ciphers.add(c);
          }
        }
        break;
      case "charset":
        if (d[1].equalsIgnoreCase("utf-8")) {
          utf8 = true;
        } else {
          throw new SaslException("DIGEST-MD5: digest challenge format violation. Unrecognised charset value: "
              + d[1]);
        }
        break;
      case "maxbuf":
        serverMax = Integer.parseInt(d[1]);
        break;
      default:
        break;
      }
    }
    if (nonce == null) {
      throw new SaslException("DIGEST-MD5: digest challenge format violation. Nonce missing");
    }
    if (qops.isEmpty()) {
      qops.add("auth");
    }
    useUtf8 = utf8;
    sendMaxBuf = serverMax;

    String realm;
    String username;
    byte[] password;
    try {
      if (realms.size() > 1) {
        RealmChoiceCallback rcc = new RealmChoiceCallback("DIGEST-MD5 realm: ", realms.toArray(new String[0]), 0,
            false);
        cbh.handle(new Callback[] {rcc});
        int[] sel = rcc.getSelectedIndexes();
        realm = sel == null || sel.length == 0 ? realms.get(0) : realms.get(sel[0]);
      } else {
        RealmCallback rc = realms.size() == 1 ? new RealmCallback("DIGEST-MD5 realm: ", realms.get(0))
            : new RealmCallback("DIGEST-MD5 realm: ");
        cbh.handle(new Callback[] {rc});
        realm = rc.getText();
        if (realm == null) {
          realm = realms.isEmpty() ? "" : realms.get(0);
        }
      }
      NameCallback nc = authzid == null ? new NameCallback("DIGEST-MD5 authentication ID: ")
          : new NameCallback("DIGEST-MD5 authentication ID: ", authzid);
      PasswordCallback pc = new PasswordCallback("DIGEST-MD5 password: ", false);
      cbh.handle(new Callback[] {nc, pc});
      username = nc.getName();
      char[] pw = pc.getPassword();
      if (username == null || pw == null) {
        throw new SaslException("DIGEST-MD5: authentication ID and password must be specified");
      }
      password = new String(pw).getBytes(charset());
      pc.clearPassword();
    } catch (SaslException e) {
      throw e;
    } catch (UnsupportedCallbackException e) {
      throw new SaslException("DIGEST-MD5: Cannot perform callback to acquire realm, authentication ID or password", e);
    } catch (IOException e) {
      throw new SaslException("DIGEST-MD5: Error acquiring realm, authentication ID or password", e);
    }

    int layer = -1;
    for (String want : qopPreference(props)) {
      if (qops.contains(want)) {
        layer = want.equals("auth") ? 0 : want.equals("auth-int") ? 1 : 2;
        break;
      }
    }
    if (layer < 0) {
      throw new SaslException("DIGEST-MD5: No common protection layer between client and server");
    }
    String cipher = null;
    if (layer == 2) {
      outer:
      for (String strength : strengthPreference(props)) {
        for (int g = 0; g < kStrengthNames.length; g++) {
          if (!kStrengthNames[g].equals(strength)) {
            continue;
          }
          for (String c : kStrengthGroups[g]) {
            if (ciphers.contains(c)) {
              cipher = c;
              break outer;
            }
          }
        }
      }
      if (cipher == null) {
        throw new SaslException("DIGEST-MD5: server did not offer a cipher with the requested strength");
      }
    }
    String qop = layer == 0 ? "auth" : layer == 1 ? "auth-int" : "auth-conf";

    byte[] cn = new byte[16];
    new SecureRandom().nextBytes(cn);
    String cnonce = java.util.Base64.getEncoder().encodeToString(cn);
    String nc = "00000001";
    String digestUri = protocol + "/" + serverName;
    byte[] a1Head = md5(bytes(username + ":" + realm + ":", charset()), password);
    byte[] a1 = concat(a1Head, bytes(":" + nonce + ":" + cnonce, StandardCharsets.UTF_8),
        authzid == null ? kEmpty : bytes(":" + authzid, charset()));
    ha1 = md5(a1);
    String resp = response(ha1, nonce, nc, cnonce, qop, a2(true, digestUri, qop));
    expectedRspauth = response(ha1, nonce, nc, cnonce, qop, a2(false, digestUri, qop));
    StringBuilder out = new StringBuilder();
    if (utf8) {
      out.append("charset=utf-8,");
    }
    out.append("username=").append(quote(username));
    out.append(",realm=").append(quote(realm));
    out.append(",nonce=").append(quote(nonce));
    out.append(",nc=").append(nc);
    out.append(",cnonce=").append(quote(cnonce));
    out.append(",digest-uri=").append(quote(digestUri));
    out.append(",maxbuf=").append(recvMaxBuf);
    out.append(",response=").append(resp);
    out.append(",qop=").append(qop);
    if (cipher != null) {
      out.append(",cipher=").append(quote(cipher));
    }
    if (authzid != null) {
      out.append(",authzid=").append(quote(authzid));
    }
    setupLayers(layer, cipher);
    return out.toString().getBytes(charset());
  }
}
