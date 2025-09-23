/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.util.Objects;

public class InterfaceAddress {
  private InetAddress address;
  private Inet4Address broadcast;
  private short maskLength;

  InterfaceAddress(InetAddress address, Inet4Address broadcast, short maskLength) {
    this.address = address;
    this.broadcast = broadcast;
    this.maskLength = maskLength;
  }

  public InetAddress getAddress() {
    return address;
  }

  public InetAddress getBroadcast() {
    return broadcast;
  }

  public short getNetworkPrefixLength() {
    return maskLength;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof InterfaceAddress)) {
      return false;
    }
    InterfaceAddress cmp = (InterfaceAddress) obj;
    return Objects.equals(address, cmp.address) && Objects.equals(broadcast, cmp.broadcast)
        && maskLength == cmp.maskLength;
  }

  @Override
  public int hashCode() {
    return address.hashCode() + ((broadcast != null) ? broadcast.hashCode() : 0) + maskLength;
  }

  @Override
  public String toString() {
    return address + "/" + maskLength + " [" + broadcast + "]";
  }
}
