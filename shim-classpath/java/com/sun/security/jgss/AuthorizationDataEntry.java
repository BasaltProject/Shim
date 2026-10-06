/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.jgss;

public final class AuthorizationDataEntry {
  private final int type;
  private final byte[] data;

  public AuthorizationDataEntry(int type, byte[] data) {
    this.type = type;
    this.data = data.clone();
  }

  public int getType() {
    return type;
  }

  public byte[] getData() {
    return data.clone();
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("AuthorizationDataEntry: type=" + type + ", data=" + data.length + " bytes:\n");
    for (int off = 0; off < data.length; off += 16) {
      sb.append(String.format("%04X: ", off));
      StringBuilder ascii = new StringBuilder();
      for (int i = 0; i < 16; i++) {
        if (off + i < data.length) {
          int b = data[off + i] & 0xff;
          sb.append(String.format("%02X ", b));
          ascii.append(b >= 0x20 && b < 0x7f ? (char) b : '.');
        } else {
          sb.append("   ");
        }
        if (i == 7) {
          sb.append("  ");
        }
      }
      sb.append(' ').append(ascii).append('\n');
    }
    return sb.toString();
  }
}
