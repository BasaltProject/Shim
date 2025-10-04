/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

public final class Inet4Address extends InetAddress {
  private static final long serialVersionUID = 3286316764910316507L;

  static final int INADDRSZ = 4;

  Inet4Address(String hostName, byte[] addr) {
    holderHostName = hostName;
    holderAddress = addr;
  }

  @Override
  public boolean isMulticastAddress() {
    return (address() & 0xf0000000) == 0xe0000000;
  }

  @Override
  public boolean isAnyLocalAddress() {
    return address() == 0;
  }

  @Override
  public boolean isLoopbackAddress() {
    return (holderAddress[0] & 0xff) == 127;
  }

  @Override
  public boolean isLinkLocalAddress() {
    int address = address();
    return ((address >>> 24) & 0xFF) == 169 && ((address >>> 16) & 0xFF) == 254;
  }

  @Override
  public boolean isSiteLocalAddress() {
    int address = address();
    return ((address >>> 24) & 0xFF) == 10 || (((address >>> 24) & 0xFF) == 172 && ((address >>> 16) & 0xF0) == 16)
        || (((address >>> 24) & 0xFF) == 192 && ((address >>> 16) & 0xFF) == 168);
  }

  @Override
  public boolean isMCGlobal() {
    byte[] byteAddr = holderAddress;
    return ((byteAddr[0] & 0xff) >= 224 && (byteAddr[0] & 0xff) <= 238)
        && !((byteAddr[0] & 0xff) == 224 && byteAddr[1] == 0 && byteAddr[2] == 0);
  }

  @Override
  public boolean isMCNodeLocal() {
    return false;
  }

  @Override
  public boolean isMCLinkLocal() {
    return (address() >>> 8) == 0xE00000;
  }

  @Override
  public boolean isMCSiteLocal() {
    return ((address() >>> 16) & 0xFFFF) == 0xEFFF;
  }

  @Override
  public boolean isMCOrgLocal() {
    int address = address();
    return ((address >>> 16) & 0xFFFC) == 0xEFC0;
  }

  private int address() {
    byte[] a = holderAddress;
    return ((a[0] & 0xff) << 24) | ((a[1] & 0xff) << 16) | ((a[2] & 0xff) << 8) | (a[3] & 0xff);
  }

  @Override
  public byte[] getAddress() {
    return holderAddress.clone();
  }

  @Override
  public String getHostAddress() {
    return numericToString(holderAddress);
  }

  static String numericToString(byte[] src) {
    return (src[0] & 0xff) + "." + (src[1] & 0xff) + "." + (src[2] & 0xff) + "." + (src[3] & 0xff);
  }

  @Override
  public int hashCode() {
    return address();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Inet4Address && ((Inet4Address) obj).address() == address();
  }

  private static IllegalArgumentException invalidLiteral(String s) {
    return new IllegalArgumentException(("Invalid IP address literal: " + s.strip()).stripTrailing());
  }

  public static Inet4Address ofLiteral(String ipAddressLiteral) {
    if (ipAddressLiteral == null) {
      throw new NullPointerException();
    }
    byte[] v4 = ipAddressLiteral.indexOf(':') >= 0 ? null : InetAddress.textToV4(ipAddressLiteral);
    if (v4 == null) {
      throw invalidLiteral(ipAddressLiteral);
    }
    return new Inet4Address(null, v4);
  }

  public static Inet4Address ofPosixLiteral(String posixIPAddressLiteral) {
    if (posixIPAddressLiteral == null) {
      throw new NullPointerException();
    }
    String s = posixIPAddressLiteral;
    if (s.isEmpty()) {
      throw invalidLiteral(s);
    }
    String[] parts = s.split("\\.", -1);
    if (parts.length > 4) {
      throw invalidLiteral(s);
    }
    long[] values = new long[parts.length];
    for (int i = 0; i < parts.length; i++) {
      String p = parts[i];
      if (p.isEmpty()) {
        throw invalidLiteral(s);
      }
      int radix = 10;
      if (p.length() > 1 && p.charAt(0) == '0') {
        if (p.charAt(1) == 'x' || p.charAt(1) == 'X') {
          radix = 16;
          p = p.substring(2);
        } else {
          radix = 8;
          p = p.substring(1);
        }
      }
      if (p.isEmpty()) {
        throw invalidLiteral(s);
      }
      long value = 0;
      for (int k = 0; k < p.length(); k++) {
        int d = Character.digit(p.charAt(k), radix);
        if (d < 0 || p.charAt(k) > 127) {
          throw invalidLiteral(s);
        }
        value = value * radix + d;
        if (value > 0xFFFFFFFFL) {
          throw invalidLiteral(s);
        }
      }
      values[i] = value;
    }
    int n = parts.length;
    for (int i = 0; i < n - 1; i++) {
      if (values[i] > 255) {
        throw invalidLiteral(s);
      }
    }
    long lastMax = (1L << (8 * (5 - n))) - 1;
    if (values[n - 1] > lastMax) {
      throw invalidLiteral(s);
    }
    long address = 0;
    for (int i = 0; i < n - 1; i++) {
      address |= values[i] << (24 - 8 * i);
    }
    address |= values[n - 1];
    byte[] b = new byte[] {(byte) (address >>> 24), (byte) (address >>> 16), (byte) (address >>> 8),
        (byte) address};
    return new Inet4Address(null, b);
  }
}
