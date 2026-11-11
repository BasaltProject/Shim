/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.ldap;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.naming.CommunicationException;
import javax.naming.NamingException;
import javax.naming.ldap.Control;

/** One LDAP connection: framing, message ids and the request/response exchange. */
final class LdapConn {
  static final class Msg {
    int tag;
    byte[] content;
    byte[] controls;
  }

  static final class Result {
    int code;
    String matched = "";
    String message = "";
    List<String> referrals = new ArrayList<String>();

    static Result parse(byte[] content) throws IOException {
      Ber.Reader r = new Ber.Reader(content);
      Result res = new Result();
      res.code = r.intv();
      res.matched = r.str();
      res.message = r.str();
      if (r.more() && r.peek() == 0xa3) {
        Ber.Reader refs = r.seq();
        while (refs.more()) {
          res.referrals.add(refs.str());
        }
      }
      return res;
    }
  }

  final String host;
  final int port;
  private Socket socket;
  private InputStream in;
  private OutputStream out;
  private int nextId = 1;
  boolean bound;
  int refs = 1;

  LdapConn(String host, int port, Socket socket) throws IOException {
    this.host = host;
    this.port = port;
    this.socket = socket;
    this.in = new BufferedInputStream(socket.getInputStream());
    this.out = socket.getOutputStream();
  }

  static Socket connect(String host, int port, boolean ssl, int connectTimeout, int readTimeout)
      throws IOException {
    Socket s = new Socket();
    s.connect(new InetSocketAddress(host, port), Math.max(connectTimeout, 0));
    if (ssl) {
      javax.net.ssl.SSLSocket t = (javax.net.ssl.SSLSocket) ((javax.net.ssl.SSLSocketFactory)
          javax.net.ssl.SSLSocketFactory.getDefault()).createSocket(s, host, port, true);
      s = t;
    }
    if (readTimeout > 0) {
      s.setSoTimeout(readTimeout);
    }
    return s;
  }

  static byte[] controlsBlock(List<Control> ctls) {
    if (ctls == null || ctls.isEmpty()) {
      return new byte[0];
    }
    ByteArrayOutputStream o = new ByteArrayOutputStream();
    for (Control c : ctls) {
      ByteArrayOutputStream one = new ByteArrayOutputStream();
      one.writeBytes(Ber.str(0x04, c.getID()));
      if (c.isCritical()) {
        one.writeBytes(Ber.bool(true));
      }
      byte[] v = c.getEncodedValue();
      if (v != null) {
        one.writeBytes(Ber.octets(0x04, v));
      }
      o.writeBytes(Ber.tlv(0x30, one.toByteArray()));
    }
    return Ber.tlv(0xa0, o.toByteArray());
  }

  /** Sends an operation and reads until a message whose tag is in the terminal set arrives. */
  synchronized List<Msg> exchange(byte[] op, List<Control> ctls, int... terminal) throws NamingException {
    try {
      int id = nextId++;
      byte[] msg = Ber.tlv(0x30, Ber.integer(0x02, id), op, controlsBlock(ctls));
      out.write(msg);
      out.flush();
      if (terminal.length == 0) {
        return new ArrayList<Msg>();
      }
      List<Msg> got = new ArrayList<Msg>();
      while (true) {
        Msg m = readMessage();
        if (m == null) {
          throw new CommunicationException("connection closed");
        }
        got.add(m);
        for (int t : terminal) {
          if (m.tag == t) {
            return got;
          }
        }
      }
    } catch (IOException e) {
      close();
      CommunicationException ce = new CommunicationException(host + ":" + port);
      ce.setRootCause(e);
      throw ce;
    }
  }

  private Msg readMessage() throws IOException {
    int t = in.read();
    if (t < 0) {
      return null;
    }
    int l = in.read();
    if (l >= 0x80) {
      int n = l & 0x7f;
      l = 0;
      for (int i = 0; i < n; i++) {
        l = (l << 8) | in.read();
      }
    }
    byte[] body = in.readNBytes(l);
    if (body.length != l) {
      return null;
    }
    Ber.Reader r = new Ber.Reader(body);
    r.intv();
    Msg m = new Msg();
    m.tag = r.peek();
    m.content = r.raw();
    if (r.more() && r.peek() == 0xa0) {
      m.controls = r.raw();
    }
    return m;
  }

  void unbind() {
    try {
      if (socket != null && !socket.isClosed()) {
        exchange(Ber.tlv(0x42), null);
      }
    } catch (NamingException e) {
      // already closing
    }
    close();
  }

  void close() {
    try {
      if (socket != null) {
        socket.close();
      }
    } catch (IOException e) {
      // ignore
    }
  }

  boolean isClosed() {
    return socket == null || socket.isClosed();
  }
}
