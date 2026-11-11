/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.dns;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Wire-level DNS client: UDP with retries and TCP fallback, plus record decoding. */
final class DnsResolver {
  static final String[] kTypeNames = {null, "A", "NS", "MD", "MF", "CNAME", "SOA", "MB", "MG", "MR", "NULL", "WKS",
      "PTR", "HINFO", "MINFO", "MX", "TXT", "RP", "AFSDB", "X25", "ISDN", "RT", "NSAP", "NSAP-PTR", "SIG", "KEY",
      "PX", "GPOS", "AAAA", "LOC", "NXT", "EID", "NIMLOC", "SRV", "ATMA", "NAPTR"};

  static final class Record {
    final String name;
    final int type;
    final int rrClass;
    final String data;

    Record(String name, int type, int rrClass, String data) {
      this.name = name;
      this.type = type;
      this.rrClass = rrClass;
      this.data = data;
    }
  }

  static final class Response {
    int rcode;
    final List<Record> answers = new ArrayList<Record>();
  }

  private static final Random kRandom = new Random();

  private DnsResolver() {
  }

  static int typeOf(String id) {
    if (id.equals("*")) {
      return 255;
    }
    for (int i = 1; i < kTypeNames.length; i++) {
      if (kTypeNames[i].equalsIgnoreCase(id)) {
        return i;
      }
    }
    if (id.equalsIgnoreCase("AXFR")) {
      return 252;
    }
    return -1;
  }

  static String typeName(int type) {
    if (type > 0 && type < kTypeNames.length) {
      return kTypeNames[type];
    }
    return type == 255 ? "*" : "TYPE" + type;
  }

  static byte[] buildQuery(int id, List<String> labels, int type, boolean recurse) {
    ByteArrayOutputStream o = new ByteArrayOutputStream();
    o.write(id >> 8);
    o.write(id);
    o.write(recurse ? 1 : 0);
    o.write(0);
    o.write(0);
    o.write(1);
    for (int i = 0; i < 6; i++) {
      o.write(0);
    }
    for (String l : labels) {
      byte[] b = l.getBytes(StandardCharsets.UTF_8);
      o.write(b.length);
      o.write(b, 0, b.length);
    }
    o.write(0);
    o.write(type >> 8);
    o.write(type);
    o.write(0);
    o.write(1);
    return o.toByteArray();
  }

  static Response query(List<InetSocketAddress> servers, List<String> labels, int type, boolean recurse,
      int timeoutMs, int retries) throws IOException {
    int id = kRandom.nextInt(65536);
    byte[] q = buildQuery(id, labels, type, recurse);
    IOException last = null;
    int timeout = timeoutMs;
    for (int attempt = 0; attempt <= retries; attempt++) {
      for (InetSocketAddress server : servers) {
        try {
          byte[] reply = udp(server, q, timeout);
          if (reply.length >= 12 && ((reply[0] & 0xff) << 8 | (reply[1] & 0xff)) == id) {
            if ((reply[2] & 0x02) != 0) {
              reply = tcp(server, q, timeout);
            }
            return parse(reply);
          }
        } catch (SocketTimeoutException e) {
          last = new SocketTimeoutException();
        } catch (PortUnreachableException e) {
          last = e;
        }
      }
      timeout *= 2;
    }
    throw last != null ? last : new SocketTimeoutException("no response");
  }

  static List<Record> transfer(List<InetSocketAddress> servers, List<String> labels, int timeout)
      throws IOException {
    byte[] q = buildQuery(kRandom.nextInt(65536), labels, 252, false);
    IOException last = null;
    for (InetSocketAddress server : servers) {
      try (Socket s = new Socket()) {
        s.connect(server, timeout);
        s.setSoTimeout(timeout);
        java.io.DataOutputStream out = new java.io.DataOutputStream(s.getOutputStream());
        out.writeShort(q.length);
        out.write(q);
        out.flush();
        DataInputStream in = new DataInputStream(s.getInputStream());
        List<Record> all = new ArrayList<Record>();
        int soas = 0;
        while (soas < 2) {
          byte[] msg = new byte[in.readUnsignedShort()];
          in.readFully(msg);
          Response r = parse(msg);
          if (r.rcode != 0) {
            throw new IOException("transfer refused");
          }
          for (Record rec : r.answers) {
            all.add(rec);
            if (rec.type == 6) {
              soas++;
            }
          }
        }
        return all;
      } catch (IOException e) {
        last = e;
      }
    }
    throw last != null ? last : new IOException("no servers");
  }

  private static byte[] udp(InetSocketAddress server, byte[] q, int timeout) throws IOException {
    try (DatagramSocket s = new DatagramSocket()) {
      s.setSoTimeout(timeout);
      s.connect(server);
      s.send(new DatagramPacket(q, q.length));
      byte[] buf = new byte[65535];
      DatagramPacket p = new DatagramPacket(buf, buf.length);
      s.receive(p);
      return java.util.Arrays.copyOf(buf, p.getLength());
    }
  }

  private static byte[] tcp(InetSocketAddress server, byte[] q, int timeout) throws IOException {
    try (Socket s = new Socket()) {
      s.connect(server, timeout);
      s.setSoTimeout(timeout);
      java.io.DataOutputStream out = new java.io.DataOutputStream(s.getOutputStream());
      out.writeShort(q.length);
      out.write(q);
      out.flush();
      DataInputStream in = new DataInputStream(s.getInputStream());
      byte[] r = new byte[in.readUnsignedShort()];
      in.readFully(r);
      return r;
    }
  }

