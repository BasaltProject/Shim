/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package com.sun.security.sasl.gsskerb;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.MessageProp;
import org.ietf.jgss.Oid;

final class GssKrb5Client extends GssKrb5Base implements SaslClient {
  private final String authzId;

  GssKrb5Client(String authzId, String protocol, String serverName, Map<String, ?> props, CallbackHandler cbh)
      throws SaslException {
    this.authzId = authzId == null ? "" : authzId;
    qopList = parseQop(props == null ? null : (String) props.get(Sasl.QOP));
    recvMaxBuf = intProp(props, Sasl.MAX_BUFFER, 65536);
    sendMaxBuf = intProp(props, "javax.security.sasl.sendmaxbuffer", 65536);
    boolean serverAuth = props != null && "true".equalsIgnoreCase(String.valueOf(props.get(Sasl.SERVER_AUTH)));
    boolean layers = false;
    for (int q : qopList) {
      layers |= q != kNone;
    }
    GSSCredential cred = null;
    if (props != null && props.get(Sasl.CREDENTIALS) != null) {
      Object c = props.get(Sasl.CREDENTIALS);
      if (!(c instanceof GSSCredential)) {
        throw new SaslException("JGSS Credentials must be of type GSSCredential");
      }
      cred = (GSSCredential) c;
    }
    try {
      GSSManager m = GSSManager.getInstance();
      GSSName target = m.createName(protocol + "@" + serverName, GSSName.NT_HOSTBASED_SERVICE);
      secCtx = m.createContext(target, new Oid("1.2.840.113554.1.2.2"), cred, GSSContext.INDEFINITE_LIFETIME);
      secCtx.requestSequenceDet(true);
      secCtx.requestReplayDet(true);
      secCtx.requestMutualAuth(serverAuth || layers);
      secCtx.requestConf(true);
      secCtx.requestInteg(true);
    } catch (GSSException e) {
      throw new SaslException("Failure to initialize security context", e);
    }
  }

  public boolean hasInitialResponse() {
    return true;
  }

  public byte[] evaluateChallenge(byte[] challenge) throws SaslException {
    if (completed) {
      throw new IllegalStateException("GSSAPI authentication already complete");
    }
    try {
      if (!secCtx.isEstablished()) {
        byte[] token = secCtx.initSecContext(challenge, 0, challenge.length);
        return token == null ? new byte[0] : token;
      }
      MessageProp prop = new MessageProp(0, false);
      byte[] msg = secCtx.unwrap(challenge, 0, challenge.length, prop);
      if (msg.length != 4) {
        throw new SaslException("Invalid protection negotiation message length: " + msg.length);
      }
      int mask = msg[0] & 0xff;
      int peerMax = ((msg[1] & 0xff) << 16) | ((msg[2] & 0xff) << 8) | (msg[3] & 0xff);
      int layer = 0;
      for (int q : qopList) {
        if ((mask & q) != 0) {
          layer = q;
          break;
        }
      }
      if (layer == 0) {
        throw new SaslException("No common protection layer between client and server");
      }
      select(layer, peerMax);
      byte[] authz = authzId.getBytes(StandardCharsets.UTF_8);
      byte[] resp = new byte[4 + authz.length];
      resp[0] = (byte) layer;
      int max = layer == kNone ? 0 : recvMaxBuf;
      resp[1] = (byte) (max >> 16);
      resp[2] = (byte) (max >> 8);
      resp[3] = (byte) max;
      System.arraycopy(authz, 0, resp, 4, authz.length);
      byte[] out = secCtx.wrap(resp, 0, resp.length, new MessageProp(0, false));
      completed = true;
      return out;
    } catch (GSSException e) {
      throw new SaslException("GSS initiate failed", e);
    }
  }
}
