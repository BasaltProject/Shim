/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import javax.net.ssl.*;

public class LegacyTlsDisabledModern {
  static SSLContext ctx(String proto) throws Exception {
    SSLContext c = SSLContext.getInstance(proto);
    c.init(null, null, null);
    return c;
  }

  static String fail(Throwable e) {
    return "!" + e.getClass().getName() + ":" + e.getMessage();
  }

  static String socket(String[] clientProtos, String[] serverProtos) {
    try {
      SSLServerSocket ss = (SSLServerSocket) ctx("TLS").getServerSocketFactory().createServerSocket(0, 5,
          InetAddress.getLoopbackAddress());
      if (serverProtos != null) {
        ss.setEnabledProtocols(serverProtos);
      }
      final String[] server = new String[1];
      Thread t = new Thread(() -> {
        try (SSLSocket s = (SSLSocket) ss.accept()) {
          s.setSoTimeout(10000);
          s.startHandshake();
          server[0] = s.getSession().getProtocol();
        } catch (Throwable e) {
          server[0] = fail(e);
        }
      });
      t.start();
      String client;
      try (SSLSocket c = (SSLSocket) ctx("TLS").getSocketFactory().createSocket(InetAddress.getLoopbackAddress(),
          ss.getLocalPort())) {
        c.setSoTimeout(10000);
        c.setEnabledProtocols(clientProtos);
        c.startHandshake();
        client = c.getSession().getProtocol();
      } catch (Throwable e) {
        client = fail(e);
      }
      t.join(15000);
      ss.close();
      return client + " | " + server[0];
    } catch (Throwable e) {
      return "setup " + fail(e);
    }
  }

  static String dtls(String[] clientProtos) {
    try {
      SSLEngine c = ctx("DTLS").createSSLEngine("localhost", 1), s = ctx("DTLS").createSSLEngine();
      c.setUseClientMode(true);
      s.setUseClientMode(false);
      c.setEnabledProtocols(clientProtos);
      c.beginHandshake();
      ByteBuffer out = ByteBuffer.allocate(20000);
      SSLEngineResult r = c.wrap(ByteBuffer.allocate(0), out);
      return r.getStatus() + " " + r.getHandshakeStatus() + " " + (out.position() > 0);
    } catch (Throwable t) {
      return fail(t);
    }
  }

  public static void main(String[] a) throws Exception {
    String[] contexts = {"TLS", "TLSv1.3", "TLSv1.2", "TLSv1.1", "TLSv1", "DTLS", "DTLSv1.2", "DTLSv1.0"};
    for (String proto : contexts) {
      SSLContext c = ctx(proto);
      for (boolean client : new boolean[] {true, false}) {
        SSLEngine e = c.createSSLEngine();
        e.setUseClientMode(client);
        System.out.println(proto + " " + client + " " + Arrays.toString(e.getEnabledProtocols()) + " "
            + e.getEnabledCipherSuites().length);
      }
      SSLParameters p = c.getDefaultSSLParameters();
      System.out.println(proto + " default " + Arrays.toString(p.getProtocols()) + " " + p.getCipherSuites().length);
    }
    for (String[] protos : new String[][] {{"TLSv1"}, {"TLSv1.1"}, {"TLSv1.2", "TLSv1"}, {"TLSv1.3", "TLSv1.1"}}) {
      System.out.println("tls " + Arrays.toString(protos) + " " + socket(protos, null));
      System.out.println("tls server " + Arrays.toString(protos) + " " + socket(new String[] {"TLSv1.2"}, protos));
    }
    for (String[] protos : new String[][] {{"DTLSv1.0"}, {"DTLSv1.2", "DTLSv1.0"}}) {
      System.out.println("dtls " + Arrays.toString(protos) + " " + dtls(protos));
    }
  }
}
