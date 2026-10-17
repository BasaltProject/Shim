/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.util.Arrays;

public final class Inet6Address extends InetAddress {
  private static final long serialVersionUID = 6880410070516793377L;

  static final int INADDRSZ = 16;

  private int scopeId;
  private boolean scopeIdSet;
  private NetworkInterface scopeIfname;
  private String scopeIfnameName;

  Inet6Address(String hostName, byte[] addr, int scopeId, String ifname) {
    holderHostName = hostName;
    holderAddress = addr;
    if (scopeId >= 0) {
      this.scopeId = scopeId;
      this.scopeIdSet = true;
    }
    if (ifname != null) {
      this.scopeIfnameName = ifname;
      try {
        NetworkInterface nif = NetworkInterface.getByName(ifname);
        if (nif == null) {
          throw new IllegalArgumentException("no such interface " + ifname);
        }
        this.scopeIfname = nif;
        this.scopeId = nif.getIndex();
        this.scopeIdSet = true;
      } catch (SocketException e) {
        throw new IllegalArgumentException("no such interface " + ifname);
      }
    }
  }

  public static Inet6Address getByAddress(String host, byte[] addr, NetworkInterface nif) throws UnknownHostException {
    if (host != null && !host.isEmpty() && host.charAt(0) == '[' && host.charAt(host.length() - 1) == ']') {
      host = host.substring(1, host.length() - 1);
    }
    if (addr != null && addr.length == INADDRSZ) {
      Inet6Address result = new Inet6Address(host, addr.clone(), -1, null);
      if (nif != null) {
        boolean found = false;
        java.util.Enumeration<InetAddress> addrs = nif.getInetAddresses();
        while (addrs.hasMoreElements()) {
          if (addrs.nextElement() instanceof Inet6Address) {
            found = true;
          }
        }
        if (!found) {
          throw new UnknownHostException("no scope_id found");
        }
        result.scopeIfname = nif;
        result.scopeId = nif.getIndex();
        result.scopeIdSet = true;
      }
      return result;
    }
    throw new UnknownHostException("addr is of illegal length");
  }

  public static Inet6Address getByAddress(String host, byte[] addr, int scope_id) throws UnknownHostException {
    if (host != null && !host.isEmpty() && host.charAt(0) == '[' && host.charAt(host.length() - 1) == ']') {
      host = host.substring(1, host.length() - 1);
    }
    if (addr != null && addr.length == INADDRSZ) {
      return new Inet6Address(host, addr.clone(), scope_id < 0 ? 0 : scope_id, null);
    }
    throw new UnknownHostException("addr is of illegal length");
  }

  public int getScopeId() {
    return scopeIdSet ? scopeId : 0;
  }

  public NetworkInterface getScopedInterface() {
    return scopeIfname;
  }

  @Override
  public boolean isMulticastAddress() {
    return (holderAddress[0] & 0xff) == 0xff;
  }

  @Override
  public boolean isAnyLocalAddress() {
    byte test = 0x00;
    for (int i = 0; i < INADDRSZ; i++) {
      test |= holderAddress[i];
    }
    return test == 0x00;
  }

  @Override
  public boolean isLoopbackAddress() {
    byte test = 0x00;
    for (int i = 0; i < 15; i++) {
      test |= holderAddress[i];
    }
    return test == 0x00 && holderAddress[15] == 0x01;
  }

  @Override
  public boolean isLinkLocalAddress() {
    return ((holderAddress[0] & 0xff) == 0xfe && (holderAddress[1] & 0xc0) == 0x80);
  }

  @Override
  public boolean isSiteLocalAddress() {
    return ((holderAddress[0] & 0xff) == 0xfe && (holderAddress[1] & 0xc0) == 0xc0);
  }

  @Override
  public boolean isMCGlobal() {
    return ((holderAddress[0] & 0xff) == 0xff && (holderAddress[1] & 0x0f) == 0x0e);
  }

  @Override
  public boolean isMCNodeLocal() {
    return ((holderAddress[0] & 0xff) == 0xff && (holderAddress[1] & 0x0f) == 0x01);
  }

  @Override
  public boolean isMCLinkLocal() {
    return ((holderAddress[0] & 0xff) == 0xff && (holderAddress[1] & 0x0f) == 0x02);
  }

  @Override
  public boolean isMCSiteLocal() {
    return ((holderAddress[0] & 0xff) == 0xff && (holderAddress[1] & 0x0f) == 0x05);
  }

  @Override
  public boolean isMCOrgLocal() {
    return ((holderAddress[0] & 0xff) == 0xff && (holderAddress[1] & 0x0f) == 0x08);
  }

  public boolean isIPv4CompatibleAddress() {
    byte[] a = holderAddress;
    for (int i = 0; i < 12; i++) {
      if (a[i] != 0) {
        return false;
      }
    }
    return true;
  }

  @Override
  public byte[] getAddress() {
    return holderAddress.clone();
  }

  @Override
  public String getHostAddress() {
    String s = numericToString(holderAddress);
    if (scopeIdSet) {
      s += "%" + (scopeIfnameName != null ? scopeIfnameName : Integer.toString(scopeId));
    }
    return s;
  }

  static String numericToString(byte[] src) {
    StringBuilder sb = new StringBuilder(39);
    for (int i = 0; i < 8; i++) {
      sb.append(Integer.toHexString(((src[i << 1] << 8) & 0xff00) | (src[(i << 1) + 1] & 0xff)));
      if (i < 7) {
        sb.append(':');
      }
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int hash = 0;
    int i = 0;
    while (i < INADDRSZ) {
      int j = 0;
      int component = 0;
      while (j < 4 && i < INADDRSZ) {
        component = (component << 8) + holderAddress[i];
        j++;
        i++;
      }
      hash += component;
    }
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Inet6Address)) {
      return false;
    }
    return Arrays.equals(holderAddress, ((Inet6Address) obj).holderAddress);
  }

  public static InetAddress ofLiteral(String ipAddressLiteral) {
    if (ipAddressLiteral == null) {
      throw new NullPointerException();
    }
    if (ipAddressLiteral.indexOf(':') < 0) {
      throw new IllegalArgumentException(("Invalid IP address literal: " + ipAddressLiteral.strip()).stripTrailing());
    }
    return InetAddress.ofLiteral(ipAddressLiteral);
  }
}
