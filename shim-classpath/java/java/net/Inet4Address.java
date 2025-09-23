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
}
