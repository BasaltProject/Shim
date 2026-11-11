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
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;

final class DigestMD5Server extends DigestMD5Base implements SaslServer {
  private final CallbackHandler cbh;
  private final List<String> realms = new ArrayList<String>();
  private final String[] qopOrder;
  private final boolean offerQop;
  private int step;
  private String nonce;
  private String authorizationId;

  DigestMD5Server(String protocol, String serverName, Map<String, ?> props, CallbackHandler cbh) throws SaslException {
    super(protocol, serverName, props, false);
    this.cbh = cbh;
    Object r = props == null ? null : props.get("com.sun.security.sasl.digest.realm");
    if (r != null) {
      for (String t : String.valueOf(r).split("\\s+")) {
        if (!t.isEmpty()) {
          realms.add(t);
        }
      }
    }
    if (realms.isEmpty()) {
      realms.add(serverName);
    }
    useUtf8 = propTrue(props, "com.sun.security.sasl.digest.utf8", true);
    offerQop = props != null && props.get(Sasl.QOP) != null;
    qopOrder = qopPreference(props);
    recvMaxBuf = propInt(props, Sasl.MAX_BUFFER, 65536);
  }

  @Override
  public String getMechanismName() {
    return kName;
  }

  @Override
  public byte[] evaluateResponse(byte[] response) throws SaslException {
    if (completed) {
      throw new IllegalStateException("DIGEST-MD5 authentication already completed");
    }
    switch (step++) {
    case 0:
      return challenge();
    case 1:
      return authenticate(response);
    default:
      throw new SaslException("DIGEST-MD5: server authentication sequence is complete");
    }
  }

  private byte[] challenge() {
    byte[] n = new byte[16];
    new SecureRandom().nextBytes(n);
    nonce = java.util.Base64.getEncoder().encodeToString(n);
    StringBuilder sb = new StringBuilder();
    for (String realm : realms) {
      sb.append("realm=").append(quote(realm)).append(',');
    }
    sb.append("nonce=").append(quote(nonce));
    boolean conf = false;
    if (offerQop) {
      sb.append(",qop=\"");
      for (int i = 0; i < qopOrder.length; i++) {
        sb.append(i == 0 ? "" : ",").append(qopOrder[i]);
        conf |= qopOrder[i].equals("auth-conf");
      }
      sb.append('"');
    }
    if (useUtf8) {
      sb.append(",charset=utf-8");
    }
    if (conf) {
      List<String> offered = new ArrayList<String>();
      for (String[] group : kStrengthGroups) {
        for (String c : group) {
          offered.add(c);
        }
      }
      sb.append(",cipher=\"").append(String.join(",", offered)).append('"');
    }
    sb.append(",algorithm=md5-sess");
    return sb.toString().getBytes(StandardCharsets.ISO_8859_1);
  }