  static Response parse(byte[] m) throws IOException {
    Response r = new Response();
    if (m.length < 12) {
      throw new IOException("short DNS message");
    }
    r.rcode = m[3] & 0x0f;
    int qd = u16(m, 4);
    int an = u16(m, 6);
    int[] pos = {12};
    for (int i = 0; i < qd; i++) {
      readName(m, pos);
      pos[0] += 4;
    }
    for (int i = 0; i < an; i++) {
      String name = readName(m, pos);
      int type = u16(m, pos[0]);
      int cls = u16(m, pos[0] + 2);
      int len = u16(m, pos[0] + 8);
      int start = pos[0] + 10;
      pos[0] = start + len;
      if (pos[0] > m.length) {
        throw new IOException("truncated record");
      }
      r.answers.add(new Record(name, type, cls, rdata(m, type, start, len)));
    }
    return r;
  }

  private static int u16(byte[] m, int o) {
    return ((m[o] & 0xff) << 8) | (m[o + 1] & 0xff);
  }

  private static long u32(byte[] m, int o) {
    return ((long) u16(m, o) << 16) | u16(m, o + 2);
  }

  static String readName(byte[] m, int[] pos) throws IOException {
    StringBuilder sb = new StringBuilder();
    int p = pos[0];
    boolean jumped = false;
    int hops = 0;
    while (true) {
      int len = m[p] & 0xff;
      if (len == 0) {
        p++;
        break;
      }
      if ((len & 0xc0) == 0xc0) {
        int target = ((len & 0x3f) << 8) | (m[p + 1] & 0xff);
        if (!jumped) {
          pos[0] = p + 2;
          jumped = true;
        }
        p = target;
        if (++hops > 64) {
          throw new IOException("compression loop");
        }
        continue;
      }
      sb.append(new String(m, p + 1, len, StandardCharsets.UTF_8)).append('.');
      p += 1 + len;
    }
    if (!jumped) {
      pos[0] = p;
    }
    return sb.length() == 0 ? "." : sb.toString();
  }

  private static String rdata(byte[] m, int type, int off, int len) throws IOException {
    int[] p = {off};
    switch (type) {
      case 1:
        return (m[off] & 0xff) + "." + (m[off + 1] & 0xff) + "." + (m[off + 2] & 0xff) + "." + (m[off + 3] & 0xff);
      case 28:
        return ipv6(m, off);
      case 2:
      case 5:
      case 12:
      case 3:
      case 4:
      case 7:
      case 8:
      case 9:
        return readName(m, p);
      case 15:
        return u16(m, off) + " " + readNameAt(m, off + 2);
      case 6: {
        String mname = readName(m, p);
        String rname = readName(m, p);
        return mname + " " + rname + " " + u32(m, p[0]) + " " + u32(m, p[0] + 4) + " " + u32(m, p[0] + 8) + " "
            + u32(m, p[0] + 12) + " " + u32(m, p[0] + 16);
      }
      case 33:
        return u16(m, off) + " " + u16(m, off + 2) + " " + u16(m, off + 4) + " " + readNameAt(m, off + 6);
      case 16: {
        StringBuilder sb = new StringBuilder();
        int i = off;
        while (i < off + len) {
          int l = m[i] & 0xff;
          String s = new String(m, i + 1, l, StandardCharsets.ISO_8859_1);
          if (sb.length() > 0) {
            sb.append(' ');
          }
          sb.append(s.indexOf(' ') >= 0 || s.isEmpty() ? "\"" + s + "\"" : s);
          i += 1 + l;
        }
        return sb.toString();
      }
      case 13: {
        StringBuilder sb = new StringBuilder();
        int i = off;
        while (i < off + len) {
          int l = m[i] & 0xff;
          if (sb.length() > 0) {
            sb.append(' ');
          }
          sb.append('"').append(new String(m, i + 1, l, StandardCharsets.ISO_8859_1)).append('"');
          i += 1 + l;
        }
        return sb.toString();
      }
      default: {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
          sb.append(String.format("%02x", m[off + i] & 0xff));
        }
        return sb.toString();
      }
    }
  }

  private static String readNameAt(byte[] m, int off) throws IOException {
    return readName(m, new int[] {off});
  }

  static String ipv6(byte[] m, int off) {
    int[] w = new int[8];
    for (int i = 0; i < 8; i++) {
      w[i] = u16(m, off + 2 * i);
    }
    int bestStart = -1;
    int bestLen = 0;
    for (int i = 0; i < 8;) {
      if (w[i] != 0) {
        i++;
        continue;
      }
      int j = i;
      while (j < 8 && w[j] == 0) {
        j++;
      }
      if (j - i > bestLen) {
        bestStart = i;
        bestLen = j - i;
      }
      i = j;
    }
    if (bestLen < 2) {
      bestStart = -1;
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 8; i++) {
      if (i == bestStart) {
        sb.append("::");
        i += bestLen - 1;
        continue;
      }
      if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ':') {
        sb.append(':');
      }
      sb.append(Integer.toHexString(w[i]));
    }
    return sb.toString();
  }

}
