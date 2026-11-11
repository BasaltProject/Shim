/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package com.sun.security.sasl.gsskerb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.MessageProp;

abstract class GssKrb5Base {
  static final int kNone = 1;
  static final int kInteg = 2;
  static final int kConf = 4;

  protected GSSContext secCtx;
  protected boolean completed;
  protected boolean integrity;
  protected boolean privacy;
  protected int selectedLayer = kNone;
  protected int recvMaxBuf = 65536;
  protected int sendMaxBuf = 65536;
  protected int rawSendSize;
  protected List<Integer> qopList = new ArrayList<Integer>();

  static List<Integer> parseQop(String s) throws SaslException {
    List<Integer> out = new ArrayList<Integer>();
    if (s == null) {
      out.add(kNone);
      return out;
    }
    for (String t : s.split("[,\\s]+")) {
      if (t.isEmpty()) {
        continue;
      }
      if (t.equals("auth")) {
        out.add(kNone);
      } else if (t.equals("auth-int")) {
        out.add(kInteg);
      } else if (t.equals("auth-conf")) {
        out.add(kConf);
      } else {
        throw new SaslException("Invalid token in " + Sasl.QOP + ": " + s);
      }
    }
    if (out.isEmpty()) {
      out.add(kNone);
    }
    return out;
  }

  static int intProp(Map<String, ?> props, String key, int dflt) throws SaslException {
    Object v = props == null ? null : props.get(key);
    if (v == null) {
      return dflt;
    }
    try {
      return Integer.parseInt(v.toString());
    } catch (NumberFormatException e) {
      throw new SaslException("Property must be string representation of integer: " + key);
    }
  }

  public String getMechanismName() {
    return "GSSAPI";
  }

  public boolean isComplete() {
    return completed;
  }

  public Object getNegotiatedProperty(String propName) {
    if (!completed) {
      throw new IllegalStateException("Authentication incomplete");
    }
    switch (propName) {
      case Sasl.QOP:
        return selectedLayer == kConf ? "auth-conf" : selectedLayer == kInteg ? "auth-int" : "auth";
      case Sasl.MAX_BUFFER:
        return Integer.toString(recvMaxBuf);
      case Sasl.RAW_SEND_SIZE:
        return Integer.toString(rawSendSize);
      default:
        return null;
    }
  }

  public byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException {
    if (!completed) {
      throw new IllegalStateException("GSSAPI authentication not completed");
    }
    if (!integrity) {
      throw new IllegalStateException("No security layer negotiated");
    }
    try {
      return secCtx.wrap(outgoing, offset, len, new MessageProp(0, privacy));
    } catch (GSSException e) {
      throw new SaslException("Problem performing GSS wrap", e);
    }
  }

  public byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException {
    if (!completed) {
      throw new IllegalStateException("GSSAPI authentication not completed");
    }
    if (!integrity) {
      throw new IllegalStateException("No security layer negotiated");
    }
    try {
      return secCtx.unwrap(incoming, offset, len, new MessageProp(0, privacy));
    } catch (GSSException e) {
      throw new SaslException("Problem performing GSS unwrap", e);
    }
  }

  public void dispose() throws SaslException {
    if (secCtx != null) {
      try {
        secCtx.dispose();
      } catch (GSSException e) {
        throw new SaslException("Problem disposing GSS context", e);
      }
      secCtx = null;
    }
  }

  protected void select(int layer, int peerMax) throws SaslException {
    selectedLayer = layer;
    integrity = layer != kNone;
    privacy = layer == kConf;
    try {
      rawSendSize = secCtx.getWrapSizeLimit(0, privacy, Math.min(peerMax, sendMaxBuf));
    } catch (GSSException e) {
      throw new SaslException("Problem computing the wrap size", e);
    }
  }

  protected byte[] layerMessage(int mask, int maxBuf) throws SaslException {
    byte[] m = new byte[4];
    m[0] = (byte) mask;
    m[1] = (byte) (maxBuf >> 16);
    m[2] = (byte) (maxBuf >> 8);
    m[3] = (byte) maxBuf;
    try {
      return secCtx.wrap(m, 0, 4, new MessageProp(0, false));
    } catch (GSSException e) {
      throw new SaslException("Problem performing GSS wrap", e);
    }
  }
}
