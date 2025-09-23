/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import shim.NetNative;

public class InetAddress implements Serializable {
  private static final long serialVersionUID = 3286316764910316507L;

  String holderHostName;
  byte[] holderAddress;

  InetAddress() { }

  static InetAddress create(String host, byte[] addr) {
    if (addr.length == 4) {
      return new Inet4Address(host, addr);
    }
    return new Inet6Address(host, addr, -1, null);
  }

  public boolean isMulticastAddress() {
    return false;
  }

  public boolean isAnyLocalAddress() {
    return false;
  }

  public boolean isLoopbackAddress() {
    return false;
  }

  public boolean isLinkLocalAddress() {
    return false;
  }

  public boolean isSiteLocalAddress() {
    return false;
  }

  public boolean isMCGlobal() {
    return false;
  }

  public boolean isMCNodeLocal() {
    return false;
  }

  public boolean isMCLinkLocal() {
    return false;
  }

  public boolean isMCSiteLocal() {
    return false;
  }

  public boolean isMCOrgLocal() {
    return false;
  }

  public boolean isReachable(int timeout) throws IOException {
    return isReachable(null, 0, timeout);
  }

  public boolean isReachable(NetworkInterface netif, int ttl, int timeout) throws IOException {
    if (ttl < 0) {
      throw new IllegalArgumentException("ttl can't be negative");
    }
    if (timeout < 0) {
      throw new IllegalArgumentException("timeout can't be negative");
    }
    if (isLoopbackAddress() || isAnyLocalAddress()) {
      return true;
    }
    Socket s = new Socket();
    try {
      s.connect(new InetSocketAddress(this, 7), Math.max(1, timeout));
      return true;
    } catch (ConnectException refused) {
      return true;
    } catch (IOException ex) {
      return false;
    } finally {
      s.close();
    }
  }

  public String getHostName() {
    return getHostName(true);
  }

  String getHostName(boolean check) {
    String name = holderHostName;
    if (name == null) {
      name = NetNative.reverse(holderAddress);
      if (name == null) {
        name = getHostAddress();
      }
      holderHostName = name;
    }
    return name;
  }

  public String getCanonicalHostName() {
    String name = NetNative.reverse(holderAddress);
    return name != null ? name : getHostAddress();
  }

  public byte[] getAddress() {
    return holderAddress.clone();
  }

  public String getHostAddress() {
    return null;
  }

  public int hashCode() {
    return -1;
  }

  public boolean equals(Object obj) {
    return false;
  }

  public int getRawAddress() {
    byte[] a = holderAddress;
    if (a.length == 4) {
      return ((a[0] & 0xff) << 24) | ((a[1] & 0xff) << 16) | ((a[2] & 0xff) << 8) | (a[3] & 0xff);
    }
    return 0;
  }

  public String toString() {
    String hostName = holderHostName;
    return ((hostName != null) ? hostName : "") + "/" + getHostAddress();
  }

  public static InetAddress getByAddress(String host, byte[] addr) throws UnknownHostException {
    if (host != null && !host.isEmpty() && host.charAt(0) == '[' && host.charAt(host.length() - 1) == ']') {
      host = host.substring(1, host.length() - 1);
    }
    if (addr != null) {
      if (addr.length == 4) {
        return new Inet4Address(host, addr.clone());
      } else if (addr.length == 16) {
        byte[] converted = convertIPv4Mapped(addr);
        if (converted != null) {
          return new Inet4Address(host, converted);
        }
        return new Inet6Address(host, addr.clone(), -1, null);
      }
    }
    throw new UnknownHostException("addr is of illegal length");
  }

  private static byte[] convertIPv4Mapped(byte[] addr) {
    for (int i = 0; i < 10; i++) {
      if (addr[i] != 0) {
        return null;
      }
    }
    if (addr[10] != (byte) 0xff || addr[11] != (byte) 0xff) {
      return null;
    }
    return Arrays.copyOfRange(addr, 12, 16);
  }

  public static InetAddress getByAddress(byte[] addr) throws UnknownHostException {
    return getByAddress(null, addr);
  }

  static InetAddress anyLocalAddress() {
    return new Inet4Address("0.0.0.0", new byte[4]);
  }

