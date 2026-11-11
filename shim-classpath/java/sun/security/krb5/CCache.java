/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.krb5;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public final class CCache {
  public PrincipalName defaultPrincipal;
  public final List<Creds> creds = new ArrayList<Creds>();

  private final DataInputStream in;
  private boolean little;

  private CCache(DataInputStream in) {
    this.in = in;
  }

  public static String defaultPath() {
    String env = System.getenv("KRB5CCNAME");
    if (env != null && !env.isEmpty()) {
      return env.startsWith("FILE:") ? env.substring(5) : env.contains(":") ? null : env;
    }
    try {
      Object uid = java.nio.file.Files.getAttribute(java.nio.file.Paths.get("/proc/self"), "unix:uid");
      return "/tmp/krb5cc_" + uid;
    } catch (IOException | RuntimeException e) {
      return null;
    }
  }

  public static CCache read(File f) throws IOException {
    try (DataInputStream in = new DataInputStream(new java.io.BufferedInputStream(new FileInputStream(f)))) {
      CCache c = new CCache(in);
      c.parse();
      return c;
    }
  }

  private int u8() throws IOException {
    return in.readUnsignedByte();
  }

  private int u16() throws IOException {
    int v = in.readUnsignedShort();
    return little ? Integer.reverseBytes(v) >>> 16 : v;
  }

  private int u32() throws IOException {
    int v = in.readInt();
    return little ? Integer.reverseBytes(v) : v;
  }

  private byte[] data() throws IOException {
    int n = u32();
    if (n < 0 || n > 1 << 24) {
      throw new IOException("bad ccache");
    }
    byte[] b = new byte[n];
    in.readFully(b);
    return b;
  }

  private PrincipalName principal(int version) throws IOException {
    int type = version == 1 ? 0 : u32();
    int n = u32();
    if (version == 1) {
      n--;
    }
    String realm = new String(data(), "UTF-8");
    String[] c = new String[n];
    for (int i = 0; i < n; i++) {
      c[i] = new String(data(), "UTF-8");
    }
    return new PrincipalName(type, c, realm);
  }

  private void parse() throws IOException {
    if (u8() != 5) {
      throw new IOException("bad ccache");
    }
    int version = u8();
    little = version < 3;
    if (version == 4) {
      int hl = in.readUnsignedShort();
      in.skipBytes(hl);
    }
    defaultPrincipal = principal(version);
    while (in.available() > 0) {
      Creds c = new Creds();
      c.client = principal(version);
      c.server = principal(version);
      c.keyType = u16();
      if (version < 4) {
        u16();
      }
      int kl = u32();
      c.key = new byte[kl];
      in.readFully(c.key);
      c.authTime = (u32() & 0xffffffffL) * 1000L;
      c.startTime = (u32() & 0xffffffffL) * 1000L;
      c.endTime = (u32() & 0xffffffffL) * 1000L;
      c.renewTill = (u32() & 0xffffffffL) * 1000L;
      if (c.renewTill == 0) {
        c.renewTill = -1;
      }
      u8();
      c.flags = u32();
      int na = u32();
      List<InetAddress> addrs = new ArrayList<InetAddress>();
      for (int i = 0; i < na; i++) {
        int t = u16();
        byte[] d = data();
        if ((t == 2 || t == 24) && (d.length == 4 || d.length == 16)) {
          addrs.add(InetAddress.getByAddress(d));
        }
      }
      c.addresses = addrs.isEmpty() ? null : addrs.toArray(new InetAddress[0]);
      int nd = u32();
      for (int i = 0; i < nd; i++) {
        u16();
        data();
      }
      c.ticket = data();
      data();
      if (c.server.getRealmString() != null && c.server.getRealmString().equals("X-CACHECONF:")) {
        continue;
      }
      creds.add(c);
    }
  }
}
