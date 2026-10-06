/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.nio.sctp;

public abstract class PeerAddressChangeNotification implements com.sun.nio.sctp.Notification {
  protected PeerAddressChangeNotification() {
  }

  public abstract java.net.SocketAddress address();
  public abstract com.sun.nio.sctp.Association association();
  public abstract com.sun.nio.sctp.PeerAddressChangeNotification.AddressChangeEvent event();

  public enum AddressChangeEvent {
    ADDR_AVAILABLE, ADDR_UNREACHABLE, ADDR_REMOVED, ADDR_ADDED, ADDR_MADE_PRIMARY, ADDR_CONFIRMED;


  }
}
