/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.dgc;

import java.io.Serializable;
import java.net.InetAddress;
import java.rmi.server.UID;
import java.security.SecureRandom;

public final class VMID implements Serializable {
  private static final long serialVersionUID = -538642295484486218L;

  @Deprecated
  private static final byte[] randomBytes;

  private byte[] addr;
  private UID uid;

  static {
    SecureRandom secureRandom = new SecureRandom();
    byte[] bytes = new byte[8];
    secureRandom.nextBytes(bytes);
    randomBytes = bytes;
  }

  public VMID() {
    addr = randomBytes;
    uid = new UID();
  }

  @Deprecated
  public static boolean isUnique() {
    return true;
  }

  public int hashCode() {
    return uid.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj instanceof VMID) {
      VMID vmid = (VMID) obj;
      if (!uid.equals(vmid.uid)) {
        return false;
      }
      if ((addr == null) ^ (vmid.addr == null)) {
        return false;
      }
      if (addr != null) {
        if (addr.length != vmid.addr.length) {
          return false;
        }
        for (int i = 0; i < addr.length; ++i) {
          if (addr[i] != vmid.addr[i]) {
            return false;
          }
        }
      }
      return true;
    } else {
      return false;
    }
  }

  public String toString() {
    StringBuilder result = new StringBuilder();
    if (addr != null) {
      for (int i = 0; i < addr.length; ++i) {
        int x = (int) (addr[i] & 0xFF);
        result.append((x < 0x10 ? "0" : "") + Integer.toString(x, 16));
      }
    }
    result.append(':');
    result.append(uid.toString());
    return result.toString();
  }
}