  private byte[] authenticate(byte[] response) throws SaslException {
    String username = null;
    String realm = "";
    String rnonce = null;
    String nc = null;
    String cnonce = null;
    String digestUri = null;
    String resp = null;
    String qop = "auth";
    String cipher = null;
    String authzid = null;
    int clientMax = 65536;
    boolean clientUtf8 = false;
    for (String[] d : parse(response, StandardCharsets.ISO_8859_1)) {
      switch (d[0]) {
      case "username":
        username = d[1];
        break;
      case "realm":
        realm = d[1];
        break;
      case "nonce":
        rnonce = d[1];
        break;
      case "nc":
        nc = d[1];
        break;
      case "cnonce":
        cnonce = d[1];
        break;
      case "digest-uri":
        digestUri = d[1];
        break;
      case "response":
        resp = d[1];
        break;
      case "qop":
        qop = d[1].toLowerCase(java.util.Locale.ROOT);
        break;
      case "cipher":
        cipher = d[1];
        break;
      case "authzid":
        authzid = d[1];
        break;
      case "maxbuf":
        clientMax = Integer.parseInt(d[1]);
        break;
      case "charset":
        clientUtf8 = d[1].equalsIgnoreCase("utf-8");
        break;
      default:
        break;
      }
    }
    if (clientUtf8 && useUtf8) {
      if (username != null) {
        username = new String(username.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
      }
      if (authzid != null) {
        authzid = new String(authzid.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
      }
      realm = new String(realm.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    } else {
      useUtf8 = false;
    }
    if (username == null || rnonce == null || cnonce == null || digestUri == null || resp == null) {
      throw new SaslException("DIGEST-MD5: digest response format violation. Missing required directive");
    }
    if (!rnonce.equals(nonce)) {
      throw new SaslException("DIGEST-MD5: digest response format violation. Mismatched nonce.");
    }
    if (nc == null || !nc.equals("00000001")) {
      throw new SaslException("DIGEST-MD5: digest response format violation. Nonce count must be 00000001.");
    }
    if (!realms.contains(realm)) {
      throw new SaslException("DIGEST-MD5: digest response format violation. Nonexistent realm: " + realm);
    }
    if (!digestUri.equalsIgnoreCase(protocol + "/" + serverName)) {
      throw new SaslException("DIGEST-MD5: digest response format violation. Mismatched URI: " + digestUri
          + "; expecting: " + protocol + "/" + serverName);
    }
    int layer;
    boolean permitted = false;
    for (String q : qopOrder) {
      permitted |= q.equals(qop);
    }
    if (!permitted) {
      throw new SaslException("DIGEST-MD5: digest response format violation. Unexpected qop: " + qop);
    }
    layer = qop.equals("auth") ? 0 : qop.equals("auth-int") ? 1 : 2;
    if (layer == 2 && (cipher == null || strengthOf(cipher) == null)) {
      throw new SaslException("DIGEST-MD5: digest response format violation. Unexpected cipher: " + cipher);
    }
    byte[] password;
    try {
      RealmCallback rc = new RealmCallback("DIGEST-MD5 realm: ", realm);
      NameCallback nameCb = new NameCallback("DIGEST-MD5 authentication ID: ", username);
      PasswordCallback pc = new PasswordCallback("DIGEST-MD5 password: ", false);
      cbh.handle(new Callback[] {rc, nameCb, pc});
      char[] pw = pc.getPassword();
      if (pw == null) {
        throw new SaslException("DIGEST-MD5: cannot acquire password for " + username + " in realm : " + realm);
      }
      password = new String(pw).getBytes(charset());
      pc.clearPassword();
    } catch (SaslException e) {
      throw e;
    } catch (UnsupportedCallbackException e) {
      throw new SaslException("DIGEST-MD5: Cannot perform callback to acquire password", e);
    } catch (IOException e) {
      throw new SaslException("DIGEST-MD5: IO error acquiring password", e);
    }
    byte[] a1Head = md5(bytes(username + ":" + realm + ":", charset()), password);
    byte[] a1 = concat(a1Head, bytes(":" + nonce + ":" + cnonce, StandardCharsets.UTF_8),
        authzid == null ? kEmpty : bytes(":" + authzid, charset()));
    ha1 = md5(a1);
    String expected = response(ha1, nonce, nc, cnonce, qop, a2(true, digestUri, qop));
    if (!expected.equals(resp)) {
      throw new SaslException("DIGEST-MD5: digest response format violation. Mismatched response.");
    }
    try {
      AuthorizeCallback ac = new AuthorizeCallback(username, authzid == null ? username : authzid);
      cbh.handle(new Callback[] {ac});
      if (!ac.isAuthorized()) {
        authorizationId = null;
        throw new SaslException("DIGEST-MD5: " + username + " is not authorized to connect as " + authzid);
      }
      String a = ac.getAuthorizedID();
      authorizationId = a != null ? a : (authzid == null ? username : authzid);
    } catch (SaslException e) {
      throw e;
    } catch (UnsupportedCallbackException e) {
      throw new SaslException("DIGEST-MD5: Cannot perform callback to check authorization", e);
    } catch (IOException e) {
      throw new SaslException("DIGEST-MD5: IO error checking authorization", e);
    }
    sendMaxBuf = clientMax;
    String rspauth = "rspauth=" + response(ha1, nonce, nc, cnonce, qop, a2(false, digestUri, qop));
    setupLayers(layer, cipher);
    completed = true;
    return rspauth.getBytes(StandardCharsets.ISO_8859_1);
  }

  @Override
  public String getAuthorizationID() {
    if (!completed) {
      throw new IllegalStateException("DIGEST-MD5 authentication not completed");
    }
    return authorizationId;
  }
}
