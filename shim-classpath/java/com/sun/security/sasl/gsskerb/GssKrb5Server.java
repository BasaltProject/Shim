/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package com.sun.security.sasl.gsskerb;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.MessageProp;
import org.ietf.jgss.Oid;

final class GssKrb5Server extends GssKrb5Base implements SaslServer {
  private final CallbackHandler cbh;
  private final String serverName;
  private boolean layerSent;
  private String authzId;

  GssKrb5Server(String protocol, String serverName, Map<String, ?> props, CallbackHandler cbh)
      throws SaslException {
    this.cbh = cbh;
    this.serverName = serverName;
    qopList = parseQop(props == null ? null : (String) props.get(Sasl.QOP));
    recvMaxBuf = intProp(props, Sasl.MAX_BUFFER, 65536);
    sendMaxBuf = intProp(props, "javax.security.sasl.sendmaxbuffer", 65536);
    try {
      GSSManager m = GSSManager.getInstance();
      GSSCredential cred;
      if (props != null && props.get(Sasl.CREDENTIALS) != null) {
        Object c = props.get(Sasl.CREDENTIALS);
        if (!(c instanceof GSSCredential)) {
          throw new SaslException("JGSS Credentials must be of type GSSCredential");
        }
        cred = (GSSCredential) c;
      } else {
        GSSName name = serverName == null ? null : m.createName(protocol + "@" + serverName,
            GSSName.NT_HOSTBASED_SERVICE);
        cred = m.createCredential(name, GSSCredential.INDEFINITE_LIFETIME, new Oid("1.2.840.113554.1.2.2"),
            GSSCredential.ACCEPT_ONLY);
      }
      secCtx = m.createContext(cred);
    } catch (GSSException e) {
      throw new SaslException("Failure to initialize security context", e);
    }
  }

  public byte[] evaluateResponse(byte[] response) throws SaslException {
    if (completed) {
      throw new IllegalStateException("GSSAPI authentication already complete");
    }
    try {
      if (!secCtx.isEstablished()) {
        byte[] token = secCtx.acceptSecContext(response, 0, response.length);
        if (secCtx.isEstablished() && token == null) {
          return sendLayers();
        }
        return token;
      }
      if (!layerSent) {
        return sendLayers();
      }
      return finish(response);
    } catch (GSSException e) {
      throw new SaslException("GSS initiate failed", e);
    }
  }

  private byte[] sendLayers() throws SaslException {
    int mask = 0;
    for (int q : qopList) {
      mask |= q;
    }
    layerSent = true;
    return layerMessage(mask, recvMaxBuf);
  }

  private byte[] finish(byte[] response) throws SaslException, GSSException {
    byte[] msg = secCtx.unwrap(response, 0, response.length, new MessageProp(0, false));
    if (msg.length < 4) {
      throw new SaslException("Invalid protection negotiation message length: " + msg.length);
    }
    int layer = msg[0] & 0xff;
    boolean allowed = false;
    for (int q : qopList) {
      allowed |= q == layer;
    }
    if (!allowed) {
      throw new SaslException("Unsupported protection level selected by the client: " + layer);
    }
    int peerMax = ((msg[1] & 0xff) << 16) | ((msg[2] & 0xff) << 8) | (msg[3] & 0xff);
    select(layer, peerMax);
    String authnId = secCtx.getSrcName().toString();
    String requested = msg.length > 4 ? new String(msg, 4, msg.length - 4, StandardCharsets.UTF_8) : authnId;
    try {
      AuthorizeCallback ac = new AuthorizeCallback(authnId, requested);
      cbh.handle(new Callback[] {ac});
      if (!ac.isAuthorized()) {
        throw new SaslException(authnId + " is not authorized to connect as " + requested);
      }
      authzId = ac.getAuthorizedID();
    } catch (SaslException e) {
      throw new SaslException("Problem with callback handler", e);
    } catch (IOException | UnsupportedCallbackException e) {
      throw new SaslException("Problem with callback handler", e);
    }
    completed = true;
    return null;
  }

  public String getAuthorizationID() {
    if (!completed) {
      throw new IllegalStateException("GSSAPI authentication not completed");
    }
    return authzId;
  }

  public Object getNegotiatedProperty(String propName) {
    Object v = super.getNegotiatedProperty(propName);
    if (v == null && Sasl.BOUND_SERVER_NAME.equals(propName)) {
      return serverName;
    }
    return v;
  }
}
