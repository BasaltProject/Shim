/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.ldap;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.CommunicationException;
import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.Control;
import javax.naming.spi.InitialContextFactory;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.RealmChoiceCallback;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

public final class LdapCtxFactory implements InitialContextFactory {
  @Override
  public Context getInitialContext(Hashtable<?, ?> envIn) throws NamingException {
    @SuppressWarnings("unchecked")
    Hashtable<Object, Object> env = envIn == null ? new Hashtable<Object, Object>()
        : (Hashtable<Object, Object>) envIn.clone();
    String urls = (String) env.get(Context.PROVIDER_URL);
    if (urls == null || urls.trim().isEmpty()) {
      urls = "ldap://localhost:389/";
    }
    NamingException last = null;
    for (String u : urls.trim().split("\\s+")) {
      try {
        return open(u, env);
      } catch (CommunicationException e) {
        last = e;
      }
    }
    throw last;
  }

  static LdapCtx open(String url, Hashtable<Object, Object> env) throws NamingException {
    boolean ssl;
    String rest;
    if (url.regionMatches(true, 0, "ldap://", 0, 7)) {
      ssl = false;
      rest = url.substring(7);
    } else if (url.regionMatches(true, 0, "ldaps://", 0, 8)) {
      ssl = true;
      rest = url.substring(8);
    } else {
      throw new ConfigurationException("Invalid LDAP URL: " + url);
    }
    if ("ssl".equals(env.get(Context.SECURITY_PROTOCOL))) {
      ssl = true;
    }
    int slash = rest.indexOf('/');
    String hostPort = slash < 0 ? rest : rest.substring(0, slash);
    String path = slash < 0 ? "" : rest.substring(slash + 1);
    int q = path.indexOf('?');
    if (q >= 0) {
      path = path.substring(0, q);
    }
    String dn = URLDecoder.decode(path.replace("+", "%2B"), StandardCharsets.UTF_8);
    String host = hostPort;
    int port = ssl ? 636 : 389;
    if (hostPort.startsWith("[")) {
      int end = hostPort.indexOf(']');
      host = hostPort.substring(1, end);
      if (hostPort.length() > end + 2) {
        port = Integer.parseInt(hostPort.substring(end + 2));
      }
    } else if (hostPort.indexOf(':') >= 0) {
      host = hostPort.substring(0, hostPort.indexOf(':'));
      port = Integer.parseInt(hostPort.substring(hostPort.indexOf(':') + 1));
    }
    if (host.isEmpty()) {
      host = "localhost";
    }
    LdapConn conn;
    try {
      conn = new LdapConn(host, port, LdapConn.connect(host, port, ssl, intProp(env, "com.sun.jndi.ldap.connect.timeout"),
          intProp(env, "com.sun.jndi.ldap.read.timeout")));
    } catch (IOException e) {
      CommunicationException ce = new CommunicationException(host + ":" + port);
      ce.setRootCause(e);
      throw ce;
    }
    LdapCtx ctx = new LdapCtx(conn, dn, env, false);
    try {
      bind(conn, env, host);
    } catch (NamingException | RuntimeException e) {
      conn.close();
      throw e;
    }
    return ctx;
  }

  private static int intProp(Hashtable<Object, Object> env, String key) {
    Object v = env.get(key);
    return v == null ? 0 : Integer.parseInt(v.toString().trim());
  }

  private static List<Control> connectControls(Hashtable<Object, Object> env) {
    Object c = env.get("java.naming.ldap.control.connect");
    return c instanceof Control[] ? java.util.Arrays.asList((Control[]) c) : null;
  }

  private static void bind(LdapConn conn, Hashtable<Object, Object> env, String host) throws NamingException {
    Object principal = env.get(Context.SECURITY_PRINCIPAL);
    Object creds = env.get(Context.SECURITY_CREDENTIALS);
    String auth = (String) env.get(Context.SECURITY_AUTHENTICATION);
    if (auth == null) {
      auth = principal == null ? "none" : "simple";
    }
    String version = String.valueOf(env.get("java.naming.ldap.version"));
    if (auth.equals("none")) {
      if (version.equals("3")) {
        return;
      }
      send(conn, env, "", new byte[0], connectControls(env));
      return;
    }
    String dn = principal == null ? "" : principal.toString();
    byte[] pw = creds == null ? new byte[0]
        : creds instanceof byte[] ? (byte[]) creds
        : creds instanceof char[] ? new String((char[]) creds).getBytes(StandardCharsets.UTF_8)
        : creds.toString().getBytes(StandardCharsets.UTF_8);
    if (auth.equals("simple")) {
      send(conn, env, dn, pw, connectControls(env));
      return;
    }
    if (auth.equals("strong")) {
      throw new AuthenticationNotSupportedException(auth);
    }
    saslBind(conn, env, host, auth, dn, pw);
  }

