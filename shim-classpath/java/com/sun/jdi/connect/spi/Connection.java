/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi.connect.spi;

public abstract class Connection {
  public Connection() {
  }

  public abstract void close() throws java.io.IOException;
  public abstract boolean isOpen();
  public abstract byte[] readPacket() throws java.io.IOException;
  public abstract void writePacket(byte[] bs) throws java.io.IOException;
}