  public static InetAddress getByName(String host) throws UnknownHostException {
    return getAllByName(host)[0];
  }

  public static InetAddress[] getAllByName(String host) throws UnknownHostException {
    if (host == null || host.isEmpty()) {
      return new InetAddress[] {getLoopbackAddress()};
    }
    boolean ipv6Expected = false;
    if (host.charAt(0) == '[') {
      if (host.length() > 2 && host.charAt(host.length() - 1) == ']') {
        host = host.substring(1, host.length() - 1);
        ipv6Expected = true;
      } else {
        throw new UnknownHostException(host + ": invalid IPv6 address literal");
      }
    }
    if (Character.digit(host.charAt(0), 16) != -1 || host.charAt(0) == ':') {
      byte[] v4 = textToV4(host);
      if (v4 != null) {
        if (ipv6Expected) {
          throw new UnknownHostException("[" + host + "]: invalid IPv6 address literal");
        }
        return new InetAddress[] {new Inet4Address(null, v4)};
      }
      if (host.indexOf(':') >= 0) {
        InetAddress v6 = parseV6(host);
        if (v6 == null) {
          throw new UnknownHostException(host + ": invalid IPv6 address literal");
        }
        return new InetAddress[] {v6};
      }
    }
    if (ipv6Expected) {
      throw new UnknownHostException("[" + host + "]: invalid IPv6 address literal");
    }
    return lookup(host);
  }

  private static InetAddress[] lookup(String host) throws UnknownHostException {
    NetNative.init();
    byte[][] all = NetNative.lookup(host);
    if (all == null || all.length == 0) {
      throw new UnknownHostException(host + ": Name or service not known");
    }
    InetAddress[] result = new InetAddress[all.length];
    for (int i = 0; i < all.length; i++) {
      result[i] = create(host, all[i]);
    }
    return result;
  }

  public static InetAddress ofLiteral(String ipAddressLiteral) {
    if (ipAddressLiteral == null) {
      throw new NullPointerException();
    }
    String text = ipAddressLiteral;
    boolean bracketed = false;
    if (text.length() > 2 && text.charAt(0) == '[' && text.charAt(text.length() - 1) == ']') {
      text = text.substring(1, text.length() - 1);
      bracketed = true;
    }
    InetAddress result = null;
    if (!text.isEmpty() && (Character.digit(text.charAt(0), 16) != -1 || text.charAt(0) == ':')) {
      byte[] v4 = textToV4(text);
      if (v4 != null) {
        result = new Inet4Address(null, v4);
      } else if (text.indexOf(':') >= 0) {
        result = parseV6(text);
      }
    }
    if (result == null || (bracketed && result instanceof Inet4Address)) {
      throw new IllegalArgumentException("Invalid IP address literal: " + ipAddressLiteral);
    }
    return result;
  }

