/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;
import shim.NetNative;

public final class NetworkInterface {
  private final String name;
  private final int index;
  private final int flags;
  private final byte[] hardware;
  private final List<InterfaceAddress> addresses = new ArrayList<InterfaceAddress>();

  private NetworkInterface(String name, int index, int flags, byte[] hardware) {
    this.name = name;
    this.index = index;
    this.flags = flags;
    this.hardware = hardware;
  }

  static NetworkInterface anyInterface() {
    InetAddress any = InetAddress.anyLocalAddress();
    NetworkInterface dummy = new NetworkInterface(any.getHostName(), 0, 0, null);
    dummy.addresses.add(new InterfaceAddress(any, null, (short) 0));
    return dummy;
  }

  private static List<NetworkInterface> snapshot() throws SocketException {
    NetNative.init();
    Object[] data = NetNative.interfaces();
    List<NetworkInterface> list = new ArrayList<NetworkInterface>();
    if (data == null || data.length < 7) {
      return list;
    }
    String[] names = (String[]) data[0];
    int[] info = (int[]) data[1];
    byte[][] hw = (byte[][]) data[2];
    byte[][] addrs = (byte[][]) data[3];
    int[] owner = (int[]) data[4];
    int[] prefix = (int[]) data[5];
    byte[][] bcast = (byte[][]) data[6];
    for (int i = 0; i < names.length; i++) {
      list.add(new NetworkInterface(names[i], info[2 * i], info[2 * i + 1], hw[i]));
    }
    for (int k = 0; k < addrs.length; k++) {
      try {
        InetAddress a = InetAddress.getByAddress(addrs[k]);
        Inet4Address b = bcast[k] == null ? null : (Inet4Address) InetAddress.getByAddress(bcast[k]);
        list.get(owner[k]).addresses.add(new InterfaceAddress(a, b, (short) prefix[k]));
      } catch (UnknownHostException ignore) { }
    }
    return list;
  }

  public String getName() {
    return name;
  }

  public Enumeration<InetAddress> getInetAddresses() {
    List<InetAddress> list = new ArrayList<InetAddress>();
    for (InterfaceAddress a : addresses) {
      list.add(a.getAddress());
    }
    return Collections.enumeration(list);
  }

  public Stream<InetAddress> inetAddresses() {
    List<InetAddress> list = new ArrayList<InetAddress>();
    for (InterfaceAddress a : addresses) {
      list.add(a.getAddress());
    }
    return list.stream();
  }

  public List<InterfaceAddress> getInterfaceAddresses() {
    return new ArrayList<InterfaceAddress>(addresses);
  }

  public Enumeration<NetworkInterface> getSubInterfaces() {
    return Collections.enumeration(new ArrayList<NetworkInterface>());
  }

  public Stream<NetworkInterface> subInterfaces() {
    return Stream.empty();
  }

  public NetworkInterface getParent() {
    return null;
  }

  public int getIndex() {
    return index;
  }

  public String getDisplayName() {
    return "".equals(name) ? null : name;
  }

  public static NetworkInterface getByName(String name) throws SocketException {
    if (name == null) {
      throw new NullPointerException();
    }
    for (NetworkInterface n : snapshot()) {
      if (n.name.equals(name)) {
        return n;
      }
    }
    return null;
  }

  public static NetworkInterface getByIndex(int index) throws SocketException {
    if (index < 0) {
      throw new IllegalArgumentException("Interface index can't be negative");
    }
    for (NetworkInterface n : snapshot()) {
      if (n.index == index) {
        return n;
      }
    }
    return null;
  }

  public static NetworkInterface getByInetAddress(InetAddress addr) throws SocketException {
    if (addr == null) {
      throw new NullPointerException();
    }
    if (!(addr instanceof Inet4Address || addr instanceof Inet6Address)) {
      throw new IllegalArgumentException("invalid address type");
    }
    for (NetworkInterface n : snapshot()) {
      for (InterfaceAddress a : n.addresses) {
        if (a.getAddress().equals(addr)) {
          return n;
        }
      }
    }
    return null;
  }

  public static Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
    List<NetworkInterface> list = snapshot();
    return list.isEmpty() ? null : Collections.enumeration(list);
  }

  public static Stream<NetworkInterface> networkInterfaces() throws SocketException {
    return snapshot().stream();
  }

  public boolean isUp() throws SocketException {
    return (flags & 0x1) != 0 && (flags & 0x40) != 0;
  }

  public boolean isLoopback() throws SocketException {
    return (flags & 0x8) != 0;
  }

  public boolean isPointToPoint() throws SocketException {
    return (flags & 0x10) != 0;
  }

  public boolean supportsMulticast() throws SocketException {
    return (flags & 0x1000) != 0;
  }

  public byte[] getHardwareAddress() throws SocketException {
    return hardware == null ? null : hardware.clone();
  }

  public int getMTU() throws SocketException {
    return isLoopback() ? 65536 : 1500;
  }

  public boolean isVirtual() {
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof NetworkInterface)) {
      return false;
    }
    NetworkInterface that = (NetworkInterface) obj;
    if (!name.equals(that.name)) {
      return false;
    }
    if (addresses.size() != that.addresses.size()) {
      return false;
    }
    for (InterfaceAddress a : addresses) {
      if (!that.addresses.contains(a)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return "name:" + name + " (" + getDisplayName() + ")";
  }
}
