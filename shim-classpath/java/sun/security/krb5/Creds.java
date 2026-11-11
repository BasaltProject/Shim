/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.krb5;

import java.net.InetAddress;

public final class Creds {
  public byte[] ticket;
  public PrincipalName client;
  public PrincipalName server;
  public int keyType;
  public byte[] key;
  public int flags;
  public long authTime;
  public long startTime;
  public long endTime;
  public long renewTill = -1;
  public InetAddress[] addresses;
  public Creds s4uTicket;
  public org.ietf.jgss.GSSName s4uName;

  public Creds withS4u(Creds ticket, org.ietf.jgss.GSSName name) {
    Creds c = new Creds();
    c.ticket = this.ticket;
    c.client = client;
    c.server = server;
    c.keyType = keyType;
    c.key = key;
    c.flags = flags;
    c.authTime = authTime;
    c.startTime = startTime;
    c.endTime = endTime;
    c.renewTill = renewTill;
    c.addresses = addresses;
    c.s4uTicket = ticket;
    c.s4uName = name;
    return c;
  }

  public boolean flag(int bit) {
    return (flags & (0x80000000 >>> bit)) != 0;
  }

  public boolean[] flagArray() {
    boolean[] f = new boolean[32];
    for (int i = 0; i < 32; i++) {
      f[i] = flag(i);
    }
    return f;
  }

  public boolean isTgt() {
    return server.isKrbtgt();
  }
}
