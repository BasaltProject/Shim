/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.util;

public class HexDumpEncoder {
  public HexDumpEncoder() {
  }

  public String encodeBuffer(byte[] data) {
    StringBuilder sb = new StringBuilder();
    for (int offset = 0; offset < data.length; offset += 16) {
      int n = Math.min(16, data.length - offset);
      sb.append(hex4(offset)).append(": ");
      for (int i = 0; i < 16; i++) {
        if (i < n) {
          sb.append(hex2(data[offset + i])).append(' ');
        } else {
          sb.append("   ");
        }
        if (i == 7) {
          sb.append("  ");
        }
      }
      sb.append(' ');
      for (int i = 0; i < n; i++) {
        byte b = data[offset + i];
        sb.append((b >= ' ' && b <= 'z') ? (char) b : '.');
      }
      sb.append('\n');
    }
    return sb.toString();
  }

  public String encode(byte[] data) {
    return encodeBuffer(data);
  }

  public static String describe(String name, byte[] data) {
    return "\n    " + name + ":\n[" + new HexDumpEncoder().encodeBuffer(data) + "]\n";
  }

  private static String hex4(int v) {
    String s = Integer.toHexString(v & 0xffff).toUpperCase();
    while (s.length() < 4) {
      s = "0" + s;
    }
    return s;
  }

  private static String hex2(byte b) {
    String s = Integer.toHexString(b & 0xff).toUpperCase();
    return s.length() < 2 ? "0" + s : s;
  }
}