  private static void send(LdapConn conn, Hashtable<Object, Object> env, String dn, byte[] pw, List<Control> ctls)
      throws NamingException {
    byte[] op = Ber.tlv(0x60, Ber.integer(0x02, 3), Ber.str(0x04, dn), Ber.octets(0x80, pw));
    List<LdapConn.Msg> msgs = conn.exchange(op, ctls, 0x61);
    try {
      LdapConn.Result r = LdapConn.Result.parse(msgs.get(msgs.size() - 1).content);
      if (r.code != 0) {
        throw fail(r);
      }
    } catch (IOException e) {
      throw new CommunicationException("malformed bind response");
    }
    conn.bound = true;
  }

  private static NamingException fail(LdapConn.Result r) {
    String text = r.message == null || r.message.isEmpty() ? "Invalid Credentials" : r.message;
    String msg = "[LDAP: error code " + r.code + " - " + text + "]";
    if (r.code == 49) {
      return new javax.naming.AuthenticationException(msg);
    }
    if (r.code == 7 || r.code == 8 || r.code == 13 || r.code == 48) {
      return new AuthenticationNotSupportedException(msg);
    }
    return new NamingException(msg);
  }

  private static void saslBind(LdapConn conn, Hashtable<Object, Object> env, String host, String mechs, String user,
      byte[] pw) throws NamingException {
    final char[] password = new String(pw, StandardCharsets.UTF_8).toCharArray();
    javax.security.auth.callback.CallbackHandler handler = callbacks -> {
      for (Callback cb : callbacks) {
        if (cb instanceof NameCallback) {
          ((NameCallback) cb).setName(user);
        } else if (cb instanceof PasswordCallback) {
          ((PasswordCallback) cb).setPassword(password);
        } else if (cb instanceof RealmCallback) {
          ((RealmCallback) cb).setText(((RealmCallback) cb).getDefaultText());
        } else if (cb instanceof RealmChoiceCallback) {
          ((RealmChoiceCallback) cb).setSelectedIndex(0);
        }
      }
    };
    java.util.Map<String, Object> props = new java.util.HashMap<String, Object>();
    for (java.util.Map.Entry<Object, Object> e : env.entrySet()) {
      if (e.getKey() instanceof String && ((String) e.getKey()).startsWith("javax.security.sasl.")) {
        props.put((String) e.getKey(), e.getValue());
      }
    }
    SaslClient client;
    try {
      client = Sasl.createSaslClient(mechs.trim().split("\\s+"), null, "ldap", host, props, handler);
    } catch (SaslException e) {
      throw saslFail(e);
    }
    if (client == null) {
      throw new AuthenticationNotSupportedException(mechs);
    }
    try {
      byte[] token = client.hasInitialResponse() ? client.evaluateChallenge(new byte[0]) : null;
      while (true) {
        byte[] cred = token == null ? new byte[0] : Ber.octets(0x04, token);
        byte[] op = Ber.tlv(0x60, Ber.integer(0x02, 3), Ber.str(0x04, user),
            Ber.tlv(0xa3, Ber.str(0x04, client.getMechanismName()), cred));
        List<LdapConn.Msg> msgs = conn.exchange(op, connectControls(env), 0x61);
        Ber.Reader r = new Ber.Reader(msgs.get(msgs.size() - 1).content);
        LdapConn.Result res = LdapConn.Result.parse(msgs.get(msgs.size() - 1).content);
        byte[] serverCreds = null;
        r.intv();
        r.str();
        r.str();
        while (r.more()) {
          if (r.peek() == 0x87) {
            serverCreds = r.raw();
          } else {
            r.raw();
          }
        }
        if (res.code == 0) {
          if (serverCreds != null && !client.isComplete()) {
            client.evaluateChallenge(serverCreds);
          }
          conn.bound = true;
          return;
        }
        if (res.code != 14) {
          throw fail(res);
        }
        token = client.evaluateChallenge(serverCreds == null ? new byte[0] : serverCreds);
      }
    } catch (SaslException e) {
      throw saslFail(e);
    } catch (IOException e) {
      throw new CommunicationException("malformed bind response");
    }
  }

  private static NamingException saslFail(SaslException e) {
    NamingException ne = new javax.naming.AuthenticationException("SASL authentication failed");
    ne.setRootCause(e);
    return ne;
  }
}
