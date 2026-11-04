/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi.connect.spi;

public abstract class TransportService {
  public TransportService() {
  }

  public abstract com.sun.jdi.connect.spi.Connection accept(com.sun.jdi.connect.spi.TransportService.ListenKey listenKey, long l, long l2) throws java.io.IOException;
  public abstract com.sun.jdi.connect.spi.Connection attach(java.lang.String string, long l, long l2) throws java.io.IOException;
  public abstract com.sun.jdi.connect.spi.TransportService.Capabilities capabilities();
  public abstract java.lang.String description();
  public abstract java.lang.String name();
  public abstract com.sun.jdi.connect.spi.TransportService.ListenKey startListening() throws java.io.IOException;
  public abstract com.sun.jdi.connect.spi.TransportService.ListenKey startListening(java.lang.String string) throws java.io.IOException;
  public abstract void stopListening(com.sun.jdi.connect.spi.TransportService.ListenKey listenKey) throws java.io.IOException;

  public static abstract class Capabilities {
    public Capabilities() {
    }

    public abstract boolean supportsAcceptTimeout();
    public abstract boolean supportsAttachTimeout();
    public abstract boolean supportsHandshakeTimeout();
    public abstract boolean supportsMultipleConnections();
  }

  public static abstract class ListenKey {
    public ListenKey() {
    }

    public abstract java.lang.String address();
  }
}