  private static InetAddress parseV6(String host) {
    int percent = host.indexOf('%');
    String body = percent >= 0 ? host.substring(0, percent) : host;
    byte[] v6 = textToV6(body);
    if (v6 == null) {
      return null;
    }
    byte[] mapped = convertIPv4Mapped(v6);
    if (mapped != null) {
      return new Inet4Address(null, mapped);
    }
    int scopeId = -1;
    String scopeName = null;
    if (percent >= 0) {
      String scope = host.substring(percent + 1);
      if (scope.isEmpty()) {
        return null;
      }
      boolean numeric = true;
      for (int i = 0; i < scope.length(); i++) {
        numeric &= scope.charAt(i) >= '0' && scope.charAt(i) <= '9';
      }
      if (numeric) {
        try {
          scopeId = Integer.parseInt(scope);
        } catch (NumberFormatException e) {
          return null;
        }
      } else {
        scopeName = scope;
      }
    }
    try {
      return new Inet6Address(null, v6, scopeId, scopeName);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  static byte[] textToV4(String src) {
    if (src.isEmpty()) {
      return null;
    }
    long[] parts = new long[4];
    int count = 0;
    int start = 0;
    for (int i = 0; i <= src.length(); i++) {
      if (i == src.length() || src.charAt(i) == '.') {
        if (i == start || count >= 4) {
          return null;
        }
        long value = 0;
        for (int k = start; k < i; k++) {
          char c = src.charAt(k);
          if (c < '0' || c > '9') {
            return null;
          }
          value = value * 10 + (c - '0');
          if (value > 0xFFFFFFFFL) {
            return null;
          }
        }
        parts[count++] = value;
        start = i + 1;
      }
    }
    byte[] res = new byte[4];
    switch (count) {
      case 1:
        for (int i = 0; i < 4; i++) {
          res[i] = (byte) (parts[0] >> (24 - 8 * i));
        }
        return res;
      case 2:
        if (parts[0] > 255 || parts[1] > 0xFFFFFF) {
          return null;
        }
        res[0] = (byte) parts[0];
        res[1] = (byte) (parts[1] >> 16);
        res[2] = (byte) (parts[1] >> 8);
        res[3] = (byte) parts[1];
        return res;
      case 3:
        if (parts[0] > 255 || parts[1] > 255 || parts[2] > 0xFFFF) {
          return null;
        }
        res[0] = (byte) parts[0];
        res[1] = (byte) parts[1];
        res[2] = (byte) (parts[2] >> 8);
        res[3] = (byte) parts[2];
        return res;
      default:
        for (int i = 0; i < 4; i++) {
          if (parts[i] > 255) {
            return null;
          }
          res[i] = (byte) parts[i];
        }
        return res;
    }
  }

  static byte[] textToV6(String src) {
    if (src.length() < 2) {
      return null;
    }
    char[] chars = src.toCharArray();
    byte[] dst = new byte[16];
    int len = chars.length;
    int j = 0;
    int i = 0;
    int colonp = -1;
    if (chars[i] == ':' && chars[++i] != ':') {
      return null;
    }
    int curtok = i;
    boolean sawXdigit = false;
    int val = 0;
    int digits = 0;
    while (i < len) {
      char ch = chars[i++];
      int chval = Character.digit(ch, 16);
      if (chval != -1) {
        val <<= 4;
        val |= chval;
        digits++;
        if (digits > 4) {
          return null;
        }
        sawXdigit = true;
        continue;
      }
      if (ch == ':') {
        curtok = i;
        if (!sawXdigit) {
          if (colonp != -1) {
            return null;
          }
          colonp = j;
          continue;
        } else if (i == len) {
          return null;
        }
        if (j + 2 > 16) {
          return null;
        }
        dst[j++] = (byte) ((val >> 8) & 0xff);
        dst[j++] = (byte) (val & 0xff);
        sawXdigit = false;
        digits = 0;
        val = 0;
        continue;
      }
      if (ch == '.' && ((j + 4) <= 16)) {
        String tail = src.substring(curtok);
        byte[] v4 = tail.split("\\.", -1).length == 4 ? textToV4(tail) : null;
        if (v4 == null) {
          return null;
        }
        for (int k = 0; k < 4; k++) {
          dst[j++] = v4[k];
        }
        sawXdigit = false;
        break;
      }
      return null;
    }
    if (sawXdigit) {
      if (j + 2 > 16) {
        return null;
      }
      dst[j++] = (byte) ((val >> 8) & 0xff);
      dst[j++] = (byte) (val & 0xff);
    }
    if (colonp != -1) {
      int n = j - colonp;
      if (j == 16) {
        return null;
      }
      for (i = 1; i <= n; i++) {
        dst[16 - i] = dst[colonp + n - i];
        dst[colonp + n - i] = 0;
      }
      j = 16;
    }
    if (j != 16) {
      return null;
    }
    return dst;
  }

  public static InetAddress getLoopbackAddress() {
    return new Inet4Address("localhost", new byte[] {127, 0, 0, 1});
  }

  public static InetAddress getLocalHost() throws UnknownHostException {
    NetNative.init();
    String name = NetNative.localHostName();
    if (name == null || name.isEmpty() || name.equals("localhost")) {
      return getLoopbackAddress();
    }
    try {
      return getAllByName(name)[0];
    } catch (UnknownHostException e) {
      throw new UnknownHostException(name + ": " + e.getMessage());
    }
  }
}
