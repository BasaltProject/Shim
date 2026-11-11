/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.krb5;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;

public final class KdcComm {
  private KdcComm() {
  }

  public static byte[] send(byte[] request, String realm) throws KrbException {
    Config cfg = Config.getInstance();
    List<String> kdcs = cfg.kdcList(realm);
    if (kdcs.isEmpty()) {
      throw new KrbException("Cannot locate KDC for " + realm);
    }
    int udpLimit = cfg.getInt(1465, "libdefaults", "udp_preference_limit");
    int timeout = cfg.getInt(30000, "libdefaults", "kdc_timeout");
    if (timeout > 0 && timeout < 1000 && cfg.get("libdefaults", "kdc_timeout") != null) {
      timeout *= 1000;
    }
    int retries = cfg.getInt(3, "libdefaults", "max_retries");
    boolean tcpOnly = udpLimit > 0 && request.length > udpLimit;
    IOException last = null;
    for (String entry : kdcs) {
      String host = entry;
      int port = 88;
      int c = entry.lastIndexOf(':');
      if (c > 0 && entry.indexOf(':') == c) {
        host = entry.substring(0, c);
        port = Integer.parseInt(entry.substring(c + 1));
      }
      try {
        if (!tcpOnly) {
          for (int attempt = 0; attempt < Math.max(retries, 1); attempt++) {
            try {
              byte[] resp = udp(request, host, port, timeout);
              if (isTooBig(resp)) {
                break;
              }
              return resp;
            } catch (SocketTimeoutException e) {
              last = e;
            }
          }
        }
        return tcp(request, host, port, timeout);
      } catch (IOException e) {
        last = e;
      }
    }
    KrbException ke = new KrbException("Cannot contact any KDC for realm " + realm);
    if (last != null) {
      ke.initCause(last);
    }
    throw ke;
  }

  private static boolean isTooBig(byte[] resp) {
    try {
      Asn1.Node n = Asn1.parse(resp);
      return n.tag == 0x7e && n.inner().need(6).asInt() == KrbException.KRB_ERR_RESPONSE_TOO_BIG;
    } catch (IOException | RuntimeException e) {
      return false;
    }
  }

  private static byte[] udp(byte[] request, String host, int port, int timeout) throws IOException {
    try (DatagramSocket s = new DatagramSocket()) {
      s.setSoTimeout(timeout);
      s.connect(new InetSocketAddress(host, port));
      s.send(new DatagramPacket(request, request.length));
      byte[] buf = new byte[65535];
      DatagramPacket p = new DatagramPacket(buf, buf.length);
      s.receive(p);
      byte[] out = new byte[p.getLength()];
      System.arraycopy(buf, 0, out, 0, out.length);
      return out;
    }
  }

  private static byte[] tcp(byte[] request, String host, int port, int timeout) throws IOException {
    try (Socket s = new Socket()) {
      s.connect(new InetSocketAddress(host, port), timeout);
      s.setSoTimeout(timeout);
      DataOutputStream out = new DataOutputStream(s.getOutputStream());
      out.writeInt(request.length);
      out.write(request);
      out.flush();
      DataInputStream in = new DataInputStream(s.getInputStream());
      int n = in.readInt();
      if (n < 0 || n > 10_000_000) {
        throw new IOException("bad length");
      }
      byte[] resp = new byte[n];
      in.readFully(resp);
      return resp;
    }
  }
}
